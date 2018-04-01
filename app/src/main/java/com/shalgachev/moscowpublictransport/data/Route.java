package com.shalgachev.moscowpublictransport.data;

import java.io.Serializable;

/**
 * Created by anton on 3/6/2018.
 */

public class Route implements Serializable {
    public TransportType transportType;
    public String providerId;
    public String id;

    public String name;

    public Route(TransportType transportType, String id, String name, String providerId)
    {
        this.transportType = transportType;
        this.providerId = providerId;
        this.name = name;
        this.id = id;
    }
    public Route(Route route)
    {
        this.transportType = route.transportType;
        this.providerId = route.providerId;
        this.name = route.name;
        this.id = route.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Route)) return false;

        Route route = (Route) o;

        if (!transportType.equals(route.transportType)) return false;
        if (!providerId.equals(route.providerId)) return false;
        if (!id.equals(route.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + transportType.hashCode();
        result = 31 * result + providerId.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s, %s)", transportType.toString(), id, name, providerId);
    }
}
