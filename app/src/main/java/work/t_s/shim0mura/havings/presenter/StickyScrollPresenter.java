package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

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
    private ObservableScrollView scrollView;
    private View toolbarView;
    private TextView titleView;
    private View descView;
    private View tabWrapper;
    private int flexibleSpaceHeight;
    private int toolbarHeight;

    protected String TAG = "StickyScrollPresenter: ";

    public StickyScrollPresenter(Context c){
        activity = (Activity)c;

        //asm = ApiServiceManager.getSingleton(activity);

        scrollView = (ObservableScrollView)activity.findViewById(R.id.scroll);
        toolbarView = activity.findViewById(R.id.toolbar);
        titleView = (TextView)activity.findViewById(R.id.title);
        descView = activity.findViewById(R.id.desc);
        tabWrapper = activity.findViewById(R.id.tab_wrapper);

        flexibleSpaceHeight = activity.getResources().getDimensionPixelSize(R.dimen.flexible_space_height);
        toolbarHeight = toolbarView.getHeight();

        scrollView.setScrollViewCallbacks(new ObservableScrollCallback());

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

        Log.d("scrollY", String.valueOf(scrollY));
        //mOverlayView.setAlpha(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));
        float scaleBase = Math.min(1, (float) scrollY / flexibleSpaceHeight - toolbarHeight);
        //mOverlayView.setAlpha(al);
        //mToolbarView.getBackground().setAlpha(1);
        toolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(scaleBase, ContextCompat.getColor(activity, R.color.colorPrimary)));

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

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scale * 20);

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
    }

    public class ObservableScrollCallback implements ObservableScrollViewCallbacks {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            Log.d("scroll", String.valueOf(scrollY));
            View view = (View)scrollView.getChildAt(scrollView.getChildCount() - 1);
            int diff = (view.getBottom()-(scrollView.getHeight() + scrollView.getScrollY()));

            Log.d("diff", String.valueOf(diff));

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
            if(isScrollEnd){
                //Log.d("scroll ended", String.valueOf(mScrollView.getCurrentScrollY()));
                //mListView.setPressed(true);
                //mListView.setScrollY(0);

            }
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
                if(!v.canScrollVertically(-1)){
                    lastY = (int)event.getRawY();
                    Log.d("scrolled_listview", "reach top");
                    presenter.scroll(-5);
                }
                result = v.onTouchEvent(event);
            }else {
                if (MotionEvent.ACTION_MOVE == event.getAction()) {
                    //Log.d("action type", "move");
                    //Log.d("point move", String.valueOf(lastY - (int) event.getY()));
                    diff = lastY - (int) event.getRawY();
                    Log.d("move raw Y", String.valueOf(event.getRawY()));
                    Log.d("diff raw Y", String.valueOf(diff));
                    presenter.scroll(diff);
                    //presenter.updateScrolling((int)event.getRawY(), false);
                    lastY = (int) event.getRawY();
                } else if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    Log.d("action type", "down");
                    lastY = (int) event.getRawY();
                    Log.d("point", String.valueOf(lastY));
                }
            }

            //return (event.getAction() == MotionEvent.ACTION_MOVE);
            //return v.onTouchEvent(event);
            return result;
        }
    }
}
