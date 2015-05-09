package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for MQ algorithm
 * 
 * SOURCE: Adapted from article
 * "The Multi-Queue Replacement Algorithm for Second Level Buffer Caches", by Y.
 * Zhou, J. F. Philbin and K. Li
 * 
 * @author Pavel Bžoch
 * 
 */
public class MQ implements ICache {

	/**
	 * pocet front
	 */
	private static int QUEUE_COUNT = 5;

	/**
	 * pocet front
	 */
	private static int LIFE_TIME = 100;

	/**
	 * velikost fronty QOUT
	 */
	private static int QOUT_CAPACITY = 10;

	/**
	 * promenna pro pocitani logickeho casu
	 */
	private long timeCounter = 0;

	/**
	 * velikost cache
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	/**
	 * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
	 * druhy je pro vypocty casu
	 */
	private Queue<Triplet<FileOnClient, Integer, Long>>[] fQueues;

	/**
	 * LRU fronty pro uchovani souboru, prvni Integer je pro pocitani referenci,
	 * druhy je pro vypocty casu
	 */
	private Queue<Triplet<FileOnClient, Integer, Long>> fQueueOut;

	@SuppressWarnings("unchecked")
	public MQ() {
		super();
		fQueueOut = new LinkedList<Triplet<FileOnClient, Integer, Long>>();
		fQueues = (Queue<Triplet<FileOnClient, Integer, Long>>[]) new Queue[QUEUE_COUNT];
		for (int i = 0; i < QUEUE_COUNT; i++) {
			fQueues[i] = new LinkedList<Triplet<FileOnClient, Integer, Long>>();
		}
		timeCounter = 0;
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (int i = 0; i < fQueues.length; i++) {
			for (Triplet<FileOnClient, Integer, Long> f : fQueues[i]) {
				if (f.getFirst().getFileName().equalsIgnoreCase(fName))
					return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Triplet<FileOnClient, Integer, Long> file = null;
		for (int i = 0; i < fQueues.length; i++) {
			if (file != null)
				break;
			for (Triplet<FileOnClient, Integer, Long> f : fQueues[i]) {
				if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
					file = f;
					fQueues[i].remove(file);
					break;
				}
			}
		}
		if (file == null)
			return null;
		else {
			file.setSecond(file.getSecond() + 1);
			file.setThird(++timeCounter + LIFE_TIME);
			int index = (int) (Math.log10(file.getSecond()) / Math.log10(2));
			if (index >= QUEUE_COUNT)
				index = QUEUE_COUNT - 1;
			fQueues[index].add(file);
			Adjust();
			return file.getFirst();
		}
	}

	/**
	 * metoda pro zarovnani LRU cache podle casu
	 */
	private void Adjust() {
		for (int i = 1; i < fQueues.length; i++) {
			for (Triplet<FileOnClient, Integer, Long> f : fQueues[i]) {
				if (f.getThird() < timeCounter) {
					fQueues[i].remove(f);
					fQueues[i - 1].add(f);
					i--;
					break;
				}
			}
		}
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (int i = 0; i < fQueues.length; i++) {
			for (Triplet<FileOnClient, Integer, Long> f : fQueues[i]) {
				obsazeno += f.getFirst().getFileSize();
			}
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		for (int i = 0; i < fQueues.length; i++) {
			if (fQueues[i].size() == 0)
				continue;
			else {
				Triplet<FileOnClient, Integer, Long> out = fQueues[i].remove();
				// v qout jsou uchovany metadata souboru
				if (fQueueOut.size() > QOUT_CAPACITY)
					fQueueOut.remove();
				fQueueOut.add(out);
				return;
			}
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
		// uvolneni mista pro dalsi soubor
		while (freeCapacity() < f.getFileSize())
			removeFile();

		// soubor je v qout - musi se nove stahnout, ale zustavaji mu parametry
		Triplet<FileOnClient, Integer, Long> newFile = null;
		for (Triplet<FileOnClient, Integer, Long> fout : fQueueOut) {
			if (fout.getFirst().getFileName().equalsIgnoreCase(f.getFileName())) {
				newFile = fout;
				fQueueOut.remove(fout);
				break;
			}
		}
		// soubor je uplne novy, zakladame parametry
		if (newFile == null)
			newFile = new Triplet<FileOnClient, Integer, Long>(f, 1,
					++timeCounter + LIFE_TIME);

		// umistime soubor do spravne LRU fronty
		int refCount = newFile.getSecond();
		int index = (int) (Math.log10(refCount) / Math.log10(2));
		if (index >= QUEUE_COUNT)
			index = QUEUE_COUNT - 1;
		fQueues[index].add(newFile);
		Adjust();
	}

	@Override
	public String toString() {
		return "MQ";
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
		this.fQueueOut.clear();
		for (int i = 0; i < QUEUE_COUNT; i++) {
			this.fQueues[i].clear();
		}
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
		return "MQ;MQ";
	}

	public static int getQUEUE_COUNT() {
		return QUEUE_COUNT;
	}

	public static void setQUEUE_COUNT(int QUEUE_COUNT) {
		MQ.QUEUE_COUNT = QUEUE_COUNT;
	}

	public static int getLIFE_TIME() {
		return LIFE_TIME;
	}

	public static void setLIFE_TIME(int LIFE_TIME) {
		MQ.LIFE_TIME = LIFE_TIME;
	}

	public static int getQOUT_CAPACITY() {
		return QOUT_CAPACITY;
	}

	public static void setQOUT_CAPACITY(int QOUT_CAPACITY) {
		MQ.QOUT_CAPACITY = QOUT_CAPACITY;
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
		for (int i = 0; i < fQueues.length; i++) {
			if (fQueues[i].size() == 0)
				continue;
			for (Triplet<FileOnClient, Integer, Long> file:fQueues[i]){
				if (file.getFirst() == f){
					fQueues[i].remove(file);
					if (fQueueOut.size() > QOUT_CAPACITY)
						fQueueOut.remove();
					fQueueOut.add(file);
					return;
				}
			}
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>();
		for (int i = 0; i < fQueues.length; i++) {
			if (fQueues[i].size() == 0)
				continue;
			for (Triplet<FileOnClient, Integer, Long> file:fQueues[i]){
				list.add(file.getFirst());
			}
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
