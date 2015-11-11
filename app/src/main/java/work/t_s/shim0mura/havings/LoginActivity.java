package work.t_s.shim0mura.havings;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.LoginPresenter;

public class LoginActivity extends SessionBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.TAG = "LoginActivity:";

        this.presenter = new LoginPresenter(this);

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

    @Subscribe
    @Override
    public void toggleLoading(final ToggleLoadingEvent event){
        super.toggleLoading(event);
    }

    @Subscribe
    @Override
    public void setError(SetErrorEvent event){
        super.setError(event);
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


