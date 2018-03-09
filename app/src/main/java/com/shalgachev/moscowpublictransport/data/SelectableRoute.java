package com.shalgachev.moscowpublictransport.data;

/**
 * Created by anton on 3/6/2018.
 */

public class SelectableRoute extends Route {
    public boolean selected = false;
    public SelectableRoute(Route route)
    {
        super(route);
    }
}
