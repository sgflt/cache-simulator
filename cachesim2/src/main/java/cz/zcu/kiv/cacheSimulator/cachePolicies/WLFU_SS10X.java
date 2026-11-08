package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class WLFU_SS10X extends WLFU_SS10 {

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
    return "WLFU-SS10X";
  }

  @Override
  public String cacheInfo() {
    return "WLFU_SS10X;WLFU-SS10X";
  }
}
