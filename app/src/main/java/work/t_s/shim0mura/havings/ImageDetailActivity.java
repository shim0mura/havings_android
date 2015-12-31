package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Date;

import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.util.TouchImageView;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ImageDetailActivity extends AppCompatActivity {

    private static final String TAG = "ImageDetailActivity:";
    private static final String SERIALIZED_ITEM = "SerializedItem";
    private static final String SERIALIZED_IMAGE = "SerializedImage";
    private static final String INCLUDE_DATE = "IncludeDate";
    private static final String SERIALIZED_DATE = "SerializedDate";



    private ItemEntity item;

    public static void startActivity(Context context, ItemEntity item, String imageUrl, @Nullable Date d){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(SERIALIZED_IMAGE, imageUrl);
        if(d != null) {
            Log.d("date", "null");
            intent.putExtra(INCLUDE_DATE, true);
            intent.putExtra(SERIALIZED_DATE, ViewUtil.dateToString(d, true));
        }else{
            Log.d("date", "not null");
            intent.putExtra(INCLUDE_DATE, false);
        }

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);
        String imageUrl = extras.getString(SERIALIZED_IMAGE);
        Boolean includeDate = extras.getBoolean(INCLUDE_DATE);

        String dateString = null;
        if(includeDate){
            dateString = extras.getString(SERIALIZED_DATE);
            Log.d("date", "aaaa" + dateString);
        }

        setContentView(R.layout.activity_image_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(item.name);

        if(dateString != null){
            TextView imageDate = (TextView)findViewById(R.id.image_date);
            imageDate.setText(dateString);
        }

        ImageView image = (ImageView)findViewById(R.id.detail_image);
        String thumbnail = ApiService.BASE_URL + imageUrl;
        Log.d(TAG, thumbnail);
        Glide.with(this)
                .load(thumbnail)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Log.d(TAG, "failed");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.d(TAG, "success");
                        findViewById(R.id.image_loader).setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(image);

    }

}
