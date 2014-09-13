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


// For now uses ext route matcher
import io.vertx.groovy.ext.routematcher.RouteMatcher

// Inspired from Sinatra / Express
def rm = RouteMatcher.newRouteMatcher()

// Extract the params from the uri
rm.get('/details/:user/:id') { req ->
  // And just spit them out in the response
  req.response().setChunked(true).end("User: ${req.params()['user']} ID: ${req.params()['id']}")
}

// Catch all - serve the index page
rm.getWithRegEx('.*') { req ->
  req.response().sendFile "route_match/index.html"
}

vertx.createHttpServer(port:8080).requestHandler({ rm.accept(it) }).listen()
