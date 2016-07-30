package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.view.FellowSelectExpandableListAdapter;

public class ItemDumpActivity extends ItemFormBaseActivity {

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
        setContentView(R.layout.activity_item_dump);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        setFellowAdapter(getText(R.string.prompt_dump_fellow_items_explanation).toString(), getText(R.string.prompt_dump_fellow_items_sub_explanation).toString());
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        item.isGarbage = true;
        List<Integer> fellowIdList = new ArrayList<Integer>();

        for(ItemEntity i : item.owningItems){
            if(i.isSelectedForSomething){
                Timber.d("item id %s name: %s", i.id, i.name);
                fellowIdList.add(i.id);
            }
        }

        item.fellowIds = fellowIdList;

        formPresenter.attemptToDumpItem(item);
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

    @Subscribe
    @Override
    public void subscribeAlert(AlertEvent event) {
        showAlert(event);
    }
}
