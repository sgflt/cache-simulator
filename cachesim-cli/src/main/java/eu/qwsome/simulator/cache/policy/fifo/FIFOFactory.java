package eu.qwsome.simulator.cache.policy.fifo;

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
public class FIFOFactory implements CacheFactory {

  @Value("${cache.policy.fifo.capacity.min}")
  private long minCapacity;

  @Value("${cache.policy.fifo.capacity.max}")
  private long maxCapacity;

  @Value("${cache.policy.fifo.capacity.step}")
  private long step;


  @Override
  public List<SimulationCacheStub> createCaches() {
    final List<SimulationCacheStub> caches = new ArrayList<>();

    for (long capacity = this.minCapacity; capacity <= this.maxCapacity; capacity += this.step) {
      caches.add(new FIFO(capacity));
    }

    return caches;
  }
}
