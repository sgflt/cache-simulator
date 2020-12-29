package eu.qwsome.simulator.cache;

import eu.qwsome.simulator.cache.core.SimulationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author Lukáš Kvídera
 */
@SpringBootApplication
public class CacheSimulatorCliApplication implements CommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(CacheSimulatorCliApplication.class);

  private final ApplicationContext applicationContext;

  CacheSimulatorCliApplication(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public static void main(final String[] args) {
    SpringApplication.run(CacheSimulatorCliApplication.class, args);
  }

  @Override
  public void run(final String... args) throws Exception {
    LOG.info("Running...");
    this.applicationContext.getBean(SimulationEngine.class).run();
  }
}