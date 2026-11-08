package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.dataAccess.AbstractFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs small simulations without any GUI.
 */
class AccessSimulationTest {

  private static final SimulationConfig CONFIG = new SimulationConfig(
    List.of("LRU", "FIFO", "ARC"), List.of(8, 64));

  @BeforeEach
  void resetServer() {
    GlobalMethods.setGenerators();
    Server.getInstance().hardReset();
  }

  @Test
  void zipfSimulationProducesResultsForEveryCacheAndSize() {
    final int requests = 2_000;
    final var simulation = new AccessSimulation(new ZipfFileNameGenerator(requests), CONFIG);

    simulation.simulateRandomFileSizes();

    final List<UserStatistics> results = simulation.getResults();
    assertEquals(1, results.size());
    final UserStatistics user = results.get(0);
    assertEquals(requests, user.getFileAccessed());
    assertArrayEquals(new Integer[]{8, 64}, user.getCacheSizes());
    assertEquals(3, user.getCacheNames().size());
    for (final String cache : user.getCacheNames()) {
      final Double[] ratios = user.getCacheHitRatios(cache);
      assertEquals(2, ratios.length, cache);
      for (final Double ratio : ratios) {
        assertTrue(ratio >= 0 && ratio <= 100, cache + " hit ratio " + ratio);
      }
    }
  }

  @Test
  void progressGrowsOverAllPassesUpTo100Percent() {
    final List<Integer> progress = new ArrayList<>();
    final var simulation = new AccessSimulation(new ZipfFileNameGenerator(1_000), CONFIG);
    simulation.setProgressListener(progress::add);

    simulation.simulateRandomFileSizes();

    assertFalse(progress.isEmpty());
    assertEquals(100, progress.get(progress.size() - 1));
    for (int i = 1; i < progress.size(); i++) {
      assertTrue(progress.get(i) >= progress.get(i - 1), "progress went back: " + progress);
    }
  }

  @Test
  void interruptedSimulationStopsImmediately() {
    // 50M requests would run for minutes if the interrupt were ignored
    final var simulation = new AccessSimulation(new ZipfFileNameGenerator(50_000_000), CONFIG);

    assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
      Thread.currentThread().interrupt();
      simulation.simulateRandomFileSizes();
      assertTrue(Thread.interrupted(), "interrupt flag stays set for the caller");
    });
  }

  @Test
  void logSimulationFeedsEveryRequestToEveryCache() {
    // the same file requested 10 times by one user: every cache gets 1 miss + 9 hits
    final int requests = 10;
    final var queue = new ListQueue(Collections.nCopies(
      requests, new Quartet<>("a", 1024L, 0L, 1L)));
    final var simulation = new AccessSimulation(queue, CONFIG);

    simulation.simulateFromLogFile();

    final UserStatistics user = simulation.getResults().get(0);
    assertEquals(requests, user.getFileAccessed());
    for (final String cache : user.getCacheNames()) {
      assertArrayEquals(new Long[]{9L, 9L}, user.getCacheHits(cache), cache);
    }
  }

  static Stream<Arguments> generators() {
    return Stream.of(
      Arguments.of("Gaussian", (LongFunction<IFileQueue>) GaussianFileNameGenerator::new),
      Arguments.of("Random", (LongFunction<IFileQueue>) RandomFileNameGenerator::new),
      Arguments.of("Random with preferences", (LongFunction<IFileQueue>) RandomFileNameGeneratorWithPrefences::new),
      Arguments.of("Zipf", (LongFunction<IFileQueue>) ZipfFileNameGenerator::new));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("generators")
  void everyGeneratorDeliversAllRequests(final String name, final LongFunction<IFileQueue> generator) {
    final int requests = 1_000;
    final var simulation = new AccessSimulation(generator.apply(requests), CONFIG);

    simulation.simulateRandomFileSizes();

    final UserStatistics user = simulation.getResults().get(0);
    assertEquals(requests, user.getFileAccessed(), name);
    for (final String cache : user.getCacheNames()) {
      for (final Long hits : user.getCacheHits(cache)) {
        // uniform random requests can miss every time in a small cache
        assertTrue(hits >= 0 && hits < requests, name + " " + cache + " hits " + hits);
      }
    }
  }

  @Test
  void afsLogWithFileSizesFromLog(@TempDir final Path dir) throws IOException {
    GlobalVariables.setLogAFSFIleName(writeAfsLog(dir, 10).toString());
    final var simulation = new AccessSimulation(new LogReaderAFS(), CONFIG);

    simulation.simulateFromLogFile();

    assertAllCachesHit9Of10(simulation);
  }

  @Test
  void afsLogWithRandomFileSizesReplaysLogForEveryCache(@TempDir final Path dir) throws IOException {
    GlobalVariables.setLogAFSFIleName(writeAfsLog(dir, 10).toString());
    final var simulation = new AccessSimulation(new LogReaderAFS(), CONFIG);

    simulation.simulateRandomFileSizes();

    assertAllCachesHit9Of10(simulation);
  }

  private static void assertAllCachesHit9Of10(final AccessSimulation simulation) {
    final UserStatistics user = simulation.getResults().get(0);
    assertEquals(10, user.getFileAccessed());
    for (final String cache : user.getCacheNames()) {
      assertArrayEquals(new Long[]{9L, 9L}, user.getCacheHits(cache), cache);
    }
  }

  /**
   * AFS file server log: one user (host 10.0.0.1, id 42) fetching the same 2 KB file
   */
  private static Path writeAfsLog(final Path dir, final int fetches) throws IOException {
    final StringBuilder log = new StringBuilder();
    for (int i = 0; i < fetches; i++) {
      log.append("Sun Nov  8 12:00:00 2026 SRXAFS_FetchData, Fid = 536870913.2.3\n")
        .append("Sun Nov  8 12:00:0").append(i).append(" 2026 FetchData, call, Host 10.0.0.1:7001, Id 42\n")
        .append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle\n")
        .append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle position 0\n")
        .append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle, Pos 0, Len 2048, file size 2048\n")
        .append("Sun Nov  8 12:00:00 2026 SRXAFS_FetchData returns 0\n");
    }
    final Path file = dir.resolve("afs.log");
    Files.writeString(file, log);
    return file;
  }

  /**
   * request source backed by a list, imitating a log file
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
      return this.it.hasNext() ? this.it.next() : null;
    }

    @Override
    public void reset() {
      this.it = this.requests.iterator();
    }
  }
}
