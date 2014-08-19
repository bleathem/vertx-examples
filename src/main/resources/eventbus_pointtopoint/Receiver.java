package eventbus_pointtopoint;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Receiver extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    EventBus eb = vertx.eventBus();

    eb.registerHandler("ping-address", message -> {
        System.out.println("Received message: " + message.body());
        // Now send back reply
        message.reply("pong!");
    });
  }
}
