package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Algoritmus vychází WLFU_SS2, který zvýhodňuje velké soubory.
 *          Tento je také zvýhodňuje, ale ne tolik.
 */
public class WLFU_SS7 extends LFU_SS {

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
      pair.setFirst(pair.getFirst() + 0.23 * pair.getSecond().getFileSize() / cacheMaxSize);
    }

    this.needSort = true;
    super.removeFile();
  }


  @Override
  public String toString() {
    return "WLFU-SS7";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS7;WLFU-SS7";
  }
}
