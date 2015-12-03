package flying.grub.tamtime.data.datahandler;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;

import flying.grub.tamtime.R;
import flying.grub.tamtime.data.DataParser;
import flying.grub.tamtime.data.Line;
import flying.grub.tamtime.data.Route;
import flying.grub.tamtime.data.Stop;
import flying.grub.tamtime.data.StopTimes;

/**
 * Created by fly on 11/9/15.
 */
public class TamMap implements DataHandler {

    private static final String TAG = TamMap.class.getSimpleName();
    private static final String JSON_PLAN = "http://bl00m.science/TamTimeData/map.json";
    private ArrayList<Line> linesList;
    private ArrayList<Stop> stopList;


    private Context context;

    public TamMap(Context context) {
        this.stopList = new ArrayList<>();
        this.linesList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void update() {
        String json = null;
        try {
            InputStream is = context.getResources().openRawResource(R.raw.map);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            setMap(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Setup toutes les instance nécéssaire
    public void setMap(JSONObject planJson) throws JSONException {
        this.linesList = new ArrayList<Line>();
        this.stopList = new ArrayList<Stop>();

        Line curntLine;
        Stop curntStop;
        Route curntRoute;
        StopTimes stpTimes;

        try {
            // Create all Stops
            JSONArray stopsDetailsJson = planJson.getJSONArray("stop_details");
            JSONObject curntStpJson;
            for (int i=0; i<stopsDetailsJson.length(); i++) {
                curntStpJson = stopsDetailsJson.getJSONObject(i);
                curntStop = new Stop(curntStpJson.getString("name"), curntStpJson.getInt("our_id"), curntStpJson.getDouble("lat"), curntStpJson.getDouble("lon"));
                this.stopList.add(curntStop);
            }
            curntStop = null; // Need to be null

            // Create all Lines/Routes/StopTimes
            JSONArray linesInfoJson = planJson.getJSONArray("lines");
            JSONObject jsonLine;
            JSONArray jsonRoutesList;
            JSONArray jsonStopsList;
            for (int i=0; i<linesInfoJson.length(); i++) { // Browse the lines JsonArray
                jsonLine = linesInfoJson.getJSONObject(i);
                curntLine = new Line(jsonLine.getInt("num"), jsonLine.getString("id"));

                jsonRoutesList = jsonLine.getJSONArray("routes");
                for (int j=0; j<jsonRoutesList.length(); j++) { // Browse the routes array
                    curntRoute = new Route(jsonRoutesList.getJSONObject(j).getString("direction"), curntLine, j+1);
                    curntLine.addRoute(curntRoute);

                    jsonStopsList = jsonRoutesList.getJSONObject(j).getJSONArray("stops");
                    for (int k=0; k<jsonStopsList.length(); k++) { // Browse the stops (StopTimes)array
                        curntStop = this.getStopByName(jsonStopsList.getJSONObject(k).getString("name")); // Get the stop
                        curntStop.addLine(curntLine);

                        stpTimes = new StopTimes(curntRoute, curntStop, curntLine, jsonStopsList.getJSONObject(k).getInt("stop_id"));
                        DataParser.getDataParser().getRealTimes().addStpTimes(stpTimes);
                        curntRoute.addStopTimes(stpTimes);
                    }
                }
                int e=0;
                while (e < this.linesList.size() && this.linesList.get(e).getLineNum() < curntLine.getLineNum()) e++;
                this.linesList.add(e, curntLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stop getStopByName(String name) {
        for (Stop stp : this.stopList) {
            if (stp.getName().equals(name)) return stp;
        }
        return null;
    }

    // SETTER
    public void setAlldistance(Location user) {
        for (Stop stop : stopList) {
            stop.calcDistanceFromUser(user);
        }
    }

    // GET
    public Line getLine(int i){
        if (linesList != null) return linesList.get(i);
        return null;
    }

    public Line getLineByNum(int num) {
        for (Line l : this.linesList) {
            if (l.getLineNum() == num) return l;
        }
        return null;
    }

    public ArrayList<Line> getLinesList() {
        return linesList;
    }

    public ArrayList<Stop> getStopList() {
        return stopList;
    }

    public Stop getStopByOurId(int ourId) {
        for (Stop stp : this.stopList) {
            if (stp.getOurId() == ourId) return stp;
        }
        return null;
    }

    public ArrayList<Stop> getAllNearStops() {
        ArrayList<Stop> res = new ArrayList<>();
        for (Stop s : stopList) {
            if (s.getDistanceFromUser() <= 500) { // in meter
                res.add(s);
            }
        }
        return res;
    }

    public ArrayList<Stop> searchInStops(String search, int number) {
        ArrayList<Stop> res = new ArrayList<>();
        String normalizedSearch = normalize(search);
        for (Stop s : stopList) {
            if (number != -1 && number == res.size()) {
                break;
            }
            if (s.getNormalisedName() == null) {
                s.setNormalisedName(normalize(s.getName()));
            }
            if (s.getNormalisedName().contains(normalizedSearch)) {
                res.add(s);
            }
        }
        return res;
    }

    public String normalize(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "").toLowerCase().replaceAll("[^A-Za-z0-9]", "");
    }

    public ArrayList<Stop> getAllReportStop() {
        ArrayList<Stop> res = new ArrayList<>();
        for (Stop s : stopList) {
            if (s.getReports().size() > 0) {
                res.add(s);
            }
        }
        return res;
    }
}
