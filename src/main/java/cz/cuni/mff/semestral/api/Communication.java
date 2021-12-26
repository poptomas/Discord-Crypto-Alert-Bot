package cz.cuni.mff.semestral.api;

import cz.cuni.mff.semestral.parse.Parser;

import cz.cuni.mff.semestral.utilities.Stopwatch;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Communication extends ListenerAdapter {

    enum Actions{ Help, Alert, Predict};
    private final Parser parser;
    private final BinanceConnection binance;

    private boolean isEmpty(String[] args) {
        return args.length == 0;
    }

    private boolean isBot(MessageReceivedEvent event) {
        //do not let (possible even other) bot respond to bot messages (it may trigger spamming)
        return event.getAuthor().isBot();
    }

    public Communication() {
        parser = new Parser();
        binance = new BinanceConnection();
    }

    public synchronized void establishConnection() {
        int delay = 5;
        while(true) {
            try {
                String rawResponse = binance.connect();
                HashMap<String, Double> json = binance.jsonParse(rawResponse);

                System.out.println("Get");

                Stopwatch sw = new Stopwatch();
                sw.start();
                parser.getCurrentData(json);
                parser.checkAlerts();
                sw.end();
                sw.printMessage();
                TimeUnit.SECONDS.sleep(delay);
                System.out.println("Done");

            } catch (IOException | InterruptedException ex){
                System.err.println("Can't connect right now");
                break;
            }
        }
    }

    private void sendMessage(MessageReceivedEvent event, String content) {
        event.getChannel().sendMessage(content).queue();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("Message received");
        String[] args = event.getMessage().getContentRaw().split(" ");
        String startSign = "?";
        if(isBot(event) || isEmpty(args)) {
            return;
        }
        String output = parser.parse(args);
        if (output.startsWith(startSign)) {
            output = parser.processInput();
        }
        sendMessage(event, output);
    }
}
