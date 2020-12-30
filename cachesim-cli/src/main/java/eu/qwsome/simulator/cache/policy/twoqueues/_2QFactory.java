package eu.qwsome.simulator.cache.policy.twoqueues;

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
class _2QFactory implements CacheFactory {

  @Value("${cache.policy.2q.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.2q.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.2q.capacity.step}")
  private long step;

  @Value("${cache.policy.2q.fifo-ratio}")
  private double fifoRatio;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new _2Q(capacity, this.fifoRatio));
    }

    return caches;
  }
}
