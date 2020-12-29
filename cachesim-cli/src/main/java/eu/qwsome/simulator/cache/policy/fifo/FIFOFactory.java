package eu.qwsome.simulator.cache.policy.fifo;

import eu.qwsome.simulator.cache.core.CacheFactory;
import eu.qwsome.simulator.cache.core.FileTraffic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

/**
 * @author Lukáš Kvídera
 */
@Component
public class FIFOFactory implements CacheFactory {

  @Value("${cache.policy.fifo.capacity}")
  private long capacity;


  @Override
  public Cache<String, FileTraffic> createCache() {
    return new FIFO(this.capacity);
  }
}
