package cz.zcu.kiv.cachesimulator.request

import cz.zcu.kiv.cachesimulator.simulation.UserId
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.Date
import kotlin.io.path.writeLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AfsLogReaderTest {
    @Test
    fun `reads fetches and skips incomplete ones`(
        @TempDir dir: Path,
    ) {
        val log = dir.resolve("afs.log")
        log.writeLines(
            fetch("Mon Jan  5 09:15:30 2009", "536870913.7.1", "192.168.1.7", 3, 4096) +
                // a fetch without its "returns" line: the reader skips its six lines
                listOf("Mon Jan  5 09:15:31 2009 SRXAFS_FetchData, Fid = 1.1.1") + List(5) { "garbage $it" } +
                fetch("Tue Feb 17 23:59:59 2009", "536870913.8.1", "10.0.0.1", 42, 2048),
        )
        val reader = AfsLogReader(log)

        val first = reader.next()
        val second = reader.next()

        @Suppress("DEPRECATION") // the old reader used Date.parse; the times must not change
        val expectedTime = Date.parse("Mon Jan  5 09:15:30 2009")
        assertEquals(Request("536870913.7.1", expectedTime, UserId.of(3, "192.168.1.7"), 4096), first)
        assertEquals("536870913.8.1", second?.fileName)
        assertNull(reader.next())

        reader.reset()
        assertEquals(first, reader.next())
    }

    private fun fetch(
        time: String,
        fid: String,
        ip: String,
        hostId: Int,
        size: Long,
    ) = listOf(
        "$time SRXAFS_FetchData, Fid = $fid",
        "$time FetchData, call, Host $ip:7001, Id $hostId",
        "$time FetchData_RXStyle",
        "$time FetchData_RXStyle position 0",
        "$time FetchData_RXStyle, Pos 0, Len $size, file size $size",
        "$time SRXAFS_FetchData returns 0",
    )
}
