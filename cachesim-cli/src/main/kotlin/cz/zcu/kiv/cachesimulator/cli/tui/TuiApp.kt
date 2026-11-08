package cz.zcu.kiv.cachesimulator.cli.tui

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Borders
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.CheckBoxList
import com.googlecode.lanterna.gui2.ComboBox
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.DefaultWindowManager
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.ProgressBar
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialog
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import cz.zcu.kiv.cachesimulator.cli.ResultTable
import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.logger
import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.policy.PolicyDescriptor
import cz.zcu.kiv.cachesimulator.report.CsvReport
import cz.zcu.kiv.cachesimulator.report.XlsReport
import cz.zcu.kiv.cachesimulator.request.RequestSource
import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.SimulationConfig
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import cz.zcu.kiv.cachesimulator.simulation.runSimulation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.absolute
import kotlin.io.path.isRegularFile
import kotlin.time.Duration
import kotlin.time.TimeSource

/** Interactive terminal UI: set up a simulation, watch it run, browse the results. */
class TuiApp private constructor(
    private val gui: WindowBasedTextGUI,
    private var settings: SimulationSettings,
) {
    /** runs coroutines on Lanterna's GUI thread */
    private val ui =
        CoroutineScope(
            SupervisorJob() +
                object : CoroutineDispatcher() {
                    override fun dispatch(
                        context: CoroutineContext,
                        block: Runnable,
                    ) = gui.guiThread.invokeLater(block)
                },
        )

    private val source = ComboBox(*RequestSource.entries.toTypedArray())
    private val logFile = TextBox(TerminalSize(30, 1), settings.afsLogFile)
    private val logSizes = ComboBox("from the log", "generated")
    private val requests = numberBox(settings.requestCount)
    private val files = numberBox(settings.maxFileId)
    private val zipfLambda = TextBox(TerminalSize(12, 1), settings.zipfAlpha.toString())
    private val minFileSize = numberBox(settings.minFileSize)
    private val maxFileSize = numberBox(settings.maxFileSize)
    private val minUserRequests = numberBox(settings.minUserRequests)
    private val sizes = TextBox(TerminalSize(30, 1), DEFAULT_SIZES)
    private val policies = CheckBoxList<PolicyDescriptor>(TerminalSize(24, 14))

    // ---------------------------------------------------------------- setup

    private fun showSetup() {
        val window = BasicWindow("Cache simulator").apply { setHints(listOf(Window.Hint.CENTERED)) }

        source.selectedItem = RequestSource.ZIPF
        CachePolicies.all.forEach { policies.addItem(it, it in DEFAULT_POLICIES) }

        val settingsPanel =
            Panel(GridLayout(2)).apply {
                row("Requests from:", source)
                row(
                    "AFS log file:",
                    Panel(LinearLayout(Direction.HORIZONTAL)).apply {
                        addComponent(logFile)
                        addComponent(Button("...", ::chooseLogFile))
                    },
                )
                row("File sizes (log):", logSizes)
                row("Requests count:", requests)
                row("Generated files:", files)
                row("Zipf lambda:", zipfLambda)
                row("Min file size [B]:", minFileSize)
                row("Max file size [B]:", maxFileSize)
                row("Min user requests:", minUserRequests)
                row("Cache sizes [MB]:", sizes)
            }

        val policyPanel =
            Panel(LinearLayout(Direction.VERTICAL)).apply {
                addComponent(policies)
                addComponent(
                    Panel(LinearLayout(Direction.HORIZONTAL)).apply {
                        addComponent(Button("All") { checkAll(true) })
                        addComponent(Button("None") { checkAll(false) })
                    },
                )
            }

        window.component =
            Panel(LinearLayout(Direction.VERTICAL)).apply {
                addComponent(
                    Panel(LinearLayout(Direction.HORIZONTAL)).apply {
                        addComponent(settingsPanel.withBorder(Borders.singleLine("Settings")))
                        addComponent(policyPanel.withBorder(Borders.singleLine("Policies")))
                    },
                )
                addComponent(Label("Tab/arrows move, Space toggles a policy, Enter presses a button."))
                addComponent(
                    Panel(LinearLayout(Direction.HORIZONTAL)).apply {
                        addComponent(Button("Run", ::startSimulation))
                        addComponent(Button("Quit", window::close))
                    },
                )
            }
        gui.addWindowAndWait(window)
    }

    private fun checkAll(checked: Boolean) {
        policies.items.forEach { policies.setChecked(it, checked) }
    }

    private fun chooseLogFile() {
        val file =
            FileDialogBuilder()
                .setTitle("AFS log file")
                .setActionLabel("Open")
                .setSelectedFile(File(logFile.text.ifBlank { "." }))
                .build()
                .showDialog(gui) ?: return
        logFile.text = file.absolutePath
        source.selectedItem = RequestSource.AFS_LOG
    }

    /** a form value that cannot be used; its message is shown to the user */
    private class InvalidInput(
        message: String,
    ) : Exception(message)

    /**
     * Reads the form into [settings].
     *
     * @return what to simulate, or null after showing what is wrong
     */
    private fun readForm(): SimulationConfig? =
        try {
            val requestSource = source.selectedItem
            val log = Path.of(logFile.text)
            if (requestSource == RequestSource.AFS_LOG && !log.isRegularFile()) invalid("Choose an existing AFS log file.")
            val min = minFileSize.positive("Min file size")
            val max = maxFileSize.positive("Max file size")
            if (min > max) invalid("Min file size must not be bigger than max file size.")

            val capacities =
                sizes.text
                    .split(',')
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .map { it.toIntOrNull() ?: invalid("Not a number: $it") }
                    .sorted()
                    .distinct()
            if (capacities.isEmpty() || capacities.first() <= 0) invalid("Cache sizes must be positive numbers separated by commas.")
            val selected = policies.checkedItems
            if (selected.isEmpty()) invalid("Select at least one policy.")

            settings =
                settings.copy(
                    afsLogFile = if (requestSource == RequestSource.AFS_LOG) log.absolute().toString() else settings.afsLogFile,
                    randomFileSizesForLog = requestSource != RequestSource.AFS_LOG || logSizes.selectedIndex == 1,
                    requestCount = requests.positive("Requests count"),
                    maxFileId = files.positive("Generated files"),
                    zipfAlpha = zipfLambda.text.trim().let { it.toDoubleOrNull() ?: invalid("Not a number: $it") },
                    minFileSize = min,
                    maxFileSize = max,
                    minUserRequests = minUserRequests.text.trim().let { it.toIntOrNull() ?: invalid("Not a number: $it") },
                )
            SimulationConfig(selected, capacities)
        } catch (e: InvalidInput) {
            MessageDialog.showMessageDialog(gui, "Cannot start the simulation", e.message, MessageDialogButton.OK)
            null
        }

    private fun invalid(message: String): Nothing = throw InvalidInput(message)

    private fun TextBox.positive(name: String): Int {
        val value = text.trim().toIntOrNull() ?: invalid("Not a number: ${text.trim()}")
        if (value <= 0) invalid("$name must be positive")
        return value
    }

    // ---------------------------------------------------------------- running

    private sealed interface Outcome {
        data class Finished(
            val results: List<UserStatistics>,
        ) : Outcome

        data class Failed(
            val error: Exception,
        ) : Outcome

        data object Cancelled : Outcome
    }

    private fun startSimulation() {
        val config = readForm() ?: return
        val requestSource = source.selectedItem
        val settings = settings

        val window = BasicWindow("Simulating").apply { setHints(listOf(Window.Hint.CENTERED, Window.Hint.MODAL)) }
        val bar = ProgressBar(0, 100, 40).apply { labelFormat = "%2.0f%%" }
        val status = Label("${config.policies.size} policies x ${config.cacheSizesMb.size} sizes")
        val start = TimeSource.Monotonic.markNow()

        val job =
            ui.launch {
                val outcome =
                    try {
                        val results =
                            runSimulation(requestSource, config, settings) { percent ->
                                gui.guiThread.invokeLater {
                                    bar.value = percent
                                    status.text = "Elapsed ${start.elapsedNow().inWholeSeconds} s"
                                }
                            }
                        Outcome.Finished(results)
                    } catch (_: CancellationException) {
                        Outcome.Cancelled
                    } catch (e: Exception) {
                        log.error("Simulation failed", e)
                        Outcome.Failed(e)
                    }
                // the dialogs below run their own event loop, so let this coroutine end first
                gui.guiThread.invokeLater {
                    window.close()
                    onFinished(outcome, start.elapsedNow())
                }
            }

        window.component =
            Panel(LinearLayout(Direction.VERTICAL)).apply {
                addComponent(bar)
                addComponent(status)
                addComponent(
                    Button("Cancel") {
                        status.text = "Cancelling..."
                        job.cancel()
                    },
                )
            }
        gui.addWindow(window)
    }

    private fun onFinished(
        outcome: Outcome,
        took: Duration,
    ) {
        when (outcome) {
            is Outcome.Failed -> {
                message("Simulation failed", outcome.error.toString())
            }

            Outcome.Cancelled -> {
                message("Cache simulator", "Simulation cancelled.")
            }

            is Outcome.Finished -> {
                if (outcome.results.isEmpty()) {
                    message("No results", "No user made enough requests. Try lowering \"Min user requests\".")
                } else {
                    showResults(outcome.results, took)
                }
            }
        }
    }

    private fun message(
        title: String,
        text: String,
    ) {
        MessageDialog.showMessageDialog(gui, title, text, MessageDialogButton.OK)
    }

    // ---------------------------------------------------------------- results

    /** shows a user in the combo box by its label */
    private class UserItem(
        val stats: UserStatistics,
    ) {
        override fun toString() = stats.userLabel
    }

    private fun showResults(
        results: List<UserStatistics>,
        took: Duration,
    ) {
        val window = BasicWindow("Results (${took.inWholeMilliseconds} ms)").apply { setHints(listOf(Window.Hint.CENTERED)) }

        val user = ComboBox(results.map(::UserItem))
        val metric = ComboBox(*ResultMetric.entries.toTypedArray())
        val summary = Label("")
        val table = Table<String>("Policy").apply { visibleRows = minOf(20, results.first().policyNames.size) }

        fun refresh() {
            val stats = user.selectedItem.stats
            summary.text = ResultTable.header(stats)
            val cells = ResultTable.cells(stats, metric.selectedItem)
            table.tableModel =
                TableModel<String>(*cells.first().toTypedArray()).apply {
                    cells.drop(1).forEach { addRow(it) }
                }
        }
        user.addListener { _, _, _ -> refresh() }
        metric.addListener { _, _, _ -> refresh() }
        refresh()

        window.component =
            Panel(LinearLayout(Direction.VERTICAL)).apply {
                addComponent(
                    Panel(GridLayout(2)).apply {
                        row("User:", user)
                        row("Show:", metric)
                    },
                )
                addComponent(summary)
                addComponent(table.withBorder(Borders.singleLine()))
                addComponent(
                    Panel(LinearLayout(Direction.HORIZONTAL)).apply {
                        addComponent(Button("Save CSV") { save("results.csv") { CsvReport.save(it, results) } })
                        addComponent(
                            Button("Save XLS") {
                                save("results.xls") {
                                    XlsReport.save(it, results)
                                    it
                                }
                            },
                        )
                        addComponent(Button("Back", window::close))
                    },
                )
            }
        gui.addWindow(window)
    }

    /** asks for a file name and saves there; [write] returns the file actually written */
    private fun save(
        defaultName: String,
        write: (Path) -> Path,
    ) {
        val name = TextInputDialog.showDialog(gui, "Save results", "File name:", defaultName)
        if (name.isNullOrBlank()) return
        val text =
            try {
                "Saved ${write(Path.of(name)).absolute()}"
            } catch (e: IOException) {
                log.warn("Cannot write {}", name, e)
                "Cannot write $name"
            }
        message("Save results", text)
    }

    companion object {
        private val log = logger<TuiApp>()

        private const val DEFAULT_SIZES = "8,16,32,64,128,256,512,1024"

        private val DEFAULT_POLICIES =
            setOf(
                CachePolicies.LRU,
                CachePolicies.LFU_SS,
                CachePolicies.ARC,
                CachePolicies.LIRS,
                CachePolicies.TWO_QUEUE,
                CachePolicies.FIFO,
            )

        /** @return process exit code */
        fun run(settings: SimulationSettings): Int =
            try {
                DefaultTerminalFactory().createScreen().use { screen ->
                    screen.startScreen()
                    val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
                    val app = TuiApp(gui, settings)
                    try {
                        app.showSetup()
                    } finally {
                        app.ui.cancel()
                    }
                    0
                }
            } catch (e: IOException) {
                System.err.println("Cannot open the terminal UI: ${e.message}")
                1
            }

        private fun numberBox(value: Int) = TextBox(TerminalSize(12, 1), value.toString()).setValidationPattern(Regex("[0-9]*").toPattern())

        private fun Panel.row(
            label: String,
            component: Component,
        ) {
            addComponent(Label(label))
            addComponent(component)
        }
    }
}
