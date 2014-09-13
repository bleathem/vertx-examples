package simpleform;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SimpleFormServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpServer(HttpServerOptions.options().setPort(8080)).requestHandler(req -> {
      if (req.uri().equals("/")) {
        // Serve the index page
        req.response().sendFile("simpleform/index.html");
      } else if (req.uri().startsWith("/form")) {
        req.response().setChunked(true);
        req.setExpectMultipart(true);
        req.endHandler((v) -> {
          for (String attr : req.formAttributes().names()) {
            req.response().write("Got attr " + attr + " : " + req.formAttributes().get(attr) + "\n");
          }
          req.response().end();
        });
      } else {
        req.response().setStatusCode(404);
        req.response().end();
      }
    }).listen();
  }
}
