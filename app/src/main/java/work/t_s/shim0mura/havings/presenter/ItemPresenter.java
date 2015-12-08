package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;

/**
 * Created by shim0mura on 2015/11/14.
 */
public class ItemPresenter {
    User user;
    Activity activity;
    StickyScrollPresenter stickyScrollPresenter;
    static ApiService service;

    protected String TAG = "ItemPresenter: ";

    public ItemPresenter(Context c, StickyScrollPresenter s){
        user = User.getSingleton(c);
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
        stickyScrollPresenter = s;
    }

    public void test(){
        ImageView imageView = ButterKnife.findById(activity, R.id.image);
        //Glide.get(activity).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(asm.getHttpClient()));
        Glide.with(activity).load("https://192.168.1.25:9292/uploads/item_image/image/23/e79877ee5f3f1e9ab2b4a9c8a289e3f16dfb25cb.jpg").into(imageView);
    }

    public void getItem(int itemId){
        Call<ItemEntity> call = service.getItem(itemId);

        call.enqueue(new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    Log.d("item", item.name);
                    Log.d("item", item.description);
                    Log.d("item", String.valueOf(item.count));
                    Log.d("item", String.valueOf(item.count));
                    Log.d("item is_list", String.valueOf(item.isList));
                    //View metadata = ButterKnife.findById(activity, R.id.upper_item_meta_data);

                    //TextView itemCount = ButterKnife.findById(metadata, R.id.item_count);

                    //setItemData(item);
                    BusHolder.get().post(item);

                    //activityにいれる
                    ViewGroup wrapperView = ButterKnife.findById(activity, R.id.wrapper);
                    wrapperView.removeViewAt(0);
                    ButterKnife.findById(activity, R.id.frame_wrapper).setVisibility(View.VISIBLE);

                    stickyScrollPresenter.initialize();
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

}
