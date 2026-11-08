package cz.zcu.kiv.cacheSimulator.fx;

import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

/**
 * Where the simulated file requests come from.
 */
enum RequestSource {

  AFS_LOG("From AFS log file"),
  GAUSSIAN("Gaussian random"),
  RANDOM("Random"),
  RANDOM_WITH_PREFERENCE("Random with preference"),
  ZIPF("Zipf random");

  private final String label;

  RequestSource(final String label) {
    this.label = label;
  }

  boolean isGenerator() {
    return this != AFS_LOG;
  }

  /**
   * creates the request queue; generator settings are read from {@link GlobalVariables}
   */
  IFileQueue createQueue() {
    final int requests = GlobalVariables.getRequestCountForRandomGenerator();
    return switch (this) {
      case AFS_LOG -> new LogReaderAFS();
      case GAUSSIAN -> new GaussianFileNameGenerator(requests);
      case RANDOM -> new RandomFileNameGenerator(requests);
      case RANDOM_WITH_PREFERENCE -> new RandomFileNameGeneratorWithPrefences(requests);
      case ZIPF -> new ZipfFileNameGenerator(requests);
    };
  }

  @Override
  public String toString() {
    return this.label;
  }
}
