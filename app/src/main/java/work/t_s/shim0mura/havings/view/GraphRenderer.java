package work.t_s.shim0mura.havings.view;

import android.graphics.Color;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2015/12/12.
 */
public class GraphRenderer {

    public static void renderSimpleGraph(LineChartView lineChartView, List<CountDataEntity> countData){

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

}
