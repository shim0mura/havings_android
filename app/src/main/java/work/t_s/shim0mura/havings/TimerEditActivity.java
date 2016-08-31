package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.CalendarTaskListEvent;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.util.CustomTimePickerDialog;

public class TimerEditActivity extends AppCompatActivity {

    public static final int TYPE_DONE = 0;
    public static final int TYPE_LATER = 1;

    private static final String SERIALIZED_TIMER = "SerializedTimer";
    private static final String SERIALIZED_TYPE = "SerializedType";
    private TimerEntity timerEntity;
    private int type;
    private TimerPresenter timerPresenter;

    private Calendar tmpCalendar;
    private Calendar current;
    private ProgressDialog progressDialog;

    @Bind(R.id.title_text) TextView titleText;
    @Bind(R.id.prompt_target_date) TextView promptTargetDate;
    @Bind(R.id.target_date) TextView targetDate;
    @Bind(R.id.prompt_target_time) TextView promptTargetTime;
    @Bind(R.id.target_time) TextView targetTime;
    @Bind(R.id.timer_done) RelativeLayout timerDoneWrapper;
    @Bind(R.id.done_time_warning) LinearLayout timerDoneWarning;
    @Bind(R.id.due_time_warning) LinearLayout dueTimeWarning;
    @Bind(R.id.next_of_next_due) RelativeLayout nextOfNextWrapper;
    @Bind(R.id.schedule) ImageView targetDueIcon;
    @Bind(R.id.next_icon) ImageView nextIcon;

    @Bind(R.id.done_time_at) TextView doneTimeAt;
    @Bind(R.id.next_due_at) TextView nextDueAt;
    @Bind(R.id.next_of_next_due_at) TextView nextOfNextDueAt;

    public static void startActivity(Context context, TimerEntity timer, int type){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_TIMER, timer);
        intent.putExtra(SERIALIZED_TYPE, type);

        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.TIMER_UPDATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        timerEntity = (TimerEntity)extras.getSerializable(SERIALIZED_TIMER);
        type = (int)extras.getSerializable(SERIALIZED_TYPE);

        setTitle(getString(R.string.prompt_edit_timer));
        ButterKnife.bind(this);

        timerPresenter = new TimerPresenter(this, new ItemEntity(), timerEntity);

        tmpCalendar = new GregorianCalendar();
        current = new GregorianCalendar();

        timerEntity.tmpNextDueAt = timerEntity.nextDueAt;

        if(type == TYPE_DONE){
            titleText.setText(R.string.prompt_timer_done_at_detail);
            promptTargetDate.setText(R.string.prompt_timer_done_date_detail);
            promptTargetTime.setText(R.string.prompt_timer_done_time_detail);
            nextOfNextWrapper.setVisibility(View.GONE);
            Calendar nextBase = new GregorianCalendar();
            nextBase.setTime(timerEntity.tmpNextDueAt);
            Calendar nextOfNext;
            if(timerEntity.repeatBy == Timer.REPEAT_TYPE_BY_DAY){
                nextOfNext = Timer.getNextDueAtFromMonth(nextBase, timerPresenter.getDefaultValues(timerEntity));
            }else{
                nextOfNext = Timer.getNextDueAtFromWeek(nextBase, timerPresenter.getDefaultValues(timerEntity));
            }
            timerEntity.tmpNextDueAt = nextOfNext.getTime();
            timerEntity.doneAt = current.getTime();
        }else{
            targetDueIcon.setImageResource(R.drawable.ic_redo_black_24dp);
            nextIcon.setImageResource(R.drawable.ic_redo_black_24dp);
            titleText.setText(R.string.prompt_timer_later_at_detail);
            promptTargetDate.setText(R.string.prompt_timer_later_date_detail);
            promptTargetTime.setText(R.string.prompt_timer_later_time_detail);
            timerDoneWrapper.setVisibility(View.GONE);
        }

        final Activity act = this;

        targetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        GregorianCalendar d = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                        d.setTime(timerEntity.tmpNextDueAt);

                        tmpCalendar.setTime(timerEntity.tmpNextDueAt);
                        tmpCalendar.set(Calendar.YEAR, year);
                        tmpCalendar.set(Calendar.MONTH, monthOfYear);
                        tmpCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        if(type == TYPE_LATER){
                            timerEntity.tmpNextDueAt = tmpCalendar.getTime();
                            setNextDueAtText();
                            setNextOfNextDueAt();
                        }else{
                            timerEntity.doneAt = tmpCalendar.getTime();
                            setDoneTimeAtText();
                        }
                        setTargetDateText();
                    }
                };
                Calendar cal = new GregorianCalendar();
                if(type == TYPE_LATER){
                    cal.setTime(timerEntity.tmpNextDueAt);
                }
                DatePickerDialog datePicker = new DatePickerDialog(act, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                if(type == TYPE_LATER){
                    datePicker.getDatePicker().setMinDate(current.getTimeInMillis());
                }else{
                    datePicker.getDatePicker().setMaxDate(current.getTimeInMillis());
                }
                datePicker.show();

            }
        });

        targetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener listner = new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tmpCalendar.setTime(timerEntity.tmpNextDueAt);
                        tmpCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        tmpCalendar.set(Calendar.MINUTE, TimerPresenter.IntervalTimePicker.MINUTES_TABLE[minute/15]);
                        if(type == TYPE_LATER){
                            timerEntity.tmpNextDueAt = tmpCalendar.getTime();
                            setNextDueAtText();
                            setNextOfNextDueAt();
                        }else{
                            setDoneTimeAtText();
                        }
                        setTargetTimeText();
                    }
                };
                Calendar cal = new GregorianCalendar();
                cal.setTime(timerEntity.tmpNextDueAt);
                CustomTimePickerDialog timePicker = new CustomTimePickerDialog(act, listner, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                timePicker.show();
            }
        });

        if(type == TYPE_DONE){
            setDoneTimeAtText();
        }
        setTargetDateText();
        setTargetTimeText();
        setNextDueAtText();
        setNextOfNextDueAt();

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

    @OnClick(R.id.post_timer)
    public void postTimer(){
        Date d = new Date();

        if(type == TYPE_DONE){
            if (d.compareTo(tmpCalendar.getTime()) < 0) {
                timerDoneWarning.setVisibility(View.VISIBLE);
            } else {
                progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);
                timerEntity.nextDueAt = timerEntity.tmpNextDueAt;
                timerEntity.doneAt = tmpCalendar.getTime();
                timerPresenter.attemptToDoneTimer(timerEntity);
            }
        }else{
            if (d.compareTo(timerEntity.tmpNextDueAt) > 0) {
                dueTimeWarning.setVisibility(View.VISIBLE);
            } else {
                progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);

                timerEntity.nextDueAt = timerEntity.tmpNextDueAt;
                timerPresenter.attemptToDoLaterTimer(timerEntity);
            }
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

    private void setTargetDateText(){
        targetDate.setText(DateFormat.format("yyyy年MM月dd日(E)", tmpCalendar.getTime()).toString());
    }

    private void setTargetTimeText(){
        targetTime.setText(DateFormat.format("kk時mm分", tmpCalendar.getTime()).toString());
    }

    private void setNextDueAtText(){
        nextDueAt.setText(Timer.getFormatDueString(timerEntity.tmpNextDueAt));
    }

    private void setDoneTimeAtText(){
        doneTimeAt.setText(Timer.getFormatDueString(timerEntity.doneAt));
    }

    private void setNextOfNextDueAt(){
        Calendar nextOfNext;
        if(timerEntity.repeatBy == Timer.REPEAT_TYPE_BY_DAY){
            nextOfNext = Timer.getNextDueAtFromMonth(tmpCalendar, timerPresenter.getDefaultValues(timerEntity));
            nextOfNextDueAt.setText(Timer.getFormatDueString(nextOfNext.getTime()));

        }else if (timerEntity.repeatBy == Timer.REPEAT_TYPE_BY_WEEK) {
            nextOfNext = Timer.getNextDueAtFromWeek(tmpCalendar, timerPresenter.getDefaultValues(timerEntity));
            nextOfNextDueAt.setText(Timer.getFormatDueString(nextOfNext.getTime()));

        }else{
            nextOfNextDueAt.setText(R.string.prompt_nothing);

        }
    }
}
