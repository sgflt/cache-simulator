package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.List;

/**
 * Runs one whole simulation the same way for every front end (GUI, CLI, TUI).
 * Settings not passed in are read from {@link GlobalVariables}.
 */
public final class SimulationRunner {

  private SimulationRunner() {
  }

  /**
   * Runs the simulation in the calling thread. Interrupt the thread to cancel it.
   *
   * @return results per user, or an empty list when cancelled
   */
  public static List<UserStatistics> run(
    final RequestSource source,
    final SimulationConfig config,
    final SimulationProgressListener progressListener
  ) {
    GlobalVariables.setLoadDataFromLog(source == RequestSource.AFS_LOG);
    GlobalMethods.setGenerators();
    final var simulation = new AccessSimulation(source.createQueue(), config);
    simulation.setProgressListener(progressListener);
    Server.getInstance().hardReset();
    if (source == RequestSource.AFS_LOG && !GlobalVariables.isRandomFileSizesForLoggedData()) {
      simulation.simulateFromLogFile();
    } else {
      simulation.simulateRandomFileSizes();
    }
    return Thread.currentThread().isInterrupted() ? List.of() : simulation.getResults();
  }
}
