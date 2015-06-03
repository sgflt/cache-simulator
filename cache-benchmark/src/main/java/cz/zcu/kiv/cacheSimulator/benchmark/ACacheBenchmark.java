package cz.zcu.kiv.cacheSimulator.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * ACacheBenchmark.java
 *    2. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
@State(Scope.Benchmark)
public abstract class ACacheBenchmark implements ICacheBenchmark {
  protected final static int REMOVE_CACHE_SIZE = 10000000;
  protected Class<?> clazz;

  @Override
  @Benchmark
  @Warmup(iterations = 4)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public ICache contains(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(this.clazz);
    cache.contains(cacheProvider.getNextFile().getFileName());
    return cache;
  }

  @Override
  @Benchmark
  @Warmup(iterations = 4)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public ICache insert(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(this.clazz);
    cache.insertFile(new FileOnClient(cacheProvider.getNextFile(), cache, 0));
    return cache;
  }

  @Override
  @Benchmark
  @Fork(value = 2)
  @Warmup(iterations = 3)
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @Measurement(batchSize = 1000, iterations = 10)
  public ICache remove(final DataProvider cacheProvider) {
    final ICache cache = cacheProvider.getCache(this.clazz, REMOVE_CACHE_SIZE);
    cache.removeFile();
    return cache;
  }
}
