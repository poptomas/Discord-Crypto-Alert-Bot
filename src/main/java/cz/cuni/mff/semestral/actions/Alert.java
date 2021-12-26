package cz.cuni.mff.semestral.actions;


import cz.cuni.mff.semestral.utilities.Pair;

public class Alert {
    private String pair;
    private Double value;
    private Double priceAtTime;
    private boolean upDirection;
    private boolean isPercent = false;

    public Alert setPair(String pair) {
        this.pair = pair;
        return this;
    }

    public Alert setValue(double value) {
        this.value = value;
        return this;
    }

    public Alert setIsPerc(boolean isPercent) {
        this.isPercent = isPercent;
        return this;
    }

    public Alert setDirection(boolean upDirection) {
        this.upDirection = upDirection;
        return this;
    }

    public Alert setPriceAtTime(Double priceAtTime) {
        this.priceAtTime = priceAtTime;
        return this;
    }

    public Pair<Double, Boolean> getValue() {
        return new Pair<>(value, isPercent);
    }

    public boolean getDirection() {
        return upDirection;
    }

    public String getCryptocurrencyPair() {
        return pair;
    }

    public double getPriceAtTime() {
        return priceAtTime;
    }
}
