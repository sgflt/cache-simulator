package cz.zcu.kiv.cachesimulator.cli

import org.junit.jupiter.api.io.TempDir
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliMainTest {
    private val out = StringWriter()
    private val err = StringWriter()

    private fun execute(vararg args: String): Int =
        commandLine()
            .setOut(PrintWriter(out, true))
            .setErr(PrintWriter(err, true))
            .execute(*args)

    @Test
    fun `lists policies`() {
        assertEquals(0, execute("list-policies"))
        assertContains(out.toString(), "LFU_SS")
        assertContains(out.toString(), "2 Queues")
    }

    @Test
    fun `prints a table for each size and policy`() {
        assertEquals(0, execute("run", "-q", "--requests", "2000", "--policies", "lru,FIFO", "--sizes", "8,1"))
        val text = out.toString()
        assertContains(text, "Simulated user - 2,000 requests")
        assertContains(text, "Read Hit Ratio [%]")
        assertContains(text, "Saved Bytes Ratio [%]")
        assertContains(text, Regex("""Policy\s+1 MB\s+8 MB"""), "sizes are sorted")
        assertContains(text, Regex("""\nLRU\s+\d"""))
        assertContains(text, Regex("""\nFIFO\s+\d"""))
    }

    @Test
    fun `writes CSV to stdout and XLS to a file`(
        @TempDir dir: Path,
    ) {
        assertEquals(0, execute("run", "-q", "-n", "2000", "-p", "LRU", "-c", "1,8", "-f", "csv"), err.toString())
        assertContains(out.toString(), "Total requested files;2000")

        val xls = dir.resolve("results.xls")
        assertEquals(0, execute("run", "-q", "-n", "2000", "-p", "LRU", "-c", "1", "-f", "xls", "-o", xls.toString()))
        assertTrue(xls.isRegularFile())
    }

    @Test
    fun `rejects bad arguments`() {
        assertEquals(2, execute("run", "--policies", "NOPE"))
        assertContains(err.toString(), "Unknown cache policy 'NOPE'")

        assertEquals(2, execute("run", "--source", "afs-log"))
        assertContains(err.toString(), "needs --log")

        assertEquals(2, execute("run", "--source", "nope"))
        assertEquals(2, execute("run", "-f", "xls"))
    }

    @Test
    fun `policies resolve by id or name, without duplicates`() {
        assertEquals(
            listOf("LRU", "ARC", "LIRS", "_2Q", "LFU_SS", "FIFO_2ndChance"),
            Policies.resolve(listOf("lru", "arc", "lirs", "2q", "lfu-ss", "LRU", "FIFO 2nd chance")).map { it.id },
        )
    }
}
