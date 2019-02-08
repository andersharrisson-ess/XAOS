/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2018-2019 by European Spallation Source ERIC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Demo;

import chart.AreaChartFX;
import chart.NumberAxis;
import chart.data.DataReducingSeries;
import chart.LogAxis;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.ValueAxis;
import plugins.CoordinatesLabel;
import plugins.CoordinatesLines;
import plugins.DataPointTooltip;
import plugins.Pan;
import plugins.Zoom;
import plugins.CursorTool;
import plugins.KeyPan;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
/**
 *
 * @author reubenlindroos
 */
public class AreaChartGenerator {
    
    private DataReducingSeries<Number, Number> series0;
    private DataReducingSeries<Number, Number> series1;
    private DataReducingSeries<Number, Number> series2;
    private AreaChartFX<Number,Number> chart ;
    
    private ValueAxis xAxis = new NumberAxis();
    private ValueAxis yAxis = new NumberAxis();
 
    private static Random RANDOM = new Random(System.currentTimeMillis());
    
   
  public AreaChartFX getChart(Integer NB_OF_POINTS) {
        if (chart == null) {
        generateChart(NB_OF_POINTS);
    } 
    //
    return chart; }
    
    
    
    public void generateChart(Integer NB_OF_POINTS) {
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

        chart = new AreaChartFX<Number, Number>(xAxis, yAxis);
        chart.setTitle("Test data");
        chart.setAnimated(false);

        chart.getChartPlugins().addAll(new CursorTool(), new KeyPan(), new CoordinatesLines(), 
               new Zoom(), new Pan(), new CoordinatesLabel(), new DataPointTooltip() );
        
        if (series0==null){
        series0 = new DataReducingSeries<>();
        series0.setName("Generated test data-horizontal");
        series0.setData(generateData(NB_OF_POINTS));
        
        
        series1 = new DataReducingSeries<>();
        series1.setName("Generated test data-vertical");
        series1.setData(generateData(NB_OF_POINTS));
        
        
        series2 = new DataReducingSeries<>();
        series2.setName("Generated test data-longitudinal");
        series2.setData(generateData(NB_OF_POINTS));}
        
        
        chart.getData().add(series0.getSeries());
        chart.getData().add(series1.getSeries());
        chart.getData().add(series2.getSeries());
        chart.setSeriesAsHorizontal(0);//red
        chart.setSeriesAsVertical(1);//blue
        chart.setSeriesAsLongitudinal(2);//horrible green
    }
 
    private static ObservableList<XYChart.Data<Number, Number>> generateData(int nbOfPoints) {
        int[] yValues = generateIntArray(0, 5, nbOfPoints);
        List<XYChart.Data<Number, Number>> data = new ArrayList<>(nbOfPoints);
        for (int i = 0; i < yValues.length; i++) {
            data.add(new XYChart.Data<Number, Number>(i, yValues[i]));
        }
        return FXCollections.observableArrayList(data);
    }
    public static int[] generateIntArray(int firstValue, int variance, int size) {
        int[] data = new int[size];
        data[0] = firstValue;
        for (int i = 1; i < data.length; i++) {
            int sign = RANDOM.nextBoolean() ? 1 : -1;
            data[i] = data[i - 1] + (int) (variance * RANDOM.nextDouble()) * sign;
        }
        return data;
    }

    public AreaChartFX setYLogAxis(Integer nb_of_points) {
        yAxis = new LogAxis();
        generateChart(nb_of_points);
        return chart;
    }
      public AreaChartFX setXLogAxis(Integer nb_of_points) {
        xAxis = new LogAxis();
        generateChart(nb_of_points);
        return chart;
    }
      public AreaChartFX resetAxes(Integer nb_of_points) {
          xAxis = new NumberAxis();
          yAxis = new NumberAxis();
          generateChart(nb_of_points);
          return chart;
      }

}