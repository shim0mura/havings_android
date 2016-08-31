package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.util.CustomTimePickerDialog;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class TimerFormActivity extends AppCompatActivity {

    protected static final String SERIALIZED_ITEM = "SerializedItem";
    protected static final String SERIALIZED_TIMER = "SerializedTimer";

    private TimerPresenter timerPresenter;
    private ItemEntity item;
    private TimerEntity timer;
    //private TimePicker timePicker;
    private Calendar tmpCalendar = Calendar.getInstance();
    private Calendar current = new GregorianCalendar();
    private Map<String, Integer> tmpValueMap;

    private ProgressDialog progressDialog;

    private TreeMap<Integer, String> weekSelection =  new TreeMap<Integer, String>();

    @Bind(R.id.timer_name) TextView timerName;
    @Bind(R.id.calendar) MaterialCalendarView calendarView;
    @Bind(R.id.is_repeating) CheckBox isRepeatingOrNot;
    //@Bind(R.id.time_selecter) LinearLayout timeSelecter;
    @Bind(R.id.next_due_at) TextView nextDueAt;
    @Bind(R.id.notice_date) TextView noticeDate;
    @Bind(R.id.notice_time) TextView noticeTime;

    @Bind(R.id.repeat_interval_selecter) LinearLayout repeatIntervalSelecter;
    @Bind(R.id.repeat_by_day) RadioButton repeatByDay;
    @Bind(R.id.repeat_by_week) RadioButton repeatByWeek;
    @Bind(R.id.repeat_by_day_selecter) LinearLayout repeatByDaySelecter;
    @Bind(R.id.repeat_by_week_selecter) LinearLayout repeatByWeekSelecter;

    @Bind(R.id.select_month_interval) Spinner selectMonthInterval;
    //@Bind(R.id.select_repeat_day) Spinner selectRepeatDay;
    @Bind(R.id.repeat_day) TextView repeatDay;
    @Bind(R.id.select_week_interval) Spinner selectWeekInterval;
    //@Bind(R.id.select_repeat_day_of_week) Spinner selectRepeatDayOfWeek;
    @Bind(R.id.repeat_day_of_week) TextView repeatDayOfWeek;

    @Bind(R.id.next_due_at_by_calendar) TextView nextDueAtByCalendar;
    @Bind(R.id.next_of_next_due_at_by_calendar) TextView nextOfNextDueAtByCalendar;

    @Bind(R.id.due_time_warning) LinearLayout dueTimeWarning;
    @Bind(R.id.due_time_warning_below) LinearLayout dueTimeWarningBelow;
    @Bind(R.id.post_timer) Button postTimerButton;

    public static void startActivity(Context context, ItemEntity i, TimerEntity t) {
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        int resultCode = ItemActivity.TIMER_UPDATED_RESULTCODE;
        if(t.id == 0) {
            t.listId = i.id;
            resultCode = ItemActivity.TIMER_CREATED_RESULTCODE;
        }
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(SERIALIZED_TIMER, t);
        Activity a = (Activity) context;

        a.startActivityForResult(intent, resultCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);
        timer = (TimerEntity)extras.getSerializable(SERIALIZED_TIMER);
        Timber.d("timer time %s", timer.nextDueAt.toString());
        timerPresenter = new TimerPresenter(this, item, timer);

        ButterKnife.bind(this);

        timerPresenter.setDefaultDecorator(calendarView);

        setDefaultProperties();

        //timePicker = timerPresenter.setTimePicker(timeSelecter);
        //timerPresenter.setTimePickerHourAndMinute(timePicker, timer.noticeHour, timer.noticeMinute);

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                timerPresenter.updateCandidateDate(widget, timer.tmpNextDueAt, getIntervalValues());
            }
        });

        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);


        updateCandidate(tmpCalendar, tmpValueMap);

        setNextDueAt();
        setSpinnerChangeListener();

        final Activity act = this;

        noticeDate.setText(DateFormat.format("yyyy年MM月dd日(E)", timer.tmpNextDueAt).toString());

        noticeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar d = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        d.setTime(timer.tmpNextDueAt);

                        tmpCalendar.setTime(timer.tmpNextDueAt);
                        tmpCalendar.set(Calendar.YEAR, year);
                        tmpCalendar.set(Calendar.MONTH, monthOfYear);
                        tmpCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        timer.tmpNextDueAt = tmpCalendar.getTime();
                        updateRepeatDaysText(dayOfMonth, tmpCalendar.get(Calendar.DAY_OF_WEEK));
                        setNextDueAt();
                        updateCandidate(tmpCalendar, getIntervalValues());
                        updateWeekIntervalSpinner();
                        noticeDate.setText(DateFormat.format("yyyy年MM月dd日(E)", timer.tmpNextDueAt).toString());
                    }
                };
                Calendar cal = new GregorianCalendar();
                cal.setTime(timer.tmpNextDueAt);
                DatePickerDialog datePicker = new DatePickerDialog(act, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMinDate(current.getTimeInMillis());
                datePicker.show();

            }
        });

        noticeTime.setText(DateFormat.format("kk時mm分", timer.tmpNextDueAt).toString());

        noticeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener listner = new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tmpCalendar.setTime(timer.tmpNextDueAt);
                        tmpCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        tmpCalendar.set(Calendar.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[minute/15]);
                        timer.tmpNextDueAt = tmpCalendar.getTime();
                        noticeTime.setText(DateFormat.format("kk時mm分", timer.tmpNextDueAt).toString());
                        setNextDueAt();
                    }
                };
                Calendar cal = new GregorianCalendar();
                cal.setTime(timer.tmpNextDueAt);
                CustomTimePickerDialog timePicker = new CustomTimePickerDialog(act, listner, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                timePicker.show();
            }
        });

        setTitle(getString(R.string.prompt_update_timer));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer");
        super.onPause();
    }

    @OnClick(R.id.is_repeating)
    public void checkIsRepeatingOrNot(View v){
        if(isRepeatingOrNot.isChecked()){
            showRepeatIntevalSelecter();
            calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        }else{
            hideRepeatIntevalSelecter();
            calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        }
        //calendarView.setSelectedDate(tmpCalendar);
        updateCandidate(tmpCalendar, getIntervalValues());

    }

    @OnClick({R.id.repeat_by_day, R.id.repeat_by_week})
    public void selectRepeatType(View v){
        toggleRepeatTypeSelecter();
        tmpCalendar.setTime(timer.tmpNextDueAt);
        updateRepeatDaysText(tmpCalendar.get(Calendar.DAY_OF_MONTH), tmpCalendar.get(Calendar.DAY_OF_WEEK));
        updateCandidate(tmpCalendar, getIntervalValues());
    }

    @OnClick(R.id.clear_timer_properties)
    public void clearProperties(){
        Timber.d(tmpValueMap.toString());
        if(timer.isRepeating){
            showRepeatIntevalSelecter();
            if(tmpValueMap.get(Timer.REPEAT_BY) != 0){
                repeatByWeek.setChecked(true);
            }else{
                repeatByDay.setChecked(true);
            }
            toggleRepeatTypeSelecter();
        }else{
            hideRepeatIntevalSelecter();
            isRepeatingOrNot.setChecked(false);
        }

        //timerPresenter.updateCandidateDate(calendarView, timer.tmpNextDueAt, tmpValueMap);
        //timerPresenter.setTimePickerHourAndMinute(timePicker, tmpValueMap.get(Timer.HOUR), tmpValueMap.get(Timer.MINUTE));

        //setOnItemSelectedListenerがセットされていると
        //setselectionの時点でOnItemSelectedが発火してしまうので、一旦listenerを外してセットし直す
        selectMonthInterval.setOnItemSelectedListener(null);
        //selectRepeatDay.setOnItemSelectedListener(null);
        selectWeekInterval.setOnItemSelectedListener(null);
        //selectRepeatDayOfWeek.setOnItemSelectedListener(null);
        noticeDate.setText(DateFormat.format("yyyy年MM月dd日(E)", timer.tmpNextDueAt).toString());
        noticeTime.setText(DateFormat.format("kk時mm分", timer.nextDueAt).toString());

        timer.tmpNextDueAt = timer.nextDueAt;
        tmpCalendar.setTime(timer.nextDueAt);
        calendarView.setCurrentDate(tmpCalendar);
        updateCandidate(tmpCalendar, tmpValueMap);
        updateWeekIntervalSpinner();
        setSpinnerSelection(tmpValueMap);
        setSpinnerChangeListener();
        updateRepeatDaysText(tmpCalendar.get(Calendar.DAY_OF_WEEK), tmpCalendar.get(Calendar.DAY_OF_WEEK));

        setNextDueAt();
    }

    @OnClick(R.id.post_timer)
    public void postTimer(){
        clearWarning();
        constructTimer();

        progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);

        if(timer.id == 0) {
            timerPresenter.attemptToCreateTimer(timer);
        }else{
            timerPresenter.attemptToUpdateTimer(timer);
        }
    }

    @Subscribe
    public void successToPost(TimerEntity timerEntity){
        if(progressDialog != null){
            progressDialog.dismiss();
        }

        Intent data = getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(ItemActivity.POSTED_TIMER, timerEntity);
        data.putExtras(extras);
        setResult(Activity.RESULT_OK, data);

        finish();
    }

    @Subscribe
    public void setValidateError(SetErrorEvent event){
        switch(event.resourceId){
            case R.id.timer_name:
                timerName.setError(getResources().getString(R.string.error_field_required));
                timerName.requestFocus();
                break;
            case R.id.time_selecter:
                dueTimeWarning.setVisibility(View.VISIBLE);
                dueTimeWarningBelow.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Subscribe
    public void subscribeAlert(AlertEvent event) {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setDefaultProperties(){
        timerName.setText(timer.name);
        timer.tmpNextDueAt = timer.nextDueAt;

        // 現在時刻よりも進んでいた場合はそこを起点としてnext_due_atを計算する
        // ex)今日が1/15で毎月20日通知で次回通知が1/20で1/18にdoneしていた場合
        //    updateで毎月17日通知を選んでしまうと1/17が次の候補日になってしまう
        //    この場合は2/17を候補日にしたいので現在時刻でなくdone_atを起点にする
        Timber.d(timer.latestCalcAt.toString());
        Timber.d(tmpCalendar.getTime().toString());


        if(timer.latestCalcAt.compareTo(tmpCalendar.getTime()) < 0) {
            Timber.d(timer.latestCalcAt.toString());
            timer.latestCalcAt = tmpCalendar.getTime();
        }

        tmpCalendar.setTime(timer.tmpNextDueAt);

        tmpValueMap = timerPresenter.getDefaultValues(timer);
        setSpinner();
        setSpinnerSelection(tmpValueMap);
        updateRepeatDaysText(tmpCalendar.get(Calendar.DAY_OF_MONTH), tmpCalendar.get(Calendar.DAY_OF_WEEK));

        if(timer.repeatBy == 0){
            repeatByDay.setChecked(true);
        }else{
            repeatByWeek.setChecked(true);
        }

        if(timer.isRepeating){
            isRepeatingOrNot.setChecked(true);
            showRepeatIntevalSelecter();
            toggleRepeatTypeSelecter();
        }else{
            hideRepeatIntevalSelecter();
            toggleRepeatTypeSelecter();
        }

        if(timer.id != 0){
           postTimerButton.setText(getText(R.string.prompt_update_timer));
        }

        calendarView.setCurrentDate(tmpCalendar);
        //calendarView.setSelectedDate(tmpCalendar);

    }

    private void constructTimer(){
        timer.name = timerName.getText().toString();
        timer.nextDueAt = timer.tmpNextDueAt;
        timer.isRepeating = isRepeatingOrNot.isChecked();

        Map<String, Integer> valueMap = getIntervalValues();
        timer.repeatBy = valueMap.get(Timer.REPEAT_BY);
        timer.repeatMonthInterval = valueMap.get(Timer.MONTH_INTERVAL);
        timer.repeatDayOfMonth = valueMap.get(Timer.DAY_OF_MONTH);
        timer.repeatWeek = valueMap.get(Timer.WEEK_NUMBER);
        timer.repeatDayOfWeek = valueMap.get(Timer.DAY_OF_WEEK) - 1;
        if(timer.repeatDayOfWeek < 0){
            timer.repeatDayOfWeek = 0;
        }
        timer.noticeHour = valueMap.get(Timer.HOUR);
        timer.noticeMinute = valueMap.get(Timer.MINUTE);
    }

    public void clearWarning(){
        dueTimeWarning.setVisibility(View.GONE);
        dueTimeWarningBelow.setVisibility(View.GONE);
    }

    public void showRepeatIntevalSelecter(){
        repeatIntervalSelecter.setVisibility(View.VISIBLE);
    }

    public void hideRepeatIntevalSelecter(){
        repeatIntervalSelecter.setVisibility(View.GONE);
    }

    public void toggleRepeatTypeSelecter(){
        if(repeatByDay.isChecked()){
            Timber.d("checked repeat by day");
            repeatByDaySelecter.setVisibility(View.VISIBLE);
            repeatByWeekSelecter.setVisibility(View.GONE);
        }else{
            Timber.d("checked repeat by week");
            repeatByDaySelecter.setVisibility(View.GONE);
            repeatByWeekSelecter.setVisibility(View.VISIBLE);
        }
    }

    public void setSpinnerSelection(Map<String, Integer> value){
        Integer[] monthIntervals = Timer.repeatByDayMonthInterval.keySet().toArray(new Integer[Timer.repeatByDayMonthInterval.size()]);
        int monthIntervalPosition = Arrays.asList(monthIntervals).indexOf(value.get(Timer.MONTH_INTERVAL));
        selectMonthInterval.setSelection(monthIntervalPosition, false);

        //selectRepeatDay.setSelection(value.get(Timer.DAY_OF_MONTH) - 1, false);

        Integer[] weekNumbers = weekSelection.keySet().toArray(new Integer[weekSelection.size()]);
        int weekNumberPosition = Arrays.asList(weekNumbers).indexOf(value.get(Timer.WEEK_NUMBER));
        selectWeekInterval.setSelection(weekNumberPosition, false);

        /*
        Integer[] repeatDayOfWeeks = Timer.repeatByDayOfWeek.keySet().toArray(new Integer[Timer.repeatByDayOfWeek.size()]);
        int repeatDayOfWeekPosition = Arrays.asList(repeatDayOfWeeks).indexOf(value.get(Timer.DAY_OF_WEEK));
        selectRepeatDayOfWeek.setSelection(repeatDayOfWeekPosition, false);
        */
    }

    public void updateRepeatDaysText(int day, int dayOfWeek){
        repeatDay.setText(getString(R.string.prompt_next_due_at_schedule_by_day, String.valueOf(day)));

        String str = Timer.repeatByDayOfWeek.get(dayOfWeek);
        int promptId = getResources().getIdentifier(str, "string", getPackageName());
        String dayOfW = getString(promptId);
        repeatDayOfWeek.setText(getString(R.string.prompt_next_due_at_schedule_by_week, dayOfW));
    }

    public void setSpinner(){

        ArrayAdapter<Timer.RepeatInterval> monthIntervalAdapter = new ArrayAdapter<Timer.RepeatInterval>(this, android.R.layout.simple_spinner_item, Timer.getRepeatIntervalObj(this, Timer.repeatByDayMonthInterval));
        monthIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthInterval.setAdapter(monthIntervalAdapter);

        updateWeekIntervalSpinner();
    }

    private void updateWeekIntervalSpinner(){
        weekSelection =  new TreeMap<Integer, String>();
        weekSelection.put(0, Timer.repeatByWeekInterval.get(0));
        int weeknum = tmpCalendar.get(Calendar.WEEK_OF_MONTH);
        weekSelection.put(weeknum, Timer.repeatByWeekInterval.get(weeknum));
        if(weeknum == 4){
            Calendar lastDayOfMonth = new GregorianCalendar();
            lastDayOfMonth.set(Calendar.YEAR, tmpCalendar.get(Calendar.YEAR));
            lastDayOfMonth.set(Calendar.MONTH, tmpCalendar.get(Calendar.MONTH));
            lastDayOfMonth.set(Calendar.DAY_OF_MONTH, tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            int w = lastDayOfMonth.get(Calendar.WEEK_OF_MONTH);
            if(w == weeknum){
                weekSelection.put(5, Timer.repeatByWeekInterval.get(5));
            }
        }

        ArrayAdapter<Timer.RepeatInterval> weekIntervalAdapter = new ArrayAdapter<Timer.RepeatInterval>(this, android.R.layout.simple_spinner_item, Timer.getRepeatIntervalObj(this, weekSelection));
        weekIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectWeekInterval.setAdapter(weekIntervalAdapter);
    }

    public void setNextDueAt(){
        nextDueAt.setText(Timer.getFormatDueString(timer.tmpNextDueAt));
        //nextDueAt.setText(Timer.sdf.format(timer.tmpNextDueAt));
    }

    public void setSpinnerChangeListener(){
        selectMonthInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Integer> value = getIntervalValues();

                Calendar calendar = new GregorianCalendar();
                calendar.setTime(timer.latestCalcAt);

                Timber.d("copy date: %s / %s / %s , %s : %s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

                Calendar nextDueAt = Timer.getNextDueAtFromMonth(calendar, value);
                updateCandidate(nextDueAt, value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        selectWeekInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Integer> value = getIntervalValues();

                Calendar calendar = new GregorianCalendar();
                calendar.setTime(timer.latestCalcAt);

                Timber.d("copy date: %s / %s / %s , %s : %s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                Calendar nextDueAt = Timer.getNextDueAtFromWeek(calendar, value);
                updateCandidate(nextDueAt, value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void updateCandidate(Calendar nextDueAt, Map<String, Integer> value){
        //calendarView.setCurrentDate(nextDueAt);
        timerPresenter.updateCandidateDate(calendarView, timer.tmpNextDueAt, value);
        nextDueAtByCalendar.setText(Timer.getFormatDueString(timer.tmpNextDueAt));
        Date nextOfNext;
        if(value.get(Timer.REPEAT_BY) == Timer.REPEAT_TYPE_BY_DAY){
            nextOfNext = Timer.getNextDueAtFromMonth(tmpCalendar, value).getTime();
        }else{
            nextOfNext = Timer.getNextDueAtFromWeek(tmpCalendar, value).getTime();
        }
        nextOfNextDueAtByCalendar.setText(Timer.getFormatDueString(nextOfNext));
        /*
        timerPresenter.updateCandidateDate(calendarView, value);
        //calendarView.setSelectedDate(nextDueAt);
        //calendarView.setCurrentDate(nextDueAt);
        timer.tmpNextDueAt = nextDueAt.getTime();
        setNextDueAt();
        */
    }

    public Map<String, Integer> getIntervalValues(){
        Map<String, Integer> valueMap = new HashMap<>();
        Calendar cal = new GregorianCalendar();
        cal.setTime(timer.tmpNextDueAt);

        valueMap.put(Timer.IS_REPEATING, (isRepeatingOrNot.isChecked() ? 1 : 0));
        if(repeatByDay.isChecked()){
            valueMap.put(Timer.REPEAT_BY, 0);
        }else{
            valueMap.put(Timer.REPEAT_BY, 1);
        }
        Timer.RepeatInterval monthInterval = (Timer.RepeatInterval)selectMonthInterval.getSelectedItem();
        valueMap.put(Timer.MONTH_INTERVAL, monthInterval.getTypeId());
        //valueMap.put(Timer.DAY_OF_MONTH, (int) selectRepeatDay.getSelectedItem());
        valueMap.put(Timer.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));

        Timer.RepeatInterval weekNumber = (Timer.RepeatInterval)selectWeekInterval.getSelectedItem();
        valueMap.put(Timer.WEEK_NUMBER, weekNumber.getTypeId());
        //Timer.RepeatInterval dayOfWeek = (Timer.RepeatInterval)selectRepeatDayOfWeek.getSelectedItem();
        //valueMap.put(Timer.DAY_OF_WEEK, dayOfWeek.getTypeId());
        valueMap.put(Timer.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK));

        valueMap.put(Timer.HOUR, cal.get(Calendar.HOUR_OF_DAY));
        valueMap.put(Timer.MINUTE, cal.get(Calendar.MINUTE));
        /*
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            valueMap.put(Timer.HOUR, timePicker.getHour());
            valueMap.put(Timer.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[timePicker.getMinute()]);
        } else {
            valueMap.put(Timer.HOUR, timePicker.getCurrentHour());
            valueMap.put(Timer.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[timePicker.getCurrentMinute()]);
        }
        */


        Timber.d(valueMap.toString());

        return valueMap;
    }

}