package cz.zcu.kiv.cacheSimulator.server;

import org.junit.Before;
import org.junit.Test;

import cz.zcu.kiv.cacheSimulator.server.Server;

/**
 * ServerTest.java
 * 1. 6. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class ServerTest {

  private static Server server = Server.getInstance();


  @Before
  public void reset() {
    server.hardReset();
  }


  @Test
  public void test() {
    // fail("Not yet implemented");
  }

}
