package eu.qwsome.simulator.cache.policy.fbr;

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
class FBRFactory implements CacheFactory {

  @Value("${cache.policy.fbr.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.fbr.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.fbr.capacity.step}")
  private long step;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new FBR(capacity));
    }

    return caches;
  }
}
