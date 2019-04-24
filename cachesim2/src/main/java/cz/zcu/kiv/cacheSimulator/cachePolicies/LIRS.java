package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

/**
 * class for LIRS algorithm
 * 
 * SOURCE: Adapted from article "LIRS: An Efficient Low Interreference Recency Set Replacement Policy to Improve Buffer Cache Performance", by 
 * S. Jiang and X. Zhang
 *  
 * @author Pavel Bžoch
 *
 */
public class LIRS implements ICache {

	/**
	 * zasobnik pro pamatovani pristupu k souborum
	 */
	private final Stack<Triplet<FileOnClient, Long, Long>> zasobnikSouboru;

	/**
	 * struktury pro uchovavani souboru
	 */
	private final ArrayList<Triplet<FileOnClient, Long, Long>> LIR;
  private final ArrayList<Triplet<FileOnClient, Long, Long>> HIR;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * kapacita cache
	 */
	private long capacity;

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
		public int compare(final Triplet<FileOnClient, Long, Long> arg0,
                       final Triplet<FileOnClient, Long, Long> arg1) {
			if (arg0.getThird() > arg1.getThird()) {
        return 1;
			} else if (arg0.getThird() < arg1.getThird()) {
        return -1;
      }
			return 0;
		}
	}

	/**
	 * konstruktor
	 * 
	 */
	public LIRS() {
		super();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.timeCounter = 1;
		this.zasobnikSouboru = new Stack<>();
		this.LIR = new ArrayList<>();
		this.HIR = new ArrayList<>();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		Triplet<FileOnClient, Long, Long> file = null;
		final long actTime = ++this.timeCounter;
		// soubor je v LIR - aktualizujeme IRR (treti parametr)
		for (final Triplet<FileOnClient, Long, Long> files : this.LIR) {
			if (files.getFirst().getFileName().equalsIgnoreCase(fileName)) {
				file = files;
				break;
			}
		}
		if (file != null) {
			// spocteme a ulozime novou hodnotu IRR
			final long IRR = this.zasobnikSouboru.size()
					- this.zasobnikSouboru.lastIndexOf(file);
      this.zasobnikSouboru.remove(file);
			file.setThird(IRR);
			file.setSecond(actTime);
      this.zasobnikSouboru.add(file);
			return file.getFirst();
		}
		// soubor je v HIR - aktualizujeme IRR (treti parametr), +- vymenime s
		// LIR
		else {
			for (final Triplet<FileOnClient, Long, Long> files : this.HIR) {
				if (files.getFirst().getFileName().equalsIgnoreCase(fileName)) {
					file = files;
					break;
				}
			}
			if (file != null) {
				// spocteme a ulozime novou hodnotu IRR
				final long IRR = this.zasobnikSouboru.size()
						- this.zasobnikSouboru.lastIndexOf(file);
        this.zasobnikSouboru.remove(file);
				file.setThird(IRR);
				file.setSecond(actTime);
        this.zasobnikSouboru.add(file);

				// zjistime, zda soubor muzeme presunout do LIR ihned
				if (LIRsize() + file.getFirst().getFileSize() < this.capacity
						* LIR_CAPACITY) {
          this.HIR.remove(file);
          this.LIR.add(file);
				}
				// zjistime, zda IRR posledniho z LIR je vetsi nez IRR
				// aktualniho souboru
				else {
					// setridime kolekci
					this.LIR.sort(new TripletCompare());
					// vsechny soubory s IRR vetsim nez aktualni prehazeme do
					// HIR
					while (!this.LIR.isEmpty()
							&& this.LIR.get(this.LIR.size() - 1).getThird() > IRR) {
            this.HIR.add(this.LIR.get(this.LIR.size() - 1));
            this.LIR.remove(this.LIR.size() - 1);
					}
					// pokud se novy soubor vejde do LIR, presuneme jej tam
					if (LIRsize() + file.getFirst().getFileSize() < this.capacity
							* LIR_CAPACITY) {
            this.HIR.remove(file);
            this.LIR.add(file);
					}
					// jinak jej presuneme na konec HIR
					else {
            this.HIR.remove(file);
            this.HIR.add(file);
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
		for (final Triplet<FileOnClient, Long, Long> file : this.LIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		return sumOfFiles;
	}

	@Override
	public long freeCapacity() {
		long sumOfFiles = 0;
		for (final Triplet<FileOnClient, Long, Long> file : this.LIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		for (final Triplet<FileOnClient, Long, Long> file : this.HIR) {
			sumOfFiles += file.getFirst().getFileSize();
		}
		return this.capacity - sumOfFiles;
	}

	@Override
	public void removeFile() {
		if (!this.HIR.isEmpty()) {
      this.HIR.remove(0);
		} else if (!this.LIR.isEmpty()) {
			this.LIR.sort(new TripletCompare());
      this.LIR.remove(this.LIR.size() - 1);
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
		final long time = ++this.timeCounter;
		final Triplet<FileOnClient, Long, Long> file = new Triplet<>(f, time,
			Long.MAX_VALUE);
    this.HIR.add(file);
    this.zasobnikSouboru.add(file);
	}
	
	@Override
	public String toString(){
		return "LIRS";
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
		this.zasobnikSouboru.clear();
		this.LIR.clear();
		this.HIR.clear();
		this.timeCounter = 0;
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
		return "LIRS;LIRS";
	}
        
        public static double getLIR_CAPACITY() {
            return LIR_CAPACITY;
        }

        public static void setLIR_CAPACITY(final double LIR_CAPACITY) {
            LIRS.LIR_CAPACITY = LIR_CAPACITY;
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
