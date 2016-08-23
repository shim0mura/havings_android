package work.t_s.shim0mura.havings.model;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.di.Api;
import work.t_s.shim0mura.havings.model.di.ApiComponent;
import work.t_s.shim0mura.havings.model.di.ApiModule;
import work.t_s.shim0mura.havings.model.di.DaggerApiComponent;
import work.t_s.shim0mura.havings.model.di.WebApiImpl;

/**
 * Created by shim0mura on 2015/11/13.
 */
public class ApiServiceManager {
    @Inject Api okhttpClient;

    private static ApiServiceManager asm;
    private static ApiService service;
    private static Retrofit.Builder builder;
    //ruby風のスネークケースをjava風のキャメルケースに置き換えたいので、その指定をcreateの中でしてる
    //http://stackoverflow.com/questions/10519265/jackson-overcoming-underscores-in-favor-of-camel-case

    private static Retrofit retrofit;
    private AuthHeaderInterceptor ahi;
    private Context context;
    private ApiKey apiKey;

    private ApiServiceManager(Context c){
        ApiComponent api = DaggerApiComponent.builder().apiModule(new ApiModule(c)).build();
        api.inject(this);
        context = c;
        builder = new Retrofit.Builder()
                .baseUrl(getApiUrl())
                .addConverterFactory(JacksonConverterFactory.create(
                        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
                ));
        retrofit = builder.client(okhttpClient.getClient()).build();

        apiKey = ApiKey.getSingleton(context);

        addJsonHeader();
        addRawJsonLogInterceptor();
        setGlideHttpClient(c);

        if(canAccessToApi()) {
            addAuthHeader(apiKey.getToken(), apiKey.getUid());
        }

    }

    public static synchronized ApiServiceManager getSingleton(Context context){
        if(asm == null){
            asm = new ApiServiceManager(context);
        }

        return asm;
    }

    public static synchronized ApiService getService(Context context){
        if(service == null){
            if(asm == null){
                asm = new ApiServiceManager(context);
            }
            service = retrofit.create(ApiService.class);
        }

        return service;
    }

    public String getApiUrl(){
        return context.getString(R.string.api_url);
    }

    public String getWebUrl(){
        return context.getString(R.string.url_by_web);
    }

    public String getRegisterUrl(){
        return context.getString(R.string.api_url) + ApiService.REGISTER;
    }

    public String getLoginUrl(){
        return context.getString(R.string.api_url) + ApiService.SIGNIN;
    }

    public Boolean canAccessToApi(){
        return (apiKey.getToken() != null && apiKey.getUid() != null);
    }

    public void setApiKey(String token, String uid, int userId){
        apiKey.storeApiKey(token, uid, userId);
        addAuthHeader(token, uid);
    }

    public void clearApiKey(){
        apiKey.clearApiKey();
        removeAuthHeader();
    }

    public OkHttpClient getHttpClient(){
        return okhttpClient.getClient();
    }

    public void setGlideHttpClient(Context c){
        // jsonHeaderInterceptorを入れたくないので別のclientを作る
        OkHttpClient client = WebApiImpl.createNewClient();
        Glide.get(c).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    public void addJsonHeader(){
        okhttpClient.getClient().interceptors().add(new JsonHeaderInterceptor());
    }

    public void addAuthHeader(String token, String uid){
        if(ahi == null){
            ahi = new AuthHeaderInterceptor(token, uid);
            okhttpClient.getClient().interceptors().add(ahi);
        }
    }

    public void removeAuthHeader(){
        if(ahi != null) {
            okhttpClient.getClient().interceptors().remove(ahi);
            ahi = null;
        }
    }

    public void addRawJsonLogInterceptor(){
        okhttpClient.getClient().interceptors().add(new RawLogInterceptor());
    }

    private class JsonHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request newRequest = original.newBuilder()
                    .addHeader("Accept", ApiService.JSON.toString())
                    .build();

            return chain.proceed(newRequest);
        }
    }

    private class AuthHeaderInterceptor implements Interceptor {

        private String token;
        private String uid;

        public AuthHeaderInterceptor(String t, String u){
            this.token = t;
            this.uid = u;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request newRequest = original.newBuilder()
                    .addHeader(ApiService.ACCESS_TOKEN_HEADER, this.token)
                    .addHeader(ApiService.UID_HEADER, this.uid)
                    .build();

            return chain.proceed(newRequest);
        }
    }

    private class RawLogInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Response response = chain.proceed(request);
            String rawJson = response.body().string();

            Timber.d("raw JSON response is: %s", rawJson);

            // Re-create the response before returning it because body can be read only once
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(), rawJson)).build();
        }
    }

}
