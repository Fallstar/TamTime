package flying.grub.tamtime.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import flying.grub.tamtime.activity.MainActivity;

public class FavoriteStops {
    private static final String TAG = FavoriteStops.class.getSimpleName();

    private static final String LINE_TAG = "LINESTOP";


    private List<Stop> favoriteStop;
    private ArrayList<LineStop> favoriteStopLine;
    private Context context;

    public FavoriteStops(Context c) {
        context = c;
        getFromPref();
    }

    private void getFromPref() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        favoriteStop = new ArrayList<>();
        for (String stopId : defaultSharedPreferences.getStringSet(TAG, new HashSet<String>())) {
            favoriteStop.add(DataParser.getDataParser().getMap().getStopByOurId(Integer.parseInt(stopId)));
        }

        favoriteStopLine = new ArrayList<>();
        for (String info : defaultSharedPreferences.getStringSet(LINE_TAG, new HashSet<String>())) {
            String[] infos = info.split("<>");
            Stop s = DataParser.getDataParser().getMap().getStopByOurId(Integer.parseInt(infos[0]));
            Line l = DataParser.getDataParser().getMap().getLineByNum(Integer.parseInt(infos[1]));
            favoriteStopLine.add(new LineStop(s, l));
        }
        sort();
    }

    // STOP

    public ArrayList<Stop> getFavoriteStop() {
        getFromPref();
        return new ArrayList<>(favoriteStop);
    }

    public boolean isInFav(Stop stop) {
        int stopId = stop.getOurId();
        for (Stop s : favoriteStop) {
            if (s.getOurId() == stopId) {
                return true;
            }
        }
        return false;
    }

    public void add(Stop stop) {
        favoriteStop.add(stop);
        sortAndPush();
    }

    public void remove(int i) {
        favoriteStop.remove(i);
        sortAndPush();
    }

    public void remove(Stop stop) {
        favoriteStop.remove(stop);
        sortAndPush();
    }

    // STOP LINES

    public void addLineStop(Line l, Stop s) {
        favoriteStopLine.add(new LineStop(s, l));
        sortAndPush();
    }

    public void removeLineStop(int position) {
        favoriteStopLine.remove(position);
        sortAndPush();
    }

    public ArrayList<LineStop> getFavStopLines() {
        getFromPref();
        return new ArrayList<>(favoriteStopLine);
    }

    private void sort() {
        Collections.sort(favoriteStop, new Comparator<Stop>() {
            @Override
            public int compare(Stop s1, Stop s2) {
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
    }

    private void sortAndPush() {
        sort();
        pushToPref();
    }

    public void pushToPref() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        ArrayList<String> fav = new ArrayList<>();
        for (Stop s : favoriteStop) {
            fav.add("" + s.getOurId());
        }

        ArrayList<String> favLineStop = new ArrayList<>();
        for (LineStop lineStop : favoriteStopLine) {
            favLineStop.add(lineStop.getStop().getOurId() + "<>" + lineStop.getLine().getLineNum());
        }

        editor.putStringSet(TAG, new HashSet<>(fav));
        editor.putStringSet(LINE_TAG, new HashSet<>(favLineStop));

        editor.commit();
    }
}
