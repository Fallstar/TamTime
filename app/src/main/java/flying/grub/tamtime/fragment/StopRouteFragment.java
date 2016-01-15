package flying.grub.tamtime.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.MainActivity;
import flying.grub.tamtime.activity.TheoriticalActivity;
import flying.grub.tamtime.adapter.AllLinesAdapter;
import flying.grub.tamtime.adapter.DividerItemDecoration;
import flying.grub.tamtime.adapter.OneRouteAdapter;
import flying.grub.tamtime.adapter.OneStopAdapter;
import flying.grub.tamtime.adapter.ReportAdapter;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.Report;
import flying.grub.tamtime.data.ReportType;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.StopTimes;
import flying.grub.tamtime.data.UpdateRunnable;

/**
 * Created by fly on 9/19/15.
 */
public class StopRouteFragment extends Fragment {

    private static final String TAG = StopRouteFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OneStopAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBarCircularIndeterminate circularIndeterminate;

    private Stop stop;
    private Line line;

    public static Fragment newInstance(Integer stopId, int linePosition) {
        StopRouteFragment f = new StopRouteFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("stopId", stopId);
        args.putInt("linePosition", linePosition);
        f.setArguments(args);
        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stop = DataParser.getDataParser().getMap().getStopByOurId(getArguments().getInt("stopId"));
        line = stop.getLines().get(getArguments().getInt("linePosition"));
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_swype_refresh,
                container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        circularIndeterminate = (ProgressBarCircularIndeterminate) view.findViewById(R.id.progressBarCircularIndeterminate);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        recyclerView.setHasFixedSize(true);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.windowBackgroundCard));

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new OneStopAdapter(stop.getStopTimeForLine(line.getLineId()));

        recyclerView.setAdapter(adapter);
        adapter.SetOnItemClickListener(new OneStopAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // Unneeded ?
            }
        });

        circularIndeterminate.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataParser.getDataParser().update();
            }
        });
        refreshLayout.setColorSchemeResources(R.color.primaryColor);

        return view;
    }

    public void onEvent(MessageEvent event){
        if (event.type == MessageEvent.Type.TIMES_UPDATE) {

            refreshLayout.setRefreshing(false);
            stop = DataParser.getDataParser().getMap().getStopByOurId(getArguments().getInt("stopId"));
            line = stop.getLines().get(getArguments().getInt("linePosition"));

            adapter = new OneStopAdapter(stop.getStopTimeForLine(line.getLineId()));
            recyclerView.swapAdapter(adapter, true);
        }
    }
}
