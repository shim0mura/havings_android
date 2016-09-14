package work.t_s.shim0mura.havings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import work.t_s.shim0mura.havings.presenter.SessionBasePresenter;

public class SessionBaseActivity extends AppCompatActivity {

    protected static String TAG = "sessionBaseActivity: ";
    protected SessionBasePresenter presenter;

    @Bind(R.id.loading_progress) View loading;
    @Bind(R.id.form) View form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Log.d(TAG, "regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Log.d(TAG, "unregist action");

        super.onPause();
    }

    @OnEditorAction(R.id.password)
    public boolean sessionStartEasily(TextView v, int actionId, KeyEvent e){
        if(presenter.isValidToStartSession()){
            Log.d(TAG, "validate-success!!!");
            InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }else{
            Log.d(TAG, "validate-failed....");
            return true;
        }

        //presenter.test();
        presenter.attemptToStartSession();
        return true;
    }

    @OnClick(R.id.session_start_button)
    public void sessionStart(View v) {
        if(presenter.isValidToStartSession()){
            Log.d(TAG, "validate-success!!!");
            InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }else{
            Log.d(TAG, "validate-failed....");
            return;
        }

        //presenter.test();
        presenter.attemptToStartSession();
    }

    @OnClick({R.id.register_by_twitter, R.id.register_by_facebook})
    public void openBrowser(View v){
        String providerTag = v.getTag().toString();
        LoginPresenter loginPresenter = new LoginPresenter(this);
        Intent intent = new Intent(Intent.ACTION_VIEW, loginPresenter.getAuthUri(providerTag));

        //新しいタブを開かせる
        //http://stackoverflow.com/questions/4119084/android-browser-open-several-urls-each-on-new-window-tab-programmatically
        Bundle b = new Bundle();
        b.putBoolean("new_window", true);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void toggleLoading(final ToggleLoadingEvent event){
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress listSpinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            event.hiding.setVisibility(View.GONE);
            event.hiding.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    event.hiding.setVisibility(View.GONE);
                }
            });

            event.showing.setVisibility(View.VISIBLE);
            event.showing.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    event.showing.setVisibility(View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            event.showing.setVisibility(View.VISIBLE);
            event.hiding.setVisibility(View.GONE);
        }
    }

    public void setError(SetErrorEvent event){
        Log.d(TAG, event.errorStr);
        TextView v = ButterKnife.findById(this, event.resourceId);
        v.setError(event.errorStr);
        v.requestFocus();
    }

    public void navigateToHome(NavigateEvent event){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void showAlert(AlertEvent event){
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }

}
