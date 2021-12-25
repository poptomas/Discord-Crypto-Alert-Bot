package cz.cuni.mff.semestral;

import cz.cuni.mff.semestral.apiconn.BinanceConnection;
import cz.cuni.mff.semestral.parse.Parser;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Arrays;

public class Communication extends ListenerAdapter {
    enum Actions{ Help, Alert, Predict};
    private final Parser parser;

    private boolean isEmpty(String[] args) {
        return args.length == 0;
    }

    private boolean isBot(MessageReceivedEvent event) {
        //do not let (possible even other) bot respond to bot messages (it may trigger spamming)
        return event.getAuthor().isBot();
    }

    public Communication() {
        parser = new Parser();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ");
        String startSign = "?";
        if(isBot(event) || isEmpty(args)) {
            return;
        }
        BinanceConnection binance = new BinanceConnection();
        try {
            binance.connect();
        }
        catch(IOException ex) {
            System.err.println("Can't connect rn");
        }

        String output = parser.parse(args);
        if(!output.startsWith(startSign)) {
            event.getChannel().sendMessage(output).queue();
        }
        else{
            parser.processInput();
        }
    }
}
