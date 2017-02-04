package info.jdavid.ok.rest

import groovy.transform.CompileStatic
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import info.jdavid.ok.server.MediaTypes
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okio.Buffer
import okio.BufferedSource
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

@CompileStatic
public class RestServerTest {

  private static Request.Builder request(final String... segments) {
    final HttpUrl.Builder url = new HttpUrl.Builder().scheme('http').host('localhost').port(8080)
    segments.each { url.addPathSegment(it as String) }
    new Request.Builder().url(url.build())
  }

  private static Request.Builder requestWithQuery(final Map<String, String> query, final String... segments) {
    HttpUrl.Builder url = new HttpUrl.Builder().scheme('http').host('localhost').port(8080)
    segments.each { url.addPathSegment(it as String) }
    query.each { url.addQueryParameter(it.key, it.value) }
    new Request.Builder().url(url.build())
  }

  private static OkHttpClient client =
    new OkHttpClient.Builder().readTimeout(0, TimeUnit.SECONDS).build()

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
  public void testOptions() {
    def testHandler = { ->
      new Response.Builder().statusLine(StatusLines.OK).
        cors('*', [ 'PUT' ]).
        header('test', 'ok').noBody().build()
    }
    server().options('/test', testHandler).with {
      def r = client.newCall(
        request('test').
          method('OPTIONS', RequestBody.create(MediaTypes.OCTET_STREAM, new byte[0])).
          build()
      ).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('0', r.header('Content-Length'))
      assertEquals('', r.body().string())
      assertEquals('ok', r.header('test'))
      assertEquals('*', r.header('Access-Control-Allow-Origin'))
      assertEquals('PUT', r.header('Access-Control-Allow-Methods'))
      it
    }.with {
      def r = client.newCall(request('test2').head().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(404, r.code())
      it
    }
  }

  @Test
  public void testHead() {
    def testHandler = { ->
      new Response.Builder().statusLine(StatusLines.OK).header('test', 'ok').noBody().build()
    }
    server().head('/test', testHandler).with {
      def r = client.newCall(request('test').head().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('0', r.header('Content-Length'))
      assertEquals('', r.body().string())
      assertEquals('ok', r.header('test'))
      it
    }.with {
      def r = client.newCall(request('test2').head().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(404, r.code())
      it
    }
  }

  @Test
  public void testGet() {
    def testHandler = { ->
      new Response.Builder().statusLine(StatusLines.OK).noBody().build()
    }
    server().get('/test', testHandler).with {
      def r = client.newCall(request('test').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('0', r.header('Content-Length'))
      assertEquals('', r.body().string())
      it
    }.with {
      def r = client.newCall(request('test2').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(404, r.code())
      it
    }
  }

  @Test
  public void testGetQueryInUrl() {
    def testHandler = { Buffer body, Headers headers, List<String> captures, HttpUrl url ->
      new Response.Builder().statusLine(StatusLines.OK).body(url.queryParameter('test')).build()
    }
    server().get('/test/q', testHandler).with {
      def q = [ test: 'query' ]
      def r = client.newCall(requestWithQuery(q, 'test', 'q').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('5', r.header('Content-Length'))
      assertEquals('query', r.body().string())
      it
    }
  }

  @Test
  public void testGetParamsInUrl() {
    def testHandler = { Buffer body, Headers headers, List<String> captures ->
      new Response.Builder().statusLine(StatusLines.OK).body(captures[0]).build()
    }
    server().get('/test/([a-z]+)', testHandler).with {
      def r = client.newCall(request('test', 'abc').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('3', r.header('Content-Length'))
      assertEquals('abc', r.body().string())
      it
    }.with {
      def r = client.newCall(request('test/_').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(404, r.code())
      it
    }
  }

  @Test
  public void testPostParamsInUrl() {
    def testHandler = { Buffer body, Headers headers, List<String> captures ->
      Map<String, Object> json = new JsonSlurper().parseText(body.readUtf8()) as Map<String, Object>
      json.key1 = captures[0]
      json.key2 = captures[1]
      new Response.Builder().statusLine(StatusLines.OK).
        body(new JsonBuilder(json).toString(), MediaTypes.JSON).build()
    }
    server().post('/test/([a-z]+)/([0-9]+)', testHandler).with {
      def body = RequestBody.create(MediaTypes.JSON, '{"key1":null,"key2":null}')
      def r = client.newCall(request('test', 'abc', '123').post(body).build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals('27', r.header('Content-Length'))
      assertTrue(r.header('Content-Type').startsWith('application/json'))
      assertEquals('{"key1":"abc","key2":"123"}', r.body().string())
      it
    }.with {
      def body = RequestBody.create(MediaTypes.JSON, '')
      def r = client.newCall(request('test', 'abc', '123').post(body).build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(500, r.code())
      it
    }.with {
      def r = client.newCall(request('test', 'abc', '123').get().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(404, r.code())
      it
    }
  }

  @Test
  public void testPut() {
    def testHandler = { Buffer body, Headers headers, List<String> captures ->
      new Response.Builder().statusLine(StatusLines.OK).
        body(new ResponseBody() {
          @Override MediaType contentType() {
            return MediaType.parse(headers.get('Content-Type'))
          }
          @Override long contentLength() throws IOException {
            return Long.parseLong(headers.get('Content-Length'))
          }
          @Override BufferedSource source() throws IOException {
            return body
          }
        }).build()
    }
    server().put('/test', testHandler).with {
      String json = '{"key1":"value1"}'
      def body = RequestBody.create(MediaTypes.JSON, json)
      def r = client.newCall(request('test').put(body).build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(200, r.code())
      assertEquals("${json.length()}" as String, r.header('Content-Length'))
      assertTrue(r.header('Content-Type').startsWith('application/json'))
      assertEquals(json, r.body().string())
      it
    }
  }

  @Test
  public void testDelete() {
    def testHandler = {  Buffer body, Headers headers, List<String> captures ->
      new Response.Builder().statusLine(StatusLines.FORBIDDEN).noBody().build()
    }
    server().delete('/test', testHandler).with {
      def r = client.newCall(request('test').delete().build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(403, r.code())
    }
  }

  @Test
  public void testPatch() {
    def text = 'Forbidden'
    def testHandler = {  Buffer body, Headers headers, List<String> captures ->
      new Response.Builder().statusLine(StatusLines.FORBIDDEN).body(text).build()
    }
    server().patch('/test', testHandler).with {
      def body = RequestBody.create(MediaTypes.TEXT, 'test')
      def r = client.newCall(request('test').patch(body).build()).execute()
      assertEquals(Protocol.HTTP_1_1, r.protocol())
      assertEquals(403, r.code())
      assertEquals("${text.length()}" as String, r.header('Content-Length'))
      assertTrue(r.header('Content-Type').startsWith('text/plain'))
      assertEquals(text, r.body().string())
    }
  }

}
