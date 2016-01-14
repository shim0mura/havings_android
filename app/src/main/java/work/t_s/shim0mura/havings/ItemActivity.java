package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.squareup.otto.Subscribe;
import com.wefika.flowlayout.FlowLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";
    private static final String ITEM_ID = "ItemId";
    public static final int ITEM_CREATED_RESULTCODE = 400;
    public static final int ITEM_UPDATED_RESULTCODE = 500;
    public static final String CREATED_ITEM = "ItemCreated";
    public static final String UPDATED_ITEM = "ItemUpdated";

    public ItemPresenter itemPresenter;
    private StickyScrollPresenter stickyScrollPresenter;
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

    private ItemEntity item = null;

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
    @Bind(R.id.item_owner) TextView ownerName;
    @Bind(R.id.owner_image) CircleImageView ownerImage;
    @Bind(R.id.action_favorite_icon) ImageView actionFavoriteIcon;
    @Bind(R.id.action_favorite_text) TextView actionFavoriteText;
    @Bind(R.id.item_tag) FlowLayout itemTag;
    @Bind(R.id.description) TextView description;

    public static void startActivity(Context context, int itemId){
        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(ITEM_ID, itemId);
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

        stickyScrollPresenter = new StickyScrollPresenter(this);
        itemPresenter = new ItemPresenter(this);

        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mToolbarView = findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, ContextCompat.getColor(this, R.color.colorPrimary)));
        mFlexibleSpaceView = findViewById(R.id.overlay);
        mDescView = findViewById(R.id.desc);

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        //mScrollView.setScrollViewCallbacks(this);

        mTitleView = (TextView)findViewById(R.id.title);
        //intentからアイテムのタイトルを読み込ませたほうがいいのでは？
        //mTitleView.setText("アイテムのタイトルを読込中……");
        setTitle(null);

        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_height);
        int flexibleSpaceAndToolbarHeight = mFlexibleSpaceHeight + toolbar.getHeight();

        mToolbalHeight = toolbar.getHeight();

        //mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.pager);

        /*
        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                //return TestFragment.newInstance(position + 1);
                return TestFragment.newInstance(position + 1, stickyScrollPresenter);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "item_list_tab " + (position + 1);
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        */

        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("item_list_tab touch", String.valueOf(v.getScrollY()));
                return v.onTouchEvent(event);
            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             Log.d("pager touch", String.valueOf(v.getScrollY()));
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

        Log.d("activity", "oncread end");
        act = this;

        final FloatingActionMenu fab = (FloatingActionMenu)findViewById(R.id.menu_labels_right);
        fab.setClosedOnTouchOutside(true);

        final FloatingActionButton programFab1 = new FloatingActionButton(this);
        programFab1.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab1.setLabelText("add item");
        programFab1.setImageResource(R.drawable.ic_already_favorite_18dp);
        programFab1.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_ADD_ITEM);
        programFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemFormActivity.startActivity(act, item, false);
            }
        });
        fab.addMenuButton(programFab1);

        final FloatingActionButton programFab2 = new FloatingActionButton(this);
        programFab2.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab2.setLabelText("edit image");
        programFab2.setImageResource(R.drawable.ic_already_favorite_18dp);
        programFab2.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        programFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemImageEditActivity.startActivity(act, item, item.isList);
            }
        });
        fab.addMenuButton(programFab2);

        final FloatingActionButton programFab3 = new FloatingActionButton(this);
        programFab3.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab3.setLabelText("edit item");
        programFab3.setImageResource(R.drawable.ic_already_favorite_18dp);
        programFab3.setTag(R.id.FAB_MENU_TYPE, FormPresenter.FAB_TYPE_EDIT_ITEM);
        programFab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ItemEditActivity.startActivity(act, item, item.isList);
                ItemDeleteActivity.startActivity(act, item, item.isList);

                Timber.d(description.getText().toString());
                ViewGroup.LayoutParams l = description.getLayoutParams();
                Timber.d("descheight: %s", l.height);
            }
        });
        fab.addMenuButton(programFab3);

        ButterKnife.bind(this);
        itemPresenter.getItem(itemId);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Log.d(TAG, "regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Log.d(TAG, "unregist action");

        super.onPause();
    }

    @OnClick(R.id.item_count)
    public void testClick(View v){
        Log.d("test click", "cliced");
        Toast.makeText(this, "テスト", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.title)
    public void testClick1(View v){
        Toast.makeText(this, "テスト comment", Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void setItemData(ItemEntity item){
        Log.d("item set", "start");
        this.item = item;

        title.setText(item.name);

        breadcrumb.setText(item.breadcrumb.replaceAll("\\s>\\s", " >\n"));

        if(item.isList){
            itemTypeIcon.setImageResource(R.drawable.list_icon);
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
                    tab.setCustomView(itemPresenter.getTabView(i, item.owningItemCount));
                    break;
                case 1:
                    tab.setCustomView(itemPresenter.getTabView(i, item.imageCount));
                    break;
                case 2:
                    tab.setCustomView(itemPresenter.getTabView(i, 0));
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

    }

    private void updateItemData(){
        title.setText(item.name);
        itemCount.setText(String.valueOf(item.count));

        if(item.tags != null && item.tags.size() > 0){
            setTag(item.tags);
        }else{

        }

        if(item.description != null) {
            description.setText(item.description);
        }else{
            description.setText("");
        }

        stickyScrollPresenter.initialize();

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

                Timber.d("list created");
                Snackbar.make(coordinatorLayout, "すなっくばーテスト", Snackbar.LENGTH_LONG).show();
                Bundle extras = data.getExtras();
                ItemEntity addedItem = (ItemEntity)extras.getSerializable(CREATED_ITEM);
                if(item.isList && addedItem.listId == item.id) {
                    pagerAdapter.unshiftItem(addedItem);
                }

            } else if (requestCode == ITEM_UPDATED_RESULTCODE) {

                Timber.d("list updated");

                Bundle extras = data.getExtras();
                item = (ItemEntity)extras.getSerializable(UPDATED_ITEM);

                updateItemData();
                Snackbar.make(coordinatorLayout, "すなっくばーUPDATE", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
