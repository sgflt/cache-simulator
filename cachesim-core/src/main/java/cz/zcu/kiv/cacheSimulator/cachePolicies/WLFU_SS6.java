package cz.zcu.kiv.cacheSimulator.cachePolicies;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class WLFU_SS6 extends LFU_SS {

  @Override
  public void removeFile() {
    double cacheMaxSize = 0.0;
    for (final MetaData entry : entries()) {
      cacheMaxSize = Math.max(cacheMaxSize, entry.getFileOnClient().getFileSize());
    }
    for (final MetaData entry : entries()) {
      final double h = entry.getReadHits();
      final double r = entry.getFileOnClient().getFileSize() / cacheMaxSize;
      entry.setReadHits(h - h * r);
    }
    markUnsorted();
    super.removeFile();
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
