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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;

public class DumpListActivity extends AppCompatActivity {

    private final static String SERIALIZED_USER = "SerializedUser";
    private UserEntity user;
    private View header;
    private View loader;
    private UserPresenter userPresenter;
    private ItemListAdapter adapter;

    @Bind(R.id.dump_item_list) ListView dumpItemList;

    public static void startActivity(Context context, UserEntity user){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_USER, user);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        user = (UserEntity)extras.getSerializable(SERIALIZED_USER);

        setContentView(R.layout.activity_dump_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        userPresenter = new UserPresenter(this);

        header = View.inflate(this, R.layout.partial_basic_header, null);
        TextView pageBreadcrumb = (TextView)header.findViewById(R.id.page_breadcrumb);
        TextView pageTitle = (TextView)header.findViewById(R.id.page_title);
        ImageView pageIcon = (ImageView)header.findViewById(R.id.page_icon);

        pageBreadcrumb.setText(user.name + "\n> " + this.getString(R.string.prompt_dump_list));
        pageTitle.setText(this.getString(R.string.prompt_dump_list));
        pageIcon.setImageResource(R.drawable.ic_delete_white_36dp);

        dumpItemList.addHeaderView(header);

        loader = View.inflate(this, R.layout.loading, null);
        dumpItemList.addFooterView(loader);

        dumpItemList.setAdapter(null);

        userPresenter.getDumpItemList(user.id, 0);
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
                        userPresenter.getDumpItemList(user.id, adapter.getNextPage());
                    }
                }
            }
        });
    }

}
