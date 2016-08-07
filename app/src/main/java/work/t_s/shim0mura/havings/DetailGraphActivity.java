package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.DetailGraphPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.EventListByDayAdapter;
import work.t_s.shim0mura.havings.view.GraphRenderer;

public class DetailGraphActivity extends DrawerActivity {

    private static final String SERIALIZED_ITEM = "SerializedItem";

    private DetailGraphPresenter detailGraphPresenter;
    private ItemEntity item;
    private List<CountDataEntity> countData;
    private LineChartView chart;
    private EventListByDayAdapter adapter;

    private PreviewLineChartView previewChart;
    private View popup;
    private ListView eventListView;
    //private EventListInPopupAdapter adapter;
    private FrameLayout popupWrapper;

    private int chartHeight;
    private int chartWidth;
    private int popupHeight;
    private int popupWidth;

    private float touchX;
    private float touchY;

    private Boolean valueSelected = false;
    private int selectedPointIndex = 0;

    private View vvv;

    @Bind(R.id.event_history) ListView eventHistory;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        popupHeight = ViewUtil.dpToPix(this, 150);
        popupWidth = ViewUtil.dpToPix(this, 150);

        final Activity act = this;
        setTitle(item.name + "のグラフ");

        detailGraphPresenter = new DetailGraphPresenter(this);
        //detailGraphPresenter.getShowingEvent(item.id);

        ButterKnife.bind(this);

        //popupWrapper = (FrameLayout)findViewById(R.id.popup);
        chart = (LineChartView)findViewById(R.id.chart);
        chart.setValueSelectionEnabled(true);

        GraphRenderer.renderSimpleLineGraph(chart, item.countProperties);

        adapter = new EventListByDayAdapter(this, R.layout.partial_recent_activity_wrapper, item.countProperties);
        eventHistory.setAdapter(adapter);
        Timber.d("size %s", item.countProperties.size());


        chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, final int pointIndex, PointValue pointValue) {
                Timber.d("point %s, position %s, size %s", pointIndex, item.countProperties.size() - pointIndex, item.countProperties.size());
                //eventHistory.smoothScrollToPosition(item.countProperties.size() - pointIndex - 1);

                if(vvv != null){
                    vvv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryTextWhite));
                }
                int firstListItemPosition = eventHistory.getFirstVisiblePosition();
                int lastListItemPosition = firstListItemPosition + eventHistory.getChildCount() - 1;
                int pos = item.countProperties.size() - pointIndex - 1;
                if (pos < firstListItemPosition || pos > lastListItemPosition ) {
                    vvv = eventHistory.getAdapter().getView(pos, null, eventHistory);
                    vvv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                } else {
                    int childIndex = pos - firstListItemPosition;
                    vvv = eventHistory.getChildAt(childIndex);
                    vvv.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                }
                Timber.d((String)vvv.getTag(R.id.TEST_TAG));
                //eventHistory.smoothScrollToPositionFromTop(item.countProperties.size() - pointIndex - 1, (eventHistory.getHeight()/2 - vvv.getHeight()/2));
                eventHistory.setSelection(pos);

                //v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            }

            @Override
            public void onValueDeselected() {
            }
        });

        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);

        //previewChart.setViewportChangeListener(new ViewportListener());

        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        //previewChart.setCurrentViewport(tempViewport);
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
