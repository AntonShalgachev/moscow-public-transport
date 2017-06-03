package com.shalgachev.moscowpublictransport.data;

import java.util.List;

/**
 * Created by anton on 5/28/2017.
 */

public interface IScheduleProvider {
    List<TransportType> getTransportTypes();
    List<CharSequence> getRoutes(TransportType transportType);
    List<CharSequence> getDaysMask(TransportType transportType, CharSequence route);
    List<Direction> getDirections(TransportType transportType, CharSequence route, CharSequence days);
    List<CharSequence> getStops(TransportType transportType, CharSequence route, CharSequence days, Direction direction);

    Schedule getSchedule(TransportType transportType, CharSequence route, CharSequence days, Direction direction, CharSequence stop);

    CharSequence getProviderName();
}
