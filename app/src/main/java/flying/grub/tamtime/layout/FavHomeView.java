package flying.grub.tamtime.layout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import flying.grub.tamtime.R;
import flying.grub.tamtime.activity.OneStopActivity;
import flying.grub.tamtime.adapter.FavWelcomeAdapter;
import flying.grub.tamtime.data.FavoriteStops;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.UpdateRunnable;

/**
 * Created by fly on 11/29/15.
 */
public class FavHomeView implements HomeLayout {

    private LinearLayout favLayout;
    private RecyclerView favStopRecyclerView;
    private FavWelcomeAdapter favStopAdapter;
    private FavoriteStops favoriteStops;

    public FragmentActivity context;

    public interface UpdateStopLine {
        void update();
    }

    public  UpdateStopLine updateStopLine;

    public void setUpdateStopLine(UpdateStopLine updateStopLine) {
        this.updateStopLine = updateStopLine;
    }

    public FavHomeView(FragmentActivity context) {
        this.context = context;
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @Override
    public boolean isFooter() {
        return false;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.item_fav_stop_welcome, container, false);
        favLayout = (LinearLayout) view.findViewById(R.id.fav_stop_recycler);
        LinearLayout linearLayout = (LinearLayout) favLayout.findViewById(R.id.progress);
        linearLayout.setVisibility(View.GONE);
        TextView textView = (TextView) favLayout.findViewById(R.id.empty_view);
        textView.setText(context.getString(R.string.no_favorite_stop));

        favStopRecyclerView = (RecyclerView) favLayout.findViewById(R.id.recycler_view);

        favStopRecyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager layoutManagerFav = new org.solovyev.android.views.llm.LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        favStopRecyclerView.setLayoutManager(layoutManagerFav);
        favStopRecyclerView.setItemAnimator(new DefaultItemAnimator());
        favoriteStops = new FavoriteStops(context);

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
                    PopupMenu popup = new PopupMenu(context, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.add_to_home, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            ArrayList<CharSequence> lines = new ArrayList<>();
                            for (Line l : favoriteStops.getFavoriteStop().get(position).getLines()) {
                                lines.add("Ligne " + l.getLineNum());
                            }
                            CharSequence[] lineString = lines.toArray(new CharSequence[lines.size()]);

                            MaterialDialog dialog = new MaterialDialog.Builder(context)
                                    .title(R.string.line_choice)
                                    .items(lineString)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                            Stop s = favoriteStops.getFavoriteStop().get(position);
                                            Line l = s.getLines().get(which);
                                            favoriteStops.addLineStop(l, s);
                                            updateStopLine.update();
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


}
