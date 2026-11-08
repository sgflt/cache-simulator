package cz.zcu.kiv.cachesimulator.config

import cz.zcu.kiv.cachesimulator.logger
import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.io.path.bufferedWriter
import kotlin.io.path.isRegularFile

/**
 * Reads and writes [SimulationSettings] as `config.xml`, in the format earlier versions used.
 */
object ConfigFile {
    val DEFAULT_PATH: Path = Path.of("config.xml")

    private val log = logger<ConfigFile>()

    /**
     * @return the settings in [path]; settings missing there keep their value from [defaults].
     *   A missing or unreadable file gives [defaults].
     */
    fun read(
        path: Path = DEFAULT_PATH,
        defaults: SimulationSettings = SimulationSettings(),
    ): SimulationSettings {
        if (!path.isRegularFile()) return defaults
        return try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile())
            parse(document.documentElement, defaults)
        } catch (e: Exception) {
            log.warn("Cannot read {}, using default settings", path, e)
            defaults
        }
    }

    fun write(
        settings: SimulationSettings,
        path: Path = DEFAULT_PATH,
    ) {
        path.bufferedWriter().use { out ->
            val xml = XMLOutputFactory.newInstance().createXMLStreamWriter(out)
            xml.writeStartDocument()
            xml.element("Config") {
                with(settings) {
                    element("workingDir", "dir" to workingDirectory)
                    element("requestCountForRandomGenerator", "value" to requestCount)
                    element("gaussianGeneratorParameters", "dispersion" to gaussianDispersion, "meanValue" to gaussianMean)
                    element(
                        "prefRandomGeneratorParameters",
                        "preferenceFile" to preferredFileMultiple,
                        "preferenceStep" to preferenceStep,
                        "nonPreferenceStep" to excludedFileMultiple,
                    )
                    element(
                        "generatorsParameters",
                        "minValue" to minFileId,
                        "maxValue" to maxFileId,
                        "minSize" to minFileSize,
                        "maxSize" to maxFileSize,
                    )
                    element("sendStatToServer", "lfu-ss" to sendLfuSsStatistics, "lrfu-ss" to sendLrfuSsStatistics)
                    element(
                        "simulatorSettings",
                        "limitForStatistics" to minUserRequests,
                        "averageNetworkSpeed" to networkSpeedMbit,
                        "cacheCapacityForDownloadWindow" to downloadWindow,
                    )
                    element("zipfLambda", "value" to zipfAlpha)
                }
            }
            xml.writeEndDocument()
            xml.flush()
            xml.close()
        }
    }

    private fun parse(
        root: Element,
        defaults: SimulationSettings,
    ): SimulationSettings {
        fun attribute(
            element: String,
            name: String,
        ): String? =
            (root.getElementsByTagName(element).item(0) as Element?)
                ?.getAttribute(name)
                ?.takeIf { it.isNotEmpty() }

        fun int(
            element: String,
            name: String,
            default: Int,
        ) = attribute(element, name)?.toInt() ?: default

        fun bool(
            element: String,
            name: String,
            default: Boolean,
        ) = attribute(element, name)?.toBoolean() ?: default

        return with(defaults) {
            copy(
                workingDirectory = attribute("workingDir", "dir") ?: workingDirectory,
                requestCount = int("requestCountForRandomGenerator", "value", requestCount),
                zipfAlpha = attribute("zipfLambda", "value")?.toDouble() ?: zipfAlpha,
                gaussianDispersion = int("gaussianGeneratorParameters", "dispersion", gaussianDispersion),
                gaussianMean = int("gaussianGeneratorParameters", "meanValue", gaussianMean),
                preferredFileMultiple = int("prefRandomGeneratorParameters", "preferenceFile", preferredFileMultiple),
                preferenceStep = int("prefRandomGeneratorParameters", "preferenceStep", preferenceStep),
                excludedFileMultiple = int("prefRandomGeneratorParameters", "nonPreferenceStep", excludedFileMultiple),
                minFileId = int("generatorsParameters", "minValue", minFileId),
                maxFileId = int("generatorsParameters", "maxValue", maxFileId),
                minFileSize = int("generatorsParameters", "minSize", minFileSize),
                maxFileSize = int("generatorsParameters", "maxSize", maxFileSize),
                sendLfuSsStatistics = bool("sendStatToServer", "lfu-ss", sendLfuSsStatistics),
                sendLrfuSsStatistics = bool("sendStatToServer", "lrfu-ss", sendLrfuSsStatistics),
                minUserRequests = int("simulatorSettings", "limitForStatistics", minUserRequests),
                networkSpeedMbit = int("simulatorSettings", "averageNetworkSpeed", networkSpeedMbit),
                downloadWindowPercent =
                    attribute("simulatorSettings", "cacheCapacityForDownloadWindow")
                        ?.let { (it.toDouble() * 100).toInt() }
                        ?: downloadWindowPercent,
            )
        }
    }

    private inline fun XMLStreamWriter.element(
        name: String,
        vararg attributes: Pair<String, Any>,
        content: XMLStreamWriter.() -> Unit = {},
    ) {
        writeStartElement(name)
        attributes.forEach { (key, value) -> writeAttribute(key, value.toString()) }
        content()
        writeEndElement()
    }
}
