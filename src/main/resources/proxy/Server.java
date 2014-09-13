package proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Server extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    vertx.createHttpServer(HttpServerOptions.options().setPort(8282)).requestHandler(req -> {

      System.out.println("Got request " + req.uri());

      for (String name : req.headers().names()) {
        System.out.println(name + ": " + req.headers().get(name));
      }

      req.dataHandler(data -> System.out.println("Got data " + data.toString("ISO-8859-1")));

      req.endHandler(v -> {
        // Now send back a response
        req.response().setChunked(true);

        for (int i = 0; i < 10; i++) {
          req.response().write("server-data-chunk-" + i);
        }

        req.response().end();
      });
    }).listen();

  }

}