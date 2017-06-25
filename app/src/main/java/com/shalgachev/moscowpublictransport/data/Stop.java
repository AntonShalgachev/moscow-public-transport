package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 6/24/2017.
 */

public class Stop implements Serializable {
    Stop(CharSequence providerId, CharSequence route, CharSequence daysMask, Direction direction, CharSequence name) {
        this.providerId = providerId;
        this.route = route;
        this.daysMask = daysMask;
        this.direction = direction;
        this.name = name;
    }

    public CharSequence providerId;
    public CharSequence route;
    public CharSequence daysMask;
    public Direction direction;
    public CharSequence name;
}
