package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import com.shalgachev.moscowpublictransport.data.ScheduleUtils;
import com.shalgachev.moscowpublictransport.data.StopListItem;
import com.shalgachev.moscowpublictransport.fragments.StopListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 6/3/2017.
 */

public class StopListPagerAdapter extends FragmentPagerAdapter {
    FragmentManager mFragmentManager;
    private Context mContext;
    private List<FragmentHolder> mFragmentHolders;
    public StopListPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
        mFragmentManager = fragmentManager;

        reset();
    }

    @Override
    public int getCount() {
        return mFragmentHolders.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentHolders.get(position).fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentHolders.get(position).title;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void addTab(CharSequence daysMask, ArrayList<StopListItem> stopListItems) {
        CharSequence title = ScheduleUtils.daysMaskToString(mContext, daysMask);
        Fragment fragment = StopListFragment.newInstance(stopListItems);

        mFragmentHolders.add(new FragmentHolder(title, fragment));
        notifyDataSetChanged();
    }

    public void reset() {
        if (mFragmentHolders != null) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            for (FragmentHolder holder : mFragmentHolders) {
                transaction.remove(holder.fragment);
            }
            transaction.commit();
            mFragmentManager.executePendingTransactions();
        }

        mFragmentHolders = new ArrayList<>();
        notifyDataSetChanged();
    }

    private class FragmentHolder {
        private CharSequence title;
        private Fragment fragment;
        FragmentHolder(CharSequence title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }
}
