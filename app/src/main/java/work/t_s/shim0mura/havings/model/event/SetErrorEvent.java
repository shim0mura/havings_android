package work.t_s.shim0mura.havings.model.event;

/**
 * Created by shim0mura on 2015/11/07.
 */
public class SetErrorEvent {

    public int resourceId;
    public String errorStr;

    public SetErrorEvent(int resourceId, String errorStr){
        this.resourceId = resourceId;
        this.errorStr = errorStr;
    }
}
