package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.NotificationEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.NotificationEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.NotificationListAdapter;

public class NotificationActivity extends DrawerActivity {

    private UserPresenter userPresenter;

    @Bind(R.id.notification_list) ListView notificationList;
    @Bind(R.id.no_notification) TextView noNotification;
    @Bind(R.id.loading_progress) LinearLayout loadingProgress;

    public static void startActivity(Context context){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        userPresenter = new UserPresenter(this);
        userPresenter.getNotifications();

        setTitle(getString(R.string.prompt_notification));
        onCreateDrawer(false);
        userPresenter.readNotifications();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_READ_NOTIFICATIONS:
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    @Subscribe
    public void setNotificationList(NotificationEvent notificationEvent){
        ToggleLoadingEvent event;
        //ArrayList<NotificationEntity> notificationEntities

        if(notificationEvent.notificationEntities.isEmpty()) {
            event = new ToggleLoadingEvent(noNotification, loadingProgress);
        }else{
            final Activity self = this;
            NotificationListAdapter adapter = new NotificationListAdapter(this, R.layout.list_notification, notificationEvent.notificationEntities);
            notificationList.setAdapter(adapter);
            notificationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String notificationType = (String) view.getTag(R.id.NOTIFICATION_TYPE);
                    int relatedId = (int) view.getTag(R.id.NOTIFICATION_RELATED_ID);
                    Timber.d("notification %s, %s", notificationType, relatedId);
                    switch (notificationType) {
                        case User.NOTIFICATION_TYPE_TIMER:
                            ItemActivity.startActivity(self, relatedId);
                            break;
                        case User.NOTIFICATION_TYPE_COMMENT:
                            ItemActivity.startActivity(self, relatedId);
                            break;
                        case User.NOTIFICATION_TYPE_IMAGE_FAVORITE:
                            ItemActivity.startActivity(self, relatedId);
                            break;
                        case User.NOTIFICATION_TYPE_FAVORITE:
                            ItemActivity.startActivity(self, relatedId);
                            break;
                        case User.NOTIFICATION_TYPE_FOLLOW:
                            UserActivity.startActivity(self, relatedId);
                        default:
                            break;
                    }
                }
            });
            event = new ToggleLoadingEvent(notificationList, loadingProgress);
        }

        ViewUtil.toggleLoading(this, event);
    }

}
