package cz.zcu.kiv.cacheSimulator.simulation;

import java.util.List;

/**
 * What to simulate: which cache policies and which cache sizes.
 *
 * @param cacheNames simple class names of policies in {@code cz.zcu.kiv.cacheSimulator.cachePolicies}
 * @param cacheSizes cache capacities in MB
 */
public record SimulationConfig(List<String> cacheNames, List<Integer> cacheSizes) {

  public SimulationConfig {
    cacheNames = List.copyOf(cacheNames);
    cacheSizes = List.copyOf(cacheSizes);
  }
}
