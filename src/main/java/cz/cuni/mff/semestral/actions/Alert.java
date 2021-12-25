package cz.cuni.mff.semestral.actions;

public class Alert {
    String pair;
    Double value;
    String direction;
    boolean perc;

    public Alert setPair(String pair) {
        this.pair = pair;
        return this;
    }

    public Alert setValue(double value) {
        this.value = value;
        return this;
    }

    public Alert setIsPerc(boolean perc) {
        this.perc = perc;
        return this;
    }

    public Alert setDirection(String direction) {
        this.direction = direction;
        return this;
    }
}
