package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.ArrayList;
import java.util.Comparator;


/**
 * class for LRFU algorithm
 * 
 * SOURCE: Adapted from article "LRFU: a spectrum of policies that subsumes the least recently used and least frequently used policies", by
 * D. Lee, J. Choi, J.-H. Kim, S. Noh, S. L. Min, Y. Cho and C. S. Kim
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
	private final ArrayList<Triplet<FileOnClient, Long, Double>> fList;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v B
	 */
	private long capacity;

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
	private class PairCompare implements
			Comparator<Triplet<FileOnClient, Long, Double>> {

		@Override
		public int compare(final Triplet<FileOnClient, Long, Double> arg0,
                       final Triplet<FileOnClient, Long, Double> arg1) {

			if (arg0.getThird() > arg1.getThird()) {
        return 1;
			} else if (arg0.getThird() < arg1.getThird()) {
        return -1;
      }
			return 0;
		}
	}
	
	/**
	 * konstruktor - iniciace parametru
	 */
	public LRFU() {
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fList = new ArrayList<>();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		Triplet<FileOnClient, Long, Double> file = null;
		for (final Triplet<FileOnClient, Long, Double> triplet : this.fList) {
			if (triplet.getFirst().getFileName().equalsIgnoreCase(fileName)) {
				file = triplet;
				break;
			}
		}
		if (file == null) {
      return null;
    }
		
		final long actTime = ++this.timeCounter;
		file.setThird(calculateF(0) + file.getThird()
				* calculateF(actTime - file.getSecond()));
		file.setSecond(actTime);
    this.needSort = true;
		return file.getFirst();
	}

	@Override
	public long freeCapacity() {
		long sumCap = 0;
		for (final Triplet<FileOnClient, Long, Double> triplet : this.fList) {
			sumCap += triplet.getFirst().getFileSize();
		}
		return this.capacity - sumCap;
	}

	@Override
	public void removeFile() {
		if (this.needSort) {
			this.fList.sort(new PairCompare());
    }
    this.needSort = false;
		if (!this.fList.isEmpty()) {
      this.fList.remove(0);
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
    this.needSort = true;
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		this.fList.add(new Triplet<>(f, ++this.timeCounter,
			calculateF(0)));
	}

	/**
	 * metoda pro vypocet priority
	 * 
	 * @param x
	 *            casovy parametr
	 * @return priorita souboru
	 */
	private static double calculateF(final long x) {
		return Math.pow((1.0 / P), (LAMBDA * x));
	}
	
	@Override
	public String toString(){
		return "LRFU";
	}
	
	@Override
	public boolean needServerStatistics() {
		return false;
	}
	
	@Override
	public void setCapacity(final long capacity) {
		this.capacity = capacity;
	}

	@Override
	public void reset() {
		this.timeCounter = 1;
		this.fList.clear();
		this.needSort = true;
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
		return "LRFU;LRFU";
	}

        public static double getP() {
            return P;
        }

        public static void setP(final double P) {
            LRFU.P = P;
        }

        public static double getLAMBDA() {
            return LAMBDA;
        }

        public static void setLAMBDA(final double LAMBDA) {
            LRFU.LAMBDA = LAMBDA;
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
