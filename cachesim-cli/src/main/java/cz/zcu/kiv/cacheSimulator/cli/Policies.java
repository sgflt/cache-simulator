package cz.zcu.kiv.cacheSimulator.cli;

import cz.zcu.kiv.cacheSimulator.shared.CacheLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The cache policies available to simulate.
 */
public final class Policies {

  /**
   * @param className   simple class name, what {@link cz.zcu.kiv.cacheSimulator.simulation.SimulationConfig} expects
   * @param displayName name shown to people
   */
  public record Policy(String className, String displayName) {
    @Override
    public String toString() {
      return this.displayName;
    }
  }

  private Policies() {
  }

  public static List<Policy> available() {
    return CacheLoader.loadAvailableCaches().stream()
      .map(info -> info.split(";", 2))
      .map(parts -> new Policy(parts[0], parts.length > 1 ? parts[1] : parts[0]))
      .toList();
  }

  /**
   * Turns names typed by the user into class names. Accepts class names ({@code LFU_SS}) and
   * display names ({@code LFU-SS}), ignoring case, or {@code all}.
   *
   * @throws IllegalArgumentException for an unknown name
   */
  public static List<String> resolve(final List<String> names) {
    final List<Policy> available = available();
    if (names.isEmpty() || names.stream().anyMatch(n -> n.equalsIgnoreCase("all"))) {
      return available.stream().map(Policy::className).toList();
    }
    final List<String> resolved = new ArrayList<>();
    for (final String name : names) {
      final String key = normalize(name);
      final Policy policy = available.stream()
        .filter(p -> normalize(p.className()).equals(key) || normalize(p.displayName()).equals(key))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
          "Unknown cache policy '" + name + "'. Run 'list-policies' to see them all."));
      if (!resolved.contains(policy.className())) {
        resolved.add(policy.className());
      }
    }
    return resolved;
  }

  private static String normalize(final String name) {
    return name.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_').replaceFirst("^_+", "");
  }
}
