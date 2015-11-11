package flying.grub.tamtime.data;

import android.content.Context;
import android.util.Log;

import flying.grub.tamtime.data.datahandler.DisruptEventHandler;
import flying.grub.tamtime.data.datahandler.RealTimes;
import flying.grub.tamtime.data.datahandler.ReportEvent;
import flying.grub.tamtime.data.datahandler.TamMap;
import flying.grub.tamtime.data.datahandler.TheoricTimes;

public class DataParser {

    private static final String TAG = DataParser.class.getSimpleName();

    private DisruptEventHandler disruptEventHandler;
    private ReportEvent reportEvent;
    private RealTimes realTimes;
    private TamMap map;
    private TheoricTimes theoricTimes;

    private static DataParser data;

    private DataParser() {
        data = this;
    }

    public void init(Context context) {
        reportEvent = new ReportEvent(context);
        disruptEventHandler = new DisruptEventHandler(context);
        realTimes = new RealTimes(context);
        map = new TamMap(context);
        theoricTimes = new TheoricTimes(context);
        map.update();
    }

    public static synchronized DataParser getDataParser() {
        if (data == null) {
            return new DataParser();
        } else {
            return data;
        }
    }

    public void update() {
        Log.d(TAG, "UPDATE");
        realTimes.update();
        reportEvent.update();
        disruptEventHandler.update();
    }

    public DisruptEventHandler getDisruptEventHandler() {
        return disruptEventHandler;
    }

    public TamMap getMap() {
        return map;
    }

    public RealTimes getRealTimes() {
        return realTimes;
    }

    public ReportEvent getReportEvent() {
        return reportEvent;
    }

    public TheoricTimes getTheoricTimes() {
        return theoricTimes;
    }
}
