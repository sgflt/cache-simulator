package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *
 * Algoritmus stejně jako WLFU_SS prioritizuje velké soubory, jen trochu odlišným způsobem.
 *
 * K hitům se přičtou vážené hity relativní velikostí souboru.
 */
public class WLFU_SS2X extends WLFU_SS2 {

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
    return "WLFU-SS2X";
  }

  @Override
  public String cacheInfo() {
    return "WLFU_SS2X;WLFU-SS2X";
  }
}
