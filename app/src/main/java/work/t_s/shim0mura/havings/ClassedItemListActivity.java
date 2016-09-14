package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;


// TODO: DumpItemListActivityと共通化
public class ClassedItemListActivity extends DrawerActivity {

    private final static String SERIALIZED_TAG_ID = "SerializedTagId";
    private final static String SERIALIZED_TAG_NAME = "SerializedTagName";

    private int tagId;
    private String tagName;
    private View header;
    private View loader;
    private UserPresenter userPresenter;
    private ItemListAdapter adapter;

    @Bind(R.id.dump_item_list)
    ListView dumpItemList;

    public static void startActivity(Context context, int tagId, String tagName){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_TAG_ID, tagId);
        intent.putExtra(SERIALIZED_TAG_NAME, tagName);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        tagId = (int)extras.getSerializable(SERIALIZED_TAG_ID);
        tagName = (String)extras.getSerializable(SERIALIZED_TAG_NAME);

        setContentView(R.layout.activity_classed_item_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        userPresenter = new UserPresenter(this);

        setTitle(this.getString(R.string.prompt_classed_item_list, tagName));

        loader = View.inflate(this, R.layout.loading, null);
        dumpItemList.addFooterView(loader);

        dumpItemList.setAdapter(null);

        userPresenter.getClassedItemList(tagId, 0);

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
    public void setDumpItemList(ItemEntity dumpItems){
        loader.findViewById(R.id.progress).setVisibility(View.GONE);

        if(adapter == null){
            initializeAdapter(dumpItems);
        }else {
            adapter.finishLoadNextItem();
            loader.findViewById(R.id.progress).setVisibility(View.GONE);
            adapter.addItem(dumpItems);
            adapter.notifyDataSetChanged();
        }
    }

    private void initializeAdapter(ItemEntity dumpItems){
        if(dumpItems.owningItems == null || dumpItems.owningItems.isEmpty()){
            dumpItemList.addFooterView(View.inflate(this, R.layout.partial_nothing_text, null));
            return;
        }

        final Activity self = this;
        adapter = new ItemListAdapter(this, R.layout.item_list, dumpItems);
        dumpItemList.setAdapter(adapter);
        dumpItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    ItemActivity.startActivity(self, (int) view.getTag(R.string.tag_item_id));
                                                }
                                            }
        );

        dumpItemList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount == firstVisibleItem + visibleItemCount) && adapter.hasNextItem()) {
                    if (!adapter.getIsLoadingNextItem()) {
                        adapter.startLoadNextItem();
                        loader.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        userPresenter.getDumpItemList(tagId, adapter.getNextPage());
                    }
                }
            }
        });
    }
}
