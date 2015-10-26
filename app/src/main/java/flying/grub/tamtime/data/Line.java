package flying.grub.tamtime.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Line {
    String lineId;
    int num;
    ArrayList<Route> routesList;
    private ArrayList<DisruptEvent> disruptEventList;

    public Line(int num, String lineId) {
        this.num = num;
        this.lineId = lineId;
        this.routesList = new ArrayList<>();
        this.disruptEventList = new ArrayList<>();
    }

    // Get
    public Route getRoute(String direction){
        for (int i = 0; i< routesList.size(); i++){
            if (routesList.get(i).getDirection().equals(direction)){
                return routesList.get(i);
            }
        }
        return null;
    }

    public ArrayList<Route> getRoutes() {
        return this.routesList;
    }

    public int getRouteCount(){
        return this.routesList.size();
    }

    public String getLineId(){
        return this.lineId;
    }

    public int getLineNum(){
        return this.num;
    }

    public ArrayList<DisruptEvent> getDisruptEventList() {
        return disruptEventList;
    }

    // Add
    public void addRoute(Route r) {
        this.routesList.add(r);
    }

    public void removeDisruptEvent(DisruptEvent event) {
        this.disruptEventList.remove(event);
    }

    public void addDisruptEvent(DisruptEvent event) {
        this.disruptEventList.add(event);
    }
}
