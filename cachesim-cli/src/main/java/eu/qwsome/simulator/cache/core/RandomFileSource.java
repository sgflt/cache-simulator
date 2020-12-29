package eu.qwsome.simulator.cache.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author Lukáš Kvídera
 */
class RandomFileSource implements FileSource {
  private static final Logger LOG = LoggerFactory.getLogger(RandomFileSource.class);

  private final int countOfFiles;
  private int generated;

  RandomFileSource(final int countOfFiles) {
    this.countOfFiles = countOfFiles;
  }

  @Override
  public FileTraffic get() {
    LOG.trace("get()");

    final var random = new Random();
    return ++this.generated <= this.countOfFiles
      ? new FileTraffic(String.valueOf(random.nextInt(100)), random.nextInt())
      : null;
  }
}
