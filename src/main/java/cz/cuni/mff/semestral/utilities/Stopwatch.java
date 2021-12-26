package cz.cuni.mff.semestral.utilities;
import java.text.MessageFormat;

public class Stopwatch {
    long startRec;
    long endRec;
    final int conversion = 1_000_000;

    public void start(){
        startRec = System.nanoTime();
    }

    public void end() {
        endRec = System.nanoTime() - startRec;
    }

    public void printMessage() {
        String message = MessageFormat.format("Elapsed {0} ms", endRec / conversion);
        System.out.println(message);
    }
}
