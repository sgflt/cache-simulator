/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

/**
 *
 * @author Pavel Bzoch
 * Trida pro vykresleni sloupcoveho grafu
 */
@SuppressWarnings("serial")
public class BarChart extends JFrame {
    
    /**
     * konstruktor - iniciace promennych a vykresleni okna
     * @param title  the frame title.
     * @param sc data
     * @param dataSelect co chceme vykreslit 
     */
    public BarChart(final String title, final UserStatistics sc, final int dataSelect) {
        super("Cache " + title);

        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/ico/results.png")).getImage());
           
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout(0, 5));
    
        final CategoryDataset dataset = createDataset(sc, dataSelect);
        final JFreeChart chart = createChart(dataset, "Cache " + title, title);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setDisplayToolTips(true);
        this.setContentPane(chartPanel);       
     }

    /**
     * Metoda pro vytvoreni datasetu podle dat
     * @return dataset
     */
    private static CategoryDataset createDataset(final UserStatistics sc, final int dataSelect) {
        
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final Integer[] cacheSizes = sc.getCacheSizes();
        final String[] cacheNames = sc.getCacheNames();
        
        switch (dataSelect){
            case 0:
                Double[] vysledkyHitRatio;
                for (final String cacheName : cacheNames) {
                    vysledkyHitRatio = sc.getCacheHitRatios(cacheName);
                    for (int i = 0; i < vysledkyHitRatio.length; i++){
                        dataset.addValue(vysledkyHitRatio[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 1:
                Long[] vysledkyHitCount;
                for (final String cacheName : cacheNames) {
                    vysledkyHitCount = sc.getCacheHits(cacheName);
                    for (int i = 0; i < vysledkyHitCount.length; i++){
                        dataset.addValue(vysledkyHitCount[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 2:          
                Double[] vysledkySavedRatio;
                for (final String cacheName : cacheNames) {
                    vysledkySavedRatio = sc.getCacheSavedBytesRatio(cacheName);
                    for (int i = 0; i < vysledkySavedRatio.length; i++){
                        dataset.addValue(vysledkySavedRatio[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 3:
                Long[] vysledkySavedBytes;
                for (final String cacheName : cacheNames) {
                    vysledkySavedBytes = sc.getCacheHits(cacheName);
                    for (int i = 0; i < vysledkySavedBytes.length; i++){
                        dataset.addValue(vysledkySavedBytes[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 4:          
                Double[] vysledkyTransfDecreaseRat;
                for (final String cacheName : cacheNames) {
                    vysledkyTransfDecreaseRat = sc.getDataTransferDegreaseRatio(cacheName);
                    for (int i = 0; i < vysledkyTransfDecreaseRat.length; i++){
                        dataset.addValue(vysledkyTransfDecreaseRat[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
            break;
            default:
                Long[] vysledkyTransfDecrease;
                for (final String cacheName : cacheNames) {
                    vysledkyTransfDecrease = sc.getDataTransferDegrease(cacheName);
                    for (int i = 0; i < vysledkyTransfDecrease.length; i++){
                        dataset.addValue(vysledkyTransfDecrease[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
            break;
        }
        return dataset;
        
    }
    
    /**
     * Creates chart.
     * 
     * @param dataset  the dataset.
     * @return The chart.
     */
    private static JFreeChart createChart(final CategoryDataset dataset, final String title, final String value) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            title,       // chart title
            "Cache Size [MB]",               // domain axis label
            value,                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                    // include legend
            true,                     // tooltips?
            false                     // URLs?
        );


        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.15);
        
        // disable bar outlines...
        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        // OPTIONAL CUSTOMISATION COMPLETED.
        
        return chart;
        
    }
}