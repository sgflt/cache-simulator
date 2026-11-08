package cz.zcu.kiv.cachesimulator.config

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigFileTest {
    @Test
    fun `settings survive a round trip`(
        @TempDir dir: Path,
    ) {
        val file = dir.resolve("config.xml")
        val settings =
            SimulationSettings(
                requestCount = 1234,
                maxFileId = 777,
                zipfAlpha = 1.25,
                sendLrfuSsStatistics = false,
                downloadWindowPercent = 40,
                workingDirectory = "/tmp/logs",
            )

        ConfigFile.write(settings, file)

        assertEquals(settings, ConfigFile.read(file))
    }

    @Test
    fun `reads a file written by the Java version`(
        @TempDir dir: Path,
    ) {
        val file = dir.resolve("config.xml")
        file.writeText(
            """
            <?xml version="1.0" ?><Config><workingDir dir="/home/me"></workingDir>
            <requestCountForRandomGenerator value="5000"></requestCountForRandomGenerator>
            <gaussianGeneratorParameters dispersion="50" meanValue="300"></gaussianGeneratorParameters>
            <prefRandomGeneratorParameters preferenceFile="10" preferenceStep="3" nonPreferenceStep="9"></prefRandomGeneratorParameters>
            <generatorsParameters minValue="2" maxValue="900" minSize="10" maxSize="2000"></generatorsParameters>
            <sendStatToServer lfu-ss="false" lrfu-ss="true"></sendStatToServer>
            <simulatorSettings limitForStatistics="5" averageNetworkSpeed="100" cacheCapacityForDownloadWindow="0.3"></simulatorSettings>
            <zipfLambda value="0.5"></zipfLambda></Config>
            """.trimIndent(),
        )

        val expected =
            SimulationSettings(
                workingDirectory = "/home/me",
                requestCount = 5000,
                gaussianDispersion = 50,
                gaussianMean = 300,
                preferredFileMultiple = 10,
                preferenceStep = 3,
                excludedFileMultiple = 9,
                minFileId = 2,
                maxFileId = 900,
                minFileSize = 10,
                maxFileSize = 2000,
                sendLfuSsStatistics = false,
                minUserRequests = 5,
                networkSpeedMbit = 100,
                downloadWindowPercent = 30,
                zipfAlpha = 0.5,
            )
        assertEquals(expected, ConfigFile.read(file))
    }

    @Test
    fun `missing or broken file gives the defaults`(
        @TempDir dir: Path,
    ) {
        val defaults = SimulationSettings(requestCount = 1)
        assertEquals(defaults, ConfigFile.read(dir.resolve("missing.xml"), defaults))
        val broken = dir.resolve("broken.xml").also { it.writeText("<Config>") }
        assertEquals(defaults, ConfigFile.read(broken, defaults))
    }
}
