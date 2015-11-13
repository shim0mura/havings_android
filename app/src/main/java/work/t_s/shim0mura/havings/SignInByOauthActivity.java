package work.t_s.shim0mura.havings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.presenter.LoginPresenter;
import work.t_s.shim0mura.havings.presenter.SessionBasePresenter;

public class SignInByOauthActivity extends SessionBaseActivity {

    private static String TAG;
    private SessionBasePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.TAG = "LoginActivity:";

        setContentView(R.layout.activity_sign_in_by_oauth);

        this.presenter = new LoginPresenter(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loginByOauth();
    }

    @Subscribe
    @Override
    public void navigateToHome(NavigateEvent event){
        super.navigateToHome(event);
    }

    @Subscribe
    @Override
    public void showAlert(AlertEvent event){
        super.showAlert(event);
    }

}
