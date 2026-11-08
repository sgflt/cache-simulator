package cz.zcu.kiv.cachesimulator.cli

import cz.zcu.kiv.cachesimulator.cli.tui.TuiApp
import cz.zcu.kiv.cachesimulator.config.ConfigFile
import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.report.CsvReport
import cz.zcu.kiv.cachesimulator.report.XlsReport
import cz.zcu.kiv.cachesimulator.request.RequestSource
import cz.zcu.kiv.cachesimulator.simulation.ResultMetric
import cz.zcu.kiv.cachesimulator.simulation.SimulationConfig
import cz.zcu.kiv.cachesimulator.simulation.UserStatistics
import cz.zcu.kiv.cachesimulator.simulation.runSimulation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Spec
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.Callable
import kotlin.concurrent.thread
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = exitProcess(commandLine().execute(*args))

/** the command line with its converters; tests use it to run commands without exiting */
fun commandLine(): CommandLine =
    CommandLine(CliMain()).apply {
        isCaseInsensitiveEnumValuesAllowed = true
        registerConverter(RequestSource::class.java) { parseEnum<RequestSource>(it) }
        registerConverter(ResultMetric::class.java) { parseEnum<ResultMetric>(it) }
    }

/** accepts "afs-log", "AFS_LOG", "saved-bytes-ratio", ... */
private inline fun <reified E : Enum<E>> parseEnum(value: String): E {
    val name = value.trim().uppercase(Locale.ROOT).replace('-', '_')
    return enumValues<E>().find { it.name == name }
        ?: throw CommandLine.TypeConversionException("'$value' is not one of ${enumValues<E>().map { it.optionName }}")
}

private val Enum<*>.optionName: String get() = name.lowercase(Locale.ROOT).replace('_', '-')

/** true when attached to an interactive terminal, not a pipe or a file */
internal fun isTerminal(): Boolean = System.console()?.isTerminal == true

/** Command-line entry point. Without a subcommand it opens the interactive terminal UI. */
@Command(
    name = "cachesim",
    mixinStandardHelpOptions = true,
    version = ["Cache simulator 3.0-alpha"],
    description = ["Simulates client-side file cache policies.%nRun without a command to open the interactive terminal UI."],
    subcommands = [ListPolicies::class, Run::class, Tui::class],
)
class CliMain : Callable<Int> {
    @Spec
    lateinit var spec: CommandSpec

    override fun call(): Int {
        if (!isTerminal()) {
            spec.commandLine().usage(spec.commandLine().out)
            return 2
        }
        return TuiApp.run(ConfigFile.read())
    }
}

@Command(name = "list-policies", description = ["Lists the cache policies that can be simulated."])
class ListPolicies : Callable<Int> {
    @Spec
    lateinit var spec: CommandSpec

    override fun call(): Int {
        val out = spec.commandLine().out
        val policies = Policies.resolve(emptyList())
        val width = policies.maxOf { it.id.length }
        policies.forEach { out.println("${it.id.padEnd(width)}  ${it.label}") }
        out.flush()
        return 0
    }
}

@Command(name = "tui", description = ["Opens the interactive terminal UI."])
class Tui : Callable<Int> {
    override fun call(): Int = TuiApp.run(ConfigFile.read())
}

enum class Format { TABLE, CSV, XLS }

@Command(
    name = "run",
    sortOptions = false,
    description = [
        "Runs a simulation and prints the results.",
        "Settings not given here come from ./config.xml when it exists, otherwise from built-in defaults.",
    ],
)
class Run : Callable<Int> {
    @Spec
    lateinit var spec: CommandSpec

    @Option(
        names = ["-s", "--source"],
        paramLabel = "SOURCE",
        description = [
            "Where requests come from: afs-log, gaussian, random, random-with-preference, zipf. " +
                "Default: afs-log with --log, otherwise zipf.",
        ],
    )
    var source: RequestSource? = null

    @Option(names = ["-l", "--log"], paramLabel = "FILE", description = ["AFS log file to replay."])
    var log: Path? = null

    @Option(names = ["--random-sizes"], description = ["With an AFS log: generate file sizes instead of using the sizes in the log."])
    var randomSizes = false

    @Option(
        names = ["-p", "--policies"],
        split = ",",
        paramLabel = "NAME",
        description = ["Policies to simulate, by id or name (see list-policies). Default: all."],
    )
    var policies: MutableList<String> = ArrayList()

    @Option(
        names = ["-c", "--sizes"],
        split = ",",
        paramLabel = "MB",
        description = ["Cache capacities in MB. Default: \${DEFAULT-VALUE}."],
        defaultValue = "8,16,32,64,128,256,512,1024",
    )
    var sizes: MutableList<Int> = ArrayList()

    @Option(names = ["-n", "--requests"], paramLabel = "N", description = ["Number of generated requests."])
    var requests: Int? = null

    @Option(names = ["--files"], paramLabel = "N", description = ["Number of distinct generated files."])
    var files: Int? = null

    @Option(names = ["--zipf-lambda"], paramLabel = "X", description = ["Zipf generator lambda."])
    var zipfLambda: Double? = null

    @Option(names = ["--mean"], paramLabel = "N", description = ["Mean file ID for the Gaussian generator."])
    var mean: Int? = null

    @Option(names = ["--dispersion"], paramLabel = "N", description = ["Dispersion for the Gaussian generator."])
    var dispersion: Int? = null

    @Option(names = ["--min-file-size"], paramLabel = "BYTES", description = ["Smallest generated file size."])
    var minFileSize: Int? = null

    @Option(names = ["--max-file-size"], paramLabel = "BYTES", description = ["Largest generated file size."])
    var maxFileSize: Int? = null

    @Option(names = ["--min-user-requests"], paramLabel = "N", description = ["Leave out users with fewer requests than this."])
    var minUserRequests: Int? = null

    @Option(names = ["--network-speed"], paramLabel = "MBIT", description = ["Average network speed in Mbit/s."])
    var networkSpeed: Int? = null

    @Option(names = ["--sliding-window"], paramLabel = "PERCENT", description = ["Sliding window as %% of cache capacity."])
    var slidingWindow: Int? = null

    @Option(
        names = ["-m", "--metric"],
        split = ",",
        paramLabel = "METRIC",
        description = [
            "Tables to print: hit-ratio, hit-count, saved-bytes-ratio, saved-bytes, transfer-decrease-ratio, transfer-decrease. " +
                "Default: hit-ratio,saved-bytes-ratio.",
        ],
    )
    var metrics: MutableList<ResultMetric> = ArrayList()

    @Option(
        names = ["-f", "--format"],
        paramLabel = "FORMAT",
        description = ["Output format: table, csv or xls. Default: \${DEFAULT-VALUE}."],
        defaultValue = "table",
    )
    var format = Format.TABLE

    @Option(names = ["-o", "--out"], paramLabel = "FILE", description = ["Write results to this file (required for xls)."])
    var out: Path? = null

    @Option(names = ["-q", "--quiet"], description = ["Do not show the progress bar."])
    var quiet = false

    private val commandLine get() = spec.commandLine()

    override fun call(): Int {
        val requestSource = source ?: if (log != null) RequestSource.AFS_LOG else RequestSource.ZIPF
        validate(requestSource)
        val descriptors =
            try {
                Policies.resolve(policies)
            } catch (e: IllegalArgumentException) {
                throw ParameterException(commandLine, e.message)
            }
        val config = SimulationConfig(descriptors, sizes.sorted().distinct())
        val results = simulate(requestSource, config, settings(requestSource))
        return when {
            results == null -> {
                commandLine.err.println("Simulation cancelled.")
                130
            }

            results.isEmpty() -> {
                commandLine.err.println("No user made enough requests. Try a lower --min-user-requests.")
                1
            }

            else -> {
                write(results)
                0
            }
        }
    }

    private fun validate(requestSource: RequestSource) {
        fun fail(message: String): Nothing = throw ParameterException(commandLine, message)

        val log = log
        if (requestSource == RequestSource.AFS_LOG) {
            if (log == null) fail("--source afs-log needs --log FILE")
            if (!log.isRegularFile()) fail("Log file not found: $log")
        } else if (log != null) {
            fail("--log only works with --source afs-log")
        }
        if (sizes.isEmpty() || sizes.any { it <= 0 }) fail("--sizes must be positive numbers")
        if (format == Format.XLS && out == null) fail("--format xls needs --out FILE")
    }

    /** ./config.xml (or the defaults) overridden by the options given */
    private fun settings(requestSource: RequestSource): SimulationSettings {
        val base = ConfigFile.read()
        return base.copy(
            afsLogFile = log?.toAbsolutePath()?.toString() ?: base.afsLogFile,
            randomFileSizesForLog = requestSource != RequestSource.AFS_LOG || randomSizes,
            requestCount = requests ?: base.requestCount,
            maxFileId = files ?: base.maxFileId,
            zipfAlpha = zipfLambda ?: base.zipfAlpha,
            gaussianMean = mean ?: base.gaussianMean,
            gaussianDispersion = dispersion ?: base.gaussianDispersion,
            minFileSize = minFileSize ?: base.minFileSize,
            maxFileSize = maxFileSize ?: base.maxFileSize,
            minUserRequests = minUserRequests ?: base.minUserRequests,
            networkSpeedMbit = networkSpeed ?: base.networkSpeedMbit,
            downloadWindowPercent = slidingWindow ?: base.downloadWindowPercent,
        )
    }

    /**
     * Runs the simulation; Ctrl+C cancels it.
     *
     * @return results, or null when cancelled
     */
    private fun simulate(
        requestSource: RequestSource,
        config: SimulationConfig,
        settings: SimulationSettings,
    ): List<UserStatistics>? {
        val showProgress = !quiet && isTerminal()
        val bar = ProgressBar(System.err)
        return runBlocking {
            val simulation =
                async {
                    runSimulation(requestSource, config, settings, onProgress = if (showProgress) bar::update else { _ -> })
                }
            val cancelOnExit =
                thread(start = false, name = "cancel-simulation") {
                    simulation.cancel()
                    runBlocking { withTimeoutOrNull(CANCEL_TIMEOUT_MS) { simulation.join() } }
                    if (showProgress) bar.finish()
                    System.err.println("Simulation cancelled.")
                }
            Runtime.getRuntime().addShutdownHook(cancelOnExit)
            try {
                simulation.await()
            } catch (_: CancellationException) {
                null
            } finally {
                // fails when the JVM is already shutting down, and then the hook does the rest
                runCatching { Runtime.getRuntime().removeShutdownHook(cancelOnExit) }
                if (showProgress) bar.finish()
            }
        }
    }

    private fun write(results: List<UserStatistics>) {
        val out = out
        val stdout = commandLine.out
        when (format) {
            Format.TABLE -> {
                val chosen = metrics.ifEmpty { listOf(ResultMetric.HIT_RATIO, ResultMetric.SAVED_BYTES_RATIO) }.toSortedSet()
                val text =
                    buildString {
                        for (stats in results) {
                            append(ResultTable.header(stats)).append("\n\n")
                            chosen.forEach { append(ResultTable.render(stats, it)).append('\n') }
                        }
                    }
                if (out == null) stdout.print(text) else out.writeText(text)
            }

            Format.CSV -> {
                if (out == null) stdout.print(CsvReport.render(results)) else CsvReport.save(out, results)
            }

            Format.XLS -> {
                XlsReport.save(checkNotNull(out), results)
            }
        }
        stdout.flush()
    }

    private companion object {
        const val CANCEL_TIMEOUT_MS = 5_000L
    }
}
