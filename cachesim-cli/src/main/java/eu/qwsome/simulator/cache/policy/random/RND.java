package eu.qwsome.simulator.cache.policy.random;

import eu.qwsome.simulator.cache.core.FileTraffic;
import eu.qwsome.simulator.cache.core.SimulationCacheStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Lukáš Kvídera
 */
class RND extends SimulationCacheStub {

  private final List<FileTraffic> content = new ArrayList<>();

  private final long capacity;
  /**
   * pro nahodne generovane indexy vyhazovanych souboru
   */
  private final Random rnd;
  private long used;

  /**
   * konstruktor - inicializace promennych
   */
  RND(final long capacity) {
    this.capacity = capacity;
    this.rnd = new Random(0);
  }

  @Override
  public FileTraffic get(final String fileName) {
    for (final var file : this.content) {
      if (file.getFileName().equalsIgnoreCase(fileName)) {
        return file;
      }
    }

    return null;
  }

  @Override
  public void put(final String key, final FileTraffic value) {
    if (value.getFileSize() > this.capacity) {
      return;
    }


    //pokud se soubor vejde, fungujeme spravne
    while (value.getFileSize() + this.used > this.capacity) {
      removeFile();
    }

    this.used += value.getFileSize();
    this.content.add(value);
  }

  private void removeFile() {
    final var removedFile = this.content.remove(this.rnd.nextInt(this.content.size()));
    this.used -= removedFile.getFileSize();
  }

  @Override
  public long getCapacity() {
    return this.capacity;
  }
}
