package work.t_s.shim0mura.havings;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;

public class TestGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Activity act = this;

        lecho.lib.hellocharts.view.LineChartView chart1 = (lecho.lib.hellocharts.view.LineChartView)findViewById(R.id.chart);

        chart1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Boolean r = v.onTouchEvent(event);
                Log.d("test_touch", "x: " + event.getX() + ",y : "+ event.getY());
                Log.d("test_touch", "x: " + event.getRawX() + ",y : "+ event.getRawY());

                View popup = getLayoutInflater().inflate(R.layout.test_popup, null);

                ViewGroup.LayoutParams lp = popup.getLayoutParams();

                Log.d("test_touch", "w:"+popup.getMeasuredWidth()+",h:"+popup.getMeasuredHeight());

                //http://stackoverflow.com/questions/13076377/imageview-getlayoutparams-returns-null
                //位置調整のためにpopupのサイズを知りたいけど、この時点で知るのは無理っぽい
                //結局画像入れたりテキスト入れたりはこの段階で行うので、その結果を自分で計算して調整したほうがてっとり早そう
                //位置合わせの参考
                //https://github.com/diogobernardino/WilliamChart/blob/bf5e9a38929d7bd2b9f1d0f4ffb3ca698a2bfbf0/library/src/com/db/chart/view/Tooltip.java
                //106行目くらい
                popup.setPadding((int)event.getX(), (int)event.getY(), 0, 0);

                FrameLayout f = (FrameLayout)act.findViewById(R.id.popup);
                f.addView(popup);
                return r;
            }
        });
        chart1.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Log.d("test", pointValue.toString());
            }

            @Override
            public void onValueDeselected() {

            }
        });

        List<PointValue> values = new ArrayList<PointValue>();
        values.add(new PointValue(0, 2));
        values.add(new PointValue(1, 4));
        values.add(new PointValue(2, 3));
        values.add(new PointValue(3, 4));

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.BLUE).setCubic(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        chart1.setLineChartData(data);

        /*
        LineChartView chart1 = (LineChartView)findViewById(R.id.chart);

        final String[] mLabelsOne= {"", "10-15", "", "15-20", "", "20-25", "", "25-30", "", "30-35", ""};
        final float[][] mValuesOne = {{3.5f, 4.7f, 4.3f, 8f, 6.5f, 10f, 7f, 8.3f, 7.0f, 7.3f, 5f},
                {2.5f, 3.5f, 3.5f, 7f, 5.5f, 8.5f, 6f, 6.3f, 5.8f, 6.3f, 4.5f},
                {1.5f, 2.5f, 2.5f, 4f, 2.5f, 5.5f, 5f, 5.3f, 4.8f, 5.3f, 3f}};

        chart1.setTooltips(new Tooltip(this, R.layout.test_popup));
        */
        /*
        chart1.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                Log.d("test", "clicked");
                Log.d("test", "x:"+rect.left+", y:"+rect.top+", w:"+rect.width()+",h:"+rect.height());
                TextView mTooltipThree = (TextView) getLayoutInflater().inflate(R.layout.test_popup, null);
                //mTooltipThree.setText(Integer.toString((int) mValuesOne[0][entryIndex]));
                Log.d("sss", mTooltipThree.getText().toString());
                mTooltipThree.setText("aaaaaaaaa");

                //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(rect.width(), rect.height());
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.leftMargin = rect.left;
                layoutParams.topMargin = rect.top;
                mTooltipThree.setLayoutParams(layoutParams);


                final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    mTooltipThree.setAlpha(0);
                    mTooltipThree.setScaleY(0);
                    mTooltipThree.animate()
                            .setDuration(200)
                            .alpha(1)
                            .scaleY(1)
                            .setInterpolator(enterInterpolator);
                }



            }
        });

        */

        /*
        LineSet dataset = new LineSet(mLabelsOne, mValuesOne[0]);
        dataset.setColor(Color.parseColor("#a34545"))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(Color.parseColor("#FF365EAF"))
                .setDotsColor(Color.parseColor("#eef1f6"));
        chart1.addData(dataset);


        chart1.setBorderSpacing(Tools.fromDpToPx(0))
                .setXLabels(AxisController.LabelPosition.INSIDE)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setLabelsColor(Color.parseColor("#e08b36"))
                .setXAxis(false)
                .setYAxis(false);

        //Animation anim = new Animation().setStartPoint(-1, 1).setEndAction(action);

        chart1.show();
        */


        /*
        chart = (LineChart) findViewById(R.id.chart);

        chart.setTouchEnabled(true);

        MyMarkerView mv = new MyMarkerView(this, R.layout.item_metadata);
        chart.setMarkerView(mv);
        setData(10, 20);
        */

        /*
        GraphView graph = (GraphView) findViewById(R.id.graph);
        final LineGraphSeries<DataPoint> series = new TestGraph(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });

        graph.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    float screenX = event.getX();
                    float screenY = event.getY();
                    float width_x = v.getWidth();
                    float viewX = screenX - v.getLeft();
                    float viewY = screenY - v.getTop();
                    float percent_x = (viewX/width_x);

                    Log.d("touch", "X: " + viewX + " Y: " + viewY +" Percent = " +percent_x + ", YVal=" );

                    return v.onTouchEvent(event);
                }
                return false;
            }

        });
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series seriess, DataPointInterface dataPoint) {
                Log.d("sss", String.valueOf(seriess.equals(series)));
                Log.d("callback", String.valueOf(dataPoint.getX()));
                Log.d("callback", String.valueOf(dataPoint.getY()));
            }
        });
        graph.addSeries(series);
        */
    }


}
