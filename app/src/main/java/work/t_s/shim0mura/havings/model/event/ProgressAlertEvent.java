package work.t_s.shim0mura.havings.model.event;

import android.app.Activity;
import android.app.ProgressDialog;

import work.t_s.shim0mura.havings.R;

/**
 * Created by shim0mura on 2016/08/03.
 */

public class ProgressAlertEvent {

    private String title;

    public ProgressAlertEvent(String t){
        this.title = t;
    }

    public ProgressDialog showProgress(Activity activity){
        return ProgressDialog.show(activity, title, activity.getString(R.string.prompt_sending), true);
    }
}
