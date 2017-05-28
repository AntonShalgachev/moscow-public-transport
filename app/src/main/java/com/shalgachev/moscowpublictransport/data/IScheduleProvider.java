package com.shalgachev.moscowpublictransport.data;

import java.util.Set;

/**
 * Created by anton on 5/28/2017.
 */

public interface IScheduleProvider {
    Set<TransportType> getTransportTypes();
    Set<CharSequence> getRoutes(TransportType transportType);
    Set<CharSequence> getDays(TransportType transportType, CharSequence route);
    Set<Direction> getDirections(TransportType transportType, CharSequence route, CharSequence days);
    Set<CharSequence> getStops(TransportType transportType, CharSequence route, CharSequence days, Direction direction);

    Schedule getSchedule(TransportType transportType, CharSequence route, CharSequence days, Direction direction, CharSequence stop);

    CharSequence getProviderName();
}
