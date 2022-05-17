package cz.cuni.mff.semestral.actions;

import cz.cuni.mff.semestral.utilities.Pair;

import java.text.MessageFormat;

/**
 * Data class containg necessary information
 * about the alert i.e., symbol, value, initial value
 */
public class Alert {
    private String symbol;
    private Double value;
    private Double priceAtTime;
    private boolean isPercent = false;

    public Alert SetPair(String inSymbol) {
        symbol = inSymbol;
        return this;
    }

    public Alert SetValue(double inValue) {
        value = inValue;
        return this;
    }

    public Alert SetIsPerc(boolean inIsPercent) {
        isPercent = inIsPercent;
        return this;
    }

    public Alert SetPriceAtTime(Double inPriceAtTime) {
        priceAtTime = inPriceAtTime;
        return this;
    }

    public Pair<Double, Boolean> GetValue() {
        return new Pair<>(value, isPercent);
    }

    public String GetCryptocurrencySymbol() {
        return symbol;
    }

    public double GetPriceAtTime() {
        return priceAtTime;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}.{1}.{2}", symbol, value, isPercent);
    }

}
