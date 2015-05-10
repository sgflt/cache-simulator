package cz.zcu.kiv.cacheSimulator.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.MQ;

/**
 * @author Lukáš Kvídera
 * @version 0.0
 *          10. 5. 2015
 */
public class FileOnServerTest {

  @Test
  public void test_IncreaseReadHit_ShouldBeIncreased() {
    final FileOnServer fileOnServer = new FileOnServer("default", 100);
    final ICache cache = new LFU_SS();

    fileOnServer.increaseReadHit(cache);
    assertEquals(1, fileOnServer.getReadHit(cache));

    fileOnServer.increaseReadHit(cache);
    assertEquals(2, fileOnServer.getReadHit(cache));
  }


  @Test
  public void test_IncreaseReadHitTwoCaches_ShouldBeIncreasedForEachCache() {
    final FileOnServer fileOnServer = new FileOnServer("default", 100);
    ICache cache = new LFU_SS();

    fileOnServer.increaseReadHit(cache);
    assertEquals(1, fileOnServer.getReadHit(cache));

    cache = new MQ();
    fileOnServer.increaseReadHit(cache);
    assertEquals(1, fileOnServer.getReadHit(cache));
  }


  @Test
  public void test_UpdateFile_ShouldIncreaseVersion() {
    final FileOnServer fileOnServer = new FileOnServer("default", 100);

    fileOnServer.updateFile(600);
    assertEquals(2, fileOnServer.getVersion());
  }


  @Test
  public void test_UpdateFile_ShouldSetSize() {
    final FileOnServer fileOnServer = new FileOnServer("default", 100);
    final int size = 600;

    fileOnServer.updateFile(size);
    assertEquals(size, fileOnServer.getFileSize());
  }

}
