package com.example.demoicsa.charts;

import android.graphics.Matrix;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.anychart.AnyChartView;

public class CollapsibleChartItem  {

    // The item's AnyChartView
    private AnyChartView chart;

    // The item's spinner
    private ProgressBar spinner;

    // The item's layout
    private LinearLayout layout;

    // The item's arrow
    private ImageView arrow;

    // The item's matrix
    private Matrix matrix;

    // Height variables
    private int minHeight;
    private int maxHeight;

    public CollapsibleChartItem(LinearLayout layout, ImageView arrow){
        this.layout = layout;
        this.arrow = arrow;
        this.matrix = new Matrix();
    }

    private void init(){
        arrow.setScaleType(ImageView.ScaleType.MATRIX);
    }

}
