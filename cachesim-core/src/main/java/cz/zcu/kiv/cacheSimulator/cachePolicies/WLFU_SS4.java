package cz.zcu.kiv.cacheSimulator.cachePolicies;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *
 * WLFU_SS4 se snaží ponechávat v cache malé soubory.
 *
 * Od hitů odeště váhu souboru.
 */
public class WLFU_SS4 extends LFU_SS {

  @Override
  public void removeFile() {
    double cacheMaxSize = 0.0;
    for (final MetaData entry : entries()) {
      cacheMaxSize = Math.max(cacheMaxSize, entry.getFileOnClient().getFileSize());
    }
    for (final MetaData entry : entries()) {
      final double h = entry.getReadHits();
      final double r = entry.getFileOnClient().getFileSize() / cacheMaxSize;
      entry.setReadHits(h - r);
    }
    markUnsorted();
    super.removeFile();
  }

  @Override
  public String toString() {
    return "WLFU-SS4";
  }

  @Override
  public String cacheInfo() {
    return "WLFU_SS4;WLFU-SS4";
  }
}
