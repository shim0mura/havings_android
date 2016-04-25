package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.TaskEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.view.DoneTaskByCalenderFragment;
import work.t_s.shim0mura.havings.view.DoneTaskByListFragment;

/**
 * Created by shim0mura on 2016/04/24.
 */
public class DoneTaskPresenter {

    public static final String DATEFORMAT = "yyyy年MM月dd日(E)";

    Activity activity;
    static ApiService service;

    public List<TaskWrapperEntity> taskWrapperEntities;
    public Map<String, Map<Integer, Map<Date, Integer>>> taskDoneMap = new HashMap<>();
    public Map<Integer, TimerEntity> timerEntityMap = new HashMap<>();
    private TaskDoneDateDecorator taskDoneDateDecorator1;
    private TaskDoneDateDecorator taskDoneDateDecorator2;
    private TaskDoneDateDecorator taskDoneDateDecorator3;


    public DoneTaskPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getTask(int itemId){
        Call<TaskWrapperEntity> call = service.getDoneTasksByList(itemId);

        call.enqueue(new Callback<TaskWrapperEntity>() {
            @Override
            public void onResponse(Response<TaskWrapperEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    TaskWrapperEntity searchResultEntity = response.body();
                    BusHolder.get().post(searchResultEntity);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    if (error.errors != null) {
                        for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                            switch (e.getKey()) {
                                default:
                                    //sendErrorToGetUser();
                                    break;
                            }
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("timer get failed");
            }
        });
    }

    public void sortTaskByEventDate(List<TaskWrapperEntity> taskWrappers){
        // { "2015-10" :
        //        { 25 :
        //              "2015/10/25/ 14:30:25" : 10(timerId)
        //
        taskWrapperEntities = taskWrappers;
        Calendar c = Calendar.getInstance();

        Timber.d("wrappersize %s", taskWrappers.size());

        for(TaskWrapperEntity taskWrapperEntity : taskWrappers){
            Timber.d("taskssize %s", taskWrapperEntity.tasks.size());
            for(TaskEntity taskEntity : taskWrapperEntity.tasks){
                Timber.d("events size %s", taskEntity.events.size());

                for(Date eventDate : taskEntity.events){
                    c.setTime(eventDate);
                    String key = getKeyByYearAndMonth(c);
                    TreeMap<Integer, Map<Date, Integer>> dayTree;
                    if(taskDoneMap.get(key) != null){
                        dayTree = (TreeMap)taskDoneMap.get(key);
                    }else{
                        dayTree = new TreeMap<>();
                        taskDoneMap.put(key, dayTree);
                    }

                    HashMap<Date, Integer> eventMap;
                    if(dayTree.get(c.get(Calendar.DAY_OF_MONTH)) != null){
                        eventMap = (HashMap)dayTree.get(c.get(Calendar.DAY_OF_MONTH));
                    }else{
                        eventMap = new HashMap<>();
                        dayTree.put(c.get(Calendar.DAY_OF_MONTH), eventMap);
                    }

                    eventMap.put(c.getTime(), taskEntity.timer.id);

                    if(timerEntityMap.get(taskEntity.timer.id) == null){
                        Timber.d("timer_id %s", taskEntity.timer.id);
                        timerEntityMap.put(taskEntity.timer.id, taskEntity.timer);
                    }
                }
            }
        }
    }

    public Map<Date, Integer> getDoneDate(Calendar c){
        String key = getKeyByYearAndMonth(c);
        if(taskDoneMap.get(key) != null && taskDoneMap.get(key).get(c.get(Calendar.DAY_OF_MONTH)) != null){
            return taskDoneMap.get(key).get(c.get(Calendar.DAY_OF_MONTH));
        }else{
            return null;
        }
    }

    public static String getDate(Date d){
        return DateFormat.format(DATEFORMAT, d).toString();
    }

    private String getKeyByYearAndMonth(Calendar c){
        return String.valueOf(c.get(Calendar.YEAR)) + "-" + String.valueOf(c.get(Calendar.MONTH));
    }

    public void changeMonthAndTaskDoneDate(MaterialCalendarView calendarView, Calendar c){
        String key = getKeyByYearAndMonth(c);
        Map<Integer, Map<Date, Integer>> taskDay = taskDoneMap.get(key);

        Timber.d("key %s", key);

        HashSet<CalendarDay> taskWeight1 = new HashSet<>();
        HashSet<CalendarDay> taskWeight2 = new HashSet<>();
        HashSet<CalendarDay> taskWeight3 = new HashSet<>();

        if(taskDay != null) {
            Timber.d("hash %s", taskDay.toString());

            for (int day : taskDay.keySet()) {
                int weight = taskDay.get(day).keySet().size();
                CalendarDay calendarDay = CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH), day);

                if(weight > 2){
                    taskWeight3.add(calendarDay);
                }else if(weight > 1){
                    taskWeight2.add(calendarDay);
                }else if(weight == 1){
                    taskWeight1.add(calendarDay);
                }
            }
        }

        taskDoneDateDecorator1.setDates(taskWeight1);
        taskDoneDateDecorator2.setDates(taskWeight2);
        taskDoneDateDecorator3.setDates(taskWeight3);

        calendarView.invalidateDecorators();

    }

    public void setDefaultDecorator(MaterialCalendarView calendarView, Calendar c){
        // githubみたいにタスクをこなした回数で表示色をかえたいけど
        // decorator側でそのタスク回数を判断して色を動的に変えられないので
        // 色の数だけdecoratorを追加する
        taskDoneDateDecorator1 = new TaskDoneDateDecorator(Color.parseColor("#338BC34A"));
        taskDoneDateDecorator2 = new TaskDoneDateDecorator(Color.parseColor("#998BC34A"));
        taskDoneDateDecorator3 = new TaskDoneDateDecorator(Color.parseColor("#ee8BC34A"));

        changeMonthAndTaskDoneDate(calendarView, c);

        calendarView.addDecorators(
                new SundayDecorator(), new SaturdayDecorator(), taskDoneDateDecorator1, taskDoneDateDecorator2, taskDoneDateDecorator3
        );
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

    public class TaskDoneDateDecorator implements DayViewDecorator {

        private Drawable highlightDrawable;
        private HashSet<CalendarDay> dates;

        public TaskDoneDateDecorator(int color) {
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

        public void setDates(HashSet<CalendarDay> days){
            dates = days;
        }

    }

    public static class DoneTaskPagerAdapter extends FragmentPagerAdapter {

        public DoneTaskPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return DoneTaskByCalenderFragment.newInstance();
            }else{
                return DoneTaskByListFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0){
                return "カレンダーで見る";
            }else{
                return "リストで見る";
            }
        }
    }


    public static class TaskDoneByDayAdapter extends ArrayAdapter<Map<String, String>> {

        Context context;
        private LayoutInflater layoutInflater;
        private int layoutResource;
        private ArrayList<Map<String, String>> timerMap = new ArrayList<>();
        private Map<Integer, TimerEntity> timerEntityMap;

        public TaskDoneByDayAdapter(Context c, int resource, Map<Integer, TimerEntity> timers){
            super(c, resource);
            context = c;
            this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layoutResource = resource;
            timerEntityMap = timers;
        }

        public void convertDateMap(Map<Date, Integer> map){
            timerMap = new ArrayList<>();
            if(map == null){
                return;
            }
            for(Date date : map.keySet()){
                HashMap<String, String> hash = new HashMap<>();

                int timerId = map.get(date);
                hash.put("timerId", String.valueOf(timerId));
                hash.put("doneDate", Timer.getFormatDueString(date));

                timerMap.add(hash);
            }
        }

        @Override
        public int getCount() {
            return timerMap.size();
        }

        @Override
        public Map<String, String> getItem(int position) {
            return timerMap.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(layoutResource, null);
                holder = new ViewHolder();

                holder.wrapper = (LinearLayout)convertView.findViewById(R.id.wrapper);
                holder.taskName = (TextView)convertView.findViewById(R.id.task_name);
                holder.notificationInterval = (TextView)convertView.findViewById(R.id.notification_interval);
                holder.doneDate = (TextView)convertView.findViewById(R.id.done_date);
                holder.notification = (LinearLayout)convertView.findViewById(R.id.notification);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, String> item = getItem(position);

            // 例外処理したい...
            final TimerEntity timer = timerEntityMap.get(Integer.valueOf(item.get("timerId")));

            holder.taskName.setText(timer.name);
            holder.notificationInterval.setText(Timer.getIntervalString((Activity)context, timer));
            holder.doneDate.setText(item.get("doneDate"));

            holder.wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemActivity.startActivity(context, timer.listId);
                }
            });

            return convertView;
        }

        class ViewHolder{

            LinearLayout wrapper;
            TextView taskName;
            TextView notificationInterval;
            TextView doneDate;
            LinearLayout notification;

        }
    }

    public static class ExpandableDoneTaskListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<TaskEntity> taskEntities;
        private LayoutInflater layoutInflater;

        public ExpandableDoneTaskListAdapter(Context c, List<TaskEntity> tasks){
            context = c;
            taskEntities = tasks;
            layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return taskEntities.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return taskEntities.get(groupPosition).events.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return taskEntities.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            TaskEntity task = (TaskEntity)getGroup(groupPosition);
            if(task == null){
                return null;
            }else{
                return task.events.get(childPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ParentViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.partial_done_task_count, null);
                holder = new ParentViewHolder();

                holder.taskName = (TextView)convertView.findViewById(R.id.task_name);
                holder.notificationInterval = (TextView)convertView.findViewById(R.id.notification_interval);
                holder.doneCount = (TextView)convertView.findViewById(R.id.done_count);
                convertView.setTag(holder);
            } else {
                holder = (ParentViewHolder) convertView.getTag();
            }

            TaskEntity taskEntity = (TaskEntity)getGroup(groupPosition);

            holder.taskName.setText(taskEntity.timer.name);
            holder.notificationInterval.setText(Timer.getIntervalString((Activity) context, taskEntity.timer));
            holder.doneCount.setText(String.valueOf(taskEntity.events.size()));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.partial_done_task_history, null);
                holder = new ChildViewHolder();

                holder.doneAt = (TextView)convertView.findViewById(R.id.done_at);

                convertView.setTag(holder);
            } else {
                holder = (ChildViewHolder) convertView.getTag();
            }

            TaskEntity taskEntity = (TaskEntity)getGroup(groupPosition);
            Date d = taskEntity.events.get(childPosition);

            holder.doneAt.setText(context.getString(R.string.prompt_done_task_done_at, Timer.getFormatDueString(d)));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        class ParentViewHolder{
            TextView taskName;
            TextView notificationInterval;
            TextView doneCount;
        }

        class ChildViewHolder {
            TextView doneAt;
        }
    }
}
