package com.shalgachev.moscowpublictransport.adapters;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.Route;
import com.shalgachev.moscowpublictransport.data.SelectableRoute;
import com.shalgachev.moscowpublictransport.data.providers.BaseScheduleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 3/6/2018.
 */

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {
    private List<SelectableRoute> mRoutes = new ArrayList<>();
    private List<SelectableRoute> mFilteredRoutes = new ArrayList<>();
    private Context mContext;
    private Listener mListener;

    public RouteListAdapter(Context context) {
        this.mContext = context;
    }

    public void setAvailableRoutes(final List<SelectableRoute> routes) {
        mFilteredRoutes = new ArrayList<>(routes);
        mRoutes = new ArrayList<>(routes);
        notifyDataSetChanged();

        if (mListener != null)
            mListener.onFilterApplied();
    }

    public void setListener(Listener listener)
    {
        mListener = listener;
    }

    private void updateRoutes(final List<SelectableRoute> routes) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mFilteredRoutes.size();
            }

            @Override
            public int getNewListSize() {
                return routes.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mFilteredRoutes.get(oldItemPosition).equals(routes.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return areItemsTheSame(oldItemPosition, newItemPosition);
            }
        });

        diffResult.dispatchUpdatesTo(this);
    }

    public Route getSelectedRoute() {
        for (SelectableRoute route : mFilteredRoutes)
            if (route.selected)
                return route;

        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_route, parent, false);
        return new ViewHolder(view, new ViewHolder.ItemIterationListener() {
            @Override
            public void onRouteSelected(SelectableRoute selectedRoute, int position) {
                for (SelectableRoute route : mRoutes)
                    route.selected = false;
                selectedRoute.selected = true;

                notifyItemRangeChanged(0, mFilteredRoutes.size(), new Payload());

                if (mListener != null)
                    mListener.onFilterApplied();
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        final SelectableRoute route = mFilteredRoutes.get(position);

        if (!payloads.isEmpty()) {
            holder.mRadioButton.setChecked(route.selected);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SelectableRoute route = mFilteredRoutes.get(position);

        holder.route = route;
        holder.mNameView.setText(route.name);
        holder.mProviderView.setText(BaseScheduleProvider.getScheduleProvider(route.providerId).getProviderName(mContext));
        holder.mRadioButton.setChecked(route.selected);
    }

    @Override
    public int getItemCount() {
        return mFilteredRoutes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView mNameView;
        public final TextView mProviderView;
        public final RadioButton mRadioButton;
        public SelectableRoute route;

        ItemIterationListener mListener;

        public ViewHolder(View view, ItemIterationListener listener) {
            super(view);
            this.view = view;
            mNameView = view.findViewById(R.id.route_name);
            mProviderView = view.findViewById(R.id.provider_name);
            mRadioButton = view.findViewById(R.id.route_radio_button);

            mListener = listener;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClick();
                }
            });
        }

        void handleClick() {
            if (mListener != null)
                mListener.onRouteSelected(route, getAdapterPosition());
        }

        public interface ItemIterationListener {
            void onRouteSelected(SelectableRoute route, int position);
        }
    }

    public interface Listener
    {
        void onFilterApplied();
    }

    static private class Payload
    {

    }
}
