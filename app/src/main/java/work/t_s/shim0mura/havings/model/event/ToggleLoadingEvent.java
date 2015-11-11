package work.t_s.shim0mura.havings.model.event;

import android.view.View;

/**
 * Created by shim0mura on 2015/11/10.
 */
public class ToggleLoadingEvent {
    public View showing;
    public View hiding;

    public ToggleLoadingEvent(View showing, View hiding){
        this.showing = showing;
        this.hiding = hiding;
    }
}
