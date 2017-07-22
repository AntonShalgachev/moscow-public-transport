package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.fragments.RouteListFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by anton on 5/26/2017.
 */

public class RouteListPagerAdapter extends FragmentPagerAdapter {
    private final List<TransportType> YTANSPORT_TYPES = Arrays.asList(TransportType.BUS, TransportType.TROLLEY, TransportType.TRAM);

    private Context mContext;

    public RouteListPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return RouteListFragment.newInstance(YTANSPORT_TYPES.get(position));
    }

    @Override
    public int getCount() {
        return YTANSPORT_TYPES.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.type_bus_name);
            case 1:
                return mContext.getString(R.string.type_trolley_name);
            case 2:
                return mContext.getString(R.string.type_tram_name);
        }
        return null;
    }
}
