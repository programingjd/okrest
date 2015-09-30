package info.jdavid.ok.rest

import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import info.jdavid.ok.server.MediaTypes
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import java.util.concurrent.TimeUnit


class RestServerTest {

  private static Request.Builder request(final String... segments) {
    HttpUrl.Builder url = new HttpUrl.Builder().scheme('http').host('localhost').port(8080)
    segments.each { url.addPathSegment(it) }
    new Request.Builder().url(url.build())
  }

  private static OkHttpClient client() {
    new OkHttpClient().with { setReadTimeout(0, TimeUnit.SECONDS); it }
  }

  private RestServer server() {
    mServer.clearHandlers()
  }

  private RestServer mServer = null;

  @Before
  public void setUp() {
    mServer = new RestServer().with { port(8080); it }
  }

  @After
  public void tearDown() {
    mServer.shutdown()
  }

  @Test
  public void test1() {
    def testHandler = { new Response.Builder().statusLine(StatusLines.OK).noBody().build() }
    server().get('/test', testHandler).with {
      start()
      def r = client().newCall(request('test').get().build()).execute()
      assertEquals(200, r.code())
      shutdown()
    }
  }

  @Test
  public void test2() {
    def testHandler = { new Response.Builder().statusLine(StatusLines.OK).noBody().build() }
    server().test('/test', testHandler).with {
      start()
      def r = client().newCall(request('test').method('TEST', RequestBody.create(MediaTypes.TEXT, 'a')).build()).execute()
      assertEquals(200, r.code())
      shutdown()
    }
  }

}
