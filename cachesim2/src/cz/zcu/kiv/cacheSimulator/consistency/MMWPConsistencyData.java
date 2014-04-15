package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro uchovani statistik pro single background consistency 
 * @author Pavel Bžoch
 *
 */
public class MMWPConsistencyData {
	
    /** 
	 * odkaz na id uzivateke
	 */
	protected long userID;
	
	/**
	 * odkaz na cachovaci politiku
	 */
	protected ICache cache;
	
	/**
	 * kolikrat jsme se ptali na verzi souboru 
	 */
	private long asksCount = 0;
	
	/**
	 * kolikrat jsme upgradovali
	 */
	private long updatesCount = 0;
	
	/**
	 * velikost dat prenesenych kvuli konzistentnosti
	 */
	private long transferredData = 0;
	
	/**
	 * pocet nekonzistentnich dat, protoze upgrady se deji pouze po vyprseni TTL
	 */
	private long inconsistenciesCount = 0;
	private long inconsFrequentCount = 0, inconsMediumFirCount = 0, inconsMediumSecCount = 0, inconsMediumThiCount = 0, inconsLeastCount = 0;

	
	/**
	 * konstruktor - iniciace promennych
	 * @param userID id uzivatele
	 * @param cache odkaz na cache
	 */
	public MMWPConsistencyData(long userID, ICache cache) {
		this.userID = userID;
		this.cache = cache;
		this.asksCount = 0;
		this.updatesCount = 0;
		this.inconsistenciesCount = 0;
		this.transferredData = 0;
		this.inconsFrequentCount = 0;
		this.inconsLeastCount = 0;
		this.inconsMediumFirCount = 0;
		this.inconsMediumSecCount = 0;
		this.inconsMediumThiCount = 0;
	}
	
	/**
	 * metoda pro zvyseni poctu ptani
	 */
	public void updateAsks(){
		this.asksCount++;
	}
	
	/**
	 * metoda pro update poctu updatu souboru
	 */
	public void updateUpdates(){
		this.updatesCount++;
	}
	
	/**
	 * metoda pro update poctu inkonzistetnosti
	 */
	public void updateInconsistencies(FileOnClient fOnCl){
		this.inconsistenciesCount++;
		if (fOnCl.getVersion() >= MMWPConsistency.getHits5()) {
			this.inconsFrequentCount++;
			return;
		}
		if (fOnCl.getVersion() < MMWPConsistency.getHits2()) {
			this.inconsLeastCount++;
			return;
		}
		if (fOnCl.getVersion() < MMWPConsistency.getHits5() && fOnCl.getVersion() >= MMWPConsistency.getHits4()){
			this.inconsMediumFirCount++;
			return;
		}
		if (fOnCl.getVersion() < MMWPConsistency.getHits4() && fOnCl.getVersion() >= MMWPConsistency.getHits3()) {
			this.inconsMediumSecCount++;
			return;
		}
		if (fOnCl.getVersion() < MMWPConsistency.getHits3()	&& fOnCl.getVersion() >= MMWPConsistency.getHits2()) {
			this.inconsMediumThiCount++;
			return;
		}
	}
	
	/**
	 * metoda pro zvyseni velikosti prenesenych dat
	 * @param fSize velikost souboru
	 */
	public void updateTransferredData(long fSize){
		this.transferredData += fSize;
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
		
		return "BackgroudSingleConsistency  [userID=" + id + ", ip=" + ip + ", cache=" + cache
				+ ", cache cap="+ cache.getCacheCapacity() +", pocetPtani=" + asksCount
				+ ", pocetUpdatu=" + updatesCount + ", pocetInkonzistentnosti="+inconsistenciesCount+"]";
	}

	//sekce getru a setru pro zjisteni 
	public long getUserID() {
		return userID;
	}

	public ICache getCache() {
		return cache;
	}

	public long getAsksCount() {
		return asksCount;
	}

	public long getUpdatesCount() {
		return updatesCount;
	}

	public long getTransferredData() {
		return transferredData;
	}

	public long getInconsFrequentCount() {
		return inconsFrequentCount;
	}

	public long getInconsMediumFirCount() {
		return inconsMediumFirCount;
	}

	public long getInconsMediumSecCount() {
		return inconsMediumSecCount;
	}

	public long getInconsMediumThiCount() {
		return inconsMediumThiCount;
	}

	public long getInconsLeastCount() {
		return inconsLeastCount;
	}
	
	public boolean checkInconsistrencies(){
		long sum = inconsFrequentCount + inconsLeastCount + inconsMediumFirCount + inconsMediumSecCount + inconsMediumThiCount;
		return sum == inconsistenciesCount;
	}


}