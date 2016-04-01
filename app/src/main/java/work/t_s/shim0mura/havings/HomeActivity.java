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

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private UserPresenter userPresenter;
    private View notificationView;

    private HomePresenter.HomeTabPagerAdapter pagerAdapter;

    //@Bind(R.id.pager) ViewPager viewPager;
    // @Bind(R.id.tabs) TabLayout tabLayout;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own bccb", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ApiService service = ApiServiceManager.getService(this);
        Call<UserEntity> call = service.getUser(10);

        call.enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(Response<UserEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    UserEntity user = response.body();
                    Log.d("user", response.toString());
                    Log.d("user", user.toString());
                    Log.d("user", user.name);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });

        DefaultTag tag = DefaultTag.getSingleton(this);
        Timber.d("version %s", tag.getCurrentMigrationVersionOfLocal());
        tag.checkMigrationVersion();
        Timber.d("version %s", tag.getCurrentMigrationVersionOfLocal());

        userPresenter = new UserPresenter(this);

        viewPager = (ViewPager)findViewById(R.id.pager);
        tabLayout = (TabLayout)findViewById(R.id.tabs);

        pagerAdapter = new HomePresenter.HomeTabPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        // http://androhi.hatenablog.com/entry/2015/06/17/083000
        // setupWithViewPagerだとontabselectedがうまく指定できない
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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
                /*
                if((boolean)notificationView.getTag(R.id.NOTIFICATION_EXIST)) {
                    TextView badge = (TextView) v.findViewById(R.id.notification_badge);
                    badge.setVisibility(View.VISIBLE);
                    badge.setText(String.valueOf(10));
                }
                */
                NotificationActivity.startActivity(act);
            }
        });

        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Timber.d("id %s", id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

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
