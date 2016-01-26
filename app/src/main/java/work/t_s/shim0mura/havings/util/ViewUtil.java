package work.t_s.shim0mura.havings.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.util.Calendar;
import java.util.Date;

import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;

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

    public static String secondsToEasyDateFormat(Activity activity, long seconds){
        StringBuilder sb = new StringBuilder();

        if(seconds < 0){
            sb.append("0");
            sb.append(activity.getString(R.string.prompt_minute));
        }else if(seconds < 60 * 60 * 1000){
            sb.append(String.format("%d", seconds / (60 * 1000)));
            sb.append(activity.getString(R.string.prompt_minute));
        }else if(seconds < 24 * 60 * 60 * 1000){
            sb.append(String.format("%d", seconds / (60 * 60 * 1000)));
            sb.append(activity.getString(R.string.prompt_hour));
        }else{
            sb.append(String.format("%d", seconds / (24 * 60 * 60 * 1000)));
            sb.append(activity.getString(R.string.prompt_day));
        }

        return sb.toString();
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

    public static void toggleLoading(Activity activity, final ToggleLoadingEvent event){
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress listSpinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

            event.hiding.setVisibility(View.GONE);
            event.hiding.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    event.hiding.setVisibility(View.GONE);
                }
            });

            event.showing.setVisibility(View.VISIBLE);
            event.showing.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    event.showing.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            event.showing.setVisibility(View.VISIBLE);
            event.hiding.setVisibility(View.GONE);
        }
    }
}
