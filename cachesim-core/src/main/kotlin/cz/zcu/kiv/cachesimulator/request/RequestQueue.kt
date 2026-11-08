package cz.zcu.kiv.cachesimulator.request

/** Receives progress in percent (0-100). It is called from the thread the simulation runs in. */
typealias ProgressListener = (percent: Int) -> Unit

/**
 * One file request.
 *
 * @property time access time in ms since the epoch; 0 for generated requests, which have no timing
 * @property userId who made the request, see [cz.zcu.kiv.cachesimulator.simulation.UserId]
 * @property fileSize size of the file in bytes, known only for requests from a log
 */
data class Request(
    val fileName: String,
    val time: Long = 0,
    val userId: Long = 0,
    val fileSize: Long = 0,
)

/** A replayable stream of file requests. */
abstract class RequestQueue {
    /** gets notified while the requests are read */
    var onProgress: ProgressListener = {}

    /** @return the next request, or null at the end */
    abstract fun next(): Request?

    /** starts over from the first request */
    abstract fun reset()
}

/**
 * Base of the random generators: produces [limit] requests and reports progress every 1 %.
 */
abstract class RequestGenerator(
    private val limit: Long,
) : RequestQueue() {
    private val progressStep = maxOf(1, limit.toInt() / 100)
    private var generated = 0L

    final override fun next(): Request? {
        generated++
        if (generated > limit) return null
        if (generated % progressStep == 0L) {
            onProgress((generated * 100 / limit).toInt())
        }
        return Request(nextFileId().toString())
    }

    final override fun reset() {
        generated = 0
        restart()
    }

    protected abstract fun nextFileId(): Int

    /** restarts the random sequence */
    protected abstract fun restart()
}
