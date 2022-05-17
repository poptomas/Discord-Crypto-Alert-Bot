package cz.cuni.mff.semestral.options;

import java.util.List;

public class Option {
    boolean checkDirection = false;
    boolean checkApi = false;
    boolean starter = false;
    boolean checkDouble = false;
    List<String> params;

    public Option registerParams(List<String> list) {
        params = list;
        return this;
    }

    public List<String> getParams() {
        return params;
    }

    public Option isStarter() {
        starter = true;
        return this;
    }

    public Option isInPredefinedList() {
        checkDirection = true;
        return this;
    }

    public Option isInApi(){
        checkApi = true;
        return this;
    }

    public Option isDouble() {
        checkDouble = true;
        return this;
    }
}
