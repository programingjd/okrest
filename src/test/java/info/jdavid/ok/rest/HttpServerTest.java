package info.jdavid.ok.rest;

import com.squareup.okhttp.CipherSuite;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;


public class HttpServerTest {

  @Test
  public void test1() throws IOException {
    new HttpServer().port(8080).start();
    final Request request = new Request.Builder().url("https://localhost:8080").header("key1", "val1").build();

    final ConnectionSpec spec =
        new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(TlsVersion.TLS_1_2).
            cipherSuites(CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
                         CipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,
                         CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256).build();
    final OkHttpClient client = new OkHttpClient().setConnectionSpecs(Collections.singletonList(spec));
    final Response response = client.newCall(request).execute();
    assertEquals(200, response.code());
  }

}
