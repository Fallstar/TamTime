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
    private Map<Stop, List<Line>> favoriteStopLine;
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

        favoriteStopLine = new HashMap<>();
        for (String info : defaultSharedPreferences.getStringSet(LINE_TAG, new HashSet<String>())) {
            String[] infos = info.split("<>");
            Stop s = DataParser.getDataParser().getMap().getStopByOurId(Integer.parseInt(infos[0]));
            Line l = DataParser.getDataParser().getMap().getLineByNum(Integer.parseInt(infos[1]));
            addLineStop(l, s);
        }
        sort();
    }

    public ArrayList<Stop> getFavoriteStop() {
        getFromPref();
        return new ArrayList<>(favoriteStop);
    }

    public HashMap<Stop, List<Line>> getFavStopLines() {
        getFromPref();
        return new HashMap<>(favoriteStopLine);
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

    private void addLineStop(Line l, Stop s) {
        if (favoriteStopLine.containsKey(s)) {
            favoriteStopLine.get(s).add(l);
        } else {
            List<Line> lines = new ArrayList<>();
            lines.add(l);
            favoriteStopLine.put(s, lines);
        }
    }

    private void removeLineStop(Line l, Stop s) {
        if (favoriteStopLine.containsKey(s) && favoriteStopLine.get(s).size() > 1) {
            favoriteStopLine.get(s).remove(l);
        } else {
            favoriteStopLine.remove(s);
        }
    }

    public void addLineForStop(Line l, Stop s) {
        addLineStop(l, s);
        sortAndPush();
    }

    public void removeLineForStop(int position) {
        int count = 0;
        Stop s = null;
        Line l = null;
        for (Map.Entry<Stop, List<Line>> entry : favoriteStopLine.entrySet()) {
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
        removeLineForStop(l, s);
    }

    private void removeLineForStop(Line l, Stop s) {
        removeLineStop(l, s);
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
        for (Map.Entry<Stop, List<Line>> entry : favoriteStopLine.entrySet()) {
            Stop s = entry.getKey();
            for (Line l : entry.getValue()) {
                favLineStop.add(s.getOurId() + "<>" + l.getLineNum());
            }
        }

        editor.putStringSet(TAG, new HashSet<>(fav));
        editor.putStringSet(LINE_TAG, new HashSet<>(favLineStop));

        editor.commit();
    }
}
