package work.t_s.shim0mura.havings.model.event;

import java.util.ArrayList;

import work.t_s.shim0mura.havings.model.entity.TimerEntity;

/**
 * Created by shim0mura on 2016/04/01.
 */
public class TimerListRenderEvent {

    public ArrayList<TimerEntity> timerListEntities;

    public TimerListRenderEvent(ArrayList<TimerEntity> entities){
        timerListEntities = entities;
    }

}
