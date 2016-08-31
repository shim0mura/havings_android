package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.view.NestedItemListAdapter;

public class TargetItemSelectActivity extends AppCompatActivity {

    private final static String SERIALIZED_TYPE = "SerializedType";
    public final static int TYPE_EDIT_ITEM = 0;
    public final static int TYPE_ADD_IMAGE = 1;
    public final static int TYPE_DUMP_ITEM = 2;
    public final static int TYPE_DELETE_ITEM = 3;

    @Bind(R.id.target_items) ListView targetItemListView;
    @Bind(R.id.loading) View loading;

    private UserPresenter userPresenter;
    private ArrayList<ItemEntity> itemList;
    private int userId;
    private int type;

    public static void startActivity(Context context, int type){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_item_select);
        Intent intent = getIntent();
        type = intent.getIntExtra(SERIALIZED_TYPE, 0);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = ApiKey.getSingleton(this).getUserId();

        userPresenter = new UserPresenter(this);
        ButterKnife.bind(this);

        setTitle(getString(R.string.prompt_target_select));
        userPresenter.getItemTree(userId, true);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer");
        super.onPause();
    }

    @Subscribe
    public void setItems(ItemEntity item){
        final Activity act = this;
        loading.setVisibility(View.GONE);
        NestedItemListAdapter adapter = new NestedItemListAdapter(this, R.layout.list_nested_item, item.owningItems);
        targetItemListView.setAdapter(adapter);
        itemList = adapter.itemEntityList;
        targetItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemEntity item = itemList.get(position);

                switch(type){
                    case TYPE_EDIT_ITEM:
                        ItemEditActivity.startActivity(act, item, item.isList);
                        break;
                    case TYPE_ADD_IMAGE:
                        ItemImageEditActivity.startActivity(act, item, item.isList);
                        break;
                    case TYPE_DUMP_ITEM:
                        ItemDumpActivity.startActivity(act, item, item.isList);
                        break;
                    case TYPE_DELETE_ITEM:
                        ItemDeleteActivity.startActivity(act, item, item.isList);
                        break;
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("activity_result");
        if (resultCode == RESULT_OK) {

            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);

            if (requestCode == ItemActivity.ITEM_CREATED_RESULTCODE) {
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_added, getString(R.string.item)), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ItemActivity.ITEM_UPDATED_RESULTCODE) {
                Bundle extras = data.getExtras();
                ItemEntity item = (ItemEntity)extras.getSerializable(ItemActivity.UPDATED_ITEM);
                String itemType = item.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_updated, itemType), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ItemActivity.IMAGE_ADDED_RESULTCODE) {
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_image_added), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ItemActivity.ITEM_DUMP_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(ItemActivity.DUMP_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_dumped, itemType), Snackbar.LENGTH_LONG).show();

            } else if (requestCode == ItemActivity.ITEM_DELETE_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(ItemActivity.DELETE_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_deleted, itemType), Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
