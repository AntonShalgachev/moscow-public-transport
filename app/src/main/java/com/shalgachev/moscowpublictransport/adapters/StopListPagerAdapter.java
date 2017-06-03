package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.fragments.StopListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/3/2017.
 */

public class StopListPagerAdapter extends FragmentPagerAdapter {
    private class FragmentHolder {
        FragmentHolder(CharSequence title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
        private CharSequence title;
        private Fragment fragment;
    }

    private Context mContext;
    private List<FragmentHolder> mFragmentHolders;

    public StopListPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;

        reset();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentHolders.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mFragmentHolders.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentHolders.get(position).title;
    }

    public void addTab(CharSequence daysMask, List<CharSequence> stopsList) {
        ArrayList<StopListItem> items = new ArrayList<>();
        for (CharSequence name : stopsList)
            items.add(new StopListItem(name, "Foreva", false));

        CharSequence title = ScheduleUtils.daysMaskToString(mContext, daysMask, true);
        Fragment fragment = StopListFragment.newInstance(items);

        mFragmentHolders.add(new FragmentHolder(title, fragment));
        notifyDataSetChanged();
    }

    public void reset() {
        mFragmentHolders = new ArrayList<>();
        notifyDataSetChanged();
    }
}
