package flying.grub.tamtime.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.adapter.AllStopAdapter;
import flying.grub.tamtime.adapter.DividerItemDecoration;
import flying.grub.tamtime.adapter.SeachResultAdapter;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Stop;

/**
 * Created by fly on 11/26/15.
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private SeachResultAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Stop> currentDisplayedStop;
    private EditText search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        setHasOptionsMenu(true);

        getActivity().setTitle(getString(R.string.home));

        recyclerView = (RecyclerView) view.findViewById(R.id.result);

        recyclerView.setHasFixedSize(false);

        layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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
                    recyclerView.swapAdapter(null, true);
                } else {
                    new SearchAsync().execute(s.toString());
                }
            }
        });

        return view;
    }

    public void selectitem(int i){
        Stop s = currentDisplayedStop.get(i);
        Intent intent = new Intent(getActivity(), OneStopActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("stopId", s.getOurId());
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    }

    private class SearchAsync extends AsyncTask<String, String, ArrayList<Stop>> {
        protected ArrayList<Stop> doInBackground(String... strings) {
            String s = strings[0];
            return DataParser.getDataParser().getMap().searchInStops(s,3);
        }

        protected void onPostExecute(ArrayList<Stop> stops) {
            currentDisplayedStop = stops;
            adapter = new SeachResultAdapter(currentDisplayedStop);
            adapter.SetOnItemClickListener(new SeachResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    selectitem(position);
                }
            });
            recyclerView.swapAdapter(adapter, true);
        }
    }
}
