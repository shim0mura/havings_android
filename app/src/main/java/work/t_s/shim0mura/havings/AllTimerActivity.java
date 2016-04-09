package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.view.TimerListAdapter;

public class AllTimerActivity extends AppCompatActivity {

    private final static String SERIALIZED_TIMERS = "SerializedTimers";
    private View header;
    private List<TimerEntity> timerEntities = new ArrayList<TimerEntity>();
    private TimerListAdapter adapter;

    @Bind(R.id.timer_list) ListView timerList;

    public static void startActivity(Context context, ArrayList<TimerEntity> timers){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_TIMERS, timers);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        timerEntities = (ArrayList<TimerEntity>) extras.getSerializable(SERIALIZED_TIMERS);

        setContentView(R.layout.activity_all_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        header = View.inflate(this, R.layout.partial_basic_header, null);
        TextView pageBreadcrumb = (TextView)header.findViewById(R.id.page_breadcrumb);
        TextView pageTitle = (TextView)header.findViewById(R.id.page_title);
        ImageView pageIcon = (ImageView)header.findViewById(R.id.page_icon);

        //pageBreadcrumb.setText(user.name + "\n> " + this.getString(R.string.prompt_timer_list));
        pageBreadcrumb.setText(this.getString(R.string.prompt_timer_list));

        pageTitle.setText(this.getString(R.string.prompt_timer_list));
        pageIcon.setImageResource(R.drawable.ic_alarm_on_white_36dp);

        timerList.addHeaderView(header);

        adapter = new TimerListAdapter(this, R.layout.partial_timer_content, timerEntities);

        timerList.setAdapter(adapter);

    }

}
