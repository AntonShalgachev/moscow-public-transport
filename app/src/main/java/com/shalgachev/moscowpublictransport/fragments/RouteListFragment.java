package com.shalgachev.moscowpublictransport.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.activities.AddTransportActivity;
import com.shalgachev.moscowpublictransport.data.Stop;
import com.shalgachev.moscowpublictransport.data.TransportType;
import com.shalgachev.moscowpublictransport.data.db.SavedStopsSQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 5/26/2017.
 */

public class RouteListFragment extends Fragment {

    private static final String ARG_TRANSPORT_TYPE = "transport_type";
    private ListView mListView;
    private TransportType mTransportType;

    public RouteListFragment() {

    }

    public static RouteListFragment newInstance(TransportType type) {
        RouteListFragment fragment = new RouteListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSPORT_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateStops();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route_list, container, false);

        mListView = (ListView) rootView.findViewById(R.id.transport_list);

        mTransportType = (TransportType) getArguments().getSerializable(ARG_TRANSPORT_TYPE);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddTransportActivity.class);
                intent.putExtra("transport_type", mTransportType);
                startActivity(intent);
            }
        });

        return rootView;
    }

    void updateStops() {
        SavedStopsSQLiteHelper db = new SavedStopsSQLiteHelper(getActivity());

        List<String> names = new ArrayList<>();

        // TODO: 7/22/2017 Query stops with actual transport type
        List<Stop> stops = db.getStops();
        for (Stop stop : stops) {
            if (!stop.transportType.equals(mTransportType))
                continue;

            names.add(stop.toString());
        }
        db.close();

        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                names.toArray(new String[]{})
        );

        mListView.setAdapter(listViewAdapter);
    }
}
