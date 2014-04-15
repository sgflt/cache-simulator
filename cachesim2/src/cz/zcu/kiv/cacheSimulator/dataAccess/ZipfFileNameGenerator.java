package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.util.Observable;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;


public class ZipfFileNameGenerator extends Observable implements IFileQueue {

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
	 * aplha pro nahodny generator
	 */
	private static double alpha = 0.75;
	
	/**
	 * promenna pro uchovani, jak casto se ma generovat udalost pro GUI
	 */
	private int modulo = 1;
	
	/**
	 * promenna pro uchovani retezce pro poslani do hlavniho gui
	 */
	private String info;
	
	/**
	 * promenna pro nahodny zipf nahodny generator
	 */
	private ZIPFRandom random;
	
	public ZipfFileNameGenerator(long limit) {
		super();
		this.info = "Simulation in progress... ";
		GlobalVariables.setActualTime(0);
		this.addObserver(MainGUI.getInstance());
		this.random = new ZIPFRandom(maxValue, ZipfFileNameGenerator.alpha);
		this.limit = limit;
		this.generatedAccesses = 0;
		this.modulo = (int)limit / 100;
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
		int rndNum = Math.abs(random.zipfNext()) % (maxValue - minValue) + minValue;
		
		//jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
		//zpozdeni se resi "jen" u pristupu z logu
		return new RequstedFile(Integer.toString(rndNum), RequstedFile.getAddTime() + generatedAccesses, -1, 0, true);
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
	 * @param minValue min hodnota
	 */
	public static void setMinValue(int minValue) {
		ZipfFileNameGenerator.minValue = minValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
	 * @param maxValue max hodnota
	 */
	public static void setMaxValue(int maxValue) {
		ZipfFileNameGenerator.maxValue = maxValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
	 * @param seedValue seed
	 */
	public static void setAlpha(double aplha) {
		ZipfFileNameGenerator.alpha = aplha;
	}
	
	@Override
	public void resetQueue() {
		GlobalVariables.setActualTime(0);
		this.random = new ZIPFRandom(maxValue, ZipfFileNameGenerator.alpha);
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
