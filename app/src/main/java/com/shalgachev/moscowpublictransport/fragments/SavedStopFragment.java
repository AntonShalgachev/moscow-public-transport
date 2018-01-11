package com.shalgachev.moscowpublictransport.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SavedStopFragment extends Fragment {

    private static final String ARG_TRANSPORT_TYPE = "transport_type";

    private TransportType mTransportType;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mRecycleView;

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

        mRecycleView = (RecyclerView) rootView.findViewById(R.id.list);
        mRecycleView.setLayoutManager(new LinearLayoutManager(context));

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void updateStops() {
        if (mRecycleView == null)
            return;

        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(getActivity());

        // TODO: 7/22/2017 Query stops with actual transport type
        List<Stop> stops = db.getStops(mTransportType);
        db.close();

        mRecycleView.setAdapter(new SavedStopRecyclerViewAdapter(stops, mListener, getActivity()));
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Stop item);
    }
}
