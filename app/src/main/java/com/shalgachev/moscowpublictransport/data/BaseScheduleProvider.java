package com.shalgachev.moscowpublictransport.data;

/**
 * Created by anton on 5/28/2017.
 */

public abstract class BaseScheduleProvider implements IScheduleProvider {
    void validateParameters(TransportType transportType, CharSequence route, CharSequence days, Direction direction, CharSequence stop) {
        if (!getTransportTypes().contains(transportType))
            throw new IllegalArgumentException("Invalid transport type");

        if (!getRoutes(transportType).contains(route))
            throw new IllegalArgumentException("Invalid route");

        if (!getDays(transportType, route).contains(days))
            throw new IllegalArgumentException("Invalid days");

        if (!getDirections(transportType, route, days).contains(direction))
            throw new IllegalArgumentException("Invalid direction");

        if (!getStops(transportType, route, days, direction).contains(stop))
            throw new IllegalArgumentException("Invalid stop");
    }
}
