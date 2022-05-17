package cz.cuni.mff.semestral.parse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Options {
    HashMap<String, Option> allOptions;
    HashMap<String, String> flagAlts;
    public Options() {
        allOptions = new HashMap<>();
        flagAlts = new HashMap<>();
    }

    private String last(List<String> params) {
        return params.get(params.size() - 1);
    }

    private void setAlternatives(List<String> params, String value) {
        for (String member: params) {
            if(member.equals(last(params))){
                break;
            }
            flagAlts.put(member, value);
        }
    }

    public void setAll() {
        Option amount, value, pair, direction, predict, alert;

        List<String> valueParams = Arrays.asList("v", "value");
        String mainParam = "value";
        value = new Option().registerParams(valueParams).requiresParam().isDouble();
        allOptions.put(mainParam, value);
        setAlternatives(valueParams, mainParam);

        List<String> pairParams = Arrays.asList("p", "pair", "cryptocurrency", "crypto", "c", "cc");
        mainParam = "pair";
        pair = new Option().registerParams(pairParams).isInApi().requiresParam();
        allOptions.put(mainParam, pair);
        setAlternatives(valueParams, mainParam);

        List<String> dirParams = Arrays.asList("d", "direction");
        mainParam = "direction";
        direction = new Option().registerParams(dirParams).isInPredefinedList().requiresParam();
        allOptions.put(mainParam, direction);
        setAlternatives(dirParams, mainParam);

        predict = new Option().registerParams(Collections.singletonList("predict")).onItsOwn();
        allOptions.put("predict", predict);

        List<String> alertParams = List.of("a", "alert", "alertion", "w", "warn", "warning");
        mainParam = "alert";
        alert = new Option().registerParams(alertParams).notWith(predict);
        allOptions.put(mainParam, alert);
        setAlternatives(alertParams, mainParam);
    }

    public HashMap<String, Option> getOptions() {
        return allOptions;
    }

    public HashMap<String, String> getAlternatives() {
        return flagAlts;
    }
}
