package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.util.Observable;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

public class RandomFileNameGeneratorWithPrefences extends Observable implements IFileQueue{

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
	 * promenna pro urceni, jaky soubor v poradi bude preferovany
	 */
	private static int preferenceFile = 100;
	
	/**
	 * kolikaty krok urcite vede na preferovany soubor
	 */
	private static int preferenceStep = 4;
	
	/**
	 * nasobky tohoto cisla nebudou pristupovany
	 */
	private static int nonPreferenceFile = 7; 
	
	/**
	 * promenna pro uchovani, jak casto se ma generovat udalost pro GUI
	 */
	private int modulo = 1;
	
	/**
	 * promenna pro uchovani retezce pro poslani do hlavniho gui
	 */
	private String info;
	
	
	public RandomFileNameGeneratorWithPrefences(long limit) {
		super();
		this.info = "Simulation in progress... ";
		GlobalVariables.setActualTime(0);
		this.addObserver(MainGUI.getInstance());
		this.rnd = new Random(seedValue);
		this.limit = limit;
		this.generatedAccesses = 0;
		this.modulo = (int)limit / 100;
	}

	@Override
	public RequestedFile getNextServerFile() {
		GlobalVariables.setActualTime(GlobalVariables.getActualTime() + 1 + RequestedFile.getAddTime());
		
		//pocet pristupu - pri prekroceni limitu se ukoncuje generovani
		// tento pristup je volen pro jednodussi cteni z logu
		generatedAccesses++;
		if (generatedAccesses > limit)
			return null;
		
		if (generatedAccesses % modulo == 0){			
			setChanged();
			notifyObservers(info + " " + ((int)(generatedAccesses * 100 / limit)) + "%");
		}
		
		int rndNum = nonPreferenceFile;
		//generovani jmena souboru a casu pristupu k nemu
		//nejprve normalni generator
		if (generatedAccesses % preferenceStep != 0){
			while(rndNum % nonPreferenceFile == 0)
				rndNum = Math.abs(rnd.nextInt()) % (maxValue - minValue) + minValue;
		}
		//preferovany soubor
		else
		{
			rndNum = (Math.abs(rnd.nextInt()) % ((maxValue - minValue) / preferenceFile) + 1) * preferenceFile;
		}
		
		//jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
		//zpozdeni se resi "jen" u pristupu z logu
		return new RequestedFile(Integer.toString(rndNum), RequestedFile.getAddTime() + generatedAccesses, -1, 0, true); 
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
	 * @param minValue min hodnota
	 */
	public static void setMinValue(int minValue) {
		RandomFileNameGeneratorWithPrefences.minValue = minValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
	 * @param maxValue max hodnota
	 */
	public static void setMaxValue(int maxValue) {
		RandomFileNameGeneratorWithPrefences.maxValue = maxValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
	 * @param seedValue seed
	 */
	public static void setSeedValue(int seedValue) {
		RandomFileNameGeneratorWithPrefences.seedValue = seedValue;
	}

	/**
	 * metoda pro nastaveni preferovanych nasobku
	 * @param preferenceFile nasobky
	 */
	public static void setPreferenceFile(int preferenceFile) {
		RandomFileNameGeneratorWithPrefences.preferenceFile = preferenceFile;
	}

	/**
	 * metoda pro nastaveni kroku, ve kterych se bude generovat preferovane jmeno
	 * @param preferenceStep krok
	 */
	public static void setPreferenceStep(int preferenceStep) {
		RandomFileNameGeneratorWithPrefences.preferenceStep = preferenceStep;
	}

	/**
	 * metoda pro nastaveni nasobku, ktere nebudou generovany
	 * @param nonPreferenceFile nasobky
	 */
	public static void setNonPreferenceFile(int nonPreferenceFile) {
		RandomFileNameGeneratorWithPrefences.nonPreferenceFile = nonPreferenceFile;
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
