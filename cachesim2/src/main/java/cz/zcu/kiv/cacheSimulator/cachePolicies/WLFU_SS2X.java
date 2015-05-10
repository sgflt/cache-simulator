package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Algoritmus stejně jako WLFU_SS prioritizuje velké soubory, jen trochu odlišným způsobem.
 *          K hitům se přičtou vážené hity relativní velikostí souboru.
 */
public class WLFU_SS2X extends WLFU_SS2 {

  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
   */
  @Override
  public void insertFile(final FileOnClient f) {

    if (f.getFileSize() > this.getCacheCapacity() / 2)
      return;

    super.insertFile(f);
  }


  @Override
  public String toString() {
    return "WLFU-SS2X";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS2X;WLFU-SS2X";
  }
}
