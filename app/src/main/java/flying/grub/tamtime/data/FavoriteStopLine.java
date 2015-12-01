package flying.grub.tamtime.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Created by fly on 11/30/15.
 */
public class FavoriteStopLine {

    private static final String TAG = FavoriteStopLine.class.getSimpleName();

    private ArrayList<LineStop> favoriteStopLine;
    private Context context;

    public FavoriteStopLine(Context c) {
        context = c;
        getFromPref();
    }

    private void getFromPref() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        favoriteStopLine = new ArrayList<>();
        for (String info : defaultSharedPreferences.getStringSet(TAG, new HashSet<String>())) {
            String[] infos = info.split("<>");
            Stop s = DataParser.getDataParser().getMap().getStopByOurId(Integer.parseInt(infos[0]));
            Line l = DataParser.getDataParser().getMap().getLineByNum(Integer.parseInt(infos[1]));
            favoriteStopLine.add(new LineStop(s, l));
        }
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

    private void sortAndPush() {
        pushToPref();
    }

    public void pushToPref() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        ArrayList<String> favLineStop = new ArrayList<>();
        for (LineStop lineStop : favoriteStopLine) {
            favLineStop.add(lineStop.getStop().getOurId() + "<>" + lineStop.getLine().getLineNum());
        }

        editor.putStringSet(TAG, new HashSet<>(favLineStop));
        editor.commit();
    }
}
