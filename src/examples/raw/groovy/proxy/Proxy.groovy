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
package proxy

def client = vertx.createHttpClient()

def server = vertx.createHttpServer(port: 8080).requestHandler { req ->
  println "Proxying request: ${req.uri()}"

  def c_req = client.request(req.method(), [port: 8282, requestURI:req.uri()]) { c_res ->
    println "Proxying response: ${c_res.statusCode()}"
    req.response().setChunked(true)
    req.response().setStatusCode(c_res.statusCode())
    req.response().headers().setAll(c_res.headers())
    c_res.dataHandler { data ->
      println "Proxying response body: ${data.toString('ISO-8859-1')}"
      req.response().writeBuffer(data)
    }
    c_res.endHandler { req.response().end() }
  }
  c_req.setChunked(true)
  c_req.headers().setAll(req.headers())
  req.dataHandler { data ->
    println "Proxying request body ${data.toString('ISO-8859-1')}"
    c_req.writeBuffer(data)
  }
  req.endHandler{ c_req.end() }

}.listen()
