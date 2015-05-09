
package cz.zcu.kiv.cacheSimulator.gui.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

/**
 * trida pro vykresleni spojnicoveho grafu
 * @author Pavel Bzoch
 */
@SuppressWarnings("serial")
public class LineChart extends JFrame{

     /**
     * konstruktor - iniciace promennych a vykresleni okna
     * @param title  the frame title.
     * @param sc data
     * @param dataSelect co chceme vykreslit
     */
    public LineChart(final String title, final UserStatistics sc, final int dataSelect) {
        super("Cache " + title);

        this.setIconImage(new javax.swing.ImageIcon(this.getClass().getResource("/ico/results.png")).getImage());

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout(0, 5));

        final CategoryDataset dataset = this.createDataset(sc, dataSelect);
        final JFreeChart chart = this.createChart(dataset, "Cache " + title, title);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setDisplayToolTips(true);
        this.setContentPane(chartPanel);
     }

    /**
     * Metoda pro vytvoreni datasetu podle dat
     * @return dataset
     */
    private CategoryDataset createDataset(final UserStatistics sc, final int dataSelect) {

        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final Integer[] cacheSizes = sc.getCacheSizes();
        final String[] cacheNames = sc.getCacheNames();

        switch (dataSelect){
            case 0:
                Double[] vysledkyHitRatio;
                for (final String cacheName:cacheNames){
                    vysledkyHitRatio = sc.getCacheHitRatios(cacheName);
                    for (int i = 0; i < vysledkyHitRatio.length; i++){
                        dataset.addValue(vysledkyHitRatio[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 1:
                Long[] vysledkyHitCount;
                for (final String cacheName:cacheNames){
                    vysledkyHitCount = sc.getCacheHits(cacheName);
                    for (int i = 0; i < vysledkyHitCount.length; i++){
                        dataset.addValue(vysledkyHitCount[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 2:
                Double[] vysledkySavedRatio;
                for (final String cacheName:cacheNames){
                    vysledkySavedRatio = sc.getCacheSavedBytesRatio(cacheName);
                    for (int i = 0; i < vysledkySavedRatio.length; i++){
                        dataset.addValue(vysledkySavedRatio[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 3:
                Long[] vysledkySavedBytes;
                for (final String cacheName:cacheNames){
                    vysledkySavedBytes = sc.getCacheHits(cacheName);
                    for (int i = 0; i < vysledkySavedBytes.length; i++){
                        dataset.addValue(vysledkySavedBytes[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
                break;
            case 4:
                Double[] vysledkyTransfDecreaseRat;
                for (final String cacheName:cacheNames){
                    vysledkyTransfDecreaseRat = sc.getDataTransferDegreaseRatio(cacheName);
                    for (int i = 0; i < vysledkyTransfDecreaseRat.length; i++){
                        dataset.addValue(vysledkyTransfDecreaseRat[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
            break;
            default:
                Long[] vysledkyTransfDecrease;
                for (final String cacheName:cacheNames){
                    vysledkyTransfDecrease = sc.getDataTransferDegrease(cacheName);
                    for (int i = 0; i < vysledkyTransfDecrease.length; i++){
                        dataset.addValue(vysledkyTransfDecrease[i], cacheName, cacheSizes[i].toString() + "[MB]");
                    }
                }
            break;
        }
        return dataset;
    }

  private JFreeChart createChart(final CategoryDataset dataset, final String title, final String value) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createLineChart(
            title,                      // chart title
            "Cache Size [MB]",              // domain axis label
            value,                     // range axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            true,                      // include legend
            true,                      // tooltips
            false                      // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
//        final StandardLegend legend = (StandardLegend) chart.getLegend();
  //      legend.setDisplaySeriesShapes(true);
    //    legend.setShapeScaleX(1.5);
      //  legend.setShapeScaleY(1.5);
        //legend.setDisplaySeriesLines(true);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);

        // ****************************************************************************
        // * JFREECHART DEVELOPER GUIDE                                               *
        // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
        // * to purchase from Object Refinery Limited:                                *
        // *                                                                          *
        // * http://www.object-refinery.com/jfreechart/guide.html                     *
        // *                                                                          *
        // * Sales are used to provide funding for the JFreeChart project - please    *
        // * support us so that we can continue developing free software.             *
        // ****************************************************************************

        // customise the renderer...
        final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
//        renderer.setDrawShapes(true);

        renderer.setSeriesStroke(
            0, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {10.0f, 6.0f}, 0.0f
            )
        );
        renderer.setSeriesStroke(
            1, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f
            )
        );
        renderer.setSeriesStroke(
            2, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {2.0f, 6.0f}, 0.0f
            )
        );
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;
    }

}
