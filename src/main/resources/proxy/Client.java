package proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.RequestOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Client extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpClientRequest request = vertx.createHttpClient(HttpClientOptions.options()).put(RequestOptions.options().setPort(8080).setRequestURI("/"), resp -> {
        System.out.println("Got response " + resp.statusCode());
      resp.bodyHandler(body -> System.out.println("Got data " + body.toString("ISO-8859-1")));
    });

    request.setChunked(true);

    for (int i = 0;i < 10;i++) {
      request.writeString("client-chunk-" + i);
    }

    request.end();
  }
}
