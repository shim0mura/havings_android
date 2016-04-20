package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;

public class UserSearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private SearchPresenter searchPresenter;
    private String searchQuery;

    @Bind(R.id.search_user_loading) LinearLayout loading;

    public static void startActivity(Context context){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        searchPresenter = new SearchPresenter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Timber.d("submit");
                if(query == null || query.isEmpty()){
                    return false;
                }

                searchView.clearFocus();
                searchPresenter.getUserSearchResult(query, 1);
                searchQuery = query;

                loading.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
    public void setSearchResultList(SearchResultEntity searchResultEntity){
        loading.setVisibility(View.GONE);
        UserListActivity.startActivity(this, UserListPresenter.SEARCH_USER_LIST, searchResultEntity);
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType, searchQuery);
                break;
        }
    }

}
