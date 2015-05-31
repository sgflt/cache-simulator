package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * Trida pro simulaci pristupu bez konzistentnosti
 *
 * @author Pavel Bžoch
 */
public class NoConsistency extends NearStrongConsistency {

  @Override
  public void updateConsistencyWrite(final ICache cache, final long userID,
      final FileOnClient fOnClient, final FileOnServer fOnServer) {
    fOnClient.updateVerAndSize(fOnServer);
  }


  @Override
  public void updateActualReadFile(final ICache cache, final long userID,
      final FileOnClient fOnClient, final FileOnServer fOnServer) {

    // kontrola ruznosti verzi
    if (fOnClient.getVersion() > fOnServer.getVersion()) {
      System.err.println("Verze na klientu je vyssi nez na serveru!");
      return;
    }
    if (fOnClient.getVersion() == fOnServer.getVersion()) {
      return;
    }
    // update dat
    NearStrongConsistencyData data = this.getByCacheAndID(cache, userID);
    if (data == null) {
      data = new NearStrongConsistencyData(userID, cache);
      this.inconsistencyHist.add(data);
    }
    data.update(fOnServer.getFileSize());
  }


  @Override
  public String getInfo() {
    return "NoConsistency;No consistency control";
  }


  @Override
  public String[] getHeaders() {
    final String[] ret = {"Cache capacity[MB]", "Number inconsistencies",
        "Size of inconsistency files[MB]"};
    return ret;
  }

}
