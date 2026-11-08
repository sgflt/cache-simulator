package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Set;


/**
 * class for ARC algorithm
 *
 * SOURCE: Adapted from article "ARC: A Self-Tuning, Low Overhead Replacement Cache",
 * by Nimrod Megiddo , Dharmendra Modha
 *
 * @author Pavel Bžoch
 *
 */
public class ARC implements ICache {

	/**
	 * argument pro adaptaci
	 */
	private double p = 0;

	/**
	 * listy pro uchovani souboru: T1, T2 drzi soubory v cache, B1, B2 jsou historie vyhozenych
	 */
	private final GhostList B1 = new GhostList();
	private final GhostList B2 = new GhostList();
	private final CachedList T1 = new CachedList();
	private final CachedList T2 = new CachedList();

	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;

	/**
	 * kapacita cache
	 */
	private long capacity;

	public ARC() {
		super();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		final String key = FileKey.of(fileName);
		final FileOnClient fromT1 = this.T1.remove(key);
		if (fromT1 != null) {
			this.T2.addLast(key, fromT1);
			return fromT1;
		}
		return this.T2.moveToEnd(key);
	}

	@Override
	public long freeCapacity() {
		return this.capacity - (this.T1.size + this.T2.size);
	}

	@Override
	public void removeFile() {
		// only used to make room for the over-capacity download window;
		// an empty body here made insertFile loop forever
		if (!this.T1.isEmpty() || !this.T2.isEmpty()) {
			replace(this.p, false);
		}
	}

	@Override
	//TODO
	public void insertFile(final FileOnClient f) {

		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		//TODO - upravit seznamy
		if (f.getFileSize() > this.capacity){
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
		int swi = 2;
		while (freeCapacity() < f.getFileSize()) {
			swi = 2;
			// soubor je v B1
			if (this.B1.contains(f)) {
				this.p = Math.min(this.capacity,
					this.p + Math.max((double) this.B2.size / this.B1.size, 1));
				replace(this.p, false);
				swi = 1;
				continue;
			}
			// soubor je v B2
			if (this.B2.contains(f)) {
				this.p = Math.max(0, this.p - Math.max((double) this.B1.size / this.B2.size, 1));
				replace(this.p, true);
				swi = 1;
				continue;
			}
			//soubor neni ani v T1, ani v T2, ani v B1, ani v B2
			if (this.T1.size + this.B1.size > this.capacity){
				if (this.T1.size < this.capacity){
					this.B1.removeFirst();
					replace(this.p, false);
				}
				else{
					this.T1.removeFirst();
				}
				continue;
			}
			else if (this.T1.size + this.T2.size + this.B1.size + this.B2.size >= this.capacity){
				if (this.T1.size + this.T2.size + this.B1.size + this.B2.size >=  2 * this.capacity){
					if (!this.B2.isEmpty()){
						this.B2.removeFirst();
					}
				}
				replace(this.p, false);
				continue;
			}
			else {
				if (!this.T1.isEmpty()){
					this.B1.addLast(this.T1.removeFirst());
					continue;
				}
				this.B2.addLast(this.T2.removeFirst());
			}
		}
		final String key = FileKey.of(f.getFileName());
		if (swi == 1) {
			this.T2.addLast(key, f);
		} else {
			this.T1.addLast(key, f);
		}
	}

	/**
	 * metoda replace
	 *
	 * @param p
	 */
	private void replace(final double p, final boolean inB2) {
		if (!this.T1.isEmpty()
				&& (this.T1.size > p || (inB2 && this.T1.size > p))) {
			this.B1.addLast(this.T1.removeFirst());
		} else if (!this.T2.isEmpty()) {
			this.B2.addLast(this.T2.removeFirst());
		} else {
			this.B1.addLast(this.T1.removeFirst());
		}
	}

	@Override
	public String toString(){
		return "ARC";
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
		this.p = 0;
		this.B1.clear();
		this.B2.clear();
		this.T1.clear();
		this.T2.clear();
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
		return "ARC;ARC";
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
	 * files in the cache (T1 or T2), oldest first, with their total size;
	 * a name is in the cache at most once, so files are looked up by name key
	 */
	private static final class CachedList {
		private final LinkedHashMap<String, FileOnClient> files = new LinkedHashMap<>();
		private long size;

		boolean isEmpty() {
			return this.files.isEmpty();
		}

		void addLast(final String key, final FileOnClient file) {
			this.files.putLast(key, file);
			this.size += file.getFileSize();
		}

		FileOnClient remove(final String key) {
			final FileOnClient file = this.files.remove(key);
			if (file != null) {
				this.size -= file.getFileSize();
			}
			return file;
		}

		FileOnClient moveToEnd(final String key) {
			final FileOnClient file = this.files.get(key);
			if (file != null) {
				this.files.putLast(key, file);
			}
			return file;
		}

		FileOnClient removeFirst() {
			final var first = this.files.pollFirstEntry();
			if (first == null) {
				throw new IndexOutOfBoundsException("Index 0 out of bounds for length 0");
			}
			this.size -= first.getValue().getFileSize();
			return first.getValue();
		}

		void clear() {
			this.files.clear();
			this.size = 0;
		}
	}

	/**
	 * history of files evicted from the cache (B1 or B2), oldest first, with their total size;
	 * the same name can be here more than once, so membership is by object identity
	 */
	private static final class GhostList {
		private final ArrayDeque<FileOnClient> files = new ArrayDeque<>();
		private final Set<FileOnClient> members = Collections.newSetFromMap(new IdentityHashMap<>());
		private long size;

		boolean isEmpty() {
			return this.files.isEmpty();
		}

		boolean contains(final FileOnClient file) {
			return this.members.contains(file);
		}

		void addLast(final FileOnClient file) {
			this.files.addLast(file);
			this.members.add(file);
			this.size += file.getFileSize();
		}

		void removeFirst() {
			final FileOnClient first = this.files.pollFirst();
			if (first == null) {
				throw new IndexOutOfBoundsException("Index 0 out of bounds for length 0");
			}
			this.members.remove(first);
			this.size -= first.getFileSize();
		}

		void clear() {
			this.files.clear();
			this.members.clear();
			this.size = 0;
		}
	}
}
