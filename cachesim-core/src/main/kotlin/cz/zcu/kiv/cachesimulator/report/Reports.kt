package cz.zcu.kiv.cachesimulator.report

import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.UserId
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import java.nio.file.Path
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

private const val MB = 1024 * 1024

/** The result tables of every report, in this order, with their titles in each format. */
private enum class Section(
    val metric: ResultMetric,
    val consoleTitle: String,
    val csvTitle: String,
    val xlsTitle: String,
) {
    HIT_RATIO(ResultMetric.HIT_RATIO, "Read hit ratio", "Read hit ratio", "Read Hit Ratio"),
    HITS(ResultMetric.HIT_COUNT, "Read hits", "Read hits", "Read Hits"),
    SAVED_BYTES_RATIO(ResultMetric.SAVED_BYTES_RATIO, "Saved bytes ratio", "Saved bytes ratio", "Saved Bytes Ratio"),
    SAVED_BYTES(ResultMetric.SAVED_BYTES, "Saved bytes", "Saved bytes", "Saved Bytes"),
    TRANSFER_DECREASE_RATIO(
        ResultMetric.TRANSFER_DECREASE_RATIO,
        "Traffic Transfer Decrease Ratio",
        "Data transfer decrease ratio",
        "Data Transfer Decrease Ratio",
    ),
    TRANSFER_DECREASE(
        ResultMetric.TRANSFER_DECREASE,
        "Traffic Transfer Decrease",
        "Data transfer decrease",
        "Data Transfer Decrease",
    ),
}

/** "name;value;value;" rows of one table */
private fun StringBuilder.appendRows(
    user: UserStatistics,
    metric: ResultMetric,
) {
    for (policy in user.policyNames) {
        append(policy).append(';')
        metric.values(user, policy).forEach { append(it).append(';') }
        append('\n')
    }
}

private fun StringBuilder.appendSizes(user: UserStatistics) {
    user.cacheSizesMb.forEach { append(it).append("MB;") }
}

/** Results as semicolon-separated text, one block per user. */
object CsvReport {
    fun render(stats: List<UserStatistics>): String =
        buildString {
            for (user in stats) {
                if (user.userId == UserId.SIMULATED) {
                    append("\n\nSimulated user\n")
                } else {
                    append("\nUser id:${UserId.hostId(user.userId)}; ip: ${UserId.ip(user.userId)}\n")
                }
                append("\nTotal network traffic without cache;${user.requestedBytes / MB}MB")
                append("\nTotal requested files;${user.requests}")
                for (section in Section.entries) {
                    if (section == Section.HIT_RATIO) append('\n')
                    append("\n${section.csvTitle};Cache size")
                    append("\nCaching policy;")
                    appendSizes(user)
                    append('\n')
                    appendRows(user, section.metric)
                }
            }
        }

    /** writes the report to [path], adding the `.csv` extension when it is missing */
    fun save(
        path: Path,
        stats: List<UserStatistics>,
    ): Path {
        val target = if (path.fileName.toString().endsWith(".csv")) path else path.resolveSibling("${path.fileName}.csv")
        target.writeText(render(stats))
        return target
    }
}

/** Results as text for the console. */
object ConsoleReport {
    fun render(stats: List<UserStatistics>): String =
        buildString {
            for (user in stats) {
                val who = if (user.userId == UserId.SIMULATED) "simulated user" else "user id: ${UserId.label(user.userId)}"
                append("\n=================== Statistics for $who ===================\n\n")
                append("Total network traffic without cache;${user.requestedBytes}\n")
                append("Total requested files;${user.requests}\n")
                for (section in Section.entries) {
                    append("\n${section.consoleTitle};Cache size")
                    append("\nCaching policy;")
                    appendSizes(user)
                    append('\n')
                    appendRows(user, section.metric)
                }
            }
        }
}

/** Results as an Excel workbook, one sheet per user. */
object XlsReport {
    fun save(
        path: Path,
        stats: List<UserStatistics>,
    ) {
        HSSFWorkbook().use { workbook ->
            stats.forEach { workbook.addSheet(it) }
            path.outputStream().use(workbook::write)
        }
    }

    private fun Workbook.addSheet(user: UserStatistics) {
        val sheet = createSheet(sheetName(user))
        sheet.createRow(1).apply {
            createCell(0).setCellValue("Total network traffic without cache")
            createCell(1).setCellValue("${user.requestedBytes / MB}MB")
        }
        sheet.createRow(2).apply {
            createCell(0).setCellValue("Total requested files")
            createCell(1).setCellValue(user.requests.toDouble())
        }
        val bold =
            createCellStyle().apply {
                setFont(createFont().apply { bold = true })
            }
        var row = 4
        for (section in Section.entries) {
            row = sheet.addTable(row, user, section, bold) + 2
        }
    }

    /** "ID 42; ip 10.0.0.1", made unique within the workbook */
    private fun Workbook.sheetName(user: UserStatistics): String {
        if (user.userId == UserId.SIMULATED) return "Simulated user"
        var name = "ID ${UserId.hostId(user.userId)}; ip ${UserId.ip(user.userId)}"
        var suffix = 1
        while (getSheet(name) != null) name += ", ${suffix++}"
        return name
    }

    /** @return the last row of the table */
    private fun Sheet.addTable(
        firstRow: Int,
        user: UserStatistics,
        section: Section,
        bold: CellStyle,
    ): Int {
        val sizes = user.cacheSizesMb
        createRow(firstRow).apply {
            createCell(0).apply {
                setCellValue("${section.xlsTitle} / Caching Policy")
                cellStyle = bold
            }
            bold.alignment = HorizontalAlignment.CENTER
            createCell(1).apply {
                setCellValue("Cache Size [MB]")
                cellStyle = bold
            }
        }
        // POI refuses a merged region of a single cell
        if (sizes.size > 1) addMergedRegion(CellRangeAddress(firstRow, firstRow, 1, sizes.size))
        createRow(firstRow + 1).apply {
            createCell(0).setCellValue("Caching policy")
            sizes.forEachIndexed { i, size ->
                createCell(i + 1).apply {
                    setCellValue(size.toDouble())
                    cellStyle = bold
                }
            }
        }
        var row = firstRow + 1
        for (policy in user.policyNames) {
            createRow(++row).apply {
                createCell(0).setCellValue(policy)
                section.metric.values(user, policy).forEachIndexed { i, value -> createCell(i + 1).setCellValue(value.toDouble()) }
            }
        }
        return row
    }
}
