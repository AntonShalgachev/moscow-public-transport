package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 6/24/2017.
 */

public class Stop implements Serializable {
    public TransportType transportType;
    public Route route;
    public CharSequence daysMask;
    public Direction direction;
    public CharSequence name;
    public int id;

    public Stop(TransportType transportType, Route route, CharSequence daysMask, Direction direction, CharSequence name, int id) {
        this.transportType = transportType;
        this.route = route;
        this.daysMask = daysMask;
        this.direction = direction;
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stop stop = (Stop) o;

        if (transportType != stop.transportType) return false;
        if (!route.equals(stop.route)) return false;
        if (!daysMask.equals(stop.daysMask)) return false;
        if (!direction.equals(stop.direction)) return false;
        if (!name.equals(stop.name)) return false;
        if (id != stop.id) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = transportType.hashCode();
        result = 31 * result + route.hashCode();
        result = 31 * result + daysMask.hashCode();
        result = 31 * result + direction.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(transportType.name()).append(",");
        sb.append(route).append(",");
        sb.append(daysMask).append(",");
        sb.append(direction.getId()).append(",");
        sb.append(name).append(",");
        sb.append(id).append(",");

        return sb.toString();
    }
}
