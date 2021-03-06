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

package upload

import io.vertx.groovy.core.streams.Pump

def req = vertx.createHttpClient().put(port: 8080, requestURI: "/someurl") { resp -> println "Response ${resp.statusCode()}" }
def filename = "upload/upload.txt"
def fs = vertx.fileSystem()

fs.props(filename) { ares ->
  def props = ares.result()
  println "props is ${props}"
  def size = props.size()
  req.headers().set("content-length", String.valueOf(size))
  fs.open(filename, [:]) { ares2 ->
    def file = ares2.result()
    def pump = Pump.pump(file, req)
    file.endHandler { req.end() }
    pump.start()
  }
}


