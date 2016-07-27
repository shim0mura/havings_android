package work.t_s.shim0mura.havings.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;

/**
 * Created by shim0mura on 2015/11/06.
 */
public class LoginPresenter extends SessionBasePresenter {

    public LoginPresenter(Context c){
        super(c);
    }

    @Override
    public Boolean isValidToStartSession() {
        Boolean isValid = false;
        if(isValidEmail() && isValidPassword()){
            Log.d(TAG, "validation success");
            isValid = true;
        }else{
            Log.d(TAG, "validation failed");
            isValid = false;
        }

        return isValid;
    }

    static public Uri getAuthUri(String provider){
        String uri = ApiService.SIGNIN_BY_OAUTH + provider + ApiService.SIGNIN_BY_OAUTH_PARAMS;
        return Uri.parse(uri);
    }

    @Override
    public void loginByOauth(){
        Bundle params = activity.getIntent().getExtras();
        String token = params.getString("token");
        String uid = params.getString("uid");
        int userid = Integer.valueOf(params.getString("userid"));


        if(token == null || uid == null){
            BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
            Log.d(TAG, "failed to get token");
        }else{
            Log.d(TAG, "get token");
            Log.d(TAG, token);
            Log.d(TAG, params.toString());


            asm.setApiKey(token, uid, userid);

            BusHolder.get().post(new NavigateEvent());
        }
    }

    @Override
    public void attemptToStartSession() {
        TextView email = ButterKnife.findById(activity, R.id.email);
        TextView password = ButterKnife.findById(activity, R.id.password);

        final int emailId = getResourceIdByName("email");
        final int passwordId = getResourceIdByName("password");

        Callback callBack = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
                ToggleLoadingEvent event = new ToggleLoadingEvent(activity.findViewById(R.id.form), activity.findViewById(R.id.loading_progress));
                BusHolder.get().post(event);
                BusHolder.get().post(new AlertEvent(AlertEvent.CANT_REACH_SERVER));
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String result = response.body().string();
                Log.d(TAG, result);
                Log.d(TAG, String.valueOf(response.code()));
                switch (response.code()){
                    case StatusCode.CREATED:
                        Log.d(TAG, "response successed, user created");
                        try{
                            JSONObject jsonResult = new JSONObject(result);
                            String token = jsonResult.getString("token");
                            Log.d(TAG, token);
                            String uid = jsonResult.getString("uid");
                            Log.d(TAG, uid);
                            int userId = Integer.valueOf(jsonResult.getString("userid"));
                            asm.setApiKey(token, uid, userId);
                            BusHolder.get().post(new NavigateEvent());
                        }catch (JSONException e){
                            e.printStackTrace();
                            BusHolder.get().post(new AlertEvent(AlertEvent.CANT_PARSE_RESPONSE));
                        }
                        break;
                    case StatusCode.Unauthorized:
                        Log.d(TAG, "response successed, but posted data wrong");
                        try{
                            String errorStr = new JSONObject(result).getString("error");
                            BusHolder.get().post(new SetErrorEvent(emailId, errorStr));
                        }catch(JSONException e){
                            e.printStackTrace();
                            BusHolder.get().post(new AlertEvent(AlertEvent.CANT_PARSE_RESPONSE));
                        }
                        break;
                    case StatusCode.UnprocessableEntity:
                        Log.d(TAG, "response successed, but posted data invalid");
                        try{
                            JSONObject jsonError = new JSONObject(result).getJSONObject("errors");
                            Log.d(TAG, jsonError.toString());
                            Iterator<String> it = jsonError.keys();
                            while(it.hasNext()){
                                String key = it.next();
                                Log.d(TAG, key);
                                Log.d(TAG, jsonError.getJSONArray(key).toString());
                                String errorStr = jsonError.getJSONArray(key).opt(0).toString();
                                if(key.equals("email")){
                                    BusHolder.get().post(new SetErrorEvent(emailId, activity.getString(R.string.email) + errorStr));
                                }else if(key.equals("password")){
                                    BusHolder.get().post(new SetErrorEvent(passwordId, activity.getString(R.string.password) + errorStr));
                                }else{
                                    Log.d(TAG, "????" + key);
                                }
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                            BusHolder.get().post(new AlertEvent(AlertEvent.CANT_PARSE_RESPONSE));
                        }
                        break;
                    default:
                        Log.d(TAG, String.valueOf(response.code()));
                        BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                }

                ToggleLoadingEvent event = new ToggleLoadingEvent(activity.findViewById(R.id.form), activity.findViewById(R.id.loading_progress));
                BusHolder.get().post(event);
            }
        };

        ToggleLoadingEvent e = new ToggleLoadingEvent(activity.findViewById(R.id.loading_progress), activity.findViewById(R.id.form));
        BusHolder.get().post(e);

        user.loginByEmail(email.getText().toString(), password.getText().toString(), callBack);
    }

}
