package com.shalgachev.moscowpublictransport.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.activities.AddTransportActivity;
import com.shalgachev.moscowpublictransport.data.TransportType;

/**
 * Created by anton on 5/26/2017.
 */

public class TransportListFragment extends Fragment {

    private static final String ARG_TRANSPORT_TYPE = "transport_type";

    public TransportListFragment() {

    }

    public static TransportListFragment newInstance(TransportType type) {
        TransportListFragment fragment = new TransportListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSPORT_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transport_list, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransportType type = (TransportType) getArguments().getSerializable(ARG_TRANSPORT_TYPE);
                Intent intent = new Intent(getActivity(), AddTransportActivity.class);
                intent.putExtra("transport_type", type);
                startActivity(intent);
            }
        });

        String[] names = {"268", "173", "800", "105", "268", "173", "800", "105", "268", "173", "800", "105", "268", "173", "800", "105"};
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                names
        );

        ListView listView = (ListView) rootView.findViewById(R.id.transport_list);
        listView.setAdapter(listViewAdapter);

        return rootView;
    }
}
