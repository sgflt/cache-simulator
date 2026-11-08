package cz.zcu.kiv.cacheSimulator.simulation;

import java.util.function.BiFunction;

/**
 * A result value shown in the results table and charts.
 */
public enum ResultMetric {

  HIT_RATIO("Read Hit Ratio [%]", UserStatistics::getCacheHitRatios),
  HIT_COUNT("Read Hit Count", UserStatistics::getCacheHits),
  SAVED_BYTES_RATIO("Saved Bytes Ratio [%]", UserStatistics::getCacheSavedBytesRatio),
  SAVED_BYTES("Saved Bytes [MB]", UserStatistics::getSavedBytes),
  TRANSFER_DECREASE_RATIO("Data Transfer Decrease Ratio [%]", UserStatistics::getDataTransferDegreaseRatio),
  TRANSFER_DECREASE("Data Transfer Decrease [MB]", UserStatistics::getDataTransferDegrease);

  private final String label;

  private final BiFunction<UserStatistics, String, Number[]> values;

  ResultMetric(final String label, final BiFunction<UserStatistics, String, Number[]> values) {
    this.label = label;
    this.values = values;
  }

  /**
   * @return one value per cache size, in the order of {@link UserStatistics#getCacheSizes()}
   */
  public Number[] values(final UserStatistics stats, final String cacheName) {
    return this.values.apply(stats, cacheName);
  }

  public String label() {
    return this.label;
  }

  @Override
  public String toString() {
    return this.label;
  }
}
