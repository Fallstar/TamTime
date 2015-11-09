package flying.grub.tamtime.data.datahandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.greenrobot.event.EventBus;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.DisruptEvent;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.MessageEvent;
import flying.grub.tamtime.data.VolleyApp;

/**
 * Created by fly on 10/26/15.
 */
public class DisruptEventHandler implements DataHandler {

    private static final String TAG = DisruptEventHandler.class.getSimpleName();

    private Context context;
    public ArrayList<DisruptEvent> disruptList;

    private static final String JSON_ALL_DISRUPT = "http://www.tam-direct.com/webservice/data.php?pattern=cityway&path=GetDisruptedLines%2Fjson%3Fkey%3DTAM%26langID%3D1";
    private static final String JSON_LINE_DISRUPT = "http://www.tam-direct.com/webservice/data.php?pattern=cityway&path=GetLineDisruptions%2Fjson%3Fkey%3DTAM%26langID%3D1%26ligID%3D";


    public DisruptEventHandler(Context context) {
        disruptList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void update() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                JSON_ALL_DISRUPT, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(final JSONObject response) {
                        backgroundTask.execute((Runnable) response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());
            }
        });
        VolleyApp.getInstance(context).addToRequestQueue(jsonObjReq);
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
        String response = downloadUrl(JSON_LINE_DISRUPT + linkId);
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
            line = DataParser.getDataParser().getMap().getLineByNum(eventJson.getJSONObject("DisruptedLine").getInt("number"));
            Calendar beginDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            try {
                beginDate.setTime(sdf.parse(eventJson.getString("beginValidityDate").replace("T", " ")));
                endDate.setTime(sdf.parse(eventJson.getString("endValidityDate").replace("T", " ")));
                title = eventJson.getString("title");
                new DisruptEvent(line, beginDate, endDate, title); //The event put himself in Data & Line ArrayList
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public void addDisruptEvent(DisruptEvent event) {
        this.disruptList.add(event);
    }

    public void removeDisruptEvent(DisruptEvent event) {
        this.disruptList.remove(event);
    }

    class backgroundTask extends AsyncTask<JSONObject, Void, Void> {

        protected Void doInBackground(JSONObject... res) {
            try {
                try {
                    ArrayList<JSONObject> events = getDisruptEventList(res[0]);
                    setDisruptEvent(events);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.Type.EVENT_UPDATE));
        }
    }
}
