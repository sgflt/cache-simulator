package cz.zcu.kiv.cacheSimulator.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.RND;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * CacheBenchmark.java
 * 2. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class RNDBenchmark extends ACacheBenchmark implements ICacheBenchmark {

  @Override
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public ICache contains(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(RND.class);
    cache.contains(cacheProvider.getNextFile().getFileName());
    return cache;
  }


  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.benchmark.ICacheBenchmark#insert(cz.zcu.kiv.cacheSimulator.benchmark.DataProvider)
   */
  @Override
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public ICache insert(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(RND.class);
    cache.insertFile(new FileOnClient(cacheProvider.getNextFile(), cache, 0));
    return cache;
  }


  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.benchmark.ICacheBenchmark#remove(cz.zcu.kiv.cacheSimulator.benchmark.DataProvider)
   */
  @Override
  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Measurement(batchSize = 1000, iterations = 10)
  @Warmup(iterations = 3)
  @Fork(value = 2)
  public ICache remove(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(RND.class, REMOVE_CACHE_SIZE);
    cache.removeFile();
    return cache;
  }

}
