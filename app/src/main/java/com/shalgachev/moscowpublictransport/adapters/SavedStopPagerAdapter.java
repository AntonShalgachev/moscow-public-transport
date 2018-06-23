package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.fragments.SavedStopFragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anton on 5/26/2017.
 */

public class SavedStopPagerAdapter extends FragmentPagerAdapter {
    private final List<TransportType> TRANSPORT_TYPES = Arrays.asList(TransportType.BUS, TransportType.TROLLEY, TransportType.TRAM);

    private boolean mFromWidget;
    private HashMap<Integer, SavedStopFragment> mFragments;
    private Context mContext;

    public SavedStopPagerAdapter(boolean fromWidget, FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
        mFromWidget = fromWidget;
        mFragments = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        return SavedStopFragment.newInstance(TRANSPORT_TYPES.get(position), mFromWidget);
    }

    public SavedStopFragment getFragment(int position) {
        if (mFragments.containsKey(position))
            return mFragments.get(position);
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

        mFragments.put(position, (SavedStopFragment)createdFragment);

        return createdFragment;
    }

    @Override
    public int getCount() {
        return TRANSPORT_TYPES.size();
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
