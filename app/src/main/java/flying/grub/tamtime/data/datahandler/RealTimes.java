package flying.grub.tamtime.data.datahandler;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.Route;
import flying.grub.tamtime.data.StopTimes;
import flying.grub.tamtime.data.VolleyApp;

/**
 * Created by fly on 11/9/15.
 */
public class RealTimes implements DataHandler {

    private static final String TAG = ReportEvent.class.getSimpleName();
    private static final String JSON_REALTIME = "http://tam.mobitrans.fr/webservice/data.php?pattern=getDetails";
    private ArrayList<StopTimes> stpTimesList;


    private Context context;

    public RealTimes(Context context) {
        this.stpTimesList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void update() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                JSON_REALTIME, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        setRealTimes(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        });
        VolleyApp.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    public void setRealTimes(JSONObject timesJson) {
        StopTimes stpTimes;

        this.resetAllRealTimes(); // Reset all the real times

        try {
            JSONArray aller = timesJson.getJSONArray("aller");
            JSONArray retour = timesJson.getJSONArray("retour");
            JSONArray curntStop;
            for (int i=0; i < aller.length(); i++) {
                curntStop = aller.getJSONArray(i);
                stpTimes = this.getTheStopTimes(curntStop.getString(0), curntStop.getString(6), curntStop.getInt(4)); // Get the StopTimes
                if (stpTimes != null) {
                    stpTimes.addRealTime(curntStop.getInt(5));
                }
            }

            for (int i=0; i < retour.length(); i++) {
                curntStop = retour.getJSONArray(i);
                stpTimes = this.getTheStopTimes(curntStop.getString(0), curntStop.getString(6), curntStop.getInt(4));
                if (stpTimes != null) {
                    stpTimes.addRealTime(curntStop.getInt(5));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new MessageEvent(MessageEvent.Type.TIMES_UPDATE));
    }

    public StopTimes getStopTimesByOurId(String ourId) {
        for (StopTimes st : this.stpTimesList) {
            if (st.getOurId().equals(ourId)) return st;
        }
        return null;
    }

    // Return the StopTimes which correspond to line/direction/stopId
    public StopTimes getTheStopTimes(String line, String direction, int stopId) {
        int linum = line.contains("L") ? Integer.parseInt(line.replace("L", "")) : Integer.parseInt(line);

        Line curLine = DataParser.getDataParser().getMap().getLineByNum(linum);
        Route route = null;

        if (curLine != null) {
            route = curLine.getRoutebyName(direction);
        }

        if (route != null) {
            return route.getStopTimesById(stopId);
        }

        return null;
    }

    public void addStpTimes(StopTimes srl) {
        this.stpTimesList.add(srl);
    }

    // Reset the real times for all StopTimes
    public void resetAllRealTimes() {
        for (StopTimes stp : stpTimesList) {
            stp.resetRealTimes();
        }
    }
}
