vertx.createHttpClient({}).getNow({"host": 'localhost', "port": 8080, "requestURI":"/"}, function(resp) {
  console.log("Got response " + resp.statusCode());
  resp.bodyHandler(function(body) {
    console.log("Got data" + body.toString('ISO-8859-1'));
  });
});
