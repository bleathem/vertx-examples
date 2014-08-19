var eb = vertx.eventBus();

vertx.setPeriodic(1000, function sendMessage() {
    eb.send('ping-address', 'ping!', function(reply) {
        console.log("Received reply: " + reply.body());
    });
})