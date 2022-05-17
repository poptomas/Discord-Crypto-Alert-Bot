package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.actions.Alert;
import cz.cuni.mff.semestral.utilities.*;
import jdk.jshell.execution.Util;

import java.lang.reflect.Array;
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

    private ArrayList<String> simplifiedInput;

    // active storages
    private HashMap<String, Double> cryptoPairs;
    private ArrayList<String> localWatchList;
    private ArrayList<Alert> currentAlerts;

    // input parsing helpers
    private EnumMap<Actions, Command> enumMapper;
    private EnumMap<Actions, Supplier<String>> simpleFuncMapper;
    private EnumMap<Actions, Function<String, String>> paramFuncMapper;
    //private EnumMap<Actions, ThreeParameterFunction<Processor, String, String>> multiFuncMapper;
    // in case, there were multiple three param functions

    public Processor() {
        simplifiedInput = new ArrayList<>();
        localWatchList = new ArrayList<>();
        currentAlerts = new ArrayList<>();
        enumMapper = new EnumMap<>(Actions.class);
        simpleFuncMapper = new EnumMap<>(Actions.class);
        paramFuncMapper = new EnumMap<>(Actions.class);
        FillEnumMaps();
    }

    private void FillEnumMaps() {
        enumMapper.put(
                Actions.ADD,
                new Command(
                        MessageFormat.format("{0}add <symbol>", triggerSign),
                        "adds a new cryptocurrency to your watchlist"
                )
        );

        enumMapper.put(
                Actions.ALERT,
                new Command(
                        MessageFormat.format("{0}alert <symbol> <value>", triggerSign),
                        "creates a new alert, i. e., !alert BTCUSDT -5% or !alert BTCUST 31000"
                )
        );

        enumMapper.put(
                Actions.RMLIST,
                new Command(
                        MessageFormat.format("{0}rml <symbol>", triggerSign),
                        "removes the symbol from your current watchlist"
                )
        );

        enumMapper.put(
                Actions.RMALERT,
                new Command(MessageFormat.format("{0}rma <symbol>", triggerSign),
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

        paramFuncMapper.put(Actions.ADD, this::AddToList);
        paramFuncMapper.put(Actions.RMLIST, this::RemoveFromList);
        paramFuncMapper.put(Actions.RMALERT, this::RemoveFromAlerts);
    }

    public void SetCurrentData(HashMap<String, Double> pairs) {
        cryptoPairs = pairs;
    }

    public EnumMap<Actions, Command> GetEnumMapper() {
        return enumMapper;
    }

    public String ClearDoneAlerts() {
        StringBuilder sb = new StringBuilder();
        List<Alert> elementsToBeRemoved = currentAlerts.stream()
                .filter(this::isTriggered)
                .collect(Collectors.toList());
        if (!elementsToBeRemoved.isEmpty()) {
            currentAlerts.removeAll(elementsToBeRemoved);
            for (Alert alert : elementsToBeRemoved) {
                var name = alert.GetCryptocurrencyPair();
                var projected = alert.GetValue();
                double triggerValue = projected.first;
                sb.append("[TRIGGER]: ").append(name);
                if (projected.second) {
                    triggerValue = Utilities.CalcPercent(alert.GetPriceAtTime(), projected.first);
                    sb.append(MessageFormat.format("({0}%)", projected.first));
                }
                sb.append(MessageFormat.format(
                        " value: {0} USD\n", triggerValue)
                );
            }
        }
        return sb.toString();
    }

    private boolean isTriggered(Alert alert) {
        String pair = alert.GetCryptocurrencyPair();
        double currentPrice = cryptoPairs.get(pair);
        double priceAtTime = alert.GetPriceAtTime();
        Pair<Double, Boolean> projected = alert.GetValue();
        double triggerThreshold = projected.first;
        if (projected.second) { // percentage
            triggerThreshold = Utilities.CalcPercent(priceAtTime, projected.first);
        }
        return (
                (priceAtTime < triggerThreshold
                        && triggerThreshold < currentPrice)      // down
                        || (priceAtTime > triggerThreshold
                        && triggerThreshold > currentPrice)      // up
        );
    }

    private String GetHelp() {
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
        if (currentAlerts.isEmpty()) {
            var command = enumMapper.get(Actions.ALERT);
            return ErrorMessage.EmptyAlerts(command);
        }
        StringBuilder sb = new StringBuilder();
        for (Alert alert : currentAlerts) {
            Pair<Double, Boolean> value = alert.GetValue();
            String name = alert.GetCryptocurrencyPair();
            sb.append(name);
            double triggerValue;
            if (value.second) { // is percentage
                var percentage = value.first;
                double originalPrice = alert.GetPriceAtTime();
                triggerValue = Utilities.CalcPercent(originalPrice, percentage);
                sb.append(
                        MessageFormat.format(
                                " (threshold {0} %)", percentage)
                );
            } else {
                triggerValue = value.first;
            }
            sb.append(
                    MessageFormat.format(
                            ": alert triggers at {0} USD (current: {1} USD)\n",
                            triggerValue, cryptoPairs.get(name))
            );
        }
        return sb.toString();
    }

    private String RemoveFromAlerts(String pair) {
        pair = Utilities.Normalize(pair);
        StringBuilder sb = new StringBuilder();
        boolean wasFound = false;
        for(var alert : currentAlerts) {
            if(pair.equals(alert.GetCryptocurrencyPair())) {
                currentAlerts.remove(alert);
                sb.append(ErrorMessage.SuccessfullyRemoved(pair));
                wasFound = true;
                break;
            }
        }
        if(!wasFound) {
            sb.append(ErrorMessage.NotFound(pair));
        }
        return sb.toString();
    }

    private String RemoveFromList(String pair) {
        pair = Utilities.Normalize(pair);

        Utilities.Print(pair);

        Utilities.Print(localWatchList.size());

        StringBuilder sb = new StringBuilder();
        boolean wasFound = false;
        for(String symbol : localWatchList) {
            Utilities.Print(symbol);
            if(pair.equals(symbol)) {
                localWatchList.remove(symbol);
                sb.append(ErrorMessage.SuccessfullyRemoved(pair));
                wasFound = true;
                break;
            }
        }
        if(!wasFound) {
            sb.append(ErrorMessage.NotFound(pair));
        }
        return sb.toString();
    }

    private String AddToList(String pair) {
        int maxAllowed = 10; // way too large messages
        StringBuilder sb = new StringBuilder();
        pair = Utilities.Normalize(pair);
        if (!cryptoPairs.containsKey(pair)) {
            sb.append(ErrorMessage.UnknownCryptoPair(pair));
        } else if (localWatchList.contains(pair)) {
            sb.append(ErrorMessage.AlreadyInTheList(pair));
        } else if (localWatchList.size() > maxAllowed) {
            sb.append(ErrorMessage.MaximumExceeded(maxAllowed));
        } else {
            localWatchList.add(pair);
            sb.append(ErrorMessage.SuccessfullyAdded(pair));
        }
        return sb.toString();
    }

    private class SimpleParser {
        private String ProcessSimpleCommand(String input) {
            boolean wasFound = false;
            String returnMessage = null;
            for (var entry : simpleFuncMapper.entrySet()) {
                Command command = enumMapper.get(entry.getKey());
                var func = entry.getValue();
                if(command.GetName().equals(input)) {
                    returnMessage = func.get();
                    wasFound = true;
                }
            }
            if(!wasFound) {
                return ErrorMessage.ParseError();
            }
            return returnMessage;
        }

        private String ProcessParamCommand(ArrayList<String> input) {
            String returnMessage = null;
            boolean found = false;
            for (var entry : paramFuncMapper.entrySet()) {
                Command command = enumMapper.get(entry.getKey());
                var func = entry.getValue();
                var first = input.get(0);
                var second = input.get(1);
                var firstPart = command.GetName().split(" ")[0];
                if(firstPart.equals(first)) {
                    returnMessage = func.apply(second);
                    found = true;
                    break;
                }
            }
            if(!found) {
                return ErrorMessage.ParseError();
            }
            else {
                return returnMessage;
            }
        }

        public String ProcessInput() {
            if(simplifiedInput.size() == 1) {
                return ProcessSimpleCommand(simplifiedInput.get(0));
            }
            else if(simplifiedInput.size() == 2) {
                return ProcessParamCommand(simplifiedInput);
            }
            else if(simplifiedInput.size() == 3) {
                // currently no other method than the alertion was implemented
                return AlertOption();
            }
            else {
                return ErrorMessage.ParseError();
            }
        }
    }

    //!clear
    private String ClearAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("Full clear initiated:\n")
            .append(ClearList())
            .append("\n")
            .append(ClearAlerts());
        return sb.toString();
    }

    // !listclear
    private String ClearList() {
        localWatchList.clear();
        return ErrorMessage.WatchlistCleared();
    }

    //!alertsclear
    private String ClearAlerts() {
        currentAlerts.clear();
        return ErrorMessage.AlertsCleared();
    }

    private String GetCurrentList() {
        if(localWatchList.isEmpty())  {
            var command = enumMapper.get(Actions.ADD);
            return ErrorMessage.EmptyList(command);
        }
        StringBuilder sBuilder = new StringBuilder();
        for (String pair : localWatchList) {
            sBuilder.append(
                    MessageFormat.format(
                            "{0} {1} USD\n", pair, cryptoPairs.get(pair)
                    )
            );
        }
        return sBuilder.toString();
    }

    private String AlertOption() {
        var command = enumMapper.get(Actions.ALERT);
        var firstPart = command.GetName().split(" ")[0];
        var first = simplifiedInput.get(0);
        if(!firstPart.equals(first)) {
            return ErrorMessage.ParseError();
        }
        StringBuilder sb = new StringBuilder();
        String pair = Utilities.Normalize(simplifiedInput.get(1));
        if (!cryptoPairs.containsKey(pair)) {
            return ErrorMessage.UnknownCryptoPair(pair);
        }
        String strValue = simplifiedInput.get(2);

        boolean isPercent = false;
        if (strValue.endsWith("%")) {
            strValue = Utilities.TrimLastCharacter(strValue);
            isPercent = true;
        }
        double value = Double.parseDouble(strValue);
        Alert alert = new Alert();
        alert.SetPair(pair)
                .SetValue(value).SetIsPerc(isPercent)
                .SetPriceAtTime(cryptoPairs.get(pair));
        
        Optional<Alert> duplicate = currentAlerts.stream()
                .filter(member -> pair.equals(member.GetCryptocurrencyPair())
                ).findAny();
        if (duplicate.isPresent()) {
            var d = duplicate.get();
            if(d.GetValue().first == value) {
                sb.append(ErrorMessage.DuplicateAlertIssue(pair, value, isPercent));
            }
            else {
                currentAlerts.add(alert);
                sb.append(ErrorMessage.AnotherAlertCreated(pair, value, isPercent));
            }
        }
        else {
            currentAlerts.add(alert);
            sb.append(ErrorMessage.SuccessfullyCreated(pair, value, isPercent));
        }
        return sb.toString();
    }

    public String ProcessInput(String[] args) {
        simplifiedInput = new ArrayList<>(Arrays.asList(args));
        for (String arg : args) {
            Utilities.Print(arg);
        }
        String command = args[0];
        String helpKeyword = enumMapper.get(Actions.HELP).GetName();
        if(command.equals(helpKeyword)) {
            return GetHelp();
        }
        else if (command.startsWith(triggerSign)) {
            var simple = new SimpleParser();
            return simple.ProcessInput();
        } else {
            return ""; // normal non-command chat
        }
    }
}

