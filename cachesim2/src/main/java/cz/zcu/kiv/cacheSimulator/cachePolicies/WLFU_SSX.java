package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          Cache with treshold
 */
public class WLFU_SSX extends WLFU_SS {

  @Override
  public void insertFile(final FileOnClient f) {

    /* ignore large files */
    if (f.getFileSize() > this.getCacheCapacity() / 2)
      return;

    super.insertFile(f);
  }


  @Override
  public String toString() {
    return "WLFU-SSX";
  }


  @Override
  public String cacheInfo() {
    return "WLFU_SSX;WLFU-SSX";
  }
}
