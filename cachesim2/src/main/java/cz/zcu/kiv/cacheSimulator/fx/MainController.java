package cz.zcu.kiv.cacheSimulator.fx;

import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.CacheLoader;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.AccessSimulation;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationConfig;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Controller of the main window ({@code /fx/main.fxml}).
 */
public class MainController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  private static final List<Integer> DEFAULT_CAPACITIES = List.of(8, 16, 32, 64, 128, 256, 512, 1024);

  // --- menu & toolbar
  @FXML private Menu requestMenu;
  @FXML private MenuItem simulateMenuItem;
  @FXML private MenuItem cancelMenuItem;
  @FXML private MenuItem saveCsvMenuItem;
  @FXML private MenuItem saveXlsMenuItem;
  @FXML private MenuItem printConsoleMenuItem;
  @FXML private Button simulateButton;
  @FXML private Button cancelButton;
  @FXML private Button saveCsvButton;
  @FXML private Button saveXlsButton;
  @FXML private Button printConsoleButton;
  @FXML private TabPane tabs;
  @FXML private Tab settingsTab;
  @FXML private Tab resultsTab;

  // --- settings tab
  @FXML private ScrollPane settingsContent;
  @FXML private ComboBox<RequestSource> sourceCombo;
  @FXML private Label noSourceHint;
  @FXML private Node pathRow;
  @FXML private TextField pathField;
  @FXML private Node requestCountRow;
  @FXML private Spinner<Integer> requestCountSpinner;
  @FXML private Node generateFilesRow;
  @FXML private Spinner<Integer> generateFilesSpinner;
  @FXML private Node meanRow;
  @FXML private Spinner<Integer> meanSpinner;
  @FXML private Node dispersionRow;
  @FXML private Spinner<Integer> dispersionSpinner;
  @FXML private Node prefDivisibleRow;
  @FXML private Spinner<Integer> prefDivisibleSpinner;
  @FXML private Node prefStepRow;
  @FXML private Spinner<Integer> prefStepSpinner;
  @FXML private Node nonPrefRow;
  @FXML private Spinner<Integer> nonPrefSpinner;
  @FXML private Node zipfRow;
  @FXML private Spinner<Double> zipfSpinner;
  @FXML private Node randomSizesRow;
  @FXML private CheckBox randomSizesCheck;
  @FXML private Node minSizeRow;
  @FXML private Spinner<Integer> minSizeSpinner;
  @FXML private Node maxSizeRow;
  @FXML private Spinner<Integer> maxSizeSpinner;
  @FXML private ListView<Integer> capacityList;
  @FXML private Spinner<Integer> capacitySpinner;
  @FXML private Button removeCapacityButton;
  @FXML private Spinner<Integer> statLimitSpinner;
  @FXML private Spinner<Integer> networkSpeedSpinner;
  @FXML private Spinner<Integer> slidingWindowSpinner;

  // --- cache algorithms tab
  @FXML private SplitPane policiesContent;
  @FXML private VBox policyBox;
  @FXML private StackPane policySettingsPane;

  // --- results tab
  @FXML private ListView<UserStatistics> userList;
  @FXML private ComboBox<ResultMetric> metricCombo;
  @FXML private Button barChartButton;
  @FXML private Button lineChartButton;
  @FXML private Label totalRequestsLabel;
  @FXML private Label totalTrafficLabel;
  @FXML private TableView<ResultRow> resultsTable;

  // --- status bar
  @FXML private ProgressBar progressBar;
  @FXML private Label statusLabel;

  private final List<CheckBox> policyChecks = new ArrayList<>();
  private final ObservableList<Integer> capacities = FXCollections.observableArrayList(DEFAULT_CAPACITIES);
  private final BooleanProperty running = new SimpleBooleanProperty(false);
  private final BooleanProperty hasResults = new SimpleBooleanProperty(false);
  private List<UserStatistics> results = List.of();
  private Task<List<UserStatistics>> simulationTask;

  /**
   * one row of the results table: a policy and its values for all cache sizes
   */
  record ResultRow(String policy, Number[] values) {
  }

  @FXML
  private void initialize() {
    initRequestSettings();
    initCapacities();
    initOtherSettings();
    initPolicies();
    initResults();
    initStateBindings();
  }

  // =====================================================================
  // settings
  // =====================================================================

  private void initRequestSettings() {
    this.sourceCombo.getItems().setAll(RequestSource.values());
    this.sourceCombo.valueProperty().addListener((obs, old, source) -> onSourceChanged(source));
    for (final RequestSource source : RequestSource.values()) {
      final MenuItem item = new MenuItem(source.toString());
      item.setOnAction(e -> {
        this.tabs.getSelectionModel().select(this.settingsTab);
        this.sourceCombo.setValue(source);
      });
      this.requestMenu.getItems().add(item);
    }

    Controls.intSpinner(this.requestCountSpinner, 1, 100_000_000,
      GlobalVariables.getRequestCountForRandomGenerator(), GlobalVariables::setRequestCountForRandomGenerator);
    Controls.intSpinner(this.generateFilesSpinner, 1, 100_000_000,
      GlobalVariables.getFileRequestGeneratorMaxValue(), GlobalVariables::setFileRequestGeneratorMaxValue);
    Controls.intSpinner(this.meanSpinner, 1, 100_000_000,
      GlobalVariables.getFileRequestGeneratorMeanValue(), GlobalVariables::setFileRequestGeneratorMeanValue);
    Controls.intSpinner(this.dispersionSpinner, 1, 100_000_000,
      GlobalVariables.getFileRequestGeneratorDispersion(), GlobalVariables::setFileRequestGeneratorDispersion);
    Controls.intSpinner(this.prefDivisibleSpinner, 1, 1_000_000,
      GlobalVariables.getFileRequestPreferenceFile(), GlobalVariables::setFileRequestPreferenceFile);
    Controls.intSpinner(this.prefStepSpinner, 1, 1_000_000,
      GlobalVariables.getFileRequestPreferenceStep(), GlobalVariables::setFileRequestPreferenceStep);
    Controls.intSpinner(this.nonPrefSpinner, 1, 1_000_000,
      GlobalVariables.getFileRequestnNonPreferenceFile(), GlobalVariables::setFileRequestnNonPreferenceFile);
    Controls.doubleSpinner(this.zipfSpinner, 0.01, 10, GlobalVariables.getZipfLambda(), 0.01,
      GlobalVariables::setZipfLambda);

    // file sizes: minimum must stay below maximum
    Controls.intSpinner(this.minSizeSpinner, 0, Integer.MAX_VALUE - 1, GlobalVariables.getMinGeneratedFileSize(), v -> {
      if (v >= this.maxSizeSpinner.getValue()) {
        this.minSizeSpinner.getValueFactory().setValue(this.maxSizeSpinner.getValue() - 1);
      } else {
        GlobalVariables.setMinGeneratedFileSize(v);
      }
    });
    Controls.intSpinner(this.maxSizeSpinner, 1, Integer.MAX_VALUE, GlobalVariables.getMaxGeneratedFileSize(), v -> {
      if (v <= this.minSizeSpinner.getValue()) {
        this.maxSizeSpinner.getValueFactory().setValue(this.minSizeSpinner.getValue() + 1);
      } else {
        GlobalVariables.setMaxGeneratedFileSize(v);
      }
    });
    this.randomSizesCheck.setSelected(GlobalVariables.isRandomFileSizesForLoggedData());
    this.randomSizesCheck.selectedProperty().addListener((obs, old, selected) ->
      GlobalVariables.setRandomFileSizesForLoggedData(selected));

    // only the inputs of the chosen request source are shown
    showWhen(this.noSourceHint, () -> source() == null);
    showWhen(this.pathRow, () -> source() == RequestSource.AFS_LOG);
    showWhen(this.requestCountRow, () -> source() != null && source().isGenerator());
    showWhen(this.generateFilesRow, () -> source() != null && source().isGenerator());
    showWhen(this.meanRow, () -> source() == RequestSource.GAUSSIAN);
    showWhen(this.dispersionRow, () -> source() == RequestSource.GAUSSIAN);
    showWhen(this.prefDivisibleRow, () -> source() == RequestSource.RANDOM_WITH_PREFERENCE);
    showWhen(this.prefStepRow, () -> source() == RequestSource.RANDOM_WITH_PREFERENCE);
    showWhen(this.nonPrefRow, () -> source() == RequestSource.RANDOM_WITH_PREFERENCE);
    showWhen(this.zipfRow, () -> source() == RequestSource.ZIPF);
    showWhen(this.randomSizesRow, () -> source() != null);
    showWhen(this.minSizeRow, () -> source() != null && this.randomSizesCheck.isSelected());
    showWhen(this.maxSizeRow, () -> source() != null && this.randomSizesCheck.isSelected());
    // generators always need random file sizes; only the AFS log has its own
    this.randomSizesCheck.disableProperty().bind(
      Bindings.createBooleanBinding(() -> source() != RequestSource.AFS_LOG, this.sourceCombo.valueProperty()));
  }

  private RequestSource source() {
    return this.sourceCombo.getValue();
  }

  private void showWhen(final Node node, final BooleanSupplier condition) {
    final BooleanBinding visible = Bindings.createBooleanBinding(condition::getAsBoolean,
      this.sourceCombo.valueProperty(), this.randomSizesCheck.selectedProperty());
    node.visibleProperty().bind(visible);
    node.managedProperty().bind(visible);
  }

  private void onSourceChanged(final RequestSource source) {
    if (source == null) {
      GlobalVariables.setLoadDataFromLog(false);
    } else if (source == RequestSource.AFS_LOG) {
      GlobalVariables.setLoadDataFromLog(true);
      if (this.pathField.getText().isEmpty()) {
        chooseAfsFile();
      }
    } else {
      GlobalVariables.setLoadDataFromLog(false);
      this.randomSizesCheck.setSelected(true);
    }
  }

  @FXML
  private void chooseAfsFile() {
    final FileChooser chooser = new FileChooser();
    chooser.setTitle("Open AFS log file");
    initialDirectory(chooser);
    final File file = chooser.showOpenDialog(window());
    if (file != null) {
      this.pathField.setText(file.getAbsolutePath());
      GlobalVariables.setLogAFSFIleName(file.getAbsolutePath());
      rememberDirectory(file);
    } else if (this.pathField.getText().isEmpty()) {
      this.statusLabel.setText("Choose an AFS log file before running the simulation.");
    }
  }

  private void initCapacities() {
    this.capacityList.setItems(this.capacities);
    Controls.intSpinner(this.capacitySpinner, 1, 1_000_000, 8, v -> { });
    this.removeCapacityButton.disableProperty().bind(
      this.capacityList.getSelectionModel().selectedItemProperty().isNull());
  }

  @FXML
  private void addCapacity() {
    final Integer capacity = this.capacitySpinner.getValue();
    if (this.capacities.contains(capacity)) {
      warn("Cache capacity " + capacity + " MB is already in the list.");
      return;
    }
    this.capacities.add(capacity);
    FXCollections.sort(this.capacities);
    this.capacityList.getSelectionModel().select(capacity);
  }

  @FXML
  private void removeCapacity() {
    final Integer selected = this.capacityList.getSelectionModel().getSelectedItem();
    if (selected != null) {
      this.capacities.remove(selected);
    }
  }

  private void initOtherSettings() {
    Controls.intSpinner(this.statLimitSpinner, 1, 100_000_000,
      GlobalVariables.getLimitForStatistics(), GlobalVariables::setLimitForStatistics);
    Controls.intSpinner(this.networkSpeedSpinner, 1, 10_000,
      GlobalVariables.getAverageNetworkSpeed(), GlobalVariables::setAverageNetworkSpeed);
    Controls.intSpinner(this.slidingWindowSpinner, 0, 75,
      Controls.percent(GlobalVariables.getCacheCapacityForDownloadWindow()),
      GlobalVariables::setCacheCapacityForDownloadWindow);
  }

  // =====================================================================
  // cache algorithms
  // =====================================================================

  private void initPolicies() {
    for (final String cacheInfo : CacheLoader.loadAvailableCaches()) {
      final String[] parts = cacheInfo.split(";");
      final String policy = parts[0];
      final String name = parts.length > 1 ? parts[1] : parts[0];
      final CheckBox check = new CheckBox(name);
      check.setUserData(policy);
      check.setSelected(true);
      check.setMaxWidth(Double.MAX_VALUE);
      check.setOnMouseEntered(e -> showPolicySettings(policy, name));
      check.setOnAction(e -> showPolicySettings(policy, name));
      check.focusedProperty().addListener((obs, old, focused) -> {
        if (focused) {
          showPolicySettings(policy, name);
        }
      });
      this.policyChecks.add(check);
      this.policyBox.getChildren().add(check);
    }
    if (this.policyChecks.isEmpty()) {
      LOG.error("No cache policies found");
    }
  }

  private void showPolicySettings(final String policy, final String name) {
    this.policySettingsPane.getChildren().setAll(PolicySettings.create(policy, name));
  }

  @FXML
  private void selectAllPolicies() {
    this.policyChecks.forEach(c -> c.setSelected(true));
  }

  @FXML
  private void selectNoPolicies() {
    this.policyChecks.forEach(c -> c.setSelected(false));
  }

  private List<String> selectedPolicies() {
    return this.policyChecks.stream()
      .filter(CheckBox::isSelected)
      .map(c -> (String) c.getUserData())
      .toList();
  }

  // =====================================================================
  // simulation
  // =====================================================================

  private void initStateBindings() {
    final BooleanBinding exportDisabled = this.running.or(this.hasResults.not());
    this.simulateButton.disableProperty().bind(this.running);
    this.simulateMenuItem.disableProperty().bind(this.running);
    this.cancelButton.disableProperty().bind(this.running.not());
    this.cancelMenuItem.disableProperty().bind(this.running.not());
    this.requestMenu.disableProperty().bind(this.running);
    for (final Button b : List.of(this.saveCsvButton, this.saveXlsButton, this.printConsoleButton)) {
      b.disableProperty().bind(exportDisabled);
    }
    for (final MenuItem m : List.of(this.saveCsvMenuItem, this.saveXlsMenuItem, this.printConsoleMenuItem)) {
      m.disableProperty().bind(exportDisabled);
    }
    this.settingsContent.disableProperty().bind(this.running);
    this.policiesContent.disableProperty().bind(this.running);
    this.resultsTab.disableProperty().bind(exportDisabled);
    this.progressBar.visibleProperty().bind(this.running);
    this.progressBar.managedProperty().bind(this.running);
  }

  private boolean checkSettings() {
    final RequestSource source = source();
    if (source == null) {
      return invalid(this.settingsTab, "You have to select a request input method.");
    }
    if (source == RequestSource.AFS_LOG && !new File(GlobalVariables.getLogAFSFIleName()).isFile()) {
      return invalid(this.settingsTab, "You have to select an AFS log file.");
    }
    if (this.capacities.isEmpty()) {
      return invalid(this.settingsTab, "You have to add at least one cache capacity.");
    }
    if (selectedPolicies().isEmpty()) {
      return invalid(this.tabs.getTabs().get(1), "You have to select at least one cache algorithm.");
    }
    return true;
  }

  private boolean invalid(final Tab tab, final String message) {
    this.tabs.getSelectionModel().select(tab);
    alert(Alert.AlertType.ERROR, "Cannot start the simulation", message);
    return false;
  }

  @FXML
  private void simulate() {
    if (this.running.get() || !checkSettings()) {
      return;
    }
    GlobalMethods.setGenerators();
    final IFileQueue queue = source().createQueue();
    final var simulation = new AccessSimulation(queue, new SimulationConfig(selectedPolicies(), this.capacities));
    final boolean fromLogWithSizes = GlobalVariables.isLoadDataFromLog()
      && !GlobalVariables.isRandomFileSizesForLoggedData();
    final Instant start = Instant.now();

    final Task<List<UserStatistics>> task = new Task<>() {
      @Override
      protected List<UserStatistics> call() {
        try {
          simulation.setProgressListener(percent -> updateProgress(percent, 100));
          Server.getInstance().hardReset();
          if (fromLogWithSizes) {
            simulation.simulateFromLogFile();
          } else {
            simulation.simulateRandomFileSizes();
          }
          return isCancelled() ? List.of() : simulation.getResults();
        } finally {
          // allow a new run only once this thread has really stopped
          Platform.runLater(() -> MainController.this.running.set(false));
        }
      }
    };
    task.setOnSucceeded(e -> onSimulationFinished(task.getValue(), Duration.between(start, Instant.now())));
    task.setOnCancelled(e -> {
      this.statusLabel.textProperty().unbind();
      this.statusLabel.setText("Simulation cancelled.");
    });
    task.setOnFailed(e -> {
      this.statusLabel.textProperty().unbind();
      this.statusLabel.setText("Simulation failed.");
      LOG.error("Simulation failed", task.getException());
      alert(Alert.AlertType.ERROR, "Simulation failed", String.valueOf(task.getException()));
    });

    this.hasResults.set(false);
    this.running.set(true);
    this.tabs.getSelectionModel().select(this.settingsTab);
    this.progressBar.progressProperty().bind(task.progressProperty());
    this.statusLabel.textProperty().bind(Bindings.createStringBinding(
      () -> String.format("Simulation in progress… %.0f %%", Math.max(0, task.getProgress()) * 100),
      task.progressProperty()));
    this.simulationTask = task;

    final Thread thread = new Thread(task, "simulation");
    thread.setDaemon(true);
    thread.start();
  }

  @FXML
  private void cancel() {
    if (this.simulationTask != null && this.simulationTask.isRunning()) {
      this.simulationTask.cancel(true);
    }
  }

  private void onSimulationFinished(final List<UserStatistics> stats, final Duration took) {
    this.statusLabel.textProperty().unbind();
    if (stats == null || stats.isEmpty()) {
      this.statusLabel.setText("Simulation finished without results.");
      alert(Alert.AlertType.WARNING, "There are no results",
        "No simulated user made enough requests. Try lowering \"Minimum requests for including a user\".");
      return;
    }
    this.results = stats;
    this.hasResults.set(true);
    this.userList.getItems().setAll(stats);
    this.userList.getSelectionModel().selectFirst();
    this.tabs.getSelectionModel().select(this.resultsTab);
    this.statusLabel.setText(String.format("Simulation finished in %.1f s.", took.toMillis() / 1000.0));
  }

  // =====================================================================
  // results
  // =====================================================================

  private void initResults() {
    this.metricCombo.getItems().setAll(ResultMetric.values());
    this.metricCombo.getSelectionModel().selectFirst();
    this.metricCombo.valueProperty().addListener((obs, old, metric) -> refreshResults());
    this.userList.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(final UserStatistics item, final boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : userName(item));
      }
    });
    this.userList.getSelectionModel().selectedItemProperty().addListener((obs, old, user) -> refreshResults());
    this.resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    final BooleanBinding noUser = this.userList.getSelectionModel().selectedItemProperty().isNull();
    this.barChartButton.disableProperty().bind(noUser);
    this.lineChartButton.disableProperty().bind(noUser);
  }

  private static String userName(final UserStatistics stats) {
    final long userId = stats.getUserID();
    if (userId == 0) {
      return "Simulated user";
    }
    final long id = userId >> 32;
    return id + ", ip: " + GlobalMethods.intToIp(userId - (id << 32));
  }

  private void refreshResults() {
    final UserStatistics stats = this.userList.getSelectionModel().getSelectedItem();
    final ResultMetric metric = this.metricCombo.getValue();
    this.resultsTable.getColumns().clear();
    this.resultsTable.getItems().clear();
    if (stats == null || metric == null) {
      this.totalRequestsLabel.setText("");
      this.totalTrafficLabel.setText("");
      return;
    }
    this.totalRequestsLabel.setText(format(stats.getFileAccessed()));
    this.totalTrafficLabel.setText(format(stats.getTotalNetworkBandwidth() / 1024 / 1024) + " MB");

    final TableColumn<ResultRow, String> policyColumn = new TableColumn<>("Cache Policy");
    policyColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().policy()));
    policyColumn.setMinWidth(140);
    this.resultsTable.getColumns().add(policyColumn);

    final Integer[] sizes = stats.getCacheSizes();
    for (int i = 0; i < sizes.length; i++) {
      final int index = i;
      final TableColumn<ResultRow, Number> column = new TableColumn<>(sizes[i] + " MB");
      column.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().values()[index]));
      column.setComparator(Comparator.comparingDouble(Number::doubleValue));
      column.setCellFactory(c -> new TableCell<>() {
        @Override
        protected void updateItem(final Number item, final boolean empty) {
          super.updateItem(item, empty);
          setText(empty || item == null ? null : format(item));
        }
      });
      column.getStyleClass().add("numeric");
      this.resultsTable.getColumns().add(column);
    }

    for (final String cacheName : stats.getCacheNames()) {
      this.resultsTable.getItems().add(new ResultRow(cacheName, metric.values(stats, cacheName)));
    }
  }

  static String format(final Number value) {
    final DecimalFormat format = (value instanceof Double || value instanceof Float)
      ? new DecimalFormat("#,##0.00")
      : new DecimalFormat("#,##0");
    return format.format(value);
  }

  @FXML
  private void showBarChart() {
    showChart(ChartWindow.Kind.BAR);
  }

  @FXML
  private void showLineChart() {
    showChart(ChartWindow.Kind.LINE);
  }

  private void showChart(final ChartWindow.Kind kind) {
    final UserStatistics stats = this.userList.getSelectionModel().getSelectedItem();
    if (stats != null) {
      ChartWindow.show(window(), kind, stats, this.metricCombo.getValue());
    }
  }

  // =====================================================================
  // export, about, exit
  // =====================================================================

  @FXML
  private void saveCsv() {
    final File file = chooseSaveFile("CSV files", "csv");
    if (file != null) {
      reportSaved(file, Output.saveStatToCSV(file.getAbsolutePath(), this.results));
    }
  }

  @FXML
  private void saveXls() {
    final File file = chooseSaveFile("Excel files", "xls");
    if (file != null) {
      reportSaved(file, Output.saveStatToXLS(file.getAbsolutePath(), this.results));
    }
  }

  private File chooseSaveFile(final String description, final String extension) {
    if (!this.hasResults.get()) {
      return null;
    }
    final FileChooser chooser = new FileChooser();
    chooser.setTitle("Save results as");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, "*." + extension));
    chooser.setInitialFileName("results." + extension);
    initialDirectory(chooser);
    File file = chooser.showSaveDialog(window());
    if (file == null) {
      return null;
    }
    if (!file.getName().toLowerCase().endsWith("." + extension)) {
      file = new File(file.getParentFile(), file.getName() + "." + extension);
    }
    rememberDirectory(file);
    return file;
  }

  private void reportSaved(final File file, final boolean ok) {
    if (ok) {
      this.statusLabel.setText("Results saved to " + file.getAbsolutePath());
    } else {
      alert(Alert.AlertType.ERROR, "Saving failed", "Results could not be saved to " + file.getAbsolutePath());
    }
  }

  @FXML
  private void printToConsole() {
    if (this.hasResults.get()) {
      Output.printAllStatConsole(this.results);
      this.statusLabel.setText("Results printed to the console.");
    }
  }

  @FXML
  private void about() {
    final Alert about = new Alert(Alert.AlertType.INFORMATION);
    about.initOwner(window());
    about.setTitle("About");
    about.setHeaderText("Cache Simulator");
    about.setContentText("""
      Pavel Bzoch, 2012
      Department of Computer Science and Engineering
      University of West Bohemia
      www.kiv.zcu.cz, www.fav.zcu.cz
      pbzoch@kiv.zcu.cz""");
    about.showAndWait();
  }

  @FXML
  private void exit() {
    Platform.exit();
  }

  // =====================================================================
  // helpers
  // =====================================================================

  private Window window() {
    return this.tabs.getScene().getWindow();
  }

  private static void initialDirectory(final FileChooser chooser) {
    final File dir = new File(GlobalVariables.getActDir());
    if (dir.isDirectory()) {
      chooser.setInitialDirectory(dir.getAbsoluteFile());
    } else if (dir.getParentFile() != null && dir.getParentFile().isDirectory()) {
      // older versions stored the last file instead of its directory
      chooser.setInitialDirectory(dir.getAbsoluteFile().getParentFile());
    }
  }

  private static void rememberDirectory(final File file) {
    final File dir = file.getAbsoluteFile().getParentFile();
    if (dir != null) {
      GlobalVariables.setActDir(dir.getPath());
    }
  }

  private void warn(final String message) {
    alert(Alert.AlertType.WARNING, null, message);
  }

  private void alert(final Alert.AlertType type, final String header, final String message) {
    final Alert alert = new Alert(type, message);
    alert.initOwner(window());
    alert.setHeaderText(header);
    alert.showAndWait();
  }
}
