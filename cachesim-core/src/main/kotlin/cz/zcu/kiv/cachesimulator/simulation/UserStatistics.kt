package cz.zcu.kiv.cachesimulator.simulation

private const val MB = 1024 * 1024

/** What one cache achieved. Ratios are in % . */
data class CacheResult(
    val policyName: String,
    val hits: Long,
    val savedBytes: Long,
    val hitRatio: Double,
    val savedBytesRatio: Double,
)

/**
 * Results of one user: for every policy one value per cache size, in the order of [cacheSizesMb].
 *
 * @property requests how many files the user requested
 * @property requestedBytes bytes the user would download without a cache
 */
class UserStatistics(
    val userId: Long,
    val requests: Long,
    val requestedBytes: Long,
    val cacheSizesMb: List<Int>,
    val results: List<CacheResult>,
) {
    /** names of the simulated policies, in simulation order */
    val policyNames: List<String> get() = results.map { it.policyName }.distinct()

    /** "Simulated user" for generated requests, otherwise "42, ip: 10.0.0.1" */
    val userLabel: String get() = UserId.label(userId)

    fun hits(policyName: String): List<Long> = valuesOf(policyName) { it.hits }

    fun hitRatios(policyName: String): List<Double> = valuesOf(policyName) { it.hitRatio }

    fun savedBytesRatios(policyName: String): List<Double> = valuesOf(policyName) { it.savedBytesRatio }

    fun savedMegabytes(policyName: String): List<Long> = valuesOf(policyName) { it.savedBytes / MB }

    /**
     * Traffic left with the cache, in MB.
     *
     * Kept as it always was: it subtracts the saved *megabytes* from the requested *bytes*, so it is barely
     * below the traffic without a cache.
     */
    fun transferDecrease(policyName: String): List<Long> = savedMegabytes(policyName).map { (requestedBytes - it) / MB }

    fun transferDecreaseRatios(policyName: String): List<Double> = savedBytesRatios(policyName).map { 100 - it }

    private inline fun <T> valuesOf(
        policyName: String,
        value: (CacheResult) -> T,
    ): List<T> = results.filter { it.policyName == policyName }.map(value)
}
