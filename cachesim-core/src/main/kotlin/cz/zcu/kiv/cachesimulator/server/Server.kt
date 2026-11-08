package cz.zcu.kiv.cachesimulator.server

import cz.zcu.kiv.cachesimulator.logger
import java.util.Random
import kotlin.math.abs

/**
 * Identifies a client cache in the server statistics. Caches of the same policy and current capacity share
 * their counters, even across users.
 */
data class StatsKey(
    val policyName: String,
    val capacity: Long,
)

enum class OpenMode { READ, WRITE, READ_WRITE }

/**
 * The file server. It knows every file's size and, for policies that need it, how often each file was requested.
 * Every simulation run has its own server.
 */
class Server(
    private val random: Random = Random(0),
) {
    private val files = HashMap<String, FileOnServer>(INITIAL_CAPACITY, 0.8f)
    private val readRequests = RequestCounters()
    private val writeRequests = RequestCounters()

    /** the last file asked for; it is likely to be asked for again right away */
    private var lastFile: FileOnServer? = null

    /** creates the file [name] with a random size in `minSize until maxSize` */
    fun createFileWithRandomSize(
        name: String,
        minSize: Long,
        maxSize: Long,
    ) {
        addFile(FileOnServer(name, abs(random.nextLong()) % (maxSize - minSize) + minSize))
    }

    fun addFile(
        name: String,
        size: Long,
    ) {
        addFile(FileOnServer(name, size))
    }

    private fun addFile(file: FileOnServer) {
        files[file.name] = file
        lastFile = file
    }

    operator fun contains(name: String): Boolean = lastFile?.name.equals(name, ignoreCase = true) || name in files

    /**
     * Fetches a file for [cacheKey]. When [countRequest] is true, the request also counts in the statistics.
     *
     * @return the file, or null when there is no such file
     */
    fun getFile(
        name: String,
        cacheKey: StatsKey,
        countRequest: Boolean,
        mode: OpenMode = OpenMode.READ,
    ): FileOnServer? {
        if (files.isEmpty()) return null
        val file = cached(name) ?: files[name] ?: return null
        if (countRequest) {
            if (mode != OpenMode.WRITE) {
                readRequests.increment(cacheKey)
                file.readRequests.increment(cacheKey)
            }
            if (mode != OpenMode.READ) {
                writeRequests.increment(cacheKey)
                file.writeRequests.increment(cacheKey)
            }
        }
        lastFile = file
        return file
    }

    /** read requests of [cacheKey] over all files */
    fun globalReadRequests(cacheKey: StatsKey): Long = readRequests.count(cacheKey).toLong()

    /** @return size of the file [name], or 0 for an unknown file */
    fun fileSize(name: String): Long {
        cached(name)?.let { return it.size }
        val file = files[name]
        if (file == null) {
            log.warn("Unknown file {}", name)
            return 0
        }
        lastFile = file
        return file.size
    }

    /** forgets all request statistics, keeps the files */
    fun resetStatistics() {
        readRequests.clear()
        writeRequests.clear()
        files.values.forEach(FileOnServer::resetStatistics)
    }

    private fun cached(name: String): FileOnServer? = lastFile?.takeIf { it.name.equals(name, ignoreCase = true) }

    private companion object {
        const val INITIAL_CAPACITY = 200_000
        val log = logger<Server>()
    }
}

/** A file on the server with its request statistics per client cache. */
class FileOnServer(
    val name: String,
    val size: Long,
) {
    internal val readRequests = RequestCounters()
    internal val writeRequests = RequestCounters()

    fun readRequests(cacheKey: StatsKey): Int = readRequests.count(cacheKey)

    fun writeRequests(cacheKey: StatsKey): Int = writeRequests.count(cacheKey)

    internal fun resetStatistics() {
        readRequests.clear()
        writeRequests.clear()
    }
}

/**
 * Request counts per cache. Kept as it always worked: the first increment only registers the cache and leaves
 * its count at 0.
 */
internal class RequestCounters {
    private val counts = HashMap<StatsKey, Int>()

    fun count(key: StatsKey): Int = counts.getOrPut(key) { 0 }

    fun increment(key: StatsKey) {
        counts[key] = counts[key]?.plus(1) ?: 0
    }

    fun clear() = counts.clear()
}
