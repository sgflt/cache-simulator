package cz.zcu.kiv.cacheSimulator.cli;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliMainTest {

  private final StringWriter out = new StringWriter();
  private final StringWriter err = new StringWriter();

  @AfterEach
  void cleanUp() {
    Server.getInstance().hardReset();
    GlobalVariables.setActualTime(0);
  }

  private int execute(final String... args) {
    final var commandLine = CliMain.commandLine();
    commandLine.setOut(new PrintWriter(this.out, true));
    commandLine.setErr(new PrintWriter(this.err, true));
    return commandLine.execute(args);
  }

  @Test
  void listsPolicies() {
    assertEquals(0, execute("list-policies"));
    assertTrue(this.out.toString().contains("LFU_SS"), this.out.toString());
    assertTrue(this.out.toString().contains("2 Queues"), this.out.toString());
  }

  @Test
  void printsTableForEachSizeAndPolicy() {
    assertEquals(0, execute("run", "-q", "--requests", "2000", "--policies", "lru,FIFO", "--sizes", "8,1"));
    final String text = this.out.toString();
    assertTrue(text.contains("Simulated user - 2,000 requests"), text);
    assertTrue(text.contains("Read Hit Ratio [%]"), text);
    assertTrue(text.contains("Saved Bytes Ratio [%]"), text);
    // sizes are sorted
    assertTrue(text.matches("(?s).*Policy\\s+1 MB\\s+8 MB.*"), text);
    assertTrue(text.matches("(?s).*\\nLRU\\s+\\d.*"), text);
    assertTrue(text.matches("(?s).*\\nFIFO\\s+\\d.*"), text);
  }

  @Test
  void writesCsvToStdoutAndXlsToFile(@TempDir final Path dir) {
    assertEquals(0, execute("run", "-q", "-n", "2000", "-p", "LRU", "-c", "1,8", "-f", "csv"), this.err.toString());
    assertTrue(this.out.toString().contains("Total requested files;2000"), this.out.toString());

    final Path xls = dir.resolve("results.xls");
    assertEquals(0, execute("run", "-q", "-n", "2000", "-p", "LRU", "-c", "1", "-f", "xls", "-o", xls.toString()));
    assertTrue(Files.isRegularFile(xls));
  }

  @Test
  void rejectsBadArguments() {
    assertEquals(2, execute("run", "--policies", "NOPE"));
    assertTrue(this.err.toString().contains("Unknown cache policy 'NOPE'"), this.err.toString());

    assertEquals(2, execute("run", "--source", "afs-log"));
    assertTrue(this.err.toString().contains("needs --log"), this.err.toString());

    assertEquals(2, execute("run", "--source", "nope"));
    assertEquals(2, execute("run", "-f", "xls"));
  }
}
