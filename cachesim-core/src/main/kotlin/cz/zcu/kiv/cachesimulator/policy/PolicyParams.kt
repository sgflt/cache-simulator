package cz.zcu.kiv.cachesimulator.policy

/** Tuning parameters of the policies that have any. */
data class PolicyParams(
    val fbr: FbrParams = FbrParams(),
    val lirs: LirsParams = LirsParams(),
    val lrdV2: LrdV2Params = LrdV2Params(),
    val lrfu: LrfuParams = LrfuParams(),
    val lrfuSs: LrfuSsParams = LrfuSsParams(),
    val lruK: LruKParams = LruKParams(),
    val mq: MqParams = MqParams(),
    val twoQueue: TwoQueueParams = TwoQueueParams(),
)

// Several defaults are float literals widened to double (0.045f is 0.04500000178813934), as the
// policies always had them; written as doubles they would change the results.

/**
 * @property oldSection share of the cache, from the least recently used end, evicted by frequency
 * @property newSection share of the cache, from the most recently used end, where hits are not counted
 */
data class FbrParams(
    val oldSection: Double = 0.3,
    val newSection: Double = 0.6,
)

/** @property lirCapacity share of the cache for files with low inter-reference recency */
data class LirsParams(
    val lirCapacity: Double = 0.9,
)

/**
 * @property divisor reference counts are divided by this...
 * @property interval ...every this many requests
 */
data class LrdV2Params(
    val divisor: Double = 1.8f.toDouble(),
    val interval: Int = 20,
)

data class LrfuParams(
    val p: Double = 2.0f.toDouble(),
    val lambda: Double = 0.045f.toDouble(),
)

/** priority = [k1] × recency + [k2] × frequency */
data class LrfuSsParams(
    val k1: Double = 0.35f.toDouble(),
    val k2: Double = 1.1f.toDouble(),
)

/**
 * @property k how many past accesses are remembered
 * @property correlatedReferencePeriod accesses closer together than this count as one
 */
data class LruKParams(
    val k: Int = 3,
    val correlatedReferencePeriod: Int = 7,
)

data class MqParams(
    val queueCount: Int = 5,
    val lifeTime: Int = 100,
    val outQueueCapacity: Int = 10,
)

/** @property fifoCapacity share of the cache for files requested only once */
data class TwoQueueParams(
    val fifoCapacity: Double = 0.50f.toDouble(),
)
