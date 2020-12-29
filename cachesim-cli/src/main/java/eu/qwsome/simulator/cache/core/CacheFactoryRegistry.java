package eu.qwsome.simulator.cache.core;

import org.springframework.stereotype.Component;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukáš Kvídera
 */
@Component
class CacheFactoryRegistry {

  private final List<Cache<String, FileTraffic>> caches;

  CacheFactoryRegistry(final List<CacheFactory> cacheFactories) {
    this.caches = cacheFactories.stream()
      .map(CacheFactory::createCache)
      .map(StatisticsCalculatingCacheDecorator::new)
      .collect(Collectors.toUnmodifiableList());
  }

  List<Cache<String, FileTraffic>> getCaches() {
    return this.caches;
  }
}
