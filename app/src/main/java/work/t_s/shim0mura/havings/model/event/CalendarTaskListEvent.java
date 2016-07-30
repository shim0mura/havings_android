package work.t_s.shim0mura.havings.model.event;

import java.util.ArrayList;

import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;

/**
 * Created by shim0mura on 2016/07/28.
 */

public class CalendarTaskListEvent {
    public ArrayList<TaskWrapperEntity> taskWrapperEntities;

    public CalendarTaskListEvent(ArrayList<TaskWrapperEntity> entities){
        taskWrapperEntities = entities;
    }
}