package work.t_s.shim0mura.havings.model.event;

import android.graphics.AvoidXfermode;

import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;

/**
 * Created by shim0mura on 2015/11/07.
 */
public class SetErrorEvent {

    public int resourceId;
    public String errorStr;
    public int resultType;
    public ModelErrorEntity errorEntity;

    public SetErrorEvent(int resourceId){
        this.resourceId = resourceId;
    }

    public SetErrorEvent(int resourceId, String errorStr){
        this.resourceId = resourceId;
        this.errorStr = errorStr;
    }

    public SetErrorEvent(int result, ModelErrorEntity errorEntity){
        this.resultType = result;
        this.errorEntity = errorEntity;
    }

}
