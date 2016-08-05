package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.UserListAdapter;

public class UserListActivity extends AppCompatActivity {

    private int userListType;
    private int relatedId;
    private SearchResultEntity searchResultEntity;

    private UserListPresenter userListPresenter;
    private UserListAdapter userListAdapter;

    @Bind(R.id.user_list) ListView userListView;
    @Bind(R.id.no_user) TextView noUser;
    @Bind(R.id.loading_progress) LinearLayout loadingProgress;

    public static void startActivity(Context context, int listType, int relatedId){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(UserListPresenter.USER_LIST_TYPE, listType);
        intent.putExtra(UserListPresenter.RELATED_ID, relatedId);

        context.startActivity(intent);
    }

    public static void startActivity(Context context, int listType, SearchResultEntity searchResult){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(UserListPresenter.USER_LIST_TYPE, listType);
        intent.putExtra(UserListPresenter.SEARCH_RESULT, searchResult);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        userListType = (int)extras.getSerializable(UserListPresenter.USER_LIST_TYPE);
        relatedId = extras.getInt(UserListPresenter.RELATED_ID, 0);
        searchResultEntity = (SearchResultEntity)extras.getSerializable(UserListPresenter.SEARCH_RESULT);
        userListPresenter = new UserListPresenter(this);

        switch(userListType){
            case UserListPresenter.ITEM_FAVORITE_USER_LIST:
                setTitle(getText(R.string.prompt_favorited_user_list));
                break;
            case UserListPresenter.FOLLOWING_USER_LIST:
                setTitle(getText(R.string.prompt_following_user_list));
                break;
            case UserListPresenter.FOLLOWED_USER_LIST:
                setTitle(getText(R.string.prompt_followed_user_list));
                break;
            case UserListPresenter.SEARCH_USER_LIST:
                setTitle(getText(R.string.prompt_searched_user_list));
        }

        ButterKnife.bind(this);

        if(relatedId != 0 && searchResultEntity == null){
            userListPresenter.getUsers(userListType, relatedId);
        }else if(searchResultEntity != null){
            setUserList(new ArrayList<UserEntity>(searchResultEntity.users));
        }
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
    public void setUserList(ArrayList<UserEntity> users){
        userListAdapter = new UserListAdapter(this, R.layout.list_user, users);
        userListView.setAdapter(userListAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("user list clicked , id: %s", view.getTag(R.id.TAG_USER_ID));
            }
        });

        Boolean userExist;
        if(users.size() > 0){
            userExist = true;
        }else {
            userExist = false;
        }
        renderList(userExist);
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_GET_USERS:
                renderList(false);
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    private void renderList(Boolean userExist){
        ToggleLoadingEvent event;

        if(userExist) {
            event = new ToggleLoadingEvent(userListView, loadingProgress);
        }else{
            switch(userListType){
                case UserListPresenter.ITEM_FAVORITE_USER_LIST:
                    noUser.setText(getText(R.string.prompt_no_favorited_user));
                    break;
                case UserListPresenter.FOLLOWING_USER_LIST:
                    noUser.setText(getText(R.string.prompt_no_following_user));
                    break;
                case UserListPresenter.FOLLOWED_USER_LIST:
                    noUser.setText(getText(R.string.prompt_no_followed_user));
                    break;
                case UserListPresenter.SEARCH_USER_LIST:
                    noUser.setText(getText(R.string.prompt_no_searched_user));
            }
            event = new ToggleLoadingEvent(noUser, loadingProgress);
        }

        ViewUtil.toggleLoading(this, event);
    }
}
