package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Tento algoritmus je upně stejnž, jako WLFU_SS5, ale používá magickou jednotku.
 *          Tedy ve výsledku se neodečítá váha hitů, ale přičítá se 1 - váha hitů.
 *          Velkým souborům se hodnota hitů nepatrně zvětší, malé soubory mohou mít výsledné hity až
 *          nekonečné.
 *          Určitě by to chtělo pořečit inverzi priorit, kdy malý soubor v cache zůstane na věky.
 */
public class WLFU_SS8 extends LFU_SS {

  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
   */
  @Override
  public void removeFile() {

    double cacheMaxSize = 0.0;

    for (final Pair<Double, FileOnClient> pair : this.list) {
      if (pair.getSecond().getFileSize() > cacheMaxSize)
        cacheMaxSize = pair.getSecond().getFileSize();
    }

    for (final Pair<Double, FileOnClient> pair : this.list) {
      final double penalty = pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize;
      pair.setFirst(pair.getFirst() + 1 - penalty);
    }

    this.needSort = true;
    super.removeFile();
  }


  @Override
  public String toString() {
    return "WLFU-SS8";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS8;WLFU-SS8";
  }
}
