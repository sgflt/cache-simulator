package cz.zcu.kiv.cacheSimulator.dataAccess;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.Observable;


public class ZipfFileNameGenerator extends Observable implements IFileQueue {

	/**
	 * promenna pro urceni poctu generovanych pozadavku
	 */
	private final long limit;
	
	/**
	 * pocet vygenerovanych pozadavku
	 */
	private long generatedAccesses;
	
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
	 * promenna pro nahodny zipf nahodny generator
	 */
	private ZIPFRandom random;

	public ZipfFileNameGenerator(final long limit) {
		super();
		this.addObserver(MainGUI.getInstance());
		this.random = new ZIPFRandom(maxValue, ZipfFileNameGenerator.alpha);
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
			notifyObservers((int) (this.generatedAccesses * 100 / this.limit));
		}
		
		//generovani jmena souboru a casu pristupu k nemu
		final int rndNum = Math.abs(this.random.zipfNext()) % (maxValue - minValue) + minValue;
		
		//jako cas pristupu vracime nulu - neresime mozne zpozdeni na siti
		//zpozdeni se resi "jen" u pristupu z logu
		return new Triplet<>(Integer.toString(rndNum), 0L, 0L);
	}

	@Override
	public Quartet<String, Long, Long, Long> getNextFileNameWithFSize() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * staticke nastaveni gaussovskeho generatoru - minimalni generovana hodnota
	 * @param minValue min hodnota
	 */
	public static void setMinValue(final int minValue) {
		ZipfFileNameGenerator.minValue = minValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - maximalni generovana hodnota
	 * @param maxValue max hodnota
	 */
	public static void setMaxValue(final int maxValue) {
		ZipfFileNameGenerator.maxValue = maxValue;
	}

	/**
	 * staticke nastaveni gaussovskeho generatoru - seed value pro nahodny generator
	 * @param seedValue seed
	 */
	public static void setAlpha(final double aplha) {
		ZipfFileNameGenerator.alpha = aplha;
	}

	@Override
	public void reset() {
		this.generatedAccesses = 0;
		this.random = new ZIPFRandom(maxValue, ZipfFileNameGenerator.alpha);
	}
}
