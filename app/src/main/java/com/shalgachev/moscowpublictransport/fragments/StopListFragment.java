package com.shalgachev.moscowpublictransport.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.adapters.StopListRecyclerViewAdapter;
import com.shalgachev.moscowpublictransport.data.StopListItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class StopListFragment extends Fragment {

    private static final String ARG_STOP_LIST_ITEMS = "column-count";
    ArrayList<StopListItem> mItems;
    private OnListFragmentInteractionListener mListener;

    public StopListFragment() {
    }

    public static StopListFragment newInstance(ArrayList<StopListItem> items) {
        StopListFragment fragment = new StopListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_STOP_LIST_ITEMS, items);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Serializable items = getArguments().getSerializable(ARG_STOP_LIST_ITEMS);
            if (items == null || !(items instanceof ArrayList))
                throw new IllegalArgumentException();

            //noinspection unchecked
            mItems = (ArrayList<StopListItem>) items;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stop_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new StopListRecyclerViewAdapter(mItems, mListener));
        }
        return view;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(StopListItem item);
    }
}
