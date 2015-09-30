package info.jdavid.ok.rest

import com.squareup.okhttp.Headers
import info.jdavid.ok.server.HttpServer
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okio.Buffer

import java.util.regex.Pattern

trait ServerTrait {
  abstract RestServer method(String name, String pattern, Closure<Response> closure)
  def methodMissing(String name, args) {
    return method(name, *args)
  }
}

class RestServer extends HttpServer implements ServerTrait {

  private Map<String, Map<Pattern, Closure<Response>>> handlers = [:]

  private def register(String key, String pattern, Closure<Response> closure) {
    (handlers[key] ?: [:].with { this.handlers[key] = it } )[~pattern] = closure
  }

  RestServer clearHandlers() {
    handlers.clear()
    return this
  }

  public RestServer head(String pattern, Closure<Response> closure) {
    register('HEAD', pattern, closure)
    return this
  }

  public RestServer get(String pattern, Closure<Response> closure) {
    register('GET', pattern, closure)
    return this
  }

  public RestServer post(String pattern, Closure<Response> closure) {
    register('POST', pattern, closure)
    return this
  }

  public RestServer put(String pattern, Closure<Response> closure) {
    register('PUT', pattern, closure)
    return this
  }

  public RestServer delete(String pattern, Closure<Response> closure) {
    register('DELETE', pattern, closure)
    return this
  }

  public RestServer patch(String pattern, Closure<Response> closure) {
    register('PATCH', pattern, closure)
    return this
  }

  RestServer method(String method, String pattern, Closure<Response> closure) {
    register(method.toUpperCase(), pattern, closure)
    return this
  }

  @Override
  protected Response handle(String method, String path, Headers requestHeaders, Buffer requestBody) {
    for (def handler in handlers[method]) {
      def matcher = path =~ handler.key
      if (matcher.matches()) {
        return handler.value(matcher[0].drop(1))
      }
    }
    return new Response.Builder().statusLine(StatusLines.NOT_FOUND).noBody().build()
  }

}
