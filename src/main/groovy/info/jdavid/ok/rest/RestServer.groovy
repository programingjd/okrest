package info.jdavid.ok.rest

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import info.jdavid.ok.server.HttpServer
import info.jdavid.ok.server.RequestHandler
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okhttp3.Headers
import okhttp3.HttpUrl
import okio.Buffer

import java.util.regex.Pattern

@CompileStatic
public class RestServer extends HttpServer {

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

  public RestServer() {
    super()
    super.requestHandler(new RequestHandler() {
      @Override public Response handle(final String clientIp, final boolean secure,
                                       final String method, final HttpUrl url,
                                       final Headers requestHeaders, final Buffer requestBody) {
        def methodHandlers = handlers[method]
        final String path = url.encodedPath()
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
              switch (value.maximumNumberOfParameters) {
                case 0: return value()
                case 1: return value(requestBody)
                case 2: return value(requestBody, requestHeaders)
                case 3: return value(requestBody, requestHeaders, groups)
                case 4: return value(requestBody, requestHeaders, groups, url)
                default: return value(requestBody, requestHeaders, groups, url, clientIp)
              }
            }
            catch (Exception exception) {
              exception.printStackTrace()
              return new Response.Builder().statusLine(StatusLines.INTERNAL_SERVER_ERROR).noBody().build()
            }
          }
          else return null
        } as Response
        return response ?: new Response.Builder().statusLine(StatusLines.NOT_FOUND).noBody().build()
      }
    })
  }

  @PackageScope RestServer clearHandlers() {
    handlers.clear()
    return this
  }

  public RestServer options(final String pattern, final Closure<Response> closure) {
    return register('OPTIONS', pattern, closure)
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

}
