package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.app.AlertDialog;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.daimajia.swipe.SwipeLayout;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.TimerFormActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;

/**
 * Created by shim0mura on 2016/01/14.
 */
public class TimerPresenter {

    private Activity activity;
    private ItemEntity item;
    private TimerEntity timer;
    private Map<Integer, SwipeLayout> timerLayoutMap = new HashMap<Integer, SwipeLayout>();
    private Calendar currentCalendar = new GregorianCalendar();
    private static final String TIMER_POST_HASH_KEY = "timer";

    private final SundayDecorator sundayDecorator = new SundayDecorator();
    private final SaturdayDecorator saturdayDecorator = new SaturdayDecorator();
    private CandidateDecorator candidateDecorator;

    static ApiService service;

    public TimerPresenter(Context c, ItemEntity i, TimerEntity t){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
        item = i;
        timer = t;
    }

    public int getTimerCounts(){
        return timerLayoutMap.size();
    }

    public void renderListTimers(ViewGroup parent){
        for(TimerEntity timerEntity : item.timers){
            addTimerLayout(parent, timerEntity);
        }
    }

    public void addTimerLayout(ViewGroup parent, TimerEntity timerEntity){
        SwipeLayout addedTimer = addTimerView(parent, timerEntity);
        timerLayoutMap.put(timerEntity.id, addedTimer);
    }

    public void reRenderTimerLayout(ViewGroup parent, TimerEntity timerEntity){
        removeTimerLayout(parent, timerEntity);
        addTimerLayout(parent, timerEntity);
    }

    public void removeTimerLayout(ViewGroup parent, TimerEntity timerEntity){
        SwipeLayout targetView = timerLayoutMap.get(timerEntity.id);
        Timber.d("timerid %s, keys %s", timerEntity.id, timerLayoutMap.keySet().toString());
        if(targetView != null) {
            parent.removeView(targetView);
            timerLayoutMap.remove(timerEntity.id);
        }
    }

    public void setDefaultDecorator(MaterialCalendarView calendarView){
        ArrayList<CalendarDay> dates = new ArrayList<>();

        candidateDecorator = new CandidateDecorator(dates);
        calendarView.addDecorators(
                sundayDecorator, saturdayDecorator, candidateDecorator
        );
    }

    public void updateCandidateDate(MaterialCalendarView calendarView, Map<String, Integer> valueMap){
        ArrayList<CalendarDay> dates = new ArrayList<>();
        CalendarDay selectedDate = calendarView.getSelectedDate();
        CalendarDay currentDate = calendarView.getCurrentDate();
        if(selectedDate == null){
            Timber.d("selected date null");
            selectedDate = currentDate;
        }

        if(valueMap.get(Timer.IS_REPEATING) != 0) {
            if(valueMap.get(Timer.REPEAT_BY) != 0){
                List<Integer> candidateDates = Timer.getCandidateDatesFromWeek(currentDate.getYear(), currentDate.getMonth(), valueMap);
                for(int day : candidateDates){
                    dates.add(CalendarDay.from(currentDate.getYear(), currentDate.getMonth(), day));
                }
            }else{
                int candidateDay = Timer.getCandidateDateFromMonth(selectedDate.getYear(), selectedDate.getMonth(), currentDate.getYear(), currentDate.getMonth(), valueMap);
                if (candidateDay != 0) {
                    dates.add(CalendarDay.from(currentDate.getYear(), currentDate.getMonth(), candidateDay));
                }
            }
        }

        candidateDecorator.setDates(dates);
        calendarView.invalidateDecorators();
    }

    public TimePicker setTimePicker(ViewGroup parent){
        TimePicker t = new TimerPresenter.IntervalTimePicker(new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
        t.setIs24HourView(true);
        t.setPadding(0, -30, 0, -30);
        parent.addView(t);
        return t;
    }

    public void setTimePickerHourAndMinute(TimePicker timePicker, int hour, int minute){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            timePicker.setHour(hour);
            timePicker.setMinute(TimerPresenter.IntervalTimePicker.getMinutePosition(minute));

        } else {
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(TimerPresenter.IntervalTimePicker.getMinutePosition(minute));
        }
    }

    public Map<Integer, Integer> getGetTimePickerHourAndMinute(TimePicker timePicker){
        Map<Integer, Integer> hourMinuteMap = new HashMap<Integer, Integer>();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            hourMinuteMap.put(Calendar.HOUR_OF_DAY, timePicker.getHour());
            hourMinuteMap.put(Calendar.MINUTE, IntervalTimePicker.MINUTES_TABLE[timePicker.getMinute()]);
        } else {
            hourMinuteMap.put(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            hourMinuteMap.put(Calendar.MINUTE, IntervalTimePicker.MINUTES_TABLE[timePicker.getCurrentMinute()]);
        }

        return hourMinuteMap;
    }

    public Map<String, Integer> getDefaultValues(TimerEntity timerEntity){
        Map<String, Integer> valueMap = new HashMap<>();

        valueMap.put(Timer.IS_REPEATING, (timerEntity.isRepeating ? 1 : 0));
        valueMap.put(Timer.REPEAT_BY, timerEntity.repeatBy);
        valueMap.put(Timer.MONTH_INTERVAL, timerEntity.repeatMonthInterval);
        valueMap.put(Timer.DAY_OF_MONTH, timerEntity.repeatDayOfMonth);
        valueMap.put(Timer.WEEK_NUMBER, timerEntity.repeatWeek);
        valueMap.put(Timer.DAY_OF_WEEK, timerEntity.repeatDayOfWeek + 1);
        //valueMap.put(Timer.HOUR, c.get(Calendar.HOUR_OF_DAY));
        //valueMap.put(Timer.MINUTE, c.get(Calendar.MINUTE));
        valueMap.put(Timer.HOUR, timerEntity.noticeHour);
        valueMap.put(Timer.MINUTE, timerEntity.noticeMinute);

        Timber.d(valueMap.toString());

        return valueMap;
    }

    public ArrayAdapter<Timer.RepeatInterval> getRepeatIntervalSpinnerAdapter(Map<Integer, String> intervalType){
        ArrayAdapter<Timer.RepeatInterval> intervalTypeSpinnerAdapter = new ArrayAdapter<Timer.RepeatInterval>(activity, android.R.layout.simple_spinner_item);
        intervalTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(Timer.RepeatInterval type : Timer.getRepeatIntervalObj(activity, intervalType)) {
            intervalTypeSpinnerAdapter.add(type);
        }
        return intervalTypeSpinnerAdapter;
    }

    public void attemptToCreateTimer(TimerEntity timerEntity){
        HashMap<String, TimerEntity> hashItem = new HashMap<String, TimerEntity>();

        if(isValidTimerToPost(timerEntity)){
            hashItem.put(TIMER_POST_HASH_KEY, timerEntity);
        }else{
            return;
        }

        Call<TimerEntity> call = service.createTimer(hashItem);
        call.enqueue(getCallbackOfPostTimer());
    }

    public void attemptToUpdateTimer(TimerEntity timerEntity){
        HashMap<String, TimerEntity> hashItem = new HashMap<String, TimerEntity>();

        if(isValidTimerToPost(timerEntity)){
            hashItem.put(TIMER_POST_HASH_KEY, timerEntity);
        }else{
            return;
        }

        Call<TimerEntity> call = service.updateTimer(timerEntity.id, hashItem);
        call.enqueue(getCallbackOfPostTimer());
    }

    public void attemptToDoneTimer(TimerEntity timerEntity){
        HashMap<String, TimerEntity> hashItem = new HashMap<String, TimerEntity>();

        if(isValidTimerToPost(timerEntity)){
            hashItem.put(TIMER_POST_HASH_KEY, timerEntity);
        }else{
            return;
        }

        Call<TimerEntity> call = service.doneTimer(timerEntity.id, hashItem);
        call.enqueue(getCallbackOfPostTimer());
    }

    public void attemptToDoLaterTimer(TimerEntity timerEntity){
        HashMap<String, TimerEntity> hashItem = new HashMap<String, TimerEntity>();

        if(isValidTimerToPost(timerEntity)){
            hashItem.put(TIMER_POST_HASH_KEY, timerEntity);
        }else{
            return;
        }

        Call<TimerEntity> call = service.doLaterTimer(timerEntity.id, hashItem);
        call.enqueue(getCallbackOfPostTimer());
    }

    public void attemptToEndTimer(TimerEntity timerEntity){
        HashMap<String, TimerEntity> hashItem = new HashMap<String, TimerEntity>();

        Call<TimerEntity> call = service.endTimer(timerEntity.id, hashItem);
        call.enqueue(getCallbackOfPostTimer());
    }

    public void attemptToDeleteTimer(TimerEntity timerEntity){
        Call<TimerEntity> call = service.deleteTimer(timerEntity.id);
        call.enqueue(getCallbackOfPostTimer());
    }

    private Callback<TimerEntity> getCallbackOfPostTimer(){
        return new Callback<TimerEntity>() {
            @Override
            public void onResponse(Response<TimerEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    TimerEntity timer = response.body();
                    BusHolder.get().post(timer);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for(Map.Entry<String, List<String>> e: error.errors.entrySet()){
                        switch(e.getKey()){
                            case "next_due_at":
                                sendErrorToDueTime();
                                break;
                            case "name":
                                sendErrorToTimerName();
                                break;
                            default:
                                break;
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post timer");
                t.printStackTrace();
            }
        };
    }

    private Boolean isValidTimerToPost(TimerEntity timerEntity){
        if(isValidTimerName(timerEntity) && isValidDueTime(timerEntity)){
            return true;
        }else{
            return false;
        }
    }

    private Boolean isValidTimerName(TimerEntity timerEntity){
        if(Timer.isValidTimerName(timerEntity)){
            return true;
        }else{
            sendErrorToTimerName();
            return false;
        }
    }

    private Boolean isValidDueTime(TimerEntity timerEntity){
        if(Timer.isValidDueTime(timerEntity)){
            return true;
        }else{
            sendErrorToDueTime();
            return false;
        }
    }

    private void sendErrorToTimerName(){
        BusHolder.get().post(new SetErrorEvent(getResourceIdByName("timer_name")));
    }

    private void sendErrorToDueTime(){
        BusHolder.get().post(new SetErrorEvent(getResourceIdByName("time_selecter"), activity.getString(R.string.error_invalid_timer_due_at)));
    }

    protected int getResourceIdByName(String id){
        Resources res = activity.getResources();

        return res.getIdentifier(id, "id", activity.getPackageName());
    }

    private SwipeLayout addTimerView(final ViewGroup parent, final TimerEntity timerEntity){
        final SwipeLayout swipeLayout = (SwipeLayout) activity.getLayoutInflater().inflate(R.layout.list_item_timer, null);

        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });

        assignTimerText(swipeLayout, timerEntity, activity);

        TextView nextDueAt = (TextView)swipeLayout.findViewById(R.id.timer_next_due_at);
        if(!timerEntity.isRepeating){
            nextDueAt.setText("");
        }else if(timerEntity.repeatBy != 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(timerEntity.nextDueAt);
            //nextDueAt.setText(Timer.sdfWithoutYear.format(Timer.getNextDueAtFromWeek(c, getDefaultValues(timerEntity)).getTime()));
            nextDueAt.setText(Timer.getFormatDueStringWithoutYear(Timer.getNextDueAtFromWeek(c, getDefaultValues(timerEntity)).getTime()));

        }else if(timerEntity.repeatBy == 0){
            Calendar c = Calendar.getInstance();
            c.setTime(timerEntity.nextDueAt);
            //nextDueAt.setText(Timer.sdfWithoutYear.format(Timer.getNextDueAtFromMonth(c, getDefaultValues(timerEntity)).getTime()));
            nextDueAt.setText(Timer.getFormatDueStringWithoutYear(Timer.getNextDueAtFromMonth(c, getDefaultValues(timerEntity)).getTime()));
        }
        timerEntity.tmpNextDueAt = timerEntity.nextDueAt;


        swipeLayout.findViewById(R.id.timer_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout.open();
            }
        });
        swipeLayout.findViewById(R.id.close_timer_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeLayout.close();
            }
        });
        swipeLayout.findViewById(R.id.timer_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                c.setTime(timerEntity.tmpNextDueAt);
                timerEntity.nextDueAt = Timer.getNextDueAtFromWeek(c, getDefaultValues(timerEntity)).getTime();
                attemptToDoneTimer(timerEntity);
            }
        });
        swipeLayout.findViewById(R.id.timer_already_done).setOnClickListener(getTimerActionDialogListener(timerEntity, true));
        swipeLayout.findViewById(R.id.timer_later_do).setOnClickListener(getTimerActionDialogListener(timerEntity, false));

        swipeLayout.findViewById(R.id.timer_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(activity, v);
                menu.getMenuInflater().inflate(R.menu.menu_timer_edit, menu.getMenu());
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit_timer:
                                TimerFormActivity.startActivity(activity, item, timerEntity);
                                break;
                            case R.id.end_timer:
                                showEndOrDeleteTimerDialog(timerEntity, true);
                                break;
                            case R.id.delete_timer:
                                showEndOrDeleteTimerDialog(timerEntity, false);
                                break;
                        }
                        return true;
                    }
                });
            }
        });

        parent.addView(swipeLayout);
        return swipeLayout;
    }

    public static View assignTimerText(View v, TimerEntity timer, Activity activity){
        TextView timerName = (TextView)v.findViewById(R.id.timer_name);
        timerName.setText(timer.name);

        TextView noticeInterval = (TextView)v.findViewById(R.id.notice_interval);
        noticeInterval.setText(Timer.getIntervalString(activity, timer));

        TextView limitFrom = (TextView)v.findViewById(R.id.limit_from);
        Date due = (timer.overDueFrom == null ? timer.nextDueAt : timer.overDueFrom);
        limitFrom.setText(Timer.getRemainingTimeString(activity, due));

        TextView noticeAt = (TextView)v.findViewById(R.id.notice_at);
        String noticeString = Timer.getFormatDueStringWithoutYear(timer.nextDueAt);
        if(timer.overDueFrom == null){
            noticeString = noticeString + activity.getString(R.string.postfix_prompt_next_due_at);
        }else{
            noticeString = noticeString + activity.getString(R.string.postfix_prompt_next_due_at_again);
        }
        noticeAt.setText(noticeString);

        RoundCornerProgressBar dueProgress = (RoundCornerProgressBar)v.findViewById(R.id.due_progress);

        int percentage = Timer.getPercentageUntilDueDate(due, timer.latestCalcAt);
        dueProgress.setMax(100);
        dueProgress.setProgress(percentage);
        dueProgress.setProgressColor(Timer.getProgressBarColor(percentage));

        return v;
    }

    private View.OnClickListener getTimerActionDialogListener(final TimerEntity timerEntity, final boolean isDone){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View layout = activity.getLayoutInflater().inflate(R.layout.dialog_timer_already_done_at, null);

                Calendar c = Calendar.getInstance();
                if(!isDone) {
                    c.setTime(timerEntity.nextDueAt);
                }

                if(isDone && timerEntity.overDueFrom == null){
                    Calendar nd = Calendar.getInstance();
                    nd.setTime(timerEntity.nextDueAt);
                    timerEntity.tmpNextDueAt = Timer.getNextDueAtFromMonth(nd, getDefaultValues(timerEntity)).getTime();
                }else{
                    timerEntity.tmpNextDueAt = timerEntity.nextDueAt;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(timerEntity.name);
                builder.setView(layout);
                if(isDone){
                    layout.findViewById(R.id.timer_done_desc).setVisibility(View.VISIBLE);
                }else{
                    layout.findViewById(R.id.timer_do_later_desc).setVisibility(View.VISIBLE);
                    layout.findViewById(R.id.timer_done).setVisibility(View.GONE);
                }

                final TimePicker timePicker = setTimePicker((ViewGroup) layout.findViewById(R.id.time_selecter));
                setTimePickerHourAndMinute(timePicker, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));

                final MaterialCalendarView calendarView = (MaterialCalendarView)layout.findViewById(R.id.calendar);
                setDefaultDecorator(calendarView);
                calendarView.setCurrentDate(c);
                if(isDone) {
                    calendarView.setSelectedDate(c);
                }else{
                    calendarView.setSelectedDate(timerEntity.nextDueAt);
                }

                updateCandidateDate(calendarView, getDefaultValues(timerEntity));
                final TextView doneAt = (TextView) layout.findViewById(R.id.done_time_at);
                Map<Integer, Integer> hourAndMinute = getGetTimePickerHourAndMinute(timePicker);
                c.set(Calendar.HOUR_OF_DAY, hourAndMinute.get(Calendar.HOUR_OF_DAY));
                c.set(Calendar.MINUTE, hourAndMinute.get(Calendar.MINUTE));
                doneAt.setText(Timer.sdf.format(c.getTime()));
                timerEntity.doneAt = c.getTime();
                final TextView timeAt = (TextView) layout.findViewById(R.id.time_at);
                timeAt.setText(Timer.sdf.format(timerEntity.tmpNextDueAt));

                calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
                    @Override
                    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                        updateCandidateDate(widget, getDefaultValues(timerEntity));
                    }
                });

                calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                        Calendar c = Calendar.getInstance();
                        if (isDone) {
                            c.set(Calendar.YEAR, date.getYear());
                            c.set(Calendar.MONTH, date.getMonth());
                            c.set(Calendar.DAY_OF_MONTH, date.getDay());
                            Map<Integer, Integer> hourAndMinute = getGetTimePickerHourAndMinute(timePicker);
                            c.set(Calendar.HOUR_OF_DAY, hourAndMinute.get(Calendar.HOUR_OF_DAY));
                            c.set(Calendar.MINUTE, hourAndMinute.get(Calendar.MINUTE));
                            doneAt.setText(Timer.sdf.format(c.getTime()));
                            timerEntity.doneAt = c.getTime();
                            c.setTime(timerEntity.nextDueAt);
                            if (timerEntity.overDueFrom == null) {
                                c.setTime(Timer.getNextDueAtFromMonth(c, getDefaultValues(timerEntity)).getTime());
                                timerEntity.tmpNextDueAt = c.getTime();
                            }
                            timeAt.setText(Timer.sdf.format(c.getTime()));
                        } else {
                            c.setTime(timerEntity.tmpNextDueAt);
                            c.set(Calendar.YEAR, date.getYear());
                            c.set(Calendar.MONTH, date.getMonth());
                            c.set(Calendar.DAY_OF_MONTH, date.getDay());
                            timerEntity.tmpNextDueAt = c.getTime();
                            timeAt.setText(Timer.sdf.format(timerEntity.tmpNextDueAt));
                        }
                    }
                });

                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        CalendarDay selected = calendarView.getSelectedDate();
                        c.set(Calendar.YEAR, selected.getYear());
                        c.set(Calendar.MONTH, selected.getMonth());
                        c.set(Calendar.DAY_OF_MONTH, selected.getDay());
                        if (isDone) {
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            c.set(Calendar.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[minute]);
                            doneAt.setText(Timer.sdf.format(c.getTime()));
                            timerEntity.doneAt = c.getTime();
                            c.setTime(timerEntity.nextDueAt);
                            if (timerEntity.overDueFrom == null) {
                                c.setTime(Timer.getNextDueAtFromMonth(c, getDefaultValues(timerEntity)).getTime());
                                timerEntity.tmpNextDueAt = c.getTime();
                            }
                            timeAt.setText(Timer.sdf.format(c.getTime()));
                        } else {
                            c.setTime(timerEntity.tmpNextDueAt);
                            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            c.set(Calendar.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[minute]);
                            timerEntity.tmpNextDueAt = c.getTime();
                            timeAt.setText(Timer.sdf.format(timerEntity.tmpNextDueAt));
                        }
                    }
                });

                builder.setPositiveButton(activity.getText(R.string.button_positive), null);
                builder.setNegativeButton(activity.getText(R.string.button_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Timber.d("dialog cancel click");
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface d) {
                        final Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Timber.d("clicked ok");
                                Date d = new Date();
                                if(isDone){
                                    if (d.compareTo(timerEntity.doneAt) < 0) {
                                        Timber.d("time invalid");
                                        layout.findViewById(R.id.done_time_warning).setVisibility(View.VISIBLE);
                                    } else {
                                        attemptToDoneTimer(timerEntity);
                                        dialog.dismiss();
                                    }
                                }else{
                                    if (d.compareTo(timerEntity.tmpNextDueAt) > 0) {
                                        Timber.d("time invalid in do later");
                                        layout.findViewById(R.id.due_time_warning).setVisibility(View.VISIBLE);
                                    } else {
                                        timerEntity.nextDueAt = timerEntity.tmpNextDueAt;
                                        attemptToDoLaterTimer(timerEntity);
                                        dialog.dismiss();
                                    }
                                }

                            }

                        });
                    }
                });

                dialog.show();
            }
        };
    }

    private void showEndOrDeleteTimerDialog(final TimerEntity timerEntity, final Boolean isEnd){
        int layoutId = isEnd ? R.layout.dialog_end_timer : R.layout.dialog_delete_timer;
        int postfixOfTitleId = isEnd ? R.string.postfix_end_of_something : R.string.postfix_delete_of_something;
        View layout = activity.getLayoutInflater().inflate(layoutId, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(timerEntity.name + activity.getString(postfixOfTitleId));
        builder.setView(layout);
        TextView timerName = (TextView)layout.findViewById(R.id.target_timer_name);
        timerName.setText(timerEntity.name);
        builder.setPositiveButton(activity.getString(R.string.button_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isEnd) {
                    attemptToEndTimer(timerEntity);
                } else {
                    attemptToDeleteTimer(timerEntity);
                }
            }
        });
        builder.setNegativeButton(activity.getString(R.string.button_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    public class SundayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();
        private final ForegroundColorSpan span =  new ForegroundColorSpan(Color.RED);

        public SundayDecorator() {
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(span);
        }
    }

    public class SaturdayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();
        private final ForegroundColorSpan span =  new ForegroundColorSpan(Color.BLUE);

        public SaturdayDecorator() {
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SATURDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(span);
        }
    }

    public class CandidateDecorator implements DayViewDecorator {

        private Drawable highlightDrawable;
        private int color = Color.parseColor("#228BC34A");
        private HashSet<CalendarDay> dates;

        public CandidateDecorator(Collection<CalendarDay> dates) {
            this.dates = new HashSet<>(dates);
            highlightDrawable = new ColorDrawable(color);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(highlightDrawable);
        }

        public void setDates(Collection<CalendarDay> dates){
            this.dates = new HashSet<>(dates);
        }
    }


    public static class IntervalTimePicker extends TimePicker {

        private static final int TIME_PICKER_INTERVAL = 15;
        public static final int[] MINUTES_TABLE = {0, 15, 30, 45};

        public IntervalTimePicker(Context context) {
            super(context);
        }

        public static int getMinutePosition(int minute){

            for(int i = 0; i < MINUTES_TABLE.length; i++){
                if(minute == MINUTES_TABLE[i]){
                    return i;
                }
            }

            if(minute > 0 && minute <= 15){
                return 1;
            }else if(minute < 30){
                return 2;
            }else if(minute < 45){
                return 3;
            }else{
                return 0;
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();

            try {
                Class<?> classForid = Class.forName("com.android.internal.R$id");
                Field field = classForid.getField("minute");

                NumberPicker mMinuteSpinner = (NumberPicker) findViewById(field.getInt(null));
                mMinuteSpinner.setMinValue(0);
                mMinuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
                List<String> displayedValues = new ArrayList<String>();
                for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL)
                    displayedValues.add(String.format("%02d", i));
                mMinuteSpinner.setDisplayedValues(displayedValues.toArray(new String[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
