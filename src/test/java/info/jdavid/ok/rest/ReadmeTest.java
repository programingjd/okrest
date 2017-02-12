package info.jdavid.ok.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import info.jdavid.ok.json.Builder;
import info.jdavid.ok.json.Parser;
import info.jdavid.ok.server.MediaTypes;
import info.jdavid.ok.server.Response;
import info.jdavid.ok.server.StatusLines;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okio.Buffer;
import org.junit.Test;


public class ReadmeTest {

  @Test @SuppressWarnings("GroovyUnusedAssignment")
  public void test1() {
    String json =
      "[{\"name\":\"a\", \"value\":\"1\"},{\"name\":\"b\", \"value\":\"2\"},{\"name\":\"c\", \"value\":\"3\"}]";

    List<Map<String, ?>> data = Collections.synchronizedList(Parser.parse(json));
    final RestServer server = new RestServer();
    server.get("/data", () -> new Response.Builder().
        statusLine(StatusLines.OK).
        body(MediaTypes.JSON, Builder.build(data)).
        build()
    );
    server.get("/data/([a-z]+)", (Buffer b, Headers h, List<String> c) -> {
                 Response.Builder builder = new Response.Builder();
                 return data.stream().
                   filter(it -> it.get("name").equals(c.get(0))).
                   map(it -> builder.
                     statusLine(StatusLines.OK).
                     body(MediaTypes.JSON, Builder.build(it)).
                     build()
                   ).
                   findFirst().
                   orElseGet(() -> builder.
                     statusLine(StatusLines.NOT_FOUND).
                     noBody().
                     build()
                   );
               }
    );
    server.post("/data", (Buffer b, Headers h, List<String> c, HttpUrl url, String clientIp) -> {
                  Response.Builder builder = new Response.Builder();
                  if (MediaType.parse(h.get("Content-Type")) == MediaTypes.JSON) {
                    try {
                      Map<String, String> obj = Parser.parse(b);
                      if (obj.get("name") == null || obj.get("name").length() == 0 ||
                          obj.get("value") == null || obj.get("value").length() == 0 ||
                          obj.get("name") == null || !obj.get("name").matches("[a-z]+")) {
                        throw new RuntimeException();
                      }
                      data.add(obj);
                      builder.
                        statusLine(StatusLines.OK).
                        noBody();
                    }
                    catch (final RuntimeException ignore) {
                      builder.
                        statusLine(StatusLines.BAD_REQUEST).
                        noBody();
                    }
                  }
                  else {
                    builder.
                      statusLine(StatusLines.BAD_REQUEST).
                      noBody();
                  }
                  return builder.build();
                }
    );
    server.delete("/data/([a-z]+)", (Buffer b, Headers h, List<String> c) -> {
      Response.Builder builder = new Response.Builder();
      return data.stream().
        filter(it -> it.get("name").equals(c.get(0))).
        findFirst().
        map(it -> {
          data.remove(it);
          return builder.
            statusLine(StatusLines.OK).
            noBody().
            build();
        }).
        orElseGet(() -> builder.
          statusLine(StatusLines.NO_CONTENT).
          noBody().
          build()
        );
    });
  }

}
