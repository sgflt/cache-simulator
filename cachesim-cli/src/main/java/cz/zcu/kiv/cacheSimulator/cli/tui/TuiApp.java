package cz.zcu.kiv.cacheSimulator.cli.tui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ProgressBar;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import cz.zcu.kiv.cacheSimulator.cli.Policies;
import cz.zcu.kiv.cacheSimulator.cli.ResultTable;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.RequestSource;
import cz.zcu.kiv.cacheSimulator.simulation.ResultMetric;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationConfig;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationRunner;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

/**
 * Interactive terminal UI: set up a simulation, watch it run, browse the results.
 */
public final class TuiApp {

  private static final Logger LOG = LoggerFactory.getLogger(TuiApp.class);

  private static final String DEFAULT_SIZES = "8,16,32,64,128,256,512,1024";
  private static final List<String> DEFAULT_POLICIES = List.of("LRU", "LFU_SS", "ARC", "LIRS", "_2Q", "FIFO");

  private final WindowBasedTextGUI gui;

  private final ComboBox<RequestSource> source = new ComboBox<>(RequestSource.values());
  private final TextBox logFile = new TextBox(new TerminalSize(30, 1));
  private final ComboBox<String> logSizes = new ComboBox<>("from the log", "generated");
  private final TextBox requests = number(GlobalVariables.getRequestCountForRandomGenerator());
  private final TextBox files = number(GlobalVariables.getFileRequestGeneratorMaxValue());
  private final TextBox zipfLambda = new TextBox(new TerminalSize(12, 1), Double.toString(GlobalVariables.getZipfLambda()));
  private final TextBox minFileSize = number(GlobalVariables.getMinGeneratedFileSize());
  private final TextBox maxFileSize = number(GlobalVariables.getMaxGeneratedFileSize());
  private final TextBox minUserRequests = number(GlobalVariables.getLimitForStatistics());
  private final TextBox sizes = new TextBox(new TerminalSize(30, 1), DEFAULT_SIZES);
  private final CheckBoxList<Policies.Policy> policies = new CheckBoxList<>(new TerminalSize(24, 14));

  private TuiApp(final WindowBasedTextGUI gui) {
    this.gui = gui;
  }

  /**
   * @return process exit code
   */
  public static int run() {
    try (Screen screen = new DefaultTerminalFactory().createScreen()) {
      screen.startScreen();
      final var gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
      new TuiApp(gui).showSetup();
      return 0;
    } catch (final IOException e) {
      System.err.println("Cannot open the terminal UI: " + e.getMessage());
      return 1;
    }
  }

  private static TextBox number(final long value) {
    return new TextBox(new TerminalSize(12, 1), Long.toString(value)).setValidationPattern(Pattern.compile("[0-9]*"));
  }

  // ---------------------------------------------------------------- setup

  private void showSetup() {
    final BasicWindow window = new BasicWindow("Cache simulator");
    window.setHints(List.of(Window.Hint.CENTERED));

    this.source.setSelectedItem(RequestSource.ZIPF);
    this.logFile.setText(GlobalVariables.getLogAFSFIleName());
    for (final Policies.Policy policy : Policies.available()) {
      this.policies.addItem(policy, DEFAULT_POLICIES.contains(policy.className()));
    }

    final Panel settings = new Panel(new GridLayout(2));
    row(settings, "Requests from:", this.source);
    final Panel logRow = new Panel(new LinearLayout(Direction.HORIZONTAL));
    logRow.addComponent(this.logFile);
    logRow.addComponent(new Button("...", this::chooseLogFile));
    row(settings, "AFS log file:", logRow);
    row(settings, "File sizes (log):", this.logSizes);
    row(settings, "Requests count:", this.requests);
    row(settings, "Generated files:", this.files);
    row(settings, "Zipf lambda:", this.zipfLambda);
    row(settings, "Min file size [B]:", this.minFileSize);
    row(settings, "Max file size [B]:", this.maxFileSize);
    row(settings, "Min user requests:", this.minUserRequests);
    row(settings, "Cache sizes [MB]:", this.sizes);

    final Panel policyPanel = new Panel(new LinearLayout(Direction.VERTICAL));
    policyPanel.addComponent(this.policies);
    final Panel policyButtons = new Panel(new LinearLayout(Direction.HORIZONTAL));
    policyButtons.addComponent(new Button("All", () -> checkAll(true)));
    policyButtons.addComponent(new Button("None", () -> checkAll(false)));
    policyPanel.addComponent(policyButtons);

    final Panel columns = new Panel(new LinearLayout(Direction.HORIZONTAL));
    columns.addComponent(settings.withBorder(Borders.singleLine("Settings")));
    columns.addComponent(policyPanel.withBorder(Borders.singleLine("Policies")));

    final Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));
    buttons.addComponent(new Button("Run", this::startSimulation));
    buttons.addComponent(new Button("Quit", window::close));

    final Panel root = new Panel(new LinearLayout(Direction.VERTICAL));
    root.addComponent(columns);
    root.addComponent(new Label("Tab/arrows move, Space toggles a policy, Enter presses a button."));
    root.addComponent(buttons);
    window.setComponent(root);
    this.gui.addWindowAndWait(window);
  }

  private static void row(final Panel panel, final String label, final Component component) {
    panel.addComponent(new Label(label));
    panel.addComponent(component);
  }

  private void checkAll(final boolean checked) {
    for (int i = 0; i < this.policies.getItemCount(); i++) {
      this.policies.setChecked(this.policies.getItemAt(i), checked);
    }
  }

  private void chooseLogFile() {
    final File file = new FileDialogBuilder()
      .setTitle("AFS log file")
      .setActionLabel("Open")
      .setSelectedFile(new File(this.logFile.getText().isBlank() ? "." : this.logFile.getText()))
      .build()
      .showDialog(this.gui);
    if (file != null) {
      this.logFile.setText(file.getAbsolutePath());
      this.source.setSelectedItem(RequestSource.AFS_LOG);
    }
  }

  /**
   * copies the form into {@link GlobalVariables}
   *
   * @return what to simulate, or null after showing what is wrong
   */
  private SimulationConfig readForm() {
    try {
      final RequestSource requestSource = this.source.getSelectedItem();
      if (requestSource == RequestSource.AFS_LOG) {
        if (!new File(this.logFile.getText()).isFile()) {
          return invalid("Choose an existing AFS log file.");
        }
        GlobalVariables.setLogAFSFIleName(new File(this.logFile.getText()).getAbsolutePath());
        GlobalVariables.setRandomFileSizesForLoggedData(this.logSizes.getSelectedIndex() == 1);
      } else {
        GlobalVariables.setRandomFileSizesForLoggedData(true);
      }
      GlobalVariables.setRequestCountForRandomGenerator(positive(this.requests, "Requests count"));
      GlobalVariables.setFileRequestGeneratorMaxValue(positive(this.files, "Generated files"));
      GlobalVariables.setZipfLambda(Double.parseDouble(this.zipfLambda.getText().trim()));
      final int min = positive(this.minFileSize, "Min file size");
      final int max = positive(this.maxFileSize, "Max file size");
      if (min > max) {
        return invalid("Min file size must not be bigger than max file size.");
      }
      GlobalVariables.setMinGeneratedFileSize(min);
      GlobalVariables.setMaxGeneratedFileSize(max);
      GlobalVariables.setLimitForStatistics(Integer.parseInt(this.minUserRequests.getText().trim()));

      final List<Integer> capacities = Arrays.stream(this.sizes.getText().split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).sorted().distinct().toList();
      if (capacities.isEmpty() || capacities.getFirst() <= 0) {
        return invalid("Cache sizes must be positive numbers separated by commas.");
      }
      final List<String> selected = this.policies.getCheckedItems().stream().map(Policies.Policy::className).toList();
      if (selected.isEmpty()) {
        return invalid("Select at least one policy.");
      }
      return new SimulationConfig(selected, capacities);
    } catch (final NumberFormatException e) {
      return invalid("Not a number: " + e.getMessage());
    }
  }

  private static int positive(final TextBox box, final String name) {
    final int value = Integer.parseInt(box.getText().trim());
    if (value <= 0) {
      throw new NumberFormatException(name + " must be positive");
    }
    return value;
  }

  private SimulationConfig invalid(final String message) {
    MessageDialog.showMessageDialog(this.gui, "Cannot start the simulation", message, MessageDialogButton.OK);
    return null;
  }

  // ---------------------------------------------------------------- running

  private void startSimulation() {
    final SimulationConfig config = readForm();
    if (config == null) {
      return;
    }
    final RequestSource requestSource = this.source.getSelectedItem();

    final BasicWindow window = new BasicWindow("Simulating");
    window.setHints(List.of(Window.Hint.CENTERED, Window.Hint.MODAL));
    final ProgressBar bar = new ProgressBar(0, 100, 40);
    bar.setLabelFormat("%2.0f%%");
    final Label status = new Label(config.cacheNames().size() + " policies x " + config.cacheSizes().size() + " sizes");
    final AtomicBoolean cancelled = new AtomicBoolean();
    final Instant start = Instant.now();
    final IntConsumer progress = percent -> this.gui.getGUIThread().invokeLater(() -> {
      bar.setValue(percent);
      status.setText("Elapsed " + Duration.between(start, Instant.now()).toSeconds() + " s");
    });

    final Thread worker = new Thread(() -> {
      List<UserStatistics> results = List.of();
      Throwable failure = null;
      try {
        results = SimulationRunner.run(requestSource, config, progress::accept);
      } catch (final Throwable e) {
        LOG.error("Simulation failed", e);
        failure = e;
      }
      final List<UserStatistics> finalResults = results;
      final Throwable finalFailure = failure;
      this.gui.getGUIThread().invokeLater(() -> {
        window.close();
        onFinished(finalResults, finalFailure, cancelled.get(), Duration.between(start, Instant.now()));
      });
    }, "simulation");
    worker.setDaemon(true);

    final Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
    panel.addComponent(bar);
    panel.addComponent(status);
    panel.addComponent(new Button("Cancel", () -> {
      cancelled.set(true);
      status.setText("Cancelling...");
      worker.interrupt();
    }));
    window.setComponent(panel);
    this.gui.addWindow(window);
    worker.start();
  }

  private void onFinished(final List<UserStatistics> results, final Throwable failure, final boolean cancelled,
                          final Duration took) {
    if (failure != null) {
      MessageDialog.showMessageDialog(this.gui, "Simulation failed", String.valueOf(failure), MessageDialogButton.OK);
    } else if (cancelled) {
      MessageDialog.showMessageDialog(this.gui, "Cache simulator", "Simulation cancelled.", MessageDialogButton.OK);
    } else if (results.isEmpty()) {
      MessageDialog.showMessageDialog(this.gui, "No results",
        "No user made enough requests. Try lowering \"Min user requests\".", MessageDialogButton.OK);
    } else {
      showResults(results, took);
    }
  }

  // ---------------------------------------------------------------- results

  /**
   * shows a user in the combo box by its label
   */
  private record UserItem(UserStatistics stats) {
    @Override
    public String toString() {
      return this.stats.getUserLabel();
    }
  }

  private void showResults(final List<UserStatistics> results, final Duration took) {
    final BasicWindow window = new BasicWindow("Results (" + took.toMillis() + " ms)");
    window.setHints(List.of(Window.Hint.CENTERED));

    final ComboBox<UserItem> user = new ComboBox<>(results.stream().map(UserItem::new).toList());
    final ComboBox<ResultMetric> metric = new ComboBox<>(ResultMetric.values());
    final Label summary = new Label("");
    final Table<String> table = new Table<>("Policy");
    table.setVisibleRows(Math.min(20, results.getFirst().getCacheNames().size()));

    final Runnable refresh = () -> {
      final UserStatistics stats = user.getSelectedItem().stats();
      summary.setText(ResultTable.header(stats));
      final List<List<String>> cells = ResultTable.cells(stats, metric.getSelectedItem());
      final TableModel<String> model = new TableModel<>(cells.getFirst().toArray(String[]::new));
      for (final List<String> row : cells.subList(1, cells.size())) {
        model.addRow(row);
      }
      table.setTableModel(model);
    };
    user.addListener((selected, previous, byUser) -> refresh.run());
    metric.addListener((selected, previous, byUser) -> refresh.run());
    refresh.run();

    final Panel selectors = new Panel(new GridLayout(2));
    row(selectors, "User:", user);
    row(selectors, "Show:", metric);

    final Panel buttons = new Panel(new LinearLayout(Direction.HORIZONTAL));
    buttons.addComponent(new Button("Save CSV", () -> save(results, "results.csv", false)));
    buttons.addComponent(new Button("Save XLS", () -> save(results, "results.xls", true)));
    buttons.addComponent(new Button("Back", window::close));

    final Panel root = new Panel(new LinearLayout(Direction.VERTICAL));
    root.addComponent(selectors);
    root.addComponent(summary);
    root.addComponent(table.withBorder(Borders.singleLine()));
    root.addComponent(buttons);
    window.setComponent(root);
    this.gui.addWindow(window);
  }

  private void save(final List<UserStatistics> results, final String defaultName, final boolean xls) {
    final String name = TextInputDialog.showDialog(this.gui, "Save results", "File name:", defaultName);
    if (name == null || name.isBlank()) {
      return;
    }
    final boolean saved = xls ? Output.saveStatToXLS(name, results) : Output.saveStatToCSV(name, results);
    MessageDialog.showMessageDialog(this.gui, "Save results",
      saved ? "Saved " + new File(name).getAbsolutePath() : "Cannot write " + name, MessageDialogButton.OK);
  }
}
