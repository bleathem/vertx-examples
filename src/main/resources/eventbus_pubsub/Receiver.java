package eventbus_pubsub;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Receiver extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    EventBus eb = vertx.eventBus();

    eb.consumer("news-feed").handler(message -> System.out.println("Received news: " + message.body()));
  }
}
