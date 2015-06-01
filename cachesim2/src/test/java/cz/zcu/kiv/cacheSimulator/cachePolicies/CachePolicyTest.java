package cz.zcu.kiv.cacheSimulator.cachePolicies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 *
 * CachePolicyTest.java
 *    1. 6. 2015
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class CachePolicyTest {

  private static final Logger LOG = LoggerFactory.getLogger(CachePolicyTest.class);

  private static final int DEFAULT_CAPACITY = 8 * 1024 * 1024;
  private static final int DEFAULT_FILE_SIZE = 31337;

  private List<ICache> caches;


  @Before
  public void init() {
    this.caches = new ArrayList<>();
    for (final String cacheName : MainGUI.getInstance().getCachesNames()) {
      try {
        final ICache cache = (ICache) Class.forName("cz.zcu.kiv.cacheSimulator.cachePolicies." + cacheName)
            .newInstance();

        cache.setCapacity(DEFAULT_CAPACITY);
        this.caches.add(cache);
      } catch (final Exception ex) {
        LOG.error("loadCaches", ex);
      }
    }
  }


  @Test
  public void testOfInitialization_ShouldInstantiateAllCaches() {
    assertNotEquals(0, this.caches.size());
    assertEquals(MainGUI.getInstance().getCachesNames().size(), this.caches.size());
  }


  @Test
  public void testInsert_ShouldContainFile() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      assertFalse(cache.toString(), cache.contains(file.getFileName()));
      cache.insertFile(file);
      assertTrue(cache.toString(), cache.contains(file.getFileName()));
    }
  }


  @Test
  public void testInsert_ShouldReturnFile() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      cache.insertFile(file);
      assertEquals(cache.toString(), file, cache.getFile(file.getFileName()));
    }
  }


  @Test
  public void testRemove_ShouldShrinkFreeCapacity() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      cache.insertFile(file);
      assertEquals(cache.toString(), DEFAULT_CAPACITY - DEFAULT_FILE_SIZE, cache.freeCapacity());
    }
  }


  @Test
  public void testRemove_ShouldNotContainFile() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      cache.insertFile(file);
      cache.removeFile(file);
      assertFalse(cache.toString(), cache.contains(file.getFileName()));
    }
  }


  @Test
  public void testRemove_ShouldNotReturnFile() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      cache.insertFile(file);
      cache.removeFile(file);
      assertNull(cache.toString(), cache.getFile(file.getFileName()));
    }
  }


  @Test
  public void testRemove_ShouldExpandFreeCapacity() {
    final FileOnServer fileOnServer = new FileOnServer("testFile", DEFAULT_FILE_SIZE);
    for (final ICache cache : this.caches) {
      final FileOnClient file = new FileOnClient(fileOnServer, cache, 0);
      cache.insertFile(file);
      cache.removeFile(file);
      assertEquals(cache.toString(), cache.getCapacity(), cache.freeCapacity());
    }
  }

}
