package com.shalgachev.moscowpublictransport.data;

/**
 * Created by anton on 5/28/2017.
 */

public class Direction {
    private CharSequence mFrom;
    private CharSequence mTo;

    public Direction(CharSequence from, CharSequence to) {
        mFrom = from;
        mTo = to;
    }

    public CharSequence getFrom() {
        return mFrom;
    }

    public CharSequence getTo() {
        return mTo;
    }
}
