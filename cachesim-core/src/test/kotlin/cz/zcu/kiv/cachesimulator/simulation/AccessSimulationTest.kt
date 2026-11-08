package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.request.AfsLogReader
import cz.zcu.kiv.cachesimulator.request.GaussianRequestGenerator
import cz.zcu.kiv.cachesimulator.request.PreferenceRequestGenerator
import cz.zcu.kiv.cachesimulator.request.Request
import cz.zcu.kiv.cachesimulator.request.RequestQueue
import cz.zcu.kiv.cachesimulator.request.RequestSource
import cz.zcu.kiv.cachesimulator.request.UniformRequestGenerator
import cz.zcu.kiv.cachesimulator.request.ZipfRequestGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs small simulations without any user interface. */
class AccessSimulationTest {
    private val config = SimulationConfig(listOf(CachePolicies.LRU, CachePolicies.FIFO, CachePolicies.ARC), listOf(8, 64))

    @Test
    fun `zipf simulation produces results for every cache and size`() {
        val requests = 2_000
        val simulation = AccessSimulation(ZipfRequestGenerator(requests.toLong()), config)

        runBlocking { simulation.simulateRandomFileSizes() }

        val user = simulation.results().single()
        assertEquals(requests.toLong(), user.requests)
        assertEquals(listOf(8, 64), user.cacheSizesMb)
        assertEquals(listOf("LRU", "FIFO", "ARC"), user.policyNames)
        for (policy in user.policyNames) {
            val ratios = user.hitRatios(policy)
            assertEquals(2, ratios.size, policy)
            ratios.forEach { assertTrue(it in 0.0..100.0, "$policy hit ratio $it") }
        }
    }

    @Test
    fun `progress grows over all passes up to 100 percent`() {
        val progress = mutableListOf<Int>()
        val simulation = AccessSimulation(ZipfRequestGenerator(1_000), config, onProgress = progress::add)

        runBlocking { simulation.simulateRandomFileSizes() }

        assertEquals(100, progress.last())
        assertEquals(progress.sorted(), progress, "progress went back")
    }

    @Test
    fun `cancelled simulation stops promptly`() {
        // 50M requests would run for minutes if cancellation were ignored
        val simulation = AccessSimulation(ZipfRequestGenerator(50_000_000), config)

        assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            runBlocking {
                val job = launch(Dispatchers.Default) { simulation.simulateRandomFileSizes() }
                delay(200)
                job.cancelAndJoin()
                assertTrue(job.isCancelled)
            }
        }
    }

    @Test
    fun `log simulation feeds every request to every cache`() {
        // the same file requested 10 times by one user: every cache gets 1 miss + 9 hits
        val requests = 10
        val simulation = AccessSimulation(ListQueue(List(requests) { Request("a", userId = 1, fileSize = 1024) }), config)

        runBlocking { simulation.simulateFromLog() }

        val user = simulation.results().single()
        assertEquals(requests.toLong(), user.requests)
        user.policyNames.forEach { assertEquals(listOf(9L, 9L), user.hits(it), it) }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("generators")
    fun `every generator delivers all requests`(
        name: String,
        generator: (Long) -> RequestQueue,
    ) {
        val requests = 1_000L
        val simulation = AccessSimulation(generator(requests), config)

        runBlocking { simulation.simulateRandomFileSizes() }

        val user = simulation.results().single()
        assertEquals(requests, user.requests, name)
        for (policy in user.policyNames) {
            // uniform random requests can miss every time in a small cache
            user.hits(policy).forEach { assertTrue(it in 0 until requests, "$name $policy hits $it") }
        }
    }

    @Test
    fun `AFS log with file sizes from the log`(
        @TempDir dir: Path,
    ) {
        val simulation = AccessSimulation(AfsLogReader(writeAfsLog(dir, 10)), config)

        runBlocking { simulation.simulateFromLog() }

        assertAllCachesHit9Of10(simulation)
    }

    @Test
    fun `AFS log with random file sizes is replayed for every cache`(
        @TempDir dir: Path,
    ) {
        val simulation = AccessSimulation(AfsLogReader(writeAfsLog(dir, 10)), config)

        runBlocking { simulation.simulateRandomFileSizes() }

        assertAllCachesHit9Of10(simulation)
    }

    @Test
    fun `runSimulation reads the AFS log and labels the user`(
        @TempDir dir: Path,
    ) {
        val settings = SimulationSettings(afsLogFile = writeAfsLog(dir, 30).toString(), randomFileSizesForLog = false)

        val results = runBlocking { runSimulation(RequestSource.AFS_LOG, config, settings) }

        val user = results.single()
        assertEquals("42, ip: 10.0.0.1", user.userLabel)
        assertEquals(30 * 2048L, user.requestedBytes)
    }

    @Test
    fun `users below the request limit have no per-cache results`() {
        val simulation =
            AccessSimulation(
                ListQueue(List(5) { Request("a", userId = 1, fileSize = 1024) }),
                config,
                SimulationContext(SimulationSettings(minUserRequests = 6)),
            )

        runBlocking { simulation.simulateFromLog() }

        assertEquals(emptyList(), simulation.results().single().policyNames)
    }

    private fun assertAllCachesHit9Of10(simulation: AccessSimulation) {
        val user = simulation.results().single()
        assertEquals(10, user.requests)
        user.policyNames.forEach { assertEquals(listOf(9L, 9L), user.hits(it), it) }
    }

    /** AFS file server log: one user (host 10.0.0.1, id 42) fetching the same 2 KB file */
    private fun writeAfsLog(
        dir: Path,
        fetches: Int,
    ): Path {
        val log =
            buildString {
                repeat(fetches) { i ->
                    append("Sun Nov  8 12:00:00 2026 SRXAFS_FetchData, Fid = 536870913.2.3\n")
                    append("Sun Nov  8 12:00:%02d 2026 FetchData, call, Host 10.0.0.1:7001, Id 42\n".format(i))
                    append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle\n")
                    append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle position 0\n")
                    append("Sun Nov  8 12:00:00 2026 FetchData_RXStyle, Pos 0, Len 2048, file size 2048\n")
                    append("Sun Nov  8 12:00:00 2026 SRXAFS_FetchData returns 0\n")
                }
            }
        return dir.resolve("afs.log").also { it.writeText(log) }
    }

    companion object {
        @JvmStatic
        fun generators(): List<Arguments> =
            listOf(
                Arguments.of("Gaussian", { n: Long -> GaussianRequestGenerator(n) }),
                Arguments.of("Random", { n: Long -> UniformRequestGenerator(n) }),
                Arguments.of("Random with preferences", { n: Long -> PreferenceRequestGenerator(n) }),
                Arguments.of("Zipf", { n: Long -> ZipfRequestGenerator(n) }),
            )
    }
}
