package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRFU algorithm
 * 
 * SOURCE: Adapted from article
 * "LRFU: a spectrum of policies that subsumes the least recently used and least frequently used policies"
 * , by D. Lee, J. Choi, J.-H. Kim, S. Noh, S. L. Min, Y. Cho and C. S. Kim
 * 
 * @author Pavel Bžoch
 * 
 */
public class LRFU implements ICache {

	/**
	 * konstanta p
	 */
	private static double P = 2.0f;

	/**
	 * konstanta lambda
	 */
	private static double LAMBDA = 0.045f;

	/**
	 * atribut pro pocitani casu
	 */
	private long timeCounter = 1;

	/**
	 * atribut pro uchovani souboru v cache
	 */
	private ArrayList<Triplet<FileOnClient, Long, Double>> fList;

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
	 * promenna pro urceni, zda je potreba setridit pole cachovanych souboru
	 */
	private boolean needSort = true;

	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class TripletCompare implements
			Comparator<Triplet<FileOnClient, Long, Double>> {

		@Override
		public int compare(Triplet<FileOnClient, Long, Double> arg0,
				Triplet<FileOnClient, Long, Double> arg1) {

			if ((Double) arg0.getThird() > (Double) arg1.getThird())
				return 1;
			else if ((Double) arg0.getThird() < (Double) arg1.getThird())
				return -1;
			return 0;
		}
	}

	/**
	 * konstruktor - iniciace parametru
	 * 
	 * @param capacity
	 */
	public LRFU() {
		super();
		this.needSort = true;
		this.fList = new ArrayList<Triplet<FileOnClient, Long, Double>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (Triplet<FileOnClient, Long, Double> triplet : fList) {
			if (triplet.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Triplet<FileOnClient, Long, Double> file = null;
		for (Triplet<FileOnClient, Long, Double> triplet : fList) {
			if (triplet.getFirst().getFileName().equalsIgnoreCase(fName)) {
				file = triplet;
				break;
			}
		}
		if (file == null)
			return null;

		long actTime = ++timeCounter;
		file.setThird(calculateF(0) + file.getThird()
				* calculateF(actTime - file.getSecond()));
		file.setSecond(actTime);
		needSort = true;
		return file.getFirst();
	}

	@Override
	public long freeCapacity() {
		long sumCap = 0;
		for (Triplet<FileOnClient, Long, Double> triplet : fList) {
			sumCap += triplet.getFirst().getFileSize();
		}
		return capacity - sumCap;
	}

	@Override
	public void removeFile() {
		if (needSort)
			Collections.sort(fList, new TripletCompare());
		needSort = false;
		if (fList.size() > 0) {
			fList.remove(0);
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
		needSort = true;
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		fList.add(new Triplet<FileOnClient, Long, Double>(f, ++timeCounter,
				calculateF(0)));
	}

	/**
	 * metoda pro vypocet priority
	 * 
	 * @param x
	 *            casovy parametr
	 * @return priorita souboru
	 */
	private double calculateF(long x) {
		return Math.pow((1.0 / P), (LAMBDA * x));
	}

	@Override
	public String toString() {
		return "LRFU";
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
		this.timeCounter = 1;
		this.fList.clear();
		this.needSort = true;
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
		return "LRFU;LRFU";
	}

	public static double getP() {
		return P;
	}

	public static void setP(double P) {
		LRFU.P = P;
	}

	public static double getLAMBDA() {
		return LAMBDA;
	}

	public static void setLAMBDA(double LAMBDA) {
		LRFU.LAMBDA = LAMBDA;
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
		Triplet<FileOnClient, Long, Double> triplet = null;
		for (Triplet<FileOnClient, Long, Double> file : fList){
			if (file.getFirst() == f){
				triplet = file;
				break;
			}
		}
		if (triplet != null){
			fList.remove(triplet);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fList.size());
		for (Triplet<FileOnClient, Long, Double> file : fList){
			list.add(file.getFirst());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}

}
