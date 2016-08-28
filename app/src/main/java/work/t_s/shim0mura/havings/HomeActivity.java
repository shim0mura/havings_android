package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
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
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.TooltipManager;
import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.DeviceTokenCheckEvent;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.model.event.NotificationEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.NotificationGcmIntentService;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;

// public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

public class HomeActivity extends DrawerActivity {


    private UserPresenter userPresenter;
    private View notificationView;
    private int userId;

    private HomePresenter.HomeTabPagerAdapter pagerAdapter;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    //final FloatingActionMenu fab = (FloatingActionMenu)findViewById(R.id.fab);
    @Bind(R.id.fab) FloatingActionMenu fab;
    @Bind(R.id.fab_placeholder) RelativeLayout fabPlaceholder;
    @Bind(R.id.main_content) CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        RealmConfiguration c = new RealmConfiguration.Builder(this).schemaVersion(1).migration(new RealmMigration() {
            @Override
            public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

            }
        }).build();
        Realm a = Realm.getInstance(c);
        a.close();
        Realm.deleteRealm(c);
        */

        DefaultTag tag = DefaultTag.getSingleton(this);
        //tag.checkMigrationVersion();
        //Timber.d("version %s", tag.getCurrentMigrationVersionOfLocal());

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
                    tab.select();
                }else{
                    tab.setIcon(pagerAdapter.getTabIcon(tab.getPosition(), false));
                }
            }
        }

        ButterKnife.bind(this);
        setTitle(R.string.prompt_home);
        checkDeviceToken();

        userPresenter.getSelf();

        showTooltip();
        setFAB();
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
        Timber.d("get unread_notification %s", event.notificationEntities.size());
        setNotificationBadge(event.notificationEntities.size());
    }

    @Subscribe
    public void checkDeviceTokenState(DeviceTokenCheckEvent event){
        String registedToken = ApiKey.getSingleton(this).getDeviceToken();
        Timber.d("regist_token_local %s", registedToken);
        HomePresenter homePresenter = new HomePresenter(this);
        if(event.deviceTokenEntity.token != null && registedToken == null){
            homePresenter.postDeviceToken(event.deviceTokenEntity.token);
        }else if(event.deviceTokenEntity.token != null && !event.deviceTokenEntity.token.equals(registedToken)){
            homePresenter.putDeviceToken(event.deviceTokenEntity.token);
        }
    }

    @Subscribe
    public void deviceTokenUpdateSuccess(DeviceTokenEntity deviceTokenEntity){
        Timber.d("token_update_success");
        ApiKey.getSingleton(this).updateDeviceToken(deviceTokenEntity.token);
    }

    private void checkDeviceToken(){
        Intent intent = new Intent(this, NotificationGcmIntentService.class);
        startService(intent);
    }

    private void setNotificationBadge(int count) {
        if(notificationView != null){
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

    @Subscribe
    public void setUserInfo(UserEntity userEntity){
        ApiKey.getSingleton(this).updateUserInfo(userEntity.name, userEntity.image, userEntity.count);
        onCreateDrawer(true);
    }

    private void showTooltip(){
        ToolTipRelativeLayout toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);

        TooltipManager tm = TooltipManager.getSingleton(this);
        String text = tm.getStatusText();
        if(text == null || text.isEmpty()){
            return;
        }

        ToolTip toolTip = new ToolTip()
                .withText(text)
                .withColor(ContextCompat.getColor(this, R.color.colorAccent))
                .withShadow()
                .withAnimationType(ToolTip.AnimationType.NONE);
        ToolTipView tv = toolTipRelativeLayout.showToolTipForView(toolTip, fabPlaceholder);
    }

    private void setFAB(){
        final Activity act = this;
        fab.setClosedOnTouchOutside(true);
        fab.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.colorAccent));

        //String itemTypeStr = (item.isList) ? getString(R.string.list) : getString(R.string.item);

        final com.github.clans.fab.FloatingActionButton deleteItem = new com.github.clans.fab.FloatingActionButton(this);
        deleteItem.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        deleteItem.setLabelText(getString(R.string.prompt_action_delete_item_from_home));
        deleteItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        deleteItem.setImageResource(R.drawable.ic_clear_black_24dp);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetItemSelectActivity.startActivity(act, TargetItemSelectActivity.TYPE_DELETE_ITEM);
            }
        });
        fab.addMenuButton(deleteItem);

        final com.github.clans.fab.FloatingActionButton dumpItem = new com.github.clans.fab.FloatingActionButton(this);
        dumpItem.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        dumpItem.setLabelText(getString(R.string.prompt_action_dump_item_from_home));
        dumpItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        dumpItem.setImageResource(R.drawable.ic_delete_black_24dp);
        dumpItem.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        dumpItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetItemSelectActivity.startActivity(act, TargetItemSelectActivity.TYPE_DUMP_ITEM);
            }
        });
        fab.addMenuButton(dumpItem);

        final com.github.clans.fab.FloatingActionButton editItem = new com.github.clans.fab.FloatingActionButton(this);
        editItem.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        editItem.setLabelText(getString(R.string.prompt_action_edit_item_from_home));
        editItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        editItem.setImageResource(R.drawable.ic_mode_edit_black_24dp);
        editItem.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        editItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetItemSelectActivity.startActivity(act, TargetItemSelectActivity.TYPE_EDIT_ITEM);
            }
        });
        fab.addMenuButton(editItem);

        final com.github.clans.fab.FloatingActionButton addImageFAB = new com.github.clans.fab.FloatingActionButton(this);
        addImageFAB.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        addImageFAB.setLabelText(getString(R.string.prompt_action_add_image_from_home));
        addImageFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        addImageFAB.setImageResource(R.drawable.ic_photo_black_24dp);
        addImageFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        addImageFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetItemSelectActivity.startActivity(act, TargetItemSelectActivity.TYPE_ADD_IMAGE);
            }
        });
        fab.addMenuButton(addImageFAB);

        final com.github.clans.fab.FloatingActionButton addItemFAB = new com.github.clans.fab.FloatingActionButton(this);
        addItemFAB.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        addItemFAB.setLabelText(getString(R.string.prompt_action_add_item));
        addItemFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        addItemFAB.setImageResource(R.drawable.item_icon_for_tab);
        addItemFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_ADD_ITEM);
        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemFormActivity.startActivityToCreateItem(act, new ItemEntity());
            }
        });
        fab.addMenuButton(addItemFAB);

        final com.github.clans.fab.FloatingActionButton addListFAB = new com.github.clans.fab.FloatingActionButton(this);
        addListFAB.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        addListFAB.setLabelText(getString(R.string.prompt_action_add_list));
        addListFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        addListFAB.setImageResource(R.drawable.list_icon_for_tab);
        addListFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_ADD_ITEM);
        addListFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListNameSelectActivity.startActivity(act, 0);

            }
        });
        fab.addMenuButton(addListFAB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("activity result");
        if (resultCode == RESULT_OK) {

            if (requestCode == ItemActivity.ITEM_CREATED_RESULTCODE) {

                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_added, getString(R.string.item)), Snackbar.LENGTH_LONG).show();


            } else if (requestCode == ItemActivity.ITEM_UPDATED_RESULTCODE) {
                Bundle extras = data.getExtras();
                ItemEntity item = (ItemEntity)extras.getSerializable(ItemActivity.UPDATED_ITEM);

                String itemType = item.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_updated, itemType), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ItemActivity.IMAGE_ADDED_RESULTCODE) {
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_image_added), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ItemActivity.ITEM_DUMP_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(ItemActivity.DUMP_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_dumped, itemType), Snackbar.LENGTH_LONG).show();

            } else if (requestCode == ItemActivity.ITEM_DELETE_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(ItemActivity.DELETE_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_deleted, itemType), Snackbar.LENGTH_LONG).show();
            }
            showTooltip();
        }
    }
}
