package eu.qwsome.simulator.cache.core;

import eu.qwsome.simulator.cache.core.event.SimulationCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author Lukáš Kvídera
 */
@Component
public
class SimulationEngine implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(SimulationEngine.class);

  private final FileSource fileSource;
  private final ApplicationEventPublisher eventPublisher;
  private final CacheFactoryRegistry cacheFactoryRegistry;

  SimulationEngine(
    final FileSource fileSource,
    final ApplicationEventPublisher eventPublisher,
    final CacheFactoryRegistry cacheFactoryRegistry
  ) {
    this.fileSource = fileSource;
    this.eventPublisher = eventPublisher;
    this.cacheFactoryRegistry = cacheFactoryRegistry;
  }

  @Override
  public void run() {
    LOG.info("Starting simulation...");

    final var caches = this.cacheFactoryRegistry.getCaches();
    for (FileTraffic file = this.fileSource.get(); file != null; file = this.fileSource.get()) {
      final var downloadedFile = file;
      caches.forEach(cache -> {
        final var fileTraffic = cache.get(downloadedFile.getFileName());
        if (fileTraffic == null) {
          cache.put(downloadedFile.getFileName(), downloadedFile);
        }
      });
    }

    this.eventPublisher.publishEvent(new SimulationCompleted());
    LOG.info("Simulation finished");
  }
}
