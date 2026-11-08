package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.policy.CachePolicy

/** One simulated cache and what it achieved so far. */
class Measurement(
    val cache: CachePolicy,
) {
    var hits = 0L
        private set

    /** bytes that did not have to be downloaded thanks to cache hits */
    var savedBytes = 0L
        private set

    fun recordHit(fileSize: Long) {
        hits++
        savedBytes += fileSize
    }
}

/** A user with their own set of caches: every configured policy in every configured size. */
class SimulatedUser(
    val id: Long,
    private val config: SimulationConfig,
    context: SimulationContext,
) {
    val measurements: List<Measurement> =
        config.policies.flatMap { policy ->
            config.cacheSizesMb.map { sizeMb -> Measurement(policy.create(context, sizeMb * 1024L * 1024L)) }
        }

    var requests = 0L
        private set

    /** bytes the user would download without a cache */
    var requestedBytes = 0L
        private set

    fun recordRequest(fileSize: Long) {
        requests++
        requestedBytes += fileSize
    }

    /** @param minRequests users with fewer requests get no per-cache results */
    fun statistics(minRequests: Int): UserStatistics =
        UserStatistics(
            userId = id,
            requests = requests,
            requestedBytes = requestedBytes,
            cacheSizesMb = config.cacheSizesMb,
            results = if (requests < minRequests) emptyList() else measurements.map(::result),
        )

    private fun result(measurement: Measurement) =
        CacheResult(
            policyName = measurement.cache.name,
            hits = measurement.hits,
            savedBytes = measurement.savedBytes,
            hitRatio = percent(measurement.hits, requests),
            savedBytesRatio = percent(measurement.savedBytes, requestedBytes),
        )

    /** percentage truncated to 3 decimal places */
    private fun percent(
        part: Long,
        whole: Long,
    ): Double = (part * 100_000 / whole).toInt() / 1000.0
}
