package cz.zcu.kiv.cachesimulator.gui

import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import javafx.scene.Scene
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.Window

/** Window with a bar or line chart of one result metric for one simulated user. */
internal object ChartWindow {
    enum class Kind { BAR, LINE }

    /** JavaFX charts only have 8 default series colors, not enough for all policies */
    private val PALETTE =
        listOf(
            "#1f77b4",
            "#ff7f0e",
            "#2ca02c",
            "#d62728",
            "#9467bd",
            "#8c564b",
            "#e377c2",
            "#7f7f7f",
            "#bcbd22",
            "#17becf",
            "#aec7e8",
            "#ffbb78",
            "#98df8a",
            "#ff9896",
            "#c5b0d5",
            "#c49c94",
            "#f7b6d2",
            "#c7c7c7",
            "#dbdb8d",
            "#9edae5",
        )

    fun show(
        owner: Window,
        kind: Kind,
        stats: UserStatistics,
        metric: ResultMetric,
    ) {
        val xAxis = CategoryAxis().apply { label = "Cache Size [MB]" }
        val yAxis = NumberAxis().apply { label = metric.label }
        val chart: XYChart<String, Number> =
            when (kind) {
                Kind.BAR -> {
                    BarChart(xAxis, yAxis).apply {
                        categoryGap = 16.0
                        barGap = 1.0
                    }
                }

                Kind.LINE -> {
                    LineChart(xAxis, yAxis)
                }
            }
        chart.title = "Cache ${metric.label}"
        chart.animated = false
        chart.data.setAll(series(stats, metric))

        Stage().apply {
            initOwner(owner)
            title = "Cache ${metric.label}"
            icons += Image(CacheSimulatorApp.resource("/ico/results.png").toExternalForm())
            scene = Scene(chart, 960.0, 640.0).apply { stylesheets += CacheSimulatorApp.STYLESHEET }
            show()
        }

        // legend and data nodes exist only after the first layout
        chart.applyCss()
        chart.layout()
        applyColors(chart, kind)
        installTooltips(chart)
    }

    private fun series(
        stats: UserStatistics,
        metric: ResultMetric,
    ): List<XYChart.Series<String, Number>> =
        stats.policyNames.map { policy ->
            XYChart.Series<String, Number>().apply {
                name = policy
                metric.values(stats, policy).forEachIndexed { i, value ->
                    data += XYChart.Data("${stats.cacheSizesMb[i]} MB", value)
                }
            }
        }

    private fun applyColors(
        chart: XYChart<String, Number>,
        kind: Kind,
    ) {
        chart.data.forEachIndexed { i, series ->
            val color = color(i)
            if (kind == Kind.LINE) series.node?.style = "-fx-stroke: $color;"
            series.data.forEach {
                it.node?.style =
                    when (kind) {
                        Kind.BAR -> "-fx-bar-fill: $color;"
                        Kind.LINE -> "-fx-background-color: $color, white;"
                    }
            }
        }
        chart.lookupAll(".chart-legend-item-symbol").forEachIndexed { i, symbol ->
            val color = color(i)
            symbol.style =
                when (kind) {
                    Kind.BAR -> "-fx-bar-fill: $color; -fx-background-color: $color;"
                    Kind.LINE -> "-fx-background-color: $color, white;"
                }
        }
    }

    private fun color(index: Int) = PALETTE[index % PALETTE.size]

    private fun installTooltips(chart: XYChart<String, Number>) {
        for (series in chart.data) {
            for (point in series.data) {
                point.node?.let { Tooltip.install(it, Tooltip("${series.name}\n${point.xValue}: ${formatResult(point.yValue)}")) }
            }
        }
    }
}
