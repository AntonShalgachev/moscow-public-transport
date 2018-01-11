package com.shalgachev.moscowpublictransport.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/24/2017.
 */

public class Utils {
    private static String TAG = "Utils";
    public static String getCharset(String contentType) {
        String[] tmp = contentType.split("charset=");

        if (tmp.length > 0)
            return tmp[tmp.length - 1];

        return "";
    }

    public static String fetchUrl(String url) {
        try {
            Log.d(TAG, String.format("Fetching %s", url));
            URL website = new URL(url);
            URLConnection connection = website.openConnection();

            String charset = getCharset(connection.getContentType());
            Charset inputCharset = Charset.forName(charset);
            InputStreamReader isr = new InputStreamReader(connection.getInputStream(), inputCharset);
            BufferedReader in = new BufferedReader(isr);

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine).append("\n");

            in.close();

            return response.toString();
        } catch (java.io.IOException e) {
            return "";
        }
    }

    public static List<CharSequence> fetchUrlAsList(String url) {
        String result = fetchUrl(url);

        String[] lines = result.split("\n");

        List<CharSequence> strings = new ArrayList<>();
        for (String line : lines) {
            if (!line.isEmpty())
                strings.add(line);
        }

        return strings;
    }
}
