package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * trida pro uchovani dat o jednom uzivateli a jedne caching policy
 * @author Pavel Bžoch
 * 
 */
public class NearStrongConsistencyData{

	/**
	 * odkaz na id uzivateke
	 */
	protected long userID;
	
	/**
	 * odkaz na cachovaci politiku
	 */
	protected ICache cache;
	
	/**
	 * kolikrat doslo k nekonzistentnosti dat
	 */
	protected int inconsistencyCount; 
	
	/**
	 * objem nekonzistentnich dat
	 */
	protected long inconsistencySize;

	/**
	 * konstruktor - iniciace promennych
	 * @param userID id uzivatele
	 * @param cache odkaz na cache
	 */
	public NearStrongConsistencyData(long userID, ICache cache) {
		this.userID = userID;
		this.cache = cache;
		this.inconsistencyCount = 0;
		this.inconsistencySize = 0;
	}
	
	/**
	 * metoda pro update statistik
	 * @param fSize velikost souboru
	 */
	public void update(long fSize){
		inconsistencyCount++;
		inconsistencySize += fSize;
	}
	
	/**
	 * metoda pro porovnani se shodou na userID a cache 
	 * @param userID userID
	 * @param cache cache policy
	 * @return true, pokud je shoda
	 */
	public boolean compareTo(long userID, ICache cache){
		boolean ret = this.cache == cache && userID == this.userID;
		if (ret)
			return true;
		else return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		long id = userID >> 32;
        long ip = userID - (id << 32);
        
        int ipAndID = (int) (id + ip);
         
		result = prime * result + ((cache == null) ? 0 : cache.hashCode());
		result = prime * result + (int) (ipAndID);
		return result;
	}
	
	@Override
	public String toString() {
		 long id = userID >> 32;
         String ip = (GlobalMethods.intToIp(userID - (id << 32)));
		
		return "AccessCOnsistencyData [userID=" + id + ", ip=" + ip + ", cache=" + cache
				+ ", cache cap="+ cache.getCacheCapacity() +", inconsistencyCount=" + inconsistencyCount
				+ ", inconsistencySize=" + inconsistencySize + "]";
	}

	//sekce getru a setru
	public long getUserID() {
		return userID;
	}

	public ICache getCache() {
		return cache;
	}

	public int getInconsistencyCount() {
		return inconsistencyCount;
	}

	public long getInconsistencySize() {
		return inconsistencySize;
	}
	
	
}

