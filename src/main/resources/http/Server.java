package http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Server extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpServer(HttpServerOptions.options().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().setChunked(true).end("<html><body><h1>Hello from vert.x!</h1></body></html>");
    }).listen();
  }
}
