package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRU-K algorithm
 * 
 * SOURCE: Adapted from article
 * "The LRU-K page replacement algorithm for database disk buffering", by E. J.
 * O'Neil, P. E. O'Neil and G. Weikum
 * 
 * @author Pavel Bžoch
 * 
 */
public class LRU_K implements ICache {

	/**
	 * Korelace pro vyhazovani souboru z cache
	 */
	private static int CORRELATED_REFERENCE_PERIOD = 7;

	/**
	 * urcuje, kolik casu si budeme pamatovat
	 */
	private static int K = 3;

	/**
	 * promenna pro pocitani logickeho casu prichodu souboru
	 */
	private long timeCounter = 0;

	/**
	 * velikost cache v kB
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * struktura pro ukladani souboru
	 */
	private ArrayList<Triplet<FileOnClient, Long[], Long>> fList;

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	public LRU_K() {
		super();
		this.fList = new ArrayList<Triplet<FileOnClient, Long[], Long>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (Triplet<FileOnClient, Long[], Long> files : fList) {
			if (files.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Triplet<FileOnClient, Long[], Long> fileInCache = null;
		for (Triplet<FileOnClient, Long[], Long> files : fList) {
			if (files.getFirst().getFileName().equalsIgnoreCase(fName)) {
				fileInCache = files;
				break;
			}
		}

		if (fileInCache == null)
			return null;
		long actTime = ++timeCounter;
		if (actTime - fileInCache.getThird() > CORRELATED_REFERENCE_PERIOD) {
			long correlPeriodOfRefPage = fileInCache.getThird()
					- fileInCache.getSecond()[0];
			for (int i = 1; i < K; i++) {
				fileInCache.getSecond()[i] = fileInCache.getSecond()[i - 1]
						+ correlPeriodOfRefPage;
			}
			fileInCache.getSecond()[0] = actTime;
			fileInCache.setThird(actTime);
		} else {
			fileInCache.setThird(actTime);
		}
		return fileInCache.getFirst();
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (Triplet<FileOnClient, Long[], Long> files : fList) {
			obsazeno += files.getFirst().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		long min = ++timeCounter;
		Triplet<FileOnClient, Long[], Long> victim = null;

		for (Triplet<FileOnClient, Long[], Long> files : fList) {
			if (timeCounter - files.getThird() > CORRELATED_REFERENCE_PERIOD)
				if (files.getSecond()[K - 1] < min) {
					victim = files;
					min = files.getSecond()[K - 1];
				}
		}

		if (victim != null)
			fList.remove(victim);
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
		long actTime = ++timeCounter;
		Long[] lastTimes = new Long[K];
		for (int i = 0; i < lastTimes.length; i++) {
			lastTimes[i] = 0L;
		}
		lastTimes[0] = actTime;
		fList.add(new Triplet<FileOnClient, Long[], Long>(f, lastTimes, actTime));

	}

	@Override
	public String toString() {
		return "LRU-K";
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
		this.timeCounter = 0;
		this.fList.clear();
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
		return "LRU_K;LRU-K";
	}

	public static int getCORRELATED_REFERENCE_PERIOD() {
		return CORRELATED_REFERENCE_PERIOD;
	}

	public static void setCORRELATED_REFERENCE_PERIOD(
			int CORRELATED_REFERENCE_PERIOD) {
		LRU_K.CORRELATED_REFERENCE_PERIOD = CORRELATED_REFERENCE_PERIOD;
	}

	public static int getK() {
		return K;
	}

	public static void setK(int K) {
		LRU_K.K = K;
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
		Triplet<FileOnClient, Long[], Long> triplet = null;
		for (Triplet<FileOnClient, Long[], Long> file : fList) {
			if (file.getFirst() == f) {
				triplet = file;
				break;
			}
		}
		if (triplet != null) {
			fList.remove(triplet);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fList.size());
		for (Triplet<FileOnClient, Long[], Long> file : fList) {
			list.add(file.getFirst());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}

}
