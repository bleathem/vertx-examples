package echo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EchoClient extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.createNetClient(NetClientOptions.options()).connect(1234, "localhost", asyncResult -> {

      if (asyncResult.succeeded()) {
        NetSocket socket = asyncResult.result();
        socket.dataHandler(buffer -> {
            System.out.println("Net client receiving: " + buffer.toString("UTF-8"));
        });

        // Now send some data
        for (int i = 0;i < 10;i++) {
          String str = "hello " + i + "\n";
          System.out.println("Net client sending: " + str);
          socket.write(str);
        }
      } else {
        System.out.println("Failed to connect " + asyncResult.cause());
      }
    });
  }
}
