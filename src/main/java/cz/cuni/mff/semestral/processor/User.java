package cz.cuni.mff.semestral.processor;

import cz.cuni.mff.semestral.actions.Alert;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class User {
    private final SortedSet<String> watchList;
    private final ArrayList<Alert> alerts;

    public User() {
        watchList = new TreeSet<>();
        alerts = new ArrayList<>();
    }

    public void AddToWatchList(String symbol) {
        watchList.add(symbol);
    }

    public void RemoveFromWatchlist(String symbol) {
        watchList.remove(symbol);
    }

    public void AddToAlerts(Alert alert) {
        alerts.add(alert);
    }

    public void RemoveFromAlerts(Alert alert) {
        alerts.remove(alert);
    }

    public void RemoveMultipleAlerts(List<Alert> elementsToBeRemoved) {
        alerts.removeAll(elementsToBeRemoved);
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

    public ArrayList<Alert> GetAlerts() {
        return alerts;
    }
}
