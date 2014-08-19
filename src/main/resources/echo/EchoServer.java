package echo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetServerOptions;

import io.vertx.core.streams.Pump;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EchoServer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createNetServer(NetServerOptions.options().setPort(1234)).connectHandler(socket -> {
      Pump.pump(socket, socket).start();
    }).listen();
  }
}
