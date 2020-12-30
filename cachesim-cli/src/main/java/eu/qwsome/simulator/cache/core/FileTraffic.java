package eu.qwsome.simulator.cache.core;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author Lukáš Kvídera
 */
public class FileTraffic {
  private final String fileName;
  private final long fileSize;

  FileTraffic(final String fileName, final long fileSize) {
    this.fileName = Objects.requireNonNull(fileName);
    this.fileSize = fileSize;
  }

  public String getFileName() {
    return this.fileName;
  }

  public long getFileSize() {
    return this.fileSize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.fileName);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FileTraffic that = (FileTraffic) o;
    return Objects.equals(this.fileName, that.fileName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("fileName", this.fileName)
      .append("fileSize", this.fileSize)
      .toString();
  }
}
