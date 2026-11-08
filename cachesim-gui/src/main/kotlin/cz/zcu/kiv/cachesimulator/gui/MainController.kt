package cz.zcu.kiv.cachesimulator.gui

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.logger
import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.policy.PolicyDescriptor
import cz.zcu.kiv.cachesimulator.policy.PolicyParams
import cz.zcu.kiv.cachesimulator.report.ConsoleReport
import cz.zcu.kiv.cachesimulator.report.CsvReport
import cz.zcu.kiv.cachesimulator.report.XlsReport
import cz.zcu.kiv.cachesimulator.request.RequestSource
import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.SimulationConfig
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import cz.zcu.kiv.cachesimulator.simulation.runSimulation
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.ProgressBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.Spinner
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Controller of the main window (`/fx/main.fxml`).
 *
 * @param settings settings to start with; changed by the form, read back when the app closes
 */
class MainController(
    settings: SimulationSettings,
) : SettingsHolder {
    override var settings = settings
        private set
    override var policyParams = PolicyParams()
        private set

    override fun updateSettings(change: SimulationSettings.() -> SimulationSettings) {
        settings = settings.change()
    }

    override fun updateParams(change: PolicyParams.() -> PolicyParams) {
        policyParams = policyParams.change()
    }

    /** runs on the JavaFX thread (`MainScope` uses `Dispatchers.Main`, which kotlinx-coroutines-javafx provides) */
    private val scope = MainScope()

    // --- menu & toolbar
    @FXML private lateinit var requestMenu: Menu

    @FXML private lateinit var simulateMenuItem: MenuItem

    @FXML private lateinit var cancelMenuItem: MenuItem

    @FXML private lateinit var saveCsvMenuItem: MenuItem

    @FXML private lateinit var saveXlsMenuItem: MenuItem

    @FXML private lateinit var printConsoleMenuItem: MenuItem

    @FXML private lateinit var simulateButton: Button

    @FXML private lateinit var cancelButton: Button

    @FXML private lateinit var saveCsvButton: Button

    @FXML private lateinit var saveXlsButton: Button

    @FXML private lateinit var printConsoleButton: Button

    @FXML private lateinit var tabs: TabPane

    @FXML private lateinit var settingsTab: Tab

    @FXML private lateinit var resultsTab: Tab

    // --- settings tab
    @FXML private lateinit var settingsContent: ScrollPane

    @FXML private lateinit var sourceCombo: ComboBox<RequestSource>

    @FXML private lateinit var noSourceHint: Label

    @FXML private lateinit var pathRow: Node

    @FXML private lateinit var pathField: TextField

    @FXML private lateinit var requestCountRow: Node

    @FXML private lateinit var requestCountSpinner: Spinner<Int>

    @FXML private lateinit var generateFilesRow: Node

    @FXML private lateinit var generateFilesSpinner: Spinner<Int>

    @FXML private lateinit var meanRow: Node

    @FXML private lateinit var meanSpinner: Spinner<Int>

    @FXML private lateinit var dispersionRow: Node

    @FXML private lateinit var dispersionSpinner: Spinner<Int>

    @FXML private lateinit var prefDivisibleRow: Node

    @FXML private lateinit var prefDivisibleSpinner: Spinner<Int>

    @FXML private lateinit var prefStepRow: Node

    @FXML private lateinit var prefStepSpinner: Spinner<Int>

    @FXML private lateinit var nonPrefRow: Node

    @FXML private lateinit var nonPrefSpinner: Spinner<Int>

    @FXML private lateinit var zipfRow: Node

    @FXML private lateinit var zipfSpinner: Spinner<Double>

    @FXML private lateinit var randomSizesRow: Node

    @FXML private lateinit var randomSizesCheck: CheckBox

    @FXML private lateinit var minSizeRow: Node

    @FXML private lateinit var minSizeSpinner: Spinner<Int>

    @FXML private lateinit var maxSizeRow: Node

    @FXML private lateinit var maxSizeSpinner: Spinner<Int>

    @FXML private lateinit var capacityList: ListView<Int>

    @FXML private lateinit var capacitySpinner: Spinner<Int>

    @FXML private lateinit var removeCapacityButton: Button

    @FXML private lateinit var statLimitSpinner: Spinner<Int>

    @FXML private lateinit var networkSpeedSpinner: Spinner<Int>

    @FXML private lateinit var slidingWindowSpinner: Spinner<Int>

    // --- cache algorithms tab
    @FXML private lateinit var policiesContent: SplitPane

    @FXML private lateinit var policyBox: VBox

    @FXML private lateinit var policySettingsPane: StackPane

    // --- results tab
    @FXML private lateinit var userList: ListView<UserStatistics>

    @FXML private lateinit var metricCombo: ComboBox<ResultMetric>

    @FXML private lateinit var barChartButton: Button

    @FXML private lateinit var lineChartButton: Button

    @FXML private lateinit var totalRequestsLabel: Label

    @FXML private lateinit var totalTrafficLabel: Label

    @FXML private lateinit var resultsTable: TableView<ResultRow>

    // --- status bar
    @FXML private lateinit var progressBar: ProgressBar

    @FXML private lateinit var statusLabel: Label

    private val policyChecks = mutableMapOf<PolicyDescriptor, CheckBox>()
    private val capacities = FXCollections.observableArrayList(DEFAULT_CAPACITIES)
    private val running = SimpleBooleanProperty(false)
    private val hasResults = SimpleBooleanProperty(false)
    private var results = emptyList<UserStatistics>()
    private var simulation: Job? = null

    /** one row of the results table: a policy and its values for all cache sizes */
    class ResultRow(
        val policy: String,
        val values: List<Number>,
    )

    private val source: RequestSource? get() = sourceCombo.value

    @FXML
    private fun initialize() {
        initRequestSettings()
        initCapacities()
        initOtherSettings()
        initPolicies()
        initResults()
        initStateBindings()
    }

    /** stops a running simulation; the window is closing */
    fun close() = scope.cancel()

    // =====================================================================
    // settings
    // =====================================================================

    private fun initRequestSettings() {
        sourceCombo.items.setAll(*RequestSource.entries.toTypedArray())
        sourceCombo.valueProperty().addListener { _, _, source -> onSourceChanged(source) }
        for (source in RequestSource.entries) {
            requestMenu.items +=
                MenuItem(source.toString()).apply {
                    setOnAction {
                        tabs.selectionModel.select(settingsTab)
                        sourceCombo.value = source
                    }
                }
        }

        requestCountSpinner.setUp(1, 100_000_000, settings.requestCount) { updateSettings { copy(requestCount = it) } }
        generateFilesSpinner.setUp(1, 100_000_000, settings.maxFileId) { updateSettings { copy(maxFileId = it) } }
        meanSpinner.setUp(1, 100_000_000, settings.gaussianMean) { updateSettings { copy(gaussianMean = it) } }
        dispersionSpinner.setUp(1, 100_000_000, settings.gaussianDispersion) { updateSettings { copy(gaussianDispersion = it) } }
        prefDivisibleSpinner.setUp(1, 1_000_000, settings.preferredFileMultiple) { updateSettings { copy(preferredFileMultiple = it) } }
        prefStepSpinner.setUp(1, 1_000_000, settings.preferenceStep) { updateSettings { copy(preferenceStep = it) } }
        nonPrefSpinner.setUp(1, 1_000_000, settings.excludedFileMultiple) { updateSettings { copy(excludedFileMultiple = it) } }
        zipfSpinner.setUp(0.01, 10.0, settings.zipfAlpha, 0.01) { updateSettings { copy(zipfAlpha = it) } }

        // file sizes: minimum must stay below maximum
        minSizeSpinner.setUp(0, Int.MAX_VALUE - 1, settings.minFileSize) {
            if (it >= maxSizeSpinner.value) {
                minSizeSpinner.valueFactory.value = maxSizeSpinner.value - 1
            } else {
                updateSettings { copy(minFileSize = it) }
            }
        }
        maxSizeSpinner.setUp(1, Int.MAX_VALUE, settings.maxFileSize) {
            if (it <= minSizeSpinner.value) {
                maxSizeSpinner.valueFactory.value = minSizeSpinner.value + 1
            } else {
                updateSettings { copy(maxFileSize = it) }
            }
        }
        randomSizesCheck.isSelected = settings.randomFileSizesForLog
        randomSizesCheck.selectedProperty().addListener { _, _, selected -> updateSettings { copy(randomFileSizesForLog = selected) } }

        // only the inputs of the chosen request source are shown
        showWhen(noSourceHint) { source == null }
        showWhen(pathRow) { source == RequestSource.AFS_LOG }
        showWhen(requestCountRow) { source?.isGenerator == true }
        showWhen(generateFilesRow) { source?.isGenerator == true }
        showWhen(meanRow) { source == RequestSource.GAUSSIAN }
        showWhen(dispersionRow) { source == RequestSource.GAUSSIAN }
        showWhen(prefDivisibleRow) { source == RequestSource.RANDOM_WITH_PREFERENCE }
        showWhen(prefStepRow) { source == RequestSource.RANDOM_WITH_PREFERENCE }
        showWhen(nonPrefRow) { source == RequestSource.RANDOM_WITH_PREFERENCE }
        showWhen(zipfRow) { source == RequestSource.ZIPF }
        showWhen(randomSizesRow) { source != null }
        showWhen(minSizeRow) { source != null && randomSizesCheck.isSelected }
        showWhen(maxSizeRow) { source != null && randomSizesCheck.isSelected }
        // generators always need random file sizes; only the AFS log has its own
        randomSizesCheck.disableProperty().bind(
            Bindings.createBooleanBinding({ source != RequestSource.AFS_LOG }, sourceCombo.valueProperty()),
        )
    }

    private fun showWhen(
        node: Node,
        condition: () -> Boolean,
    ) {
        val visible = Bindings.createBooleanBinding(condition, sourceCombo.valueProperty(), randomSizesCheck.selectedProperty())
        node.visibleProperty().bind(visible)
        node.managedProperty().bind(visible)
    }

    private fun onSourceChanged(source: RequestSource?) {
        when {
            source == RequestSource.AFS_LOG -> if (pathField.text.isEmpty()) chooseAfsFile()
            source != null -> randomSizesCheck.isSelected = true
        }
    }

    @FXML
    private fun chooseAfsFile() {
        val file =
            FileChooser()
                .apply {
                    title = "Open AFS log file"
                    setInitialDirectory()
                }.showOpenDialog(window())
        if (file != null) {
            pathField.text = file.absolutePath
            updateSettings { copy(afsLogFile = file.absolutePath) }
            rememberDirectory(file)
        } else if (pathField.text.isEmpty()) {
            statusLabel.text = "Choose an AFS log file before running the simulation."
        }
    }

    private fun initCapacities() {
        capacityList.items = capacities
        capacitySpinner.setUp(1, 1_000_000, 8)
        removeCapacityButton.disableProperty().bind(capacityList.selectionModel.selectedItemProperty().isNull)
    }

    @FXML
    private fun addCapacity() {
        val capacity = capacitySpinner.value
        if (capacity in capacities) {
            warn("Cache capacity $capacity MB is already in the list.")
            return
        }
        capacities += capacity
        capacities.sort()
        capacityList.selectionModel.select(capacity)
    }

    @FXML
    private fun removeCapacity() {
        capacityList.selectionModel.selectedItem?.let { capacities.remove(it) }
    }

    private fun initOtherSettings() {
        statLimitSpinner.setUp(1, 100_000_000, settings.minUserRequests) { updateSettings { copy(minUserRequests = it) } }
        networkSpeedSpinner.setUp(1, 10_000, settings.networkSpeedMbit) { updateSettings { copy(networkSpeedMbit = it) } }
        slidingWindowSpinner.setUp(0, 75, settings.downloadWindowPercent) { updateSettings { copy(downloadWindowPercent = it) } }
    }

    // =====================================================================
    // cache algorithms
    // =====================================================================

    private fun initPolicies() {
        for (policy in CachePolicies.all) {
            val check =
                CheckBox(policy.label).apply {
                    isSelected = true
                    maxWidth = Double.MAX_VALUE
                    setOnMouseEntered { showPolicySettings(policy) }
                    setOnAction { showPolicySettings(policy) }
                    focusedProperty().addListener { _, _, focused -> if (focused) showPolicySettings(policy) }
                }
            policyChecks[policy] = check
            policyBox.children += check
        }
    }

    private fun showPolicySettings(policy: PolicyDescriptor) {
        policySettingsPane.children.setAll(PolicySettings.create(policy, this))
    }

    @FXML
    private fun selectAllPolicies() = policyChecks.values.forEach { it.isSelected = true }

    @FXML
    private fun selectNoPolicies() = policyChecks.values.forEach { it.isSelected = false }

    private fun selectedPolicies() = policyChecks.filterValues { it.isSelected }.keys.toList()

    // =====================================================================
    // simulation
    // =====================================================================

    private fun initStateBindings() {
        val exportDisabled = running.or(hasResults.not())
        simulateButton.disableProperty().bind(running)
        simulateMenuItem.disableProperty().bind(running)
        cancelButton.disableProperty().bind(running.not())
        cancelMenuItem.disableProperty().bind(running.not())
        requestMenu.disableProperty().bind(running)
        listOf(saveCsvButton, saveXlsButton, printConsoleButton).forEach { it.disableProperty().bind(exportDisabled) }
        listOf(saveCsvMenuItem, saveXlsMenuItem, printConsoleMenuItem).forEach { it.disableProperty().bind(exportDisabled) }
        settingsContent.disableProperty().bind(running)
        policiesContent.disableProperty().bind(running)
        resultsTab.disableProperty().bind(exportDisabled)
        progressBar.visibleProperty().bind(running)
        progressBar.managedProperty().bind(running)
    }

    /** @return true when the simulation can start, otherwise shows what is missing */
    private fun checkSettings(): Boolean {
        val problem =
            when {
                source == null -> settingsTab to "You have to select a request input method."
                source == RequestSource.AFS_LOG && !File(settings.afsLogFile).isFile -> settingsTab to "You have to select an AFS log file."
                capacities.isEmpty() -> settingsTab to "You have to add at least one cache capacity."
                selectedPolicies().isEmpty() -> tabs.tabs[1] to "You have to select at least one cache algorithm."
                else -> return true
            }
        tabs.selectionModel.select(problem.first)
        alert(Alert.AlertType.ERROR, "Cannot start the simulation", problem.second)
        return false
    }

    @FXML
    private fun simulate() {
        if (running.get() || !checkSettings()) return
        val source = checkNotNull(source)
        val config = SimulationConfig(selectedPolicies(), capacities.toList())
        val settings = settings
        val policyParams = policyParams
        val start = TimeSource.Monotonic.markNow()
        // the simulation reports far more often than the screen needs; a state flow keeps only the latest value
        val progress = MutableStateFlow(0)

        hasResults.set(false)
        running.set(true)
        tabs.selectionModel.select(settingsTab)
        simulation =
            scope.launch {
                val progressUpdates =
                    progress
                        .onEach {
                            progressBar.progress = it / 100.0
                            statusLabel.text = "Simulation in progress… $it %"
                        }.launchIn(this)
                val outcome =
                    try {
                        Result.success(runSimulation(source, config, settings, policyParams) { progress.value = it })
                    } catch (e: CancellationException) {
                        statusLabel.text = "Simulation cancelled."
                        throw e
                    } catch (e: Exception) {
                        Result.failure(e)
                    } finally {
                        // runSimulation returns only once the simulation has stopped, so a new one can start
                        progressUpdates.cancel()
                        running.set(false)
                    }
                outcome
                    .onSuccess { onSimulationFinished(it, start.elapsedNow()) }
                    .onFailure(::onSimulationFailed)
            }
    }

    @FXML
    private fun cancel() {
        simulation?.cancel()
    }

    private fun onSimulationFinished(
        stats: List<UserStatistics>,
        took: Duration,
    ) {
        if (stats.isEmpty()) {
            statusLabel.text = "Simulation finished without results."
            alert(
                Alert.AlertType.WARNING,
                "There are no results",
                "No simulated user made enough requests. Try lowering \"Minimum requests for including a user\".",
            )
            return
        }
        results = stats
        hasResults.set(true)
        userList.items.setAll(stats)
        userList.selectionModel.selectFirst()
        tabs.selectionModel.select(resultsTab)
        statusLabel.text = "Simulation finished in %.1f s.".format(took.inWholeMilliseconds / 1000.0)
    }

    private fun onSimulationFailed(error: Throwable) {
        statusLabel.text = "Simulation failed."
        log.error("Simulation failed", error)
        alert(Alert.AlertType.ERROR, "Simulation failed", error.toString())
    }

    // =====================================================================
    // results
    // =====================================================================

    private fun initResults() {
        metricCombo.items.setAll(*ResultMetric.entries.toTypedArray())
        metricCombo.selectionModel.selectFirst()
        metricCombo.valueProperty().addListener { _, _, _ -> refreshResults() }
        userList.setCellFactory {
            object : ListCell<UserStatistics>() {
                override fun updateItem(
                    item: UserStatistics?,
                    empty: Boolean,
                ) {
                    super.updateItem(item, empty)
                    text = if (empty) null else item?.userLabel
                }
            }
        }
        userList.selectionModel.selectedItemProperty().addListener { _, _, _ -> refreshResults() }
        resultsTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        val noUser = userList.selectionModel.selectedItemProperty().isNull
        barChartButton.disableProperty().bind(noUser)
        lineChartButton.disableProperty().bind(noUser)
    }

    private fun refreshResults() {
        val stats = userList.selectionModel.selectedItem
        val metric = metricCombo.value
        resultsTable.columns.clear()
        resultsTable.items.clear()
        if (stats == null || metric == null) {
            totalRequestsLabel.text = ""
            totalTrafficLabel.text = ""
            return
        }
        totalRequestsLabel.text = formatResult(stats.requests)
        totalTrafficLabel.text = "${formatResult(stats.requestedBytes / 1024 / 1024)} MB"

        resultsTable.columns +=
            TableColumn<ResultRow, String>("Cache Policy").apply {
                setCellValueFactory { ReadOnlyStringWrapper(it.value.policy) }
                minWidth = 140.0
            }
        stats.cacheSizesMb.forEachIndexed { index, size ->
            resultsTable.columns +=
                TableColumn<ResultRow, Number>("$size MB").apply {
                    setCellValueFactory { ReadOnlyObjectWrapper(it.value.values[index]) }
                    comparator = compareBy { it.toDouble() }
                    setCellFactory { NumberCell() }
                    styleClass += "numeric"
                }
        }
        resultsTable.items.setAll(stats.policyNames.map { ResultRow(it, metric.values(stats, it)) })
    }

    private class NumberCell : TableCell<ResultRow, Number>() {
        override fun updateItem(
            item: Number?,
            empty: Boolean,
        ) {
            super.updateItem(item, empty)
            text = if (empty) null else item?.let(::formatResult)
        }
    }

    @FXML
    private fun showBarChart() = showChart(ChartWindow.Kind.BAR)

    @FXML
    private fun showLineChart() = showChart(ChartWindow.Kind.LINE)

    private fun showChart(kind: ChartWindow.Kind) {
        userList.selectionModel.selectedItem?.let { ChartWindow.show(window(), kind, it, metricCombo.value) }
    }

    // =====================================================================
    // export, about, exit
    // =====================================================================

    @FXML
    private fun saveCsv() {
        chooseSaveFile("CSV files", "csv")?.let { save(it) { path -> CsvReport.save(path, results) } }
    }

    @FXML
    private fun saveXls() {
        chooseSaveFile("Excel files", "xls")?.let { save(it) { path -> XlsReport.save(path, results) } }
    }

    private fun chooseSaveFile(
        description: String,
        extension: String,
    ): File? {
        if (!hasResults.get()) return null
        val chosen =
            FileChooser()
                .apply {
                    title = "Save results as"
                    extensionFilters += FileChooser.ExtensionFilter(description, "*.$extension")
                    initialFileName = "results.$extension"
                    setInitialDirectory()
                }.showSaveDialog(window()) ?: return null
        val file =
            if (chosen.name.endsWith(
                    ".$extension",
                    ignoreCase = true,
                )
            ) {
                chosen
            } else {
                File(chosen.parentFile, "${chosen.name}.$extension")
            }
        rememberDirectory(file)
        return file
    }

    private fun save(
        file: File,
        write: (Path) -> Unit,
    ) {
        try {
            write(file.toPath())
            statusLabel.text = "Results saved to ${file.absolutePath}"
        } catch (e: IOException) {
            log.error("Cannot save results to {}", file, e)
            alert(Alert.AlertType.ERROR, "Saving failed", "Results could not be saved to ${file.absolutePath}")
        }
    }

    @FXML
    private fun printToConsole() {
        if (hasResults.get()) {
            print(ConsoleReport.render(results))
            statusLabel.text = "Results printed to the console."
        }
    }

    @FXML
    private fun about() {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(window())
            title = "About"
            headerText = "Cache Simulator"
            contentText =
                """
                Pavel Bzoch, 2012
                Department of Computer Science and Engineering
                University of West Bohemia
                www.kiv.zcu.cz, www.fav.zcu.cz
                pbzoch@kiv.zcu.cz
                """.trimIndent()
            showAndWait()
        }
    }

    @FXML
    private fun exit() = Platform.exit()

    // =====================================================================
    // helpers
    // =====================================================================

    private fun window(): Window = tabs.scene.window

    private fun FileChooser.setInitialDirectory() {
        val dir = File(settings.workingDirectory).absoluteFile
        when {
            dir.isDirectory -> initialDirectory = dir

            // older versions stored the last file instead of its directory
            dir.parentFile?.isDirectory == true -> initialDirectory = dir.parentFile
        }
    }

    private fun rememberDirectory(file: File) {
        file.absoluteFile.parentFile?.let { dir -> updateSettings { copy(workingDirectory = dir.path) } }
    }

    private fun warn(message: String) = alert(Alert.AlertType.WARNING, null, message)

    private fun alert(
        type: Alert.AlertType,
        header: String?,
        message: String,
    ) {
        Alert(type, message).apply {
            initOwner(window())
            headerText = header
            showAndWait()
        }
    }

    private companion object {
        val log = logger<MainController>()
        val DEFAULT_CAPACITIES = listOf(8, 16, 32, 64, 128, 256, 512, 1024)
    }
}
