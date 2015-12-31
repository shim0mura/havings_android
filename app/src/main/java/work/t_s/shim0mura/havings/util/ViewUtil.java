package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by shim0mura on 2015/11/10.
 */
public class ViewUtil {

    private static Calendar calendar = Calendar.getInstance();

    public static void showSimpleAlert(Activity activity, String title, String message){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public static String dateToString(Date date, Boolean includeYear){
        // http://www.kaoriya.net/blog/2012/01/17/
        StringBuilder s = new StringBuilder();
        calendar.setTime(date);
        if(includeYear) {
            s.append(calendar.get(Calendar.YEAR)).append('/');
        }
        s.append(calendar.get(Calendar.MONTH) + 1).append('/')
                .append(calendar.get(Calendar.DAY_OF_MONTH)).append(' ');
        return s.toString();
    }

    public static int dpToPix(Context context, int dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int)(dp * (metrics.densityDpi / 160f));
        return px;
    }

    // http://stackoverflow.com/questions/8589645/how-to-determine-mime-type-of-file-in-android
    public static String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
