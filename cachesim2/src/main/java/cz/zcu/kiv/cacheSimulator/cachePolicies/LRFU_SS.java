package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;


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
		public int compare(final Quartet<FileOnClient, Long, Double, Integer> o1,
                       final Quartet<FileOnClient, Long, Double, Integer> o2) {
			if (o1.getFourth() > o2.getFourth()) {
        return 1;
			} else if (o1.getFourth() < o2.getFourth()) {
        return -1;
      }
			return 0;
		}
	}

	/**
	 * struktura pro uchovani souboru
	 */
	private final ArrayList<Quartet<FileOnClient, Long, Double, Integer>> list;

	/**
	 * velikost cache v B
	 */
	private long capacity;

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
	 * promenna pro urceni poctu pristupu do cache a pro aktualizaci globalnich statistik
	 */
	private long accessCount = 0;
	
	/**
	 * promenna pro uchovani odkazu na server
	 */
	private final Server server = Server.getInstance();
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	public LRFU_SS() {
		this.list = new ArrayList<>();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public boolean isInCache(final String fName) {
    this.accessCount++;
		if (this.accessCount % 20 == 0){
			setGlobalReadCountServer(this.server.getGlobalReadHits(this));
		}
		for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
        return true;
      }
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(final String fName) {
		for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName)) {
				f.setSecond(System.nanoTime());
				f.setThird(f.getThird() + 1);
        this.needSort = true;
        this.needRecalculate = true;
				return f.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
			obsazeno += f.getFirst().getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		if (this.needRecalculate) {
			recalculatePriorities();
		}
		if (this.needSort) {
			this.list.sort(new QuartetCompare());
		}
    this.needSort = false;
    this.needRecalculate = false;
		if (!this.list.isEmpty()) {
      this.list.remove(0);
    }
	}

	/**
	 * metoda pro rekalkulaci priorit
	 */
	public void recalculatePriorities() {
		if (this.list.size() <= 1) {
      return;
    }
		long oldestTime = this.list.get(0).getSecond();
		long newestTime = this.list.get(0).getSecond();
		double maxReadHit = this.list.get(0).getThird();
		double minReadHit = this.list.get(0).getThird();
		// zjisteni lokalnich extremu
		for (final Quartet<FileOnClient, Long, Double, Integer> f : this.list) {
			if (f.getSecond() > newestTime) {
        newestTime = f.getSecond();
      }
			if (f.getSecond() < oldestTime) {
        oldestTime = f.getSecond();
      }
			if (maxReadHit < f.getThird()) {
        maxReadHit = f.getThird();
      }
			if (minReadHit > f.getThird()) {
        minReadHit = f.getThird();
      }
		}
		// vypocet priorit
		int PLRU;
		int PLFU_SS;
		for (final var f : this.list) {
			PLFU_SS = (int) ((f.getThird() - minReadHit) * 65535.0 / (maxReadHit - minReadHit));
			PLRU = (int) ((f.getSecond() - (double) oldestTime) * 65535.0 / ((double) newestTime - (double) oldestTime));
			f.setFourth((int) (K1 * PLRU + K2 * PLFU_SS));
		}
	}

	@Override
	public void insertFile(final FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		if (f.getFileSize() > this.capacity){
			if (!this.fOverCapacity.isEmpty()){
        this.fOverCapacity.add(f);
				return;
			}
			while (freeCapacity() < (long)((double)this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        removeFile();
      }
      this.fOverCapacity.add(f);
			this.capacity = (long) ((double)this.capacity * (1-GlobalVariables.getCacheCapacityForDownloadWindow()));
			return;
		}
		
		if (!this.fOverCapacity.isEmpty()) {
      checkTimes();
    }
			
		//pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		double localReadCount = 0;
		for (final Quartet<FileOnClient, Long, Double, Integer> files : this.list) {
			localReadCount += files.getThird();
		}
		double readHits = 0;
		if (this.globalReadCount > 0) {
      readHits = ((double)f.getReadHit() - (double)f.getWriteHit()) / (double) this.globalReadCount
				* localReadCount + 1;
    }
		this.list.add(new Quartet<>(f, System.nanoTime(),
			readHits, 0));
    this.needSort = true;
    this.needRecalculate = true;
	}

	/**
	 * metoda pro nastaveni poctu globalnich hitu
	 * 
	 * @param readCount
	 *            pocet hitu
	 */
	public void setGlobalReadCountServer(final long readCount) {
		this.globalReadCount = readCount;
	}
	
	@Override
	public String toString(){
		return "LRFU-SS";
	}
	
	@Override
	public boolean needServerStatistics() {
		return true;
	}
	
	@Override
	public void setCapacity(final long capacity) {
		this.capacity = capacity;
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
	 * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak odstranime okenko
	 */
	private void checkTimes() {
		boolean hasBeenRemoved = true;
		while (hasBeenRemoved){
			hasBeenRemoved = false;
			if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()){
        this.fOverCapacity.remove(0);
				hasBeenRemoved = true;
			}
		}
		if (this.fOverCapacity.isEmpty()){
			this.capacity = GlobalVariables.getCacheCapacity();
		}
	}
	
	@Override
	public String cacheInfo(){
		return "LRFU_SS;LRFU-SS";
	}
        
        public static double getK1() {
            return K1;
        }

        public static void setK1(final double K1) {
            LRFU_SS.K1 = K1;
        }

        public static double getK2() {
            return K2;
        }

        public static void setK2(final double K2) {
            LRFU_SS.K2 = K2;
        }
        
        @Override
    	public int hashCode() {
    		final int prime = 31;
    		int result = 1;
    		result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
    		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
    		return result;
    	}
}
