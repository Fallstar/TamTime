package flying.grub.tamtime.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import de.greenrobot.event.EventBus;

/**
 * Created by fly on 10/26/15.
 */
public class HandlerDistuptEvent implements DataHandler {

    private static final String TAG = HandlerDistuptEvent.class.getSimpleName();

    private Context context;
    public ArrayList<DisruptEvent> disruptList;

    public int eventToDownload;

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
                            setDisruptEvent(getDisruptEventList(response));
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

    @Override
    public void dataUpdate() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.Type.EVENT_UPDATE));
    }

    public ArrayList<JSONObject> getDisruptEventList(JSONObject response) throws Exception {
        ArrayList<JSONObject> res = new ArrayList<>();
        JSONArray list = response.getJSONObject("DisruptionServiceObj").getJSONArray("Line");
        for (int i=0; i<list.length(); i++) {
            try {
                res.addAll(this.getLineDisruptEvent(list.getJSONObject(i).getInt("id")));
            } catch (Exception e) {
                System.err.println(JSON_LINE_DISRUPT + list.getJSONObject(i).getInt("id"));
                e.printStackTrace();
            }
        }
        return res;
    }

    public ArrayList<JSONObject> getLineDisruptEvent(int linkId) throws Exception {
        URL request = new URL(JSON_LINE_DISRUPT + linkId);
        Scanner scanner = new Scanner(request.openStream());
        String response = scanner.useDelimiter("\\Z").next();
        scanner.close();
        JSONObject all = new JSONObject(response);
        JSONArray resJson = all.getJSONObject("DisruptionServiceObj").optJSONArray("Disruption");
        ArrayList<JSONObject> res = new ArrayList<>();
        if (resJson != null) {
            for (int i=0; i<resJson.length(); i++) res.add(resJson.getJSONObject(i));
        } else {
            res.add(all.getJSONObject("DisruptionServiceObj").getJSONObject("Disruption"));
        }
        return res;
    }

    public void setDisruptEvent(ArrayList<JSONObject> jsonEventList) throws JSONException {
        Line line;
        String title;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (this.disruptList.size() > 0) this.disruptList.get(0).destroy(); // Clear all the disrupt event
        for (JSONObject eventJson : jsonEventList) {
            line = DataParser.getDataParser().getLineByNum(eventJson.getJSONObject("DisruptedLine").getInt("number"));
            Calendar beginDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            try {
                beginDate.setTime(sdf.parse(eventJson.getString("beginValidityDate").replace("T", " ")));
                endDate.setTime(sdf.parse(eventJson.getString("endValidityDate").replace("T", " ")));
                title = eventJson.getString("title");
                new DisruptEvent(DataParser.getDataParser(), line, beginDate, endDate, title); //The event put himself in Data & Line ArrayList
                dataUpdate();
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
