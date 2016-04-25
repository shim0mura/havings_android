package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.squareup.otto.Subscribe;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.PickupActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.SearchTagResultActivity;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.entity.PickupEntity;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.presenter.DoneTaskPresenter;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;

/**
 * Created by shim0mura on 2016/04/25.
 */
public class DoneTaskByCalenderFragment extends Fragment {

    private DoneTaskPresenter doneTaskPresenter;
    private Calendar calendar;
    private DoneTaskPresenter.TaskDoneByDayAdapter adapter;
    private View header;
    private TextView prompt;

    @Bind(R.id.calendar) MaterialCalendarView calendarView;
    @Bind(R.id.task_list) ListView taskListView;

    public static DoneTaskByCalenderFragment newInstance() {
        DoneTaskByCalenderFragment fragment = new DoneTaskByCalenderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DoneTaskByCalenderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_task_by_calendar, container, false);
        ButterKnife.bind(this, view);

        header = View.inflate(getActivity(), R.layout.partial_done_task_result, null);
        prompt = (TextView)header.findViewById(R.id.prompt);
        prompt.setText(getString(R.string.prompt_done_task_result, DoneTaskPresenter.getDate(calendar.getTime()), 0));

        taskListView.addHeaderView(header);
        taskListView.setAdapter(null);

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
        doneTaskPresenter.setDefaultDecorator(calendarView, calendar);

        if(adapter == null){
            adapter = new DoneTaskPresenter.TaskDoneByDayAdapter(getActivity(), R.layout.partial_task_done_date, doneTaskPresenter.timerEntityMap);
            adapter.convertDateMap(doneTaskPresenter.getDoneDate(calendar));
            taskListView.setAdapter(adapter);
        }

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
                Calendar cc = date.getCalendar();
                Map<Date, Integer> map = doneTaskPresenter.getDoneDate(cc);
                int taskCount = (map == null ? 0 : map.size());
                prompt.setText(getString(R.string.prompt_done_task_result, DoneTaskPresenter.getDate(cc.getTime()), taskCount));

                if(map != null) {
                    adapter.convertDateMap(map);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
