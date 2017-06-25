package com.shalgachev.moscowpublictransport.data;

/**
 * Created by anton on 6/13/2017.
 */
public class ScheduleArgs {
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

    BaseScheduleProvider.OperationType operationType;
    TransportType transportType;
    CharSequence route;
    CharSequence daysMask;
    Direction direction;
    CharSequence stop;
}
