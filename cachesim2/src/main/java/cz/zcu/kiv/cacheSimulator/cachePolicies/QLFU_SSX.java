package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class QLFU_SSX extends QLFU_SS {

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
    return "QLFU-SSX";
  }

  @Override
  public String cacheInfo() {
    return "QLFU_SSX;QLFU-SSX";
  }
}
