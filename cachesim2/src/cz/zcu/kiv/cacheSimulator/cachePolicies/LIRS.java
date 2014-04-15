package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LIRS algorithm
 * 
 * SOURCE: Adapted from article
 * "LIRS: An Efficient Low Interreference Recency Set Replacement Policy to Improve Buffer Cache Performance"
 * , by S. Jiang and X. Zhang
 * 
 * @author Pavel Bžoch
 * 
 */
public class LIRS implements ICache {

	/**
	 * zasobnik pro pamatovani pristupu k souborum
	 */
	private Stack<Triplet<FileOnClient, Long, Long>> zasobnikSouboru;

	/**
	 * struktury pro uchovavani souboru
	 */
	private ArrayList<Triplet<FileOnClient, Long, Long>> LIR, HIR;

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	/**
	 * kapacita cache
	 */
	private long capacity;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * promenna pro urceni logickeho casu
	 */
	private long timeCounter = 0;

	/**
	 * promenna pro urceni, kolik kapacity cache se ma dat na LIR soubory
	 */
	private static double LIR_CAPACITY = 0.9;

	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class TripletCompare implements
			Comparator<Triplet<FileOnClient, Long, Long>> {

		@Override
		public int compare(Triplet<FileOnClient, Long, Long> arg0,
				Triplet<FileOnClient, Long, Long> arg1) {
			if ((Long) arg0.getThird() > (Long) arg1.getThird())
				return 1;
			else if ((Long) arg0.getThird() < (Long) arg1.getThird())
				return -1;
			return 0;
		}
	}

	/**
	 * konstruktor
	 * 
	 * @param capacity
	 *            kapacita cache
	 */
	public LIRS() {
		super();
		this.timeCounter = 1;
		this.zasobnikSouboru = new Stack<Triplet<FileOnClient, Long, Long>>();
		this.LIR = new ArrayList<Triplet<FileOnClient, Long, Long>>();
		this.HIR = new ArrayList<Triplet<FileOnClient, Long, Long>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (Triplet<FileOnClient, Long, Long> file : LIR) {
			if (file.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		for (Triplet<FileOnClient, Long, Long> file : HIR) {
			if (file.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Triplet<FileOnClient, Long, Long> file = null;
		long actTime = ++timeCounter;
		// soubor je v LIR - aktualizujeme IRR (treti parametr)
		for (Triplet<FileOnClient, Long, Long> files : LIR) {
			if (files.getFirst().getFileName().equalsIgnoreCase(fName)) {
				file = files;
				break;
			}
		}
		if (file != null) {
			// spocteme a ulozime novou hodnotu IRR
			long IRR = zasobnikSouboru.size()
					- zasobnikSouboru.lastIndexOf(file);
			zasobnikSouboru.remove(file);
			file.setThird(IRR);
			file.setSecond(actTime);
			zasobnikSouboru.add(file);
			return file.getFirst();
		}
		// soubor je v HIR - aktualizujeme IRR (treti parametr), +- vymenime s
		// LIR
		else {
			for (Triplet<FileOnClient, Long, Long> files : HIR) {
				if (files.getFirst().getFileName().equalsIgnoreCase(fName)) {
					file = files;
					break;
				}
			}
			if (file != null) {
				// spocteme a ulozime novou hodnotu IRR
				long IRR = zasobnikSouboru.size()
						- zasobnikSouboru.lastIndexOf(file);
				zasobnikSouboru.remove(file);
				file.setThird(IRR);
				file.setSecond(actTime);
				zasobnikSouboru.add(file);

				// zjistime, zda soubor muzeme presunout do LIR ihned
				if (LIRsize() + file.getFirst().getFileSize() < capacity
						* LIR_CAPACITY) {
					HIR.remove(file);
					LIR.add(file);
				}
				// zjistime, zda IRR posledniho z LIR je vetsi nez IRR
				// aktualniho souboru
				else {
					// setridime kolekci
					Collections.sort(LIR, new TripletCompare());
					// vsechny soubory s IRR vetsim nez aktualni prehazeme do
					// HIR
					while (LIR.size() > 0
							&& LIR.get(LIR.size() - 1).getThird() > IRR) {
						HIR.add(LIR.get(LIR.size() - 1));
						LIR.remove(LIR.size() - 1);
					}
					// pokud se novy soubor vejde do LIR, presuneme jej tam
					if (LIRsize() + file.getFirst().getFileSize() < capacity
							* LIR_CAPACITY) {
						HIR.remove(file);
						LIR.add(file);
					}
					// jinak jej presuneme na konec HIR
					else {
						HIR.remove(file);
						HIR.add(file);
					}

				}
				return file.getFirst();
			}
		}
		return null;
	}

	/**
	 * metoda pro vypocteni kapacity LIR
	 * 
	 * @return kapacita LIR
	 */
	private long LIRsize() {
		long sumOfFiles = 0;
		for (Triplet<FileOnClient, Long, Long> file : LIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		return sumOfFiles;
	}

	@Override
	public long freeCapacity() {
		long sumOfFiles = 0;
		for (Triplet<FileOnClient, Long, Long> file : LIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		for (Triplet<FileOnClient, Long, Long> file : HIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		return this.capacity - sumOfFiles;
	}

	@Override
	public void removeFile() {
		if (HIR.size() > 0) {
			HIR.remove(0);
		} else if (LIR.size() > 0) {
			Collections.sort(LIR, new TripletCompare());
			LIR.remove(LIR.size() - 1);
		}
	}

	@Override
	public void insertFile(FileOnClient f) {
		// napred zkontrolujeme, jestli se soubor vejde do cache
		// pokud se nevejde, vztvorime pro nej okenko
		if (f.getFileSize() > this.capacity) {
			if (!fOverCapacity.isEmpty()) {
				fOverCapacity.add(f);
				return;
			}
			while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables
					.getCacheCapacityForDownloadWindow()))
				removeFile();
			fOverCapacity.add(f);
			this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables
					.getCacheCapacityForDownloadWindow()));
			return;
		}

		if (!fOverCapacity.isEmpty())
			checkTimes();

		// pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		long time = ++timeCounter;
		Triplet<FileOnClient, Long, Long> file = new Triplet<FileOnClient, Long, Long>(
				f, time, Long.MAX_VALUE);
		HIR.add(file);
		zasobnikSouboru.add(file);
	}

	@Override
	public String toString() {
		return "LIRS";
	}

	@Override
	public boolean needServerStatistics() {
		return false;
	}

	@Override
	public void setCapacity(long capacity) {
		this.capacity = capacity;
		this.initialCapacity = capacity;
	}

	@Override
	public void reset() {
		this.zasobnikSouboru.clear();
		this.LIR.clear();
		this.HIR.clear();
		this.timeCounter = 0;
		this.fOverCapacity.clear();
	}

	/**
	 * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache
	 * stazene - pak odstranime okenko
	 */
	private void checkTimes() {
		boolean hasBeenRemoved = true;
		while (hasBeenRemoved) {
			hasBeenRemoved = false;
			if (!fOverCapacity.isEmpty()
					&& fOverCapacity.get(0).getFRemoveTime() < GlobalVariables
							.getActualTime()) {
				fOverCapacity.remove(0);
				hasBeenRemoved = true;
			}
		}
		if (fOverCapacity.isEmpty()) {
			this.capacity = this.initialCapacity;
		}
	}

	@Override
	public String cacheInfo() {
		return "LIRS;LIRS";
	}

	public static double getLIR_CAPACITY() {
		return LIR_CAPACITY;
	}

	public static void setLIR_CAPACITY(double LIR_CAPACITY) {
		LIRS.LIR_CAPACITY = LIR_CAPACITY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (initialCapacity ^ (initialCapacity >>> 32));
		result = prime * result
				+ ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}
	
	@Override
	public void removeFile(FileOnClient f) {
		Triplet<FileOnClient, Long, Long> triplet = null;
		for (Triplet<FileOnClient, Long, Long> file : HIR){
			if (file.getFirst() == f){
				triplet = file;
				break;
			}
		}
		if (triplet != null){
			HIR.remove(triplet);
			return;
		}
		for (Triplet<FileOnClient, Long, Long> file : LIR){
			if (file.getFirst() == f){
				triplet = file;
				break;
			}
		}
		if (triplet != null){
			HIR.remove(triplet);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(HIR.size() + LIR.size());
		for (Triplet<FileOnClient, Long, Long> triplet : this.HIR){
			list.add(triplet.getFirst());
		}
		for (Triplet<FileOnClient, Long, Long> triplet : this.LIR){
			list.add(triplet.getFirst());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}

}
