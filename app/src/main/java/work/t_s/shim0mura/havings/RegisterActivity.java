package work.t_s.shim0mura.havings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.LoginPresenter;
import work.t_s.shim0mura.havings.presenter.RegisterPresenter;

public class RegisterActivity extends SessionBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.TAG = "RegisterActivity";

        this.presenter = new RegisterPresenter(this);
        if(presenter.isUserAbleToAccess()){
            navigateToHome(new NavigateEvent());
            Log.d(TAG, "can access with token!");
            return;
        }

        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.login)
    public void transitionToLogin(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
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
