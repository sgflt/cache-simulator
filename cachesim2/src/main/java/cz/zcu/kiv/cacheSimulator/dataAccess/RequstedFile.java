package cz.zcu.kiv.cacheSimulator.dataAccess;

/**
 * trida pro uchovani jednoho pristupu k souboru
 * pouziva se v pripade, ze jsou data z logu uchovavana v pameti pro rychlejsi simulaci
 * @author Pavel Bzoch
 *
 */
public class RequstedFile {
	/**
	 * promenna pro uchovani jmena souboru
	 */
	private String fname;
	
	/**
	 * promenna pro uchovani casu pristupu k souboru
	 */
	private long accessTime;
	
	/**
	 * promenna pro uchovani velikosti souboru 
	 */
	private long fSize;
	
	/**
	 * promenna pro uchovani ID uzivatele, ktery k souboru pristoupil
	 */
	private long userID;

	/**
	 * konstruktor - iniciace promennych
	 * @param fname jmeno souboru
	 * @param accessTime cas pristupu
	 * @param fSize velikost souboru
	 * @param userID identifikace uzivatele
	 */
	public RequstedFile(String fname, long accessTime, long fSize, long userID) {
		super();
		this.fname = fname;
		this.accessTime = accessTime;
		this.fSize = fSize;
		this.userID = userID;
	}

	/**
	 * metoda pro vraceni jmeno souboru
	 * @return jmeno souboru
	 */
	public String getFname() {
		return fname;
	}

	/**
	 * metoda pro vraceni casu pristupu k souboru
	 * @return cas pristupu
	 */
	public long getAccessTime() {
		return accessTime;
	}

	/**
	 * metoda pro vraceni velikosti souboru
	 * @return velikost souboru
	 */
	public long getfSize() {
		return fSize;
	}

	/**
	 * metoda pro vraceni id uzivatele
	 * @return identifikace uzivatele
	 */
	public long getUserID() {
		return userID;
	}
}
