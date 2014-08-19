vertx.createHttpServer({"host": "localhost", "port":8080}).requestHandler(function(req) {
    req.response().setChunked(true).writeString("<html><body><h1>Hello from vert.x!</h1></body></html>").end()
}).listen();
