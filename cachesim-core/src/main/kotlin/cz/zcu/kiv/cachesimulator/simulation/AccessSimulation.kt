package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.logger
import cz.zcu.kiv.cachesimulator.policy.CachePolicy
import cz.zcu.kiv.cachesimulator.policy.CachedFile
import cz.zcu.kiv.cachesimulator.policy.LfuSs
import cz.zcu.kiv.cachesimulator.policy.LrfuSs
import cz.zcu.kiv.cachesimulator.request.ProgressListener
import cz.zcu.kiv.cachesimulator.request.Request
import cz.zcu.kiv.cachesimulator.request.RequestQueue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.TreeMap
import kotlin.time.measureTime

/**
 * Feeds requests to every simulated cache. Both simulations check for cancellation of the calling coroutine
 * after every request.
 */
class AccessSimulation(
    private val requests: RequestQueue,
    private val config: SimulationConfig,
    private val context: SimulationContext = SimulationContext(),
    private val onProgress: ProgressListener = {},
) {
    private val server = context.server
    private val settings = context.settings

    /** by user ID, so results come out sorted by user */
    private val users = TreeMap<Long, SimulatedUser>()

    private fun user(id: Long) = users.getOrPut(id) { SimulatedUser(id, config, context) }

    /**
     * Replays the requests once per cache, all as one user. Files the server does not know yet get a
     * random size.
     */
    suspend fun simulateRandomFileSizes() {
        val coroutineContext = currentCoroutineContext()
        val took =
            measureTime {
                val user = user(UserId.SIMULATED)
                val measurements = user.measurements
                measurements.forEachIndexed { pass, measurement ->
                    // progress over all passes
                    requests.onProgress = { percent -> onProgress((pass * 100 + percent) / measurements.size) }
                    generateSequence(requests::next).forEach { request ->
                        coroutineContext.ensureActive()
                        context.now = request.time
                        if (request.fileName !in server) {
                            server.createFileWithRandomSize(
                                request.fileName,
                                settings.minFileSize.toLong(),
                                settings.maxFileSize.toLong(),
                            )
                        }
                        val cache = measurement.cache
                        val cached = cache.get(request.fileName)
                        if (cached != null) recordHit(measurement, request, cached.size) else fetch(cache, request)
                    }
                    requests.reset()
                    server.resetStatistics()
                    measurement.cache.reset()
                }
                requests.onProgress = {}
                generateSequence(requests::next).forEach { user.recordRequest(server.fileSize(it.fileName)) }
                onProgress(100)
            }
        log.info("Simulation done in {} ms", took.inWholeMilliseconds)
    }

    /**
     * Replays the requests once, for every user who appears in them, with file sizes from the requests.
     */
    suspend fun simulateFromLog() {
        val coroutineContext = currentCoroutineContext()
        requests.onProgress = onProgress
        generateSequence(requests::next).forEach { request ->
            coroutineContext.ensureActive()
            context.now = request.time
            val user = user(request.userId)
            if (request.fileName !in server) server.addFile(request.fileName, request.fileSize)
            user.recordRequest(server.fileSize(request.fileName))
            for (measurement in user.measurements) {
                val cache = measurement.cache
                if (cache.get(request.fileName) != null) {
                    // asks the cache a second time, as this simulation always has; for most policies that
                    // counts as another access
                    recordHit(measurement, request, cache.get(request.fileName)!!.size)
                } else {
                    fetch(cache, request)
                }
            }
        }
    }

    /** @return results of every user who made a request */
    fun results(): List<UserStatistics> = users.values.map { it.statistics(settings.minUserRequests) }

    private fun recordHit(
        measurement: Measurement,
        request: Request,
        fileSize: Long,
    ) {
        measurement.recordHit(fileSize)
        // tell the server about hits too, so that its statistics see every access
        if (reportsHits(measurement.cache)) {
            server.getFile(request.fileName, measurement.cache.statsKey, countRequest = true)
        }
    }

    /** downloads the file into [cache] after a miss */
    private fun fetch(
        cache: CachePolicy,
        request: Request,
    ) {
        val file =
            checkNotNull(server.getFile(request.fileName, cache.statsKey, cache.needsServerStatistics)) {
                "File ${request.fileName} is not on the server"
            }
        cache.insertFile(CachedFile.fetched(file, cache.statsKey, request.time))
    }

    private fun reportsHits(cache: CachePolicy) =
        when (cache) {
            is LfuSs -> settings.sendLfuSsStatistics
            is LrfuSs -> settings.sendLrfuSsStatistics
            else -> false
        }

    private companion object {
        val log = logger<AccessSimulation>()
    }
}
