package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 6/3/2017.
 */

public class StopListItem implements Serializable {
    public CharSequence providerId;
    public CharSequence route;
    public CharSequence daysMask;
    public Direction direction;
    public CharSequence stop;
    public CharSequence next;
    public boolean selected;

    public StopListItem(CharSequence providerId, CharSequence route, CharSequence daysMask, Direction direction, CharSequence stop, CharSequence next, boolean selected) {
        this.providerId = providerId;
        this.route = route;
        this.daysMask = daysMask;
        this.direction = direction;
        this.stop = stop;
        this.next = next;
        this.selected = selected;
    }
}
