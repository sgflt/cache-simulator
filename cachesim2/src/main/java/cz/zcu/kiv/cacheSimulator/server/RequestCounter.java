package cz.zcu.kiv.cacheSimulator.server;

/**
 * @author Lukáš Kvídera
 */
class RequestCounter {
  private int hit;

  int increaseHit() {
    return ++this.hit;
  }

  int getHit() {
    return this.hit;
  }
}