package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 3/6/2018.
 */

public class Route implements Serializable {
    public String providerId;
    public String name;

    public Route(String name)
    {
        this.name = name;
    }
    public Route(String name, String providerId)
    {
        this.providerId = providerId;
        this.name = name;
    }
    public Route(Route route)
    {
        this.providerId = route.providerId;
        this.name = route.name;
    }

    public void setProviderId(String providerId)
    {
        this.providerId = providerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        if (!providerId.equals(route.providerId)) return false;
        if (!name.equals(route.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = providerId.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, providerId);
    }
}
