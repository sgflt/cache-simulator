package eu.qwsome.simulator.cache.core;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Lukáš Kvídera
 */
public abstract class SimulationCacheStub implements Cache<String, FileTraffic> {

  /**
   * @return count of bytes
   */
  public abstract long getCapacity();

  @Override
  public Map<String, FileTraffic> getAll(final Set<? extends String> keys) {
    return null;
  }

  @Override
  public boolean containsKey(final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void loadAll(final Set<? extends String> keys, final boolean replaceExistingValues, final CompletionListener completionListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileTraffic getAndPut(final String key, final FileTraffic value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(final Map<? extends String, ? extends FileTraffic> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean putIfAbsent(final String key, final FileTraffic value) {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean remove(final String key, final FileTraffic oldValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileTraffic getAndRemove(final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean replace(final String key, final FileTraffic oldValue, final FileTraffic newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(final String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean replace(final String key, final FileTraffic value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FileTraffic getAndReplace(final String key, final FileTraffic value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAll(final Set<? extends String> keys) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <C extends Configuration<String, FileTraffic>> C getConfiguration(final Class<C> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invoke(final String key, final EntryProcessor<String, FileTraffic, T> entryProcessor, final Object... arguments) throws EntryProcessorException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Map<String, EntryProcessorResult<T>> invokeAll(final Set<? extends String> keys, final EntryProcessor<String, FileTraffic, T> entryProcessor, final Object... arguments) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CacheManager getCacheManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
  }

  @Override
  public boolean isClosed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T unwrap(final Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void registerCacheEntryListener(final CacheEntryListenerConfiguration<String, FileTraffic> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<String, FileTraffic> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Entry<String, FileTraffic>> iterator() {
    throw new UnsupportedOperationException();
  }
}
