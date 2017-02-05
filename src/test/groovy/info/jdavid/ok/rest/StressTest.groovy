package info.jdavid.ok.rest

import groovy.transform.CompileStatic
import info.jdavid.ok.server.Dispatcher
import info.jdavid.ok.server.MediaTypes
import info.jdavid.ok.server.Response
import info.jdavid.ok.server.StatusLines
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.Okio
import okio.Source
import okio.Timeout

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


@CompileStatic
public class StressTest {

  private static final int seconds = 15

  public static class Server extends RestServer {

    public Server() {
      port(8080)
      this.dispatcher(new Dispatcher.Logged())
    }

    @Override protected void setup() {
      get('/', {  ->
        final long start = System.currentTimeMillis()
        return new Response.Builder().statusLine(StatusLines.OK).chunks(MediaTypes.TEXT,
                         (1..seconds).collect { new TestBody(start, it) } as ResponseBody[]).build()
      })
      get('/loaderio-e5904ad6382227a74edb74c73ad8e603.txt', { ->
        return new Response.Builder().statusLine(StatusLines.OK).body('loaderio-e5904ad6382227a74edb74c73ad8e603').build()
      })
    }

    public static void main(final String[] args) {
      new Server().start()
    }

  }

  public static class Clients {

    public static void main(final String[] args) {
      final int n = 1000
      final ExecutorService service = Executors.newFixedThreadPool(n)
      final OkHttpClient.Builder client =
        new OkHttpClient.Builder().readTimeout(seconds * 2, TimeUnit.SECONDS)
      final Request.Builder request = new Request.Builder().url('http://localhost:8080/')
      final AtomicInteger successes = new AtomicInteger()
      final AtomicInteger failures = new AtomicInteger()
      final List<Runnable> tasks = (1..n).collect {
        return {
          final okhttp3.Response response = client.build().newCall(request.build()).execute()
          //if (response.isSuccessful() && response.body().string().size() == seconds) {
          if (response.isSuccessful()) {
            final BufferedSource body = response.body().source()
            for (int i=0; i<seconds; ++i) {
              print body.readByte() as char
            }
            body.close()
            successes.incrementAndGet()
          }
          else {
            response.body().close()
            failures.incrementAndGet()
          }
        } as Runnable
      }
      tasks.each {
        service.submit(it)
      }
      service.shutdown()
      service.awaitTermination(60, TimeUnit.SECONDS)
      println()
      println "${successes.get()} successes"
      println "${failures.get()} failures"
    }

  }

  private static class TestBody extends ResponseBody {

    private final long start
    private final int n

    public TestBody(final long start, final int n) {
      this.start = start
      this.n = n
    }

    @Override public MediaType contentType() {
      return MediaTypes.TEXT
    }

    @Override public long contentLength() {
      return 1
    }

    @Override public BufferedSource source() {
      return Okio.buffer(new TestSource(start, n))
    }

  }

  private static class TestSource implements Source {

    private final long start
    private final int n
    private boolean done = false

    public TestSource(final long start, final int n) {
      this.start = start
      this.n = n
    }

    @Override public long read(final Buffer sink, final long byteCount) throws IOException {
      if (done) throw new EOFException()
      if (byteCount <= 0L) return 0
      final long now = System.currentTimeMillis()
      final time = start + n * 1000L - now
      if (time > 0L) Thread.sleep(time)
      sink.write(new byte[1].with { it[0] = (96 + n) as byte; it })
      done = true
      return 1
    }

    @Override public Timeout timeout() {
      return Timeout.NONE
    }

    @Override public void close() throws IOException {
      done = true
    }

  }

}
