package cz.zcu.kiv.cachesimulator.request

import cz.zcu.kiv.cachesimulator.logger
import cz.zcu.kiv.cachesimulator.simulation.UserId
import java.io.BufferedReader
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.path.bufferedReader
import kotlin.io.path.fileSize

/**
 * Replays the file fetches recorded in an AFS file server log. A fetch takes six lines:
 * ```
 * Sun Nov  8 12:00:00 2026 SRXAFS_FetchData, Fid = 536870913.2.3
 * Sun Nov  8 12:00:01 2026 FetchData, call, Host 10.0.0.1:7001, Id 42
 * ...
 * Sun Nov  8 12:00:00 2026 FetchData_RXStyle, Pos 0, Len 2048, file size 2048
 * Sun Nov  8 12:00:00 2026 SRXAFS_FetchData returns 0
 * ```
 */
class AfsLogReader(
    private val logFile: Path,
) : RequestQueue() {
    private val bytesPerPercent = maxOf(1, logFile.fileSize() / 100)
    private val record = arrayOfNulls<String>(RECORD_LINES)
    private var reader: BufferedReader? = open()
    private var position = 0L
    private var percent = 0

    override fun next(): Request? {
        val request = readRequest() ?: return null
        if (position / bytesPerPercent > percent) {
            percent++
            onProgress(percent)
        }
        return request
    }

    override fun reset() {
        reader?.close()
        reader = open()
        position = 0
        percent = 0
    }

    private fun open(): BufferedReader? =
        try {
            logFile.bufferedReader()
        } catch (e: IOException) {
            log.error("Cannot read AFS log {}", logFile, e)
            null
        }

    private fun readRequest(): Request? {
        val input = reader ?: return null
        try {
            var line = input.readLine()
            while (line != null) {
                position += line.length + 1
                if (line.contains(BEGIN) && line.splitFields().size == 2) {
                    record[0] = line
                    for (i in 1 until RECORD_LINES) {
                        line = input.readLine() ?: return null
                        position += line.length + 1
                        record[i] = line
                    }
                    // not a complete fetch: go on with its last line
                    if (!line.contains(END)) continue
                    return parseRecord(record.requireNoNulls())
                }
                line = input.readLine()
            }
        } catch (e: IOException) {
            log.error("Cannot read AFS log {}", logFile, e)
        }
        close()
        return null
    }

    private fun close() {
        reader?.close()
        reader = null
    }

    private fun parseRecord(lines: Array<String>): Request {
        val time = parseTime(lines[1].substring(0, 24))
        val fileName = lines[0].substringAfterLast(FID)
        val fileSize =
            lines[4]
                .splitFields()
                .last()
                .substringAfterLast(FILE_SIZE)
                .toLong()
        val call = lines[1].splitFields()
        val ip = call[2].substring(call[2].indexOf(HOST) + HOST.length, call[2].lastIndexOf(':'))
        val hostId = call[3].substringAfterLast(HOST_ID).toLong()
        return Request(fileName, time, UserId.of(hostId, ip), fileSize)
    }

    private companion object {
        const val RECORD_LINES = 6
        const val BEGIN = "SRXAFS_FetchData, Fid ="
        const val END = "SRXAFS_FetchData returns"
        const val FID = "Fid = "
        const val FILE_SIZE = "file size "
        const val HOST = "Host "
        const val HOST_ID = "Id "

        val log = logger<AfsLogReader>()

        /** "Nov  8 12:00:00 2026", after the day of the week, which is not checked */
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM ppd HH:mm:ss yyyy", Locale.US)

        /** @param text "Sun Nov  8 12:00:00 2026" in the local time zone */
        fun parseTime(text: String): Long =
            LocalDateTime
                .parse(text.substring(4), TIME_FORMAT)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        /** splits like Java's `String.split(", ")`, which drops trailing empty fields */
        fun String.splitFields(): List<String> = split(", ").dropLastWhile { it.isEmpty() }
    }
}
