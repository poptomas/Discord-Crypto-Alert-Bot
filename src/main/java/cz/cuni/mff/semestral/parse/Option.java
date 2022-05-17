package cz.cuni.mff.semestral.parse;

import java.util.ArrayList;
import java.util.List;

public class Option {
    enum Direction { Up, Down };
    Option excludedOption;
    boolean single = false;
    boolean checkDirection = false;
    boolean checkApi = false;
    boolean paramReq = false;
    boolean checkDouble = false;
    List<String> params;

    Option registerParams(List<String> list) {
        params = list;
        return this;
    }

    public List<String> getParams() {
        return params;
    }

    Option notWith(Option option) {
        excludedOption = option;
        return this;
    }

    Option onItsOwn() {
        single = true;
        return this;
    }

    Option isInPredefinedList() {
        checkDirection = true;
        return this;
    }

    Option isInApi(){
        checkApi = true;
        return this;
    }

    Option requiresParam() {
        paramReq = true;
        return this;
    }

    Option isDouble() {
        checkDouble = true;
        return this;
    }
}
