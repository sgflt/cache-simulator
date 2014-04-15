package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * trida pro prezentaci cache algoritmu 2Q
 * class for 2Q algorithm
 * 
 * SOURCE: Adapted from article "2Q: A Low Overhead High Performance Buffer Management Replacement Algorithm", 
 * by Theodore Johnson, Dennis  Shasha
 * 
 * @author Pavel Bzoch
 * 
 */
public class _2Q implements ICache {

	/**
	 * prvni fronta pro jen jednou referencovane soubory
	 */
	private Queue<FileOnClient> fQueueFIFO;
	
	/**
	 * LRU fronta pro vicekrat referencovane soubory
	 */
	private Queue<FileOnClient> fQueueLRU;

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	/**
	 * velikost cache v B
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * konstanta pro urceni, jak ma byt velka fifo pamet (v % velikosti cache)
	 */
	private static double FIFO_CAPACITY = 0.50f;

	public _2Q() {
		super();
		fQueueFIFO = new LinkedList<FileOnClient>();
		fQueueLRU = new LinkedList<FileOnClient>();
		fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (FileOnClient f : fQueueFIFO) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return true;
		}
		for (FileOnClient f : fQueueLRU) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		FileOnClient fromCache = null;
		for (FileOnClient f : fQueueFIFO) {
			if (f.getFileName().equalsIgnoreCase(fName)) {
				fromCache = f;
				break;
			}
		}
		if (fromCache != null) {
			fQueueFIFO.remove(fromCache);
			fQueueLRU.add(fromCache);
			return fromCache;
		} else {
			for (FileOnClient f : fQueueLRU) {
				if (f.getFileName().equalsIgnoreCase(fName)) {
					fromCache = f;
					break;
				}
			}
			if (fromCache != null) {
				fQueueLRU.remove(fromCache);
				fQueueLRU.add(fromCache);
				return fromCache;
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (FileOnClient f : fQueueFIFO) {
			obsazeno += f.getFileSize();
		}
		for (FileOnClient f : fQueueLRU) {
			obsazeno += f.getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		if (!fQueueFIFO.isEmpty())
			fQueueFIFO.remove();
		else {
			if (!fQueueLRU.isEmpty())
				fQueueLRU.remove();
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
		long fifoSize = 0;
		for (FileOnClient fifo : fQueueFIFO) {
			fifoSize += fifo.getFileSize();
		}
		while (fifoSize > (int) ((double) FIFO_CAPACITY * (double) this.capacity)) {
			fifoSize -= fQueueFIFO.remove().getFileSize();
		}
		fQueueFIFO.add(f);

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
	public String toString() {
		return "2Q";
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
		fQueueFIFO.clear();
		fQueueLRU.clear();
		fOverCapacity.clear();
	}

	@Override
	public String cacheInfo() {
		return "_2Q;2 Queues";
	}

	public static double getFIFO_CAPACITY() {
		return FIFO_CAPACITY;
	}

	public static void setFIFO_CAPACITY(double FIFO_CAPACITY) {
		_2Q.FIFO_CAPACITY = FIFO_CAPACITY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (initialCapacity ^ (initialCapacity >>> 32));
		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}

	@Override
	public void removeFile(FileOnClient f) {
		if (fQueueFIFO.contains(f))
			fQueueFIFO.remove(f);
		if (fQueueLRU.contains(f))
			fQueueLRU.remove(f);
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fQueueFIFO.size() + fQueueLRU.size());
		list.addAll(fQueueFIFO);
		list.addAll(fQueueLRU);
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
