package info.jdavid.ok.rest

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import groovy.transform.CompileStatic
import info.jdavid.ok.json.Builder
import info.jdavid.ok.json.Parser
import info.jdavid.ok.server.MediaTypes
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okio.Buffer
import org.junit.Test

@CompileStatic
public class ReadmeTest {

  @Test @SuppressWarnings("GroovyUnusedAssignment")
  public void test1() {
    List data = Collections.synchronizedList([
      [name: 'a', value: '1'],
      [name: 'b', value: '2'],
      [name: 'c', value: '3']
    ])
    def server = new RestServer().with {
      get('/data') { ->
        return new Response.Builder().statusLine(StatusLines.OK).
          body(MediaTypes.JSON, Builder.build(data)).build()
      }
      get('/data/([a-z]+)') { Buffer b, Headers h, List<String> c ->
        def builder = new Response.Builder()
        def found = data.find { it['name'] == c[0] } as Map
        if (found) {
          builder.statusLine(StatusLines.OK).
            body(MediaTypes.JSON, Builder.build(found))
        }
        else {
          builder.statusLine(StatusLines.NOT_FOUND).noBody()
        }
        return builder.build()
      }
      post('/data') { Buffer b, Headers h, List<String> c, HttpUrl url, String clientIp ->
        def builder = new Response.Builder()
        if (MediaType.parse(h.get('Content-Type')) == MediaTypes.JSON) {
          try {
            def obj = Parser.parse(b) as Map
            if (!(obj['name'] && obj['value'] && obj['name'] ==~ /[a-z]+/)) {
              throw new RuntimeException()
            }
            data.add(obj)
            builder.statusLine(StatusLines.OK).noBody()
          }
          catch (ignore) {
            builder.statusLine(StatusLines.BAD_REQUEST).noBody()
          }
        }
        else {
          builder.statusLine(StatusLines.BAD_REQUEST).noBody()
        }
        return builder.build()
      }
      delete('/data/([a-z]+)') { Buffer b, Headers h, List<String> c ->
          def builder = new Response.Builder()
          def found = data.find { it['name'] == c[0] } as Map
          if (found) {
            data.remove(found)
            builder.statusLine(StatusLines.OK).noBody()
          }
          else {
            builder.statusLine(StatusLines.NO_CONTENT).noBody()
          }
          return builder.build()
      }
    }
  }

}
