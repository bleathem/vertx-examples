package websockets;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocketConnectOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebSocketsClient extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpClient client = vertx.createHttpClient(HttpClientOptions.options());

    client.connectWebsocket(WebSocketConnectOptions.options().setPort(8080).setRequestURI("/some-uri"), websocket -> {
      websocket.dataHandler(data -> {
        System.out.println("Received data " + data.toString("ISO-8859-1"));
        client.close();
      });
      websocket.writeMessage(Buffer.buffer("Hello world"));
    });
  }
}
