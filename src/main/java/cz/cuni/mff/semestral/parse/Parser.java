package cz.cuni.mff.semestral.parse;

import cz.cuni.mff.semestral.actions.Alert;
import cz.cuni.mff.semestral.options.Option;
import cz.cuni.mff.semestral.options.Options;
import cz.cuni.mff.semestral.utilities.Pair;

import java.text.MessageFormat;
import java.util.*;

public class Parser {
    enum Actions { GET, ADD, DELETE, ALERT };
    Options maker;
    HashMap<String, Option> optionsMap;
    HashMap<String, String> flagAlternatives;

    HashMap<String, String> inputPairs;
    ArrayList<String> simplifiedInput;

    EnumMap<Actions, String> activityMap;

    HashMap<String, Double> cryptoPairs;
    ArrayList<String> localWatchList;
    ArrayList<Alert> currentAlerts;

    public Parser() {
        maker = new Options();
        inputPairs = new HashMap<>();
        simplifiedInput = new ArrayList<>();
        activityMap = new EnumMap<>(Actions.class);
        localWatchList = new ArrayList<>();
        currentAlerts = new ArrayList<>();
        setOptions();
        setActions();
    }

    private void setOptions() {
        maker.setAll();
        optionsMap = maker.getOptions();
        flagAlternatives = maker.getAlternatives();
    }

    private void setActions() {
        activityMap.put(Actions.GET, "get");
        activityMap.put(Actions.ALERT, "alert");
        activityMap.put(Actions.DELETE, "delete");
        activityMap.put(Actions.ADD, "add");
    }

    private Pair<String, String> splitToKVPair(String argument) {
        Pair<String, String> pair = new Pair<>();
        pair.first = argument.substring(0, argument.indexOf("="));
        pair.second = argument.substring(argument.indexOf("=") + 1, argument.length());
        return pair;
    }

    public void getCurrentData(HashMap<String, Double> pairs) {
        cryptoPairs = pairs;
    }

    private double calcPercent(double priceAtTime, double percent, boolean upDirection) {
        double projected = percent / 100 * priceAtTime;
        if(upDirection) {
            return priceAtTime + projected;
        }
        else {
            return priceAtTime - projected;
        }
    }

    private boolean isTriggered(Alert alert) {
        String pair = alert.getCryptocurrencyPair();
        double priceAtTime = alert.getPriceAtTime();
        double currentPrice = cryptoPairs.get(pair);
        Pair<Double, Boolean> projected = alert.getValue();
        boolean upDirection = alert.getDirection();

        if(projected.second) {
            projected.first = calcPercent(priceAtTime, projected.first, upDirection);
        }
        double alertValue = projected.first;
        System.out.println(upDirection);
        System.out.println(!upDirection);
        System.out.println(alertValue > currentPrice);
        System.out.println(alertValue < currentPrice);
        return ((upDirection && alertValue > currentPrice) || (!upDirection && alertValue < currentPrice));
    }

    public void checkAlerts() {
        currentAlerts.removeIf(this::isTriggered);
    }

    private boolean isEmpty(ArrayList<String> list) {
        return list.size() == 0;
    }

    private String trimFirstChar(String token) {
        return token.substring(1);
    }

    private String trimLastChar(String token) {
        return token.substring(0, token.length() - 1);
    }

    private String getOptWithNames() {
        // change when you get the "proper" option checker
        StringBuilder sb = new StringBuilder();
        if(localWatchList.isEmpty()) {
            return emptyList();
        }
        for (String member: localWatchList) {
            // already checked that they really exist
            sb.append(member).append(" : ").append(cryptoPairs.get(member)).append("\n");
        }
        return sb.toString();
    }

    private String getOptSimplified() {
        StringBuilder sBuilder = new StringBuilder();
        for (String pair: simplifiedInput) {
            pair = pair.replace("/", "").toUpperCase();
            if(!cryptoPairs.containsKey(pair)) {
                sBuilder.append(unknownCryptoPair(pair));
            }
            else {
                sBuilder.append(pair).append(" ").append(cryptoPairs.get(pair)).append("\n");
            }
        }
        simplifiedInput.clear();
        return sBuilder.toString();
    }

    private String alertOptSimplified() {
        StringBuilder sb = new StringBuilder();
        if(simplifiedInput.size() != 3) {
            return invalidNumberOfArguments();
        }
        // allow BTC/USDT (respectively some "best effort" alternatives with this particular intention)
        String pair = simplifiedInput.get(0).replace("/", "").toUpperCase();
        if(!cryptoPairs.containsKey(pair)) {
            return unknownCryptoPair(pair);
        }
        String strValue = simplifiedInput.get(1);
        String inDirection = simplifiedInput.get(2);
        boolean direction = inDirection.equalsIgnoreCase("UP");
        System.out.println(Objects.equals(inDirection, "UP"));
        boolean isPercent = false;
        double value = 0;
        if(strValue.endsWith("%")) {
            strValue = trimLastChar(strValue);
            value = Double.parseDouble(strValue);
            isPercent = true;
        }
        Alert alert = new Alert();
        alert.setPair(pair)
                .setDirection(direction)
                .setValue(value).setIsPerc(isPercent)
                .setPriceAtTime(cryptoPairs.get(pair));
        Optional<Alert> hasDuplicate = currentAlerts.stream()
                .filter(member -> pair.equals(member.getCryptocurrencyPair()))
                .findAny();
        if(hasDuplicate.isPresent()){
            sb.append(alreadyInTheList(pair));
        }
        else {
            currentAlerts.add(alert);
            sb.append(successfullyCreated());
        }
        return sb.toString();
    }

    private String withPrecision(double value) {
        return String.format("%.2f", value);
    }

    private String alertOptWithNames() {
        StringBuilder sb = new StringBuilder();
        System.out.println(currentAlerts.size());
        for (Alert alert: currentAlerts) {
            boolean upDirection = alert.getDirection();
            String pair = alert.getCryptocurrencyPair();
            double priceAtTime = alert.getPriceAtTime();
            Pair<Double, Boolean> value = alert.getValue();
            double projectedValue;
            if(value.second) {
                projectedValue = calcPercent(priceAtTime, value.first, upDirection);
            }
            else {
                projectedValue = value.first;
            }
            sb.append(pair)
                    .append(" current price: ").append(withPrecision(cryptoPairs.get(pair)))
                    .append(", alert at: ");
            if(value.second) {
                sb.append(projectedValue)
                        .append(" (").append(upDirection ? "+" : "-")
                        .append(withPrecision(value.first))
                        .append(" %)");
            }
            else{
                sb.append(projectedValue);
            }
            sb.append("\n");
        }
        if(sb.isEmpty()) {
            sb.append(emptyList());
        }
        return sb.toString();
    }

    public String processInput() {
        String command = inputPairs.get("command");
        String retMessage = "";
        if(command.equalsIgnoreCase(Actions.GET.name())){
            if(isEmpty(simplifiedInput)) {
                retMessage = getOptWithNames();
            }
            else {
                retMessage = getOptSimplified();
            }
        }
        else if(command.equalsIgnoreCase(Actions.ALERT.name())) {
            if(isEmpty(simplifiedInput)) {
                retMessage = alertOptWithNames();
            }
            else {
                retMessage = alertOptSimplified();
            }
        }
        else if(command.equalsIgnoreCase(Actions.ADD.name())) {
            retMessage = addToList(simplifiedInput);
        }
        else if(command.equalsIgnoreCase(Actions.DELETE.name())) {
            retMessage = removeFromList(simplifiedInput);
        }
        else {
            retMessage = unknownOption(command);
        }
        simplifiedInput.clear();
        return retMessage;
    }
    private String normalize(String pair) {
        return pair.replace("/", "").toUpperCase();
    }

    private String removeFromList(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String pair: list) {
            pair = normalize(pair);
            if(!localWatchList.contains(pair)) {
                sb.append(notFound(pair));
            }
            else {
                localWatchList.remove(pair);
                sb.append(successfullyRemoved(pair));
            }
        }
        return sb.toString();
    }

    private String addToList(ArrayList<String> list) {
        int maxAllowed = 10; // way too large messages
        StringBuilder sb = new StringBuilder();
        for (String pair: list) {
            pair = normalize(pair);
            if(!cryptoPairs.containsKey(pair)) {
                sb.append(unknownCryptoPair(pair));
            }
            else if(localWatchList.contains(pair)) {
                sb.append(alreadyInTheList(pair));
            }
            else if(localWatchList.size() > maxAllowed) {
                sb.append(maximumExceeded(maxAllowed));
            }
            else {
                localWatchList.add(pair);
                sb.append(successfullyAdded(pair));
            }
        }
        return sb.toString();
    }

    public String parse(String[] args) {
        boolean awaitArg = false;
        Pair<String, String> keyValue = new Pair<>();
        String startSign = "?";
        boolean firstArg = true;
        boolean simplified = false;

        for(String argument : args) {
            if(firstArg) {
                if(argument.startsWith(startSign)) {
                    inputPairs.put("command", trimFirstChar(argument));
                    firstArg = false;
                }
                else {
                    return firstCommandIssue(argument);
                }
            }
            else if(simplified) {
                simplifiedInput.add(argument);
            }
            else if(awaitArg) {
                keyValue.second = argument;
                inputPairs.put(keyValue.first, keyValue.second);
            }
            // figure out both (key, val) at once
            else if(argument.contains("=")){
                keyValue = splitToKVPair(argument);
                String first = keyValue.first;
                if(optionsMap.containsKey(first) || flagAlternatives.containsKey(first)) {
                    inputPairs.put(first, keyValue.second);
                }
                else {
                    return nonExistentKey(first);
                }
            }
            else{
                if(optionsMap.containsKey(argument) || flagAlternatives.containsKey(argument)) {
                    keyValue.first = argument;
                    awaitArg = true;
                    continue;
                }
                else { // try to do the best effort approach - assume simplified version usage
                    // e.g. ?alert ETHUSDT 10% up
                    simplifiedInput.add(argument);
                    simplified = true;
                }

            }
            awaitArg = false;
        }
        return startSign; // in quotes return 0 for the bot
    }


    // TODO reduce messages to more generic ones
    static String nonExistentKey(String key) {
        return MessageFormat.format("The key \"{0}\" does not exist in the allowed options (?help)\n", key);
    }

    static String firstCommandIssue(String arg) {
        return MessageFormat.format("Invalid argument: {0} - First argument shall start with a question mark (?)\n", arg);
    }

    static String unknownOption(String option) {
        return MessageFormat.format("Unknown option: {0}, for available options use ?help\n", option);
    }

    static String unknownCryptoPair(String pair) {
        return MessageFormat.format("Unknown cryptocurrency pair: {0}, take a look at https://coinmarketcap.com/exchanges/binance\n", pair);
    }

    static String invalidNumberOfArguments() {
        return "Invalid number of arguments, see ?help.\n";
    }

    static String successfullyCreated() {
        return "Alert was successfully created\n";
    }

    static String successfullyAdded(String str) {
        return MessageFormat.format("{0} was successfully added to your watchlist.\n", str);
    }

    static String maximumExceeded(int size) {
        return MessageFormat.format("Maximum exceeded (max allowed: {0})", size);
    }

    static String successfullyRemoved(String str) {
        return MessageFormat.format("{0} was successfully removed from your watchlist.\n", str);
    }

    static String notFound(String str) {
        return MessageFormat.format("{0} was not found in your list.\n", str);
    }

    static String emptyList() {
        return "Your watchlist is empty, add some using \"?add\"\n";
    }

    static String alreadyInTheList(String pair) {
        return MessageFormat.format("{0} is already in your list\n", pair);
    }
}
