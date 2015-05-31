package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.util.Observable;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;


/**
 * trida pro generovani pristupu k souborum - pomoci gaussovskeho generatoru nahodnych cisel
 *
 * class for generating accesses to the files with Gaussian random generator
 *
 * @author Pavel Bzoch
 *
 */
public class GaussianFileNameGenerator extends Observable implements IFileQueue {

  /**
   * atribut pro generovani jmen souboru / atribut for random number generating
   */
  private Random rnd;

  /**
   * promenna pro urceni poctu generovanych pozadavku / variable for specifying limit of generated requests
   */
  private final long limit;

  /**
   * pocet vygenerovanych pozadavku / number of generated accesses
   */
  private long generatedAccesses = 0;

  /**
   * atribuity pro random generator / attributes for random generator
   */
  private static int meanValue = 0, dispersion = 0;

  /**
   * interval pro nahodny generator / intervals for random generators
   */
  private static int minValue = 1, maxValue = 500;

  /**
   * cislo pro nahodny generator / seed value for random generator
   */
  private static int seedValue = 0;

  /**
   * promenna pro uchovani, jak casto se ma generovat udalost pro GUI
   * variable for time interval between sending progress event
   */
  private int modulo = 1;

  /**
   * promenna pro uchovani retezce pro poslani do hlavniho gui
   */
  private String info;

  /**
   * staticke nastaveni gaussovskeho generatoru - stredni hodnota
   * setting of mean value for random generator
   * @param meanValue stredni hodnota
   */
  public static void setMeanValue(final int meanValue) {
    GaussianFileNameGenerator.meanValue = meanValue;
  }

  /**
   * staticke nastaveni gaussovskeho generatoru - rozptyl
   * setting of dispersion value for random generator
   * @param dispersion rozptyl
   */
  public static void setDispersion(final int dispersion) {
    GaussianFileNameGenerator.dispersion = dispersion;
  }

  /**
   * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
   * setting of min value for random generator
   * @param minValue min hodnota
   */
  public static void setMinValue(final int minValue) {
    GaussianFileNameGenerator.minValue = minValue;
  }

  /**
   * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
   * setting of max value for random generator
   * @param maxValue max hodnota
   */
  public static void setMaxValue(final int maxValue) {
    GaussianFileNameGenerator.maxValue = maxValue;
  }

  /**
   * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
   * setting of seed value for random generator
   * @param seedValue seed
   */
  public static void setSeedValue(final int seedValue) {
    GaussianFileNameGenerator.seedValue = seedValue;
  }

  /**
   * konstruktor
   * constructor - set limit for number of generated requests
   * @param limit pocet generovanych cisel
   */
  public GaussianFileNameGenerator(final long limit){
    super();
    GlobalVariables.setActualTime(0);
    this.addObserver(MainGUI.getInstance());
    this.rnd = new Random(seedValue);
    this.limit = limit;
    this.generatedAccesses = 0;
    this.modulo = (int)limit / 100;
    this.info = "Simulation in progress... ";
  }

  @Override
  public RequestedFile getNextServerFile() {
    GlobalVariables.setActualTime(GlobalVariables.getActualTime() + 1 + RequestedFile.getAddTime());

    //pocet pristupu - pri prekroceni limitu se ukoncuje generovani
    // tento pristup je volen pro jednodussi cteni z logu
    this.generatedAccesses++;
    if (this.generatedAccesses > this.limit){
      return null;
    }

    if (this.generatedAccesses % this.modulo == 0){
      this.setChanged();
      this.notifyObservers(this.info + " " + ((int)(this.generatedAccesses * 100 / this.limit)) + "%");
    }

    //generovani jmena souboru a casu pristupu k nemu
    int rndNum = minValue - 1;
    rndNum = (int) ((meanValue + this.rnd.nextGaussian() * dispersion));
    while (rndNum < minValue || rndNum > maxValue)
      rndNum = (int) ((meanValue + this.rnd.nextGaussian() * dispersion));
    if (rndNum > maxValue)
      rndNum = maxValue;
    if (rndNum < minValue)
      rndNum = minValue;

    //jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
    //zpozdeni se resi "jen" u pristupu z logu
    return new RequestedFile(Integer.toString(rndNum), RequestedFile.getAddTime() + this.generatedAccesses, -1, 0, true);
  }

  @Override
  public void resetQueue() {
    GlobalVariables.setActualTime(0);
    this.rnd = new Random(seedValue);
    this.generatedAccesses = 0;
    this.info = "Simulation in progress... ";
    this.setChanged();
    this.notifyObservers(0);
  }


  @Override
  public void setInfo(final String info) {
    this.info = info;
  }

  @Override
  public void cleanUp() {
    this.deleteObservers();
  }

}
