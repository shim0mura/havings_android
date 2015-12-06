package work.t_s.shim0mura.havings;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.jakewharton.scalpel.ScalpelFrameLayout;

import java.util.ArrayList;

import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;

//public class ItemActivity extends AppCompatActivity implements ObservableScrollViewCallbacks{
public class ItemActivity extends AppCompatActivity {


    private static final float MAX_TEXT_SCALE_DELTA = 0.9f;

    private ItemPresenter itemPresenter;
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
    private FragmentPagerAdapter adapter;

    private boolean isEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.toolbar = toolbar;

        itemPresenter = new ItemPresenter(this);
        stickyScrollPresenter = new StickyScrollPresenter(this);
        //itemPresenter.test();

        mImageView = findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mToolbarView = findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, ContextCompat.getColor(this, R.color.colorPrimary)));
        mFlexibleSpaceView = findViewById(R.id.overlay);
        mDescView = findViewById(R.id.desc);

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        //mScrollView.setScrollViewCallbacks(this);

        mTitleView = (TextView)findViewById(R.id.title);
        //mTitleView.setText(getTitle());
        mTitleView.setText("あいうえおかきくけこさしすせそ");
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

        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                //return TestFragment.newInstance(position + 1);
                return TestFragment.newInstance(position + 1, stickyScrollPresenter);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "tab " + (position + 1);
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("tab touch", String.valueOf(v.getScrollY()));
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

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        Log.d("activity", "oncread end");

        final Activity a = this;

        ViewTreeObserver vto = viewPager.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                stickyScrollPresenter.initialize();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        stickyScrollPresenter.updateScrolling(0);

        Log.d("title size in onwindow", String.valueOf(mTitleView.getHeight()));

    }

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
            View view = inflater.inflate(R.layout.tab, container, false);
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
