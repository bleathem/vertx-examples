package route_match;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.routematcher.RouteMatcher;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RouteMatchServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    // Inspired from Sinatra / Express
    RouteMatcher rm = RouteMatcher.routeMatcher();

    // Extract the params from the uri
    rm.matchMethod(HttpMethod.GET, "/details/:user/:id", req -> {
      // And just spit them out in the response
      req.response().setChunked(true).end("User: " + req.params().get("user") + " ID: " + req.params().get("id"));
    });

    // Catch all - serve the index page
    rm.matchMethodWithRegEx(HttpMethod.GET, ".*", req -> {
      req.response().sendFile("route_match/index.html");
    });

    vertx.createHttpServer(new HttpServerOptions().setPort(8080)).requestHandler(req -> rm.accept(req)).listen();
  }
}
