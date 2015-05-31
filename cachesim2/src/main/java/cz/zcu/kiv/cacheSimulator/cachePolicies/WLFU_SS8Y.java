package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.PriorityQueue;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *          Tento algoritmus je úplně stejný, jako WLFU_SS5, ale používá magickou jednotku.
 *          Tedy ve výsledku se neodečítá váha hitů, ale přičítá se 1 - váha hitů.
 *          Velkým souborům se hodnota hitů nepatrně zvětší, malé soubory mohou mít výsledné hity až
 *          nekonečné.
 *          Určitě by to chtělo pořešit inverzi priorit, kdy malý soubor v cache zůstane na věky.
 */
public class WLFU_SS8Y extends LFU_SS {

  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
   */
  @Override
  public void removeFile() {

    double cacheMaxSize = 0.0;
    final PriorityQueue<Pair<Double, FileOnClient>> priorityQueue = new PriorityQueue<>(
        this.list.size(), new PairCompare());

    for (final Pair<Double, FileOnClient> pair : this.list) {
      if (pair.getSecond().getFileSize() > cacheMaxSize)
        cacheMaxSize = pair.getSecond().getFileSize();
    }

    for (Pair<Double, FileOnClient> pair : this.list) {
      final double penalty = pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize;
      pair = new Pair<>(pair.getFirst() + 1 - penalty, pair.getSecond());
      priorityQueue.add(pair);
    }

    this.needSort = false;
    if (this.list.size() > 0)
      this.list.remove(priorityQueue.remove());

    if (this.list.size() > 2)
      if ((this.list.get(this.list.size() - 1)).getFirst() > 15) {
        for (final Pair<Double, FileOnClient> f : this.list) {
          f.setFirst(f.getFirst() / 2);
        }
      }
  }


  @Override
  public String toString() {
    return "WLFU-SS8Y";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS8Y;WLFU-SS8Y";
  }
}
