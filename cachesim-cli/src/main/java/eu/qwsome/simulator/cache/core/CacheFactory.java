package eu.qwsome.simulator.cache.core;

import java.util.List;

/**
 * @author Lukáš Kvídera
 */
public interface CacheFactory {

  /**
   * @return caches of the same type but with different parameters that may change caching efficiency
   */
  List<SimulationCacheStub> createCaches();
}
