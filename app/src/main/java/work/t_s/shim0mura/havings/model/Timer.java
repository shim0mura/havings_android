package work.t_s.shim0mura.havings.model;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;

/**
 * Created by shim0mura on 2016/01/17.
 */
public class Timer {

    public static final int MAX_COUNT_PER_LIST = 4;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分");
    public static SimpleDateFormat sdfWithoutYear = new SimpleDateFormat("MM月dd日HH時mm分");
    public static final String DUE_DATEFORMAT = "yyyy年MM月dd日(E) HH時mm分";
    public static final String DUE_DATEFORMAT_WITHOUT_YEAR = "MM月dd日(E) HH時mm分";

    public static final String IS_REPEATING = "IsRepeating";
    public static final String REPEAT_BY = "RepeatBy";
    public static final String MONTH_INTERVAL = "MonthInterval";
    public static final String DAY_OF_MONTH = "DayOfMonth";
    public static final String WEEK_NUMBER = "WeekNumber";
    public static final String DAY_OF_WEEK = "DayOfWeek";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";

    public static final String REPEAT_BY_DAY_EVERY_MONTH = "prompt_repeat_by_day_every_month";
    public static final String REPEAT_BY_DAY_EVERY_TWO_MONTH = "prompt_repeat_by_day_every_two_month";
    public static final String REPEAT_BY_DAY_EVERY_THREE_MONTH = "prompt_repeat_by_day_every_three_month";
    public static final String REPEAT_BY_DAY_EVERY_FOUR_MONTH = "prompt_repeat_by_day_every_four_month";
    public static final String REPEAT_BY_DAY_EVERY_SIX_MONTH = "prompt_repeat_by_day_every_six_month";

    public static final String REPEAT_BY_WEEK_EVERY_WEEK = "prompt_repeat_by_week_every_week";
    public static final String REPEAT_BY_WEEK_FIRST_WEEK = "prompt_repeat_by_week_first_week";
    public static final String REPEAT_BY_WEEK_SECOND_WEEK = "prompt_repeat_by_week_second_week";
    public static final String REPEAT_BY_WEEK_THIRD_WEEK = "prompt_repeat_by_week_third_week";
    public static final String REPEAT_BY_WEEK_FOURTH_WEEK = "prompt_repeat_by_week_fourth_week";
    public static final String REPEAT_BY_WEEK_LAST_WEEK = "prompt_repeat_by_week_last_week";

    public static final String REPEAT_BY_WEEK_SUNDAY = "prompt_repeat_by_week_sunday";
    public static final String REPEAT_BY_WEEK_MONDAY = "prompt_repeat_by_week_monday";
    public static final String REPEAT_BY_WEEK_TUESDAY = "prompt_repeat_by_week_tuesday";
    public static final String REPEAT_BY_WEEK_WEDNESDAY = "prompt_repeat_by_week_wednesday";
    public static final String REPEAT_BY_WEEK_THURSDAY = "prompt_repeat_by_week_thursday";
    public static final String REPEAT_BY_WEEK_FRIDAY = "prompt_repeat_by_week_friday";
    public static final String REPEAT_BY_WEEK_SATURDAY = "prompt_repeat_by_week_saturday";

    public static final Map<Integer, String> repeatByDayMonthInterval = new TreeMap<Integer, String>(){{
        put(0, REPEAT_BY_DAY_EVERY_MONTH);
        put(1, REPEAT_BY_DAY_EVERY_TWO_MONTH);
        put(2, REPEAT_BY_DAY_EVERY_THREE_MONTH);
        put(3, REPEAT_BY_DAY_EVERY_FOUR_MONTH);
        put(5, REPEAT_BY_DAY_EVERY_SIX_MONTH);
    }};

    public static final Map<Integer, String> repeatByWeekInterval = new TreeMap<Integer, String>(){{
        put(0, REPEAT_BY_WEEK_EVERY_WEEK);
        put(1, REPEAT_BY_WEEK_FIRST_WEEK);
        put(2, REPEAT_BY_WEEK_SECOND_WEEK);
        put(3, REPEAT_BY_WEEK_THIRD_WEEK);
        put(4, REPEAT_BY_WEEK_FOURTH_WEEK);
        put(5, REPEAT_BY_WEEK_LAST_WEEK);
    }};

    // サーバ側だとSundayが0になるけど、javaのCalendarの仕様で
    // Sundayが1になってほかも1ずつ多い値になるので、androidの表示上ではjavaのCalendarに合わせる
    public static final Map<Integer, String> repeatByDayOfWeek = new TreeMap<Integer, String>(){{
        put(1, REPEAT_BY_WEEK_SUNDAY);
        put(2, REPEAT_BY_WEEK_MONDAY);
        put(3, REPEAT_BY_WEEK_TUESDAY);
        put(4, REPEAT_BY_WEEK_WEDNESDAY);
        put(5, REPEAT_BY_WEEK_THURSDAY);
        put(6, REPEAT_BY_WEEK_FRIDAY);
        put(7, REPEAT_BY_WEEK_SATURDAY);
    }};

    public static List<RepeatInterval> getRepeatIntervalObj(Activity activity, Map<Integer, String> repeatType){
        List<RepeatInterval> p = new ArrayList<RepeatInterval>();
        for(Map.Entry<Integer, String> type : repeatType.entrySet()) {
            p.add(new RepeatInterval(type.getKey(), repeatType, activity));
            Timber.d("create intervalType %s, %s", type.getKey(), type.toString());
        }
        return p;
    }

    public static String getFormatDueString(Date d){
        return DateFormat.format(DUE_DATEFORMAT, d).toString();
    }

    public static String getFormatDueStringWithoutYear(Date d){
        return DateFormat.format(DUE_DATEFORMAT_WITHOUT_YEAR, d).toString();
    }

    public static String getIntervalString(Activity activity, TimerEntity timerEntity){
        StringBuilder sb = new StringBuilder();
        if(timerEntity.isRepeating){
            Resources resources = activity.getResources();
            if(timerEntity.repeatBy != 0){
                int week_interval_prompt_id = resources.getIdentifier(repeatByWeekInterval.get(timerEntity.repeatWeek), "string", activity.getPackageName());
                int day_of_week_prompt_id = resources.getIdentifier(repeatByDayOfWeek.get(timerEntity.repeatDayOfWeek + 1), "string", activity.getPackageName());
                sb.append(resources.getString(week_interval_prompt_id));
                sb.append(resources.getString(day_of_week_prompt_id));
            }else{
                int month_interval_prompt_id = resources.getIdentifier(repeatByDayMonthInterval.get(timerEntity.repeatMonthInterval), "string", activity.getPackageName());
                sb.append(resources.getString(month_interval_prompt_id));
                sb.append(" " + timerEntity.repeatDayOfMonth);
                sb.append(activity.getString(R.string.prompt_day));
            }
            sb.append(activity.getString(R.string.postfix_prompt_next_due_at));
        }else{
            sb.append(activity.getString(R.string.prompt_timer_no_repeat));
        }
        return sb.toString();
    }

    public static String getRemainingTimeString(Activity activity, Date nextDue){
        long seconds = new Date().getTime() - nextDue.getTime();
        long absSeconds = Math.abs(seconds);
        StringBuilder sb = new StringBuilder();
        //sb.append(getFormatDueStringWithoutYear(nextDue));

        sb.append(" (");
        Boolean overTime;
        if(seconds > 0){
            overTime = true;
            //sb.append(activity.getString(R.string.postfix_prompt_next_due_at_again) + );
        }else{
            overTime = false;
            //sb.append(activity.getString(R.string.postfix_prompt_next_due_at) + " (");
            sb.append(activity.getString(R.string.prefix_remaining_due_time));
        }

        if(absSeconds < 60 * 60 * 1000){
            sb.append(String.format("%d", absSeconds / (60 * 1000)));
            sb.append(activity.getString(R.string.prompt_minute));
        }else if(absSeconds < 24 * 60 * 60 * 1000){
            sb.append(String.format("%d", absSeconds / (60 * 60 * 1000)));
            sb.append(activity.getString(R.string.prompt_hour));
        }else{
            sb.append(String.format("%d", absSeconds / (24 * 60 * 60 * 1000)));
            sb.append(activity.getString(R.string.prompt_day));
        }

        if(overTime){
            sb.append(activity.getString(R.string.postfix_remaining_due_time));
        }

        sb.append(")");

        return sb.toString();
    }

    public static int getPercentageUntilDueDate(Date nextDue, Date startAt){
        long current = new Date().getTime();
        if(current > nextDue.getTime()) {
            return 100;
        }else if(startAt.getTime() > current){
            return 0;
        }else{
            return (int)(100 - ((float)(nextDue.getTime() - current) / (float)(nextDue.getTime() - startAt.getTime())) * 100);
        }
    }

    public static int getProgressBarColor(int percentage){
        return Color.rgb((255 * percentage) / 100, (255 * (100 - percentage)) / 100, 0);
    }

    public static Calendar getNextDueAtFromMonth(Calendar currentDate, Map<String, Integer> values){
        Calendar candidateDate = new GregorianCalendar(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1);
        Calendar checkDate = Calendar.getInstance();
        int candidateDay = values.get(DAY_OF_MONTH);
        int monthInterval = values.get(MONTH_INTERVAL);
        int hour = values.get(HOUR);
        int minute = values.get(MINUTE);

        if(candidateDay >= currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)){
            candidateDate.set(Calendar.DAY_OF_MONTH, currentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        }else{
            candidateDate.set(Calendar.DAY_OF_MONTH, candidateDay);
        }

        candidateDate.set(Calendar.HOUR_OF_DAY, hour);
        candidateDate.set(Calendar.MINUTE, minute);
        candidateDate.set(Calendar.SECOND, 0);
        candidateDate.set(Calendar.MILLISECOND, 0);


        if(currentDate.compareTo(candidateDate) >= 0){
            candidateDate.set(Calendar.MONTH, candidateDate.get(Calendar.MONTH) + 1);
        }

        checkDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + monthInterval, 1);
        int candidateDateLastDay = checkDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        if(candidateDay >= candidateDateLastDay){
            candidateDate.set(candidateDate.get(Calendar.YEAR), candidateDate.get(Calendar.MONTH) + monthInterval, candidateDateLastDay, hour, minute);
        }else{
            candidateDate.set(Calendar.MONTH, candidateDate.get(Calendar.MONTH) + monthInterval);
        }

        return candidateDate;
    }

    public static int getCandidateDateFromMonth(int currentYear, int currentMonth, int candidateYear, int candidateMonth, Map<String, Integer> values){
        int monthDiff = (candidateYear - currentYear) * 12 + (candidateMonth - currentMonth);
        int candidateDay = values.get(DAY_OF_MONTH);

        if(monthDiff % (values.get(MONTH_INTERVAL) + 1) == 0){
            Calendar candidateDate = new GregorianCalendar(candidateYear, candidateMonth, 1);
            int candidateDateLastDay = candidateDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            if(candidateDay > candidateDateLastDay){
                candidateDay = candidateDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
        }else{
            candidateDay = 0;
        }
        return candidateDay;
    }

    public static Calendar getNextDueAtFromWeek(Calendar currentDate, Map<String, Integer> values){
        Calendar candidateDate;
        int weekNumber = values.get(WEEK_NUMBER);
        int week = values.get(DAY_OF_WEEK);
        int hour = values.get(HOUR);
        int minute = values.get(MINUTE);

        if(weekNumber == 0){
            candidateDate = getNextWeekDate(currentDate, week);
        }else{
            candidateDate = getDateFromWeekNumber(currentDate, weekNumber, week);
            candidateDate.set(Calendar.HOUR_OF_DAY, hour);
            candidateDate.set(Calendar.MINUTE, minute);
            candidateDate.set(Calendar.SECOND, 0);
            candidateDate.set(Calendar.MILLISECOND, 0);
            if(currentDate.compareTo(candidateDate) >= 0){
                candidateDate = getDateFromWeekNumber(new GregorianCalendar(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.DAY_OF_MONTH)), weekNumber, week);
            }
        }
        candidateDate.set(Calendar.HOUR_OF_DAY, hour);
        candidateDate.set(Calendar.MINUTE, minute);

        return candidateDate;
    }

    public static List<Integer> getCandidateDatesFromWeek(int year, int month, Map<String, Integer> values){
        int weekNumber = values.get(WEEK_NUMBER);
        int week = values.get(DAY_OF_WEEK);
        Calendar calendar = new GregorianCalendar(year, month, 1);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        List<Integer> candidateDates = new ArrayList<>();
        int candidateDay = week - firstDayOfWeek + 1;
        if(candidateDay <= 0){
            candidateDay = candidateDay + 7;
        }

        if(weekNumber == 0){
            while(candidateDay <= lastDayOfMonth){
                candidateDates.add(candidateDay);
                candidateDay = candidateDay + 7;
            }
        }else{
            candidateDay = candidateDay + (7 * (weekNumber - 1));

            if(candidateDay > lastDayOfMonth){
                candidateDay = candidateDay - 7;
            }
            candidateDates.add(candidateDay);
        }

        return candidateDates;
    }

    private static Calendar getNextWeekDate(Calendar currentCalendar, int week){
        Calendar nextDate = Calendar.getInstance();
        int lastDayOfMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int todayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        int val = (7 - todayOfWeek + week) % 7;

        if(val == 0){
            val = 7;
        }

        int candidateDay = currentCalendar.get(Calendar.DAY_OF_MONTH) + val;
        if(candidateDay > lastDayOfMonth){
            nextDate.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH) + 1, candidateDay - lastDayOfMonth);
        }else{
            nextDate.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), candidateDay);
        }
        return nextDate;
    }

    private static Calendar getDateFromWeekNumber(Calendar currentCalendar, int weekNumber, int week){
        Calendar nextDate = Calendar.getInstance();
        int firstDayOfWeek = new GregorianCalendar(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 1).get(Calendar.DAY_OF_WEEK);
        int lastDayOfMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstCandidateDay = week - firstDayOfWeek + 1;

        if(firstCandidateDay <= 0){
            firstCandidateDay = firstCandidateDay + 7;
        }

        int candidateDay = firstCandidateDay + (7 * (weekNumber - 1));
        while(candidateDay > lastDayOfMonth){
            candidateDay = candidateDay - 7;
        }

        nextDate.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), candidateDay);
        return nextDate;
    }

    public static Boolean isValidTimerName(TimerEntity timerEntity) {
        if(timerEntity.name != null && !timerEntity.name.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    public static Boolean isValidDueTime(TimerEntity timerEntity) {
        Date currentDate = new Date();
        if(currentDate.compareTo(timerEntity.nextDueAt) >= 0){
            return false;
        } else {
            return true;
        }
    }

    public static class RepeatInterval{

        final private int typeId;
        final private String typePrompt;

        public RepeatInterval(int type, Map<Integer, String> intervalType, Activity activity){
            typeId = type;
            Resources resources = activity.getResources();
            int prompt_id = resources.getIdentifier(intervalType.get(type), "string", activity.getPackageName());
            typePrompt = resources.getString(prompt_id);
        }

        public int getTypeId(){
            return typeId;
        }

        @Override
        public String toString() {
            return typePrompt;
        }
    }

}
