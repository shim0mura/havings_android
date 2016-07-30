package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        constructForm();
        if(item.isGarbage){
            itemCountWrapper.setVisibility(View.GONE);
        }

        setDefaultValue();

        setTitle(getString(R.string.prompt_edit_form, itemTypeString));
        Button b = (Button)findViewById(R.id.post_item);
        b.setText(getString(R.string.prompt_edit_item_button, itemTypeString));

        formPresenter.getUserListTree();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();

        progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);
        formPresenter.attemptToUpdateItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){

        if(progressDialog != null){
            progressDialog.dismiss();
        }
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

    @Subscribe
    @Override
    public void subscribeAlert(AlertEvent event) {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        showAlert(event);
    }
}
