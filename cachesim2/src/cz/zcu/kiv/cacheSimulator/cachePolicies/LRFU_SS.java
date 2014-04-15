package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRFU-SS algorithm
 * 
 * @author Pavel Bžoch
 * 
 */
public class LRFU_SS implements ICache {

	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class QuartetCompare implements
			Comparator<Quartet<FileOnClient, Long, Double, Integer>> {

		@Override
		public int compare(Quartet<FileOnClient, Long, Double, Integer> o1,
				Quartet<FileOnClient, Long, Double, Integer> o2) {
			if ((Integer) o1.getFourth() > (Integer) o2.getFourth())
				return 1;
			else if ((Integer) o1.getFourth() < (Integer) o2.getFourth())
				return -1;
			return 0;
		}
	}

	/**
	 * struktura pro uchovani souboru
	 */
	private ArrayList<Quartet<FileOnClient, Long, Double, Integer>> list;
	
	/**
	 * velikost cache v B
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * promenne pro urceni, jestli je treba tridit
	 */
	private boolean needSort = true;

	/**
	 * promenna pro urceni, zda je potreba spocitat znovu priority
	 */
	private boolean needRecalculate = true;

	/**
	 * koeficienty pro urceni priority
	 */
	private static double K1 = 0.35f, K2 = 1.1f;

	/**
	 * poromenna pro urceni globalniho poctu hitu na cteni
	 */
	private long globalReadCount = Long.MAX_VALUE;

	/**
	 * promenna pro urceni poctu pristupu do cache a pro aktualizaci globalnich
	 * statistik
	 */
	private long accessCount = 0;

	/**
	 * promenna pro uchovani odkazu na server
	 */
	private Server server = Server.getInstance();

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;

	/**
	 * konstruktor - inicializce cache
	 */
	public LRFU_SS() {
		list = new ArrayList<Quartet<FileOnClient, Long, Double, Integer>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		accessCount++;
		if (accessCount % 20 == 0) {
			setGlobalReadCountServer(server.getGlobalReadHits(this));
		}
		for (Quartet<FileOnClient, Long, Double, Integer> f : list) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (Quartet<FileOnClient, Long, Double, Integer> f : list) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
				f.setSecond(GlobalVariables.getActualTime());
				f.setThird(f.getThird() + 1);
				needSort = true;
				needRecalculate = true;
				return f.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		if (needRecalculate) {
			recalculatePriorities();
		}
		if (needSort) {
			Collections.sort(list, new QuartetCompare());
		}
		needSort = false;
		needRecalculate = false;
		long obsazeno = 0;
		for (Quartet<FileOnClient, Long, Double, Integer> f : list) {
			obsazeno += f.getFirst().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		if (needRecalculate) {
			recalculatePriorities();
		}
		if (needSort) {
			Collections.sort(list, new QuartetCompare());
		}
		needSort = false;
		needRecalculate = false;
		if (list.size() > 0){
			list.remove(0).getFirst();
		}
		
	}

	/**
	 * metoda pro rekalkulaci priorit
	 */
	public void recalculatePriorities() {
		if (list.size() <= 1)
			return;
		long oldestTime = list.get(0).getSecond(), newestTime = list.get(0)
				.getSecond();
		double maxReadHit = list.get(0).getThird(), minReadHit = list.get(0)
				.getThird();
		// zjisteni lokalnich extremu
		for (Quartet<FileOnClient, Long, Double, Integer> f : list) {
			if (f.getSecond() > newestTime)
				newestTime = f.getSecond();
			if (f.getSecond() < oldestTime)
				oldestTime = f.getSecond();
			if (maxReadHit < f.getThird())
				maxReadHit = f.getThird();
			if (minReadHit > f.getThird())
				minReadHit = f.getThird();
		}
		// vypocet priorit
		int PLRU, PLFU_SS;
		for (Quartet<FileOnClient, Long, Double, Integer> f : list) {
			PLFU_SS = (int) (((double)f.getThird() - (double)minReadHit) * 65535.0 / ((double)maxReadHit - (double)minReadHit));
			PLRU = (int) ((double)(f.getSecond() - (double)oldestTime) * 65535.0 / ((double)newestTime - (double)oldestTime));
			f.setFourth((int) (K1 * PLRU + K2 * PLFU_SS));
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
					.getCacheCapacityForDownloadWindow())) {
				removeFile();

			}
			fOverCapacity.add(f);
			this.capacity = (long) ((double) this.capacity * (1 - GlobalVariables
					.getCacheCapacityForDownloadWindow()));
			return;
		}

		if (!fOverCapacity.isEmpty()) {
			checkTimes();
		}

		// pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		double localReadCount = 0;
		for (Quartet<FileOnClient, Long, Double, Integer> files : list) {
			localReadCount += files.getThird();
		}
		double readHits = 0;
		if (globalReadCount > 0)
			readHits = ((double) f.getReadHit() - (double) f.getWriteHit())
					/ (double) globalReadCount * (double) localReadCount + 1;
		
		list.add(new Quartet<FileOnClient, Long, Double, Integer>(f, GlobalVariables.getActualTime(), readHits, 0));
		
		needSort = true;
		needRecalculate = true;
	}

	/**
	 * metoda pro nastaveni poctu globalnich hitu
	 * 
	 * @param readCount
	 *            pocet hitu
	 */
	public void setGlobalReadCountServer(long readCount) {
		this.globalReadCount = readCount;
	}

	@Override
	public String toString() {
		return "LRFU-SS";
		// return "LRFU-SS algorithm (K1="+K1+", K2=" + K2 +") ";
	}

	@Override
	public boolean needServerStatistics() {
		return true;
	}

	@Override
	public void setCapacity(long capacity) {
		this.capacity = capacity;
		this.initialCapacity = capacity;
	}

	@Override
	public void reset() {
		this.list.clear();
		this.needRecalculate = true;
		this.needSort = true;
		this.accessCount = 0;
		this.globalReadCount = Long.MAX_VALUE;
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
		return "LRFU_SS;LRFU-SS";
	}

	public static double getK1() {
		return K1;
	}

	public static void setK1(double K1) {
		LRFU_SS.K1 = K1;
	}

	public static double getK2() {
		return K2;
	}

	public static void setK2(double K2) {
		LRFU_SS.K2 = K2;
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
		Quartet<FileOnClient, Long, Double, Integer> quart = null;
		for (Quartet<FileOnClient, Long, Double, Integer> file : this.list){
			if (file.getFirst() == f){
				quart = file;
				break;
			}
		}
		if (quart != null){
			this.list.remove(quart);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> listRet = new ArrayList<FileOnClient>(this.list.size());
		for (Quartet<FileOnClient, Long, Double, Integer> file: this.list){
			listRet.add(file.getFirst());
		}
		return listRet;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
