package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import kotlin.math.pow

/**
 * Base of the LFU policies: evicts the file with the fewest hits.
 */
abstract class LfuPolicy(
    name: String,
    context: SimulationContext,
    capacity: Long,
) : CachePolicy(name, context, capacity) {
    protected class Entry(
        val file: CachedFile,
        var hits: Double,
    )

    /** cached files, sorted by hits only right before an eviction */
    protected val entries = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()
    private var needsSort = true

    override fun get(fileName: String): CachedFile? {
        val entry = byName[fileKey(fileName)] ?: return null
        entry.hits++
        needsSort = true
        return entry.file
    }

    override fun removeFile() {
        if (needsSort) entries.sortBy { it.hits }
        needsSort = false
        if (entries.isNotEmpty()) {
            val evicted = entries.removeFirst()
            byName.remove(fileKey(evicted.file.name))
            usedCapacity -= evicted.file.size
        }
        afterEviction()
    }

    override fun store(file: CachedFile) = addEntry(file, 1.0)

    protected fun addEntry(
        file: CachedFile,
        hits: Double,
    ) {
        val entry = Entry(file, hits)
        entries += entry
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
        needsSort = true
    }

    /** forces sorting before the next eviction */
    protected fun markUnsorted() {
        needsSort = true
    }

    protected open fun afterEviction() {}

    /** halves all hits once the most frequent file has more than [AGING_THRESHOLD] hits */
    protected fun ageWhenHot(minEntries: Int) {
        if (entries.size > minEntries && entries.last().hits > AGING_THRESHOLD) {
            entries.forEach { it.hits /= 2 }
        }
    }

    override fun clear() {
        entries.clear()
        byName.clear()
        needsSort = true
    }

    private companion object {
        const val AGING_THRESHOLD = 15
    }
}

/**
 * LFU without aging: hit counts only grow.
 *
 * SOURCE: "Analysis of caching algorithms for distributed file systems", B. Reed and D. D. E. Long
 */
class LfuNoReduction(
    context: SimulationContext,
    capacity: Long,
) : LfuPolicy("Standard LFU", context, capacity)

/**
 * LFU with aging: hit counts are halved when they grow too big.
 *
 * SOURCE: "Analysis of caching algorithms for distributed file systems", B. Reed and D. D. E. Long
 */
class LfuReduction(
    context: SimulationContext,
    capacity: Long,
) : LfuPolicy("LFU Reduction", context, capacity) {
    override fun afterEviction() = ageWhenHot(minEntries = 1)
}

/**
 * Base of the least reference density policies: evicts the file with the fewest hits per request since it
 * was cached.
 *
 * SOURCE: "Principles of database buffer management", W. Effelsberg and T. Haerder
 */
abstract class LrdPolicy(
    name: String,
    context: SimulationContext,
    capacity: Long,
) : CachePolicy(name, context, capacity) {
    protected class Entry(
        val file: CachedFile,
        /** the global counter when the file was cached */
        val since: Long,
    ) {
        var hits = 0L
        var referenceDensity = 1.0
    }

    protected val entries = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()

    /** counts requests */
    protected var globalCounter = 0L
        private set
    private var needsRecalculation = true

    override fun get(fileName: String): CachedFile? {
        val entry = byName[fileKey(fileName)] ?: return null
        entry.hits++
        globalCounter++
        needsRecalculation = true
        afterRequest()
        return entry.file
    }

    override fun removeFile() {
        if (needsRecalculation) {
            entries.forEach { it.referenceDensity = it.hits / (globalCounter.toDouble() - it.since) }
            entries.sortBy { it.referenceDensity }
        }
        needsRecalculation = false
        val evicted = entries.removeFirst()
        byName.remove(fileKey(evicted.file.name))
        usedCapacity -= evicted.file.size
    }

    override fun store(file: CachedFile) {
        val entry = Entry(file, globalCounter++)
        entries += entry
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
        needsRecalculation = true
        afterRequest()
    }

    protected open fun afterRequest() {}

    override fun clear() {
        entries.clear()
        byName.clear()
        globalCounter = 0
        needsRecalculation = true
    }
}

/** LRD version 1: reference counts only grow. */
class LrdV1(
    context: SimulationContext,
    capacity: Long,
) : LrdPolicy("LRDv1", context, capacity)

/** LRD version 2: reference counts are divided at regular intervals, so old hits count less. */
class LrdV2(
    context: SimulationContext,
    capacity: Long,
) : LrdPolicy("LRDv2", context, capacity) {
    private val params = context.policyParams.lrdV2

    override fun afterRequest() {
        if (globalCounter % params.interval == 0L) {
            entries.forEach { it.hits = (it.hits / params.divisor).toLong() }
        }
    }
}

/**
 * Least recently/frequently used: a priority combining both, decaying with time.
 *
 * SOURCE: "LRFU: a spectrum of policies that subsumes the least recently used and least frequently used
 * policies", D. Lee, J. Choi, J.-H. Kim, S. Noh, S. L. Min, Y. Cho and C. S. Kim
 */
class Lrfu(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("LRFU", context, capacity) {
    private class Entry(
        val file: CachedFile,
        var lastAccess: Long,
        var priority: Double,
    )

    private val params = context.policyParams.lrfu
    private val entries = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()
    private var time = 1L
    private var needsSort = true

    override fun get(fileName: String): CachedFile? {
        val entry = byName[fileKey(fileName)] ?: return null
        val now = ++time
        entry.priority = weight(0) + entry.priority * weight(now - entry.lastAccess)
        entry.lastAccess = now
        needsSort = true
        return entry.file
    }

    override fun removeFile() {
        if (needsSort) entries.sortBy { it.priority }
        needsSort = false
        if (entries.isNotEmpty()) {
            val evicted = entries.removeFirst()
            byName.remove(fileKey(evicted.file.name))
            usedCapacity -= evicted.file.size
        }
    }

    override fun makeRoom(bytes: Long) {
        needsSort = true
        super.makeRoom(bytes)
    }

    override fun store(file: CachedFile) {
        val entry = Entry(file, ++time, weight(0))
        entries += entry
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
    }

    /** weight of an access [age] time units ago */
    private fun weight(age: Long): Double = (1.0 / params.p).pow(params.lambda * age)

    override fun clear() {
        entries.clear()
        byName.clear()
        time = 1
        needsSort = true
    }
}

/**
 * Frequency-based replacement: LRU order, split into new, middle and old sections. Hits in the new section do
 * not count; the least frequently used file of the old section is evicted.
 *
 * SOURCE: "Towards building a fault tolerant and conflict-free distributed file system for mobile clients",
 * A. Boukerche and R. Al-Shaikh
 */
class Fbr(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("FBR", context, capacity) {
    private class Entry(
        val file: CachedFile,
    ) {
        var hits = 1
    }

    private val params = context.policyParams.fbr

    /** least recently used first */
    private val entries = ArrayList<Entry>()
    private val names = HashSet<String>()

    override fun get(fileName: String): CachedFile? {
        if (fileName !in names) return null
        val index = entries.indexOfFirst { it.file.name.equals(fileName, ignoreCase = true) }
        if (index < 0) return null
        val entry = entries[index]
        // hits count outside the new section; with no new section boundary, everything but the head counts
        val newSectionEnd = maxOf(indexWhereCumulativeSizeExceeds(params.newSection), 0)
        if (index > newSectionEnd) entry.hits++
        entries.removeAt(index)
        entries += entry
        return entry.file
    }

    override fun removeFile() {
        if (entries.isEmpty()) return
        val oldStart = indexWhereCumulativeSizeExceeds(1 - params.oldSection)
        val evicted =
            if (oldStart < 0) {
                entries.removeLast()
            } else {
                entries.subList(oldStart, entries.size).minBy { it.hits }.also { entries.remove(it) }
            }
        usedCapacity -= evicted.file.size
        names.remove(evicted.file.name)
    }

    /** @return index of the first file where the sizes so far exceed [share] of the capacity, or -1 */
    private fun indexWhereCumulativeSizeExceeds(share: Double): Int {
        var sum = 0L
        return entries.indexOfFirst { entry ->
            sum += entry.file.size
            sum > share * capacity
        }
    }

    override fun store(file: CachedFile) {
        entries += Entry(file)
        names += file.name
        usedCapacity += file.size
    }

    override fun clear() {
        entries.clear()
        names.clear()
    }
}
