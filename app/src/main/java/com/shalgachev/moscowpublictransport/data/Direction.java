package com.shalgachev.moscowpublictransport.data;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by anton on 5/28/2017.
 */

public class Direction implements Comparable<Direction>, Serializable {
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + getFrom().hashCode();
        hash = 31 * hash + getTo().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Direction other = (Direction) obj;

        return other.getId().equals(getId());
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
