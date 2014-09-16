package io.vertx.examples;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExamplesTest extends ConversionTestBase {

  private Error failure(Throwable cause) {
    AssertionError afe = new AssertionError();
    afe.initCause(cause);
    return afe;
  }

  private Error failure(String message) {
    return new AssertionError(message);
  }

  private Map<String, String> assertCompile(String... sources) {
    return assertCompile(new JavaScriptLang(), sources);
  }

  private Map<String, String> assertCompile(Lang lang, String... sources) {
    try {
      return ConvertingProcessor.convert(ExamplesTest.class.getClassLoader(), lang, sources);
    } catch (Exception e) {
      throw failure(e);
    }
  }
  
  private void assertCompileAll(String... sources) throws IOException {
    for (Lang lang : langs()) {
      Map<String, String> results = assertCompile(lang, sources);
      for (Map.Entry<String, String> result : results.entrySet()) {
        File f = new File(new File(new File("target"), lang.getExtension()), result.getKey());
        if (!f.getParentFile().exists()) {
          assertTrue(f.getParentFile().mkdirs());
        }
        if (!f.exists()) {
          assertTrue(f.createNewFile());
        }
        Files.write(f.toPath(), result.getValue().getBytes());
      }
    }
  }

  @Test
  public void testEcho() throws Exception {
    assertCompileAll("echo/EchoServer.java", "echo/EchoClient.java");
  }

  @Test
  public void testHttp() throws Exception {
    assertCompileAll("http/Server.java", "http/Client.java");
  }

  @Test
  public void testHttps() throws Exception {
    assertCompileAll("https/Server.java", "https/Client.java");
  }

  @Test
  public void testProxy() throws Exception {
    assertCompileAll("proxy/Proxy.java", "proxy/Server.java", "proxy/Client.java");
  }

  @Test
  public void testEventBusPointToPoint() throws Exception {
    assertCompileAll("eventbus_pointtopoint/Sender.java", "eventbus_pointtopoint/Receiver.java");
  }

  @Test
  public void testEventBusPubSub() throws Exception {
    assertCompileAll("eventbus_pubsub/Sender.java", "eventbus_pubsub/Receiver.java");
  }

  @Test
  public void testSendFile() throws Exception {
    assertCompileAll("sendfile/SendFile.java");
  }

  @Test
  public void testSimpleFormUpload() throws Exception {
    assertCompileAll("simpleformupload/SimpleFormUploadServer.java");
  }

  @Test
  public void testSimpleForm() throws Exception {
    assertCompileAll("simpleform/SimpleFormServer.java");
  }

  @Test
  public void testSsl() throws Exception {
    assertCompileAll("ssl/Server.java", "ssl/Client.java");
  }

  @Test
  public void testSockJS() throws Exception {
    assertCompileAll("sockjs/SockJSExample.java");
  }

  @Test
  public void testRouteMatch() throws Exception {
    assertCompileAll("route_match/RouteMatchServer.java");
  }

  @Test
  public void testEventBusBridge() throws Exception {
    assertCompileAll("eventbusbridge/BridgeServer.java");
  }

  @Test
  public void testUpload() throws Exception {
    assertCompileAll("upload/Server.java", "upload/Client.java");
  }

  @Test
  public void testWebSockets() throws Exception {
    assertCompileAll("websockets/WebSocketsServer.java", "websockets/WebSocketsClient.java");
  }
}
