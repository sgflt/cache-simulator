package cz.zcu.kiv.cachesimulator.cli

import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import java.text.DecimalFormat

/** Formats results as a plain-text table: one row per policy, one column per cache size. */
object ResultTable {
    fun header(stats: UserStatistics): String =
        "${stats.userLabel} - ${format(stats.requests)} requests, ${format(stats.requestedBytes / 1024 / 1024)} MB without cache"

    /** @return rows of cells; the first row is the header */
    fun cells(
        stats: UserStatistics,
        metric: ResultMetric,
    ): List<List<String>> =
        listOf(listOf("Policy") + stats.cacheSizesMb.map { "$it MB" }) +
            stats.policyNames.map { policy -> listOf(policy) + metric.values(stats, policy).map(::format) }

    fun render(
        stats: UserStatistics,
        metric: ResultMetric,
    ): String {
        val rows = cells(stats, metric)
        val widths = rows.first().indices.map { column -> rows.maxOf { it[column].length } }
        return buildString {
            appendLine(metric.label)
            rows.forEachIndexed { index, row ->
                val line =
                    row.mapIndexed { column, cell ->
                        if (column == 0) cell.padEnd(widths[column]) else cell.padStart(widths[column])
                    }
                appendLine(line.joinToString("  "))
                if (index == 0) appendLine("-".repeat(widths.sum() + 2 * (widths.size - 1)))
            }
        }
    }

    fun format(value: Number): String = DecimalFormat(if (value is Double || value is Float) "#,##0.00" else "#,##0").format(value)
}
