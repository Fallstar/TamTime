package flying.grub.tamtime.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.adapter.FavWelcomeAdapter;
import flying.grub.tamtime.adapter.SeachResultAdapter;
import flying.grub.tamtime.adapter.StopLineHomeAdapter;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.FavoriteStops;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.UpdateRunnable;
import flying.grub.tamtime.layout.FavHomeView;
import flying.grub.tamtime.layout.SearchView;

/**
 * Created by fly on 11/26/15.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    public FavHomeView favHomeView;
    public SearchView searchView;

    private RecyclerView favStopLinesRecycler;
    private StopLineHomeAdapter stopLineHomeAdapter;
    private FavoriteStops favoriteStops;

    private UpdateRunnable updateRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_recycler, container, false);
        setHasOptionsMenu(true);

        getActivity().setTitle(getString(R.string.home));

        favHomeView = new FavHomeView(getActivity());
        favHomeView.setUpdateStopLine(new FavHomeView.UpdateStopLine() {
            @Override
            public void update() {
                setupFavStopLine();
            }
        });

        searchView = new SearchView(getActivity());

        favStopLinesRecycler = (RecyclerView) view.findViewById(R.id.stop_lines);
        RecyclerView.LayoutManager layoutManagerSL = new org.solovyev.android.views.llm.LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        favStopLinesRecycler.setLayoutManager(layoutManagerSL);
        favStopLinesRecycler.setItemAnimator(new DefaultItemAnimator());
        favStopLinesRecycler.setHasFixedSize(false);
        favStopLinesRecycler.setBackgroundColor(getResources().getColor(R.color.windowBackgroundCard));
        setupFavStopLine();

        return view;
    }

    public void setupFavStopLine() {
        stopLineHomeAdapter = new StopLineHomeAdapter(favoriteStops.getFavStopLines(), getActivity());
        stopLineHomeAdapter.SetOnItemClickListener(new StopLineHomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                PopupMenu popup = new PopupMenu(getActivity(), view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.remove_from_home, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        favoriteStops.removeLineStop(position);
                        stopLineHomeAdapter = new StopLineHomeAdapter(favoriteStops.getFavStopLines(), getActivity());
                        favStopLinesRecycler.swapAdapter(stopLineHomeAdapter, true);
                        return true;
                    }
                });

                popup.show();
            }
        });
        favStopLinesRecycler.swapAdapter(stopLineHomeAdapter, true);
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
        updateRunnable = new UpdateRunnable();
        updateRunnable.run();
        updateRunnable.stop();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(MessageEvent event){
        if (event.type == MessageEvent.Type.TIMES_UPDATE) {
            setupFavStopLine();
        }
    }
}
