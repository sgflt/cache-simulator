package eu.qwsome.simulator.cache.core;

import javax.cache.Cache;

/**
 * @author Lukáš Kvídera
 */
public interface CacheFactory {

  Cache<String, FileTraffic> createCache();
}
