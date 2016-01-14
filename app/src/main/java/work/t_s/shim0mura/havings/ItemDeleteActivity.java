package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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

public class ItemDeleteActivity extends ItemFormBaseActivity {

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_delete);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        setFellowAdapter(getText(R.string.prompt_delete_fellow_items_explanation).toString(), getText(R.string.prompt_delete_fellow_items_sub_explanation).toString());
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        List<Integer> fellowIdList = new ArrayList<Integer>();

        for(ItemEntity i : item.owningItems){
            if(i.isSelectedForSomething){
                Timber.d("item id %s name: %s", i.id, i.name);
                fellowIdList.add(i.id);
            }
        }

        item.fellowIds = fellowIdList;

        formPresenter.attemptToDeleteItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.fellowIds = new ArrayList<Integer>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        Intent data = new Intent();
        data.putExtra(ItemActivity.UPDATED_ITEM, item);
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
