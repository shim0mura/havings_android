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
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.AllTimerActivity;
import work.t_s.shim0mura.havings.DetailGraphActivity;
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
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.CalendarTaskListEvent;
import work.t_s.shim0mura.havings.model.event.CountGraphEvent;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.model.event.TimerListRenderEvent;
import work.t_s.shim0mura.havings.presenter.DoneTaskPresenter;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/03/30.
 */
public class DashboardTabFragment extends Fragment {

    private static final String BUNDLE_ARG = "BundleArg";
    private static final int MAX_TIMER_SHOWING = 3;

    private static final String COUNT_GRAPH_EVENT = "CountGraphEvent";
    private static final String ITEM_PERCENTAGE_EVENT = "ItemPercentageEvent";
    private static final String TIMER_LIST_RENDER_EVENT = "TimerListRenderEvent";
    private static final String CALENDAR_TASK_LIST_EVENT = "CalendarTaskListEvent";

    @Bind(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.total_item_percentage_wrapper) LinearLayout graphDetailWrapper;
    @Bind(R.id.pie_chart) PieChartView pieChartView;
    @Bind(R.id.timers) LinearLayout deadlineNearingTimers;
    @Bind(R.id.view_more_timer) LinearLayout viewMoreTimer;
    @Bind(R.id.calendar) MaterialCalendarView calendarView;
    @Bind(R.id.task_done_header) View taskDoneHeader;
    @Bind(R.id.task_done_date) LinearLayout taskDoneDate;
    @Bind(R.id.item_graph_date_from) TextView graphDateFrom;
    @Bind(R.id.item_graph_date_to) TextView graphDateTo;
    @Bind(R.id.no_graph) TextView noGraph;
    @Bind(R.id.graph_date_range) LinearLayout graphDateWrapper;
    @Bind(R.id.to_detail_graph) LinearLayout detailCountWrapper;

    @Bind(R.id.chart_wrapper) LinearLayout chartWrapper;
    @Bind(R.id.calendar_wrapper) LinearLayout calendarWrapper;

    private HomePresenter homePresenter;
    private UserPresenter userPresenter;
    private DoneTaskPresenter doneTaskPresenter;
    private CountGraphEvent countGraphEvent;
    private ItemPercentageGraphEvent itemPercentageGraphEvent;
    private TimerListRenderEvent timerListRenderEvent;
    private CalendarTaskListEvent calendarTaskListEvent;
    private ArrayList<ItemPercentageEntity> itemPercentageEntityArrayList;
    private ArrayList<TimerEntity> timerEntities;
    private ArrayList<TaskWrapperEntity> taskWrapperEntities;

    private Calendar calendar;
    private boolean swipeRefreshLayoutShown = false;

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
        super.onCreateView(inflater, container, savedInstanceState);

        View view =  inflater.inflate(R.layout.fragment_dashboard_tab, container, false);
        ButterKnife.bind(this, view);

        if(savedInstanceState == null){
            userPresenter.getItemPercentage();
            homePresenter.getAllTimers();
            doneTaskPresenter.getAllTask();
            homePresenter.getCountData();
        }else{
            countGraphEvent = (CountGraphEvent) savedInstanceState.getSerializable(COUNT_GRAPH_EVENT);
            renderCountGraph(countGraphEvent);
            itemPercentageGraphEvent = (ItemPercentageGraphEvent) savedInstanceState.getSerializable(ITEM_PERCENTAGE_EVENT);
            renderPieChart(itemPercentageGraphEvent);
            timerListRenderEvent = (TimerListRenderEvent) savedInstanceState.getSerializable(TIMER_LIST_RENDER_EVENT);
            renderTimers(timerListRenderEvent);
            calendarTaskListEvent = (CalendarTaskListEvent) savedInstanceState.getSerializable(CALENDAR_TASK_LIST_EVENT);
            renderCalendar(calendarTaskListEvent);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayoutShown = true;
                userPresenter.getItemPercentage();
                homePresenter.getAllTimers();
                doneTaskPresenter.getAllTask();

            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(COUNT_GRAPH_EVENT, countGraphEvent);
        outState.putSerializable(ITEM_PERCENTAGE_EVENT, itemPercentageGraphEvent);
        outState.putSerializable(TIMER_LIST_RENDER_EVENT, timerListRenderEvent);
        outState.putSerializable(CALENDAR_TASK_LIST_EVENT, calendarTaskListEvent);
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

    private void resetSwipe(){
        if(swipeRefreshLayoutShown){
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayoutShown = false;
        }
    }

    @Subscribe
    public void getPieChartData(ItemPercentageGraphEvent graphEvent){
        itemPercentageGraphEvent = graphEvent;
        renderPieChart(graphEvent);
        resetSwipe();
    }

    private void renderPieChart(ItemPercentageGraphEvent graphEvent){
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
    public void getAllTimers(TimerListRenderEvent timersEvent){
        timerListRenderEvent = timersEvent;
        renderTimers(timersEvent);
        resetSwipe();

    }

    private void renderTimers(TimerListRenderEvent timersEvent){
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
        }
    }

    @Subscribe
    public void getCalendarEvents(CalendarTaskListEvent taskListEvent) {
        renderCalendar(taskListEvent);
        resetSwipe();

    }

    private void renderCalendar(CalendarTaskListEvent taskListEvent){
        calendarTaskListEvent = taskListEvent;

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
    public void getCountGraph(CountGraphEvent event){
        renderCountGraph(event);
        countGraphEvent = event;
    }

    private void renderCountGraph(final CountGraphEvent event){
        LineChartView lineChartView = (LineChartView)getActivity().findViewById(R.id.item_graph);


        final Activity act = getActivity();
        if(event.countDataEntities.size() > 0){
            GraphRenderer.renderSimpleLineGraph(lineChartView, event.countDataEntities);

            getActivity().findViewById(R.id.navigate_to_detailgraph).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemEntity item = new ItemEntity();
                    item.name = getString(R.string.prompt_you);
                    item.countProperties = event.countDataEntities;
                    DetailGraphActivity.startActivity(act, item);
                }
            });

            int countSize = event.countDataEntities.size();
            Date start;
            Date end;
            if(event.countDataEntities.size() > 0){
                start = event.countDataEntities.get(0).date;
                end = event.countDataEntities.get(countSize - 1).date;
            }else{
                start = new Date();
                end = start;
            }
            graphDateFrom.setText(ViewUtil.dateToString(start, true));
            graphDateTo.setText(ViewUtil.dateToString(end, true));

        }else{
            noGraph.setVisibility(View.VISIBLE);
            graphDateWrapper.setVisibility(View.GONE);
            detailCountWrapper.setVisibility(View.GONE);
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

    /*
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
    */

}
