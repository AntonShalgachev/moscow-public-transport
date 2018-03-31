package com.shalgachev.moscowpublictransport.data.db;

import com.shalgachev.moscowpublictransport.data.Season;
import com.shalgachev.moscowpublictransport.data.TransportType;

/**
 * Created by anton on 3/31/2018.
 */

public class SavedStop {
    String providerId;
    TransportType transportType;
    String route;
    Season season;
    String daysMask;
    String directionId;
    int stopId;
}
