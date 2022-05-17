package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.utilities.Utilities;
import java.text.MessageFormat;

public class Messenger {
    final static String symbolsURL = "https://coinmarketcap.com/exchanges/binance";


    static String ParseError() {
        return MessageFormat.format(
                "Parse error, take a look at {0}help\n",
                Processor.triggerSign
        );
    }

    static String ValueError(double value, boolean isPercent) {
        if(isPercent) {
            return MessageFormat.format(
                    "Value error ({0}%) - with percent, the minimum is -100%\n",
                    Utilities.NumFormat(value)
            );
        }
        else {
            return MessageFormat.format(
                    "Value error ({0} USD) - the target value of cryptocurrency never gets below zero\n",
                    Utilities.NumFormat(value)
            );
        }
    }

    static String AlertsCleared() {
        return "Your alerts were cleared";
    }

    static String WatchlistCleared() {
        return "Your watchlist was cleared";
    }

    static String AllCleared( ) {
        return MessageFormat.format(
                "Full clear initiated:\n{0}\n{1}\n",
                WatchlistCleared(), AlertsCleared()
        );
    }

    static String UnknownCryptocurrencySymbol(String symbol) {
        return MessageFormat.format(
                "Unknown cryptocurrency symbol: {0}. {1}\n",
                SuggestURL(), symbol
        );
    }

    static String GetTriggerValue(String name, double projectedValue, double currentValue) {
        double changeInPercentNeeded = Utilities.CalcPercentFromValues(currentValue, projectedValue);
        return MessageFormat.format(
            "{0} alert triggers at {1} USD ({2} % in change needed)\n - current price: {3} USD\n",
            name, Utilities.NumFormat(projectedValue),
            changeInPercentNeeded, Utilities.NumFormat(currentValue)
        );
    }

    static String SuggestURL() {
        return MessageFormat.format("Take a look at {0}", symbolsURL);
    }

    static String SuccessfullyCreated(String symbol, double value, boolean isPercent) {
        String valuePart = Utilities.GetValueWithInfo(value, isPercent);
        return MessageFormat.format("Alert for {0} (value: {1}) was successfully created\n",
                symbol, valuePart
        );
    }

    static String AnotherAlertCreated(String symbol, double value, boolean isPercent) {
        String valuePart = Utilities.GetValueWithInfo(value, isPercent);
        return MessageFormat.format(
                "Another alert for {0} was created (value: {1})\n",
                symbol, valuePart
        );
    }

    static String DuplicateAlertIssue(String symbol, double value, boolean isPerc) {
        String valuePart = Utilities.GetValueWithInfo(value, isPerc);
        return MessageFormat.format(
                "Alert {0} already exists with the exact value ({1})\n",
                symbol, valuePart
        );
    }

    static String SuccessfullyAdded(String symbol) {
        return MessageFormat.format("{0} was successfully added to your watchlist.\n", symbol);
    }

    static String MaximumExceeded(int size) {
        return MessageFormat.format("Maximum exceeded (max allowed: {0})\n", size);
    }

    static String SuccessfullyRemoved(String symbol, String storage) {
        return MessageFormat.format("{0} was successfully removed from your {1}.\n", symbol, storage);
    }

    static String NotFound(String symbol) {
        return MessageFormat.format("{0} was not found in your list.\n", symbol);
    }

    static String AlreadyInTheList(String symbol) {
        return MessageFormat.format("{0} is already in your list\n", symbol);
    }

    static String EmptyList(Command command) {
        return MessageFormat.format(
                "Your watchlist is empty, add some using {0} command\n",
                command.GetName()
        );
    }

    static String EmptyAlerts(Command command) {
        return MessageFormat.format(
                "Your alerts are empty, add some using {0} command\n",
                command.GetName()
        );
    }
}
