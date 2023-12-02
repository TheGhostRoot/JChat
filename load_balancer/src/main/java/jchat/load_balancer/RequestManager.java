package jchat.load_balancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class RequestManager {

    public static String get(String link, Map<String, Object> headers) {
        try {
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con = setAllHeaders(con, headers);
            con.setDoOutput(true);

            return getResponceAndCheckConnection(con);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String post(String link, Map<String, Object> headers) {
        try {
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con = setAllHeaders(con, headers);
            con.setDoOutput(true);

            return getResponceAndCheckConnection(con);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String delete(String link, Map<String, Object> headers) {
        try {
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con = setAllHeaders(con, headers);
            con.setDoOutput(true);

            return getResponceAndCheckConnection(con);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String patch(String link, Map<String, Object> headers) {
        try {
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PATCH");
            con = setAllHeaders(con, headers);
            con.setDoOutput(true);

            return getResponceAndCheckConnection(con);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static HttpURLConnection setAllHeaders(HttpURLConnection con, Map<String, Object> headers) {
        if (headers == null) {
            return con;
        }
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue().toString());
        }
        return con;
    }

    private static String getResponceAndCheckConnection(HttpURLConnection con) {
        try {
            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while (null != (inputLine = in.readLine())) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            return "";
        }
    }
}
