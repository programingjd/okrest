package info.jdavid.ok.rest;

import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.internal.Platform;
import com.squareup.okhttp.internal.SslContextBuilder;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.framed.FramedConnection;
import com.squareup.okhttp.internal.framed.FramedStream;
import com.squareup.okhttp.internal.framed.Header;
import com.squareup.okhttp.internal.framed.IncomingStreamHandler;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


public class HttpServer {

  private static final List<Protocol> PROTOCOLS = Util.immutableList(Protocol.HTTP_2, Protocol.SPDY_3);

  private static void log(final String...values) {
    switch (values.length) {
      case 0:
        return;
      case 1:
        System.out.println(values[0]);
        return;
      case 2:
        System.out.println(values[0] + ": " + values[1]);
        return;
      default:
        boolean addSeparator = false;
        for (final String value: values) {
          if (addSeparator || (addSeparator = !addSeparator)) System.out.print(' ');
          System.out.print(value);
        }
        System.out.println();
    }
  }

  private static void log(final Throwable t) {
    t.printStackTrace();
  }

  private boolean started = false;
  private int port = 8080;
  private IncomingStreamHandler handler = new IncomingStreamHandler() {
    @Override public void receive(final FramedStream stream) throws IOException {
      HttpServer.this.handle(stream);
    }
  };

  /**
   * Sets the port number for the server.
   * @param port the port number.
   * @return this.
   */
  public HttpServer port(final int port) {
    if (started) throw new IllegalStateException("The port number cannot be changed while the server is running.");
    this.port = port;
    return this;
  }

  /**
   * Starts the server.
   */
  public void start() {
    if (started) throw new IllegalStateException("The server has already been started.");
    new Thread() {
      @Override public void run() {

        try {
          final ServerSocket serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);
          final Socket socket = serverSocket.accept();
          final SSLSocket sslSocket =
              (SSLSocket)SslContextBuilder.localhost().getSocketFactory().createSocket(
                  socket,
                  socket.getInetAddress().getHostAddress(),
                  socket.getPort(),
                  true);
          sslSocket.setUseClientMode(false);
          Platform.get().configureTlsExtensions(sslSocket, null, PROTOCOLS);
          sslSocket.startHandshake();
          final Protocol protocol = getSelectedProtocol(sslSocket);
          final FramedConnection connection =
              new FramedConnection.Builder(false, sslSocket).protocol(protocol).handler(handler).build();
          connection.sendConnectionPreface();
        }
        catch (final IOException e) {
          log(e);
        }
      }
    }.start();
  }

  private void handle(final FramedStream stream) throws IOException {
    final List<Header> headers = stream.getRequestHeaders();
    if (headers != null) {
      for (final Header header : headers) {
        log(header.name.utf8(), header.value.utf8());
      }
    }
  }

  private void send404(final FramedStream stream, final String path) throws IOException {
    final List<Header> headers = Util.immutableList(new Header(":status", "404"), new Header(":version", "HTTP/1.1"));
    stream.reply(headers, true);
  }

  private Protocol getSelectedProtocol(final SSLSocket sslSocket) {
    final String name = Platform.get().getSelectedProtocol(sslSocket);
    if (name == null) return null;
    try {
      return Protocol.get(name);
    }
    catch (final IOException e) {
      log(e);
      return null;
    }
  }



}
