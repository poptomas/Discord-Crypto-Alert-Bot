package cz.cuni.mff.semestral.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.cuni.mff.semestral.utilities.Utilities;

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

    public HashMap<String, Double> jsonParse(String responseBody) {
        HashMap<String, Double> cryptocurrencyPairs = new HashMap<>();
        JsonArray jsonArr = JsonParser.parseString(responseBody).getAsJsonArray();
        for (JsonElement elem: jsonArr) {
            String pair = elem.getAsJsonObject().get("symbol").toString();
            String pairTrim = Utilities.TrimQuotes(pair); // "ETHUSDT" symbol by default
            String price = elem.getAsJsonObject().get("price").toString();
            String priceTrim = Utilities.TrimQuotes(price);
            try {
                double convertedPrice = Double.parseDouble(priceTrim);
                cryptocurrencyPairs.put(pairTrim, convertedPrice);
            }
            catch(Exception ex) {
                Utilities.Print("Convertibility issue");
            }
        }

        /*
        for (Map.Entry<String, Double> val:
                cryptocurrencyPairs.entrySet()) {
            System.out.println(val.getKey() + ": " + val.getValue());
        }*/
        return cryptocurrencyPairs;
    }

    public String connect() throws IOException {
        URL url = new URL(binance + dir);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(delay);
        if(connection.getResponseCode() == okCode){
            StringBuilder stringBuilder = new StringBuilder();
            try(InputStreamReader iStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(iStreamReader)
            ) {
                String line;
                while(true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    stringBuilder.append(line);
                }
            }
            return stringBuilder.toString();
        }
        return null;
    }
}
