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

def server = vertx.createHttpServer(port:8080)

// Serve the static resources
server.requestHandler { req ->
  if (req.uri() == '/') req.response().sendFile('eventbusbridge/index.html')
  if (req.uri() == '/vertxbus.js') req.response().sendFile('eventbusbridge/vertxbus.js')
}

// At the moment we use the native SockJSServer
def sockJSServer = io.vertx.groovy.ext.sockjs.SockJSServer.sockJSServer(vertx, server)
sockJSServer.bridge(
    [prefix: "/eventbus"],
    [inboundPermitted: [[:]], outboundPermitted: [[:]]])
server.listen()
