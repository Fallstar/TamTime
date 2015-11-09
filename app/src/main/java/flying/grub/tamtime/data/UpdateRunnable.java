package flying.grub.tamtime.data;

import android.content.Context;

import android.os.Handler;

/**
 * Created by fly on 10/20/15.
 */
public class UpdateRunnable implements Runnable {

    private Handler handler = new Handler();

    private static final int TIME = 30000; // 30 sec

    @Override
    public void run() {
        DataParser.getDataParser().update();
        handler.postDelayed(this, TIME);
    }

    public void stop() {
        handler.removeCallbacks(this);
    }
}
