package cz.zcu.kiv.cacheSimulator.cli;

import cz.zcu.kiv.cacheSimulator.cli.tui.TuiApp;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.RequestSource;
import cz.zcu.kiv.cacheSimulator.simulation.ResultMetric;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationConfig;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationProgressListener;
import cz.zcu.kiv.cacheSimulator.simulation.SimulationRunner;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Command-line entry point. Without a subcommand it opens the interactive terminal UI.
 */
@Command(
  name = "cachesim",
  mixinStandardHelpOptions = true,
  version = "Cache simulator 3.0-alpha",
  description = "Simulates client-side file cache policies.%nRun without a command to open the interactive terminal UI.",
  subcommands = {CliMain.ListPolicies.class, CliMain.Run.class, CliMain.Tui.class}
)
public final class CliMain implements Callable<Integer> {

  static final String CONFIG_FILE = "config.xml";

  @Spec
  private CommandLine.Model.CommandSpec spec;

  public static void main(final String[] args) {
    System.exit(commandLine().execute(args));
  }

  static CommandLine commandLine() {
    final var commandLine = new CommandLine(new CliMain());
    commandLine.setCaseInsensitiveEnumValuesAllowed(true);
    commandLine.registerConverter(RequestSource.class, value -> parseEnum(RequestSource.class, value));
    commandLine.registerConverter(ResultMetric.class, value -> parseEnum(ResultMetric.class, value));
    return commandLine;
  }

  @Override
  public Integer call() {
    if (!isTerminal()) {
      this.spec.commandLine().usage(this.spec.commandLine().getOut());
      return 2;
    }
    loadConfigFile();
    return TuiApp.run();
  }

  /**
   * true when attached to an interactive terminal, not a pipe or a file
   */
  static boolean isTerminal() {
    return System.console() != null && System.console().isTerminal();
  }

  /**
   * reads ./config.xml when present, like the GUI does
   */
  static void loadConfigFile() {
    final File config = new File(CONFIG_FILE);
    if (config.isFile()) {
      ConfigReaderWriter.read();
    }
  }

  /**
   * accepts "afs-log", "AFS_LOG", "saved-bytes-ratio", ...
   */
  static <E extends Enum<E>> E parseEnum(final Class<E> type, final String value) {
    final String name = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    try {
      return Enum.valueOf(type, name);
    } catch (final IllegalArgumentException e) {
      throw new CommandLine.TypeConversionException("'" + value + "' is not one of "
        + Arrays.stream(type.getEnumConstants()).map(CliMain::optionName).toList());
    }
  }

  static String optionName(final Enum<?> value) {
    return value.name().toLowerCase(Locale.ROOT).replace('_', '-');
  }

  @Command(name = "list-policies", description = "Lists the cache policies that can be simulated.")
  static final class ListPolicies implements Callable<Integer> {

    @Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() {
      final PrintWriter out = this.spec.commandLine().getOut();
      final List<Policies.Policy> policies = Policies.available();
      final int width = policies.stream().mapToInt(p -> p.className().length()).max().orElse(0);
      for (final Policies.Policy policy : policies) {
        out.printf("%-" + width + "s  %s%n", policy.className(), policy.displayName());
      }
      out.flush();
      return 0;
    }
  }

  @Command(name = "tui", description = "Opens the interactive terminal UI.")
  static final class Tui implements Callable<Integer> {

    @Override
    public Integer call() {
      loadConfigFile();
      return TuiApp.run();
    }
  }

  enum Format { TABLE, CSV, XLS }

  @Command(
    name = "run",
    sortOptions = false,
    description = {
      "Runs a simulation and prints the results.",
      "Settings not given here come from ./config.xml when it exists, otherwise from built-in defaults."
    }
  )
  static final class Run implements Callable<Integer> {

    @Spec
    private CommandLine.Model.CommandSpec spec;

    @Option(names = {"-s", "--source"}, paramLabel = "SOURCE",
      description = "Where requests come from: afs-log, gaussian, random, random-with-preference, zipf."
        + " Default: afs-log with --log, otherwise zipf.")
    RequestSource source;

    @Option(names = {"-l", "--log"}, paramLabel = "FILE", description = "AFS log file to replay.")
    Path log;

    @Option(names = "--random-sizes",
      description = "With an AFS log: generate file sizes instead of using the sizes in the log.")
    boolean randomSizes;

    @Option(names = {"-p", "--policies"}, split = ",", paramLabel = "NAME",
      description = "Policies to simulate, by class or display name (see list-policies). Default: all.")
    List<String> policies = new ArrayList<>();

    @Option(names = {"-c", "--sizes"}, split = ",", paramLabel = "MB",
      description = "Cache capacities in MB. Default: ${DEFAULT-VALUE}.", defaultValue = "8,16,32,64,128,256,512,1024")
    List<Integer> sizes;

    @Option(names = {"-n", "--requests"}, paramLabel = "N", description = "Number of generated requests.")
    Integer requests;

    @Option(names = "--files", paramLabel = "N", description = "Number of distinct generated files.")
    Integer files;

    @Option(names = "--zipf-lambda", paramLabel = "X", description = "Zipf generator lambda.")
    Double zipfLambda;

    @Option(names = "--mean", paramLabel = "N", description = "Mean file ID for the Gaussian generator.")
    Integer mean;

    @Option(names = "--dispersion", paramLabel = "N", description = "Dispersion for the Gaussian generator.")
    Integer dispersion;

    @Option(names = "--min-file-size", paramLabel = "BYTES", description = "Smallest generated file size.")
    Integer minFileSize;

    @Option(names = "--max-file-size", paramLabel = "BYTES", description = "Largest generated file size.")
    Integer maxFileSize;

    @Option(names = "--min-user-requests", paramLabel = "N",
      description = "Leave out users with fewer requests than this.")
    Integer minUserRequests;

    @Option(names = "--network-speed", paramLabel = "MBIT", description = "Average network speed in Mbit/s.")
    Integer networkSpeed;

    @Option(names = "--sliding-window", paramLabel = "PERCENT",
      description = "Sliding window as %% of cache capacity.")
    Integer slidingWindow;

    @Option(names = {"-m", "--metric"}, split = ",", paramLabel = "METRIC",
      description = "Tables to print: hit-ratio, hit-count, saved-bytes-ratio, saved-bytes,"
        + " transfer-decrease-ratio, transfer-decrease. Default: hit-ratio,saved-bytes-ratio.")
    List<ResultMetric> metrics = new ArrayList<>();

    @Option(names = {"-f", "--format"}, paramLabel = "FORMAT",
      description = "Output format: table, csv or xls. Default: ${DEFAULT-VALUE}.", defaultValue = "table")
    Format format;

    @Option(names = {"-o", "--out"}, paramLabel = "FILE", description = "Write results to this file (required for xls).")
    Path out;

    @Option(names = {"-q", "--quiet"}, description = "Do not show the progress bar.")
    boolean quiet;

    @Override
    public Integer call() throws Exception {
      final RequestSource requestSource = this.source != null ? this.source
        : this.log != null ? RequestSource.AFS_LOG : RequestSource.ZIPF;
      validate(requestSource);
      final List<String> policyNames;
      try {
        policyNames = Policies.resolve(this.policies);
      } catch (final IllegalArgumentException e) {
        throw new ParameterException(this.spec.commandLine(), e.getMessage());
      }

      loadConfigFile();
      applySettings(requestSource);

      final var config = new SimulationConfig(policyNames, this.sizes.stream().sorted().distinct().toList());
      final List<UserStatistics> results = simulate(requestSource, config);
      if (results == null) {
        this.spec.commandLine().getErr().println("Simulation cancelled.");
        return 130;
      }
      if (results.isEmpty()) {
        this.spec.commandLine().getErr().println(
          "No user made enough requests. Try a lower --min-user-requests.");
        return 1;
      }
      return write(results);
    }

    private void validate(final RequestSource requestSource) {
      final var commandLine = this.spec.commandLine();
      if (requestSource == RequestSource.AFS_LOG) {
        if (this.log == null) {
          throw new ParameterException(commandLine, "--source afs-log needs --log FILE");
        }
        if (!Files.isRegularFile(this.log)) {
          throw new ParameterException(commandLine, "Log file not found: " + this.log);
        }
      } else if (this.log != null) {
        throw new ParameterException(commandLine, "--log only works with --source afs-log");
      }
      if (this.sizes.isEmpty() || this.sizes.stream().anyMatch(size -> size <= 0)) {
        throw new ParameterException(commandLine, "--sizes must be positive numbers");
      }
      if (this.format == Format.XLS && this.out == null) {
        throw new ParameterException(commandLine, "--format xls needs --out FILE");
      }
    }

    private void applySettings(final RequestSource requestSource) {
      if (requestSource == RequestSource.AFS_LOG) {
        GlobalVariables.setLogAFSFIleName(this.log.toAbsolutePath().toString());
        GlobalVariables.setRandomFileSizesForLoggedData(this.randomSizes);
      } else {
        GlobalVariables.setRandomFileSizesForLoggedData(true);
      }
      set(this.requests, GlobalVariables::setRequestCountForRandomGenerator);
      set(this.files, GlobalVariables::setFileRequestGeneratorMaxValue);
      set(this.zipfLambda, GlobalVariables::setZipfLambda);
      set(this.mean, GlobalVariables::setFileRequestGeneratorMeanValue);
      set(this.dispersion, GlobalVariables::setFileRequestGeneratorDispersion);
      set(this.minFileSize, GlobalVariables::setMinGeneratedFileSize);
      set(this.maxFileSize, GlobalVariables::setMaxGeneratedFileSize);
      set(this.minUserRequests, GlobalVariables::setLimitForStatistics);
      set(this.networkSpeed, GlobalVariables::setAverageNetworkSpeed);
      set(this.slidingWindow, GlobalVariables::setCacheCapacityForDownloadWindow);
    }

    private static <T> void set(final T value, final Consumer<T> setter) {
      if (value != null) {
        setter.accept(value);
      }
    }

    /**
     * runs in its own thread so Ctrl+C can stop it cleanly
     *
     * @return results, or null when cancelled
     */
    private List<UserStatistics> simulate(final RequestSource requestSource, final SimulationConfig config)
      throws InterruptedException {
      final boolean showProgress = !this.quiet && isTerminal();
      final ProgressBar bar = new ProgressBar(System.err);
      final SimulationProgressListener listener = showProgress ? bar : SimulationProgressListener.NONE;
      final AtomicReference<List<UserStatistics>> results = new AtomicReference<>();
      final AtomicReference<Throwable> failure = new AtomicReference<>();
      final AtomicBoolean cancelled = new AtomicBoolean();
      final Thread worker = new Thread(() -> {
        try {
          results.set(SimulationRunner.run(requestSource, config, listener));
        } catch (final Throwable e) {
          failure.set(e);
        }
      }, "simulation");
      final Thread cancel = new Thread(() -> {
        cancelled.set(true);
        worker.interrupt();
        try {
          worker.join(5000);
        } catch (final InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
        if (showProgress) {
          bar.finish();
        }
        System.err.println("Simulation cancelled.");
      }, "cancel-simulation");
      Runtime.getRuntime().addShutdownHook(cancel);
      worker.start();
      worker.join();
      Runtime.getRuntime().removeShutdownHook(cancel);
      if (showProgress) {
        bar.finish();
      }
      if (failure.get() != null) {
        throw new IllegalStateException("Simulation failed", failure.get());
      }
      return cancelled.get() ? null : results.get();
    }

    private int write(final List<UserStatistics> results) throws IOException {
      final PrintWriter stdout = this.spec.commandLine().getOut();
      switch (this.format) {
        case TABLE -> {
          final Set<ResultMetric> chosen = this.metrics.isEmpty()
            ? EnumSet.of(ResultMetric.HIT_RATIO, ResultMetric.SAVED_BYTES_RATIO)
            : EnumSet.copyOf(this.metrics);
          final StringBuilder text = new StringBuilder();
          for (final UserStatistics stats : results) {
            text.append(ResultTable.header(stats)).append("\n\n");
            for (final ResultMetric metric : chosen) {
              text.append(ResultTable.render(stats, metric)).append('\n');
            }
          }
          if (this.out == null) {
            stdout.print(text);
            stdout.flush();
          } else {
            Files.writeString(this.out, text);
          }
        }
        case CSV -> {
          final Path target = this.out != null ? this.out : Files.createTempFile("cachesim-", ".csv");
          if (!Output.saveStatToCSV(target.toString(), results)) {
            throw new IOException("Cannot write " + target);
          }
          if (this.out == null) {
            stdout.print(Files.readString(target));
            stdout.flush();
            Files.delete(target);
          }
        }
        case XLS -> {
          if (!Output.saveStatToXLS(this.out.toString(), results)) {
            throw new IOException("Cannot write " + this.out);
          }
        }
      }
      return 0;
    }
  }
}
