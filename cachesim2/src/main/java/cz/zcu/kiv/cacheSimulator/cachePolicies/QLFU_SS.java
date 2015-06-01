package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.Collections;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class QLFU_SS extends LFU_SS {

  @Override
  public void insertFile(final FileOnClient f) {

    if (f.getFileSize() > this.getCapacity())
      return;

    // pokud se soubor vejde, fungujeme spravne
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    final double readHits = 1;
    this.list.add(new Pair<>(readHits, f));

    this.needSort = true;
  }


  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
   */
  @Override
  public void removeFile() {

    double localReadCount = 0.0;

    for (final Pair<Double, FileOnClient> files : this.list) {
      localReadCount += files.getFirst();
    }

    for (final Pair<Double, FileOnClient> pair : this.list) {
      final double penalty = ((double) pair.getSecond().getReadHit() - (double) pair.getSecond()
          .getWriteHit()) / this.globalReadCount * localReadCount;

      pair.setFirst(pair.getFirst() + penalty);
    }

    if (this.needSort) {
      Collections.sort(this.list, comparator);
    }

    this.needSort = false;

    if (this.list.size() > 0)
      this.list.remove(0);

    if (this.list.size() > 2) {
      if ((this.list.get(this.list.size() - 1)).getFirst() > 15) {
        for (final Pair<Double, FileOnClient> f : this.list) {
          f.setFirst(f.getFirst() / 2);
        }
      }
    }
  }


  @Override
  public String toString() {
    return "QLFU-SS";
  }


  @Override
  public String cacheInfo() {
    return "QLFU_SS;QLFU-SS";
  }
}
