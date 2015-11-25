package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;

import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.User;

/**
 * Created by shim0mura on 2015/11/14.
 */
public class ItemPresenter {
    User user;
    Activity activity;
    static ApiServiceManager asm;
    protected String TAG = "ItemPresenter: ";

    public ItemPresenter(Context c){
        user = User.getSingleton(c);
        activity = (Activity)c;

        asm = ApiServiceManager.getSingleton(activity);
    }

    public void test(){
        ImageView imageView = ButterKnife.findById(activity, R.id.image);
        Glide.get(activity).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(asm.getHttpClient()));
        Glide.with(activity).load("https://192.168.1.25:9292/uploads/item_image/image/23/e79877ee5f3f1e9ab2b4a9c8a289e3f16dfb25cb.jpg").into(imageView);
    }


}
