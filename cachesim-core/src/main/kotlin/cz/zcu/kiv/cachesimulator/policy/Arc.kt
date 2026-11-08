package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Adaptive replacement cache: T1 holds files seen once, T2 files seen again; B1 and B2 remember files evicted
 * from them and steer the target size [p] of T1.
 *
 * SOURCE: "ARC: A Self-Tuning, Low Overhead Replacement Cache", Nimrod Megiddo, Dharmendra Modha
 */
class Arc(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("ARC", context, capacity) {
    /** target size of T1 */
    private var p = 0.0
    private val t1 = CachedList()
    private val t2 = CachedList()
    private val b1 = GhostList()
    private val b2 = GhostList()

    override val freeCapacity: Long get() = capacity - (t1.size + t2.size)

    override fun get(fileName: String): CachedFile? {
        val key = fileKey(fileName)
        t1.remove(key)?.let { file ->
            t2.addLast(key, file)
            return file
        }
        return t2.moveToEnd(key)
    }

    /** used only to make room for a download window */
    override fun removeFile() {
        if (!t1.isEmpty() || !t2.isEmpty()) replace()
    }

    override fun insertFile(file: CachedFile) {
        if (file.size > capacity) {
            openDownloadWindow(file)
            return
        }
        closeFinishedDownloads()
        var seenBefore = false
        while (freeCapacity < file.size) {
            seenBefore = false
            // ghost lists hold the evicted file objects, so a newly fetched file is never found in them
            when {
                file in b1 -> {
                    p = minOf(capacity.toDouble(), p + maxOf(b2.size.toDouble() / b1.size, 1.0))
                    replace()
                    seenBefore = true
                }

                file in b2 -> {
                    p = maxOf(0.0, p - maxOf(b1.size.toDouble() / b2.size, 1.0))
                    replace()
                    seenBefore = true
                }

                t1.size + b1.size > capacity -> {
                    if (t1.size < capacity) {
                        b1.removeFirst()
                        replace()
                    } else {
                        t1.removeFirst()
                    }
                }

                t1.size + t2.size + b1.size + b2.size >= capacity -> {
                    if (t1.size + t2.size + b1.size + b2.size >= 2 * capacity && !b2.isEmpty()) {
                        b2.removeFirst()
                    }
                    replace()
                }

                !t1.isEmpty() -> {
                    b1.addLast(t1.removeFirst())
                }

                else -> {
                    b2.addLast(t2.removeFirst())
                }
            }
        }
        val key = fileKey(file.name)
        if (seenBefore) t2.addLast(key, file) else t1.addLast(key, file)
    }

    /** moves the oldest file of T1 or T2 to its ghost list */
    private fun replace() {
        if (!t1.isEmpty() && t1.size.toDouble() > p) {
            b1.addLast(t1.removeFirst())
        } else if (!t2.isEmpty()) {
            b2.addLast(t2.removeFirst())
        } else {
            b1.addLast(t1.removeFirst())
        }
    }

    override fun store(file: CachedFile) = error("ARC caches files in insertFile")

    override fun clear() {
        p = 0.0
        t1.clear()
        t2.clear()
        b1.clear()
        b2.clear()
    }

    /** files in the cache, oldest first, with their total size; a name is here at most once */
    private class CachedList {
        private val files = LinkedHashMap<String, CachedFile>()
        var size = 0L
            private set

        fun isEmpty() = files.isEmpty()

        fun addLast(
            key: String,
            file: CachedFile,
        ) {
            files.putLast(key, file)
            size += file.size
        }

        fun remove(key: String): CachedFile? = files.remove(key)?.also { size -= it.size }

        fun moveToEnd(key: String): CachedFile? = files[key]?.also { files.putLast(key, it) }

        fun removeFirst(): CachedFile {
            val first = files.pollFirstEntry() ?: throw IndexOutOfBoundsException("Index 0 out of bounds for length 0")
            size -= first.value.size
            return first.value
        }

        fun clear() {
            files.clear()
            size = 0
        }
    }

    /** files evicted from the cache, oldest first, with their total size; membership is by object identity */
    private class GhostList {
        private val files = ArrayDeque<CachedFile>()
        private val members: MutableSet<CachedFile> = Collections.newSetFromMap(IdentityHashMap())
        var size = 0L
            private set

        fun isEmpty() = files.isEmpty()

        operator fun contains(file: CachedFile) = file in members

        fun addLast(file: CachedFile) {
            files.addLast(file)
            members += file
            size += file.size
        }

        fun removeFirst() {
            val first = files.removeFirstOrNull() ?: throw IndexOutOfBoundsException("Index 0 out of bounds for length 0")
            members -= first
            size -= first.size
        }

        fun clear() {
            files.clear()
            members.clear()
            size = 0
        }
    }
}
