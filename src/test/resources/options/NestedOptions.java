package options;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JKSOptions;
import io.vertx.examples.OptionsTest;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class NestedOptions extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    OptionsTest.options = HttpServerOptions.options().setKeyStoreOptions(JKSOptions.options().setPath("/mystore.jks").setPassword("secret"));
  }
}
