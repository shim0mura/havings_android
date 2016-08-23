package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.DumpListActivity;
import work.t_s.shim0mura.havings.HavingsGlobal;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.ProfileEditActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.UserActivity;
import work.t_s.shim0mura.havings.UserFavoritesActivity;
import work.t_s.shim0mura.havings.UserListActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.TimelineEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.ToggleLoadingEvent;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.Share;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class UserFragment extends Fragment {

    private static final String BUNDLE_USER_ID = "userId";

    private int userId;
    private UserPresenter userPresenter;
    private UserEntity user;

    @Bind(R.id.loading) LinearLayout loading;
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
    @Bind(R.id.description) TextView description;
    @Bind(R.id.action_follow) LinearLayout actionFollow;
    @Bind(R.id.action_setting_user) LinearLayout actionSettingUser;

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, view);

        userPresenter = new UserPresenter(getActivity());
        userPresenter.getSelf();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer from dashboard fragment");
    }

    @Override
    public void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer from dashboard fragment");
        super.onPause();
    }


    @OnClick(R.id.user_following)
    public void redirectToFollowingUserList(){
        UserListActivity.startActivity(getActivity(), UserListPresenter.FOLLOWING_USER_LIST, user.id);
    }

    @OnClick(R.id.user_followed)
    public void redirectToFollowedUserList(){
        UserListActivity.startActivity(getActivity(), UserListPresenter.FOLLOWED_USER_LIST, user.id);
    }

    @OnClick(R.id.user_like)
    public void redirectToUserFavorites(){
        UserFavoritesActivity.startActivity(getActivity(), user);
    }

    @OnClick(R.id.user_dump)
    public void redirectToUserDump(){
        DumpListActivity.startActivity(getActivity(), user);
    }

    @OnClick(R.id.user_detail)
    public void redirectToUserActivity(){
        UserActivity.startActivity(getActivity(), user.id);
    }

    @OnClick(R.id.action_setting_user)
    public void redirectToEditProfile(){
        ProfileEditActivity.startActivity(getActivity(), user);
    }

    @Subscribe
    public void renderUser(UserEntity userEntity){

        ToggleLoadingEvent event = new ToggleLoadingEvent(desc, loading);
        ViewUtil.toggleLoading(getActivity(), event);

        user = userEntity;

        userName.setText(user.name);

        if(user.backgroundImage != null){
            setUserBackground(user.backgroundImage);
        }else{
            setBackground();
        }

        if(user.image != null){
            String thumbnailUrl = ApiServiceManager.getSingleton(getActivity()).getApiUrl() + user.image;
            Glide.with(this).load(thumbnailUrl).into(userThumbnail);
        }

        totalItemCount.setText(String.valueOf(user.count));
        followingCount.setText(String.valueOf(user.followingCount));
        followerCount.setText(String.valueOf(user.followerCount));
        favoriteCount.setText(String.valueOf(user.favoritesCount + user.imageFavoritesCount));
        dumpItemCount.setText(String.valueOf(user.dumpItemsCount));
        description.setText(user.description);

        actionFollow.setVisibility(View.GONE);
        actionSettingUser.setVisibility(View.VISIBLE);
    }

    private void setUserBackground(String thumbnailUrl){
        thumbnailUrl = ApiServiceManager.getSingleton(getActivity()).getApiUrl() + thumbnailUrl;
        Glide.with(this).load(thumbnailUrl).into(backgroundImage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            overlay.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shadow));
        } else {
            overlay.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.shadow));
        }
    }

    private void setBackground(){
        overlay.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
    }

}
