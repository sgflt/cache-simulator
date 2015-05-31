/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

/**
 *
 * @author Pavel Bzoch Trida pro vystupy ze simulatoru
 *
 */
public class Output {

  /**
   * metoda pro tisk vsech statistik souhrne do konzole
   *
   * @param userID
   *            id uzivatele
   * @param cachesResults
   *            vysledky cachovacich algoritmu
   */
  public static void printAllStatConsole(final ArrayList<UserStatistics> stat) {
    if (stat == null || stat.isEmpty()) {
      return;
    }
    for (final UserStatistics user : stat) {
      if (user.getUserID() == 0) {
        System.out
            .println("\n=================== Statistics for simulated user ===================\n");
      } else {
        final long id = user.getUserID() >> 32;
        final String ip = (GlobalMethods.intToIp(user.getUserID()
            - (id << 32)));
        System.out
            .println("\n=================== Statistics for user id: "
                + id + ", ip: " + ip + " ===================\n");
      }

      System.out.println("Total network traffic without cache;"
          + user.getTotalNetworkBandwidth());
      System.out.println("Total requested files;"
          + user.getFileAccessed());

      // vytisteni read hit ratio
      System.out.print("\nRead hit ratio;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();

      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (int i = 0; i < user.getCacheHitRatios(cacheName).length; i++) {
          System.out
              .print(user.getCacheHitRatios(cacheName)[i] + ";");
        }
        System.out.print("\n");
      }

      // vytisteni read hits
      System.out.print("\nRead hits;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();
      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (final long pom : user.getCacheHits(cacheName)) {
          System.out.print(pom + ";");
        }
        System.out.print("\n");
      }

      // vytisteni saved bytes ratio
      System.out.print("\nSaved bytes ratio;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();
      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (final double pom : user.getCacheSavedBytesRatio(cacheName)) {
          System.out.print(pom + ";");
        }
        System.out.print("\n");
      }

      // vytisteni saved bytes
      System.out.print("\nSaved bytes;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();
      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (final long pom : user.getSavedBytes(cacheName)) {
          System.out.print(pom + ";");
        }
        System.out.print("\n");
      }

      // vytisteni traffic transfer decrease ratio
      System.out.print("\nTraffic Transfer Decrease Ratio;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();
      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (final double pom : user.getDataTransferDegreaseRatio(cacheName)) {
          System.out.print(pom + ";");
        }
        System.out.print("\n");
      }

      // vytisteni traffic transfer decrease
      System.out.print("\nTraffic Transfer Decrease;Cache size");
      System.out.print("\nCaching policy;");
      for (int i = 0; i < user.getCacheSizes().length; i++) {
        System.out.print(user.getCacheSizes()[i] + "MB;");
      }
      System.out.println();
      for (final String cacheName : user.getCacheNames()) {
        System.out.print(cacheName + ";");
        for (final long pom : user.getDataTransferDegrease(cacheName)) {
          System.out.print(pom + ";");
        }
        System.out.print("\n");
      }
    }
  }

  /**
   * metoda pro ulozeni statistik do CSV souboru
   *
   * @param fName
   *            jmeno soubor, kam ulozit
   */
  public static boolean saveStatToCSV(String fName,
      final ArrayList<UserStatistics> stat) {
    if (stat == null || stat.isEmpty()) {
      return false;
    }
    if (!fName.endsWith(".csv")) {
      fName = fName + ".csv";
    }
    final File file = new File(fName);
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      try (final BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
        for (final UserStatistics user : stat) {
          // vytisteni hlavicky
          if (user.getUserID() == 0) {
            bf.newLine();
            bf.write("\nSimulated user");
            bf.newLine();
          } else {
            bf.newLine();
            final long id = user.getUserID() >> 32;
            final String ip = (GlobalMethods.intToIp(user.getUserID()
                - (id << 32)));
            bf.write("User id:" + id + "; ip: " + ip);
            bf.newLine();
          }

          // vytisteni globalnich statistik uzivatele
          bf.write("\nTotal network traffic without cache;"
              + (user.getTotalNetworkBandwidth() / 1024 / 1024)
              + "MB");
          bf.write("\nTotal requested files;" + user.getFileAccessed());

          // vztisteni statistik pro jednotlive cache policy
          writeReadHitRatioToCSV(bf, user);
          writeReadHitsToCSV(bf, user);
          writeSavedByteRatioToCSV(bf, user);
          writeSavedBytesToCSV(bf, user);
          writeTransferDecreaseRatioToCSV(bf, user);
          writeTransferDecreaseToCSV(bf, user);

        }
      }
      return true;

    } catch (final IOException ex) {
      return false;
    }
  }

  /**
   * metoda pro vztisteni statistik read hits
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeReadHitsToCSV(final BufferedWriter bf, final UserStatistics sc)
      throws IOException {
    // vytisteni read hits
    bf.write("\nRead hits;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();
    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (final long pom : sc.getCacheHits(cacheName)) {
        bf.write(pom + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro vytisteni statistik read hit ratio
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeReadHitRatioToCSV(final BufferedWriter bf,
      final UserStatistics sc) throws IOException {
    // vytisteni read hit ratio
    bf.write("\n\nRead hit ratio;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();

    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (int i = 0; i < sc.getCacheHitRatios(cacheName).length; i++) {
        bf.write(sc.getCacheHitRatios(cacheName)[i] + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro vytisteni statistik saved bytes ratio
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeSavedByteRatioToCSV(final BufferedWriter bf,
      final UserStatistics sc) throws IOException {
    // vytisteni saved bytes ratio
    bf.write("\nSaved bytes ratio;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();
    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (final double pom : sc.getCacheSavedBytesRatio(cacheName)) {
        bf.write(pom + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro vytisteni statistik saved bytes
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeSavedBytesToCSV(final BufferedWriter bf,
      final UserStatistics sc) throws IOException {
    // vytisteni saved bytes
    bf.write("\nSaved bytes;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();
    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (final long pom : sc.getSavedBytes(cacheName)) {
        bf.write(pom + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro vytisteni statistik transfer decrease ratio
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeTransferDecreaseRatioToCSV(final BufferedWriter bf,
      final UserStatistics sc) throws IOException {
    // vytisteni data traffic decrease ratio
    bf.write("\nData transfer decrease ratio;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();
    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (final double pom : sc.getDataTransferDegreaseRatio(cacheName)) {
        bf.write(pom + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro vytisteni statistik transfer decrease
   *
   * @param bf
   *            proud, kam se pise
   * @param sc
   *            statistiky
   * @throws IOException
   *             mozne vyjimky pri zapisu
   */
  private static void writeTransferDecreaseToCSV(final BufferedWriter bf,
      final UserStatistics sc) throws IOException {
    // vytisteni data traffic decrease
    bf.write("\nData transfer decrease;Cache size");
    bf.write("\nCaching policy;");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      bf.write(sc.getCacheSizes()[i] + "MB;");
    }
    bf.newLine();
    for (final String cacheName : sc.getCacheNames()) {
      bf.write(cacheName + ";");
      for (final long pom : sc.getDataTransferDegrease(cacheName)) {
        bf.write(pom + ";");
      }
      bf.write("\n");
    }
  }

  /**
   * metoda pro ulozeni vysledku do xls (excelovska tabulka)
   *
   * @param fName
   *            jmeno souboru
   * @param stat
   *            ukladane statistiky
   * @return true, pokud vse probehlo v poradku
   */
  public static boolean saveStatToXLS(final String fName,
      final ArrayList<UserStatistics> stat) {
    try (Workbook wb = new HSSFWorkbook()) {
      // Workbook wb = new XSSFWorkbook();

      for (final UserStatistics user : stat) {

        int lineCounter = 0;

        Sheet sheet;

        // nastaveni jmena listu
        if (user.getUserID() == 0) {
          sheet = wb.createSheet("Simulated user");
        } else {
          final long id = user.getUserID() >> 32;
          final String ip = (GlobalMethods.intToIp(user.getUserID()
              - (id << 32)));
          String sheetName = "ID " + id + "; ip " + ip;
          sheet = wb.getSheet(sheetName);

          int pom = 1;
          while (sheet != null) {
            sheetName = sheetName + ", " + pom;
            sheet = wb.getSheet(sheetName);
            pom++;
          }
          sheet = wb.createSheet(sheetName);
        }

        // zapis globalnich statistik simulace
        lineCounter++;
        Row row = sheet.createRow(lineCounter);

        row.createCell(0).setCellValue(
            "Total network traffic without cache");
        row.createCell(1).setCellValue(
            user.getTotalNetworkBandwidth() / 1024 / 1024 + "MB");

        lineCounter++;
        row = sheet.createRow(lineCounter);
        row.createCell(0).setCellValue("Total requested files");
        row.createCell(1).setCellValue(user.getFileAccessed());
        lineCounter++;
        lineCounter++;

        // vytvoreni tucneho fontu
        final Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        final CellStyle style = wb.createCellStyle();
        style.setFont(font);

        // zapis statistik o cache policies
        lineCounter = writeReadHitRatioToXLS(lineCounter, sheet, user,
            style) + 1;
        lineCounter = writeReadHitsXLS(++lineCounter, sheet, user,
            style) + 1;
        lineCounter = writeSavedBytesRatioXLS(++lineCounter, sheet,
            user, style) + 1;
        lineCounter = writeSavedBytesXLS(++lineCounter, sheet, user,
            style) + 1;
        lineCounter = writeTransferDecreaseRatioToXLS(++lineCounter,
            sheet, user, style) + 1;
        writeTransferDecreaseToXLS(++lineCounter, sheet, user, style);
      }

      // Write the output to a file
      try (final FileOutputStream fileOut = new FileOutputStream(fName)) {
        wb.write(fileOut);
      }
      return true;
    } catch (final IOException ex) {
      return false;
    }
  }

  /**
   * metoda pro zapis statistik read hit ratio do excelovske tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeReadHitRatioToXLS(int lineCounter, final Sheet sheet,
      final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;

    // zapis nadpisu tabulky
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Read Hit Ratio / Caching Policy");
    cell.setCellStyle(boldStyle);

    // zapis velikosti cache retezce
    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);
    // zapisujeme do sloucene bunky
    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    // zapis velikosti u cache ciselne
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    // zapis statistik
    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getCacheHitRatios(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getCacheHitRatios(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro zapis statistik read hits do excelovske tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeReadHitsXLS(int lineCounter, final Sheet sheet,
      final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Read Hits / Caching Policy");
    cell.setCellStyle(boldStyle);

    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getCacheHits(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getCacheHits(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro zapis statistik saved bytes ratio do excelovske tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeSavedBytesRatioXLS(int lineCounter, final Sheet sheet,
      final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Saved Bytes Ratio / Caching Policy");
    cell.setCellStyle(boldStyle);

    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getCacheSavedBytesRatio(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getCacheSavedBytesRatio(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro zapis statistik saved bytes do excelovske tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeSavedBytesXLS(int lineCounter, final Sheet sheet,
      final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Saved Bytes / Caching Policy");
    cell.setCellStyle(boldStyle);

    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getSavedBytes(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getSavedBytes(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro zapis statistik data transfer decrease ratio do excelovske
   * tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeTransferDecreaseRatioToXLS(int lineCounter,
      final Sheet sheet, final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Data Transfer Decrease Ratio / Caching Policy");
    cell.setCellStyle(boldStyle);

    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getDataTransferDegreaseRatio(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getDataTransferDegreaseRatio(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro zapis statistik data transfer decrease do excelovske tabulky
   *
   * @param lineCounter
   *            ukazatel, na ktere radce v tabulce jsme
   * @param sheet
   *            list, kam zapisujeme
   * @param sc
   *            statistiky pro zapis
   * @param boldStyle
   *            tucny stzl pro nadpisy
   * @return aktualni radka
   */
  private static int writeTransferDecreaseToXLS(int lineCounter, final Sheet sheet,
      final UserStatistics sc, final CellStyle boldStyle) {
    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(columnCounter++);
    cell.setCellValue("Data Transfer Decrease / Caching Policy");
    cell.setCellStyle(boldStyle);

    cell = row.createCell(columnCounter);
    boldStyle.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue("Cache Size [MB]");
    cell.setCellStyle(boldStyle);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + sc.getCacheSizes().length - 1));

    lineCounter++;
    columnCounter = 0;

    row = sheet.createRow(lineCounter);
    row.createCell(columnCounter++).setCellValue("Caching policy");
    for (int i = 0; i < sc.getCacheSizes().length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(sc.getCacheSizes()[i]);
      cell.setCellStyle(boldStyle);
    }

    for (final String cache : sc.getCacheNames()) {
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      row.createCell(columnCounter++).setCellValue(cache);
      for (int i = 0; i < sc.getDataTransferDegrease(cache).length; i++) {
        row.createCell(columnCounter++).setCellValue(
            sc.getDataTransferDegrease(cache)[i]);
      }
    }
    return lineCounter;
  }

  /**
   * metoda pro vytisteni vysledku mereni consistency do konzole
   *
   * @param cons
   *            namerena data
   * @param cacheClass
   *            pole se jmeny trid cache policies
   * @param cacheNames
   *            pole se jmeny cache policies
   * @param userIDs
   *            pole s user ID
   */
  public static void printAllStatConsoleConsistency(
      final IConsistencySimulation cons, final String[] cacheClass,
      final String[] cacheNames, final long[] userIDs) {
    if (userIDs.length == 0 || cons == null) {
      return;
    }
    final String consContName = cons.getInfo().split(";")[1];
    System.out
        .println("\n======= Consistency Control Statistics for "+consContName +" =======\n");

    for (final long userID : userIDs) {
      for (int i = 0; i < cacheNames.length; i++) {
        if (userID == 0) {
          System.out
              .println("\n=================== Statistics for simulated user ===================\n");
        } else {
          final long id = userID >> 32;
          final String ip = (GlobalMethods.intToIp(userID - (id << 32)));
          System.out
              .println("\n=================== Statistics for user ID: "
                  + id
                  + ", ip: "
                  + ip
                  + ", cache: "
                  + cacheNames[i] + " ===================\n");
        }
        for (final String header : cons.getHeaders()) {
          System.out.print(header + ";");
        }
        System.out.println();
        final Object[][] data = cons.getData(cacheClass[i], userID);
        if (data != null)
          for (int j = 0; j < data.length; j++) {
            for (int k = 0; k < data[j].length; k++) {
              System.out.print(data[j][k] + ";");
            }
            System.out.println();
          }
        else{
          System.out.println("No data for this user!\n");
        }
      }
    }

  }

  /**
   * metoda pro zapis vysledku consistency simulace do XLS
   *
   * @param cons
   *            namerena data
   * @param cacheClass
   *            pole se jmeny trid cache policies
   * @param cacheNames
   *            pole se jmeny cache policies
   * @param userIDs
   *            pole s user ID
   * @param fName
   *            jmeno souboru, kam se ma zapisovat
     * @return true, pokud vse probehlo v poradku
   */
  public static boolean saveConsistencyControlXLS(
      final IConsistencySimulation cons, final String[] cacheClass,
      final String[] cacheNames, final long[] userIDs, final String fName) {
    if (userIDs.length == 0 || cons == null) {
      return false;
    }
    try (final Workbook wb = new HSSFWorkbook()) {
      for (final long userID : userIDs) {

        int lineCounter = 0;

        Sheet sheet;

        // nastaveni jmena listu
        if (userID == 0) {
          sheet = wb.createSheet("Simulated user");
        } else {
          final long id = userID >> 32;
          final String ip = (GlobalMethods.intToIp(userID
              - (id << 32)));
          String sheetName = "ID " + id + "; ip " + ip;
          sheet = wb.getSheet(sheetName);

          int pom = 1;
          while (sheet != null) {
            sheetName = sheetName + ", " + pom;
            sheet = wb.getSheet(sheetName);
            pom++;
          }
          sheet = wb.createSheet(sheetName);
        }

        // vytvoreni tucneho fontu
        final Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        final CellStyle style = wb.createCellStyle();
        style.setFont(font);


        // zapis globalnich statistik simulace
        lineCounter++;
        final Row row = sheet.createRow(lineCounter);
        final String consContName = cons.getInfo().split(";")[1];
        style.setAlignment(CellStyle.ALIGN_CENTER);

        final Cell cell = row.createCell(0);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        cell.setCellValue("Statistics for " + consContName);
        cell.setCellStyle(style);

        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
            lineCounter, // first row (0-based)
            lineCounter, // last row (0-based)
            0, // first column (0-based)
            cons.getHeaders().length));

        lineCounter++;
        lineCounter++;

        for (int i = 0; i < cacheNames.length; i++) {

          final Object[][] data = cons.getData(cacheClass[i], userID);
          lineCounter = writeConStatToXLS(sheet, cacheNames[i], cons.getHeaders(), data, lineCounter, style) + 1;

        }
      }
      // Write the output to a file
      try (final FileOutputStream fileOut = new FileOutputStream(fName)) {
        wb.write(fileOut);
      }
      return true;
    } catch (final IOException ex) {
      return false;
    }
  }

  /**
   * metoda pro zapis jednech vysledku consistency control
   * @param sheet jmeno sesit
   * @param cacheName jmeno cache
   * @param headers jmena nadpisu
   * @param data data pro zapis
   * @param lineCounter pocitadlo radek
   * @param style tucny font pro zapis
   * @return cislo aktualni radky
   */
  private static int writeConStatToXLS(final Sheet sheet, final String cacheName,
      final String[] headers, final Object[][] data, int lineCounter, final CellStyle style) {

    if (data == null){
      final Row row = sheet.createRow(lineCounter);
      final Cell cell = row.createCell(1);
      cell.setCellValue("No data for this combination of user & cache & cache capacity!");
      cell.setCellStyle(style);
      return lineCounter + 1;
    }

    int columnCounter = 0;
    Row row = sheet.createRow(lineCounter);
    Cell cell = row.createCell(1);

    cell = row.createCell(columnCounter);
    style.setAlignment(CellStyle.ALIGN_CENTER);
    cell.setCellValue(cacheName);
    cell.setCellStyle(style);

    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
        lineCounter, // first row (0-based)
        lineCounter, // last row (0-based)
        columnCounter, // first column (0-based)
        columnCounter + headers.length));

    lineCounter++;
    columnCounter = 0;

    //zapis hlavicek sloupecku
    row = sheet.createRow(lineCounter);
    for (int i = 0; i < headers.length; i++) {
      cell = row.createCell(columnCounter++);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(style);
    }

    //zapis jednotlivych radek s daty
    for(int i = 0; i < data.length; i++){
      lineCounter++;
      columnCounter = 0;
      row = sheet.createRow(lineCounter);
      for (int j = 0; j < data[i].length;j++){
        row.createCell(columnCounter++).setCellValue(data[i][j].toString());
      }
    }
    return lineCounter + 1;
  }

  /**
   * metoda pro zapis vysledku consistency simulace do CSV
   *
   * @param cons
   *            namerena data
   * @param cacheClass
   *            pole se jmeny trid cache policies
   * @param cacheNames
   *            pole se jmeny cache policies
   * @param userIDs
   *            pole s user ID
   * @param fName
   *            jmeno souboru, kam se ma zapisovat
     * @return true, pokud vse probehlo v poradku
   */
  public static boolean saveConsistencyControlCSV(
      final IConsistencySimulation cons, final String[] cacheClass,
      final String[] cacheNames, final long[] userIDs, String fName) {
    if (userIDs.length == 0 || cons == null) {
      return false;
    }
    if (!fName.endsWith(".csv")) {
      fName = fName + ".csv";
    }
    final File file = new File(fName);
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      try (final BufferedWriter bf = new BufferedWriter(new FileWriter(file))) {
        final String consContName = cons.getInfo().split(";")[1];
        bf.write("\nConsistency Control Statistics for "+consContName+"\n");

        for (final long userID : userIDs) {
          for (int i = 0; i < cacheNames.length; i++) {
            if (userID == 0) {
              bf.write("\nSimulated user\n");
            } else {
              final long id = userID >> 32;
              final String ip = (GlobalMethods.intToIp(userID - (id << 32)));
              bf.write("\nUser ID: " + id + ", ip: " + ip
                  + ", cache: " + cacheNames[i] + "\n");
            }
            for (final String header : cons.getHeaders()) {
              bf.write(header + ";");
            }
            bf.newLine();
            final Object[][] data = cons.getData(cacheClass[i], userID);
            if (data != null)
              for (int j = 0; j < data.length; j++) {
                for (int k = 0; k < data[j].length; k++) {
                  bf.write(data[j][k] + ";");
                }
                bf.newLine();
            }
            else
              bf.write("No data for this user\n");
          }
        }
      }
      return true;

    } catch (final IOException ex) {
      return false;
    }
  }
}
