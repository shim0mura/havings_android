package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import com.squareup.otto.Subscribe;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.util.TouchImageView;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ImageDetailActivity extends AppCompatActivity {

    private static final String SERIALIZED_ITEM = "SerializedItem";
    private static final String SERIALIZED_ITEM_IMAGE = "SerializedItemImage";
    private static final String SERIALIZED_ITEM_IMAGE_ID = "SerializedItemImageId";

    private ItemEntity item;
    private ItemImageEntity itemImageEntity;
    private ItemPresenter itemPresenter;

    @Bind(R.id.image_date) TextView imageDate;
    @Bind(R.id.detail_image) ImageView detailImage;
    @Bind(R.id.image_memo) TextView imageMemo;
    @Bind(R.id.image_favorite_count) TextView imageFavoriteCount;
    @Bind(R.id.image_favorite_button) ImageView imageFavoriteButton;


    public static void startActivity(Context context, ItemEntity item, ItemImageEntity itemImage){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(SERIALIZED_ITEM_IMAGE, itemImage);

        context.startActivity(intent);
    }

    public static void startActivity(Context context, ItemEntity item, int itemImageId){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(SERIALIZED_ITEM_IMAGE_ID, itemImageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);
        itemPresenter = new ItemPresenter(this);

        setContentView(R.layout.activity_image_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        setTitle(item.name + getString(R.string.postfix_prompt_of_item_image));

        int itemImageId = extras.getInt(SERIALIZED_ITEM_IMAGE_ID, 0);
        if(itemImageId == 0) {
            itemImageEntity = (ItemImageEntity)extras.getSerializable(SERIALIZED_ITEM_IMAGE);
            renderImages();
        }else{
            itemPresenter.getItemImage(item.id, itemImageId);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregist action");
        super.onPause();
    }

    @Subscribe
    public void getItemImageEntity(ItemImageEntity itemImage){
        itemImageEntity = itemImage;
        renderImages();
    }

    private void renderImages(){
        imageDate.setText(ViewUtil.dateToString(itemImageEntity.addedDate, true));
        imageFavoriteCount.setText(String.valueOf(itemImageEntity.imageFavoriteCount));
        if(itemImageEntity.memo.isEmpty()){
            imageMemo.setVisibility(View.GONE);
        }else {
            imageMemo.setText(itemImageEntity.memo);
        }
        toggleFavoriteState();

        String thumbnail = ApiService.BASE_URL + itemImageEntity.url;
        Glide.with(this)
                .load(thumbnail)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Timber.d("failed to get image");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        findViewById(R.id.image_loader).setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(detailImage);
    }

    @OnClick(R.id.image_favorite_button)
    public void actionFavorite(){
        if(itemImageEntity.isFavorited){
            itemPresenter.unfavoriteItemImage(itemImageEntity.id);
        }else{
            itemPresenter.favoriteItemImage(itemImageEntity.id);
        }
    }

    @OnClick(R.id.favorite_count_wrapper)
    public void redirectToFavoritedUserList(){
        UserListActivity.startActivity(this, UserListPresenter.ITEM_IMAGE_FAVORITE_USER_LIST, itemImageEntity.id);
    }

    @Subscribe
    public void applyGereralResult(ResultEntity resultEntity){
        Timber.d("get general result %s", resultEntity.resultType);
        switch(resultEntity.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM_IMAGE:
                updateFavoriteProperties(true);
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE:
                updateFavoriteProperties(false);
                break;
            default:
                Timber.w("Unexpected ResultCode Returned... code: %s, relatedId: %s", resultEntity.resultType, resultEntity.relatedId);
                break;
        }
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM_IMAGE:
                Timber.d("failed to favorite item image");
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE:
                Timber.d("failed to unfavorite item image");
                break;
            case GeneralResult.RESULT_GET_ITEM_IMAGE:
                Timber.d("failed to get item image");
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    private void updateFavoriteProperties(Boolean isFavorited){
        itemImageEntity.isFavorited = isFavorited;
        int count = Integer.parseInt(imageFavoriteCount.getText().toString());
        if(isFavorited){
            imageFavoriteCount.setText(String.valueOf(count + 1));
        }else{
            count = count - 1;
            if(count < 0){
                count = 0;
            }
            imageFavoriteCount.setText(String.valueOf(count));
        }
        toggleFavoriteState();
    }

    private void toggleFavoriteState(){
        if(itemImageEntity.isFavorited){
            imageFavoriteButton.setImageResource(R.drawable.ic_already_favorite_36dp);
        }else{
            imageFavoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);
        }
    }

}
