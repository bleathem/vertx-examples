/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.examples;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Runner {

  private static Vertx vertx = Vertx.vertx();
  private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

  public static void main(String[] args) throws Exception {
    ArrayList<Method> examples = new ArrayList<>();
    for (Method m : Runner.class.getDeclaredMethods()) {
      int mods = m.getModifiers();
      if (Modifier.isPublic(mods) && Modifier.isStatic(mods) && m.getParameterTypes().length == 0) {
        examples.add(m);
      }
    }
    examples.sort((m1, m2) -> m1.getName().compareTo(m2.getName()));
    System.out.println(
        "##########################\n" +
        "# Vert.x examples runner #\n" +
        "##########################\n");
    while (true) {
      Method example;
      if (args.length > 0 && args[0] != null) {
        example = null;
        for (Method m : examples) {
          if (m.getName().equals(args[0].trim())) {
            example = m;
          }
        }
        args = new String[0]; // So we run other examples
      } else {
        System.out.println("Choose an example:");
        int index = 1;
        for (Method m : examples) {
          System.out.println((index++) + ":" + m.getName());
        }
        System.out.print("> ");
        String s = reader.readLine();
        try {
          index = Integer.parseInt(s) - 1;
          if (index >= 0 && index < examples.size()) {
            example = examples.get(index);
          } else {
            System.out.println("Invalid example <" + s + ">");
            continue;
          }
        } catch (NumberFormatException ignore) {
          System.out.println("Invalid example <" + s + ">");
          continue;
        }
      }
      run(example);
    }
  }

  private static void run(Method m) throws Exception {
    m.invoke(null);
  }

  public static void echo() {
    deploy("groovy:echo/EchoServer.groovy", "groovy:echo/EchoClient.groovy");
  }

  public static void eventbus_pointtopoint() {
    deploy("groovy:eventbus_pointtopoint/Receiver.groovy", "groovy:eventbus_pointtopoint/Sender.groovy");
  }

  public static void eventbus_pubsub() {
    deploy("groovy:eventbus_pubsub/Receiver.groovy", "groovy:eventbus_pubsub/Sender.groovy");
  }

  public static void eventbusbridge() {
    deploy("groovy:eventbusbridge/BridgeServer.groovy");
  }

  public static void fanout() {
    deploy("groovy:fanout/FanoutServer.groovy");
  }

  public static void http() {
    deploy("groovy:http/Server.groovy", "groovy:http/Client.groovy");
  }

  public static void https() {
    deploy("groovy:https/Server.groovy", "groovy:https/Client.groovy");
  }

  public static void proxy() {
    deploy("groovy:proxy/Server.groovy", "groovy:proxy/Proxy.groovy", "groovy:proxy/Client.groovy");
  }

  public static void route_matcher() {
    deploy("groovy:route_match/RouteMatchServer.groovy");
  }

  public static void sendfile() {
    deploy("groovy:sendfile/SendFile.groovy");
  }

  public static void simpleform() {
    deploy("groovy:simpleform/SimpleFormServer.groovy");
  }

  public static void simpleformupload() {
    deploy("groovy:simpleformupload/SimpleFormUploadServer.groovy");
  }

  public static void sockjs() {
    deploy("groovy:sockjs/SockJSExample.groovy");
  }

  public static void ssl() {
    deploy("groovy:ssl/Server.groovy", "groovy:ssl/Client.groovy");
  }

  public static void upload() {
    deploy("groovy:upload/Server.groovy", "groovy:upload/Client.groovy");
  }

  public static void websockets() {
    deploy("groovy:websockets/WebSocketsServer.groovy", "groovy:websockets/WebSocketsClient.groovy");
  }

  public static void deploy(String... verticles) {
    BlockingQueue<AsyncResult<String[]>> queue = new ArrayBlockingQueue<AsyncResult<String[]>>(1);
    deploy(Arrays.asList(verticles), new String[0], queue::add);
    AsyncResult<String[]> result;
    try {
      result = queue.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }
    if (result.succeeded()) {
      System.out.println("Press a key after run...");
      try {
        System.in.read();
      } catch (IOException e) {
      }
      CountDownLatch done = new CountDownLatch(1);
      undeploy(result.result(), d -> {
        done.countDown();
      });
      try {
        done.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    } else {
      System.out.println("Failed to deploy:");
      result.cause().printStackTrace();
    }
  }

  /**
   * Deploy the provided verticles sequentially.
   *
   * @param verticles the verticles to deploy
   * @param previousDepl the previous deployments
   * @param resultHandler the result handler
   */
  private static void deploy(List<String> verticles, String[] previousDepl, Handler<AsyncResult<String[]>> resultHandler) {
    if (verticles.size() > 0) {
      String verticle = verticles.get(0);
      vertx.deployVerticle(verticle, result -> {
        if (result.succeeded()) {
          System.out.println("Deployed: " + verticle + " as " + result.result());
          String[] nextDepl = Arrays.copyOf(previousDepl, previousDepl.length + 1);
          nextDepl[previousDepl.length] = result.result();
          deploy(verticles.subList(1, verticles.size()), nextDepl, resultHandler);
        } else {
          undeploy(previousDepl, done -> {
            resultHandler.handle(Future.completedFuture(result.cause()));
          });
        }
      });
    } else {
      resultHandler.handle(Future.completedFuture(previousDepl));
    }
  }

  private static void undeploy(String[] deployments, Handler<Void> doneHandler) {
    if (deployments.length == 0) {
      doneHandler.handle(null);
    } else {
      String[] next = Arrays.copyOf(deployments, deployments.length - 1);
      vertx.undeployVerticle(deployments[deployments.length - 1], result -> {
        undeploy(next, doneHandler);
      });
    }
  }
}
