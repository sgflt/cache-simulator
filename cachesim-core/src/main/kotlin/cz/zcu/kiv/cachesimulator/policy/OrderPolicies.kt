package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import java.util.LinkedList
import java.util.Random

/**
 * Keeps files in one ordered map and evicts from one end of it.
 *
 * @param touchOnHit a hit moves the file to the newest end
 * @param evictNewest evict from the newest end instead of the oldest
 */
abstract class OrderedPolicy(
    name: String,
    context: SimulationContext,
    capacity: Long,
    private val touchOnHit: Boolean,
    private val evictNewest: Boolean,
) : CachePolicy(name, context, capacity) {
    private val files = LinkedHashMap<String, CachedFile>()

    override fun get(fileName: String): CachedFile? {
        val key = fileKey(fileName)
        val file = files[key]
        if (file != null && touchOnHit) files.putLast(key, file)
        return file
    }

    override fun removeFile() {
        val evicted = (if (evictNewest) files.pollLastEntry() else files.pollFirstEntry()) ?: throw NoSuchElementException()
        usedCapacity -= evicted.value.size
    }

    override fun store(file: CachedFile) {
        files[fileKey(file.name)] = file
        usedCapacity += file.size
    }

    override fun clear() = files.clear()
}

/**
 * Least recently used.
 *
 * SOURCE: "Analysis of caching algorithms for distributed file systems", B. Reed and D. D. E. Long
 */
class Lru(
    context: SimulationContext,
    capacity: Long,
) : OrderedPolicy("LRU", context, capacity, touchOnHit = true, evictNewest = false)

/**
 * Most recently used.
 *
 * SOURCE: "An evaluation of buffer management strategies for relational database systems", H.-T. Chou and D. J. DeWitt
 */
class Mru(
    context: SimulationContext,
    capacity: Long,
) : OrderedPolicy("MRU", context, capacity, touchOnHit = true, evictNewest = true)

/**
 * First in, first out.
 *
 * SOURCE: "An anomaly in space-time characteristics of certain programs running in a paging machine",
 * L. A. Belady, R. A. Nelson and G. S. Shedler
 */
class Fifo(
    context: SimulationContext,
    capacity: Long,
) : OrderedPolicy("FIFO", context, capacity, touchOnHit = false, evictNewest = false)

/** a cached file with its reference bit */
private class ReferencedFile(
    val file: CachedFile,
    var referenced: Boolean,
)

/**
 * FIFO that gives a file referenced since it was queued a second round.
 *
 * SOURCE: "Page Replacement and Reference Bit Emulation in Mach", R. P. Draves
 */
class FifoSecondChance(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("FiFO 2nd chance", context, capacity) {
    private val queue = LinkedList<ReferencedFile>()
    private val byName = HashMap<String, ReferencedFile>()

    override fun get(fileName: String): CachedFile? = byName[fileKey(fileName)]?.apply { referenced = true }?.file

    override fun removeFile() {
        var entry = queue.remove()
        while (entry.referenced) {
            entry.referenced = false
            queue.add(entry)
            entry = queue.remove()
        }
        byName.remove(fileKey(entry.file.name))
        usedCapacity -= entry.file.size
    }

    override fun store(file: CachedFile) {
        val entry = ReferencedFile(file, referenced = false)
        queue.add(entry)
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
    }

    override fun clear() {
        queue.clear()
        byName.clear()
    }
}

/**
 * Files on a clock face; the hand skips (and clears) referenced files.
 *
 * SOURCE: "Modern Operating Systems (Second Edition)", Andrew S. Tanenbaum
 */
class Clock(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("Clock", context, capacity) {
    private val files = ArrayList<ReferencedFile>()
    private val byName = HashMap<String, ReferencedFile>()

    /** where the next file goes */
    private var hand = 0

    override fun get(fileName: String): CachedFile? = byName[fileKey(fileName)]?.apply { referenced = true }?.file

    override fun removeFile() {
        hand %= files.size
        var entry = files[hand]
        while (entry.referenced) {
            entry.referenced = false
            hand = (hand + 1) % files.size
            entry = files[hand]
        }
        files.removeAt(hand)
        byName.remove(fileKey(entry.file.name))
        usedCapacity -= entry.file.size
    }

    override fun store(file: CachedFile) {
        val entry = ReferencedFile(file, referenced = true)
        files.add(hand, entry)
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
        hand++
    }

    override fun clear() {
        files.clear()
        byName.clear()
        hand = 0
    }
}

/**
 * Evicts a random file.
 *
 * SOURCE: "Analysis of caching algorithms for distributed file systems", B. Reed and D. D. E. Long
 */
class Rnd(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("RND", context, capacity) {
    private val files = ArrayList<CachedFile>()
    private val byName = HashMap<String, CachedFile>()
    private var random = Random(SEED)

    override fun get(fileName: String): CachedFile? = byName[fileKey(fileName)]

    override fun removeFile() {
        val evicted = files.removeAt(random.nextInt(files.size))
        byName.remove(fileKey(evicted.name))
        usedCapacity -= evicted.size
    }

    override fun store(file: CachedFile) {
        files += file
        byName[fileKey(file.name)] = file
        usedCapacity += file.size
    }

    override fun clear() {
        files.clear()
        byName.clear()
        random = Random(SEED)
    }

    private companion object {
        const val SEED = 0L
    }
}
