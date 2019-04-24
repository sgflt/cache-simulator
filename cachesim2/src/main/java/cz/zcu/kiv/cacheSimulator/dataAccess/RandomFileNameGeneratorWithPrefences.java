package cz.zcu.kiv.cacheSimulator.dataAccess;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.Observable;
import java.util.Random;


public class RandomFileNameGeneratorWithPrefences extends Observable implements IFileQueue{

	/**
	 * atribut pro generovani jmen souboru
	 */
	private Random rnd;

	/**
	 * promenna pro urceni poctu generovanych pozadavku
	 */
	private final long limit;
	
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


	public RandomFileNameGeneratorWithPrefences(final long limit) {
		super();
		this.addObserver(MainGUI.getInstance());
		this.rnd = new Random(seedValue);
		this.limit = limit;
		this.generatedAccesses = 0;
		this.modulo = (int)limit / 100;
	}

	@Override
	public Triplet<String, Long, Long> getNextFileName() {
		GlobalVariables.setActualTime(0);
		
		//pocet pristupu - pri prekroceni limitu se ukoncuje generovani
		// tento pristup je volen pro jednodussi cteni z logu
		this.generatedAccesses++;
		if (this.generatedAccesses > this.limit) {
			return null;
		}

		if (this.generatedAccesses % this.modulo == 0) {
			setChanged();
			notifyObservers(new Integer((int) (this.generatedAccesses * 100 / this.limit)));
		}
		
		int rndNum = nonPreferenceFile;
		//generovani jmena souboru a casu pristupu k nemu
		//nejprve normalni generator
		if (this.generatedAccesses % preferenceStep != 0) {
			while (rndNum % nonPreferenceFile == 0) {
				rndNum = Math.abs(this.rnd.nextInt()) % (maxValue - minValue) + minValue;
			}
		}
		//preferovany soubor
		else
		{
			rndNum = (Math.abs(this.rnd.nextInt()) % ((maxValue - minValue) / preferenceFile) + 1) * preferenceFile;
		}
		
		//jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
		//zpozdeni se resi "jen" u pristupu z logu
		return new Triplet<String, Long, Long>(Integer.toString(rndNum), 0L, 0L);
	}

	@Override
	public Quartet<String, Long, Long, Long> getNextFileNameWithFSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		this.generatedAccesses = 0;
		this.rnd = new Random(seedValue);
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
	 * @param minValue min hodnota
	 */
	public static void setMinValue(final int minValue) {
		RandomFileNameGeneratorWithPrefences.minValue = minValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
	 * @param maxValue max hodnota
	 */
	public static void setMaxValue(final int maxValue) {
		RandomFileNameGeneratorWithPrefences.maxValue = maxValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
	 * @param seedValue seed
	 */
	public static void setSeedValue(final int seedValue) {
		RandomFileNameGeneratorWithPrefences.seedValue = seedValue;
	}

	/**
	 * metoda pro nastaveni preferovanych nasobku
	 * @param preferenceFile nasobky
	 */
	public static void setPreferenceFile(final int preferenceFile) {
		RandomFileNameGeneratorWithPrefences.preferenceFile = preferenceFile;
	}

	/**
	 * metoda pro nastaveni kroku, ve kterych se bude generovat preferovane jmeno
	 * @param preferenceStep krok
	 */
	public static void setPreferenceStep(final int preferenceStep) {
		RandomFileNameGeneratorWithPrefences.preferenceStep = preferenceStep;
	}

	/**
	 * metoda pro nastaveni nasobku, ktere nebudou generovany
	 * @param nonPreferenceFile nasobky
	 */
	public static void setNonPreferenceFile(final int nonPreferenceFile) {
		RandomFileNameGeneratorWithPrefences.nonPreferenceFile = nonPreferenceFile;
	}
	
	
	
}
