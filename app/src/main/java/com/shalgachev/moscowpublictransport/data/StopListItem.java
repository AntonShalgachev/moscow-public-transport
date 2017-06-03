package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 6/3/2017.
 */

public class StopListItem implements Serializable {
    public final CharSequence stop;
    public final CharSequence next;
    public boolean selected;

    public StopListItem(CharSequence stop, CharSequence next, boolean selected) {
        this.stop = stop;
        this.next = next;
        this.selected = selected;
    }
}
