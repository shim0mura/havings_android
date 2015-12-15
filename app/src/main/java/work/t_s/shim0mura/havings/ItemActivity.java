package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.squareup.otto.Subscribe;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.view.TestFragment;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = "ItemActivity";

    public ItemPresenter itemPresenter;
    private StickyScrollPresenter stickyScrollPresenter;
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
    public View mListView;

    private int wrapperViewSize;
    private int statusBarSize;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.toolbar = toolbar;

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("itemId", 0);

        stickyScrollPresenter = new StickyScrollPresenter(this);
        itemPresenter = new ItemPresenter(this);
        itemPresenter.getItem(itemId);

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

        ButterKnife.bind(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //stickyScrollPresenter.updateScrolling(0);

        Log.d("title size in onwindow", String.valueOf(mTitleView.getHeight()));

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
        for(String tagString : tags){
            TextView tag = ItemPresenter.createTag(this, tagString, true);
            itemTag.addView(tag);
        }
    }

    /*
    @OnClick(R.id.test)
    public void testClick2(View v){
        Toast.makeText(this, "テスト タグ", Toast.LENGTH_LONG).show();
    }
    */

    /*
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        Log.d("scroll", String.valueOf(scrollY));
        View view = (View)mScrollView.getChildAt(mScrollView.getChildCount() - 1);
        int diff = (view.getBottom()-(mScrollView.getHeight()+mScrollView.getScrollY()));

        Log.d("diff", String.valueOf(diff));

        if(scrollY > 1174){
Log.d("scroll end", "enedd");
            isEnd = true;
            updateScrolling(scrollY);


        }else{
            isEnd = false;
            updateScrolling(scrollY);

        }
    }

    @Override
    public void onDownMotionEvent() {
        if(isEnd){
            //Log.d("scroll ended", String.valueOf(mScrollView.getCurrentScrollY()));
            //mListView.setPressed(true);
            //mListView.setScrollY(0);

        }
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
    */

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    /*
    public void updateScrolling(int scrollY){


        //mOverlayView.setAlpha(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));
        float scaleBase = Math.min(1, (float) scrollY / mFlexibleSpaceHeight - mToolbalHeight);
        //mOverlayView.setAlpha(al);
        //mToolbarView.getBackground().setAlpha(1);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(scaleBase, ContextCompat.getColor(this, R.color.colorPrimary)));

        //Log.d("aplha", String.valueOf(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1)));
        //Log.d("original-aplha", String.valueOf(al));

        //float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        float scale = 1 + (1 - scaleBase);

        // Pivot the title view to (0, 0)
        //mTitleView.setPivotX(0);
        //mTitleView.setPivotY(0);

        // Scale the title view
        //mTitleView.setScaleX(scale);
        //mTitleView.setScaleY(scale);

        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale * 20);

        //mTitleView.setWidth();

        //Log.d("scale", String.valueOf(scale));

        // Translate the title view
        int adjustedScrollY = (int) ScrollUtils.getFloat(scrollY, 0, mFlexibleSpaceHeight);
        //int maxTitleTranslationY = mToolbarView.getHeight() + mFlexibleSpaceHeight - (int) (mTitleView.getHeight() * (1 + scale));
        //int maxTitleTranslationY = mToolbarView.getHeight() + mFlexibleSpaceHeight - (int) (mTitleView.getHeight() * (1 + scale));

        //int maxTitleTranslationY = (int) (mFlexibleSpaceHeight - mTitleView.getHeight() * scale);
        int h = mTitleView.getHeight();
        int maxTitleTranslationY = (int) (mFlexibleSpaceHeight - mTitleView.getHeight());

        //int titleTranslationY = (int) (maxTitleTranslationY * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight);
        int titleTranslationY = Math.max(maxTitleTranslationY - scrollY, 0);
        mTitleView.setTranslationY(titleTranslationY);

        //mOverlayView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        //mImageView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        mDescView.setTranslationY(-scrollY);
        findViewById(R.id.tab_wrapper).setTranslationY(-scrollY);
        //mListView.setScrollY(scrollY);
        //mOverlayView.setTranslationY(-scrollY);
        //mImageView.setTranslationY(-scrollY);

        //Log.d("scale", String.valueOf(maxTitleTranslationY));
        //Log.d("scale", String.valueOf(titleTranslationY));

        //Log.d("scale", String.valueOf(scale));
        //Log.d("maxtitle", String.valueOf(maxTitleTranslationY));
        //Log.d("title-height", String.valueOf(h));
        //Log.d("title-inlin-height", String.valueOf(mTitleView.getLineHeight()));

        //Log.d("scroll", String.valueOf(scrollY));


        if(scrollY == 1175){
            Log.d("dragging", String.valueOf(dragging));
            mScrollView.setScrollY(1);

        }
    }
    */

    public static class CustomTouchListener implements AdapterView.OnTouchListener {

        private Activity activity;

        public CustomTouchListener(Activity a){
            activity = a;
            Log.d("TouchListener", "called");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("action type", String.valueOf(event.getAction()));
            Log.d("touch", String.valueOf(event.getAction() == MotionEvent.ACTION_MOVE));
            Log.d("super touch", String.valueOf(v.onTouchEvent(event)));
            //return (event.getAction() == MotionEvent.ACTION_MOVE);
            return v.onTouchEvent(event);
        }
    }

    /*
    public static class TestFragment extends Fragment {

        public TestFragment() {
        }

        public static TestFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt("page", page);
            TestFragment fragment = new TestFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int page = getArguments().getInt("page", 0);
            View view = inflater.inflate(R.layout.item_list_tab, container, false);
            if(page == 1) {
                final ListView listView = (ListView)view.findViewById(R.id.page_text);
                ArrayList<String> items = new ArrayList<String>();
                for (int i = 1; i <= 100; i++) {
                    items.add("Item " + i);
                }
                listView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, items));


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.d("position", String.valueOf(position));

                                                    }
                                                }
                );


                listView.setOnTouchListener(new AdapterView.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        //Log.d("ontouch in listview", "hh");
                        //v.setScrollY((int)event.getY());
                        Log.d("action type", String.valueOf(event.getAction()));
                        Log.d("touch", String.valueOf(event.getAction() == MotionEvent.ACTION_MOVE));
                        Log.d("super touch", String.valueOf(v.onTouchEvent(event)));
                        //return (event.getAction() == MotionEvent.ACTION_MOVE);
                        return v.onTouchEvent(event);
                    }

                });

                listView.setOnTouchListener(new CustomTouchListener(this.getActivity()));

                listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        Log.d("schroll state", "changed");
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        Log.d("scroll from listview", String.valueOf(visibleItemCount));
                        Log.d("listview", String.valueOf(listView.getHeight()));
                        //view.setEnabled(false);
                    }
                });

            }else{
                //((TextView) view.findViewById(R.id.page_text)).setText("Page " + page);

            }
            return view;
        }
    }
    */

}
