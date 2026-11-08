package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext

/** how many requests pass between two updates of the server-wide read count */
private const val SERVER_POLL_INTERVAL = 20L

/**
 * LFU with server statistics: a newly cached file starts with as many hits as its share of the server's
 * read requests suggests, instead of starting from scratch.
 *
 * @param ignoreLargeFiles do not cache files bigger than half of the cache (the "X" variants)
 */
open class LfuSs(
    context: SimulationContext,
    capacity: Long,
    name: String = "LFU-SS",
    private val ignoreLargeFiles: Boolean = false,
) : LfuPolicy(name, context, capacity) {
    /** read requests of this cache over all files, as last seen on the server */
    protected var globalReadCount = Long.MAX_VALUE
        private set
    private var requestCount = 0L

    override val needsServerStatistics: Boolean get() = true

    override fun get(fileName: String): CachedFile? {
        if (++requestCount % SERVER_POLL_INTERVAL == 0L) {
            globalReadCount = context.server.globalReadRequests(statsKey)
        }
        return super.get(fileName)
    }

    final override fun insertFile(file: CachedFile) {
        if (ignoreLargeFiles && file.size > configuredCapacity / 2) return
        admit(file)
    }

    /** caches a file that is not too large */
    protected open fun admit(file: CachedFile) = super.insertFile(file)

    override fun store(file: CachedFile) {
        val localHits = entries.sumOf { it.hits }
        addEntry(file, file.serverShare() * localHits + 1.0)
    }

    /** the file's read requests, minus its writes, relative to all reads of this cache on the server */
    protected fun CachedFile.serverShare(): Double = (readRequests.toDouble() - writeRequests.toDouble()) / globalReadCount.toDouble()

    override fun afterEviction() = ageWhenHot(minEntries = 2)

    override fun clear() {
        super.clear()
        globalReadCount = Long.MAX_VALUE
        requestCount = 0
    }
}

/**
 * LFU-SS variant that adds each file's server share to its hits before every eviction, and starts new files
 * at one hit. It never opens a download window: files bigger than the cache are not cached at all.
 */
class QlfuSs(
    context: SimulationContext,
    capacity: Long,
    name: String = "QLFU-SS",
    ignoreLargeFiles: Boolean = false,
) : LfuSs(context, capacity, name, ignoreLargeFiles) {
    override fun admit(file: CachedFile) {
        if (file.size > configuredCapacity) return
        makeRoom(file.size)
        addEntry(file, 1.0)
    }

    override fun removeFile() {
        val localHits = entries.sumOf { it.hits }
        entries.forEach { it.hits += it.file.serverShare() * localHits }
        super.removeFile()
    }
}

/**
 * LFU-SS variant that re-weights all hits by the files' sizes before every eviction.
 *
 * @param weight new hits from the current hits and the file size relative to the biggest cached file
 */
class WlfuSs(
    context: SimulationContext,
    capacity: Long,
    name: String,
    ignoreLargeFiles: Boolean = false,
    private val weight: (hits: Double, relativeSize: Double) -> Double,
) : LfuSs(context, capacity, name, ignoreLargeFiles) {
    override fun removeFile() {
        val biggest = entries.maxOfOrNull { it.file.size }?.toDouble() ?: 0.0
        entries.forEach { it.hits = weight(it.hits, it.file.size / biggest) }
        markUnsorted()
        super.removeFile()
    }
}

/**
 * LRFU with server statistics: the priority combines recency and LFU-SS style frequency.
 *
 * Recency is measured with [System.nanoTime], so results differ slightly from run to run.
 */
class LrfuSs(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("LRFU-SS", context, capacity) {
    private class Entry(
        val file: CachedFile,
        var hits: Double,
    ) {
        var lastAccess = System.nanoTime()
        var priority = 0.0
    }

    private val params = context.policyParams.lrfuSs
    private val entries = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()
    private var needsSort = true
    private var needsRecalculation = true
    private var globalReadCount = Long.MAX_VALUE
    private var requestCount = 0L

    override val needsServerStatistics: Boolean get() = true

    override fun get(fileName: String): CachedFile? {
        if (++requestCount % SERVER_POLL_INTERVAL == 0L) {
            globalReadCount = context.server.globalReadRequests(statsKey)
        }
        val entry = byName[fileKey(fileName)] ?: return null
        entry.lastAccess = System.nanoTime()
        entry.hits++
        needsSort = true
        needsRecalculation = true
        return entry.file
    }

    override fun removeFile() {
        if (needsRecalculation) recalculatePriorities()
        if (needsSort) entries.sortBy { it.priority }
        needsSort = false
        needsRecalculation = false
        if (entries.isNotEmpty()) {
            val evicted = entries.removeFirst()
            byName.remove(fileKey(evicted.file.name))
            usedCapacity -= evicted.file.size
        }
    }

    /** scales recency and hits to 0..65535 among the cached files and combines them */
    private fun recalculatePriorities() {
        if (entries.size <= 1) return
        var oldest = entries[0].lastAccess
        var newest = oldest
        var maxHits = entries[0].hits
        var minHits = maxHits
        for (entry in entries) {
            if (entry.lastAccess > newest) {
                newest = entry.lastAccess
            } else if (entry.lastAccess < oldest) {
                oldest = entry.lastAccess
            }
            if (maxHits < entry.hits) {
                maxHits = entry.hits
            } else if (minHits > entry.hits) {
                minHits = entry.hits
            }
        }
        for (entry in entries) {
            val frequency = ((entry.hits - minHits) * SCALE / (maxHits - minHits)).toInt()
            val recency = ((entry.lastAccess - oldest) * SCALE / (newest - oldest)).toInt()
            entry.priority = (params.k1 * recency + params.k2 * frequency).toInt().toDouble()
        }
    }

    override fun store(file: CachedFile) {
        val localHits = entries.sumOf { it.hits }
        val hits =
            if (globalReadCount > 0) {
                (file.readRequests.toDouble() - file.writeRequests.toDouble()) / globalReadCount.toDouble() * localHits + 1
            } else {
                0.0
            }
        val entry = Entry(file, hits)
        entries += entry
        byName[fileKey(file.name)] = entry
        needsSort = true
        needsRecalculation = true
        usedCapacity += file.size
    }

    override fun clear() {
        entries.clear()
        byName.clear()
        needsSort = true
        needsRecalculation = true
        requestCount = 0
        globalReadCount = Long.MAX_VALUE
    }

    private companion object {
        const val SCALE = 65535.0
    }
}
