package com.shalgachev.moscowpublictransport.data;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by anton on 5/28/2017.
 */

public class Direction implements Comparable<Direction>, Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Direction direction = (Direction) o;

        return mId.equals(direction.mId);

    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public int compareTo(@NonNull Direction other) {
        return other.getId().toString().compareTo(other.toString());
    }

    public Direction(CharSequence id, CharSequence from, CharSequence to) {
        mId = id;
        mFrom = from;
        mTo = to;
    }

    public Direction(CharSequence id) {
        mId = id;
    }

    public void setEndpoints(CharSequence from, CharSequence to) {
        mFrom = from;
        mTo = to;
    }

    public CharSequence getFrom() {
        return mFrom;
    }

    public CharSequence getTo() {
        return mTo;
    }

    public CharSequence getId() {
        return mId;
    }

    private CharSequence mId;
    private CharSequence mFrom;
    private CharSequence mTo;
}
