package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *
 * Cache with treshold
 */
public class WLFU_SSX extends WLFU_SS {

  @Override
  public void insertFile(final FileOnClient fileOnClient) {
    /* ignore large files */
    if (fileOnClient.getFileSize() > getCacheCapacity() / 2) {
      return;
    }
    super.insertFile(fileOnClient);
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
