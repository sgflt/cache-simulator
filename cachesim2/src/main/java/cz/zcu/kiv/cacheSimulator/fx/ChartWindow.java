package cz.zcu.kiv.cacheSimulator.fx;

import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/**
 * Window with a bar or line chart of one result metric for one simulated user.
 */
final class ChartWindow {

  enum Kind { BAR, LINE }

  /**
   * JavaFX charts only have 8 default series colors, not enough for all policies
   */
  private static final String[] PALETTE = {
    "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f",
    "#bcbd22", "#17becf", "#aec7e8", "#ffbb78", "#98df8a", "#ff9896", "#c5b0d5", "#c49c94",
    "#f7b6d2", "#c7c7c7", "#dbdb8d", "#9edae5"
  };

  private ChartWindow() {
  }

  static void show(final Window owner, final Kind kind, final UserStatistics stats, final ResultMetric metric) {
    final CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel("Cache Size [MB]");
    final NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel(metric.label());

    final XYChart<String, Number> chart = kind == Kind.BAR
      ? new BarChart<>(xAxis, yAxis)
      : new LineChart<>(xAxis, yAxis);
    chart.setTitle("Cache " + metric.label());
    chart.setAnimated(false);
    if (chart instanceof final BarChart<String, Number> bar) {
      bar.setCategoryGap(16);
      bar.setBarGap(1);
    }
    chart.getData().setAll(createSeries(stats, metric));

    final Stage stage = new Stage();
    stage.initOwner(owner);
    stage.setTitle("Cache " + metric.label());
    stage.getIcons().add(new Image(ChartWindow.class.getResource("/ico/results.png").toExternalForm()));
    final Scene scene = new Scene(chart, 960, 640);
    scene.getStylesheets().add(CacheSimulatorApp.stylesheet());
    stage.setScene(scene);
    stage.show();

    // legend and data nodes exist only after the first layout
    chart.applyCss();
    chart.layout();
    applyColors(chart, kind);
    installTooltips(chart);
  }

  private static List<XYChart.Series<String, Number>> createSeries(final UserStatistics stats,
                                                                    final ResultMetric metric) {
    final Integer[] sizes = stats.getCacheSizes();
    return stats.getCacheNames().stream().map(cacheName -> {
      final XYChart.Series<String, Number> series = new XYChart.Series<>();
      series.setName(cacheName);
      final Number[] values = metric.values(stats, cacheName);
      for (int i = 0; i < values.length; i++) {
        series.getData().add(new XYChart.Data<>(sizes[i] + " MB", values[i]));
      }
      return series;
    }).toList();
  }

  private static void applyColors(final XYChart<String, Number> chart, final Kind kind) {
    final List<XYChart.Series<String, Number>> data = chart.getData();
    for (int i = 0; i < data.size(); i++) {
      final String color = PALETTE[i % PALETTE.length];
      final XYChart.Series<String, Number> series = data.get(i);
      if (kind == Kind.LINE && series.getNode() != null) {
        series.getNode().setStyle("-fx-stroke: " + color + ";");
      }
      for (final XYChart.Data<String, Number> d : series.getData()) {
        if (d.getNode() != null) {
          d.getNode().setStyle(kind == Kind.BAR
            ? "-fx-bar-fill: " + color + ";"
            : "-fx-background-color: " + color + ", white;");
        }
      }
    }
    int i = 0;
    for (final Node symbol : chart.lookupAll(".chart-legend-item-symbol")) {
      final String color = PALETTE[i++ % PALETTE.length];
      symbol.setStyle(kind == Kind.BAR
        ? "-fx-bar-fill: " + color + "; -fx-background-color: " + color + ";"
        : "-fx-background-color: " + color + ", white;");
    }
  }

  private static void installTooltips(final XYChart<String, Number> chart) {
    for (final XYChart.Series<String, Number> series : chart.getData()) {
      for (final XYChart.Data<String, Number> d : series.getData()) {
        if (d.getNode() != null) {
          Tooltip.install(d.getNode(), new Tooltip(
            series.getName() + "\n" + d.getXValue() + ": " + MainController.format(d.getYValue())));
        }
      }
    }
  }
}
