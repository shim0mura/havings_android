package work.t_s.shim0mura.havings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.squareup.otto.Subscribe;

import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;

public class SignInByOauthActivity extends AppCompatActivity {

    private static String TAG = "SignInByOAuthActivity....: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_by_oauth);

        Bundle params = getIntent().getExtras();
        String token = params.getString("token");
        String uid = params.getString("uid");

        // TODO: LoginPresenterにこのロジックを入れる
        if(token == null || uid == null){
            BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
            Log.d(TAG, "failed to get token");
        }else{
            Log.d(TAG, "get token");
            Log.d(TAG, token);
            ApiKey.storeApiKey(this, token, uid);

            BusHolder.get().post(new NavigateEvent());

            User user = User.getSingleton(this);

            user.setTokenAndUid(this);

        }
    }

    @Subscribe
    public void navigateToHome(NavigateEvent event){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Subscribe
    public void showAlert(AlertEvent event){
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }

}
