package http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Client extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createHttpClient(new HttpClientOptions()).request(HttpMethod.GET, 8080, "localhost", "/", resp -> {
      System.out.println("Got response " + resp.statusCode());
      resp.bodyHandler(body -> {
        System.out.println("Got data " + body.toString("ISO-8859-1"));
      });
    });
  }
}
