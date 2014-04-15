package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.util.Observable;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;


/**
 * trida pro nahodne generovani jmen pristupovanych souboru
 * @author Pavel Bzoch
 *
 */
public class RandomFileNameGenerator extends Observable implements IFileQueue{

	/**
	 * atribut pro generovani jmen souboru
	 */
	private Random rnd;

	/**
	 * promenna pro urceni poctu generovanych pozadavku
	 */
	private long limit;
	
	/**
	 * pocet vygenerovanych pozadavku
	 */
	private long generatedAccesses = 0;
	
	/**
	 * interval pro nahodny generator
	 */
	private static int minValue = 1, maxValue = 500;
	
	/**
	 * cislo pro nahodny generator
	 */
	private static int seedValue = 0;
	
	/**
	 * promenna pro uchovani, jak casto se ma generovat udalost pro GUI
	 */
	private int modulo = 1;
	
	/**
	 * promenna pro uchovani retezce pro poslani do hlavniho gui
	 */
	private String info;
	
	
	public RandomFileNameGenerator(long limit) {
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
	public RequstedFile getNextServerFile() {
		GlobalVariables.setActualTime(GlobalVariables.getActualTime() + 1 + RequstedFile.getAddTime());
		
		//pocet pristupu - pri prekroceni limitu se ukoncuje generovani
		// tento pristup je volen pro jednodussi cteni z logu
		generatedAccesses++;
		if (generatedAccesses > limit)
			return null;
		
		if (generatedAccesses % modulo == 0){
			setChanged();
			notifyObservers(info + " " + ((int)(generatedAccesses * 100 / limit)) + "%");
		}
		
		//generovani jmena souboru a casu pristupu k nemu
		int rndNum = Math.abs(rnd.nextInt()) % (maxValue - minValue) + minValue;
		
		//jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
		//zpozdeni se resi "jen" u pristupu z logu
		return new RequstedFile(Integer.toString(rndNum), RequstedFile.getAddTime() + generatedAccesses, -1, 0, true);
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
	 * @param minValue min hodnota
	 */
	public static void setMinValue(int minValue) {
		RandomFileNameGenerator.minValue = minValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
	 * @param maxValue max hodnota
	 */
	public static void setMaxValue(int maxValue) {
		RandomFileNameGenerator.maxValue = maxValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
	 * @param seedValue seed
	 */
	public static void setSeedValue(int seedValue) {
		RandomFileNameGenerator.seedValue = seedValue;
	}
	
	@Override
	public void resetQueue() {
		GlobalVariables.setActualTime(0);
		this.rnd = new Random(seedValue);
		this.generatedAccesses = 0;
		this.info = "Simulation in progress... ";
		setChanged();
		notifyObservers(new Integer(0));
	}
	
	@Override
	public void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public void cleanUp() {
		this.deleteObservers();
	}

}
