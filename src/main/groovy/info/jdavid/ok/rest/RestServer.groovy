package info.jdavid.ok.rest

import com.squareup.okhttp.Headers
import groovy.transform.CompileStatic
import info.jdavid.ok.server.HttpServer
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okio.Buffer

import java.util.regex.Pattern

@CompileStatic
class RestServer extends HttpServer {

  private final Map<String, Map<Pattern, Closure<Response>>> handlers = [:]

  private RestServer register(final String key, final String pattern, final Closure<Response> closure) {
    (handlers[key] ?: newMap(key))[~pattern] = closure
    return this
  }

  private Map<Pattern, Closure<Response>> newMap(final String key) {
    final Map<Pattern, Closure<Response>> map = [:]
    handlers[key] = map
    return map
  }

  RestServer clearHandlers() {
    handlers.clear()
    return this
  }

  protected void setup() {
    super.setup()
  }

  public RestServer head(final String pattern, final Closure<Response> closure) {
    return register('HEAD', pattern, closure)
  }

  public RestServer get(final String pattern, final Closure<Response> closure) {
    return register('GET', pattern, closure)
  }

  public RestServer post(final String pattern, final Closure<Response> closure) {
    return register('POST', pattern, closure)
  }

  public RestServer put(final String pattern, final Closure<Response> closure) {
    return register('PUT', pattern, closure)
  }

  public RestServer delete(final String pattern, final Closure<Response> closure) {
    return register('DELETE', pattern, closure)
  }

  public RestServer patch(final String pattern, final Closure<Response> closure) {
    return register('PATCH', pattern, closure)
  }

  @Override
  protected Response handle(final String method, final String path,
                            final Headers requestHeaders, final Buffer requestBody) {
    def methodHandlers = handlers[method]
    final Response response = methodHandlers == null ? null : methodHandlers.findResult { key, value ->
      def matcher = path =~ key
      if (matcher.matches()) {
        final List<String> groups
        if (matcher[0] instanceof List) {
          groups = (matcher[0] as List<String>).drop(1)
        }
        else {
          groups = []
        }
        try {
          return value(requestBody, requestHeaders, groups)
        }
        catch (Exception exception) {
          exception.printStackTrace()
          return new Response.Builder().statusLine(StatusLines.INTERNAL_SERVER_ERROR).noBody().build()
        }
      }
    } as Response
    return response ?: new Response.Builder().statusLine(StatusLines.NOT_FOUND).noBody().build()
  }

}
