package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.dataAccess.AbstractFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
  void progressIsReportedUpTo100Percent() {
    final List<Integer> progress = new ArrayList<>();
    final var queue = new ZipfFileNameGenerator(1_000);
    queue.setProgressListener(progress::add);

    new AccessSimulation(queue, CONFIG).simulateRandomFileSizes();

    assertFalse(progress.isEmpty());
    assertEquals(100, progress.get(progress.size() - 1));
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
