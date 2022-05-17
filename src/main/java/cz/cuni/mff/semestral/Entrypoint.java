package cz.cuni.mff.semestral;

import cz.cuni.mff.semestral.api.Communication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;

public class Entrypoint {
    public static void main(String[] args) throws LoginException {
        String token = "OTIzODgxNzIyOTY0MjEzNzcw.YcWd5w.KKrEEDHgexIrUXhNUvXSo_w32Nw";
        JDA jda = JDABuilder.createDefault(token).build();
        jda.getPresence().setActivity(Activity.playing("Waiting forever"));
        Communication comm = new Communication();
        jda.addEventListener(comm);
        comm.EstablishConnection(jda);
    }
}
