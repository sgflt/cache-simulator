package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

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
	private final AccessStack<Triplet<FileOnClient, Long, Long>> zasobnikSouboru;

	/**
	 * struktury pro uchovavani souboru
	 */
	private final ArrayList<Triplet<FileOnClient, Long, Long>> LIR;
	private final ArrayList<Triplet<FileOnClient, Long, Long>> HIR;

	/**
	 * the same entries by name key, for lookups
	 */
	private final Map<String, Triplet<FileOnClient, Long, Long>> lirByName = new HashMap<>();
	private final Map<String, Triplet<FileOnClient, Long, Long>> hirByName = new HashMap<>();

	/**
	 * sums of sizes of the files in LIR and HIR
	 */
	private long lirUsedCapacity;
	private long hirUsedCapacity;

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
		this.zasobnikSouboru = new AccessStack<>();
		this.LIR = new ArrayList<>();
		this.HIR = new ArrayList<>();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		final String key = FileKey.of(fileName);
		Triplet<FileOnClient, Long, Long> file = this.lirByName.get(key);
		final long actTime = ++this.timeCounter;
		// soubor je v LIR - aktualizujeme IRR (treti parametr)
		if (file != null) {
			// spocteme a ulozime novou hodnotu IRR
			final long IRR = this.zasobnikSouboru.size()
					- this.zasobnikSouboru.indexOf(file);
      this.zasobnikSouboru.remove(file);
			file.setThird(IRR);
			file.setSecond(actTime);
      this.zasobnikSouboru.push(file);
			return file.getFirst();
		}
		// soubor je v HIR - aktualizujeme IRR (treti parametr), +- vymenime s
		// LIR
		else {
			file = this.hirByName.get(key);
			if (file != null) {
				// spocteme a ulozime novou hodnotu IRR
				final long IRR = this.zasobnikSouboru.size()
						- this.zasobnikSouboru.indexOf(file);
        this.zasobnikSouboru.remove(file);
				file.setThird(IRR);
				file.setSecond(actTime);
        this.zasobnikSouboru.push(file);

				// zjistime, zda soubor muzeme presunout do LIR ihned
				if (this.lirUsedCapacity + file.getFirst().getFileSize() < this.capacity
						* LIR_CAPACITY) {
          removeFromHir(file);
          addToLir(file);
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
            addToHir(removeLastFromLir());
					}
					// pokud se novy soubor vejde do LIR, presuneme jej tam
					if (this.lirUsedCapacity + file.getFirst().getFileSize() < this.capacity
							* LIR_CAPACITY) {
            removeFromHir(file);
            addToLir(file);
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

	private void addToLir(final Triplet<FileOnClient, Long, Long> file) {
		this.LIR.add(file);
		this.lirByName.put(FileKey.of(file.getFirst().getFileName()), file);
		this.lirUsedCapacity += file.getFirst().getFileSize();
	}

	private Triplet<FileOnClient, Long, Long> removeLastFromLir() {
		final Triplet<FileOnClient, Long, Long> file = this.LIR.remove(this.LIR.size() - 1);
		this.lirByName.remove(FileKey.of(file.getFirst().getFileName()));
		this.lirUsedCapacity -= file.getFirst().getFileSize();
		return file;
	}

	private void addToHir(final Triplet<FileOnClient, Long, Long> file) {
		this.HIR.add(file);
		this.hirByName.put(FileKey.of(file.getFirst().getFileName()), file);
		this.hirUsedCapacity += file.getFirst().getFileSize();
	}

	private void removeFromHir(final Triplet<FileOnClient, Long, Long> file) {
		this.HIR.remove(file);
		forgetHir(file);
	}

	private void forgetHir(final Triplet<FileOnClient, Long, Long> file) {
		this.hirByName.remove(FileKey.of(file.getFirst().getFileName()));
		this.hirUsedCapacity -= file.getFirst().getFileSize();
	}

	@Override
	public long freeCapacity() {
		return this.capacity - (this.lirUsedCapacity + this.hirUsedCapacity);
	}

	@Override
	public void removeFile() {
		if (!this.HIR.isEmpty()) {
      forgetHir(this.HIR.remove(0));
		} else if (!this.LIR.isEmpty()) {
			this.LIR.sort(new TripletCompare());
      removeLastFromLir();
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
    addToHir(file);
    this.zasobnikSouboru.push(file);
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
		this.lirByName.clear();
		this.hirByName.clear();
		this.lirUsedCapacity = 0;
		this.hirUsedCapacity = 0;
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

	/**
	 * Stack of accesses where every entry is present at most once. Answers "how deep is this
	 * entry" in O(log n) instead of scanning: entries get increasing slots and a Fenwick tree
	 * counts the slots still present.
	 */
	static final class AccessStack<T> {
		private static final int INITIAL_SLOTS = 1024;

		private final IdentityHashMap<T, Integer> slots = new IdentityHashMap<>();
		private final BitSet present = new BitSet();
		private int[] tree = new int[INITIAL_SLOTS + 1];
		private int nextSlot;
		private int count;

		int size() {
			return this.count;
		}

		void push(final T item) {
			if (this.nextSlot + 1 >= this.tree.length) {
				grow();
			}
			this.slots.put(item, this.nextSlot);
			this.present.set(this.nextSlot);
			add(this.nextSlot, 1);
			this.nextSlot++;
			this.count++;
		}

		void remove(final T item) {
			final Integer slot = this.slots.remove(item);
			if (slot != null) {
				this.present.clear(slot);
				add(slot, -1);
				this.count--;
			}
		}

		/**
		 * @return position from the bottom of the stack, or -1 when not present
		 */
		int indexOf(final T item) {
			final Integer slot = this.slots.get(item);
			if (slot == null) {
				return -1;
			}
			int below = 0;
			for (int i = slot; i > 0; i -= i & -i) {
				below += this.tree[i];
			}
			return below;
		}

		void clear() {
			this.slots.clear();
			this.present.clear();
			this.tree = new int[INITIAL_SLOTS + 1];
			this.nextSlot = 0;
			this.count = 0;
		}

		private void add(final int slot, final int delta) {
			for (int i = slot + 1; i < this.tree.length; i += i & -i) {
				this.tree[i] += delta;
			}
		}

		private void grow() {
			final int[] bigger = new int[(this.tree.length - 1) * 2 + 1];
			for (int slot = this.present.nextSetBit(0); slot >= 0; slot = this.present.nextSetBit(slot + 1)) {
				bigger[slot + 1] = 1;
			}
			for (int i = 1; i < bigger.length; i++) {
				final int parent = i + (i & -i);
				if (parent < bigger.length) {
					bigger[parent] += bigger[i];
				}
			}
			this.tree = bigger;
		}
	}
}
