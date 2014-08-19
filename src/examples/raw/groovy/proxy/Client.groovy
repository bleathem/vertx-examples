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

def request = vertx.createHttpClient().put(port: 8080, requestURI: '/') { resp ->
  println "Got response ${resp.statusCode()}"
  resp.bodyHandler { body -> println "Got data ${body.toString('ISO-8859-1')}" }
}

request.setChunked(true)

10.times { request.writeString("client-chunk-$it") }

request.end()
