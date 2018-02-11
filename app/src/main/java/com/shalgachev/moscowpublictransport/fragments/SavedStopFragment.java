package com.shalgachev.moscowpublictransport.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.activities.AddTransportActivity;
import com.shalgachev.moscowpublictransport.adapters.SavedStopRecyclerViewAdapter;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;
import com.shalgachev.moscowpublictransport.helpers.ExtraHelper;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class SavedStopFragment extends Fragment {

    private static final String ARG_TRANSPORT_TYPE = "transport_type";

    private TransportType mTransportType;
    private SavedStopRecyclerViewAdapter.ViewHolder.ItemIterationListener mListener;
    private RecyclerView mRecycleView;
    private SavedStopRecyclerViewAdapter mRecyclerAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SavedStopFragment() {
    }

    public static SavedStopFragment newInstance(TransportType transportType) {
        SavedStopFragment fragment = new SavedStopFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSPORT_TYPE, transportType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTransportType = (TransportType) getArguments().getSerializable(ARG_TRANSPORT_TYPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateStops();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved_stop_list, container, false);
        Context context = rootView.getContext();

        mRecycleView = rootView.findViewById(R.id.list);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setLayoutManager(new LinearLayoutManager(context));

        mRecyclerAdapter = new SavedStopRecyclerViewAdapter(mListener, getActivity());
        mRecycleView.setAdapter(mRecyclerAdapter);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddTransportActivity.class);
                intent.putExtra(ExtraHelper.TRANSPORT_TYPE_EXTRA, mTransportType);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (SavedStopRecyclerViewAdapter.ViewHolder.ItemIterationListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ItemIterationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateStops() {
        if (mRecycleView == null)
            return;

        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(getActivity());

        List<Stop> stops = db.getStopsOnMainMenu(mTransportType);
        db.close();

        mRecyclerAdapter.updateStops(stops);
    }

    public SavedStopRecyclerViewAdapter getRecyclerAdapter() {
        return mRecyclerAdapter;
    }
}
