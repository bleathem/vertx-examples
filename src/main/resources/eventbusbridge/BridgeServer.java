package eventbusbridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sockjs.BridgeOptions;
import io.vertx.ext.sockjs.SockJSServer;
import io.vertx.ext.sockjs.SockJSServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BridgeServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));

    // Serve the static resources
    server.requestHandler(req -> {
      if (req.uri().equals("/")) req.response().sendFile("eventbusbridge/index.html");
      if (req.uri().equals("/vertxbus.js")) req.response().sendFile("eventbusbridge/vertxbus.js");
    });

    // At the moment we use the native SockJSServer
    SockJSServer sockJSServer = SockJSServer.sockJSServer(vertx, server);
    sockJSServer.bridge(
        new SockJSServerOptions().setPrefix("/eventbus"),
        new BridgeOptions().addInboundPermitted(new JsonObject()).addOutboundPermitted(new JsonObject()));
    server.listen();
  }
}
