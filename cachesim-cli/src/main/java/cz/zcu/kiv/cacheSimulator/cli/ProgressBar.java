package cz.zcu.kiv.cacheSimulator.cli;

import cz.zcu.kiv.cacheSimulator.simulation.SimulationProgressListener;

import java.io.PrintStream;

/**
 * One-line progress bar redrawn in place on a terminal.
 */
final class ProgressBar implements SimulationProgressListener {

  private static final int WIDTH = 40;

  private final PrintStream out;
  private int last = -1;

  ProgressBar(final PrintStream out) {
    this.out = out;
  }

  @Override
  public void onProgress(final int percent) {
    final int value = Math.clamp(percent, 0, 100);
    if (value == this.last) {
      return;
    }
    this.last = value;
    final int filled = value * WIDTH / 100;
    this.out.print("\r[" + "#".repeat(filled) + " ".repeat(WIDTH - filled) + "] " + value + " %");
    this.out.flush();
  }

  void finish() {
    this.out.print("\r" + " ".repeat(WIDTH + 8) + "\r");
    this.out.flush();
  }
}
