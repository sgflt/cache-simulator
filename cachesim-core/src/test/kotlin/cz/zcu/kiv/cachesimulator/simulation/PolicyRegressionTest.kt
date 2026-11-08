package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.request.GaussianRequestGenerator
import cz.zcu.kiv.cachesimulator.request.Request
import cz.zcu.kiv.cachesimulator.request.RequestQueue
import cz.zcu.kiv.cachesimulator.request.ZipfRequestGenerator
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.util.Random
import kotlin.io.path.writeLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Runs every cache policy on fixed request streams and compares the results with the recorded ones in
 * `policy-regression.txt`. Refactoring must not change a single number.
 *
 * To re-record after an intended behaviour change, run with `-Dpolicy.regression.record=<path to the resource file>`.
 */
class PolicyRegressionTest {
    @Test
    fun `every policy gives the recorded results`() {
        val config = SimulationConfig(CachePolicies.all, SIZES_MB)

        val actual =
            buildList {
                // big files, few of them fit: exercises eviction and the download window
                addAll(runLog("zipf-big-files", config, generate(ZipfRequestGenerator(20_000), 1, 1024, 5 * MB)))
                // several users with their own caches
                addAll(runLog("gauss-3-users", config, generate(GaussianRequestGenerator(20_000), 3, 1024, 2 * MB)))
                // small files: caches hold hundreds or thousands of entries
                addAll(runLog("zipf-small-files", config, generate(ZipfRequestGenerator(20_000), 1, 1024, 64 * 1024)))
                // generator mode: one pass per cache, server statistics reset between passes
                addAll(runRandomSizes("zipf-generator-mode", config, generate(ZipfRequestGenerator(10_000), 1, 1024, 512 * 1024)))
            }.filterNot { NOT_REPRODUCIBLE in it }

        System.getProperty("policy.regression.record")?.let { record ->
            Path.of(record).writeLines(actual)
            return
        }

        val expected = assertNotNull(javaClass.getResource(RESOURCE), "$RESOURCE is missing").readText().lines()
        assertEquals(expected.joinToString("\n").trimEnd(), actual.joinToString("\n"))
    }

    /** turns a generator's file names into a log with fixed per-file sizes and growing access times */
    private fun generate(
        generator: RequestQueue,
        users: Int,
        minSize: Long,
        maxSize: Long,
    ): List<Request> {
        val random = Random(42)
        val sizes = HashMap<String, Long>()
        var time = 0L
        return generateSequence(generator::next)
            .map { request ->
                val size = sizes.getOrPut(request.fileName) { minSize + (random.nextDouble() * (maxSize - minSize)).toLong() }
                time += 1 + random.nextInt(200)
                Request(request.fileName, time, random.nextInt(users).toLong(), size)
            }.toList()
    }

    private fun runLog(
        scenario: String,
        config: SimulationConfig,
        requests: List<Request>,
    ): List<String> {
        val simulation = AccessSimulation(ListQueue(requests), config)
        runBlocking { simulation.simulateFromLog() }
        return describe(scenario, simulation)
    }

    private fun runRandomSizes(
        scenario: String,
        config: SimulationConfig,
        requests: List<Request>,
    ): List<String> {
        // files exist up front, so the server's random sizes are never used
        val context = SimulationContext()
        requests.forEach { if (it.fileName !in context.server) context.server.addFile(it.fileName, it.fileSize) }
        val simulation = AccessSimulation(ListQueue(requests), config, context)
        runBlocking { simulation.simulateRandomFileSizes() }
        return describe(scenario, simulation)
    }

    private fun describe(
        scenario: String,
        simulation: AccessSimulation,
    ): List<String> =
        simulation.results().sortedBy { it.userId }.flatMap { user ->
            val prefix = "$scenario user=${user.userId}"
            listOf("$prefix requests=${user.requests} bytes=${user.requestedBytes}") +
                user.policyNames.sorted().map { policy ->
                    "$prefix $policy hits=${user.hits(policy)} hitRatio=${user.hitRatios(policy)} " +
                        "savedRatio=${user.savedBytesRatios(policy)}"
                }
        }

    private companion object {
        const val RESOURCE = "/policy-regression.txt"
        const val MB = 1024L * 1024
        val SIZES_MB = listOf(1, 8, 64)

        /**
         * LRFU-SS ranks files by System.nanoTime(), so its numbers change from run to run.
         * It still runs here (it must not crash), but its results are not compared.
         */
        const val NOT_REPRODUCIBLE = " LRFU-SS "
    }
}

/** request source backed by a list, imitating a log file */
internal class ListQueue(
    private val requests: List<Request>,
) : RequestQueue() {
    private var iterator = requests.iterator()

    override fun next(): Request? = if (iterator.hasNext()) iterator.next() else null

    override fun reset() {
        iterator = requests.iterator()
    }
}
