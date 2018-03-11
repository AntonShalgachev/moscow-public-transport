package com.shalgachev.moscowpublictransport.data;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/24/2017.
 */

public class Utils {
    private static String LOG_TAG = "Utils";

    @Nullable
    public static String getCharset(String contentType) {
        if (contentType == null)
            return null;

        String[] tmp = contentType.split("charset=");

        if (tmp.length > 0)
            return tmp[tmp.length - 1];

        return null;
    }

    public static boolean isInternetAvailable() {
        try {
            final InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            return false;
        }
    }

    @Nullable
    public static String fetchUrl(String url) {
        if (!isInternetAvailable())
            return null;

        try {
            Log.d(LOG_TAG, String.format("Fetching %s", url));
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.connect();

            InputStreamReader isr;
            try {
                String charset = getCharset(connection.getContentType());
                Charset inputCharset = Charset.forName(charset);
                isr = new InputStreamReader(connection.getInputStream(), inputCharset);
            } catch (IllegalCharsetNameException e) {
                isr = new InputStreamReader(connection.getInputStream());
            }
            BufferedReader in = new BufferedReader(isr);

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine).append("\n");

            in.close();

            return response.toString();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static List<CharSequence> fetchUrlAsList(String url) {
        String result = fetchUrl(url);

        if (result == null)
            return null;

        String[] lines = result.split("\n");

        List<CharSequence> strings = new ArrayList<>();
        for (String line : lines) {
            if (!line.isEmpty())
                strings.add(line);
        }

        return strings;
    }

    @Nullable
    public static List<String> fetchUrlAsStringList(String url) {
        List<CharSequence> lines = fetchUrlAsList(url);

        if (lines == null)
            return null;

        List<String> strings = new ArrayList<>();
        for (CharSequence line : lines)
            strings.add(line.toString());

        return strings;
    }
}
