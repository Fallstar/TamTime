package flying.grub.tamtime.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by fly on 10/26/15.
 */
public class HandlerDistuptEvent implements DataHandler {

    private static final String TAG = HandlerDistuptEvent.class.getSimpleName();

    private Context context;
    public ArrayList<DisruptEvent> disruptList;

    public ArrayList<JSONObject> temp;

    private static final String JSON_ALL_DISRUPT = "http://www.tam-direct.com/webservice/data.php?pattern=cityway&path=GetDisruptedLines%2Fjson%3Fkey%3DTAM%26langID%3D1";
    private static final String JSON_LINE_DISRUPT = "http://www.tam-direct.com/webservice/data.php?pattern=cityway&path=GetLineDisruptions%2Fjson%3Fkey%3DTAM%26langID%3D1%26ligID%3D";


    public HandlerDistuptEvent(Context context) {
        disruptList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void setData() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                JSON_ALL_DISRUPT, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getDisruptEventList(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        });
        VolleyApp.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    public void getDisruptEventList(JSONObject response) throws Exception {
        temp = new ArrayList<>();
        JSONArray list = response.getJSONObject("DisruptionServiceObj").getJSONArray("Line");
        for (int i=0; i<list.length(); i++) {
            callGetLineDisruptEvent(list.getJSONObject(i).getInt("id"));
            Log.d(TAG, JSON_LINE_DISRUPT + list.getJSONObject(i).getInt("id"));
        }
        setDisruptEvent();
    }

    public void callGetLineDisruptEvent(int linkId) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                JSON_LINE_DISRUPT + linkId, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            temp.addAll(getLineDisruptEvent(response));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        });
        VolleyApp.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    public ArrayList<JSONObject> getLineDisruptEvent(JSONObject response) throws Exception {
        JSONArray resJson = response.getJSONObject("DisruptionServiceObj").optJSONArray("Disruption");
        ArrayList<JSONObject> res = new ArrayList<JSONObject>();
        if (resJson != null) {
            for (int i=0; i<resJson.length(); i++) res.add(resJson.getJSONObject(i));
        } else {
            res.add(response.getJSONObject("DisruptionServiceObj").getJSONObject("Disruption"));
        }
        return res;
    }

    public void setDisruptEvent() throws JSONException {
        Line line;
        String title;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (this.disruptList.size() > 0) this.disruptList.get(0).destroy(); // Clear all the disrupt event
        for (JSONObject eventJson : temp) {
            line = DataParser.getDataParser().getLineByNum(eventJson.getJSONObject("DisruptedLine").getInt("number"));
            Calendar beginDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            try {
                beginDate.setTime(sdf.parse(eventJson.getString("beginValidityDate").replace("T", " ")));
                endDate.setTime(sdf.parse(eventJson.getString("endValidityDate").replace("T", " ")));
                title = eventJson.getString("title");
                new DisruptEvent(DataParser.getDataParser(), line, beginDate, endDate, title); //The event put himself in Data & Line ArrayList
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addDisruptEvent(DisruptEvent event) {
        this.disruptList.add(event);
    }

    public void removeDisruptEvent(DisruptEvent event) {
        this.disruptList.remove(event);
    }
}
