package flying.grub.tamtime.layout;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

import java.util.ArrayList;
import java.util.zip.Inflater;

import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.adapter.SeachResultAdapter;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Stop;

/**
 * Created by fly on 11/29/15.
 */
public class SearchView implements HomeLayout {

    public FragmentActivity context;

    public SearchView(FragmentActivity context) {
        this.context = context;
    }

    private RecyclerView researchRecyclerView;
    private SeachResultAdapter researchAdapter;

    private ArrayList<Stop> searchStop;
    private EditText search;

    @Override
    public boolean isHeader() {
        return true;
    }

    @Override
    public boolean isFooter() {
        return false;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {

        View view = inflater.inflate(R.layout.item_research_welcome, container, false);
        RecyclerView.LayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

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
        return view;
    }

    public void selectitem(Stop s){
        Intent intent = new Intent(context, OneStopActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("stopId", s.getOurId());
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
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
