package cz.zcu.kiv.cacheSimulator.cachePolicies;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *
 * Algoritmus mění váhu hitů podle jeho velikosti.
 *
 * Vetší soubory mají větší váhu a spíše se z cache vyhodí malý.
 */
public class WLFU_SS extends LFU_SS {

  @Override
  public void removeFile() {
    double cacheMaxSize = 0.0;
    for (final MetaData entry : entries()) {
      cacheMaxSize = Math.max(cacheMaxSize, entry.getFileOnClient().getFileSize());
    }
    for (final MetaData entry : entries()) {
      final double h = entry.getReadHits();
      final double r = entry.getFileOnClient().getFileSize() / cacheMaxSize;
      entry.setReadHits(h * r);
    }
    markUnsorted();
    super.removeFile();
  }

  @Override
  public String toString() {
    return "WLFU-SS";
  }

  @Override
  public String cacheInfo() {
    return "WLFU_SS;WLFU-SS";
  }
}
