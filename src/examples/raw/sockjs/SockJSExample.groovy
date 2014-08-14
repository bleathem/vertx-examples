/*
* Copyright 2011-2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

def server = vertx.createHttpServer(port:8080)

// Serve the index page
server.requestHandler { req ->
  if (req.uri() == "/") req.response().sendFile 'sockjs/index.html'
}

// At the moment we use the native SockJSServer
def sockJSServer = io.vertx.ext.sockjs.SockJSServer.sockJSServer(vertx.delegate, server.delegate)

// The handler for the SockJS app - we just echo data back
sockJSServer.installApp(io.vertx.ext.sockjs.SockJSServerOptions.options().setPrefix("/testapp")) { sock ->
  sock.dataHandler { buff ->
    sock.writeBuffer(buff)
  }
}

server.listen()

