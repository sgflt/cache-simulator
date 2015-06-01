package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class QLFU_SSX extends QLFU_SS {

  @Override
  public void insertFile(final FileOnClient f) {

    /* ignore large files */
    if (f.getFileSize() > this.getCapacity() / 2)
      return;

    super.insertFile(f);
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
