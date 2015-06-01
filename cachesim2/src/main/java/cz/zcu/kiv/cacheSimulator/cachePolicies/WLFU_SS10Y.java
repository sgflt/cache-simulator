package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Tento algoritmus je upně stejnž, jako WLFU_SS5, ale používá magickou jednotku.
 *          Tedy ve výsledku se neodečítá váha hitů, ale přičítá se 1 - váha souboru.
 *          Velkým souborům se hodnota hitů nepatrně zvětší, malé soubory mohou mít výsledné hity až
 *          o jedna větší. *
 */
public class WLFU_SS10Y extends LFU_SS {

  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
   */
  @Override
  public void removeFile() {

    double cacheMaxSize = 0.0;
    final ArrayList<Pair<Double, FileOnClient>> priorityQueue = new ArrayList<>(
        this.list.size());

    for (final Pair<Double, FileOnClient> pair : this.list) {
      if (pair.getSecond().getFileSize() > cacheMaxSize)
        cacheMaxSize = pair.getSecond().getFileSize();
    }

    for (Pair<Double, FileOnClient> pair : this.list) {
      final double penalty = pair.getSecond().getFileSize() / cacheMaxSize;
      pair = new Pair<>(pair.getFirst() + 1 - penalty, pair.getSecond());
      priorityQueue.add(pair);
    }

    this.needSort = false;
    Collections.sort(priorityQueue, comparator);

    if (this.list.size() > 0)
      this.list.remove(priorityQueue.remove(0));

    if (this.list.size() > 2)
      if ((this.list.get(this.list.size() - 1)).getFirst() > 15) {
        for (final Pair<Double, FileOnClient> f : this.list) {
          f.setFirst(f.getFirst() / 2);
        }
      }

  }


  @Override
  public String toString() {
    return "WLFU-SS10Y";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS10Y;WLFU-SS10Y";
  }
}
