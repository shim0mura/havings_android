package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;

public class ItemImageEditActivity extends ItemFormBaseActivity {

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_CREATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_image_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        addExistPicture();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();

        List<ItemImageEntity> addedImages = formPresenter.constructAddingImage(getAddedImageViews());
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.imageDataForPost.addAll(addedImages);

        item.imageMetadataForUpdate = getChangedImageMetadata();
        item.imageDeleting = getDeletingImage();
        Timber.d("deleting image ids %s", item.imageDeleting.toString());

        formPresenter.attemptToUpdateItemImage(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        Intent data = new Intent();
        data.putExtra(ItemActivity.UPDATED_ITEM, itemEntity);
        setResult(Activity.RESULT_OK, data);

        finish();
    }


    @Subscribe
    @Override
    public void subscribeSetListUserOwning(UserListEntity[] list){
        //setListUserOwning(list);
    };

    @Subscribe
    @Override
    public void subscribeSetValidateError(SetErrorEvent event) {
        //setValidateError(event);
    }

}
