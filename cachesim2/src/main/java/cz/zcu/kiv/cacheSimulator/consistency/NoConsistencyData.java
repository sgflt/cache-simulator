package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * trida pro simulaci pristupu bez konzistetnosti dat 
 * @author Pavel Bžoch
 *
 */
public class NoConsistencyData extends NearStrongConsistencyData {

	/**
	 * konstruktor - volani konstruktoru rodicovske tridy
	 * @param userID
	 * @param cache
	 */
	public NoConsistencyData(long userID, ICache cache) {
		super(userID, cache);
	}
	
	@Override
	public String toString() {
		 long id = userID >> 32;
         String ip = (GlobalMethods.intToIp(userID - (id << 32)));
		
		return "NoConsistency [userID=" + id + ", ip=" + ip + ", cache=" + cache
				+ ", cache cap="+ cache.getCacheCapacity() +", inconsistencyCount=" + inconsistencyCount
				+ ", inconsistencySize=" + inconsistencySize + "]";
	}
	
	

}