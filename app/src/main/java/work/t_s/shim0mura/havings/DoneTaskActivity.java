package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.presenter.CommentPresenter;
import work.t_s.shim0mura.havings.presenter.DoneTaskPresenter;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;

public class DoneTaskActivity extends AppCompatActivity {

    private static final String SERIALIZED_LIST_ID = "listId";

    private TaskWrapperEntity taskWrapperEntity;
    private DoneTaskPresenter doneTaskPresenter;
    private int listId;

    @Bind(R.id.tabs) TabLayout tabLayout;
    @Bind(R.id.viewpager) ViewPager viewPager;

    public static void startActivity(Context context, int listId){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_LIST_ID, listId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        listId = extras.getInt(SERIALIZED_LIST_ID, 0);

        ButterKnife.bind(this);

        doneTaskPresenter= new DoneTaskPresenter(this);
        doneTaskPresenter.getTask(listId);

        FragmentPagerAdapter adapter = new DoneTaskPresenter.DoneTaskPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

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

    @Subscribe
    public void setTasks(TaskWrapperEntity tasks) {
        taskWrapperEntity = tasks;
        ArrayList<TaskWrapperEntity> taskArray = new ArrayList<TaskWrapperEntity>();
        taskArray.add(tasks);
        doneTaskPresenter.sortTaskByEventDate(taskArray);

        BusHolder.get().post(doneTaskPresenter);
    }

}
