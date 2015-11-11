package flying.grub.tamtime.activity;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.UpdateRunnable;
import flying.grub.tamtime.fragment.LineRouteFragment;
import flying.grub.tamtime.R;
import flying.grub.tamtime.slidingTab.SlidingTabLayout;


public class OneLineActivity extends AppCompatActivity {

    private static final String TAG = OneLineActivity.class.getSimpleName();
    private Toolbar toolbar;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;

    private int linePosition;
    private Line line;

    private UpdateRunnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slidingtabs);

        Bundle bundle = getIntent().getExtras();
        linePosition = bundle.getInt("id");

        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        line = DataParser.getDataParser().getMap().getLine(linePosition);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        viewPager.setAdapter(new OneLinePageAdapter(getSupportFragmentManager()));

        slidingTabLayout = new SlidingTabLayout(getApplicationContext());
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.textClearColor));
        slidingTabLayout.setDividerColors(getResources().getColor(R.color.primaryColor));
        slidingTabLayout.setViewPager(viewPager);

        Log.d(TAG, line.getDisruptEventList() + "");
        if (line.getDisruptEventList().size() > 0) {
            showInfo(line.getDisruptEventList().get(0).toString());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
        updateRunnable = new UpdateRunnable();
        updateRunnable.run();
    }

    @Override
    public void onPause(){
        super.onPause();
        updateRunnable.stop();
        EventBus.getDefault().unregister(this);
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go_theoritical:
                Intent intent = new Intent(getApplicationContext(), TheoriticalActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("linePosition", linePosition);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showInfo(String text) {
        Snackbar.with(getApplicationContext())
                .text(text)
                .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
                .actionLabel("Ok")
                .actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked(Snackbar snackbar) {
                        Log.d(TAG, "TEST");
                    }
                })
                .actionColor(getResources().getColor(R.color.accentColor))
                .show(this);
    }

    public class OneLinePageAdapter extends FragmentStatePagerAdapter {
        public OneLinePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int routePosition) {
            return line.getRoutes().get(routePosition).getDirection();
        }

        @Override
        public int getCount() {
            return line.getRouteCount();
        }

        @Override
        public Fragment getItem(int position) {
            return LineRouteFragment.newInstance(linePosition, position);
        }
    }

    public void onEvent(MessageEvent event) {
        if (event.type == MessageEvent.Type.EVENT_UPDATE) {
            if (line.getDisruptEventList().size() > 0) {
                showInfo(line.getDisruptEventList().get(0).toString());
            }
        }
    }
}