package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class QLFU_SS extends LFU_SS {

  @Override
  public void insertFile(final FileOnClient fileOnClient) {
    if (fileOnClient.getFileSize() > getCacheCapacity()) {
      return;
    }
    //pokud se soubor vejde, fungujeme spravne
    while (freeCapacity() < fileOnClient.getFileSize()) {
      removeFile();
    }
    addEntry(fileOnClient, 1.0);
  }

  @Override
  public void removeFile() {
    double localReadCount = 0.0;
    for (final MetaData entry : entries()) {
      localReadCount += entry.getReadHits();
    }
    for (final MetaData entry : entries()) {
      final FileOnClient file = entry.getFileOnClient();
      final double penalty = ((double) file.getCountOfReadRequests() - (double) file.getCountOfWriteRequests())
        / (double) getGlobalReadCount() * localReadCount;
      entry.setReadHits(entry.getReadHits() + penalty);
    }
    super.removeFile();
  }

  @Override
  public String toString() {
    return "QLFU-SS";
  }

  @Override
  public String cacheInfo() {
    return "QLFU_SS;QLFU-SS";
  }
}
