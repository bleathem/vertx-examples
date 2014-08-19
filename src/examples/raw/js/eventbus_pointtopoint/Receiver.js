var eb = vertx.eventBus();

eb.registerHandler("ping-address", function(message) {
    console.log('Received message ' + message.body());
    // Now send back reply
    message.reply("pong!");
});