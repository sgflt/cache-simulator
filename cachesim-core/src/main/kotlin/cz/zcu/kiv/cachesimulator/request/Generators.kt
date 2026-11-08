package cz.zcu.kiv.cachesimulator.request

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import java.util.Random
import kotlin.math.abs

/** seed of every generator, so that runs are repeatable */
private const val SEED = 0L

/** File IDs with a normal distribution, limited to `minFileId..maxFileId`. */
class GaussianRequestGenerator(
    limit: Long,
    private val settings: SimulationSettings = SimulationSettings(),
) : RequestGenerator(limit) {
    private var random = Random(SEED)

    override fun nextFileId(): Int {
        val range = settings.minFileId..settings.maxFileId
        while (true) {
            val id = (settings.gaussianMean + random.nextGaussian() * settings.gaussianDispersion).toInt()
            if (id in range) return id
        }
    }

    override fun restart() {
        random = Random(SEED)
    }
}

/** File IDs with a uniform distribution. */
class UniformRequestGenerator(
    limit: Long,
    private val settings: SimulationSettings = SimulationSettings(),
) : RequestGenerator(limit) {
    private var random = Random(SEED)

    override fun nextFileId(): Int = abs(random.nextInt()) % (settings.maxFileId - settings.minFileId) + settings.minFileId

    override fun restart() {
        random = Random(SEED)
    }
}

/**
 * Uniform file IDs, except that every [SimulationSettings.preferenceStep]-th request goes to a multiple of
 * [SimulationSettings.preferredFileMultiple], and multiples of [SimulationSettings.excludedFileMultiple]
 * are never requested otherwise.
 */
class PreferenceRequestGenerator(
    limit: Long,
    private val settings: SimulationSettings = SimulationSettings(),
) : RequestGenerator(limit) {
    private var random = Random(SEED)
    private var count = 0L

    override fun nextFileId(): Int {
        count++
        with(settings) {
            val span = maxFileId - minFileId
            if (count % preferenceStep == 0L) {
                return (abs(random.nextInt()) % (span / preferredFileMultiple) + 1) * preferredFileMultiple
            }
            var id = excludedFileMultiple
            while (id % excludedFileMultiple == 0) {
                id = abs(random.nextInt()) % span + minFileId
            }
            return id
        }
    }

    override fun restart() {
        random = Random(SEED)
        count = 0
    }
}

/** File IDs with a Zipf distribution: a few files get most of the requests. */
class ZipfRequestGenerator(
    limit: Long,
    private val settings: SimulationSettings = SimulationSettings(),
) : RequestGenerator(limit) {
    private var zipf = newDistribution()

    override fun nextFileId(): Int = abs(zipf.next()) % (settings.maxFileId - settings.minFileId) + settings.minFileId

    override fun restart() {
        zipf = newDistribution()
    }

    private fun newDistribution() = ZipfDistribution(settings.maxFileId, settings.zipfAlpha)
}

/**
 * Random numbers `0 until n` with a Zipf distribution.
 */
class ZipfDistribution(
    private val n: Int,
    private val alpha: Double,
) {
    private val random = Random(SEED)

    /** cumulative probability of each value */
    private val cumulative: DoubleArray by lazy {
        // summed in the same order as the original linear scan, so results stay bit-for-bit the same
        var sum = 0.0
        for (i in 1..n) sum += 1.0 / Math.pow(i.toDouble(), alpha)
        val normalization = 1.0 / sum
        var probability = 0.0
        DoubleArray(n) { i ->
            probability += normalization / Math.pow((i + 1).toDouble(), alpha)
            probability
        }
    }

    /** @return the next value, or -1 when rounding left the last cumulative probability just below the drawn number */
    fun next(): Int {
        val table = cumulative
        var z: Double
        do {
            z = random.nextDouble()
        } while (z == 0.0 || z == 1.0)

        // first value whose cumulative probability reaches z
        var low = 0
        var high = n - 1
        while (low < high) {
            val mid = (low + high) ushr 1
            if (table[mid] >= z) high = mid else low = mid + 1
        }
        return if (n > 0 && table[low] >= z) low else -1
    }
}
