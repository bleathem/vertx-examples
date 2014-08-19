var Pump = require('vertx-js/pump');
vertx.createNetServer({"port": 1234}).connectHandler(function(sock) {
    Pump.pump(sock, sock).start();
}).listen();