package https;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JKSOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Server extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpServer server =
        vertx.createHttpServer(HttpServerOptions.options().setHost("localhost").setPort(4443).setSsl(true).setKeyStoreOptions(
            JKSOptions.options().setPath("server-keystore.jks").setPassword("wibble")
        ));

    server.requestHandler(req -> {
      req.response().setChunked(true).end("<html><body><h1>Hello from vert.x!</h1></body></html>");
    }).listen();
  }
}
