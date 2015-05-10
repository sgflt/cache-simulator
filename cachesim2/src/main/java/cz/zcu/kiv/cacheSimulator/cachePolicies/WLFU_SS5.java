package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          WLFU_SS5 je opakem WLFU_SS2
 *          Ponechává malé soubory v cache. Od hitů se odečtou vážené hity.
 *          Větší soubory dosahují nižších vážených hitů.
 */
public class WLFU_SS5 extends LFU_SS {

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
      pair.setFirst(pair.getFirst()
          - (pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize));
    }

    this.needSort = true;
    super.removeFile();
  }


  @Override
  public String toString() {
    return "WLFU-SS5";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS5;WLFU-SS5";
  }
}
