package eu.qwsome.simulator.cache.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Lukáš Kvídera
 */
@Configuration
class FileSourceFactory {
  private static final Logger LOG = LoggerFactory.getLogger(FileSourceFactory.class);

  @Bean
  @Profile("random")
  static FileSource createRandomFileSource(@Value("${simulation.file-source.random-file-source.count-of-files}") final int countOfFiles) {
    LOG.info("createRandomFileSource()");

    return new RandomFileSource(countOfFiles);
  }
}
