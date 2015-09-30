package info.jdavid.ok.rest

import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Protocol
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import groovy.json.JsonBuilder
import groovy.json.JsonParser
import groovy.json.JsonSlurper
import info.jdavid.ok.server.MediaTypes
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okio.Buffer
import org.junit.AfterClass
import org.junit.BeforeClass
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

  private static RestServer server() {
    mServer.clearHandlers()
  }

  private static RestServer mServer = null;

  @BeforeClass
  public static void setUp() {
    mServer = new RestServer().with { port(8080); start(); it }
  }

  @AfterClass
  public static void tearDown() {
    mServer.shutdown()
  }

  @Test
  public void test1() {
    def testHandler = { a, b ->
      new Response.Builder().statusLine(StatusLines.OK).noBody().build()
    }
    server().get('/test', testHandler).with {
      def r = client().newCall(request('test').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('0', r.header('Content-Length'))
      assertEquals('', r.body().string())
    }
  }

  @Test
  public void test2() {
    def testHandler = { Buffer body, List<String> it ->
      new Response.Builder().statusLine(StatusLines.OK).body(it[0]).build()
    }
    server().get('/test/([a-z]+)', testHandler).with {
      def r = client().newCall(request('test', 'abc').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('3', r.header('Content-Length'))
      assertEquals('abc', r.body().string())
    }
  }

  @Test
  public void test3() {
    def testHandler = { Buffer body, List<String> it ->
      def json = new JsonSlurper().parseText(body.readUtf8())
      json.key1 = it[0]
      json.key2 = it[1]
      new Response.Builder().statusLine(StatusLines.OK).
        body(new JsonBuilder(json).toString(), MediaTypes.JSON).build()
    }
    server().post('/test/([a-z]+)/([0-9]+)', testHandler).with {
      def body = RequestBody.create(MediaTypes.JSON, '{"key1":null,"key2":null}')
      def r = client().newCall(request('test', 'abc', '123').post(body).build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('27', r.header('Content-Length'))
      assertTrue(r.header('Content-Type').startsWith('application/json'))
      assertEquals('{"key1":"abc","key2":"123"}', r.body().string())
    }
  }

}
