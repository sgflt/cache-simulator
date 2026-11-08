package cz.zcu.kiv.cachesimulator.config

/**
 * Everything that configures a simulation apart from the policies and cache sizes.
 * The defaults are the simulator's built-in settings; `config.xml` can override them (see [ConfigFile]).
 */
data class SimulationSettings(
    /** number of requests a generator produces */
    val requestCount: Int = 100_000,
    /** smallest generated file ID */
    val minFileId: Int = 1,
    /** largest generated file ID, i.e. roughly the number of distinct files */
    val maxFileId: Int = 10_000,
    val gaussianMean: Int = 5_000,
    val gaussianDispersion: Int = 100,
    /** the preference generator favours multiples of this number... */
    val preferredFileMultiple: Int = 100,
    /** ...with every n-th request */
    val preferenceStep: Int = 4,
    /** the preference generator never requests multiples of this number otherwise */
    val excludedFileMultiple: Int = 7,
    val zipfAlpha: Double = 0.75,
    /** smallest generated file size in bytes */
    val minFileSize: Int = 1024,
    /** largest generated file size in bytes */
    val maxFileSize: Int = 5 * 1024 * 1024,
    /** AFS log file to replay */
    val afsLogFile: String = "",
    /** with an AFS log: generate file sizes instead of using the sizes in the log */
    val randomFileSizesForLog: Boolean = true,
    /** LFU-SS family reports cache hits to the server as well, not only misses */
    val sendLfuSsStatistics: Boolean = true,
    /** LRFU-SS reports cache hits to the server as well, not only misses */
    val sendLrfuSsStatistics: Boolean = true,
    /** users with fewer requests get no results */
    val minUserRequests: Int = 20,
    /** average network speed in Mbit/s; decides how long a file bigger than the cache takes to download */
    val networkSpeedMbit: Int = 80,
    /** share of the cache, in %, given to downloading a file bigger than the cache */
    val downloadWindowPercent: Int = 25,
    /** last directory used in file dialogs */
    val workingDirectory: String = ".",
) {
    /** [downloadWindowPercent] as a fraction of 1 */
    val downloadWindow: Double get() = downloadWindowPercent / 100.0
}
