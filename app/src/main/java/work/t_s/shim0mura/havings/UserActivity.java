package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.Share;

public class UserActivity extends DrawerActivity {

    private static final String SERIALIZED_USER_ID = "SerializedUserId";

    private StickyScrollPresenter stickyScrollPresenter;
    private ItemPresenter itemPresenter;
    private UserPresenter userPresenter;
    private UserEntity user;

    @Bind(R.id.desc) View desc;
    @Bind(R.id.image) ImageView backgroundImage;
    @Bind(R.id.overlay) View overlay;
    @Bind(R.id.user_thumbnail) CircleImageView userThumbnail;
    @Bind(R.id.user_name) TextView userName;
    @Bind(R.id.total_item_count) TextView totalItemCount;
    @Bind(R.id.following_count) TextView followingCount;
    @Bind(R.id.follower_count) TextView followerCount;
    @Bind(R.id.favorite_count) TextView favoriteCount;
    @Bind(R.id.dump_item_count) TextView dumpItemCount;
    @Bind(R.id.is_following_viewer) TextView followsOnesidely;
    @Bind(R.id.action_follow) LinearLayout actionFollow;
    @Bind(R.id.action_follow_icon) ImageView actionFollowIcon;
    @Bind(R.id.action_follow_text) TextView actionFollowText;
    @Bind(R.id.action_setting_user) LinearLayout actionSettingUser;
    @Bind(R.id.description) TextView description;
    @Bind(R.id.share) View share;

    @Bind(R.id.tabs) TabLayout tabLayout;
    @Bind(R.id.pager) ViewPager viewPager;

    private UserPresenter.UserPagerAdapter pagerAdapter;

    public static void startActivity(Context context, int userId){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        int userId = extras.getInt(SERIALIZED_USER_ID, 0);

        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stickyScrollPresenter = new StickyScrollPresenter(this, StickyScrollPresenter.SCROLL_TYPE_USER);
        itemPresenter = new ItemPresenter(this);
        userPresenter = new UserPresenter(this);

        ButterKnife.bind(this);

        userPresenter.getUser(userId);

        onCreateDrawer(false);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @OnClick(R.id.user_following)
    public void redirectToFollowingUserList(){
        UserListActivity.startActivity(this, UserListPresenter.FOLLOWING_USER_LIST, user.id);
    }

    @OnClick(R.id.user_followed)
    public void redirectToFollowedUserList(){
        UserListActivity.startActivity(this, UserListPresenter.FOLLOWED_USER_LIST, user.id);
    }

    @OnClick(R.id.action_setting_user)
    public void redirectToEditProfile(){
        ProfileEditActivity.startActivity(this, user);
    }

    @OnClick(R.id.user_like)
    public void redirectToUserFavorites(){
        UserFavoritesActivity.startActivity(this, user);
    }

    @OnClick(R.id.user_dump)
    public void redirectToUserDump(){
        DumpListActivity.startActivity(this, user);
    }

    @OnClick(R.id.action_follow)
    public void followUser(){
        if(user.relation == 0){
            userPresenter.followUser(user.id);
        }else if(user.relation == 1 || user.relation == 2){
            new AlertDialog.Builder(this)
                    .setTitle(this.getString(R.string.action_unfollow))
                    .setMessage(user.name + this.getString(R.string.prompt_action_unfollow))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            userPresenter.unfollowUser(user.id);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Subscribe
    public void renderUser(UserEntity userEntity){
        user = userEntity;

        userName.setText(user.name);
        setTitle(user.name);

        if(user.backgroundImage != null){
            setUserBackground(user.backgroundImage);
        }else{
            setBackground();
        }

        if(user.image != null){
            String thumbnailUrl = ApiService.BASE_URL + user.image;
            Glide.with(this).load(thumbnailUrl).into(userThumbnail);
        }

        totalItemCount.setText(String.valueOf(user.count));
        followingCount.setText(String.valueOf(user.followingCount));
        followerCount.setText(String.valueOf(user.followerCount));
        favoriteCount.setText(String.valueOf(user.favoritesCount + user.imageFavoritesCount));
        dumpItemCount.setText(String.valueOf(user.dumpItemsCount));
        description.setText(user.description);

        toggleFollowingState();
        if(user.relation == User.RELATION_NOTHING && user.isFollowingViewer){
            followsOnesidely.setVisibility(View.VISIBLE);
        }

        pagerAdapter = new UserPresenter.UserPagerAdapter(this, stickyScrollPresenter, userPresenter, itemPresenter, user);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            switch(i){
                case 0:
                    tab.setCustomView(itemPresenter.getTabView(i, true, user.registeredItemCount));
                    break;
                case 1:
                    tab.setCustomView(itemPresenter.getTabView(i, true, user.registeredItemImageCount));
                    break;
                case 2:
                    tab.setCustomView(itemPresenter.getTabView(i, true, 0));
                    break;
            }
        }

        findViewById(R.id.loading_progress).setVisibility(View.GONE);
        findViewById(R.id.frame_wrapper).setVisibility(View.VISIBLE);

        final View des = findViewById(R.id.desc);

        ViewTreeObserver vto = des.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    des.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    des.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                Timber.d("desc height aftrer vto %s", des.getHeight());

                stickyScrollPresenter.initializeUser();

            }
        });

        final Activity self = this;
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Share.startIntent(self, user.name, User.getPath(user), backgroundImage);
            }
        });
    }

    @Subscribe
    public void applyFollowResult(ResultEntity resultEntity){
        Timber.d("get general result %s", resultEntity.resultType);
        switch(resultEntity.resultType){
            case GeneralResult.RESULT_FOLLOW_USER:
                followerCount.setText(String.valueOf(user.followerCount + 1));
                if(user.isFollowingViewer){
                    user.relation = User.RELATION_FRIEND;
                }else{
                    user.relation = User.RELATION_FOLLOWED;
                }
                toggleFollowingState();
                break;
            case GeneralResult.RESULT_UNFOLLOW_USER:
                followerCount.setText(String.valueOf(user.followerCount - 1));
                user.relation = User.RELATION_NOTHING;
                toggleFollowingState();
                break;
            default:
                Timber.w("Unexpected ResultCode Returned in userFollow... code: %s, relatedId: %s", resultEntity.resultType, resultEntity.relatedId);
                break;
        }
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_FOLLOW_USER:
                Timber.d("failed to follow user");
                break;
            case GeneralResult.RESULT_UNFOLLOW_USER:
                Timber.d("failed to unfollow user");
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned in userFollow... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    private void setUserBackground(String thumbnailUrl){
        thumbnailUrl = ApiService.BASE_URL + thumbnailUrl;
        Glide.with(this).load(thumbnailUrl).into(backgroundImage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            overlay.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shadow));
        } else {
            overlay.setBackground(ContextCompat.getDrawable(this, R.drawable.shadow));
        }
    }

    private void setBackground(){
        overlay.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    private void toggleFollowingState(){
        switch(user.relation){
            case User.RELATION_NOTHING:
                actionFollowIcon.setImageResource(R.drawable.ic_person_add_black_18dp);
                actionFollowText.setText(this.getString(R.string.action_follow));
                actionFollowText.setTextColor(ContextCompat.getColor(this, R.color.secondaryText));
                break;
            case User.RELATION_FOLLOWED:
                actionFollowIcon.setImageResource(R.drawable.ic_check_circle_blue_400_18dp);
                actionFollowText.setText(this.getString(R.string.action_already_follow));
                actionFollowText.setTextColor(ContextCompat.getColor(this, R.color.following));
                break;
            case User.RELATION_FRIEND:
                actionFollowIcon.setImageResource(R.drawable.ic_check_circle_blue_400_18dp);
                actionFollowText.setText(this.getString(R.string.action_already_follow));
                actionFollowText.setTextColor(ContextCompat.getColor(this, R.color.following));
                break;
            case User.RELATION_HIMSELF:
                actionFollow.setVisibility(View.GONE);
                actionSettingUser.setVisibility(View.VISIBLE);
                break;
        }
    }
}
