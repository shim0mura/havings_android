package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.NavigateEvent;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2015/12/12.
 */
public class GraphRenderer {

    private static int pieChartPartialView = R.layout.partial_item_percentage_detail;

    private static int PIECHART_LABEL_FONT_SIZE = 15;
    private static int PIECHART_SUBTEXT_FONT_SIZE = 16;
    private static int PIECHART_MAINTEXT_FONT_SIZE = 25;
    private static int PIECHART_DETAIL_STANDARD_FONT_SIZE = 14;

    public static void renderSimpleLineGraph(LineChartView lineChartView, List<CountDataEntity> countData){

        if(countData.size() <= 0){
            return;
        }

        if(shouldAddTodayData(countData.get(countData.size()-1).date)){
            addTodayData(countData);
        }

        List<PointValue> yValues = new ArrayList<PointValue>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int x = 0; x < countData.size(); ++x) {
            CountDataEntity dataItem = countData.get(x);
            //DateTime recordedAt = dataItem.date.getRecordedAt();
            //String formattedRecordedAt = DateTimeFormatter.getFormattedDateTime(recordedAt);
            int yValue = dataItem.count;
            yValues.add(new PointValue(dataItem.date.getTime(), yValue));
            AxisValue axisValue = new AxisValue(dataItem.date.getTime());

            axisValue.setLabel(ViewUtil.dateToString(dataItem.date, false));
            axisValues.add(axisValue);
        }


        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(yValues).setColor(Color.BLUE).setCubic(false);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisX = new Axis(axisValues);
        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("モノの数");
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(5);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        lineChartView.setLineChartData(data);

        final Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.bottom = 0;
        v.top = v.top + 10;
        v.left = v.left - 10;
        v.right = v.right + 10;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);

    }

    private static Boolean shouldAddTodayData(Date lastDate){
        DateTime lastDt = new DateTime(lastDate);
        DateTime today = new DateTime();
        if(Days.daysBetween(lastDt, today).getDays() > 0){
            return true;
        }else{
            return false;
        }
    }

    private static void addTodayData(List<CountDataEntity> counts){
        CountDataEntity c = new CountDataEntity();
        c.count = counts.get(counts.size() - 1).count;
        c.date = new Date();
        counts.add(c);
    }

    public static void renderPieChart(Context context, PieChartView pieChartView, LinearLayout detailWrapper, ArrayList<ItemPercentageEntity> itemPercentageEntities){
        pieChartView.setOnValueTouchListener(new GraphRenderer.ValueTouchListener());

        List<SliceValue> values = new ArrayList<SliceValue>();
        int totalCount = 0;

        for(ItemPercentageEntity ipe : itemPercentageEntities) {
            totalCount += ipe.count;

            SliceValue s = new SliceValue((float)ipe.count, ContextCompat.getColor(context, ItemPercentageEntity.categoryColor.get(ipe.type)));

            StringBuilder sb = new StringBuilder();
            sb.append(ItemPercentageEntity.categoryName.get(ipe.type));
            sb.append(" - ");
            //sb.append(String.valueOf(ipe.count));
            sb.append("(");
            sb.append(String.valueOf(ipe.percentage));
            sb.append("%)");

            s.setLabel(new String(sb));
            //s.setLabel("衣 - 10items(30%)");
            values.add(s);
        }

        PieChartData data = new PieChartData(values);

        data.setValueLabelTextSize(PIECHART_LABEL_FONT_SIZE);

        data.setHasCenterCircle(true);
        data.setCenterText1FontSize(PIECHART_SUBTEXT_FONT_SIZE);
        data.setCenterText1(context.getString(R.string.item_total_count));
        data.setCenterText2FontSize(PIECHART_MAINTEXT_FONT_SIZE);
        data.setCenterText2(String.valueOf(totalCount));

        pieChartView.setValueSelectionEnabled(true);

        data.setHasLabels(true);
        pieChartView.setPieChartData(data);

        detailWrapper.removeAllViews();
        for(ItemPercentageEntity ipe : itemPercentageEntities){
            detailWrapper.addView(renderPieChartCategory(context, ipe));
        }
    }

    public static void renderPieChartDetail(Context context, LinearLayout detailWrapper, ItemPercentageEntity itemPercentageEntity){
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View header = renderPieChartCategory(context, itemPercentageEntity);
        if(header == null){
            return;
        }

        detailWrapper.removeAllViews();
        detailWrapper.addView(header);

        for(ItemPercentageEntity ipe : itemPercentageEntity.childs){
            View parentView = layoutInflater.inflate(pieChartPartialView, null);
            ImageView parentIcon = (ImageView)parentView.findViewById(R.id.item_category_icon);
            View depthPadding = parentView.findViewById(R.id.category_depth_padding1);
            TextView parentName = (TextView)parentView.findViewById(R.id.item_category);
            TextView parentItemCount = (TextView)parentView.findViewById(R.id.item_count);
            TextView parentItemPercentage = (TextView)parentView.findViewById(R.id.item_percentage);

            parentIcon.setVisibility(View.GONE);
            depthPadding.setVisibility(View.VISIBLE);

            parentName.setText(ipe.tag);
            parentItemCount.setText(String.valueOf(ipe.count));
            parentItemPercentage.setText(String.valueOf(ipe.percentage));

            detailWrapper.addView(parentView);

            for(ItemPercentageEntity childIpe : ipe.childs){
                View childView = layoutInflater.inflate(pieChartPartialView, null);
                ImageView childIcon = (ImageView)childView.findViewById(R.id.item_category_icon);
                View childPadding = childView.findViewById(R.id.category_depth_padding2);
                TextView childName = (TextView)childView.findViewById(R.id.item_category);
                TextView childItemCount = (TextView)childView.findViewById(R.id.item_count);
                TextView childItemPercentage = (TextView)childView.findViewById(R.id.item_percentage);

                childIcon.setVisibility(View.GONE);
                childPadding.setVisibility(View.VISIBLE);

                childName.setText(childIpe.tag);
                childName.setTypeface(null, Typeface.NORMAL);
                childName.setTextSize(TypedValue.COMPLEX_UNIT_SP, PIECHART_DETAIL_STANDARD_FONT_SIZE);
                childItemCount.setText(String.valueOf(childIpe.count));
                childItemCount.setTypeface(null, Typeface.NORMAL);
                childItemCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, PIECHART_DETAIL_STANDARD_FONT_SIZE);
                childItemPercentage.setText(String.valueOf(childIpe.percentage));
                childItemPercentage.setTypeface(null, Typeface.NORMAL);

                detailWrapper.addView(childView);
            }
        }
    }

    public static View renderPieChartCategory(Context context, ItemPercentageEntity itemPercentageEntity){

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View categoryView = layoutInflater.inflate(pieChartPartialView, null);
        ImageView categoryIcon = (ImageView)categoryView.findViewById(R.id.item_category_icon);
        TextView categoryName = (TextView)categoryView.findViewById(R.id.item_category);
        TextView categoryItemCount = (TextView)categoryView.findViewById(R.id.item_count);
        TextView categoryItemPercentage = (TextView)categoryView.findViewById(R.id.item_percentage);

        switch(itemPercentageEntity.type){
            case 1:
                categoryIcon.setImageResource(R.drawable.clothing);
                break;
            case 2:
                categoryIcon.setImageResource(R.drawable.food);
                break;
            case 3:
                categoryIcon.setImageResource(R.drawable.living);
                break;
            default:
                categoryIcon.setImageResource(R.drawable.item_etc);
                break;
        }

        categoryView.setBackgroundColor(ContextCompat.getColor(context, ItemPercentageEntity.categoryColor.get(itemPercentageEntity.type)));
        categoryName.setText(ItemPercentageEntity.categoryName.get(itemPercentageEntity.type));
        categoryItemCount.setText(String.valueOf(itemPercentageEntity.count));
        categoryItemPercentage.setText(String.valueOf(itemPercentageEntity.percentage));

        return categoryView;
    }

    public static class ValueTouchListener implements PieChartOnValueSelectListener {

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
            Timber.d("arcIndex %s, value %s", arcIndex, value);
            BusHolder.get().post(new GenericEvent(arcIndex));
        }

        @Override
        public void onValueDeselected() {

        }

    }

}
