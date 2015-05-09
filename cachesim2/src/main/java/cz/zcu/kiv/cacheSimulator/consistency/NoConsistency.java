package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * Trida pro simulaci pristupu bez konzistentnosti
 * @author Pavel Bžoch
 *
 */
public class NoConsistency extends NearStrongConsistency implements IConsistencySimulation{

	@Override
	public void updateConsistencyWrite(ICache cache, long userID,
			FileOnClient fOnClient, FileOnServer fOnServer) {
		fOnClient.updateVerAndSize(fOnServer);
	}

	@Override
	public void updateActualReadFile(ICache cache, long userID,
			FileOnClient fOnClient, FileOnServer fOnServer) {
				
		//kontrola ruznosti verzi
		if (fOnClient.getVersion() > fOnServer.getVersion()){
			System.err.println("Verze na klientu je vyssi nez na serveru!");
			return;
		}
		if (fOnClient.getVersion() == fOnServer.getVersion()){
			return;
		}
		//update dat
		NearStrongConsistencyData data = getByCacheAndID(cache, userID);
		if (data == null){
			data = new NearStrongConsistencyData(userID, cache);
			inconsistencyHist.add(data);
		}
		data.update(fOnServer.getFileSize());
	}
	
	@Override
	public String getInfo() {
		return "NoConsistency;No consistency control";
	}
	
	@Override
	public String[] getHeaders() {
		 String[] ret = {"Cache capacity[MB]","Number inconsistencies", "Size of inconsistency files[MB]"};
		 return ret;
	}
	
}

