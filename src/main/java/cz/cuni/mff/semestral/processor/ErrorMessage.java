package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.utilities.Utilities;

import java.text.MessageFormat;

public class ErrorMessage {

    static String ParseError() {
        return MessageFormat.format(
                "Parse error, take a look at {0}help\n",
                Processor.triggerSign
        );
    }

    static String AlertsCleared() {
        return "Your alerts were cleared";
    }

    static String WatchlistCleared() {
        return "Your watchlist was cleared";
    }

    static String UnknownCryptoPair(String pair) {
        return MessageFormat.format(
                "Unknown cryptocurrency pair: {0}, take a look at https://coinmarketcap.com/exchanges/binance\n",
                pair
        );
    }

    static String SuccessfullyCreated(String symbol, double value, boolean isPerc) {
        String valuePart = Utilities.GetValueWithInfo(value, isPerc);
        return MessageFormat.format("Alert {0} (value: {1}) was successfully created\n",
                symbol, valuePart
        );
    }

    static String AnotherAlertCreated(String symbol, double value, boolean isPerc) {
        String valuePart = Utilities.GetValueWithInfo(value, isPerc);
        return MessageFormat.format("Another alert for {0} was created (value: {1})", symbol, valuePart);
    }

    static String DuplicateAlertIssue(String symbol, double value, boolean isPerc) {
        String valuePart = Utilities.GetValueWithInfo(value, isPerc);
        return MessageFormat.format("Alert {0} already exists with the exact value ({1})", symbol, valuePart);
    }


    static String SuccessfullyAdded(String str) {
        return MessageFormat.format("{0} was successfully added to your watchlist.\n", str);
    }

    static String MaximumExceeded(int size) {
        return MessageFormat.format("Maximum exceeded (max allowed: {0})", size);
    }

    static String SuccessfullyRemoved(String str) {
        return MessageFormat.format("{0} was successfully removed from your watchlist.\n", str);
    }

    static String NotFound(String str) {
        return MessageFormat.format("{0} was not found in your list.\n", str);
    }

    static String AlreadyInTheList(String pair) {
        return MessageFormat.format("{0} is already in your list\n", pair);
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
