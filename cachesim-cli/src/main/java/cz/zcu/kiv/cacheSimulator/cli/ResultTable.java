package cz.zcu.kiv.cacheSimulator.cli;

import cz.zcu.kiv.cacheSimulator.simulation.ResultMetric;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Formats results as a plain-text table: one row per policy, one column per cache size.
 */
public final class ResultTable {

  private ResultTable() {
  }

  public static String header(final UserStatistics stats) {
    return "%s - %s requests, %s MB without cache".formatted(
      stats.getUserLabel(),
      format(stats.getFileAccessed()),
      format(stats.getTotalNetworkBandwidth() / 1024 / 1024));
  }

  /**
   * @return rows of cells; the first row is the header
   */
  public static List<List<String>> cells(final UserStatistics stats, final ResultMetric metric) {
    final List<List<String>> rows = new ArrayList<>();
    final List<String> header = new ArrayList<>();
    header.add("Policy");
    for (final Integer size : stats.getCacheSizes()) {
      header.add(size + " MB");
    }
    rows.add(header);
    for (final String cacheName : stats.getCacheNames()) {
      final List<String> row = new ArrayList<>();
      row.add(cacheName);
      for (final Number value : metric.values(stats, cacheName)) {
        row.add(value == null ? "" : format(value));
      }
      rows.add(row);
    }
    return rows;
  }

  public static String render(final UserStatistics stats, final ResultMetric metric) {
    final List<List<String>> rows = cells(stats, metric);
    final int columns = rows.getFirst().size();
    final int[] widths = new int[columns];
    for (final List<String> row : rows) {
      for (int i = 0; i < columns; i++) {
        widths[i] = Math.max(widths[i], row.get(i).length());
      }
    }
    final StringBuilder out = new StringBuilder(metric.label()).append('\n');
    for (int r = 0; r < rows.size(); r++) {
      final List<String> row = rows.get(r);
      for (int i = 0; i < columns; i++) {
        if (i == 0) {
          out.append(pad(row.get(i), widths[i], false));
        } else {
          out.append("  ").append(pad(row.get(i), widths[i], true));
        }
      }
      out.append('\n');
      if (r == 0) {
        int total = 0;
        for (final int width : widths) {
          total += width;
        }
        out.append("-".repeat(total + 2 * (columns - 1))).append('\n');
      }
    }
    return out.toString();
  }

  public static String format(final Number value) {
    final DecimalFormat format = (value instanceof Double || value instanceof Float)
      ? new DecimalFormat("#,##0.00")
      : new DecimalFormat("#,##0");
    return format.format(value);
  }

  private static String pad(final String text, final int width, final boolean right) {
    final String space = " ".repeat(width - text.length());
    return right ? space + text : text + space;
  }
}
