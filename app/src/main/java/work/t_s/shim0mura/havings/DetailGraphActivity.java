package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.DetailGraphPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.EventListInPopupAdapter;
import work.t_s.shim0mura.havings.view.GraphRenderer;

public class DetailGraphActivity extends AppCompatActivity {

    private static final String TAG = "detailGraphActivity:";

    private static final String SERIALIZED_ITEM = "SerializedItem";

    private DetailGraphPresenter detailGraphPresenter;
    private ItemEntity item;
    private List<CountDataEntity> countData;
    private LineChartView chart;
    private PreviewLineChartView previewChart;
    private View popup;
    private ListView eventListView;
    private EventListInPopupAdapter adapter;
    private FrameLayout popupWrapper;

    private int chartHeight;
    private int chartWidth;
    private int popupHeight;
    private int popupWidth;

    private float touchX;
    private float touchY;

    private Boolean valueSelected = false;
    private int selectedPointIndex = 0;


    public static void startActivity(Context context, ItemEntity item){
        Intent intent = new Intent(context, DetailGraphActivity.class);
        intent.putExtra(SERIALIZED_ITEM, item);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);

        setContentView(R.layout.activity_detail_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        popupHeight = ViewUtil.dpToPix(this, 150);
        popupWidth = ViewUtil.dpToPix(this, 150);

        final Activity act = this;
        setTitle(item.name + "のグラフ");

        detailGraphPresenter = new DetailGraphPresenter(this);
        detailGraphPresenter.getShowingEvent(item.id);

        popupWrapper = (FrameLayout)findViewById(R.id.popup);
        chart = (LineChartView)findViewById(R.id.chart);
        previewChart = (PreviewLineChartView)findViewById(R.id.chart_preview);

        popup = getLayoutInflater().inflate(R.layout.test_popup, popupWrapper, false);

        eventListView = (ListView)popup.findViewById(R.id.event_list);
        adapter = new EventListInPopupAdapter(act, R.layout.event_list_in_popup);
        eventListView.setAdapter(adapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventEntity tag = (EventEntity)view.getTag(R.string.tag_event);

                switch(tag.eventType){
                    case EventEntity.EVENT_TYPE_ADD_IMAGE:
                        ImageDetailActivity.startActivity(act, item, tag.item.thumbnail, tag.date);
                        break;
                    case EventEntity.EVENT_TYPE_ADD_ITEM:
                        ItemActivity.startActivity(act, item.id);
                        break;
                    case EventEntity.EVENT_TYPE_ADD_LIST:
                        ItemActivity.startActivity(act, item.id);
                        break;
                    case EventEntity.EVENT_TYPE_DUMP_ITEM:
                        ItemActivity.startActivity(act, item.id);
                        break;
                    default:
                        //ItemActivity.startActivity(act, tag);
                        break;
                }
            }
        });

        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Boolean r = v.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    touchX = event.getX();
                    touchY = event.getY();
                }

                return r;
            }
        });

        chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue pointValue) {
                Log.d("test", pointValue.toString());
                Log.d("event position", "i:"+lineIndex+", i1:"+pointIndex);
                selectedPointIndex = pointIndex;
                valueSelected = true;
            }

            @Override
            public void onValueDeselected() {
                Log.d("test deselect", "sss");
            }
        });

        chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onTouch, onValueTouch, onClickの順で呼ばれる
                //onTouchでしかpositionは取得できないのでACTION_UPのタイミングでx,y取得
                //upがあって且つonValueSelectのばあいに
                Log.d("test click", "click");

                if(valueSelected){
                    Log.d("value selected", "true");
                    if(countData.size() > selectedPointIndex){
                        CountDataEntity data = countData.get(selectedPointIndex);
                        setPopupContent(data);
                        adapter.changeEvents(data.events);
                        adapter.setDate(data.date);
                        if(data.events.size() > 0){
                            eventListView.setVisibility(View.VISIBLE);
                        }else{
                            eventListView.setVisibility(View.GONE);
                        }
                    }else{
                        adapter.resetEvents();
                        eventListView.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();

                    int x = 0;
                    int y = 0;
                    if(touchX > popupWidth){
                        x = (int)touchX - popupWidth - 10;
                    }else{
                        x = (int)touchX + 10;
                    }
                    if(touchY + popupHeight > chartHeight){
                        Timber.d("touchY: %s, popupHeight: %s, chartHeight: %s", touchY, popupHeight, chartHeight);
                        y = (int)touchY - ((int)touchY + popupHeight - chartHeight);
                    }else{
                        Timber.d("touchY: %s, popupHeight: %s, chartHeight: %s", touchY, popupHeight, chartHeight);

                        y = (int)touchY - 50;
                    }
                    popup.setX(x);
                    popup.setY(y);

                    showPopup();
                    valueSelected = false;
                }else{
                    hidePopup();
                }

            }
        });

        chart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= 16) {
                    chart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    chart.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                chartHeight = chart.getHeight();
                chartWidth = chart.getWidth();

                ViewGroup.LayoutParams lp = popup.getLayoutParams();

                popupWrapper.addView(popup);

                hidePopup();
            }
        });

        // prepare preview data, is better to use separate deep copy for preview chart.
        // Set color to grey to make preview area more visible.
        GraphRenderer.renderSimpleGraph(chart, item.countProperties);
        GraphRenderer.renderSimpleGraph(previewChart, item.countProperties);

        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);

        previewChart.setViewportChangeListener(new ViewportListener());

        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        previewChart.setCurrentViewport(tempViewport);

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);

        super.onPause();
    }

    private void hidePopup(){
        popup.setVisibility(View.GONE);
        valueSelected = false;
    }

    private void showPopup(){
        popup.setVisibility(View.VISIBLE);
    }

    private void setPopupContent(CountDataEntity data){
        ImageView image = (ImageView)popup.findViewById(R.id.item_image);
        String thumbnail = null;
        int size = data.events.size();
        for(int i = 0; i < size; i++){
            if(data.events.get(i).item.thumbnail != null){
                thumbnail = data.events.get(i).item.thumbnail;
                break;
            }
        }
        if(thumbnail != null){
            thumbnail = ApiService.BASE_URL + thumbnail;
            Glide.with(this).load(thumbnail).into(image);
            image.setVisibility(View.VISIBLE);
        }else{
            image.setImageResource(android.R.color.transparent);
            image.setVisibility(View.GONE);
        }

        TextView popupDate = (TextView)popup.findViewById(R.id.popup_date);
        popupDate.setText(ViewUtil.dateToString(data.date, true));
        TextView popupCount = (TextView)popup.findViewById(R.id.popup_item_count);
        popupCount.setText(String.valueOf(data.count));
    }

    @Subscribe
    public void setEventData(ItemEntity item){
        countData = item.countProperties;
    }

    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            chart.setCurrentViewport(newViewport);
            hidePopup();
        }

    }

}
