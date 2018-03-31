package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 3/6/2018.
 */

public class Route implements Serializable {
    public TransportType transportType;
    public String providerId;
    public String name;

    public Route(TransportType transportType, String name, String providerId)
    {
        this.transportType = transportType;
        this.providerId = providerId;
        this.name = name;
    }
    public Route(Route route)
    {
        this.transportType = route.transportType;
        this.providerId = route.providerId;
        this.name = route.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Route)) return false;

        Route route = (Route) o;

        if (!transportType.equals(route.transportType)) return false;
        if (!providerId.equals(route.providerId)) return false;
        if (!name.equals(route.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + transportType.hashCode();
        result = 31 * result + providerId.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", transportType.toString(), name, providerId);
    }
}
