package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import java.util.LinkedList
import kotlin.math.log10

/**
 * Multi-queue: LRU queues for increasing hit counts; files not accessed for a while drop a queue lower.
 * Recently evicted files keep their hit count in the out queue.
 *
 * SOURCE: "The Multi-Queue Replacement Algorithm for Second Level Buffer Caches", Y. Zhou, J. F. Philbin and K. Li
 */
class Mq(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("MQ", context, capacity) {
    /** compared by identity */
    private class Entry(
        val file: CachedFile,
        var expiresAt: Long,
    ) {
        var hits = 1
    }

    private val params = context.policyParams.mq
    private val queues = List(params.queueCount) { LinkedList<Entry>() }
    private val outQueue = LinkedList<Entry>()

    /** the queue each file is in, by exact name */
    private val queueOf = HashMap<String, LinkedList<Entry>>()
    private var time = 0L

    override fun get(fileName: String): CachedFile? {
        val queue = queueOf[fileName] ?: return null
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.file.name.equals(fileName, ignoreCase = true)) {
                iterator.remove()
                entry.hits++
                entry.expiresAt = ++time + params.lifeTime
                enqueue(entry)
                adjust()
                return entry.file
            }
        }
        return null
    }

    /** puts [entry] at the end of the queue for its hit count */
    private fun enqueue(entry: Entry) {
        val index = minOf((log10(entry.hits.toDouble()) / log10(2.0)).toInt(), params.queueCount - 1)
        queues[index] += entry
        queueOf[entry.file.name] = queues[index]
    }

    /** moves expired files one queue down, one at a time */
    private fun adjust() {
        var i = 1
        while (i < queues.size) {
            val expired = queues[i].firstOrNull { it.expiresAt < time }
            if (expired == null) {
                i++
            } else {
                queues[i].remove(expired)
                queues[i - 1] += expired
                queueOf[expired.file.name] = queues[i - 1]
            }
        }
    }

    override fun removeFile() {
        val queue = queues.firstOrNull { it.isNotEmpty() } ?: return
        val evicted = queue.remove()
        queueOf.remove(evicted.file.name)
        if (outQueue.size > params.outQueueCapacity) outQueue.remove()
        outQueue += evicted
        usedCapacity -= evicted.file.size
    }

    override fun store(file: CachedFile) {
        // a file from the out queue comes back with its hit count
        val entry =
            outQueue.firstOrNull { it.file.name.equals(file.name, ignoreCase = true) }?.also { outQueue.remove(it) }
                ?: Entry(file, ++time + params.lifeTime)
        enqueue(entry)
        usedCapacity += entry.file.size
        adjust()
    }

    override fun clear() {
        queues.forEach { it.clear() }
        outQueue.clear()
        queueOf.clear()
        time = 0
    }
}

/**
 * LRU-K: evicts the file whose K-th most recent access is the oldest, ignoring correlated references.
 *
 * SOURCE: "The LRU-K page replacement algorithm for database disk buffering", E. J. O'Neil, P. E. O'Neil and G. Weikum
 */
class LruK(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("LRU-K", context, capacity) {
    /** compared by identity */
    private class Entry(
        val file: CachedFile,
        /** the last K access times, most recent first */
        val history: LongArray,
        var lastAccess: Long,
    )

    private val params = context.policyParams.lruK
    private val entries = ArrayList<Entry>()
    private val byName = HashMap<String, Entry>()
    private var time = 0L

    override fun get(fileName: String): CachedFile? {
        val entry = byName[fileKey(fileName)] ?: return null
        val now = ++time
        if (now - entry.lastAccess > params.correlatedReferencePeriod) {
            val correlationPeriod = entry.lastAccess - entry.history[0]
            for (i in 1 until params.k) {
                entry.history[i] = entry.history[i - 1] + correlationPeriod
            }
            entry.history[0] = now
        }
        entry.lastAccess = now
        return entry.file
    }

    override fun removeFile() {
        val now = ++time
        var oldest = now
        var victim: Entry? = null
        for (entry in entries) {
            val kthAccess = entry.history[params.k - 1]
            if (now - entry.lastAccess > params.correlatedReferencePeriod && kthAccess < oldest) {
                victim = entry
                oldest = kthAccess
            }
        }
        if (victim != null) {
            entries.remove(victim)
            byName.remove(fileKey(victim.file.name))
            usedCapacity -= victim.file.size
        }
    }

    override fun store(file: CachedFile) {
        val now = ++time
        val history = LongArray(params.k).also { it[0] = now }
        val entry = Entry(file, history, now)
        entries += entry
        byName[fileKey(file.name)] = entry
        usedCapacity += file.size
    }

    override fun clear() {
        entries.clear()
        byName.clear()
        time = 0
    }
}

/**
 * 2Q: files requested once wait in a FIFO queue; files requested again move to an LRU queue.
 *
 * SOURCE: "2Q: A Low Overhead High Performance Buffer Management Replacement Algorithm",
 * Theodore Johnson, Dennis Shasha
 */
class TwoQueue(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("2Q", context, capacity) {
    private val fifoShare = context.policyParams.twoQueue.fifoCapacity
    private val fifo = LinkedHashMap<String, CachedFile>()
    private var fifoUsedCapacity = 0L
    private val lru = LinkedHashMap<String, CachedFile>()

    override fun get(fileName: String): CachedFile? {
        val key = fileKey(fileName)
        fifo.remove(key)?.let { file ->
            fifoUsedCapacity -= file.size
            lru.putLast(key, file)
            return file
        }
        return lru[key]?.also { lru.putLast(key, it) }
    }

    override fun removeFile() {
        when {
            fifo.isNotEmpty() -> evictFromFifo()
            lru.isNotEmpty() -> usedCapacity -= lru.pollFirstEntry().value.size
        }
    }

    private fun evictFromFifo() {
        val evicted = checkNotNull(fifo.pollFirstEntry()) { "FIFO queue is empty" }.value
        fifoUsedCapacity -= evicted.size
        usedCapacity -= evicted.size
    }

    override fun store(file: CachedFile) {
        while (fifoUsedCapacity > (fifoShare * capacity.toDouble()).toInt()) evictFromFifo()
        fifo[fileKey(file.name)] = file
        fifoUsedCapacity += file.size
        usedCapacity += file.size
    }

    override fun clear() {
        fifo.clear()
        lru.clear()
        fifoUsedCapacity = 0
    }
}
