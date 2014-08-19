require('vertx-js/vertx');

var client = vertx.createNetClient({});

client.connect(1234, "localhost", function(sock, err) {

    if (sock) {

        sock.dataHandler(function(buffer) {
            console.log("client receiving " + buffer.toString('UTF-8'));
        });

        // Now send some data
        for (var i = 0; i < 10; i++) {
            var str = "hello" + i + "\n";
            console.log("Net client sending: " + str);
            sock.writeString(str);
        }
    } else {
        console.log("Failed to connect " + err)
    }
});