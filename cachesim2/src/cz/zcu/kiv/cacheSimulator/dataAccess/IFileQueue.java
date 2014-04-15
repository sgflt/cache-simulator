package cz.zcu.kiv.cacheSimulator.dataAccess;


/**
 * rozhrani pro generovani nazvu souboru  
 * 
 * interface for generating file requests 
 * 
 * @author Pavel Bzoch
 *
 */
public interface IFileQueue{

	/**
	 * metoda pro generovani jmena souboru a casu pristupu k nemu 
	 * method for generating file name, access time and userID
	 * @return trojice jmeno + cas + userID
	 */
	 public RequstedFile getNextServerFile();
	 
	 /**
	  * metoda pro zresetovani generatotru pozadavku
	  */
	 public void resetQueue();
	 
	/**
	 * metoda pro nastaveni retezce, ktery se posila do GUI jako informace o progressu
	 * @param info info retezec
	 */
	 public void setInfo(String info);
	 
	 /**
	  * metoda pro uklid po nacteni vsech potrebnych hodnot
	  */
	 public void cleanUp();

}
