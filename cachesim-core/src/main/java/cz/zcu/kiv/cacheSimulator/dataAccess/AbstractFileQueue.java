package cz.zcu.kiv.cacheSimulator.dataAccess;

import cz.zcu.kiv.cacheSimulator.simulation.SimulationProgressListener;

/**
 * Common base for request sources: holds the progress listener.
 */
public abstract class AbstractFileQueue implements IFileQueue {

  private SimulationProgressListener progressListener = SimulationProgressListener.NONE;

  @Override
  public void setProgressListener(final SimulationProgressListener listener) {
    this.progressListener = listener == null ? SimulationProgressListener.NONE : listener;
  }

  protected void fireProgress(final int percent) {
    this.progressListener.onProgress(percent);
  }
}
