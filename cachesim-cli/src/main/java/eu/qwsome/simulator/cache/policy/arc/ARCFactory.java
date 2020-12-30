package eu.qwsome.simulator.cache.policy.arc;

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
class ARCFactory implements CacheFactory {

  @Value("${cache.policy.arc.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.arc.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.arc.capacity.step}")
  private long step;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new ARC(capacity));
    }

    return caches;
  }
}
