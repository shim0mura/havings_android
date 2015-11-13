package work.t_s.shim0mura.havings.model;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import work.t_s.shim0mura.havings.model.di.Api;
import work.t_s.shim0mura.havings.model.di.ApiComponent;
import work.t_s.shim0mura.havings.model.di.ApiModule;
import work.t_s.shim0mura.havings.model.di.DaggerApiComponent;

/**
 * Created by shim0mura on 2015/11/05.
 */
public class User {
    @Inject Api okhttpClient;

    private static User user;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");

    private User(Context context){
        ApiComponent api = DaggerApiComponent.builder().apiModule(new ApiModule(context)).build();
        api.inject(this);
    }

    public static synchronized User getSingleton(Context context){
        if(user == null){
            user = new User(context);
        }

        return user;
    }

    public void test(){
        Request request = new Request.Builder()
                //.url("https://192.168.1.25:9292/dummy?seconds=3")
                .url("https://192.168.1.25:9292/user/10")
                .addHeader("Accept", "application/json")
                .addHeader("uid", "redgtee@chj.m")
                .addHeader("access-token", "w_LBj4FpJVG33kkpGzXr989zs_X9Soix")
                .get()
                .build();

        okhttpClient.execute(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                Log.d("aaaaaaa", result);
            }
        });
    }

    public void sleep(Callback callback){
        Request request = new Request.Builder()
                .url("https://192.168.1.25:9292/dummy?seconds=7")
                .get()
                .build();

        okhttpClient.execute(request, callback);
    }

    public void registerByEmail(String name, String email, String password, Callback callback){

        //devise側のjsonフォーマットに合わせなければいけないので
        //jackson使うより自前でjson組み立てたほうが楽だったからjackson使わず自分でjson組み立てる
        JSONObject userInfomation = new JSONObject();
        JSONObject userWrapper = new JSONObject();

        try {
            userInfomation.put("name", name);
            userInfomation.put("email", email);
            userInfomation.put("password", password);
            userWrapper.put("user", userInfomation);
        }catch(JSONException e){
            e.printStackTrace();
        }

        /*
        UserEntity user = new UserEntity();
        user.setRegisterInfo(name, email, password);
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(user);
        }catch(JsonProcessingException e){
            e.printStackTrace();
        }
        Log.d("sssss", json);
        */

        // 基本的にapi通信はretrofit任せだけど
        // ログイン周りはX_ACCESS_TOKENヘッダなどが必要なかったり
        // リクエストとレスポンスのjson形式がdevise依存で自分で変更しにくくjacksonでの変換が面倒
        // なので登録とログインだけは生のOkhttpClientを使ってる

        RequestBody requestBody = RequestBody.create(ApiService.JSON, userWrapper.toString());

        Request request = new Request.Builder()
                .url(ApiService.REGISTER)
                .addHeader("Accept", ApiService.JSON.toString())
                .post(requestBody)
                .build();

        okhttpClient.execute(request, callback);
    }

    public void loginByEmail(String email, String password, Callback callback){
        JSONObject userInfomation = new JSONObject();
        JSONObject userWrapper = new JSONObject();

        try {
            userInfomation.put("email", email);
            userInfomation.put("password", password);
            userWrapper.put("user", userInfomation);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(ApiService.JSON, userWrapper.toString());

        Request request = new Request.Builder()
                .url(ApiService.SIGNIN)
                .addHeader("Accept", ApiService.JSON.toString())
                .post(requestBody)
                .build();

        okhttpClient.execute(request, callback);
    }

    public boolean isValidPassword(String password){
        return password.length() >= 8;
    }

    public boolean isValidEmail(String email){
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
