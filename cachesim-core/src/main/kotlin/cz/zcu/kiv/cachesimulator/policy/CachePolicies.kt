package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext

/**
 * A policy that can be simulated.
 *
 * @property id stable identifier, used on the command line and in saved selections
 * @property label name shown to people
 */
class PolicyDescriptor internal constructor(
    val id: String,
    val label: String,
    private val factory: (SimulationContext, Long) -> CachePolicy,
) {
    /** a new cache of [capacityBytes] bytes */
    fun create(
        context: SimulationContext,
        capacityBytes: Long,
    ): CachePolicy = factory(context, capacityBytes)

    override fun toString() = label
}

/** All policies the simulator knows. */
object CachePolicies {
    val ARC = PolicyDescriptor("ARC", "ARC", ::Arc)
    val CLOCK = PolicyDescriptor("Clock", "Clock", ::Clock)
    val FBR = PolicyDescriptor("FBR", "FBR", ::Fbr)
    val FIFO = PolicyDescriptor("FIFO", "FIFO", ::Fifo)
    val FIFO_SECOND_CHANCE = PolicyDescriptor("FIFO_2ndChance", "FIFO 2nd chance", ::FifoSecondChance)
    val LFU_NO_REDUCTION = PolicyDescriptor("LFU_NO_REDUCTION", "Standard LFU", ::LfuNoReduction)
    val LFU_REDUCTION = PolicyDescriptor("LFU_REDUCTION", "LFU with reduction", ::LfuReduction)
    val LFU_SS = PolicyDescriptor("LFU_SS", "LFU-SS") { context, capacity -> LfuSs(context, capacity) }
    val LIRS = PolicyDescriptor("LIRS", "LIRS", ::Lirs)
    val LRD_V1 = PolicyDescriptor("LRDv1", "LRD version 1", ::LrdV1)
    val LRD_V2 = PolicyDescriptor("LRDv2", "LRD version 2", ::LrdV2)
    val LRFU = PolicyDescriptor("LRFU", "LRFU", ::Lrfu)
    val LRFU_SS = PolicyDescriptor("LRFU_SS", "LRFU-SS", ::LrfuSs)
    val LRU = PolicyDescriptor("LRU", "LRU", ::Lru)
    val LRU_K = PolicyDescriptor("LRU_K", "LRU-K", ::LruK)
    val MQ = PolicyDescriptor("MQ", "MQ", ::Mq)
    val MRU = PolicyDescriptor("MRU", "MRU", ::Mru)
    val RND = PolicyDescriptor("RND", "Random", ::Rnd)
    val TWO_QUEUE = PolicyDescriptor("_2Q", "2 Queues", ::TwoQueue)

    private val qlfuSsVariants = listOf("QLFU_SS" to false, "QLFU_SSX" to true).map { (id, x) -> qlfuSs(id, x) }

    /** WLFU-SS variants: how each re-weights hits by relative file size, and which have an "X" variant */
    private val wlfuSsVariants =
        listOf<Triple<String, (Double, Double) -> Double, Boolean>>(
            Triple("", { h, r -> h * r }, true),
            Triple("2", { h, r -> h + h * r }, true),
            Triple("3", { h, r -> h + r }, false),
            Triple("4", { h, r -> h - r }, false),
            Triple("5", { h, r -> h - h * r }, false),
            Triple("6", { h, r -> h - h * r }, true),
            Triple("7", { h, r -> h + 0.23 * r }, false),
            Triple("8", { h, r -> h + 1 - h * r }, true),
            Triple("9", { h, r -> h - 1 + h * r }, false),
            Triple("10", { h, r -> h + 1 - r }, true),
        ).flatMap { (suffix, weight, hasX) ->
            listOfNotNull(wlfuSs(suffix, weight, false), wlfuSs(suffix, weight, true).takeIf { hasX })
        }

    /** sorted by [PolicyDescriptor.id], as earlier versions listed them */
    val all: List<PolicyDescriptor> =
        (
            listOf(
                ARC,
                CLOCK,
                FBR,
                FIFO,
                FIFO_SECOND_CHANCE,
                LFU_NO_REDUCTION,
                LFU_REDUCTION,
                LFU_SS,
                LIRS,
                LRD_V1,
                LRD_V2,
                LRFU,
                LRFU_SS,
                LRU,
                LRU_K,
                MQ,
                MRU,
                RND,
                TWO_QUEUE,
            ) + qlfuSsVariants + wlfuSsVariants
        ).sortedBy { "${it.id};" }

    private val byId = all.associateBy { it.id }

    /** @return the policy with this [PolicyDescriptor.id], or null */
    operator fun get(id: String): PolicyDescriptor? = byId[id]

    /** @throws IllegalArgumentException for an unknown id */
    fun require(id: String): PolicyDescriptor = requireNotNull(byId[id]) { "Unknown cache policy '$id'" }

    private fun qlfuSs(
        id: String,
        ignoreLargeFiles: Boolean,
    ): PolicyDescriptor {
        val name = id.replace('_', '-')
        return PolicyDescriptor(id, name) { context, capacity -> QlfuSs(context, capacity, name, ignoreLargeFiles) }
    }

    private fun wlfuSs(
        suffix: String,
        weight: (Double, Double) -> Double,
        ignoreLargeFiles: Boolean,
    ): PolicyDescriptor {
        val x = if (ignoreLargeFiles) "X" else ""
        val name = "WLFU-SS$suffix$x"
        return PolicyDescriptor("WLFU_SS$suffix$x", name) { context, capacity ->
            WlfuSs(context, capacity, name, ignoreLargeFiles, weight)
        }
    }
}
