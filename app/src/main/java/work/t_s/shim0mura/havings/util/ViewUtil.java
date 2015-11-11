package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

/**
 * Created by shim0mura on 2015/11/10.
 */
public class ViewUtil {
    public static void showSimpleAlert(Activity activity, String title, String message){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
