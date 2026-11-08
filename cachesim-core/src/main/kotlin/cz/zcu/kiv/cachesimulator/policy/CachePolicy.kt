package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.server.FileOnServer
import cz.zcu.kiv.cachesimulator.server.StatsKey
import cz.zcu.kiv.cachesimulator.simulation.SimulationContext

/**
 * A file in a client cache.
 *
 * @property accessTime when the file was requested; 0 for generated requests
 * @property readRequests read requests of this file by the cache, as the server counted them when it was fetched
 * @property writeRequests write requests of this file by the cache, as the server counted them when it was fetched
 */
class CachedFile(
    val name: String,
    val size: Long,
    val accessTime: Long,
    val readRequests: Int = 0,
    val writeRequests: Int = 0,
) {
    companion object {
        fun fetched(
            file: FileOnServer,
            cacheKey: StatsKey,
            accessTime: Long,
        ) = CachedFile(file.name, file.size, accessTime, file.readRequests(cacheKey), file.writeRequests(cacheKey))
    }
}

/**
 * A cache replacement policy simulating one client cache.
 *
 * Files bigger than the whole cache are not cached. Downloading one takes a share of the cache (the download
 * window, [cz.zcu.kiv.cachesimulator.config.SimulationSettings.downloadWindowPercent]) until the download has
 * finished at the average network speed.
 *
 * @param name name in the results; together with the current capacity it identifies the cache on the server
 * @param configuredCapacity cache size in bytes
 */
abstract class CachePolicy(
    val name: String,
    protected val context: SimulationContext,
    val configuredCapacity: Long,
) {
    /** usable capacity in bytes: [configuredCapacity] minus the download window while one is open */
    var capacity: Long = configuredCapacity
        private set

    /** bytes taken by the cached files; policies keep it up to date */
    protected var usedCapacity: Long = 0

    open val freeCapacity: Long get() = capacity - usedCapacity

    /** files bigger than the cache that are still being downloaded, oldest first */
    private val downloads = ArrayDeque<CachedFile>()

    /** whether the server has to count this cache's requests */
    open val needsServerStatistics: Boolean get() = false

    /** this cache in the server statistics */
    val statsKey: StatsKey get() = StatsKey(name, capacity)

    /** @return the cached file, or null on a cache miss; a hit updates the policy's bookkeeping */
    abstract fun get(fileName: String): CachedFile?

    /** evicts one file */
    abstract fun removeFile()

    /** caches [file] after a miss, evicting files as needed */
    open fun insertFile(file: CachedFile) {
        if (file.size > capacity) {
            if (downloads.isNotEmpty()) {
                downloads += file
            } else {
                openDownloadWindow(file)
            }
            return
        }
        closeFinishedDownloads()
        makeRoom(file.size)
        store(file)
    }

    /** empties the cache */
    fun reset() {
        capacity = configuredCapacity
        usedCapacity = 0
        downloads.clear()
        clear()
    }

    /** adds [file] to the cache; there is room for it already */
    protected abstract fun store(file: CachedFile)

    /** forgets all cached files and the policy's bookkeeping */
    protected abstract fun clear()

    protected open fun makeRoom(bytes: Long) {
        while (freeCapacity < bytes) removeFile()
    }

    /** makes room for downloading [file], which is bigger than the cache */
    protected fun openDownloadWindow(file: CachedFile) {
        val window = context.settings.downloadWindow
        while (freeCapacity < (capacity.toDouble() * window).toLong()) removeFile()
        downloads += file
        capacity = (capacity.toDouble() * (1 - window)).toLong()
    }

    /** gives the download window back to the cache once all downloads have finished */
    protected fun closeFinishedDownloads() {
        if (downloads.isEmpty()) return
        while (downloads.isNotEmpty() && downloads.first().downloadedAt() < context.now) {
            downloads.removeFirst()
        }
        if (downloads.isEmpty()) capacity = configuredCapacity
    }

    private fun CachedFile.downloadedAt(): Long =
        if (accessTime ==
            0L
        ) {
            0
        } else {
            size * 8 / 1000 / context.settings.networkSpeedMbit + accessTime
        }

    override fun toString() = name
}
