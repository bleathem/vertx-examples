package options;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.examples.OptionsTest;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class OptionsAdd extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    OptionsTest.options = new HttpServerOptions().addEnabledCipherSuite("foo").addEnabledCipherSuite("bar");
  }
}
