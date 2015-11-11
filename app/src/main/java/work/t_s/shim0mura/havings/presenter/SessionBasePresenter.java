package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;

/**
 * Created by shim0mura on 2015/11/11.
 */
abstract public class SessionBasePresenter {
    User user;
    Activity activity;
    protected String TAG = "SessionBasePresenter...";

    public SessionBasePresenter(Context c){
        user = User.getSingleton(c);
        activity = (Activity)c;
        Log.d(TAG, activity.toString());
    }

    public void test(){

        ApiKey.storeApiKey(activity, "test", "ttttttttt");

        ToggleLoadingEvent e = new ToggleLoadingEvent(activity.findViewById(R.id.loading_progress), activity.findViewById(R.id.form));
        BusHolder.get().post(e);
        Log.d(TAG, "sleepstart");
        Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                user.setTokenAndUid(activity);
                Log.d(TAG, "test result:" + result);
                Log.d(TAG, "token:"+ user.testGetToken());
                Log.d(TAG, "uid:"+ user.testGetUid());
                ToggleLoadingEvent e = new ToggleLoadingEvent(activity.findViewById(R.id.form), activity.findViewById(R.id.loading_progress));
                BusHolder.get().post(e);
                BusHolder.get().post(new AlertEvent(0));
            }
        };
        user.sleep(callback);
    }

    abstract public Boolean isValidToStartSession();

    protected boolean isValidName(){
        TextView name = ButterKnife.findById(activity, R.id.name);

        Boolean isValid = false;
        name.setError(null);
        if(name.getText().toString().isEmpty()){
            BusHolder.get().post(new SetErrorEvent(getResourceIdByName("name"), activity.getString(R.string.error_field_required)));
            isValid = false;
        }else{
            isValid = true;
        }

        return isValid;
    }

    protected boolean isValidPassword(){
        TextView password = ButterKnife.findById(activity, R.id.password);

        Boolean isValid = false;
        password.setError(null);
        if(!user.isValidPassword(password.getText().toString())){
            BusHolder.get().post(new SetErrorEvent(getResourceIdByName("password"), activity.getString(R.string.error_invalid_password)));
            isValid = false;
        }else{
            isValid = true;
        }

        return isValid;
    }

    protected boolean isValidEmail(){
        TextView email = ButterKnife.findById(activity, R.id.email);
        String emailStr = email.getText().toString();

        Boolean isValid = false;
        email.setError(null);
        if(emailStr.isEmpty()){
            BusHolder.get().post(new SetErrorEvent(getResourceIdByName("email"), activity.getString(R.string.error_field_required)));
            isValid = false;
        }else if(!user.isValidEmail(emailStr)){
            BusHolder.get().post(new SetErrorEvent(getResourceIdByName("email"), activity.getString(R.string.error_invalid_email)));
            isValid = false;
        }else{
            isValid = true;
        }

        return isValid;
    }

    abstract public void attemptToStartSession();

    protected int getResourceIdByName(String id){
        Resources res = activity.getResources();

        return res.getIdentifier(id, "id", activity.getPackageName());
    }
}
