package cz.zcu.kiv.cachesimulator.request

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import java.nio.file.Path

/** Where the simulated file requests come from. */
enum class RequestSource(
    private val label: String,
) {
    AFS_LOG("From AFS log file"),
    GAUSSIAN("Gaussian random"),
    RANDOM("Random"),
    RANDOM_WITH_PREFERENCE("Random with preference"),
    ZIPF("Zipf random"),
    ;

    val isGenerator: Boolean get() = this != AFS_LOG

    fun createQueue(settings: SimulationSettings): RequestQueue {
        val count = settings.requestCount.toLong()
        return when (this) {
            AFS_LOG -> AfsLogReader(Path.of(settings.afsLogFile))
            GAUSSIAN -> GaussianRequestGenerator(count, settings)
            RANDOM -> UniformRequestGenerator(count, settings)
            RANDOM_WITH_PREFERENCE -> PreferenceRequestGenerator(count, settings)
            ZIPF -> ZipfRequestGenerator(count, settings)
        }
    }

    override fun toString() = label
}
