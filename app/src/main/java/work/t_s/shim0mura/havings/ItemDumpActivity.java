package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.ProgressDialog;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.TooltipManager;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.view.FellowSelectExpandableListAdapter;

public class ItemDumpActivity extends ItemFormBaseActivity {

    @Bind(R.id.fellow_item_wrapper) LinearLayout fellowWrapper;

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_DUMP_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_dump);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.prompt_item_dump, (item.isList ? getString(R.string.list) : getString(R.string.item))));
        ButterKnife.bind(this);
        if(!item.isList){
            fellowWrapper.setVisibility(View.GONE);
        }

        setFellowAdapter(getText(R.string.prompt_dump_fellow_items_explanation).toString(), getText(R.string.prompt_dump_fellow_items_sub_explanation).toString(), getText(R.string.prompt_dump_item_button).toString());
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

        progressDialog = ProgressDialog.show(this, getTitle(), getString(R.string.prompt_sending), true);
        formPresenter.attemptToDumpItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.fellowIds = new ArrayList<Integer>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        TooltipManager tm = TooltipManager.getSingleton(this);
        int status = tm.getStatus();
        if(status == TooltipManager.STATUS_DUMP){
            tm.setNextStatus();
        }

        Intent data = new Intent();
        data.putExtra(ItemActivity.DUMP_ITEM, item);
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
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        showAlert(event);
    }
}
