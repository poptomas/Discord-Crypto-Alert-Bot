package cz.cuni.mff.semestral;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;

import cz.cuni.mff.semestral.api.Communication;

/**
 * Entrypoint of the project
 * Contains the bot token which is required to pass the OAuth2 provided by the Discord API
 * - registers classes which the JDA component is supposed
 * to listen for events
 * - sets activities concerning the state of the bot just for fun
 */
public class Entrypoint {
    public static void main(String[] args) throws LoginException {
        String token = "OTIzODgxNzIyOTY0MjEzNzcw.YcWd5w.KKrEEDHgexIrUXhNUvXSo_w32Nw"; // no longer works
        JDA jda = JDABuilder.createDefault(token).build();
        jda.getPresence().setActivity(Activity.playing("Waiting forever"));
        Communication comm = new Communication();
        jda.addEventListener(comm);
        comm.Launch(jda);
    }
}
