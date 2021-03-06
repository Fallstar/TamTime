package flying.grub.tamtime.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import flying.grub.tamtime.R;
import flying.grub.tamtime.data.StopTimes;

/**
 * Created by fly on 11/28/15.
 */
public class AllRouteStopLine extends RecyclerView.Adapter<AllRouteStopLine.ViewHolder> {

    private static final String TAG = OneRouteAdapter.class.getSimpleName();
    public OnItemClickListener mItemClickListener;
    private ArrayList<StopTimes> stops;
    private boolean isTheoritical;

    private Context context;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public AllRouteStopLine(ArrayList<StopTimes> stops, Context context) {
        this.context = context;
        this.stops = stops;
        this.isTheoritical = false;
    }

    public AllRouteStopLine(ArrayList<StopTimes> stops, Context context, boolean isTheoritical) {
        this(stops, context);
        this.isTheoritical = isTheoritical;
    }

    @Override
    public AllRouteStopLine.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_one_line, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        StopTimes stop = stops.get(position);
        holder.stop.setText(stop.getRoute().getDirection());

        ArrayList<String> times = stop.getStrNextTimes(3); // Request 3 next times
        holder.tps1.setText(times.get(0));
        holder.tps2.setText(times.get(1));
        holder.tps3.setText(times.get(2));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return stops.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView stop;
        public TextView tps1;
        public TextView tps2;
        public TextView tps3;
        public RelativeLayout relativeLayout;


        public ViewHolder(View v) {
            super(v);
            stop = (TextView) itemView.findViewById(R.id.title);
            tps1 = (TextView) itemView.findViewById(R.id.tps1);
            tps2 = (TextView) itemView.findViewById(R.id.tps2);
            tps3 = (TextView) itemView.findViewById(R.id.tps3);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.element);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

    }

}
