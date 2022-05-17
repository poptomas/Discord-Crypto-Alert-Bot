package cz.cuni.mff.semestral.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class BinanceConnection {
    final String binance = "https://api.binance.com";
    final String dir = "/api/v3/ticker/price";
    final String method = "GET";
    final int delay = 5000;
    final int okCode = 200;
    private HashMap<String, Double> cryptocurrencyPairs;

    public BinanceConnection() {
        cryptocurrencyPairs = new HashMap<>();
    }

    private String trimQuotes(String token) {
        return token.substring(1, token.length() - 1);
    }

    private void jsonParse(String responseBody) {
        JsonArray jsonArr = JsonParser.parseString(responseBody).getAsJsonArray();
        for (JsonElement elem: jsonArr) {
            String pair = elem.getAsJsonObject().get("symbol").toString();
            String pairTrim = trimQuotes(pair); // "ETHUSDT" by default
            String price = elem.getAsJsonObject().get("price").toString();
            String priceTrim = trimQuotes(price);
            try {
                double convertedPrice = Double.parseDouble(priceTrim);
                cryptocurrencyPairs.put(pairTrim, convertedPrice);
            }
            catch(Exception ex) {
                System.err.println("issue");
            }
        }
    }

    public void connect() throws IOException {
        URL url = new URL(binance + dir);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(delay);
        if(connection.getResponseCode() == okCode){
            InputStreamReader iStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(iStreamReader);
            String line;
            StringBuilder sBuilder = new StringBuilder();
            while(true) {
                line = reader.readLine();
                if(line == null) {
                    break;
                }
                sBuilder.append(line);
            }
            reader.close();
            jsonParse(sBuilder.toString());
        }
    }
}
