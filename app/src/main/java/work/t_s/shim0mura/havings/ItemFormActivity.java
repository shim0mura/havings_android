package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import work.t_s.shim0mura.havings.model.TooltipManager;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;

public class ItemFormActivity extends ItemFormBaseActivity {

    private ItemEntity relatedItem;
    private static final String SERIALIZED_IMAGE = "SerializedImage";

    public static void startActivityToCreateItem(Context context, ItemEntity i){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        ItemEntity item = new ItemEntity();
        item.listId = i.id;
        item.isList = false;
        item.count = 1;
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(AS_LIST, false);

        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_CREATED_RESULTCODE);
    }

    public static void startActivityToCreateList(Context context, int id, String name, String[] tags, Uri image){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        ItemEntity i = new ItemEntity();
        i.listId = id;
        i.isList = true;
        i.name = name;
        i.tags = Arrays.asList(tags);
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(SERIALIZED_IMAGE, image);
        intent.putExtra(AS_LIST, true);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_CREATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_form);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri imageUri = getIntent().getParcelableExtra(SERIALIZED_IMAGE);

        ButterKnife.bind(this);
        constructForm();

        setTitle(getString(R.string.prompt_create_form, itemTypeString));
        Button b = (Button)findViewById(R.id.post_item);
        b.setText(getString(R.string.prompt_post_item_button, itemTypeString));

        if(item.isList){
            setDefaultValue();
            asGarbageWrapper.setVisibility(View.GONE);
        }

        if(imageUri != null){
            addNewPicture(imageUri);
        }

        formPresenter.getUserListTree();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();

        List<ItemImageEntity> addedImages = formPresenter.constructAddingImage(getAddedImageViews());
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.imageDataForPost.addAll(addedImages);

        progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);

        formPresenter.attemptToCreateItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){

        if(progressDialog != null){
            progressDialog.dismiss();
        }

        TooltipManager tm = TooltipManager.getSingleton(this);
        int status = tm.getStatus();
        if(status == TooltipManager.STATUS_LIST || status == TooltipManager.STATUS_ITEM || status == TooltipManager.STATUS_DUMP){
            tm.setNextStatus();
        }

        if(item.isList){
            ItemActivity.startClearActivity(this, item.listId);
        }else{
            Intent data = getIntent();
            Bundle extras = new Bundle();
            extras.putSerializable(ItemActivity.CREATED_ITEM, itemEntity);
            data.putExtras(extras);
            setResult(Activity.RESULT_OK, data);

            finish();
        }
    }

    @Subscribe
    @Override
    public void subscribeSetListUserOwning(UserListEntity[] list){
        setListUserOwning(list);
    };


    @Subscribe
    @Override
    public void subscribeSetValidateError(SetErrorEvent event) {
        setValidateError(event);
    }

    @Subscribe
    @Override
    public void subscribeAlert(AlertEvent event) {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        showAlert(event);
    }
}
