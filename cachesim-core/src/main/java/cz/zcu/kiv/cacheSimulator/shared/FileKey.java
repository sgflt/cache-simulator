package cz.zcu.kiv.cacheSimulator.shared;

/**
 * Map key for file names that matches exactly like {@link String#equalsIgnoreCase(String)}.
 * Cache policies use it to look files up in a hash map instead of scanning a list.
 */
public final class FileKey {

  private FileKey() {
  }

  /**
   * @return a key that is equal for two names exactly when the names are equal ignoring case
   */
  public static String of(final String fileName) {
    final int length = fileName.length();
    for (int i = 0; i < length; i++) {
      final char c = fileName.charAt(i);
      if (c >= 0x80 || (c >= 'A' && c <= 'Z')) {
        return fold(fileName);
      }
    }
    // plain lower-case ASCII already is its own key
    return fileName;
  }

  private static String fold(final String fileName) {
    final StringBuilder key = new StringBuilder(fileName.length());
    fileName.codePoints().forEach(cp -> key.appendCodePoint(Character.toLowerCase(Character.toUpperCase(cp))));
    return key.toString();
  }
}
