package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.actions.Alert;

import java.util.*;

/**
 * Data class for containing storages (watchlist and alerts)
 * of a particular user
 */
public class UserData {
    private final SortedSet<String> watchList;
    private final HashMap<String, Alert> alerts;

    public UserData() {
        watchList = new TreeSet<>();
        alerts = new HashMap<>();
    }

    public void AddToWatchList(String symbol) {
        watchList.add(symbol);
    }

    public void RemoveFromWatchlist(String symbol) {
        watchList.remove(symbol);
    }

    public void AddToAlerts(Alert alert) {
        var uniqueKey = alert.toString();
        alerts.put(uniqueKey, alert);
    }

    public void RemoveFromAlerts(List<Alert> elementsToBeRemoved) {
        for (Alert alert: elementsToBeRemoved) {
            alerts.remove(alert.toString());
        }
    }

    public void ClearWatchlist() {
        watchList.clear();
    }

    public void ClearAlerts() {
        alerts.clear();
    }

    public SortedSet<String> GetWatchlist() {
        return watchList;
    }

    public HashMap<String, Alert> GetAlerts() {
        return alerts;
    }
}
