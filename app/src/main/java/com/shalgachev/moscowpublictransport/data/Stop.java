package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 6/24/2017.
 */

public class Stop implements Serializable {
    public Stop(CharSequence providerId, TransportType transportType, CharSequence route, CharSequence daysMask, Direction direction, CharSequence name) {
        this.providerId = providerId;
        this.transportType = transportType;
        this.route = route;
        this.daysMask = daysMask;
        this.direction = direction;
        this.name = name;
    }

    public CharSequence providerId;
    public TransportType transportType;
    public CharSequence route;
    public CharSequence daysMask;
    public Direction direction;
    public CharSequence name;
}
