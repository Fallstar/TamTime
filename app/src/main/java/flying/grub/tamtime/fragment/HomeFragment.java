package flying.grub.tamtime.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.adapter.AllStopAdapter;
import flying.grub.tamtime.adapter.DividerItemDecoration;
import flying.grub.tamtime.adapter.FavWelcomeAdapter;
import flying.grub.tamtime.adapter.FavoriteAdapter;
import flying.grub.tamtime.adapter.OneRouteAdapter;
import flying.grub.tamtime.adapter.SeachResultAdapter;
import flying.grub.tamtime.adapter.StopLineHomeAdapter;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.FavoriteStops;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.UpdateRunnable;

/**
 * Created by fly on 11/26/15.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private LinearLayout favLayout;
    private RecyclerView researchRecyclerView;
    private SeachResultAdapter researchAdapter;

    private RecyclerView favStopRecyclerView;
    private FavWelcomeAdapter favStopAdapter;
    private FavoriteStops favoriteStops;

    private RecyclerView favStopLinesRecycler;
    private StopLineHomeAdapter stopLineHomeAdapter;

    private ArrayList<Stop> searchStop;
    private EditText search;

    private UpdateRunnable updateRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        setHasOptionsMenu(true);

        getActivity().setTitle(getString(R.string.home));

        // MAIN
        final RelativeLayout main = (RelativeLayout) view.findViewById(R.id.main_home);



        // SEARCH
        RecyclerView.LayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        researchRecyclerView = (RecyclerView) view.findViewById(R.id.result);
        researchRecyclerView.setLayoutManager(layoutManager);
        researchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        researchRecyclerView.setHasFixedSize(false);
        search = (EditText) view.findViewById(R.id.search_text);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new SearchAsync().execute(search.getText().toString());
                    return true;
                }
                return false;
            }
        });
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                search.setCursorVisible(b);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    researchRecyclerView.swapAdapter(null, true);
                } else {
                    new SearchAsync().execute(s.toString());
                }
            }
        });

        // FAVORITES STOPS
        favLayout = (LinearLayout) view.findViewById(R.id.fav_stop_recycler);
        LinearLayout linearLayout = (LinearLayout) favLayout.findViewById(R.id.progress);
        linearLayout.setVisibility(View.GONE);
        TextView textView = (TextView) favLayout.findViewById(R.id.empty_view);
        textView.setText(getString(R.string.no_favorite_stop));

        favStopRecyclerView = (RecyclerView) favLayout.findViewById(R.id.recycler_view);

        favStopRecyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager layoutManagerFav = new org.solovyev.android.views.llm.LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        favStopRecyclerView.setLayoutManager(layoutManagerFav);
        favStopRecyclerView.setItemAnimator(new DefaultItemAnimator());
        favoriteStops = new FavoriteStops(getActivity());

        if (favoriteStops.getFavoriteStop().size() == 0) {
            textView.setVisibility(View.VISIBLE);
            favStopRecyclerView.setVisibility(View.GONE);
        } else {
            favStopAdapter = new FavWelcomeAdapter(favoriteStops.getFavoriteStop());
            favStopRecyclerView.setAdapter(favStopAdapter);
            favStopAdapter.SetOnItemClickListener(new FavWelcomeAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View v, int position) {
                    selectitem(favoriteStops.getFavoriteStop().get(position));
                }
            });

            favStopAdapter.SetOnMenuClickListener(new FavWelcomeAdapter.OnMenuClickListener() {
                @Override
                public void onItemClick(View v, final int position) {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(getActivity(), v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.welcome_fav_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            ArrayList<CharSequence> lines = new ArrayList<>();
                            for (Line l : favoriteStops.getFavoriteStop().get(position).getLines()) {
                                lines.add("Ligne " + l.getLineNum());
                            }
                            CharSequence[] lineString = lines.toArray(new CharSequence[lines.size()]);

                            MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                                    .title(R.string.line_choice)
                                    .items(lineString)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                            Stop s = favoriteStops.getFavoriteStop().get(position);
                                            Line l = s.getLines().get(which);
                                            favoriteStops.addLineForStop(l, s);
                                            setupFavStopLine();
                                            dialog.dismiss();
                                        }
                                    }).build();
                            dialog.show();
                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        // FAVORITES STOPS LINES
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
                        favoriteStops.removeLineForStop(position);
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

    public void selectitem(Stop s){
        Intent intent = new Intent(getActivity(), OneStopActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("stopId", s.getOurId());
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
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

    private class SearchAsync extends AsyncTask<String, String, ArrayList<Stop>> {
        protected ArrayList<Stop> doInBackground(String... strings) {
            String s = strings[0];
            return DataParser.getDataParser().getMap().searchInStops(s,3);
        }

        protected void onPostExecute(ArrayList<Stop> stops) {
            searchStop = stops;
            researchAdapter = new SeachResultAdapter(searchStop);
            researchAdapter.SetOnItemClickListener(new SeachResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    selectitem(searchStop.get(position));
                }
            });
            researchRecyclerView.swapAdapter(researchAdapter, true);
        }
    }
}
