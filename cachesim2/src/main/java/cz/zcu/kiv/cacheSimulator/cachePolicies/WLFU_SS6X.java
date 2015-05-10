package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class WLFU_SS6X extends WLFU_SS6 {

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
    return "WLFU-SS6";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SS6;WLFU-SS6";
  }
}
