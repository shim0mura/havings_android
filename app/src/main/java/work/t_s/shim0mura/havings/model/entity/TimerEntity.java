package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

/**
 * Created by shim0mura on 2016/01/16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TimerEntity implements Serializable {

    public int id;

    public String name;
    public int listId;
    public String listName;
    public Boolean isActive;
    public Boolean isDeleted;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date nextDueAt;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date tmpNextDueAt;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date overDueFrom;

    public Boolean isRepeating;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date latestCalcAt;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date startAt;

    public int repeatBy;

    public int repeatMonthInterval;
    public int repeatDayOfMonth;

    public int repeatWeek;
    public int repeatDayOfWeek;

    public int noticeHour;
    public int noticeMinute;

    // タスク完了時にのみ使用
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date doneAt;

    public TimerEntity(){
        Calendar nextCalendar = new GregorianCalendar();
        int minute = nextCalendar.get(Calendar.MINUTE);
        if(minute > 0 && minute < 15){
            minute = 15;
        }else if(minute < 30){
            minute = 30;
        }else if(minute < 45){
            minute = 45;
        }else{
            minute = 0;
            nextCalendar.set(Calendar.HOUR_OF_DAY, nextCalendar.get(Calendar.HOUR_OF_DAY) + 1);
        }
        nextCalendar.set(Calendar.MINUTE, minute);
        nextCalendar.set(Calendar.HOUR_OF_DAY, nextCalendar.get(Calendar.HOUR_OF_DAY) + 1);

        Calendar currentCalendar = Calendar.getInstance();

        nextDueAt = new Date(nextCalendar.getTimeInMillis());
        tmpNextDueAt = new Date(nextCalendar.getTimeInMillis());
        isRepeating = false;
        startAt = new Date(currentCalendar.getTimeInMillis());
        latestCalcAt = new Date(currentCalendar.getTimeInMillis());
        repeatBy = 0;
        repeatMonthInterval = 0;
        repeatDayOfMonth = nextCalendar.get(Calendar.DAY_OF_MONTH);
        repeatWeek = 0;
        repeatDayOfWeek = nextCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        noticeHour = nextCalendar.get(Calendar.HOUR_OF_DAY);
        noticeMinute = nextCalendar.get(Calendar.MINUTE);
        isActive = true;
        isDeleted = false;
    }

}
