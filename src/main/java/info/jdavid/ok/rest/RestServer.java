package info.jdavid.ok.rest;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.jdavid.ok.server.HttpServer;
import info.jdavid.ok.server.Response;
import info.jdavid.ok.server.StatusLines;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okio.Buffer;

@SuppressWarnings("unused")
public class RestServer extends HttpServer {

  private static interface F {}

  @FunctionalInterface
  public static interface Function5<Buffer, Headers, List, HttpUrl, String, Response> extends F {
    public Response apply(final Buffer body, final Headers headers, final List groups,
                          final HttpUrl url, final String clientIp);
  }

  @FunctionalInterface
  public static interface Function4<Buffer, Headers, List, HttpUrl, Response> extends F {
    public Response apply(final Buffer body, final Headers headers, final List groups, final HttpUrl url);
  }

  @FunctionalInterface
  public static interface Function3<Buffer, Headers, List, Response> extends F {
    public Response apply(final Buffer body, final Headers headers, final List groups);
  }

  @FunctionalInterface
  public static interface Function2<Buffer, Headers, Response> extends F {
    public Response apply(final Buffer body, final Headers headers);
  }

  @FunctionalInterface
  public static interface Function1<Buffer, Response> extends F {
    public Response apply(final Buffer body);
  }

  @FunctionalInterface
  public static interface Function0<Response> extends F {
    public Response apply();
  }

  public RestServer() {
    super();
    super.requestHandler((clientIp, secure, method, url, requestHeaders, requestBody) -> {
      final Set<Map.Entry<Pattern, F>> methodHandlers = handlers.get(method).entrySet();
      final String path = url.encodedPath();
      return methodHandlers.stream().
        map(it -> entry(it.getKey().matcher(path), it.getValue())).
        filter(it -> it.getKey().matches()).
        map(it -> {
          try {
            final F f = it.getValue();
            if (f instanceof Function0) {
              //noinspection unchecked
              return ((Function0<Response>)f).
                apply();
            }
            else if (f instanceof Function1) {
              //noinspection unchecked
              return ((Function1<Buffer, Response>)f).
                apply(requestBody);
            }
            else if (f instanceof Function2) {
              //noinspection unchecked
              return ((Function2<Buffer, Headers, Response>)f).
                apply(requestBody, requestHeaders);
            }
            else {
              final Matcher matcher = it.getKey();
              final int n = matcher.groupCount();
              final List<String> groups = new ArrayList<>(n);
              for (int i = 1; i <= n; ++i) {
                groups.add(matcher.group(i));
              }
              if (f instanceof Function3) {
                //noinspection unchecked
                return ((Function3<Buffer, Headers, List, Response>)f).
                  apply(requestBody, requestHeaders, groups);
              }
              else if (f instanceof Function4) {
                //noinspection unchecked
                return ((Function4<Buffer, Headers, List, HttpUrl, Response>)f).
                  apply(requestBody, requestHeaders, groups, url);
              }
              else if (f instanceof Function5) {
                //noinspection unchecked
                return ((Function5<Buffer, Headers, List, HttpUrl, String, Response>)f).
                  apply(requestBody, requestHeaders, groups, url, clientIp);
              }
            }
          }
          catch (final Exception e) {
            e.printStackTrace();
          }
          return new Response.Builder().statusLine(StatusLines.INTERNAL_SERVER_ERROR).noBody().build();
        }).
        findFirst().
        orElseGet(() -> new Response.Builder().statusLine(StatusLines.NOT_FOUND).noBody().build());
    });
  }

  public RestServer options(final String pattern,
                            final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer options(final String pattern,
                            final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer options(final String pattern,
                            final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer options(final String pattern,
                            final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer options(final String pattern,
                            final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer options(final String pattern,
                            final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("OPTIONS", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer head(final String pattern,
                         final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("HEAD", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer get(final String pattern,
                        final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("GET", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer post(final String pattern,
                         final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("POST", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer put(final String pattern,
                        final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PUT", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer delete(final String pattern,
                           final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("DELETE", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function0<Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function1<Buffer, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function2<Buffer, Headers, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function3<Buffer, Headers, List<String>, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function4<Buffer, Headers, List<String>, HttpUrl, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  public RestServer patch(final String pattern,
                          final Function5<Buffer, Headers, List<String>, HttpUrl, String, Response> f) {
    if (pattern == null) throw new NullPointerException();
    if (f == null) throw new NullPointerException();
    return register("PATCH", pattern, f);
  }

  private RestServer register(final String key, final String pattern, final F f) {
    final Map<Pattern, F> map = handlers.computeIfAbsent(key, notUsed -> new LinkedHashMap<>());
    map.put(Pattern.compile(pattern), f);
    return this;
  }

  private final Map<String, Map<Pattern, F>> handlers = new HashMap<>();

  private static <K, V> Map.Entry<K, V> entry(final K key, final V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  RestServer clearHandlers() {
    handlers.clear();
    return this;
  }

}

