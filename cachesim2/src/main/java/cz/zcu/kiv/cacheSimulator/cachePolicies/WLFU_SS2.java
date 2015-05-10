package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Algoritmus stejně jako WLFU_SS prioritizuje velké soubory, jen trochu odlišným způsobem.
 *          K hitům se přičtou vážené hity relativní velikostí souboru.
 */
public class WLFU_SS2 extends LFU_SS {

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
      pair.setFirst(pair.getFirst() + pair.getFirst() * pair.getSecond().getFileSize()
          / cacheMaxSize);
    }

    this.needSort = true;
    super.removeFile();
  }


  @Override
  public String toString() {
    return "WLFU-SS2";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS2;WLFU-SS2";
  }
}
