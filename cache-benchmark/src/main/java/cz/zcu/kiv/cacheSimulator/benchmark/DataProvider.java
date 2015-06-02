package cz.zcu.kiv.cacheSimulator.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * DataProvider.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
@State(Scope.Thread)
public class DataProvider implements IDataProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DataProvider.class);
  private static final Random rnd = new Random();

  private static final int COUNT_OF_FILES = 8 * 1024;
  private static final int MAX_FILENAME = 1024;
  private static final int MAX_SIZE = 8 * 1024;

  private static final int DEFAULT_CAPACITY = 8 * 1024 * 1024;


  private final List<FileOnServer> files = new ArrayList<>(COUNT_OF_FILES);

  private ICache cache;

  private int index = 0;

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.benchmark.IDataProvider#getCache()
   */
  @Override
  public ICache getCache(final Class<?> clazz, final int size) {
    if (this.cache == null || this.cache.freeCapacity() == this.cache.getCapacity()) {
      try {
        this.cache = (ICache) clazz.newInstance();
        this.cache.setCapacity(size);

        for (int i = 0; i < COUNT_OF_FILES; ++i) {
          final FileOnServer rf = new FileOnServer(
              Integer.toString(rnd.nextInt(MAX_FILENAME)),
              rnd.nextInt(MAX_SIZE)
              );

          this.files.add(rf);
          this.cache.insertFile(new FileOnClient(rf, this.cache, 0));
        }
      } catch (InstantiationException | IllegalAccessException e) {
        this.cache = null;
      }
    }

    return this.cache;
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.benchmark.IDataProvider#getCache(java.lang.Class)
   */
  @Override
  public ICache getCache(final Class<?> clazz) {
    return this.getCache(clazz, DEFAULT_CAPACITY);
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.benchmark.IDataProvider#getNextFile()
   */
  @Override
  public FileOnServer getNextFile() {
    this.index = this.index++ % this.files.size();
    return this.files.get(this.index);
  }
}
