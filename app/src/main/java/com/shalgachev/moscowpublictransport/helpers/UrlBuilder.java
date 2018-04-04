package com.shalgachev.moscowpublictransport.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by anton on 3/27/2018.
 */

public class UrlBuilder {
    private StringBuilder mBuilder;
    private String mEncoding;

    private String mConnector = "?";

    public UrlBuilder(String base, String encoding) throws UnsupportedEncodingException {
        mBuilder = new StringBuilder(base);
        mEncoding = encoding;

        URLEncoder.encode("test", mEncoding);
    }

    public UrlBuilder(String base ) throws UnsupportedEncodingException {
        this(base, "UTF-8");
    }

    public UrlBuilder appendParam(String key, String value) {
        mBuilder.append(mConnector).append(key).append('=');
        try {
            mBuilder.append(URLEncoder.encode(value, mEncoding));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Encoding should have been tested in constructor");
        }
        mConnector = "&";

        return this;
    }

    public String build() {
        return mBuilder.toString();
    }
}
