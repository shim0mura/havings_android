package work.t_s.shim0mura.havings.model.di;

import android.content.Context;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import javax.inject.Singleton;

/**
 * Created by shim0mura on 2015/11/05.
 */
@Singleton
public interface Api {
    void test();
    void execute(final Request request, final Callback callback);
    OkHttpClient getClient();
}
