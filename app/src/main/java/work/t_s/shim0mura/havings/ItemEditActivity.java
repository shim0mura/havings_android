package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;

public class ItemEditActivity extends ItemFormBaseActivity {

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_UPDATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        constructForm();

        setDefaultValue();

        showListNameInputView();

        formPresenter.getUserListTree();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();

        formPresenter.attemptToUpdateItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        Intent data = getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(ItemActivity.UPDATED_ITEM, item);
        data.putExtras(extras);
        setResult(Activity.RESULT_OK, data);

        finish();
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
}
