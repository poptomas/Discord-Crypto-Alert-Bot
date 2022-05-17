package cz.cuni.mff.semestral.utilities;

import java.text.MessageFormat;

/**
 * Stopwatch class
 * - kept for debugging performance purposes
 * - originally used to measure performance
 * of the API connection and further processing concerning the received data
 */
public class Stopwatch {
    long startRec;
    long endRec;
    final int conversion = 1_000_000;

    public void Start(){
        startRec = System.nanoTime();
    }

    public void End() {
        endRec = System.nanoTime() - startRec;
    }

    public void PrintMessage() {
        String message = MessageFormat.format(
                "Elapsed {0} ms", endRec / conversion
        );
        Utilities.Print(message);
    }
}
