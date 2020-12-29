package eu.qwsome.simulator.cache.policy.fifo;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * @author Lukáš Kvídera
 */
public class FIFO extends SimulationCacheStub {

  private final Queue<FileTraffic> queue = new LinkedList<>();

  /**
   * Files that does not fit cache
   */
  private final List<FileTraffic> overCapacity = new ArrayList<>();


  /**
   * velikost cache v B
   */
  private final long capacity;

  FIFO(final long capacity) {
    this.capacity = capacity;
  }

  @Override
  public FileTraffic get(final String key) {
    for (final var file : this.queue) {
      if (file.getFileName().equalsIgnoreCase(key)) {
        return file;
      }
    }

    return null;
  }


  @Override
  public void put(final String key, final FileTraffic value) {
    // TODO
    this.queue.add(value);
  }

  @Override
  public boolean remove(final String key) {
    return false;
  }
}
