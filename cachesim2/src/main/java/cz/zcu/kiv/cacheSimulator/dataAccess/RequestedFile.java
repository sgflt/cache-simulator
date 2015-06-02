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
   * promenna pro uchovani, zda byl nacten soubor pro cteni nebo doslo k zapisu souboru
   */
  private final boolean read;

  /**
   * promenna pro pripocitavani casu, pokud simulace bezi vicekrat za sebou
   */
  private static long addTime = 0;

  /**
   * konstruktor - iniciace promennych
   * @param fname jmeno souboru
   * @param accessTime cas pristupu
   * @param fSize velikost souboru
   * @param userID identifikace uzivatele
   */
  public RequestedFile(final String fname, final long accessTime, final long fSize, final long userID, final boolean read) {
    super();
    this.fname = fname;
    this.accessTime = accessTime + addTime;
    this.fSize = fSize;
    this.userID = userID;
    this.read = read;
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

  /**
   * metoda vraci, jestli byl soubor cten/zapisovan
   * @return true, pokud cten
   */
  public boolean isRead() {
    return this.read;
  }

  /**
   * metoda pro nastaveni add time
   * @param addTime pripocitavany cas
   */
  public static void setAddTime(final long addTime) {
    if (addTime < 0)
      return;
    RequestedFile.addTime = addTime;
  }

  /**
   * metoda pro zjisteni periody simulace
   * @return cas, jak dlouho probihala simualace
   */
  public static long getAddTime() {
    return addTime;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RequestedFile [fname=" + this.fname + ", userID=" + this.userID + "]";
  }
}
