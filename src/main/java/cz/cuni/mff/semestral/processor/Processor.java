package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.actions.Alert;
import cz.cuni.mff.semestral.utilities.Pair;
import cz.cuni.mff.semestral.utilities.Utilities;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Processor {

    /**
     * the first char of a command needs to be a trigger sign, otherwise the bot is
     * supposed to idle (not to answer to a potential regular chat between users)
     */
    public static String triggerSign = "!";

    private enum Actions {
        ALERT, ADD,
        RMALERT, RMLIST,
        LIST, ALERTS,
        FULLCLEAR, LISTCLEAR, ALERTSCLEAR,
        HELP
    }

    /**
     * a data class of the user
     * the processor is currently operating with
     */
    private UserData userData;

    // parsed user input
    private ArrayList<String> parseInput;
    // active storages

    private final HashMap<String, UserData> userMap;
    private HashMap<String, Double> cryptocurrencyPairs;

    // processor helper - mapped delegation based on the length of the input
    private final EnumMap<Actions, Command> enumMapper;
    private final EnumMap<Actions, Supplier<String>> simpleFuncMapper;
    private final EnumMap<Actions, Function<String, String>> paramFuncMapper;

    // runs particular methods based on the delegate
    private final Runner runner;

    public Processor() {
        parseInput = new ArrayList<>();
        enumMapper = new EnumMap<>(Actions.class);
        simpleFuncMapper = new EnumMap<>(Actions.class);
        paramFuncMapper = new EnumMap<>(Actions.class);
        runner = new Runner();
        userMap = new HashMap<>();
        FillEnumMaps();
    }

    /**
     * Sets available options and their mapping
     * to particular functions
     * - with the primary effort to keep the variables
     * on a single place
     */
    private void FillEnumMaps() {
        enumMapper.put(
                Actions.ADD,
                new Command(
                        MessageFormat.format("{0}add [symbol]", triggerSign),
                        "adds a new cryptocurrency to your watchlist"
                )
        );

        enumMapper.put(
                Actions.ALERT,
                new Command(
                        MessageFormat.format("{0}alert [symbol] [value]", triggerSign),
                        "creates a new alert, i. e., !alert BTCUSDT -5% or !alert BTCUST 31000"
                )
        );

        enumMapper.put(
                Actions.RMLIST,
                new Command(
                        MessageFormat.format("{0}rml [symbol]", triggerSign),
                        "removes the symbol from your current watchlist"
                )
        );

        enumMapper.put(
                Actions.RMALERT,
                new Command(MessageFormat.format("{0}rma [symbol]", triggerSign),
                        "removes the symbol your current alerts"
                )
        );

        enumMapper.put(
                Actions.LISTCLEAR,
                new Command(
                        MessageFormat.format("{0}clearl", triggerSign),
                        "clears your current watchlist"
                )
        );
        enumMapper.put(
                Actions.ALERTSCLEAR,
                new Command(
                        MessageFormat.format("{0}cleara", triggerSign),
                        "clears your current alerts"
                )
        );
        enumMapper.put(
                Actions.FULLCLEAR,
                new Command(
                        MessageFormat.format("{0}clear", triggerSign),
                        "clears all your current watchlist and alerts"
                )
        );
        enumMapper.put(
                Actions.ALERTS,
                new Command(MessageFormat.format("{0}alerts", triggerSign),
                        "shows your currently assigned alerts"
                )
        );
        enumMapper.put(
                Actions.HELP,
                new Command(MessageFormat.format("{0}help", triggerSign),
                        "shows this help"
                )
        );
        enumMapper.put(
                Actions.LIST,
                new Command(
                        MessageFormat.format("{0}watchlist", triggerSign),
                        "shows your current watchlist"
                )
        );

        simpleFuncMapper.put(Actions.LISTCLEAR, this::ClearWatchList);
        simpleFuncMapper.put(Actions.ALERTSCLEAR, this::ClearAlerts);
        simpleFuncMapper.put(Actions.FULLCLEAR, this::ClearAll);
        simpleFuncMapper.put(Actions.HELP, this::GetHelp);
        simpleFuncMapper.put(Actions.ALERTS, this::GetAlerts);
        simpleFuncMapper.put(Actions.LIST, this::GetWatchList);

        paramFuncMapper.put(Actions.ADD, this::AddToWatchlist);
        paramFuncMapper.put(Actions.RMLIST, this::RemoveFromWatchlist);
        paramFuncMapper.put(Actions.RMALERT, this::RemoveFromAlerts);
    }

    /**
     *
     * @param userInput remaining user input to be processed
     * @param index index at which the processor is at
     * @param userName user's nickname on Discord
     * @return Message of successful completion or failure during processing
     * - returns an empty string when no trigger sign is encountered
     * in order not to answer to potentially casual messages not targeted
     * for the bot
     */
    public String ProcessInput(String[] userInput, int index, String userName) {
        String command = userInput[index].toLowerCase();
        String helpKeyword = enumMapper.get(Actions.HELP).GetName();
        if (command.equals(helpKeyword)) {
            Arrays.fill(userInput, null);
            return GetHelp();
        }
        else if (command.startsWith(triggerSign)) {
            return ProcessCommand(command, userInput, index, userName);
        }
        else {
            return "";
        }
    }

    /**
     * Clears finished alerts (if exist) of a particular user
     * @param calledUser User of whom the function removes triggered (finished) alerts
     * @return Message containing alerts with symbols and values which the triggers were initiated,
     * otherwise, if nothing is found, an empty string is returned
     */
    public String ClearFinishedAlerts(UserData calledUser) {
        var usersAlerts = calledUser.GetAlerts();
        StringBuilder sb = new StringBuilder();
        List<Alert> finishedAlerts = usersAlerts
                .values().stream()
                .filter(this::IsAlertTriggered)
                .collect(Collectors.toList());
        if (!finishedAlerts.isEmpty()) {
            userData.RemoveFromAlerts(finishedAlerts);
            for (Alert alert : finishedAlerts) {
                var name = alert.GetCryptocurrencySymbol();
                var projected = alert.GetValue();
                double triggerValue = projected.first;
                // TODO move abstraction to Message class
                sb.append(name);
                if (projected.second) {
                    triggerValue = Utilities.CalcValueFromPercent(alert.GetPriceAtTime(), projected.first);
                    sb.append(MessageFormat.format("({0}%)", projected.first));
                }
                sb.append(MessageFormat.format(
                        " value: {0} USD\n", triggerValue)
                );
            }
        }
        return sb.toString();
    }

    /**
     * API method to return the current "progress"
     * while processing the user input
     * @return The part of the processed input:
     * For instance, !add BTCUSDT !rml BTCUSDT returns !add BTCUSDT during the first call of this method,
     * afterward, in the second call !rml BTCUSDT is returned
     */
    public String GetPartialUserInput() {
        ArrayList<String> temporary = parseInput;
        parseInput = new ArrayList<>();
        return String.join(" ", temporary);
    }

    public HashMap<String, UserData> GetUserMap() {
        return userMap;
    }

    public void SetCurrentData(HashMap<String, Double> pairs) {
        cryptocurrencyPairs = pairs;
    }

    /**
     * Checks whether the initially entered value (or the equivalent amount in case of
     * percents) surpassed the current value of a particular cryptocurrency symbol
     * (stored in the alert class)
     * @param alert Alert (containing the cryptocurrency symbol and its price at which the alert was created)
     *              which is supposed to be examined
     * @return Returns the decision whether the alert was triggered
     */
    private boolean IsAlertTriggered(Alert alert) {
        String alertSymbol = alert.GetCryptocurrencySymbol();
        double currentPrice = cryptocurrencyPairs.get(alertSymbol);
        double priceAtTime = alert.GetPriceAtTime();
        Pair<Double, Boolean> projected = alert.GetValue();
        double triggerThreshold = projected.first;
        if (projected.second) { // percentage
            triggerThreshold = Utilities.CalcValueFromPercent(priceAtTime, triggerThreshold);
        }
        return (
                (priceAtTime < triggerThreshold
                        && triggerThreshold < currentPrice)      // down
                        || (priceAtTime > triggerThreshold
                        && triggerThreshold > currentPrice)      // up
        );
    }

    /**
     *
     * @param command Action command intended by the user
     * @param args User input
     * @param index Index at which args the method is at
     * @param userName user's nickname on Discord
     * @return Return value concerning completion/fail of the intended action
     */
    private String ProcessCommand(String command, String[] args, int index, String userName) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(command);
        int finalIdx = index + 1;
        for(; finalIdx < args.length; ++finalIdx) {
            String arg = args[finalIdx];
            if(arg.startsWith(triggerSign)) {
                break;
            }
            arguments.add(arg);
        }
        parseInput = arguments;
        return runner.ProcessCommand(userName);
    }

    /**
     * Helper command which displays currently supported commands
     * @return Formatted message containing the supported commands
     */
    private String GetHelp() {
        parseInput.add(enumMapper.get(Actions.HELP).GetName());
        StringBuilder sb = new StringBuilder();

        sb.append(Messenger.GetHelpHeader());
        for (var entry : enumMapper.entrySet()) {
            Command command = entry.getValue();
            sb.append(command.GetLine());
        }
        return sb.toString();
    }

    /**
     *
     * @return Returns a formatted message containing alerts of a particular user
     * - if the user's alert storage is empty, the message (with a command how to add an alert) is returned
     */
    private String GetAlerts() {
        var usersAlerts = userData.GetAlerts();
        if (usersAlerts.isEmpty()) {
            var command = enumMapper.get(Actions.ALERT);
            return Messenger.EmptyAlerts(command);
        }
        StringBuilder sb = new StringBuilder();

        for (Alert alert : usersAlerts.values()) {
            Pair<Double, Boolean> value = alert.GetValue();
            String name = alert.GetCryptocurrencySymbol();
            double triggerValue;
            boolean isPercent = value.second;
            if (isPercent) { // is percentage
                var percentage = value.first;
                double originalPrice = alert.GetPriceAtTime();
                triggerValue = Utilities.CalcValueFromPercent(originalPrice, percentage);
            }
            else {
                triggerValue = value.first;
            }
            sb.append(
                    Messenger.GetTriggerValue(name, triggerValue, cryptocurrencyPairs.get(name))
            );
        }
        return sb.toString();
    }

    /**
     * Removes alert(s) from the user's alerts storage based on the entered symbol
     * @param symbol Cryptocurrency symbol
     * @return Message
     * - in case of success the enumeration of successful removals
     * - otherwise, the information that the cryptocurrency symbol was not found
     */
    private String RemoveFromAlerts(String symbol) {
        var usersAlerts = userData.GetAlerts();
        symbol = Utilities.Normalize(symbol);
        String storage = "alerts";
        StringBuilder sb = new StringBuilder();
        Utilities.Print(usersAlerts.size());
        ArrayList<Alert> removalList = new ArrayList<>();

        for(var alertPair : usersAlerts.entrySet()) {
            var uniqueKey = alertPair.getKey();
            var alert = alertPair.getValue();
            if(uniqueKey.startsWith(symbol)) {
                removalList.add(alert);
                sb.append(Messenger.SuccessfullyRemoved(symbol, storage));
            }
        }
        if(removalList.isEmpty()) {
            sb.append(Messenger.NotFound(symbol, storage));
        }
        else {
            userData.RemoveFromAlerts(removalList);
        }
        return sb.toString();
    }

    /**
     * Tries to add the symbol to the watchlist,
     * returning message varies upon success and failed operations
     * such as that the symbol is already in the list, unknown symbol,
     * or that maximum allowed capacity was exceeded
     * @param symbol Cryptocurrency symbol
     * @return Message containing the result of the operation
     */
    private String AddToWatchlist(String symbol) {
        int maxWatchListAllowed = 50;
        var userList = userData.GetWatchlist();
        symbol = Utilities.Normalize(symbol);
        if (!cryptocurrencyPairs.containsKey(symbol)) {
            return Messenger.UnknownCryptocurrencySymbol(symbol);
        }
        else if (userList.contains(symbol)) {
            return Messenger.AlreadyInTheWatchList(symbol);
        }
        else if (userList.size() >= maxWatchListAllowed) {
            return Messenger.MaximumExceeded(maxWatchListAllowed);
        }
        else {
            userData.AddToWatchList(symbol);
            return Messenger.AddedSuccessfullyToWatchList(symbol);
        }
    }

    /**
     * Removes a symbol from the user's watchlist
     * @param symbol Cryptocurrency symbol
     * @return Message
     * - in case of success, the information of successful removal
     * - otherwise, the information that the cryptocurrency symbol was not found
     */
    private String RemoveFromWatchlist(String symbol) {
        var usersWatchlist = userData.GetWatchlist();
        symbol = Utilities.Normalize(symbol);
        String storage = "watchlist";
        if(usersWatchlist.contains(symbol)) {
            userData.RemoveFromWatchlist(symbol);
            return Messenger.SuccessfullyRemoved(symbol, storage);
        }
        else {
            return Messenger.NotFound(symbol, storage);
        }
    }

    private String GetWatchList() {
        var usersList = userData.GetWatchlist();
        if(usersList.isEmpty())  {
            var command = enumMapper.get(Actions.ADD);
            return Messenger.EmptyList(command);
        }
        StringBuilder sBuilder = new StringBuilder();
        for (String symbol : usersList) {
            Pair<String, Double> pair = new Pair<>(symbol, cryptocurrencyPairs.get(symbol));
            sBuilder.append(pair);
        }
        return sBuilder.toString();
    }

    /**
     * Clears both alert and watchlist storages
     * @return Message concerning the completion of the action
     */
    private String ClearAll() {
        String watchListReturnValue = ClearWatchList();
        String alertsReturnValue = ClearAlerts();
        return Messenger.AllCleared(watchListReturnValue, alertsReturnValue);
    }

    /**
     * @return Message of a successful completion/info that
     * the alert storage is already empty
     */
    private String ClearWatchList() {
        var usersList = userData.GetWatchlist();
        if(usersList.isEmpty()) {
            return Messenger.WatchListIsEmpty();
        }
        else {
            userData.ClearWatchlist();
            return Messenger.WatchlistCleared();
        }
    }

    /**
     * @return Message of a successful completion/info that
     * the alert storage is already empty
     */
    private String ClearAlerts() {
        var usersList = userData.GetAlerts();
        if(usersList.isEmpty()) {
            return Messenger.AlertsAreEmpty();
        }
        else {
            userData.ClearAlerts();
            return Messenger.AlertsCleared();
        }
    }

    /**
     * Adds a new alert to the user's storage,
     * checks whether the entered input is compliant with the help provided
     * @return Message concerning successful completion or
     * failure, i. e., non-compliant input values, duplicate finding notice
     */
    private String AddToAlerts() {
        double minPercent = -100;
        String percentSymbol = "%";
        var command = enumMapper.get(Actions.ALERT);

        String firstPart = command.GetName().split(" ")[0];
        String first = parseInput.get(0);
        if(!firstPart.equals(first)) {
            return Messenger.ParseError();
        }
        String pair = Utilities.Normalize(parseInput.get(1));
        if (!cryptocurrencyPairs.containsKey(pair)) {
            return Messenger.UnknownCryptocurrencySymbol(pair);
        }
        String strValue = parseInput.get(2);

        boolean isPercent = false;
        if (strValue.endsWith(percentSymbol)) {
            strValue = Utilities.TrimLastCharacter(strValue);
            isPercent = true;
        }
        double value = Double.parseDouble(strValue);
        if((isPercent && value < minPercent)
                || (isPercent && value == 0)
                || (!isPercent && value < 0)) {
            return Messenger.ValueError(value, isPercent);
        }

        Alert alert = new Alert();
        alert.SetPair(pair)
                .SetValue(value).SetIsPerc(isPercent)
                .SetPriceAtTime(cryptocurrencyPairs.get(pair));

        var usersAlerts = userData.GetAlerts();
        Optional<Alert> duplicate = usersAlerts
                .values().stream()
                .filter(member -> pair.equals(member.GetCryptocurrencySymbol()))
                .findAny();

        if (duplicate.isPresent()) {
            var d = duplicate.get();
            if(d.GetValue().first == value) {
                return Messenger.DuplicateAlertIssue(pair, value, isPercent);
            }
            else {
                userData.AddToAlerts(alert);
                return Messenger.AnotherAlertCreated(pair, value, isPercent);
            }
        }
        else {
            userData.AddToAlerts(alert);
            return Messenger.AlertSuccessfullyCreated(pair, value, isPercent);
        }
    }

    /**
     * Runner class which delegates commands based
     * on the size of the input
     */
    private class Runner {
        /**
         * Adds the user to the superclass' user hashmap,
         * in case, the userName has never been encountered
         * @param userName user's nickname on Discord
         */
        private void AddUserIfNotExists(String userName) {
            userData = userMap.get(userName);
            if(userData == null) {
                userData = new UserData();
                userMap.put(userName, userData);
            }
        }

        /**
         * Delegates commands based on the number of arguments
         * of the input
         *
         * @param userName user's nickname on Discord
         * @return Propagated return values from particular superclass' methods
         * - in case of invalid input argument size parse error message is returned
         */
        public String ProcessCommand(String userName) {
            AddUserIfNotExists(userName);
            if(parseInput.size() == 1) {
                return ProcessSimpleCommand(parseInput.get(0));
            }
            else if(parseInput.size() == 2) {
                return ProcessParamCommand(parseInput);
            }
            else if(parseInput.size() == 3) {
                // currently, no other method than the alert command was implemented
                // otherwise in case of extension, for instance,
                // ProcessMultiParamCommand(parseInput) implementation is suggested
                return AddToAlerts();
            }
            else {
                return Messenger.ParseError();
            }
        }

        /**
         * Processes simple parameterless command (i. e. !alerts)
         * @param action Action command intended by the user
         * @return Return value of a particular delegated function
         */
        private String ProcessSimpleCommand(String action) {
            boolean wasFound = false;
            String returnMessage = null;
            for (var entry : simpleFuncMapper.entrySet()) {
                Command command = enumMapper.get(entry.getKey());
                var func = entry.getValue();
                if(command.GetName().equals(action)) {
                    returnMessage = func.get();
                    wasFound = true;
                    break;
                }
            }
            if(!wasFound) {
                return Messenger.UnknownAction(action);
            }
            return returnMessage;
        }

        /**
         * Processes a two-argument command (i. e. !add BTCUSDT)
         * @param input User input
         * @return Return value of a particular function call
         * - in case no function was found, an error message is returned
         */
        private String ProcessParamCommand(ArrayList<String> input) {
            String returnMessage = null;
            boolean wasFound = false;
            for (var entry : paramFuncMapper.entrySet()) {
                Command command = enumMapper.get(entry.getKey());
                var func = entry.getValue();
                var first = input.get(0);
                var second = input.get(1);
                var firstPart = command.GetName().split(" ")[0];
                if(firstPart.equals(first)) {
                    returnMessage = func.apply(second);
                    wasFound = true;
                    break;
                }
            }
            if(!wasFound) {
                return Messenger.UnknownAction(String.join(" ", input));
            }
            else {
                return returnMessage;
            }
        }
    }
}

