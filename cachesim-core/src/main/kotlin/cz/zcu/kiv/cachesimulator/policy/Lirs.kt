package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import java.util.BitSet
import java.util.IdentityHashMap

/**
 * Low inter-reference recency set: files re-requested soon (low IRR) stay in the LIR set, the others in the HIR
 * set, which is evicted first.
 *
 * SOURCE: "LIRS: An Efficient Low Inter-reference Recency Set Replacement Policy to Improve Buffer Cache
 * Performance", S. Jiang and X. Zhang
 */
class Lirs(
    context: SimulationContext,
    capacity: Long,
) : CachePolicy("LIRS", context, capacity) {
    /** compared by identity */
    private class Entry(
        val file: CachedFile,
        var lastAccess: Long,
        /** inter-reference recency */
        var irr: Long,
    )

    private val lirShare = context.policyParams.lirs.lirCapacity
    private val accessStack = AccessStack<Entry>()
    private val lir = ArrayList<Entry>()
    private val hir = ArrayList<Entry>()
    private val lirByName = HashMap<String, Entry>()
    private val hirByName = HashMap<String, Entry>()
    private var lirUsedCapacity = 0L
    private var hirUsedCapacity = 0L
    private var time = 1L

    override val freeCapacity: Long get() = capacity - (lirUsedCapacity + hirUsedCapacity)

    override fun get(fileName: String): CachedFile? {
        val key = fileKey(fileName)
        val now = ++time
        lirByName[key]?.let { entry ->
            touch(entry, now)
            return entry.file
        }
        val entry = hirByName[key] ?: return null
        val irr = touch(entry, now)
        if (!fitsIntoLir(entry)) {
            // files re-requested later than this one lose their LIR status
            lir.sortBy { it.irr }
            while (lir.isNotEmpty() && lir.last().irr > irr) addToHir(removeLastFromLir())
        }
        if (fitsIntoLir(entry)) {
            removeFromHir(entry)
            addToLir(entry)
        } else {
            hir.remove(entry)
            hir.add(entry)
        }
        return entry.file
    }

    /** records an access to [entry] on the access stack; @return its new IRR */
    private fun touch(
        entry: Entry,
        now: Long,
    ): Long {
        val irr = (accessStack.size - accessStack.indexOf(entry)).toLong()
        accessStack.remove(entry)
        entry.irr = irr
        entry.lastAccess = now
        accessStack.push(entry)
        return irr
    }

    private fun fitsIntoLir(entry: Entry) = (lirUsedCapacity + entry.file.size).toDouble() < capacity * lirShare

    private fun addToLir(entry: Entry) {
        lir += entry
        lirByName[fileKey(entry.file.name)] = entry
        lirUsedCapacity += entry.file.size
    }

    private fun removeLastFromLir(): Entry {
        val entry = lir.removeLast()
        lirByName.remove(fileKey(entry.file.name))
        lirUsedCapacity -= entry.file.size
        return entry
    }

    private fun addToHir(entry: Entry) {
        hir += entry
        hirByName[fileKey(entry.file.name)] = entry
        hirUsedCapacity += entry.file.size
    }

    private fun removeFromHir(entry: Entry) {
        hir.remove(entry)
        forgetHir(entry)
    }

    private fun forgetHir(entry: Entry) {
        hirByName.remove(fileKey(entry.file.name))
        hirUsedCapacity -= entry.file.size
    }

    override fun removeFile() {
        if (hir.isNotEmpty()) {
            forgetHir(hir.removeFirst())
        } else if (lir.isNotEmpty()) {
            lir.sortBy { it.irr }
            removeLastFromLir()
        }
    }

    override fun store(file: CachedFile) {
        val entry = Entry(file, ++time, Long.MAX_VALUE)
        addToHir(entry)
        accessStack.push(entry)
    }

    override fun clear() {
        accessStack.clear()
        lir.clear()
        hir.clear()
        lirByName.clear()
        hirByName.clear()
        lirUsedCapacity = 0
        hirUsedCapacity = 0
        time = 0
    }
}

/**
 * Stack of accesses where every item is present at most once. Answers "how deep is this item" in O(log n)
 * instead of scanning: items get increasing slots, and a Fenwick tree counts the slots still present.
 */
internal class AccessStack<T : Any> {
    private val slots = IdentityHashMap<T, Int>()
    private val present = BitSet()
    private var tree = IntArray(INITIAL_SLOTS + 1)
    private var nextSlot = 0

    var size = 0
        private set

    fun push(item: T) {
        if (nextSlot + 1 >= tree.size) grow()
        slots[item] = nextSlot
        present.set(nextSlot)
        add(nextSlot, 1)
        nextSlot++
        size++
    }

    fun remove(item: T) {
        val slot = slots.remove(item) ?: return
        present.clear(slot)
        add(slot, -1)
        size--
    }

    /** @return position from the bottom of the stack, or -1 when not present */
    fun indexOf(item: T): Int {
        var i = slots[item] ?: return -1
        var below = 0
        while (i > 0) {
            below += tree[i]
            i -= i and -i
        }
        return below
    }

    fun clear() {
        slots.clear()
        present.clear()
        tree = IntArray(INITIAL_SLOTS + 1)
        nextSlot = 0
        size = 0
    }

    private fun add(
        slot: Int,
        delta: Int,
    ) {
        var i = slot + 1
        while (i < tree.size) {
            tree[i] += delta
            i += i and -i
        }
    }

    private fun grow() {
        val bigger = IntArray((tree.size - 1) * 2 + 1)
        var slot = present.nextSetBit(0)
        while (slot >= 0) {
            bigger[slot + 1] = 1
            slot = present.nextSetBit(slot + 1)
        }
        for (i in 1 until bigger.size) {
            val parent = i + (i and -i)
            if (parent < bigger.size) bigger[parent] += bigger[i]
        }
        tree = bigger
    }

    private companion object {
        const val INITIAL_SLOTS = 1024
    }
}
