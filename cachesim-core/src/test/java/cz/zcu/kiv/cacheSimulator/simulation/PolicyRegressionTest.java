package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.dataAccess.AbstractFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.CacheLoader;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Runs every cache policy on fixed request streams and compares the results with
 * the recorded ones in {@code policy-regression.txt}. Policy optimizations must not
 * change a single number.
 * <p>
 * To re-record after an intended behaviour change, run with
 * {@code -Dpolicy.regression.record=<path to the resource file>}.
 */
class PolicyRegressionTest {

  private static final String RESOURCE = "/policy-regression.txt";
  private static final List<Integer> SIZES_MB = List.of(1, 8, 64);

  /**
   * LRFU-SS ranks files by System.nanoTime(), so its numbers change from run to run.
   * It still runs here (it must not crash), but its results are not compared.
   */
  private static final String NOT_REPRODUCIBLE = " LRFU-SS ";

  @BeforeEach
  void reset() {
    GlobalMethods.setGenerators();
    Server.getInstance().hardReset();
    GlobalVariables.setActualTime(0);
  }

  @AfterEach
  void cleanUp() {
    Server.getInstance().hardReset();
    GlobalVariables.setActualTime(0);
  }

  @Test
  void everyPolicyGivesTheRecordedResults() throws IOException {
    final List<String> policies = CacheLoader.loadAvailableCaches().stream()
      .map(info -> info.split(";")[0])
      .toList();
    final var config = new SimulationConfig(policies, SIZES_MB);

    final List<String> actual = new ArrayList<>();
    // big files, few of them fit: exercises eviction and the over-capacity window
    actual.addAll(runLog("zipf-big-files", config,
      generate(new ZipfFileNameGenerator(20_000), 1, 1024, 5 * 1024 * 1024)));
    // several users with their own caches
    actual.addAll(runLog("gauss-3-users", config,
      generate(new GaussianFileNameGenerator(20_000), 3, 1024, 2 * 1024 * 1024)));
    // small files: caches hold hundreds or thousands of entries
    actual.addAll(runLog("zipf-small-files", config,
      generate(new ZipfFileNameGenerator(20_000), 1, 1024, 64 * 1024)));
    // generator mode: one pass per measurement, server statistics reset between passes
    actual.addAll(runRandomSizes("zipf-generator-mode", config,
      generate(new ZipfFileNameGenerator(10_000), 1, 1024, 512 * 1024)));

    actual.removeIf(line -> line.contains(NOT_REPRODUCIBLE));

    final String record = System.getProperty("policy.regression.record");
    if (record != null) {
      Files.write(Path.of(record), actual, StandardCharsets.UTF_8);
      return;
    }

    final List<String> expected;
    try (InputStream in = PolicyRegressionTest.class.getResourceAsStream(RESOURCE)) {
      assertNotNull(in, RESOURCE + " is missing");
      expected = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
    }
    assertEquals(String.join("\n", expected), String.join("\n", actual));
  }

  /**
   * turns a generator's file names into a log with fixed per-file sizes and growing access times
   */
  private static List<Quartet<String, Long, Long, Long>> generate(
    final IFileQueue generator, final int users, final long minSize, final long maxSize) {
    final Random rnd = new Random(42);
    final Map<String, Long> sizes = new HashMap<>();
    final List<Quartet<String, Long, Long, Long>> requests = new ArrayList<>();
    Triplet<String, Long, Long> next;
    long time = 0;
    while ((next = generator.getNextFileName()) != null) {
      final String name = next.getFirst();
      final long size = sizes.computeIfAbsent(name, n -> minSize + (long) (rnd.nextDouble() * (maxSize - minSize)));
      time += 1 + rnd.nextInt(200);
      requests.add(new Quartet<>(name, size, time, (long) rnd.nextInt(users)));
    }
    return requests;
  }

  private static List<String> runLog(final String scenario, final SimulationConfig config,
                                     final List<Quartet<String, Long, Long, Long>> requests) {
    Server.getInstance().hardReset();
    final var simulation = new AccessSimulation(new ListQueue(requests), config);
    simulation.simulateFromLogFile();
    return describe(scenario, simulation);
  }

  private static List<String> runRandomSizes(final String scenario, final SimulationConfig config,
                                             final List<Quartet<String, Long, Long, Long>> requests) {
    // files exist up front, so the server's unseeded size generator is never used
    final Server server = Server.getInstance();
    server.hardReset();
    for (final var request : requests) {
      if (!server.existFileOnServer(request.getFirst())) {
        server.insertNewFile(request.getFirst(), request.getSecond());
      }
    }
    final var simulation = new AccessSimulation(new ListQueue(requests), config);
    simulation.simulateRandomFileSizes();
    return describe(scenario, simulation);
  }

  private static List<String> describe(final String scenario, final AccessSimulation simulation) {
    final List<String> lines = new ArrayList<>();
    final List<UserStatistics> users = new ArrayList<>(simulation.getResults());
    users.sort(Comparator.comparingLong(UserStatistics::getUserID));
    for (final UserStatistics user : users) {
      lines.add(scenario + " user=" + user.getUserID() + " requests=" + user.getFileAccessed()
        + " bytes=" + user.getTotalNetworkBandwidth());
      for (final String cache : user.getCacheNames().stream().sorted().toList()) {
        lines.add(scenario + " user=" + user.getUserID() + " " + cache
          + " hits=" + List.of(user.getCacheHits(cache))
          + " hitRatio=" + List.of(user.getCacheHitRatios(cache))
          + " savedRatio=" + List.of(user.getCacheSavedBytesRatio(cache)));
      }
    }
    return lines;
  }

  /**
   * request source backed by a list; also drives the global clock like the AFS log reader
   */
  private static final class ListQueue extends AbstractFileQueue {

    private final List<Quartet<String, Long, Long, Long>> requests;
    private Iterator<Quartet<String, Long, Long, Long>> it;

    ListQueue(final List<Quartet<String, Long, Long, Long>> requests) {
      this.requests = requests;
      this.it = requests.iterator();
    }

    @Override
    public Triplet<String, Long, Long> getNextFileName() {
      final var q = getNextFileNameWithFSize();
      return q == null ? null : new Triplet<>(q.getFirst(), q.getThird(), q.getFourth());
    }

    @Override
    public Quartet<String, Long, Long, Long> getNextFileNameWithFSize() {
      if (!this.it.hasNext()) {
        return null;
      }
      final var next = this.it.next();
      GlobalVariables.setActualTime(next.getThird());
      return next;
    }

    @Override
    public void reset() {
      this.it = this.requests.iterator();
      GlobalVariables.setActualTime(0);
    }
  }
}
