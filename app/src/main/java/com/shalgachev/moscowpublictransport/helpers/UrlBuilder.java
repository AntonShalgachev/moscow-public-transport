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

    public UrlBuilder(String base, String encoding) {
        mBuilder = new StringBuilder(base);
        mEncoding = encoding;
    }

    public UrlBuilder appendParam(String key, String value) throws UnsupportedEncodingException {
        mBuilder.append(mConnector).append(key).append('=');
        mBuilder.append(URLEncoder.encode(value, mEncoding));
        mConnector = "&";

        return this;
    }

    public String build() {
        return mBuilder.toString();
    }
}
