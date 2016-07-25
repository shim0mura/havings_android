package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.view.PieChartView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.model.event.NotificationEvent;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.view.GraphRenderer;

public class HomeActivity extends AppCompatActivity {

    private UserPresenter userPresenter;
    private View notificationView;
    private int userId;

    private HomePresenter.HomeTabPagerAdapter pagerAdapter;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own bccb", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DefaultTag tag = DefaultTag.getSingleton(this);
        tag.checkMigrationVersion();
        Timber.d("version %s", tag.getCurrentMigrationVersionOfLocal());

        userPresenter = new UserPresenter(this);

        viewPager = (ViewPager)findViewById(R.id.pager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);

        ApiKey apiKey = ApiKey.getSingleton(this);
        //userId = apiKey.getUid();
        pagerAdapter = new HomePresenter.HomeTabPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        // http://androhi.hatenablog.com/entry/2015/06/17/083000
        // setupWithViewPagerだとontabselectedがうまく指定できない
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){
            @Override
            public void onPageSelected(int position) {
                Timber.d("page change");
                InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(viewPager.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                super.onPageSelected(position);
            }
        });
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tab.setIcon(pagerAdapter.getTabIcon(position, true));
                toolbar.setTitle(pagerAdapter.getTabTitle(position));
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(pagerAdapter.getTabIcon(tab.getPosition(), false));
                super.onTabUnselected(tab);
            }
        });

        // タブ周りの初期化
        toolbar.setTitle(pagerAdapter.getTabTitle(0));
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if(tab != null) {
                if(i == 0){
                    tab.setIcon(pagerAdapter.getTabIcon(tab.getPosition(), true));
                }else{
                    tab.setIcon(pagerAdapter.getTabIcon(tab.getPosition(), false));
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        userPresenter.getNotificationCount();
        Timber.d("register observer");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem notification = menu.findItem(R.id.notification);
        MenuItemCompat.setActionView(notification, R.layout.partial_notification);
        View view = MenuItemCompat.getActionView(notification);

        final Activity act = this;

        notificationView = view;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("notification clicked");

                NotificationActivity.startActivity(act);
            }
        });

        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe
    public void getUnreadNotificationCount(NotificationEvent event){
        Timber.d("get unread notification %s", event.notificationEntities.size());
        setNotificationBadge(event.notificationEntities.size());
    }

    private void setNotificationBadge(int count) {
        TextView badge = (TextView) notificationView.findViewById(R.id.notification_badge);
        ImageView icon = (ImageView) notificationView.findViewById(R.id.notification_icon);

        if (count != 0) {
            notificationView.setTag(R.id.NOTIFICATION_EXIST, true);
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
            icon.setImageResource(R.drawable.ic_notifications_white_36dp);
        } else {
            notificationView.setTag(R.id.NOTIFICATION_EXIST, false);
            badge.setVisibility(View.GONE);
            icon.setImageResource(R.drawable.ic_notifications_none_white_36dp);
        }
    }
}
