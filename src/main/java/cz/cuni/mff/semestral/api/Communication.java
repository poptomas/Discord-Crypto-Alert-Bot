package cz.cuni.mff.semestral.api;

import cz.cuni.mff.semestral.processor.Processor;
import cz.cuni.mff.semestral.utilities.Stopwatch;
import cz.cuni.mff.semestral.utilities.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Communication extends ListenerAdapter {
    String startSign = "!";
    private final Processor processor;
    private final BinanceConnection binance;

    private boolean isBot(MessageReceivedEvent event) {
        //do not let (possible even other) bot respond to bot messages
        // which may trigger spamming
        return event.getAuthor().isBot();
    }

    public Communication() {
        processor = new Processor();
        binance = new BinanceConnection();
    }

    public synchronized void establishConnection(JDA jda) {
        int delay = 1; // seconds
        while(true) {
            try {
                String rawResponse = binance.connect();
                HashMap<String, Double> json = binance.jsonParse(rawResponse);

                //Stopwatch sw = new Stopwatch();
                //sw.start();
                processor.SetCurrentData(json);
                var alertInfo = processor.ClearDoneAlerts();
                if(!alertInfo.isEmpty()) {
                    Utilities.Print(alertInfo);
                    List<TextChannel> channels = jda.getTextChannelsByName(
                            "general", true
                    );
                    for(var channel : channels) {
                        sendMessage(channel, alertInfo);
                    }
                }
                //sw.end();
                //sw.printMessage();
                TimeUnit.SECONDS.sleep(delay);
                //System.out.println("Done");

            } catch (IOException | InterruptedException ex){
                System.err.println("Can't connect right now");
                break;
            }
        }
    }

    private void sendMessage(TextChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    private void sendMessage(MessageReceivedEvent event, String content) {
        event.getChannel().sendMessage(content).queue();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("Message received");
        String[] args = event.getMessage().getContentRaw().split(" ");
        if(isBot(event) || Utilities.IsEmpty(args)) {
            return;
        }
        String parserOutput = processor.ProcessInput(args);
        if(!parserOutput.isEmpty()) {
            sendMessage(event, parserOutput);
        }
    }
}
