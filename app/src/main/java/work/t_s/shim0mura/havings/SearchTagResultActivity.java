package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;
import work.t_s.shim0mura.havings.view.SearchResultAdapter;

public class SearchTagResultActivity extends AppCompatActivity {

    private final static String SERIALIZED_TAGS = "SerializedTags";
    private View header;
    private View loader;
    private String tags;
    private SearchResultAdapter adapter;
    private SearchPresenter searchPresenter;

    @Bind(R.id.search_result) ListView searchResultList;

    public static void startActivity(Context context, String Tags){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_TAGS, Tags);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        tags = (String)extras.getSerializable(SERIALIZED_TAGS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tag_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        //header = View.inflate(this, R.layout.partial_search_result_header, null);
        header = layoutInflater.inflate(R.layout.partial_search_result_header, searchResultList, false);

        TextView searchContent = (TextView)header.findViewById(R.id.search_content);

        searchContent.setText(String.format(this.getString(R.string.postfix_search_result), tags));

        searchResultList.addHeaderView(header);

        //loader = View.inflate(this, R.layout.loading, null);
        loader = layoutInflater.inflate(R.layout.loading, searchResultList, false);

        searchResultList.addFooterView(loader);

        searchResultList.setAdapter(null);

        searchPresenter = new SearchPresenter(this);
        searchPresenter.getSearchResult(tags, 1);
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
    public void setSearchResultList(SearchResultEntity searchResultEntity){
        // addFooterとかaddHeaderした後にsetAdapterしないと
        // header/footerは表示されないし、動的にremoveやaddも出来ないので
        // 中身の表示非表示で切り替え
        loader.findViewById(R.id.progress).setVisibility(View.GONE);

        if(adapter == null){
            initializeAdapter(searchResultEntity);
            Timber.d("initialize footer count %s", searchResultList.getFooterViewsCount());
        }else {
            adapter.finishLoadNextItem();
            loader.findViewById(R.id.progress).setVisibility(View.GONE);
            adapter.addItem(searchResultEntity);
            adapter.notifyDataSetChanged();
        }
    }

    private void initializeAdapter(SearchResultEntity searchResultEntity){
        TextView searchCount = (TextView)header.findViewById(R.id.search_result_count);
        searchCount.setText(String.format(getString(R.string.postfix_item_count), searchResultEntity.totalCount));

        if(searchResultEntity.items == null || searchResultEntity.items.isEmpty()){
            searchResultList.addFooterView(View.inflate(this, R.layout.partial_nothing_text, null));
            return;
        }

        final Activity self = this;
        adapter = new SearchResultAdapter(this, R.layout.partial_popular_item_list, searchResultEntity);
        searchResultList.setAdapter(adapter);
        searchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    ItemActivity.startActivity(self, (int) view.getTag(R.string.tag_item_id));
                                                }
                                            }
        );

        searchResultList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount == firstVisibleItem + visibleItemCount) && adapter.hasNextItem()) {
                    if (!adapter.getIsLoadingNextItem()) {
                        adapter.startLoadNextItem();
                        loader.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        searchPresenter.getSearchResult(tags, adapter.getCurrentPage() + 1);
                        Timber.d("footer count %s", searchResultList.getFooterViewsCount());
                    }
                }
            }
        });
    }

}
