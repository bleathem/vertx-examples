package websockets;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebSocketsServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpServer(HttpServerOptions.options().setPort(8080)).websocketHandler(ws -> {
        ws.dataHandler(data -> ws.writeMessage(data));
    }).requestHandler(req -> {
      if (req.uri().equals("/")) req.response().sendFile("websockets/ws.html");
    }).listen();
  }
}
