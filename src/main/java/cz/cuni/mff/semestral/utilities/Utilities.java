package cz.cuni.mff.semestral.utilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utilities {
    @SafeVarargs
    public static <T> void Print(T... args) {
        for (T arg: args) {
            System.out.print(arg + " ");
        }
        System.out.println();
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

    public static double CalcPercent(double priceAtTime, double percent) {
        double projected = percent / 100 * priceAtTime;
        return priceAtTime + projected;
    }

    public static String WithPrecision(double value) {
        return String.format("%.2f", value);
    }

    public static String GetValueWithInfo(double value, boolean isPerc) {
        String valuePart;
        if(isPerc) {
            valuePart = MessageFormat.format("{0} %", value);
        }
        else {
            valuePart = MessageFormat.format("{0} USD", value);
        }
        return valuePart;
    }
}
