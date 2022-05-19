package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.utilities.Utilities;

import java.text.MessageFormat;

/**
 * Class to hide concrete implementation details
 * mostly concerning output formatting
 */
public class Messenger {
    final static String symbolsURL = "https://coinmarketcap.com/exchanges/binance";

    /**
     * @return Generic message in case the user's input does not comply with the rules contained in help
     */
    static String ParseError() {
        return MessageFormat.format(
                "Parse error, take a look at {0}help\n",
                Processor.triggerSign
        );
    }

    /**
     *
     * @param value Numeric (absolute) or percent value of a cryptocurrency based on isPercent
     * @param isPercent percentage
     * @return Message of unsuccessful value parsing
     */
    static String ValueError(double value, boolean isPercent) {
        if(isPercent) {
            return MessageFormat.format(
                    "Value error ({0}%) - with percent, the minimum is -100% (and 0% would be triggered immediately)\n",
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

    static String GetHelpHeader() {
        return MessageFormat.format(
                "For cryptocurrency pairs - {0} Currently supported commands:\n",
                SuggestURL()
        );
    }

    static String AllCleared(String watchListInfo, String alertsInfo) {
        return MessageFormat.format(
                "Full clear initiated:\n{0}\n{1}\n",
                watchListInfo, alertsInfo
        );
    }

    static String GetTriggerValue(String name, double projectedValue, double currentValue) {
        double changeInPercentNeeded = Utilities.CalcPercentFromValues(currentValue, projectedValue);
        return MessageFormat.format(
            "{0} alert triggers at {1} USD ({2} % in change needed)\n - current value: {3} USD\n",
            name, Utilities.NumFormat(projectedValue),
            changeInPercentNeeded, Utilities.NumFormat(currentValue)
        );
    }

    static String SuggestURL() {
        return MessageFormat.format("Take a look at: {0}\n", symbolsURL);
    }

    static String UnknownCryptocurrencySymbol(String symbol) {
        return MessageFormat.format(
                "Unknown cryptocurrency symbol: {0}. {1}\n",
                symbol, SuggestURL()
        );
    }

    static String UnknownAction(String input) {
        return MessageFormat.format("Unknown action: {0}", input);
    }

    static String AlertSuccessfullyCreated(String symbol, double value, boolean isPercent, double currentValue) {
        String valuePart = Utilities.GetValueWithInfo(value, isPercent);
        return MessageFormat.format("Alert for {0} (target value: {1}) was created - current value: {2}\n",
                symbol, valuePart, currentValue
        );
    }

    static String AddedSuccessfullyToWatchList(String symbol) {
        return MessageFormat.format("{0} was successfully added to your watchlist.\n", symbol);
    }

    static String SuccessfullyRemoved(String symbol, String storage) {
        return MessageFormat.format("{0} was successfully removed from your {1}.\n", symbol, storage);
    }

    static String MaximumExceeded(int size) {
        return MessageFormat.format("Maximum exceeded (max allowed: {0})\n", size);
    }

    static String NotFound(String symbol, String storage) {
        return MessageFormat.format("{0} was not found in your {1}", symbol, storage);
    }

    static String AlreadyInTheWatchList(String symbol) {
        return MessageFormat.format("{0} is already in your list\n", symbol);
    }

    static String WatchListIsEmpty() {
        return "Your watchlist is empty";
    }

    static String EmptyList(Command command) {
        return MessageFormat.format(
                "{0}, consider adding some using the command: {1}\n",
                WatchListIsEmpty(), command.GetName()
        );
    }

    static String AlertsAreEmpty() {
        return "Your alerts are empty";
    }

    static String EmptyAlerts(Command command) {
        return MessageFormat.format(
                "{0}, consider adding some using the command: {1} \n",
                AlertsAreEmpty(), command.GetName()
        );
    }

    static String AnotherAlertCreated(String symbol, double value, boolean isPercent, double currentValue) {
        String valuePart = Utilities.GetValueWithInfo(value, isPercent);
        return MessageFormat.format(
                "Another alert for {0} (target value: {1}) was created - current value: {2} \n",
                symbol, valuePart, currentValue
        );
    }

    static String DuplicateAlertIssue(String symbol, double value, boolean isPerc) {
        String valuePart = Utilities.GetValueWithInfo(value, isPerc);
        return MessageFormat.format(
                "Alert {0} already exists with the exact value ({1})\n",
                symbol, valuePart
        );
    }

    static String SignChanged(String startSymbol) {
        return MessageFormat.format("From now on, run the commands starting with {0}\n", startSymbol);
    }

    static String SignLengthIssue(String startSymbol) {
        return MessageFormat.format("The start symbol \"{0}\" is way too long\n", startSymbol);
    }
}
