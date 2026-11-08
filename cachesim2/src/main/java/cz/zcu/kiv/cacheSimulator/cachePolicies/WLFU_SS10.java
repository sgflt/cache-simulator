package cz.zcu.kiv.cacheSimulator.cachePolicies;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 *
 * Tento algoritmus je upně stejnž, jako WLFU_SS5, ale používá magickou jednotku.
 *
 * Tedy ve výsledku se neodečítá váha hitů, ale přičítá se 1 - váha souboru.
 *
 * Velkým souborům se hodnota hitů nepatrně zvětší, malé soubory mohou mít výsledné hity až o jedna větší.
 */
public class WLFU_SS10 extends LFU_SS {

  @Override
  public void removeFile() {
    double cacheMaxSize = 0.0;
    for (final MetaData entry : entries()) {
      cacheMaxSize = Math.max(cacheMaxSize, entry.getFileOnClient().getFileSize());
    }
    for (final MetaData entry : entries()) {
      final double h = entry.getReadHits();
      final double r = entry.getFileOnClient().getFileSize() / cacheMaxSize;
      entry.setReadHits(h + 1 - r);
    }
    markUnsorted();
    super.removeFile();
  }

  @Override
  public String toString() {
    return "WLFU-SS10";
  }

  @Override
  public String cacheInfo() {
    return "WLFU_SS10;WLFU-SS10";
  }
}
