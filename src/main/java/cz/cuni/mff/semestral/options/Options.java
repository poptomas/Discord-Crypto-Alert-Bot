package cz.cuni.mff.semestral.options;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Options {
    HashMap<String, Option> allOptions;
    HashMap<String, String> flagAlts;
    public Options() {
        allOptions = new HashMap<>();
        flagAlts = new HashMap<>();
    }

    public void setAll() {
        setValueOption();
        setPairOption();
        setDirectionOption();
        setGetOption();
        setAlertOption();
        setAddOption();
        setDeleteOption();
    }

    private void setValueOption() {
        List<String> valueParams = Arrays.asList("v", "value");
        String mainParam = "value";
        Option value = new Option().registerParams(valueParams).isDouble();
        allOptions.put(mainParam, value);
        setAlternatives(valueParams, mainParam);
    }

    private void setPairOption(){
        List<String> pairParams = Arrays.asList("p", "pair", "cryptocurrency", "crypto", "c", "cc");
        String mainParam = "pair";
        Option pair = new Option().registerParams(pairParams).isInApi();
        allOptions.put(mainParam, pair);
        setAlternatives(pairParams, mainParam);
    }

    private void setDirectionOption() {
        List<String> dirParams = Arrays.asList("d", "direction");
        String mainParam = "direction";
        Option direction = new Option().registerParams(dirParams).isInPredefinedList();
        allOptions.put(mainParam, direction);
        setAlternatives(dirParams, mainParam);
    }

    private void setGetOption(){
        List<String> reqParams = Arrays.asList("g", "get", "r", "req", "request");
        String mainParam = "get";
        Option get = new Option().registerParams(reqParams).isStarter();
        allOptions.put(mainParam, get);
        setAlternatives(reqParams, mainParam);
    }

    private void setAlertOption() {
        List<String> alertParams = Arrays.asList("a", "alert", "alertion", "w", "warn", "warning");
        String mainParam = "alert";
        Option alert = new Option().registerParams(alertParams).isStarter();
        allOptions.put(mainParam, alert);
        setAlternatives(alertParams, mainParam);
    }

    private void setAddOption() {
        List<String> addParams = Arrays.asList("add", "set");
        String mainParam = "add";
        Option alert = new Option().registerParams(addParams).isStarter();
        allOptions.put(mainParam, alert);
        setAlternatives(addParams, mainParam);
    }

    private void setDeleteOption() {
        List<String> deleteParams = Arrays.asList("del", "delete", "rm", "remove");
        String mainParam = "delete";
        Option alert = new Option().registerParams(deleteParams).isStarter();
        allOptions.put(mainParam, alert);
        setAlternatives(deleteParams, mainParam);
    }

    private void setAlternatives(List<String> params, String value) {
        for (String member: params) {
            flagAlts.put(member, value);
        }
    }

    public HashMap<String, Option> getOptions() {
        return allOptions;
    }

    public HashMap<String, String> getAlternatives() {
        return flagAlts;
    }
}
