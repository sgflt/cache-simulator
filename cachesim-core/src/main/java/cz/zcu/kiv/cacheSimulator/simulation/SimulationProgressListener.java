package cz.zcu.kiv.cacheSimulator.simulation;

/**
 * Receives simulation progress, in percent (0-100).
 * Called from the simulation thread, so GUI code must hand the value over to its UI thread.
 */
@FunctionalInterface
public interface SimulationProgressListener {

  SimulationProgressListener NONE = percent -> { };

  void onProgress(int percent);
}
