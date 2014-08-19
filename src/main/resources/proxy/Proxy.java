package proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.RequestOptions;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Proxy extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    HttpClient client = vertx.createHttpClient(HttpClientOptions.options());
    HttpServer server = vertx.createHttpServer(HttpServerOptions.options().setPort(8080)).requestHandler(req -> {
      System.out.println("Proxying request: " + req.uri());
      HttpClientRequest c_req = client.request(req.method(), RequestOptions.options().setPort(8282).setRequestURI(req.uri()), c_res -> {
        System.out.println("Proxying response: " + c_res.statusCode());
        req.response().setChunked(true);
        req.response().setStatusCode(c_res.statusCode());
        req.response().headers().setAll(c_res.headers());
        c_res.dataHandler(data -> {
          System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
          req.response().writeBuffer(data);
        });
        c_res.endHandler((v) -> req.response().end());
      });
      c_req.setChunked(true);
      c_req.headers().setAll(req.headers());
      req.dataHandler(data -> {
        System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
        c_req.writeBuffer(data);
      });
      req.endHandler((v) -> c_req.end());
    }).listen();
  }
}
