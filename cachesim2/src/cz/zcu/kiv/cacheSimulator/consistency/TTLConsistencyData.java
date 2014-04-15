package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;


/**
 * trida pro uchovavani dat o TTL konzistentnosti 
 * @author Pavel Bžoch
 *
 */
public class TTLConsistencyData {

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
	 * promenne pro ulozeni casu, kdy doslo naposledy ke sledovani veryi souboru
	 */
	private long lastAccessTime = -1;
	/**
	 * pocet nekonzistentnich dat, protoze upgrady se deji pouze po vyprseni TTL
	 */
	private long inconsistenciesCount = 0;
	
	/**
	 * velikost dat prenesenych kvuli konzistentnosti
	 */
	private long transferredData = 0;

	/**
	 * konstruktor - iniciace promennych
	 * 
	 * @param userID
	 *            id uzivatele
	 * @param cache
	 *            odkaz na cache
	 */
	public TTLConsistencyData(long userID, ICache cache) {
		this.userID = userID;
		this.cache = cache;
		this.asksCount = 0;
		this.updatesCount = 0;
		this.inconsistenciesCount = 0;
		this.lastAccessTime = -1;
		this.transferredData = 0;
	}

	/**
	 * metoda pro zvyseni poctu ptani se
	 */
	public void updateAsks(){
		this.asksCount++;
	}
	
	/**
	 * metoda pro update poctu updatu souboru
	 */
	public void updateUpdates() {
		this.updatesCount++;
	}

	/**
	 * metoda pro update poctu inkonzistetnosti
	 */
	public void updateInconsistencies() {
		this.inconsistenciesCount++;
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
	 * 
	 * @param userID
	 *            userID
	 * @param cache
	 *            cache policy
	 * @return true, pokud je shoda
	 */
	public boolean compareTo(long userID, ICache cache) {
		boolean ret = this.cache == cache && userID == this.userID;
		if (ret)
			return true;
		else
			return false;
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

		return "TTL consistency [userID=" + id + ", ip=" + ip
				+ ", cache=" + cache + ", cache cap="
				+ cache.getCacheCapacity() + ", pocetPtani=" + asksCount+", pocetUpdatu=" + updatesCount + ", pocetInkonzistentnosti="
				+ inconsistenciesCount + "]";
	}
	
	
	/**
	 * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole souboru
	 * @return cas posledni kontroly
	 */
	public long getLastAccessTime() {
		return lastAccessTime;
	}

	/**
	 * metoda pro nastaveni noveho casu kontroly souboru
	 * @param lastAccessTimeFrequent cas
	 */
	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	//sekce getru - ziskani pozadovanych hodnot
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

	public long getInconsistenciesCount() {
		return inconsistenciesCount;
	}

	public long getTransferredData() {
		return transferredData;
	}
	
	
	
}
