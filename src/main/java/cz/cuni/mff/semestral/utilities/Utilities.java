package cz.cuni.mff.semestral.utilities;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Utility class to abstract some
 * technical details of implementation
 */

public class Utilities {
    final static NumberFormat numberFormat = new DecimalFormat("##.#######");

    @SafeVarargs
    public static <T> void Print(T... args) {
        for (T arg: args) {
            System.out.print(arg + " ");
        }
        System.out.println();
    }

    public static <T> String NumFormat(T value) {
        return numberFormat.format(value);
    }

    public static <T> boolean IsEmpty(ArrayList<T> list) {
        return list.size() == 0;
    }

    public static <T> boolean IsEmpty(T[] array) {
        return array.length == 0;
    }

    public static String Normalize(String pair) {
        return pair.replace("/", "").toUpperCase();
    }

    public static String TrimQuotes(String token) {
        return token.substring(1, token.length() - 1);
    }

    public static String TrimFirstCharacter(String token) {
        return token.substring(1);
    }

    public static String TrimLastCharacter(String token) {
        return token.substring(0, token.length() - 1);
    }

    public static double CalcValueFromPercent(double priceAtTime, double percent) {
        double projected = percent / 100 * priceAtTime;
        return priceAtTime + projected;
    }

    public static double CalcPercentFromValues(double valueAtTime, double valueProjected) {
        return valueProjected * 100 / valueAtTime - 100;
    }

    public static String GetValueWithInfo(double value, boolean isPerc) {
        String valuePart;
        if(isPerc) {
            valuePart = String.format("%10s %%", NumFormat(value));
        }
        else {
            valuePart = String.format("%10s USD", NumFormat(value));
        }
        return valuePart;
    }
}
