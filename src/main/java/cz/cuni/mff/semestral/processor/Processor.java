package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.actions.Alert;
import cz.cuni.mff.semestral.utilities.Pair;
import cz.cuni.mff.semestral.utilities.Utilities;
import cz.cuni.mff.semestral.processor.User;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Processor {
    // the first char of a command needs to be the trigger sign, otherwise the bot is
    // supposed to idle
    public static String triggerSign = "!";

    private enum Actions {
        ALERT, ADD,
        RMALERT, RMLIST,
        LIST, CURRENT,
        FULLCLEAR, LISTCLEAR, ALERTSCLEAR,
        HELP
    }
    private ArrayList<String> parseInput;
    // active storages
    private HashMap<String, Double> cryptocurrencyPairs;
    private HashMap<String, User> userMap;
    private User user;

    // parse helper mappers
    private final EnumMap<Actions, Command> enumMapper;
    private final EnumMap<Actions, Supplier<String>> simpleFuncMapper;
    private final EnumMap<Actions, Function<String, String>> paramFuncMapper;
    // parser
    private final SimpleParser simple;

    //private EnumMap<Actions, ThreeParameterFunction<Processor, String, String>> multiFuncMapper;
    // in case, there were multiple three param functions

    public Processor() {
        parseInput = new ArrayList<>();
        enumMapper = new EnumMap<>(Actions.class);
        simpleFuncMapper = new EnumMap<>(Actions.class);
        paramFuncMapper = new EnumMap<>(Actions.class);
        simple = new SimpleParser();
        userMap = new HashMap<>();
        FillEnumMaps();
    }

    /**
     * Set available options and their mapping
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
                Actions.CURRENT,
                new Command(MessageFormat.format("{0}current", triggerSign),
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
                        MessageFormat.format("{0}list", triggerSign),
                        "shows your current watchlist"
                )
        );

        simpleFuncMapper.put(Actions.LISTCLEAR, this::ClearList);
        simpleFuncMapper.put(Actions.ALERTSCLEAR, this::ClearAlerts);
        simpleFuncMapper.put(Actions.FULLCLEAR, this::ClearAll);
        simpleFuncMapper.put(Actions.HELP, this::GetHelp);
        simpleFuncMapper.put(Actions.CURRENT, this::GetCurrentAlerts);
        simpleFuncMapper.put(Actions.LIST, this::GetCurrentList);

        paramFuncMapper.put(Actions.ADD, this::AddToWatchlist);
        paramFuncMapper.put(Actions.RMLIST, this::RemoveFromWatchlist);
        paramFuncMapper.put(Actions.RMALERT, this::RemoveFromAlerts);
    }

    private String ProcessCommand(String command, String[] args, int index, String user) {
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
        return simple.ProcessCommand(user);
    }

    /**
     *
     * @return Part of the user input (parsed input command)
     * i.e., !add BTCUSDT !rml BTCUSDT returns !add BTCUSDT in the second call !rml BTCUSDT is removed
     */
    public String GetPartialUserInput() {
        ArrayList<String> temporary = parseInput;
        parseInput = new ArrayList<>();
        return String.join(" ", temporary);
    }

    public HashMap<String, User> GetUserMap() {
        return userMap;
    }

    public String ProcessInput(String[] args, int index, String user) {
        String command = args[index];
        String helpKeyword = enumMapper.get(Actions.HELP).GetName();
        if (command.equals(helpKeyword)) {
            Arrays.fill(args, null);
            return GetHelp();
        }
        else if (command.startsWith(triggerSign)) {
            return ProcessCommand(command, args, index, user);
        }
        else {
            return "";
        }
    }

    public String ClearFinishedAlerts(User calledUser) {
        var usersAlerts = calledUser.GetAlerts();
        StringBuilder sb = new StringBuilder();
        List<Alert> elementsToBeRemoved = usersAlerts.stream()
                .filter(this::IsTriggered)
                .collect(Collectors.toList());
        if (!elementsToBeRemoved.isEmpty()) {
            user.RemoveMultipleAlerts(elementsToBeRemoved);
            for (Alert alert : elementsToBeRemoved) {
                var name = alert.GetCryptocurrencySymbol();
                var projected = alert.GetValue();
                double triggerValue = projected.first;
                // TODO MOVE ABSTRACTION TO ERRORMESSAGE
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

    public void SetCurrentData(HashMap<String, Double> pairs) {
        cryptocurrencyPairs = pairs;
    }

    private boolean IsTriggered(Alert alert) {
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

    private String GetHelp() {
        parseInput.add(enumMapper.get(Actions.HELP).GetName());
        StringBuilder sb = new StringBuilder();
        sb.append("For cryptocurrency pairs, take a look at https://coinmarketcap.com/exchanges/binance\n");
        sb.append("Currently supported commands: \n");
        for (var entry : enumMapper.entrySet()) {
            Command command = entry.getValue();
            sb.append(command.GetLine());
        }
        return sb.toString();
    }

    private String GetCurrentAlerts() {
        var usersAlerts = user.GetAlerts();
        if (usersAlerts.isEmpty()) {
            var command = enumMapper.get(Actions.ALERT);
            return Messenger.EmptyAlerts(command);
        }
        StringBuilder sb = new StringBuilder();
        for (Alert alert : usersAlerts) {
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

    private String RemoveFromAlerts(String symbol) {
        var usersAlerts = user.GetAlerts();
        symbol = Utilities.Normalize(symbol);
        String storage = "alerts";
        StringBuilder sb = new StringBuilder();
        boolean wasFound = false;
        for(var alert : usersAlerts) {
            String alertSymbol = alert.GetCryptocurrencySymbol();
            if(symbol.equals(alertSymbol)) {
                user.RemoveFromAlerts(alert);
                sb.append(Messenger.SuccessfullyRemoved(symbol, storage));
                wasFound = true;
            }
        }
        if(!wasFound) {
            sb.append(Messenger.NotFound(symbol));
        }
        return sb.toString();
    }

    private String RemoveFromWatchlist(String symbol) {
        var usersWatchlist = user.GetWatchlist();
        symbol = Utilities.Normalize(symbol);
        String storage = "watchlist";
        if(usersWatchlist.contains(symbol)) {
            user.RemoveFromWatchlist(symbol);
            return Messenger.SuccessfullyRemoved(symbol, storage);
        }
        else {
            return Messenger.NotFound(symbol);
        }
    }

    private String AddToWatchlist(String symbol) {
        int maxWatchListAllowed = 50;
        var userList = user.GetWatchlist();
        symbol = Utilities.Normalize(symbol);
        if (!cryptocurrencyPairs.containsKey(symbol)) {
            return Messenger.UnknownCryptocurrencySymbol(symbol);
        }
        else if (userList.contains(symbol)) {
            return Messenger.AlreadyInTheList(symbol);
        }
        else if (userList.size() >= maxWatchListAllowed) {
            return Messenger.MaximumExceeded(maxWatchListAllowed);
        }
        else {
            user.AddToWatchList(symbol);
            return Messenger.SuccessfullyAdded(symbol);
        }
    }

    private class SimpleParser {
        public String ProcessCommand(String userName) {
            user = userMap.get(userName);
            if(user == null) {
                user = new User();
                userMap.put(userName, user);
            }
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
                return SetAlert();
            }
            else {
                return Messenger.ParseError();
            }
        }

        private String ProcessSimpleCommand(String input) {
            boolean wasFound = false;
            String returnMessage = null;
            for (var entry : simpleFuncMapper.entrySet()) {
                Command command = enumMapper.get(entry.getKey());
                var func = entry.getValue();
                if(command.GetName().equals(input)) {
                    returnMessage = func.get();
                    wasFound = true;
                    break;
                }
            }
            if(!wasFound) {
                return Messenger.ParseError();
            }
            return returnMessage;
        }

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
                return Messenger.ParseError();
            }
            else {
                return returnMessage;
            }
        }
    }

    //!clear
    private String ClearAll() {
        ClearList();
        ClearAlerts();
        return Messenger.AllCleared();
    }

    // !listclear
    private String ClearList() {
        var usersList = user.GetWatchlist();
        user.ClearWatchlist();
        return Messenger.WatchlistCleared();
    }

    //!alertsclear
    private String ClearAlerts() {
        user.ClearAlerts();
        return Messenger.AlertsCleared();
    }

    private String GetCurrentList() {
        var usersList = user.GetWatchlist();
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

    private String SetAlert() {
        double minPercent = -100;
        String percentSymbol = "%";
        var command = enumMapper.get(Actions.ALERT);

        String firstPart = command.GetName().split(" ")[0];
        String first = parseInput.get(0);
        if(!firstPart.equals(first)) {
            return Messenger.ParseError();
        }
        StringBuilder sb = new StringBuilder();
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
        if((isPercent && value < minPercent) || (!isPercent && value < 0)) {
            return Messenger.ValueError(value, isPercent);
        }

        Alert alert = new Alert();
        alert.SetPair(pair)
                .SetValue(value).SetIsPerc(isPercent)
                .SetPriceAtTime(cryptocurrencyPairs.get(pair));

        var usersAlerts = user.GetAlerts();
        Optional<Alert> duplicate = usersAlerts.stream()
                .filter(member -> pair.equals(member.GetCryptocurrencySymbol()))
                .findAny();

        if (duplicate.isPresent()) {
            var d = duplicate.get();
            if(d.GetValue().first == value) {
                sb.append(Messenger.DuplicateAlertIssue(pair, value, isPercent));
            }
            else {
                user.AddToAlerts(alert);
                sb.append(Messenger.AnotherAlertCreated(pair, value, isPercent));
            }
        }
        else {
            user.AddToAlerts(alert);
            sb.append(Messenger.SuccessfullyCreated(pair, value, isPercent));
        }
        return sb.toString();
    }
}

