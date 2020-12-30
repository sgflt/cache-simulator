package eu.qwsome.simulator.cache.policy.lru;

import eu.qwsome.simulator.cache.core.CacheFactory;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukáš Kvídera
 */
@Component
class LRUFactory implements CacheFactory {

  @Value("${cache.policy.lru.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.lru.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.lru.capacity.step}")
  private long step;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new LRU(capacity));
    }

    return caches;
  }
}
