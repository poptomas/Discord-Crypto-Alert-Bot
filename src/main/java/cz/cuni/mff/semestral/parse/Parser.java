package cz.cuni.mff.semestral.parse;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    Options maker;
    HashMap<String, Option> optionsMap;
    HashMap<String, String> flagAlternatives;
    HashMap<String, String> inputPairs;

    public Parser() {
        maker = new Options();
        maker.setAll();
        optionsMap = maker.getOptions();
        flagAlternatives = maker.getAlternatives();
        inputPairs = new HashMap<>();
    }

    private static class Pair<T, U> {
        public T first;
        public U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public Pair() {
            this.first = null;
            this.second = null;
        }
    }


    private Pair<String, String> splitToKVPair(String argument) {
        Pair<String, String> pair = new Pair<>();
        pair.first = argument.substring(0, argument.indexOf("="));
        pair.second = argument.substring(argument.indexOf("=") + 1, argument.length());
        return pair;
    }

    private String trimFirstChar(String token) {
        return token.substring(1, token.length());
    }

    public String processInput() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : inputPairs.entrySet()) {
            String record = entry.getKey() + " : " + entry.getValue() + "\n";
            sb.append(record);
        }

        return sb.toString();
    }

    public String parse(String[] args) {
        boolean awaitArg = false;
        Pair<String, String> keyValue = new Pair<>();
        String startSign = "?";
        boolean firstArg = true;

        for(String argument : args) {
            if(firstArg) {
                if(argument.startsWith(startSign)) {
                    inputPairs.put("command", trimFirstChar(argument));
                    firstArg = false;
                }
                else {
                    return firstCommandIssue;
                }
            }
            else if(awaitArg) {
                keyValue.second = argument;
                inputPairs.put(keyValue.first, keyValue.second);
            }
            // figure out both (key, val) at once
            else if(argument.contains("=")){
                keyValue = splitToKVPair(argument);
                if(optionsMap.containsKey(keyValue.first) || flagAlternatives.containsKey(keyValue.first)) {
                    inputPairs.put(keyValue.first, keyValue.second);
                }
                else {
                    return nonExistentKey;
                }
            }
            else{
                if(optionsMap.containsKey(argument) || flagAlternatives.containsKey(argument)) {
                    keyValue.first = argument;
                    awaitArg = true;
                    continue;
                }
                return nonExistentKey;
            }
            awaitArg = false;
        }
        return startSign; // in quotes return 0 for the bot
    }

    static String nonExistentKey = "An entered key does not exist in the allowed options (?help)";
    static String firstCommandIssue = "First command starts with a question mark (?)";
}
