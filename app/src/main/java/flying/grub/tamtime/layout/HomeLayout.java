package flying.grub.tamtime.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.zip.Inflater;

/**
 * Created by fly on 11/29/15.
 */
public interface HomeLayout {

    boolean isHeader();
    boolean isFooter();
    View getView(LayoutInflater inflater, ViewGroup container);
}
