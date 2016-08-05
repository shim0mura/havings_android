package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.CommentEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.CommentPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.CommentAdapter;
import work.t_s.shim0mura.havings.view.UserListAdapter;

public class CommentActivity extends AppCompatActivity {

    private int itemId;
    private CommentPresenter commentPresenter;
    private CommentAdapter commentAdapter;

    @Bind(R.id.comment_wrapper) RelativeLayout commentWrapper;
    @Bind(R.id.comment_list) ListView commentList;
    @Bind(R.id.edit_comment) EditText editComment;
    @Bind(R.id.no_comment) TextView noComment;
    @Bind(R.id.loading_progress) LinearLayout loadingProgress;


    public static void startActivity(Context context, int itemId, String itemName){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(CommentPresenter.ITEM_ID_COMMENT_BELONGS, itemId);
        intent.putExtra(CommentPresenter.ITEM_NAME_COMMENT_BELONGS, itemName);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        itemId = extras.getInt(CommentPresenter.ITEM_ID_COMMENT_BELONGS, 0);
        String itemName = (String)extras.getSerializable(CommentPresenter.ITEM_NAME_COMMENT_BELONGS);
        setTitle(getString(R.string.prompt_comment, itemName));

        commentPresenter = new CommentPresenter(this);

        ButterKnife.bind(this);

        commentPresenter.getComments(itemId);
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
    public void setCommentList(ArrayList<CommentEntity> comments){
        commentAdapter = new CommentAdapter(this, R.layout.list_comment, comments, commentPresenter);
        commentList.setAdapter(commentAdapter);

        Boolean commentExist;
        if(comments.size() > 0){
            commentExist = true;
        }else{
            commentExist = false;
        }
        renderComment(commentExist);
    }

    @Subscribe
    public void commentPosted(CommentEntity commentEntity){
        if(commentEntity.isDeleted){
            commentAdapter.removeComment(commentEntity);
        }else{
            commentAdapter.addComment(commentEntity);
            editComment.setText(null);
            InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodMgr.hideSoftInputFromWindow(editComment.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);
        switch(errorEvent.resultType){
            case CommentPresenter.ERROR_EMPTY_COMMENT:
                editComment.setError(getString(R.string.error_to_post_empty_comment));
                break;
            case CommentPresenter.ERROR_TO_GET_COMMENTS:
                renderComment(false);
                break;
            case CommentPresenter.ERROR_TO_POST_COMMENT:
                Snackbar.make(coordinatorLayout, getString(R.string.error_to_post_comment), Snackbar.LENGTH_LONG).show();
                break;
            case CommentPresenter.ERROR_TO_DELETE_COMMENT:
                Snackbar.make(coordinatorLayout, getString(R.string.error_to_delete_comment), Snackbar.LENGTH_LONG).show();
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    @OnClick(R.id.send_comment)
    public void sendComment(){
        commentPresenter.postComment(itemId, editComment.getText().toString());
    }

    private void renderComment(Boolean commentExist){
        ToggleLoadingEvent event;

        if(commentExist) {
            event = new ToggleLoadingEvent(commentWrapper, loadingProgress);
        }else{
            event = new ToggleLoadingEvent(noComment, loadingProgress);
        }

        ViewUtil.toggleLoading(this, event);
    }

}
