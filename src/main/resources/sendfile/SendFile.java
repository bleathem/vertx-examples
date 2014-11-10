package sendfile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SendFile extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpServer(new HttpServerOptions().setPort(8080)).requestHandler(req -> {
      String filename = "sendfile/" + (req.uri().equals("/") ? "index.html" : "." + req.uri());
      System.out.println(filename);
      req.response().sendFile(filename);
    }).listen();
  }
}
