package com.shalgachev.moscowpublictransport.data.db;

import com.shalgachev.moscowpublictransport.data.TransportType;

/**
 * Created by anton on 7/2/2017.
 */

public class SQLStop {
    public CharSequence providerId;
    public TransportType transportType;
    public CharSequence route;
    public CharSequence daysMask;
    public CharSequence directionId;
    public CharSequence directionFrom;
    public CharSequence directionTo;
    public CharSequence name;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CharSequence getProviderId() {
        return providerId;
    }

    public void setProviderId(CharSequence providerId) {
        this.providerId = providerId;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public CharSequence getRoute() {
        return route;
    }

    public void setRoute(CharSequence route) {
        this.route = route;
    }

    public CharSequence getDaysMask() {
        return daysMask;
    }

    public void setDaysMask(CharSequence daysMask) {
        this.daysMask = daysMask;
    }

    public CharSequence getDirectionId() {
        return directionId;
    }

    public void setDirectionId(CharSequence directionId) {
        this.directionId = directionId;
    }

    public CharSequence getDirectionFrom() {
        return directionFrom;
    }

    public void setDirectionFrom(CharSequence directionFrom) {
        this.directionFrom = directionFrom;
    }

    public CharSequence getDirectionTo() {
        return directionTo;
    }

    public void setDirectionTo(CharSequence directionTo) {
        this.directionTo = directionTo;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }
}
