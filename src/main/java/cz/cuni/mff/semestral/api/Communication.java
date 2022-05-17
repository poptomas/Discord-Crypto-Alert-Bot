package cz.cuni.mff.semestral.api;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import cz.cuni.mff.semestral.processor.Processor;
import cz.cuni.mff.semestral.utilities.Utilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Communication extends ListenerAdapter {
    private final Processor processor;
    private final BinanceConnection binance;
    private final Random random;
    private final EmbedBuilder builder;

    private boolean isBot(MessageReceivedEvent event) {
        //do not let (possible even other) bot respond to bot messages
        // which may trigger spamming
        return event.getAuthor().isBot();
    }

    public Communication() {
        processor = new Processor();
        binance = new BinanceConnection();
        random = new Random();
        builder = new EmbedBuilder();
    }

    public synchronized void EstablishConnection(JDA jda) {
        int delay = 1; // seconds
        while(true) {
            try {
                String rawResponse = binance.connect();
                HashMap<String, Double> json = binance.jsonParse(rawResponse);
                processor.SetCurrentData(json);
                List<TextChannel> channels = jda.getTextChannels();
                for(var channel : channels) {
                    var users = channel.getMembers();
                    var userMap = processor.GetUserMap();
                    for(var member : users) {
                        var username = member.getUser().getName();
                        var user = userMap.get(username);
                        if(user != null) {
                            var alertInfo = processor.ClearFinishedAlerts(user);
                            if(!alertInfo.isEmpty()) {
                                builder.setColor(random.nextInt());
                                builder.setTitle("Alert trigger for " + username);
                                builder.setDescription(alertInfo);
                                builder.setFooter(
                                        "Initiated by: " + username,
                                        Objects.requireNonNull(member.getUser().getAvatarUrl())
                                );
                                // for the raw text variant sendMessage(channel, output);
                                sendMessage(channel, builder);
                                builder.clear();
                            }
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(delay);
            }
            catch (IOException | InterruptedException ex){
                System.err.println("Can't connect right now");
                break;
            }
        }
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        String userInput = event.getMessage()
                .getContentRaw()
                .replaceAll("\\s+", " ");
        String[] inputArgs = userInput.trim().split(" ");
        if (isBot(event) || Utilities.IsEmpty(inputArgs)) {
            return;
        }
        String user = Objects.requireNonNull(event.getMember()).getUser().getName();
        for(int index = 0; index < inputArgs.length; ++index) {
            if(inputArgs[index] == null) {
                // propagated changes from the processor that
                // the commands should not be processed any more (!help ... whatever should be ignored)
                break;
            }
            String output = processor.ProcessInput(inputArgs, index, user);
            if (!output.isEmpty()) {
                String title = processor.GetPartialUserInput();
                builder.setColor(random.nextInt());
                builder.setTitle(title);
                builder.setDescription(output);
                builder.setFooter(
                        "Initiated by: " + user,
                        Objects.requireNonNull(event.getMember()).getUser().getAvatarUrl()
                );
                // for the raw text variant sendMessage(channel, output);
                sendMessage(event, builder);
                builder.clear();
            }
        }
    }

    private void sendMessage(MessageReceivedEvent event, EmbedBuilder info) {
        event.getChannel().sendMessageEmbeds(info.build()).queue();
    }

    private void sendMessage(TextChannel channel, EmbedBuilder info) {
        channel.sendMessageEmbeds(info.build()).queue();
    }

    // raw messages - original version
    private void sendMessage(TextChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    // raw messages - original version
    private void sendMessage(MessageReceivedEvent event, String content) {
        event.getChannel().sendMessage(content).queue();
    }

}
