package work.t_s.shim0mura.havings.model;

import android.content.Context;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.inject.Inject;

import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import work.t_s.shim0mura.havings.model.di.Api;
import work.t_s.shim0mura.havings.model.di.ApiComponent;
import work.t_s.shim0mura.havings.model.di.ApiModule;
import work.t_s.shim0mura.havings.model.di.DaggerApiComponent;

/**
 * Created by shim0mura on 2015/11/13.
 */
public class ApiServiceManager {
    @Inject Api okhttpClient;

    private static ApiServiceManager asm;
    private static ApiService service;
    private static Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create());

    private static Retrofit retrofit;
    private AuthHeaderInterceptor ahi;
    private Context context;
    private ApiKey apiKey;

    private ApiServiceManager(Context c){
        ApiComponent api = DaggerApiComponent.builder().apiModule(new ApiModule(c)).build();
        api.inject(this);
        retrofit = builder.client(okhttpClient.getClient()).build();
        context = c;

        apiKey = ApiKey.getSingleton(context);
        if(canAccessToApi()){
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

    public Boolean canAccessToApi(){
        return (apiKey.getToken() != null && apiKey.getUid() != null);
    }

    public void setApiKey(String token, String uid){
        apiKey.storeApiKey(token, uid);
        addAuthHeader(token, uid);
    }

    public void clearApiKey(){
        apiKey.clearApiKey();
        removeAuthHeader();
    }

    public OkHttpClient getHttpClient(){
        return okhttpClient.getClient();
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

}
