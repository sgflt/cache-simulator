package cz.zcu.kiv.cacheSimulator.dataAccess;

/**
 * trida pro uchovani jednoho pristupu k souboru
 * pouziva se v pripade, ze jsou data z logu uchovavana v pameti pro rychlejsi simulaci
 * @author Pavel Bzoch
 *
 */
public class RequestedFile {
	/**
	 * promenna pro uchovani jmena souboru
	 */
	private final String fname;
	
	/**
	 * promenna pro uchovani casu pristupu k souboru
	 */
	private final long accessTime;
	
	/**
	 * promenna pro uchovani velikosti souboru 
	 */
	private final long fSize;
	
	/**
	 * promenna pro uchovani ID uzivatele, ktery k souboru pristoupil
	 */
	private final long userID;

	/**
	 * konstruktor - iniciace promennych
	 * @param fname jmeno souboru
	 * @param accessTime cas pristupu
	 * @param fSize velikost souboru
	 * @param userID identifikace uzivatele
	 */
	public RequestedFile(final String fname, final long accessTime, final long fSize, final long userID) {
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
		return this.fname;
	}

	/**
	 * metoda pro vraceni casu pristupu k souboru
	 * @return cas pristupu
	 */
	public long getAccessTime() {
		return this.accessTime;
	}

	/**
	 * metoda pro vraceni velikosti souboru
	 * @return velikost souboru
	 */
	public long getfSize() {
		return this.fSize;
	}

	/**
	 * metoda pro vraceni id uzivatele
	 * @return identifikace uzivatele
	 */
	public long getUserID() {
		return this.userID;
	}
}
