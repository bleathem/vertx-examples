package route_match;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.routematcher.RouteMatcher;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RouteMatchServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    // Inspired from Sinatra / Express
    RouteMatcher rm = RouteMatcher.newRouteMatcher();

    // Extract the params from the uri
    rm.get("/details/:user/:id", req -> {
        // And just spit them out in the response
        req.response().setChunked(true).writeString("User: " + req.params().get("user") + " ID: " + req.params().get("id")).end();
    });

    // Catch all - serve the index page
    rm.getWithRegEx(".*", req -> {
        req.response().sendFile("route_match/index.html");
    });

    vertx.createHttpServer(HttpServerOptions.options().setPort(8080)).requestHandler(req -> rm.accept(req)).listen();
  }
}
