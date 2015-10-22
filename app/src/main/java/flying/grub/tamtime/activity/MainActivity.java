package flying.grub.tamtime.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.FavoriteStops;
import flying.grub.tamtime.fragment.AllLinesFragment;
import flying.grub.tamtime.fragment.AllStopFragment;
import flying.grub.tamtime.fragment.AllStopReport;
import flying.grub.tamtime.fragment.FavoriteStopsFragment;
import flying.grub.tamtime.fragment.NavigationDrawerFragment;
import flying.grub.tamtime.R;
import flying.grub.tamtime.fragment.NearStopFragment;
import flying.grub.tamtime.fragment.WebFragment;
import flying.grub.tamtime.navigation.DrawerCallback;
import flying.grub.tamtime.navigation.ItemWithDrawable;

public class MainActivity extends AppCompatActivity implements DrawerCallback {

    private Toolbar mToolbar;

    private NavigationDrawerFragment navigationDrawerFragment;
    private static final String MAP_URL = "http://tam.cartographie.pro/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DataParser dataParser = DataParser.getDataParser();
        dataParser.init(this);


        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        navigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen()) {
            navigationDrawerFragment.closeDrawer();
        }
    }

    @Override
    public void onDrawerClick(ItemWithDrawable element) {
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment;
        Intent intent;
        switch (element.getId()) {
            case 1:
                fragment = new AllLinesFragment();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack("");
                transaction.commit();
                break;
            case 2:
                fragment = new AllStopFragment();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack("");
                transaction.commit();
                break;
            case 3:
                fragment = new FavoriteStopsFragment();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack("");
                transaction.commit();
                break;
            case 4:
                fragment = new NearStopFragment();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack("");
                transaction.commit();
                break;
            case 5:
                WebFragment webFragment = WebFragment.newInstance(MAP_URL);
                transaction.replace(R.id.container, webFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 6:
                fragment = new AllStopReport();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack("");
                transaction.commit();
                break;
            case 7:
                NavigationDrawerFragment.currentSelectedPosition.setI(0);
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
                break;
            case 8:
                NavigationDrawerFragment.currentSelectedPosition.setI(0);
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }
}

