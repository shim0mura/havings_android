package work.t_s.shim0mura.havings.view;

import android.graphics.Color;

import org.joda.time.DateTime;
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

/**
 * Created by shim0mura on 2015/12/12.
 */
public class GraphRenderer {

    public static void renderSimpleGraph(LineChartView lineChartView, List<CountDataEntity> countData){

        /*
        List<PointValue> values = new ArrayList<PointValue>();
        values.add(new PointValue(0, 2));
        values.add(new PointValue(1, 4));
        values.add(new PointValue(2, 3));
        values.add(new PointValue(3, 4));
        */
        CountDataEntity c = new CountDataEntity();
        c.count = 40;
        c.date = new Date();
        countData.add(c);

        List<PointValue> yValues = new ArrayList<PointValue>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int x = 0; x < countData.size(); ++x) {
            CountDataEntity dataItem = countData.get(x);
            //DateTime recordedAt = dataItem.date.getRecordedAt();
            //String formattedRecordedAt = DateTimeFormatter.getFormattedDateTime(recordedAt);
            int yValue = dataItem.count;
            yValues.add(new PointValue(dataItem.date.getTime(), yValue));
            AxisValue axisValue = new AxisValue(dataItem.date.getTime());

            StringBuilder s = new StringBuilder();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataItem.date);
            s.append(calendar.get(Calendar.YEAR)).append('/')
                    .append(calendar.get(Calendar.MONTH) + 1).append('/')
                    .append(calendar.get(Calendar.DAY_OF_MONTH)).append(' ');

            axisValue.setLabel(s.toString());
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
        axisX.setMaxLabelChars(10);
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

}
