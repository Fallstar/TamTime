package flying.grub.tamtime.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.StopTimes;

/**
 * Created by fly on 11/28/15.
 */
public class StopLineHomeAdapter extends RecyclerView.Adapter<StopLineHomeAdapter.ViewHolder> {

    private static final String TAG = StopLineHomeAdapter.class.getSimpleName();

    public OnItemClickListener mItemClickListener;
    private Map<Stop, List<Line>> stopListMap;
    private FragmentActivity context;

    public StopLineHomeAdapter(Map<Stop, List<Line>> stopLines, FragmentActivity context) {
        this.stopListMap = stopLines;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_home_line_stop, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int count = 0;
        Stop s = null;
        Line l = null;
        for (Map.Entry<Stop, List<Line>> entry : stopListMap.entrySet()) {
            s = entry.getKey();
            for (Line line : entry.getValue()) {
                l = line;
                count ++;
                if (count == position) {
                    break;
                }
            }
            if (count == position) {
                break;
            }
        }

        holder.title.setText(s.getName() + " - Ligne " + l.getLineNum());
        holder.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        holder.recyclerView.setLayoutManager(layoutManager);
        holder.recyclerView.setItemAnimator(new DefaultItemAnimator());
        AllRouteStopLine adapter = new AllRouteStopLine(s.getStopTimeForLine(l.getLineId()), context);
        final Stop finalS = s;
        adapter.SetOnItemClickListener(new AllRouteStopLine.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(context, OneStopActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("stopId", finalS.getOurId());
                intent.putExtras(bundle);
                context.startActivity(intent);
                context.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
            }
        });
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (Map.Entry<Stop, List<Line>> entry : stopListMap.entrySet()) {
            for (Line l : entry.getValue()) {
                count ++;
            }
        }
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView title;
        public RecyclerView recyclerView;
        public ImageView menu;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) itemView.findViewById(R.id.title);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.times);
            menu = (ImageView) itemView.findViewById(R.id.menu);
            menu.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

    }
}
