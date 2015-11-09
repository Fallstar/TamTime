package flying.grub.tamtime.data.datahandler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.DownloadService;
import flying.grub.tamtime.data.StopTimes;
import flying.grub.tamtime.data.VolleyApp;

/**
 * Created by fly on 11/9/15.
 */
public class TheoricTimes implements DataHandler {

    private static final String TAG = TamMap.class.getSimpleName();
    private static final String JSON_THEOTIME = "http://www.bl00m.science/TamTimeData/timesTest.json";

    private Context context;

    public TheoricTimes(Context context) {
        this.context = context;
    }

    public void downloadAllTheo(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("url", JSON_THEOTIME);
        context.startService(intent);
    }

    public boolean asTheo(Context context) {
        File file = context.getFileStreamPath("theo.json");
        return file.isFile();
    }

    public boolean needTheoUpdate(Context context) {
        Log.d(TAG, asTheo(context) + "");
        return !asTheo(context); //TO DO (MD5)
    }

    @Override
    public void update() { // Theoric times
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                JSON_THEOTIME, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        setTheoTimes(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        });
        VolleyApp.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    public void setTheoTimes(JSONObject theoTimesJson) {
        StopTimes curntStpTim;
        JSONObject curntDay;
        String curntStopKey, curntDayKey;
        try {
            JSONObject stpTimJson = theoTimesJson.getJSONObject("stops");
            String date = theoTimesJson.getString("date");

            Iterator stopKey = stpTimJson.keys();

            while (stopKey.hasNext()) {
                curntStopKey = (String) stopKey.next();
                curntStpTim = DataParser.getDataParser().getRealTimes().getStopTimesByOurId(curntStopKey);
                curntStpTim.resetTheoTimes();

                curntDay = stpTimJson.getJSONObject(curntStopKey);
                Iterator stopDay = curntDay.keys();
                while (stopDay.hasNext()) {
                    curntDayKey = (String)stopDay.next();
                    curntStpTim.addTheoTimes(curntDay.getJSONArray(curntDayKey), date, Integer.parseInt(curntDayKey));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
