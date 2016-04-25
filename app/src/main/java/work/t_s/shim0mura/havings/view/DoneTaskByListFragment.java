package work.t_s.shim0mura.havings.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.TaskEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.presenter.DoneTaskPresenter;

/**
 * Created by shim0mura on 2016/04/25.
 */
public class DoneTaskByListFragment extends Fragment {

    private DoneTaskPresenter doneTaskPresenter;
    private DoneTaskPresenter.ExpandableDoneTaskListAdapter adapterForActive;
    private DoneTaskPresenter.ExpandableDoneTaskListAdapter adapterForFinished;

    @Bind(R.id.expandable_active_list) ExpandableListView activeListView;
    @Bind(R.id.expandable_finished_list) ExpandableListView finishedListView;
    @Bind(R.id.no_done_task_at_active) TextView noDoneTaskAtActive;
    @Bind(R.id.no_done_task_at_finished) TextView noDoneTaskAtFinished;

    public static DoneTaskByListFragment newInstance() {
        DoneTaskByListFragment fragment = new DoneTaskByListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DoneTaskByListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_task_by_list, container, false);
        ButterKnife.bind(this, view);

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
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Timber.d("unregister observer from dashboard fragment");
        super.onPause();
    }

    @Subscribe
    public void setPresenter(DoneTaskPresenter presenter){
        doneTaskPresenter = presenter;
        TaskWrapperEntity t = doneTaskPresenter.taskWrapperEntities.get(0);
        if(t == null){
            noDoneTaskAtActive();
            noDoneTaskAtFinished();
            return;
        }

        activeListView.setGroupIndicator(null);
        finishedListView.setGroupIndicator(null);

        List<TaskEntity> activeTasks = new ArrayList<>();
        List<TaskEntity> finishedTasks = new ArrayList<>();

        for(TaskEntity task : t.tasks){
            if(task.timer.isActive){
                activeTasks.add(task);
            }else{
                finishedTasks.add(task);
            }
        }

        if(activeTasks.isEmpty()){
            noDoneTaskAtActive();
        }else{
            adapterForActive = new DoneTaskPresenter.ExpandableDoneTaskListAdapter(getActivity(), activeTasks);
            activeListView.setAdapter(adapterForActive);
        }

        if(finishedTasks.isEmpty()){
            noDoneTaskAtFinished();
        }else{
            adapterForFinished = new DoneTaskPresenter.ExpandableDoneTaskListAdapter(getActivity(), finishedTasks);
            finishedListView.setAdapter(adapterForFinished);
        }
    }

    private void noDoneTaskAtActive(){
        noDoneTaskAtActive.setVisibility(View.VISIBLE);
        activeListView.setVisibility(View.GONE);
    }

    private void noDoneTaskAtFinished(){
        noDoneTaskAtFinished.setVisibility(View.VISIBLE);
        finishedListView.setVisibility(View.GONE);
    }

}
