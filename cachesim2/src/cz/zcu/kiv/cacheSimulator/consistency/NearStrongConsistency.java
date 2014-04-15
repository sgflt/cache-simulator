package cz.zcu.kiv.cacheSimulator.consistency;

import java.util.ArrayList;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro praci s konzistentnosti, kdy se pred pristupem k souboru vzdy ptame, 
 * jestli je aktualni a pokud neni, stahneme novou verzi 
 * @author Pavel Bzoch
 *
 */
public class NearStrongConsistency implements IConsistencySimulation{

	/**
	 * promenna pro uchovani data o nekonzistentnim stavu
	 */
	protected ArrayList<NearStrongConsistencyData> inconsistencyHist;
	
	/**
	 * konstruktor - iniciace promennych
	 */
	public NearStrongConsistency() {
		inconsistencyHist = new ArrayList<NearStrongConsistencyData>();
	}

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
		
		//update verze
		fOnClient.updateVerAndSize(fOnServer);
	}
	
	/**
	 * metoda pro ziskani datove instance podle cache a userID
	 * @param hash hask kod
	 * @return instance tridy NoConsistencyData
	 */
	protected NearStrongConsistencyData getByCacheAndID(ICache cache, long userID) {
		for (NearStrongConsistencyData data : inconsistencyHist){
			if (data.compareTo(userID, cache))
				return data;
		}
		return null;
	}

	public void printStat() {
		System.out.println("Statistiky pro simulaci pristupove konzistentnosti");
		for (NearStrongConsistencyData data : inconsistencyHist){
			System.out.println(data);
		}
	}
	
	@Override
	public String getInfo() {
		return "NearStrongConsistency;Near Strong consistency control";
	}
	
	@Override
	public String[] getHeaders() {
		 String[] ret = {"Cache capacity[MB]","Number inconsistencies", "Size of transferred files[MB]"};
		 return ret;
	}

	@Override
	public Object[][] getData(String cacheName, long userID) {
		Object[][] ret= new Object[MainGUI.getInstance().getCacheSizes().length][getHeaders().length];
		int row = 0;
		boolean isRes = false;
		for (NearStrongConsistencyData data : inconsistencyHist) {
			if (data.getUserID() == userID && data.getCache().getClass().getName().contains(cacheName)){
				ret[row][0] = data.getCache().getCacheCapacity() / 1024/1024;
				ret[row][1] = data.getInconsistencyCount();
				ret[row][2] = data.getInconsistencySize() /1024 /1024.0;
				row++;
				isRes = true;
			}
		}
		if (!isRes)
			return null;
		return ret;
	}	

}