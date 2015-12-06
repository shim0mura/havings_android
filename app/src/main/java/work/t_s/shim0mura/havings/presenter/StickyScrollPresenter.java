package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;

import java.io.InputStream;

import butterknife.ButterKnife;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.User;

/**
 * Created by shim0mura on 2015/11/25.
 */
public class StickyScrollPresenter {

    Activity activity;
    //static ApiServiceManager asm;

    private Boolean isScrollEnd = false;
    private Boolean isItemTypeIconHide = false;

    public Boolean scrollFromTab = false;
    public Boolean scrollDirection = true;

    public ObservableScrollView scrollView;
    private View scrollGapView;
    private View toolbarView;
    private TextView titleView;
    private View breadcrumbView;
    private View descView;
    private View itemTypeIconView;
    private View tabWrapper;
    private int flexibleSpaceHeight;
    private int breadcrumbHeight;
    private int toolbarHeight;
    private int lastScroll = 0;
    private float scaleFrom;

    protected String TAG = "StickyScrollPresenter: ";

    public StickyScrollPresenter(Context c){
        activity = (Activity)c;

        //asm = ApiServiceManager.getSingleton(activity);

        scrollView = (ObservableScrollView)activity.findViewById(R.id.scroll);
        scrollGapView = activity.findViewById(R.id.scroll_gap);
        toolbarView = activity.findViewById(R.id.toolbar);
        titleView = (TextView)activity.findViewById(R.id.title);
        breadcrumbView = activity.findViewById(R.id.breadcrumb);
        descView = activity.findViewById(R.id.desc);
        itemTypeIconView = activity.findViewById(R.id.item_type);
        tabWrapper = activity.findViewById(R.id.tab_wrapper);

        TypedValue outValue = new TypedValue();
        activity.getResources().getValue(R.dimen.title_scale_start, outValue, true);
        scaleFrom = outValue.getFloat();

        flexibleSpaceHeight = activity.getResources().getDimensionPixelSize(R.dimen.flexible_space_height);

        scrollView.setScrollViewCallbacks(new ObservableScrollCallback());

    }

    public void initialize(){
        //activity.findViewById(R.id.frame_wrapper).measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        breadcrumbHeight = breadcrumbView.getHeight();
        toolbarHeight = toolbarView.getHeight();

        // descの大きさがディスプレイサイズ以上だと、ディスプレイサイズをオーバーした分のviewがレンダリングされない
        // なのでここで子要素を含めた時の大きさを計算して、その大きさにセットしておく
        descView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int descHeight = descView.getMeasuredHeight();
        android.view.ViewGroup.LayoutParams descLayout = descView.getLayoutParams();
        descLayout.height = descHeight;

        /*
        Log.d("image", String.valueOf(activity.findViewById(R.id.image).getMeasuredHeight()));
        Log.d("upper meta", String.valueOf(activity.findViewById(R.id.upper_item_meta_data).getMeasuredHeight()));
        Log.d("down meta", String.valueOf(activity.findViewById(R.id.adding_item_meta_data).getMeasuredHeight()));
        Log.d("description", String.valueOf(activity.findViewById(R.id.description).getMeasuredHeight()));
        Log.d("description", String.valueOf(activity.findViewById(R.id.description).getHeight()));
        */

        // ステータスバーのサイズ取得
        // ステータスバーのdimenはシステム内部で定義されてるっぽい
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarSize = 72;
        if (resourceId > 0) {
            statusBarSize = activity.getResources().getDimensionPixelSize(resourceId);
            Log.d("statusbar", String.valueOf(statusBarSize));
        }

        // ナビゲーションバーを除いた画面サイズの取得
        WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point point = new Point();
        disp.getSize(point);

        tabWrapper.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //activity.findViewById(R.id.tab_wrapper).setPadding(0, descHeight, 0, 0);

        tabWrapper.setPadding(0, descHeight, 0, 0);
        Log.d("displaysize", String.valueOf(point.y));
        Log.d("desc height", String.valueOf(descHeight));
        Log.d("wrapper view size", String.valueOf(tabWrapper.getHeight()));


        activity.findViewById(R.id.item_tag).measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // tabの位置調整
        android.view.ViewGroup.LayoutParams paramTabWrapper = tabWrapper.getLayoutParams();
        //android.view.ViewGroup.LayoutParams paramTabWrapper = activity.findViewById(R.id.tab_wrapper).getLayoutParams();
        paramTabWrapper.height = point.y - statusBarSize + descHeight;

        Log.d("wrapper view size", String.valueOf(tabWrapper.getHeight()));

        // obsevableScrollのサイズ調整
        // tabを含むheightにしないとスクロールしてくれない
        activity.findViewById(R.id.scroll_gap).setPadding(0, flexibleSpaceHeight + toolbarHeight, 0, 0);
        android.view.ViewGroup.LayoutParams paramScroll = activity.findViewById(R.id.scroll_gap).getLayoutParams();
        paramScroll.height = point.y - statusBarSize - activity.findViewById(R.id.tabs).getHeight() + descHeight;

        Log.d("scroll gap size", String.valueOf(paramScroll.height));

        android.view.ViewGroup.LayoutParams params = activity.findViewById(R.id.pager).getLayoutParams();
        Log.d("pager size", String.valueOf(params.height));
        params.height = point.y - statusBarSize - toolbarView.getHeight() - activity.findViewById(R.id.tabs).getHeight();
        Log.d("after pager size", String.valueOf(params.height));
        Log.d("toolbar hieght", String.valueOf(toolbarHeight));
        Log.d("tabsize", String.valueOf(activity.findViewById(R.id.tabs).getHeight()));

        updateScrolling(0);
    }

    public void test(){
        ImageView imageView = ButterKnife.findById(activity, R.id.image);
        //Glide.get(activity).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(asm.getHttpClient()));
        //Glide.with(activity).load("https://192.168.1.25:9292/uploads/item_image/image/23/e79877ee5f3f1e9ab2b4a9c8a289e3f16dfb25cb.jpg").into(imageView);
    }

    public void scroll(int scrolling){
        scrollView.setScrollY(scrolling + scrollView.getCurrentScrollY());
    }

    public void updateScrolling(int scrollY){

        // Tabからスクロールさせるとき、何故かスクロール位置がずれることがありスクロールがガタつく
        // なのでTabからスクロールさせるときはTab側で取得したtouchの方向と
        // scroll側での変位の方向が合ってる場合のみスクロールさせてガタつかないようにする
        Boolean direction;
        if((scrollY - lastScroll) >= 0){
            direction = true;
        }else{
            direction = false;
        }

        if(scrollFromTab && direction != scrollDirection){
            return;
        }else{
            lastScroll = scrollY;
        }

        Log.d("scrollY", String.valueOf(scrollY));
        //mOverlayView.setAlpha(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));
        float scaleOrig = (float)scrollY / (float)(flexibleSpaceHeight - toolbarHeight);
        float scaleBase = Math.max(scaleFrom, scaleOrig);

        //mOverlayView.setAlpha(al);
        //mToolbarView.getBackground().setAlpha(1);
        toolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(scaleOrig, ContextCompat.getColor(activity, R.color.colorPrimary)));

        //Log.d("aplha", String.valueOf(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1)));
        //Log.d("original-aplha", String.valueOf(al));

        //float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        float scale = Math.max(1, 1 + (1 - scaleBase));

        // Pivot the title view to (0, 0)
        //itemTypeIconView.setPivotX(100);
        //itemTypeIconView.setPivotY(-100);

        // Scale the title view
        //itemTypeIconView.setScaleX(scale);
        //itemTypeIconView.setScaleY(scale);

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, scale * 20);

        //mTitleView.setWidth();

        //Log.d("scale", String.valueOf(scale));

        // Translate the title view
        //int adjustedScrollY = (int) ScrollUtils.getFloat(scrollY, 0, mFlexibleSpaceHeight);
        //int maxTitleTranslationY = mToolbarView.getHeight() + mFlexibleSpaceHeight - (int) (mTitleView.getHeight() * (1 + scale));
        //int maxTitleTranslationY = mToolbarView.getHeight() + mFlexibleSpaceHeight - (int) (mTitleView.getHeight() * (1 + scale));

        //int maxTitleTranslationY = (int) (mFlexibleSpaceHeight - mTitleView.getHeight() * scale);
        //int h = mTitleView.getHeight();
        int maxTitleTranslationY = (int) (flexibleSpaceHeight - titleView.getHeight());

        //int titleTranslationY = (int) (maxTitleTranslationY * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight);
        int titleTranslationY = Math.max(maxTitleTranslationY - scrollY, 0);
        titleView.setTranslationY(titleTranslationY);
        itemTypeIconView.setTranslationY(titleTranslationY + titleView.getHeight()/2 - itemTypeIconView.getHeight()/2);

        if(scaleBase > scaleFrom){
            if(!isItemTypeIconHide){
                ViewPropertyAnimator vpa = itemTypeIconView.animate();
                vpa.cancel();
                vpa.scaleX(0).scaleY(0).setDuration(200).start();
                isItemTypeIconHide = true;
            }
        }else{
            if(isItemTypeIconHide){
                ViewPropertyAnimator vpa = itemTypeIconView.animate();
                vpa.cancel();
                vpa.scaleX(1).scaleY(1).setDuration(200).start();
                isItemTypeIconHide = false;
            }
        }

        breadcrumbView.setTranslationY(titleTranslationY - breadcrumbHeight);

        //mOverlayView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        //mImageView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        descView.setTranslationY(-scrollY);
        tabWrapper.setTranslationY(-scrollY);
        //mListView.setScrollY(scrollY);
        //mOverlayView.setTranslationY(-scrollY);
        //mImageView.setTranslationY(-scrollY);

        //Log.d("scale", String.valueOf(maxTitleTranslationY));
        //Log.d("scale", String.valueOf(titleTranslationY));

        /*
        Log.d("scale", String.valueOf(scale));
        Log.d("maxtitle", String.valueOf(maxTitleTranslationY));
        Log.d("title-height", String.valueOf(h));
        Log.d("title-inlin-height", String.valueOf(mTitleView.getLineHeight()));

        Log.d("scroll", String.valueOf(scrollY));
        */


        /*
        if(scrollY == 1175){
            Log.d("dragging", String.valueOf(dragging));
            mScrollView.setScrollY(1);

        }
        */
        scrollFromTab = false;
    }

    public class ObservableScrollCallback implements ObservableScrollViewCallbacks {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            //Log.d("scroll", String.valueOf(scrollY));
            View view = (View)scrollView.getChildAt(scrollView.getChildCount() - 1);
            int diff = (view.getBottom()-(scrollView.getHeight() + scrollView.getScrollY()));

            //Log.d("diff", String.valueOf(diff));

            if(diff <= 0){
                Log.d("scroll end", "enedd");
                isScrollEnd = true;
                //updateScrolling(scrollY);

            }else{
                isScrollEnd = false;
                updateScrolling(scrollY);
            }
        }

        @Override
        public void onDownMotionEvent() {

        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        }

    }

    public static class CustomTouchListener implements View.OnTouchListener {

        private StickyScrollPresenter presenter;
        private int lastY;
        private int diff;

        public CustomTouchListener(StickyScrollPresenter p){
            presenter = p;
            Log.d("TouchListener", "called");
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Boolean result = true;
            if(presenter.isScrollEnd){
                // tabがtoolbar直下に来てる場合
                // tabがtoolbar直下で且つlistviewなどが一番上までスクロールしてた場合、
                // tabごとスクロールさせる
                if(!v.canScrollVertically(-1)){
                    lastY = (int)event.getRawY();
                    Log.d("scrolled_listview", "reach top");
                    presenter.scroll(-5);
                }
                result = v.onTouchEvent(event);
            }else {
                // tabがtoolbar直下には来てない場合
                // listviewなどをスクロールしてもそのviewをスクロールさせずに
                // 兄弟要素のobservableScrollViewをスクロールさせる

                if (MotionEvent.ACTION_MOVE == event.getAction()) {
                    //Log.d("action type", "move");
                    //Log.d("point move", String.valueOf(lastY - (int) event.getY()));
                    diff = lastY - (int) event.getRawY();
                    Log.d("move raw Y", String.valueOf(event.getRawY()));
                    Log.d("diff raw Y", String.valueOf(diff));
                    //presenter.scroll(diff);
                    //v.getParent().requestDisallowInterceptTouchEvent(true);
                    //presenter.updateScrolling((int)event.getRawY(), false);
                    //Log.d("scrolling", String.valueOf(presenter.scrolling));
                    if(diff >= 0){
                        presenter.scrollDirection = true;
                    }else{
                        presenter.scrollDirection = false;
                    }
                    presenter.scrollFromTab = true;
                    presenter.scrollView.dispatchTouchEvent(event);
                    //Log.d("scrolling", String.valueOf(presenter.scrolling));
                    lastY = (int) event.getRawY();

                } else if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    presenter.scrollView.dispatchTouchEvent(event);

                    Log.d("action type", "down");
                    lastY = (int) event.getRawY();
                    Log.d("point", String.valueOf(lastY));
                } else if (MotionEvent.ACTION_UP == event.getAction()){
                    presenter.scrollView.dispatchTouchEvent(event);
                }
            }

            //return (event.getAction() == MotionEvent.ACTION_MOVE);
            //return v.onTouchEvent(event);
            return result;
        }
    }
}
