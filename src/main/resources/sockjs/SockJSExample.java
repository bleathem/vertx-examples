package sockjs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JKSOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

import io.vertx.core.streams.Pump;
import io.vertx.ext.sockjs.SockJSServer;
import io.vertx.ext.sockjs.SockJSServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSExample extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer(HttpServerOptions.options().setPort(8080));

    // Serve the index page
    server.requestHandler(req -> {
      if (req.uri().equals("/")) req.response().sendFile("sockjs/index.html");
    });

    SockJSServer sockJSServer = SockJSServer.sockJSServer(vertx, server);

    // The handler for the SockJS app - we just echo data back
    sockJSServer.installApp(SockJSServerOptions.options().setPrefix("/testapp"), sock -> {
      sock.dataHandler(buff -> {
        sock.write(buff);
      });
    });

    server.listen();
  }
}
