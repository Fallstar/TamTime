package flying.grub.tamtime.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import flying.grub.tamtime.R;
import flying.grub.tamtime.data.Stop;

/**
 * Created by fly on 11/26/15.
 */
public class SeachResultAdapter extends RecyclerView.Adapter<SeachResultAdapter.ViewHolder> {

    public OnItemClickListener mItemClickListener;
    private ArrayList<Stop> stops;

    public SeachResultAdapter(ArrayList<Stop> stops) {
        this.stops = stops;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(stops.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textView;
        public LinearLayout linearLayout;


        public ViewHolder(View v) {
            super(v);
            textView = (TextView) itemView.findViewById(R.id.title);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.element);
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
