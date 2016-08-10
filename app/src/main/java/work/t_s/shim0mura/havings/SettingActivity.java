package work.t_s.shim0mura.havings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.di.Api;
import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;
import work.t_s.shim0mura.havings.presenter.HomePresenter;

public class SettingActivity extends AppCompatActivity {

    @Bind(R.id.notification_state) Switch notificatioinState;

    private HomePresenter homePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        homePresenter = new HomePresenter(this);
        ButterKnife.bind(this);

        notificatioinState.setChecked(ApiKey.getSingleton(this).getNotificationState());
        setTitle(getString(R.string.prompt_setting));

        notificatioinState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                homePresenter.changeDeviceTokenState(isChecked);
            }
        });

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
    public void setNotificationState(DeviceTokenEntity deviceTokenEntity){
        ApiKey.getSingleton(this).updateNotificationState(deviceTokenEntity.isEnable);

    }
}
