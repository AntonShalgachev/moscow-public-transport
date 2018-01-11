package com.shalgachev.moscowpublictransport.data;

import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;

/**
 * Created by anton on 6/13/2017.
 */
public class ScheduleArgs {
    public BaseScheduleProvider.OperationType operationType;
    public TransportType transportType;
    public CharSequence route;
    public CharSequence daysMask;
    public Direction direction;

    public Stop stop;

    public static ScheduleArgs asRoutesArgs(TransportType type) {
        ScheduleArgs args = new ScheduleArgs();
        args.operationType = BaseScheduleProvider.OperationType.ROUTES;
        args.transportType = type;

        return args;
    }

    public static ScheduleArgs asStopsArgs(TransportType type, CharSequence route) {
        ScheduleArgs args = new ScheduleArgs();
        args.operationType = BaseScheduleProvider.OperationType.STOPS;
        args.transportType = type;
        args.route = route;

        return args;
    }

    public static ScheduleArgs asScheduleArgs(Stop stop) {
        ScheduleArgs args = new ScheduleArgs();
        args.operationType = BaseScheduleProvider.OperationType.SCHEDULE;

        args.stop = stop;

        return args;
    }
}
