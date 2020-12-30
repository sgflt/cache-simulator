package eu.qwsome.simulator.cache.policy.random;

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
class RNDFactory implements CacheFactory {

  @Value("${cache.policy.rnd.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.rnd.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.rnd.capacity.step}")
  private long step;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new RND(capacity));
    }

    return caches;
  }
}
