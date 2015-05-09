package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for FBR algorithm
 * 
 * SOURCE: Adapted from article
 * "Towards building a fault tolerant and conflict-free distributed file system for mobile clients"
 * , by A. Boukerche and R. Al-Shaikh
 * 
 * @author Pavel Bžoch
 * 
 */
public class FBR implements ICache {
	/**
	 * struktura pro uchovani souboru
	 */
	private ArrayList<Pair<FileOnClient, Integer>> fQueue;

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	/**
	 * velikost cache v B
	 */
	private long capacity = 0;

	/**
	 * konstanta pro urceni stare sekce
	 */
	private static double OLD_SECTION = 0.3;

	/**
	 * konstanta pro urceni nove sekce - neikrementuje se pocet hitu pri zasahu
	 */

	private static double NEW_SECTION = 0.6;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * konstruktor - inicializace cache
	 */
	public FBR() {
		fQueue = new ArrayList<Pair<FileOnClient, Integer>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (Pair<FileOnClient, Integer> pair : fQueue) {
			if (pair.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Pair<FileOnClient, Integer> foundFile = null;
		int pairIndex = 0;
		for (int i = 0; i < fQueue.size(); i++) {
			if (fQueue.get(i).getFirst().getFileName().equalsIgnoreCase(fName)) {
				foundFile = fQueue.get(i);
				pairIndex = i;
			}
		}
		if (foundFile == null)
			return null;
		else {
			// rozdeleni cache podle indexu
			long sumCap = 0;
			int newIndex = 0;
			for (int i = 0; i < fQueue.size(); i++) {
				sumCap += fQueue.get(i).getFirst().getFileSize();
				if (sumCap > (NEW_SECTION) * capacity) {
					newIndex = i;
					break;
				}
			}

			if (newIndex < pairIndex)
				foundFile.setSecond(foundFile.getSecond() + 1);

			fQueue.remove(foundFile);
			fQueue.add(foundFile);
			return foundFile.getFirst();
		}
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (Pair<FileOnClient, Integer> pair : fQueue) {
			obsazeno += pair.getFirst().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		long sumCap = 0;
		int oldIndex = -1;

		if (fQueue.size() == 0)
			return;

		for (int i = 0; i < fQueue.size(); i++) {
			sumCap += fQueue.get(i).getFirst().getFileSize();
			if (sumCap > (1 - OLD_SECTION) * capacity) {
				oldIndex = i;
				break;
			}
		}
		// odebereme podle LRU
		if (oldIndex == -1) {
			fQueue.remove(fQueue.size() - 1);
			return;
		}
		// odebereme podle LFU z OLD section
		Pair<FileOnClient, Integer> file = fQueue.get(oldIndex);
		for (int i = oldIndex; i < fQueue.size(); i++) {
			if (fQueue.get(i).getSecond() < file.getSecond())
				file = fQueue.get(i);
		}
		fQueue.remove(file);

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
		fQueue.add(new Pair<FileOnClient, Integer>(f, 1));
	}

	@Override
	public String toString() {
		return "FBR";
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
		this.fQueue.clear();
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
		return "FBR;FBR";
	}

	public static double getOLD_SECTION() {
		return OLD_SECTION;
	}

	public static void setOLD_SECTION(double OLD_SECTION) {
		FBR.OLD_SECTION = OLD_SECTION;
	}

	public static double getNEW_SECTION() {
		return NEW_SECTION;
	}

	public static void setNEW_SECTION(double NEW_SECTION) {
		FBR.NEW_SECTION = NEW_SECTION;
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
		Pair<FileOnClient, Integer> pair = null;
		for (Pair<FileOnClient, Integer> file : fQueue){
			if (file.getFirst() == f){
				pair = file;
				break;
			}
		}
		if (pair != null){
			fQueue.remove(pair);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fQueue.size());
		for (Pair<FileOnClient, Integer> file : fQueue){
			list.add(file.getFirst());
		}
		return list;
	}
	
	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
