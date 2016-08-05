package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.squareup.otto.Subscribe;
import com.wefika.flowlayout.FlowLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.model.Line;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.Timer;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.ProgressAlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.util.Share;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";
    private static final String ITEM_ID = "ItemId";
    private static final String CREATE_LIST = "CreateList";
    public static final int ITEM_CREATED_RESULTCODE = 400;
    public static final int ITEM_UPDATED_RESULTCODE = 500;
    public static final int TIMER_CREATED_RESULTCODE = 600;
    public static final int TIMER_UPDATED_RESULTCODE = 700;
    public static final int IMAGE_ADDED_RESULTCODE = 800;
    public static final int ITEM_DUMP_RESULTCODE = 900;
    public static final int ITEM_DELETE_RESULTCODE = 1000;


    public static final String CREATED_ITEM = "ItemCreated";
    public static final String UPDATED_ITEM = "ItemUpdated";
    public static final String ADDED_IMAGES = "AddedImages";
    public static final String DUMP_ITEM = "ItemDumped";
    public static final String DELETE_ITEM = "ItemDeleted";

    public static final String POSTED_TIMER = "PostedTimer";
    public static final String UPDATED_TIMER = "UpdatedTimer";


    public ItemPresenter itemPresenter;
    private StickyScrollPresenter stickyScrollPresenter;
    private TimerPresenter timerPresenter;
    private Activity act;
    private Toolbar toolbar;

    private View mImageView;
    private View mOverlayView;
    private View mToolbarView;
    private View mDescView;
    private View mFlexibleSpaceView;
    private ObservableScrollView mScrollView;
    private int mParallaxImageHeight;
    private int mFlexibleSpaceHeight;
    private int mToolbalHeight;
    private TextView mTitleView;

    private int userId;
    private ItemEntity item = null;
    private ProgressDialog progressDialog;

    private int wrapperViewSize;
    private int statusBarSize;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ItemPresenter.ItemPagerAdapter pagerAdapter;

    @Bind(R.id.image) ImageView thumbnail;
    @Bind(R.id.overlay) View overlay;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.breadcrumb) TextView breadcrumb;
    @Bind(R.id.item_type) ImageView itemTypeIcon;
    @Bind(R.id.item_count) TextView itemCount;
    @Bind(R.id.favorite_count) TextView favoriteCount;
    @Bind(R.id.comment_count) TextView commentCount;
    @Bind(R.id.done_count) TextView doneCount;
    @Bind(R.id.done_count_wrapper) LinearLayout doneCountWrapper;
    @Bind(R.id.item_owner) TextView ownerName;
    @Bind(R.id.prompt_private_type) LinearLayout promptPrivateType;
    @Bind(R.id.owner_image) CircleImageView ownerImage;
    @Bind(R.id.action_favorite_icon) ImageView actionFavoriteIcon;
    @Bind(R.id.action_favorite_text) TextView actionFavoriteText;
    @Bind(R.id.item_tag) FlowLayout itemTag;
    @Bind(R.id.description) TextView description;
    @Bind(R.id.timer_wrapper) LinearLayout timerWrapper;
    @Bind(R.id.add_timer_button) Button addTimerButton;
    @Bind(R.id.share) View share;

    public static void startActivity(Context context, int itemId){
        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(ITEM_ID, itemId);
        context.startActivity(intent);
    }

    public static void startClearActivity(Context context, int itemId){
        Intent intent = new Intent(context, ItemActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ITEM_ID, itemId);
        intent.putExtra(CREATE_LIST, true);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.toolbar = toolbar;

        Intent intent = getIntent();
        int itemId = intent.getIntExtra(ITEM_ID, 0);
        boolean createList = intent.getBooleanExtra(CREATE_LIST, false);

        // リスト追加の場合はactivity resultが使えないので、再度読み込みしてスナックバー表示する
        if(createList){
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);
            Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_added, getString(R.string.list)), Snackbar.LENGTH_LONG).show();
        }
        userId = ApiKey.getSingleton(this).getUserId();

        stickyScrollPresenter = new StickyScrollPresenter(this, StickyScrollPresenter.SCROLL_TYPE_ITEM);
        itemPresenter = new ItemPresenter(this);

        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mToolbarView = findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, ContextCompat.getColor(this, R.color.colorPrimary)));
        mFlexibleSpaceView = findViewById(R.id.overlay);
        mDescView = findViewById(R.id.desc);

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);

        mTitleView = (TextView)findViewById(R.id.title);
        setTitle(null);

        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_height);

        mToolbalHeight = toolbar.getHeight();

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.pager);

        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return v.onTouchEvent(event);
            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             return v.onTouchEvent(event);
                                         }
                                     }
        );

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_already_favorite_18dp);
        act = this;

        ButterKnife.bind(this);
        itemPresenter.getItem(itemId);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);

        BusHolder.get().register(this);
        Timber.d("regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregist action");
        super.onPause();
    }

    @Subscribe
    public void setItemData(ItemEntity itemEntity){
        Log.d("item set", "start");
        item = itemEntity;

        title.setText(item.name);

        breadcrumb.setText(item.breadcrumb.replaceAll("\\s>\\s", " >\n"));

        if(item.isGarbage) {
            itemTypeIcon.setImageResource(R.drawable.ic_delete_white_36dp);
        }else if(item.isList) {
            itemTypeIcon.setImageResource(R.drawable.list_icon);
        }

        if(item.isList && userId == item.owner.id && !item.isGarbage){
            timerPresenter = new TimerPresenter(this, item, new TimerEntity());
            if(item.timers.size() > 0) {
                timerPresenter.renderListTimers(timerWrapper);
            }

            doneCountWrapper.setVisibility(View.VISIBLE);
            doneCount.setText(String.valueOf(item.doneCount));

            if(!item.canAddTimer) {
                addTimerButton.setVisibility(View.GONE);
            }else if(item.timers.size() == 0){
                LinearLayout noTimer = (LinearLayout)timerWrapper.findViewById(R.id.no_timer);
                noTimer.setVisibility(View.VISIBLE);
            }
        }else {
            timerWrapper.setVisibility(View.GONE);
            addTimerButton.setVisibility(View.GONE);
        }

        if(item.privateType > 0){
            promptPrivateType.setVisibility(View.VISIBLE);
        }

        if(item.thumbnail != null){
            setItemThumbnail(item.thumbnail);
        }else{
            setItemHeaderBackground();
        }

        itemCount.setText(String.valueOf(item.count));

        ownerName.setText(item.owner.name);
        if(item.owner.image != null){
            String ownerThumbnail = ApiService.BASE_URL + item.owner.image;
            Glide.with(this).load(ownerThumbnail).into(ownerImage);
        }

        favoriteCount.setText(String.valueOf(item.favoriteCount));
        commentCount.setText(String.valueOf(item.commentCount));

        if(item.isFavorited){
            actionFavoriteIcon.setImageResource(R.drawable.ic_already_favorite_18dp);
            actionFavoriteText.setText(R.string.already_favorite_item);
            actionFavoriteText.setTextColor(ContextCompat.getColor(this, R.color.favorite));
        }

        if(item.tags != null && item.tags.size() > 0){
            setTag(item.tags);
        }else{

        }

        if(item.description != null) {
            description.setText(item.description);
        }else{
            description.setText("");
        }

        description.requestLayout();

        Timber.d("description height %s", description.getHeight());
        Timber.d("tag height %s", itemTag.getMeasuredHeight());

        pagerAdapter = new ItemPresenter.ItemPagerAdapter(this, stickyScrollPresenter, itemPresenter, item);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            switch(i){
                case 0:
                    tab.setCustomView(itemPresenter.getTabView(i, item.isList, (item.isList ? item.owningItemCount : item.imageCount)));
                    break;
                case 1:
                    tab.setCustomView(itemPresenter.getTabView(i, item.isList, (item.isList ? item.imageCount : 0)));
                    break;
                case 2:
                    tab.setCustomView(itemPresenter.getTabView(i, item.isList, 0));
                    break;
            }
        }

        ViewGroup wrapperView = ButterKnife.findById(this, R.id.wrapper);
        wrapperView.removeViewAt(0);
        ButterKnife.findById(this, R.id.frame_wrapper).setVisibility(View.VISIBLE);

        final View des = findViewById(R.id.desc);
        ViewTreeObserver vto = findViewById(R.id.desc).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    des.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    des.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                Timber.d("desc height aftrer vto %s", des.getHeight());
                stickyScrollPresenter.initialize();

            }
        });

        final Context self = this;
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Share.startIntent(self, item.name, Item.getPath(item), thumbnail);
            }
        });

        if(userId == item.owner.id){
            setFAB();
        }
    }

    private void updateItemData(){
        title.setText(item.name);
        itemCount.setText(String.valueOf(item.count));

        if(item.tags != null && item.tags.size() > 0){
            Timber.d(item.tags.toString());

            setTag(item.tags);
        }else{
            Timber.d("tag false");
        }

        if(item.description != null) {
            description.setText(item.description);
        }else{
            description.setText("");
        }

        stickyScrollPresenter.initialize();

    }

    @Subscribe
    public void updateTimerLayout(TimerEntity timerEntity){
        Timber.d("timer update id %s", timerEntity.id);
        if(progressDialog != null){
            progressDialog.dismiss();
        }

        if(timerWrapper != null && timerEntity.isActive){
            timerPresenter.reRenderTimerLayout(timerWrapper, timerEntity);
        }else if(timerWrapper != null && !timerEntity.isActive && !timerEntity.isDeleted){
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);
            timerPresenter.removeTimerLayout(timerWrapper, timerEntity);
            Snackbar.make(coordinatorLayout, "タイマー終了しました", Snackbar.LENGTH_LONG).show();
            addTimerButton.setVisibility(View.VISIBLE);
        }else if(timerWrapper != null && !timerEntity.isActive && timerEntity.isDeleted){
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);
            timerPresenter.removeTimerLayout(timerWrapper, timerEntity);
            Snackbar.make(coordinatorLayout, "タイマー削除しました", Snackbar.LENGTH_LONG).show();
            addTimerButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.add_timer_button)
    public void addNewTimer(){
        TimerFormActivity.startActivity(this, item, new TimerEntity());
    }

    private void setItemThumbnail(String thumbnailUrl){
        thumbnailUrl = ApiService.BASE_URL + thumbnailUrl;
        Glide.with(this).load(thumbnailUrl).into(thumbnail);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            overlay.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shadow));
        } else {
            overlay.setBackground(ContextCompat.getDrawable(this, R.drawable.shadow));
        }
    }

    @OnClick(R.id.action_favorite)
    public void actionFavorite(){
        if(item.isFavorited){
            itemPresenter.unfavoriteItem(item.id);
        }else{
            itemPresenter.favoriteItem(item.id);
        }
    }

    @OnClick(R.id.user_wrapper)
    public void redirectoToUser(){
        UserActivity.startActivity(this, item.owner.id);
    }

    @OnClick(R.id.favorite_count_wrapper)
    public void redirectToFavoritedUserList(){
        UserListActivity.startActivity(this, UserListPresenter.ITEM_FAVORITE_USER_LIST, item.id);
    }

    @OnClick(R.id.comment_count_wrapper)
    public void redirectToCommentList(){
        CommentActivity.startActivity(this, item.id, item.name);
    }

    @OnClick(R.id.done_count_wrapper)
    public void redirectToDoneList(){
        DoneTaskActivity.startActivity(this, item.id, item.name);
    }

    @Subscribe
    public void applyGereralResult(ResultEntity resultEntity){
        Timber.d("get general result %s", resultEntity.resultType);
        switch(resultEntity.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM:
                updateFavoriteProperties(true);
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM:
                updateFavoriteProperties(false);
                break;
            default:
                Timber.w("Unexpected ResultCode Returned... code: %s, relatedId: %s", resultEntity.resultType, resultEntity.relatedId);
                break;
        }
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM:
                Timber.d("failed to favorite item");
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM:
                Timber.d("failed to unfavorite item");
                break;
            case GeneralResult.RESULT_FAVORITE_ITEM_IMAGE:
                Timber.d("failed to favorite item image");
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE:
                Timber.d("failed to unfavorite item image");
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    private void updateFavoriteProperties(Boolean isFavorited){
        item.isFavorited = isFavorited;
        int count = Integer.parseInt(favoriteCount.getText().toString());
        if(isFavorited){
            favoriteCount.setText(String.valueOf(count + 1));
        }else{
            count = count - 1;
            if(count < 0){
                count = 0;
            }
            favoriteCount.setText(String.valueOf(count));
        }
        toggleFavoriteState();
    }

    private void toggleFavoriteState(){
        if(item.isFavorited){
            actionFavoriteIcon.setImageResource(R.drawable.ic_already_favorite_18dp);
            actionFavoriteText.setText(R.string.already_favorite_item);
            actionFavoriteText.setTextColor(ContextCompat.getColor(this, R.color.favorite));
        }else{
            actionFavoriteIcon.setImageResource(R.drawable.ic_favorite_black_18dp);
            actionFavoriteText.setText(R.string.action_favorite_item);
            actionFavoriteText.setTextColor(ContextCompat.getColor(this, R.color.secondaryText));
        }
    }


    private void setItemHeaderBackground(){
        Log.d(TAG, "thumbnail not set");
        overlay.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    private void setTag(List<String> tags){
        itemTag.removeAllViews();
        for(String tagString : tags){
            TextView tag = ItemPresenter.createTag(this, tagString, true);
            itemTag.addView(tag);
        }
    }

    private void setFAB(){
        final FloatingActionMenu fab = (FloatingActionMenu)findViewById(R.id.menu_labels_right);
        fab.setClosedOnTouchOutside(true);
        fab.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.colorAccent));

        String itemTypeStr = (item.isList) ? getString(R.string.list) : getString(R.string.item);

        final FloatingActionButton deleteItem = new FloatingActionButton(this);
        deleteItem.setButtonSize(FloatingActionButton.SIZE_MINI);
        deleteItem.setLabelText(getString(R.string.prompt_action_delete_item, itemTypeStr));
        deleteItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        deleteItem.setImageResource(R.drawable.ic_clear_black_24dp);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemDeleteActivity.startActivity(act, item, item.isList);
            }
        });
        fab.addMenuButton(deleteItem);

        if(!item.isGarbage){
            final FloatingActionButton dumpItem = new FloatingActionButton(this);
            dumpItem.setButtonSize(FloatingActionButton.SIZE_MINI);
            dumpItem.setLabelText(getString(R.string.prompt_action_dump_item, itemTypeStr));
            dumpItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
            dumpItem.setImageResource(R.drawable.ic_delete_black_24dp);
            dumpItem.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
            dumpItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemDumpActivity.startActivity(act, item, item.isList);

                }
            });
            fab.addMenuButton(dumpItem);
        }

        final FloatingActionButton editItem = new FloatingActionButton(this);
        editItem.setButtonSize(FloatingActionButton.SIZE_MINI);
        editItem.setLabelText(getString(R.string.prompt_action_edit_item, itemTypeStr));
        editItem.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
        editItem.setImageResource(R.drawable.ic_mode_edit_black_24dp);
        editItem.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        editItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemEditActivity.startActivity(act, item, item.isList);
            }
        });
        fab.addMenuButton(editItem);

        if(item.isList && !item.isGarbage) {
            final FloatingActionButton addImageFAB = new FloatingActionButton(this);
            addImageFAB.setButtonSize(FloatingActionButton.SIZE_MINI);
            addImageFAB.setLabelText(getString(R.string.prompt_action_add_image, itemTypeStr));
            addImageFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
            addImageFAB.setImageResource(R.drawable.ic_photo_black_24dp);
            addImageFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
            addImageFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemImageEditActivity.startActivity(act, item, item.isList);
                }
            });
            fab.addMenuButton(addImageFAB);

            final FloatingActionButton addItemFAB = new FloatingActionButton(this);
            addItemFAB.setButtonSize(FloatingActionButton.SIZE_MINI);
            addItemFAB.setLabelText(getString(R.string.prompt_action_add_item));
            addItemFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
            addItemFAB.setImageResource(R.drawable.item_icon_for_tab);
            addItemFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_ADD_ITEM);
            addItemFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemFormActivity.startActivityToCreateItem(act, item);
                }
            });
            fab.addMenuButton(addItemFAB);

            final FloatingActionButton addListFAB = new FloatingActionButton(this);
            addListFAB.setButtonSize(FloatingActionButton.SIZE_MINI);
            addListFAB.setLabelText(getString(R.string.prompt_action_add_list));
            addListFAB.setColorNormal(ContextCompat.getColor(this, R.color.fabBackground));
            addListFAB.setImageResource(R.drawable.list_icon_for_tab);
            addListFAB.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_ADD_ITEM);
            addListFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ItemFormActivity.startActivity(act, item, true);
                    //ImageChooseActivity.startActivity(act, "testsets", item.id);
                    ListNameSelectActivity.startActivity(act, item.id);

                }
            });
            fab.addMenuButton(addListFAB);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("activity result");
        if (resultCode == RESULT_OK) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);

            if (requestCode == ITEM_CREATED_RESULTCODE) {

                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_added, getString(R.string.item)), Snackbar.LENGTH_LONG).show();
                Bundle extras = data.getExtras();
                ItemEntity addedItem = (ItemEntity)extras.getSerializable(CREATED_ITEM);
                if(item.isList && addedItem.listId == item.id) {
                    pagerAdapter.unshiftItem(addedItem);
                    pagerAdapter.notifyDataSetChanged();
                }

            } else if (requestCode == ITEM_UPDATED_RESULTCODE) {
                Bundle extras = data.getExtras();
                item = (ItemEntity)extras.getSerializable(UPDATED_ITEM);

                updateItemData();
                String itemType = item.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_updated, itemType), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == IMAGE_ADDED_RESULTCODE) {
                Bundle extras = data.getExtras();
                ItemImageListEntity imageListEntity = (ItemImageListEntity) extras.getSerializable(ADDED_IMAGES);

                pagerAdapter.addImage(imageListEntity);

                Snackbar.make(coordinatorLayout, getString(R.string.prompt_image_added), Snackbar.LENGTH_LONG).show();
            } else if (requestCode == ITEM_DUMP_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(DUMP_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_dumped, itemType), Snackbar.LENGTH_LONG).show();

                if(item.isList && deletedItem.listId == item.id) {
                    pagerAdapter.shiftItem(deletedItem);
                    pagerAdapter.notifyDataSetChanged();
                }
            } else if (requestCode == ITEM_DELETE_RESULTCODE){
                Bundle extras = data.getExtras();
                ItemEntity deletedItem = (ItemEntity)extras.getSerializable(DELETE_ITEM);
                String itemType = deletedItem.isList ? getString(R.string.list) : getString(R.string.item);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_item_deleted, itemType), Snackbar.LENGTH_LONG).show();

                if(item.isList && deletedItem.listId == item.id) {
                    pagerAdapter.shiftItem(deletedItem);
                    pagerAdapter.notifyDataSetChanged();
                }
            } else if (requestCode == TIMER_CREATED_RESULTCODE){
                Bundle extras = data.getExtras();
                TimerEntity addedTimer = (TimerEntity)extras.getSerializable(POSTED_TIMER);
                timerPresenter.addTimerLayout(timerWrapper, addedTimer);
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_added_timer), Snackbar.LENGTH_LONG).show();
                View noTimer = timerWrapper.findViewById(R.id.no_timer);
                noTimer.setVisibility(View.GONE);
                if(timerPresenter.getTimerCounts() >= Timer.MAX_COUNT_PER_LIST){
                    addTimerButton.setVisibility(View.GONE);
                }
            } else if (requestCode == TIMER_UPDATED_RESULTCODE){
                Bundle extras = data.getExtras();
                Timber.d(extras.toString());
                TimerEntity updatedTimer = (TimerEntity)extras.getSerializable(POSTED_TIMER);
                timerPresenter.reRenderTimerLayout(timerWrapper, updatedTimer);

                Snackbar.make(coordinatorLayout, getString(R.string.prompt_updated_timer), Snackbar.LENGTH_LONG).show();

            }
        }
    }

    @Subscribe
    public void subscribeProgressAlert(ProgressAlertEvent event) {
        progressDialog = event.showProgress(this);
    }

    @Subscribe
    public void subscribeAlert(AlertEvent event) {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        showAlert(event);
    }

    public void showAlert(AlertEvent event){
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }
}
