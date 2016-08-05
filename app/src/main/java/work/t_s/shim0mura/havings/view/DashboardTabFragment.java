package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.view.PieChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.AllTimerActivity;
import work.t_s.shim0mura.havings.DoneTaskActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.ProfileEditActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.RegisterActivity;
import work.t_s.shim0mura.havings.TokenAutoCompActivity;
import work.t_s.shim0mura.havings.UserActivity;
import work.t_s.shim0mura.havings.UserSearchActivity;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.CalendarTaskListEvent;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.model.event.TimerListRenderEvent;
import work.t_s.shim0mura.havings.presenter.DoneTaskPresenter;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;

/**
 * Created by shim0mura on 2016/03/30.
 */
public class DashboardTabFragment extends Fragment {

    private static final String BUNDLE_ARG = "BundleArg";
    private static final int MAX_TIMER_SHOWING = 3;

    @Bind(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.total_item_percentage_wrapper) LinearLayout graphDetailWrapper;
    @Bind(R.id.pie_chart) PieChartView pieChartView;
    @Bind(R.id.timers) LinearLayout deadlineNearingTimers;
    @Bind(R.id.view_more_timer) LinearLayout viewMoreTimer;
    @Bind(R.id.calendar) MaterialCalendarView calendarView;
    @Bind(R.id.task_done_header) View taskDoneHeader;
    @Bind(R.id.task_done_date) LinearLayout taskDoneDate;

    @Bind(R.id.chart_wrapper) LinearLayout chartWrapper;
    @Bind(R.id.calendar_wrapper) LinearLayout calendarWrapper;

    private HomePresenter homePresenter;
    private UserPresenter userPresenter;
    private DoneTaskPresenter doneTaskPresenter;
    private ArrayList<ItemPercentageEntity> itemPercentageEntityArrayList;
    private ArrayList<TimerEntity> timerEntities;
    private ArrayList<TaskWrapperEntity> taskWrapperEntities;

    private Calendar calendar;

    public DashboardTabFragment(){}

    public static DashboardTabFragment newInstance() {
        DashboardTabFragment fragment = new DashboardTabFragment();
        Bundle args = new Bundle();
        //args.putString(BUNDLE_ARG, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPresenter = new UserPresenter(getActivity());
        homePresenter = new HomePresenter(getActivity());
        doneTaskPresenter= new DoneTaskPresenter(getActivity());

        calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_dashboard_tab, container, false);
        ButterKnife.bind(this, view);

        if(itemPercentageEntityArrayList == null){
            userPresenter.getItemPercentage();
            homePresenter.getAllTimers();
            doneTaskPresenter.getAllTask();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("swipe end");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer from dashboard fragment");
    }

    @Override
    public void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer from dashboard fragment");
        super.onPause();
    }

    @Subscribe
    public void renderPieChart(ItemPercentageGraphEvent graphEvent){
        itemPercentageEntityArrayList = graphEvent.itemPercentageEntities;

        if(itemPercentageEntityArrayList.isEmpty()){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.partial_nothing_text, null);
            TextView nothing = (TextView)v.findViewById(R.id.nothing);
            nothing.setText(R.string.prompt_no_chart);
            graphDetailWrapper.addView(v);
        }else{
            GraphRenderer.renderPieChart(getActivity(), pieChartView, graphDetailWrapper, itemPercentageEntityArrayList);
        }
    }

    @Subscribe
    public void renderTimers(TimerListRenderEvent timersEvent){

        timerEntities = timersEvent.timerListEntities;
        final Activity activity = getActivity();

        if(timerEntities.isEmpty()){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.partial_nothing_text, null);
            TextView nothing = (TextView)v.findViewById(R.id.nothing);
            nothing.setText(R.string.prompt_no_timers);
            deadlineNearingTimers.addView(v);
            return;
        }else{
            LayoutInflater layoutInflater = LayoutInflater.from(activity);

            for(int i = 0; i < MAX_TIMER_SHOWING; i++){
                TimerEntity timer = timerEntities.get(i);

                View v = layoutInflater.inflate(R.layout.partial_timer_content, null);
                TimerPresenter.assignTimerText(v, timer, activity);

                if(timer.listName != null) {
                    LinearLayout listNameWrapper = (LinearLayout)v.findViewById(R.id.list_name_wrapper);
                    listNameWrapper.setVisibility(View.VISIBLE);
                    TextView listName = (TextView)v.findViewById(R.id.list_name);
                    listName.setText(timer.listName);
                }
                ImageView timerMenu = (ImageView)v.findViewById(R.id.timer_menu);
                timerMenu.setVisibility(View.GONE);

                v.setTag(R.id.TAG_ITEM_ID, timer.listId);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int itemId = (int)v.getTag(R.id.TAG_ITEM_ID);
                        ItemActivity.startActivity(activity, itemId);
                    }
                });

                deadlineNearingTimers.addView(v);
            }
        }


        if(timerEntities.size() <= MAX_TIMER_SHOWING){
            viewMoreTimer.setVisibility(View.GONE);
            /*
            View v = layoutInflater.inflate(R.layout.partial_view_more, null);
            deadlineNearingTimers.addView(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AllTimerActivity.startActivity(activity, timerEntities);
                }
            });
            */
        }
    }

    @Subscribe
    public void renderCalendar(CalendarTaskListEvent taskListEvent) {
        taskWrapperEntities = taskListEvent.taskWrapperEntities;

        doneTaskPresenter.setDefaultDecorator(calendarView, calendar);
        doneTaskPresenter.sortTaskByEventDate(taskWrapperEntities);

        doneTaskPresenter.changeMonthAndTaskDoneDate(calendarView, calendar);

        TextView prompt = (TextView)taskDoneHeader.findViewById(R.id.prompt);
        prompt.setText(getString(R.string.prompt_done_task_result, DoneTaskPresenter.getDate(calendar.getTime()), 0));

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                calendar.set(Calendar.YEAR, date.getYear());
                calendar.set(Calendar.MONTH, date.getMonth());
                doneTaskPresenter.changeMonthAndTaskDoneDate(calendarView, calendar);
            }
        });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                showDoneDate(date);
            }
        });
    }

    private void showDoneDate(CalendarDay date){

        Calendar cc = date.getCalendar();
        Map<Date, Integer> map = doneTaskPresenter.getDoneDate(cc);
        int taskCount = (map == null ? 0 : map.size());

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        final TextView prompt = (TextView)taskDoneHeader.findViewById(R.id.prompt);
        prompt.setText(getString(R.string.prompt_done_task_result, DoneTaskPresenter.getDate(cc.getTime()), taskCount));

        if(map == null){
            return;
        }

        taskDoneDate.removeAllViews();

        Map<Integer, TimerEntity> timerMap = doneTaskPresenter.timerEntityMap;
        for(Map.Entry<Date, Integer> m : map.entrySet()){
            final TimerEntity timer = timerMap.get(m.getValue());
            View v = layoutInflater.inflate(R.layout.partial_task_done_date, null);

            TextView timerName = (TextView)v.findViewById(R.id.task_name);
            timerName.setText(timer.name);
            TextView listName = (TextView)v.findViewById(R.id.list_name);
            listName.setText(doneTaskPresenter.listEntityMap.get(timer.listId).name);
            TextView notification = (TextView)v.findViewById(R.id.notification_interval);
            notification.setText(Timer.getIntervalString(getActivity(), timer));
            TextView doneDate = (TextView)v.findViewById(R.id.done_date);
            doneDate.setText(Timer.getFormatDueString(m.getKey()));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemActivity.startActivity(getActivity(), timer.listId);
                }
            });
            taskDoneDate.addView(v);
        }
    }

    @Subscribe
    public void renderPieChartDetail(GenericEvent genericEvent){
        ItemPercentageEntity categoryParent = itemPercentageEntityArrayList.get(genericEvent.result);

        if(categoryParent == null){
            return;
        }

        GraphRenderer.renderPieChartDetail(getActivity(), graphDetailWrapper, categoryParent);
    }

    @OnClick(R.id.view_more_timer)
    public void toAllTimer(View v){
        AllTimerActivity.startActivity(getActivity(), timerEntities);
    }

    @OnClick(R.id.logout)
    public void logout(View v){
        Log.d("ssss", "logout");
        ApiServiceManager asm = ApiServiceManager.getSingleton(getActivity());
        asm.clearApiKey();
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.item)
    public void navigateToItem(View v){
        ItemActivity.startActivity(getActivity(), 61);

        //UserActivity.startActivity(this, 10);

        //Intent intent = new Intent(this, UserActivity.class);
        //intent.putExtra("itemId", 2);
        //startActivity(intent);
    }

    @OnClick(R.id.user_10)
    public void navigateToUser(View v){
        UserActivity.startActivity(getActivity(), 10);
    }

    @OnClick(R.id.user_6)
    public void navigateToUser6(View v){
        UserActivity.startActivity(getActivity(), 6);
    }

    @OnClick(R.id.edit_profile)
    public void navigateToEditProfile(View v){
        ProfileEditActivity.startActivity(getActivity(), 10);
    }

    @OnClick(R.id.user_search)
    public void navigateToUserSearch(View v){
        UserSearchActivity.startActivity(getActivity());
    }

    @OnClick(R.id.done_task)
    public void navigateToDoneTask(View v){
        DoneTaskActivity.startActivity(getActivity(), 2, "ssss");
    }

    @OnClick(R.id.tag_comp_test)
    public void navigateToTagTest(View v){
        Intent intent = new Intent(getActivity(), TokenAutoCompActivity.class);
        startActivity(intent);
    }

}
