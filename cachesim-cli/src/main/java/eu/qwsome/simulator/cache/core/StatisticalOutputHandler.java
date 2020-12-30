package eu.qwsome.simulator.cache.core;

import eu.qwsome.simulator.cache.core.event.SimulationCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import java.util.List;

/**
 * @author Lukáš Kvídera
 */
@Component
class StatisticalOutputHandler {
  private static final Logger LOG = LoggerFactory.getLogger(StatisticalOutputHandler.class);

  private final List<Cache<String, FileTraffic>> caches;

  StatisticalOutputHandler(final CacheFactoryRegistry registry) {
    this.caches = registry.getCaches();
  }

  @EventListener
  public void onSimulationCompleted(final SimulationCompleted event) {
    LOG.info("Building statictics...");

    this.caches.forEach(cache -> {
      LOG.info("{}", cache);
      final var decorator = (StatisticsCalculatingCacheDecorator) cache;

      LOG.info(
        "cachename={}, cachesize={}, hitratio={}, savedtraffic={}",
        decorator.getName(),
        decorator.getCapacity(),
        decorator.getHitRatio(),
        decorator.getSavedTraffic()
      );
    });
  }
}
