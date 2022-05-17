package cz.cuni.mff.semestral.actions;
import cz.cuni.mff.semestral.utilities.Pair;

/**
 * Data class for alert info
 */
public class Alert {
    private String pair;
    private Double value;
    private Double priceAtTime;
    private boolean isPercent = false;

    public Alert SetPair(String pair) {
        this.pair = pair;
        return this;
    }

    public Alert SetValue(double value) {
        this.value = value;
        return this;
    }

    public Alert SetIsPerc(boolean isPercent) {
        this.isPercent = isPercent;
        return this;
    }

    public Alert SetPriceAtTime(Double priceAtTime) {
        this.priceAtTime = priceAtTime;
        return this;
    }

    public Pair<Double, Boolean> GetValue() {
        return new Pair<>(value, isPercent);
    }

    @Override
    public String toString() {
        return pair;
    }

    public String GetCryptocurrencySymbol() {
        return pair;
    }

    public double GetPriceAtTime() {
        return priceAtTime;
    }
}
