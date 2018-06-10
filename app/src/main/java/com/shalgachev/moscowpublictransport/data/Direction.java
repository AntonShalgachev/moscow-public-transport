package com.shalgachev.moscowpublictransport.data;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by anton on 5/28/2017.
 */

public class Direction implements Comparable<Direction>, Serializable {
    private String mId;
    private String mFrom;
    private String mTo;

    public Direction(String id, String from, String to) {
        mId = id;
        mFrom = from;
        mTo = to;
    }

    public Direction(String id) {
        mId = id;
    }

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
        return other.getId().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return mId;
    }

    public void setEndpoints(String from, String to) {
        mFrom = from;
        mTo = to;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getTo() {
        return mTo;
    }

    public String getId() {
        return mId;
    }
}
