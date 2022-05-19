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
    // a hook to the command processor class
    private final Processor processor;
    // a hook to the binance connection class
    private final BinanceConnection binance;
    // randomized output color of the MessageEmbed
    private final Random random;
    // MessageEmbed builder
    private final EmbedBuilder builder;

    public Communication() {
        processor = new Processor();
        binance = new BinanceConnection();
        random = new Random();
        builder = new EmbedBuilder();
    }

    /**
     * Receives a message from the user and answers accordingly
     * @param event event that is launched after the message is sent by the user
     */
    public void onMessageReceived(MessageReceivedEvent event) {
        String userInput = event.getMessage()
                .getContentRaw()
                .replaceAll("\\s+", " ");
        String[] inputArgs = userInput.trim().split(" ");
        if (IsBot(event) || Utilities.IsEmpty(inputArgs)) {
            return;
        }
        var member = event.getMember();
        if(member == null) {
            return;
        }
        String user = member.getUser().getName();
        Answer(event, inputArgs, user);
    }

    /**
     * Launches requests with a pre-set delay
     * and regularly checks for triggered (finished) alerts
     * for users in the channel
     * @param jda JDA-specific (Discord library) component
     */
    public synchronized void Launch(JDA jda) {
        int delay = 1; // seconds
        while(true) {
            try {
                ConnectToService(jda);
                ProcessAlerts(jda);
                TimeUnit.SECONDS.sleep(delay);
            }
            catch (IOException | InterruptedException ex){
                System.err.println("Can't connect right now");
                break;
            }
        }
    }

    /**
     * Checks whether the message sent was from the bot
     * - it needs to disabled for a bot to respond to bot messages
     * (it would answer to own messages too which would cause major spamming difficulties)
     * @param event event that is launched after the message is sent by the user
     * @return Answer whether the author of the message is a bot
     */
    private boolean IsBot(MessageReceivedEvent event) {
        return event.getAuthor().isBot();
    }

    /**
     * Connects to Binance API, processes its json file and sends
     * the parsed data to the processor
     * @param jda JDA-specific (Discord library) component
     * @throws IOException in case, the input stream readers are not available
     */
    private void ConnectToService(JDA jda) throws IOException {
        String rawResponse = binance.Connect();
        HashMap<String, Double> json = binance.ParseJson(rawResponse);
        processor.SetCurrentData(json);
    }

    /**
     * regularly checks for triggered (finished) alerts for users in the channels of
     * the Discord server
     * @param jda JDA-specific (Discord library) component
     */
    private void ProcessAlerts(JDA jda) {
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
                        SendMessage(channel, builder);
                        builder.clear();
                    }
                }
            }
        }
    }

    /**
     * Answers to the user's message
     * @param event event that is launched after the message is sent by the user
     * @param inputArgs user input
     * @param userName user's nickname on Discord
     */
    private void Answer(MessageReceivedEvent event, String[] inputArgs, String userName) {
        for(int index = 0; index < inputArgs.length; ++index) {
            if(inputArgs[index] == null) {
                // propagated changes from the processor that
                // the commands should not be processed any more (!help ... whatever should be ignored)
                break;
            }
            String output = processor.ProcessInput(inputArgs, index, userName);
            if (!output.isEmpty()) {
                Utilities.Print(output);
                String title = processor.GetPartialUserInput();
                builder.setColor(random.nextInt());
                builder.setTitle(title);
                builder.setDescription(output);
                builder.setFooter(
                        "Initiated by: " + userName,
                        Objects.requireNonNull(event.getMember()).getUser().getAvatarUrl()
                );
                // for the raw text variant sendMessage(channel, output);
                SendMessage(event, builder);
                builder.clear();
            }
        }
    }

    /**
     * Sends a Discord-specific MessageEmbeds message to a channel where the event was triggered
     * @param event event that is launched after the message is sent by the user
     * @param info MessageEmbed stored information
     */
    private void SendMessage(MessageReceivedEvent event, EmbedBuilder info) {
        event.getChannel().sendMessageEmbeds(info.build()).queue();
    }

    /**
     * Sends a message using the Discord-specific MessageEmbeds
     * @param channel Channel to which the bot should send the message
     * @param info MessageEmbed stored information
     */
    private void SendMessage(TextChannel channel, EmbedBuilder info) {
        channel.sendMessageEmbeds(info.build()).queue();
    }

    /**
     * Sends a message (initial version) using the Discord-specific MessageEmbeds
     * @param channel Channel to which the bot should send the message
     * @param content information to be sent
     */
    private void SendMessage(TextChannel channel, String content) {
        channel.sendMessage(content).queue();
    }

    /**
     * Sends raw messages (initial version) to a channel where the event was triggered
     * @param event event that is launched after the message is sent by the user
     * @param content information to be sent
     */
    private void SendMessage(MessageReceivedEvent event, String content) {
        event.getChannel().sendMessage(content).queue();
    }

}
