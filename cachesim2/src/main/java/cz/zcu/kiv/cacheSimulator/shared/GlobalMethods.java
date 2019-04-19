package cz.zcu.kiv.cacheSimulator.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;


/**
 * trida jen se statickymi metodami
 *
 * @author Pavel Bzoch
 */
public class GlobalMethods {

  /**
   * metoda pro konverzi ip adresy na cislo
   *
   * @param ip ip adresa jako retezec
   * @return ip adresa jako 32bit cislo
   */
  public static long ipToInt(final String ip) {
    final String[] ips = ip.split("\\.");
    long result = 0;
    for (final String str : ips) {
      result = result << 8 | ((Integer.valueOf(str)) & 0xFF);
    }
    return result;
  }

  /**
   * metoda pro prevod int > String
   *
   * @param cislo cislo pro prevod
   * @return retezec prezentujici IP adresu
   */
  public static String intToIp(final long cislo) {
    final StringBuilder ret = new StringBuilder();
    final String hex = Integer.toHexString((int) cislo);
    if (hex.length() != 8) {
      return hex;
    }
    for (int i = 0; i < 4; i++) {
      ret.append(Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16));
      if (i < 3) {
        ret.append(".");
      }
    }
    return ret.toString();
  }

  /**
   * metoda pro nastaveni nahodnych generatoru cisel
   */
  public static void setGenerators() {
    // nastaveni nahodnych generatoru

    //nsateveni gaussovskeho nahodneho generatoru
    GaussianFileNameGenerator.setDispersion(GlobalVariables
      .getFileRequestGeneratorDispersion());
    GaussianFileNameGenerator.setMeanValue(GlobalVariables
      .getFileRequestGeneratorMeanValue());
    GaussianFileNameGenerator.setMinValue(GlobalVariables
      .getFileRequestGeneratorMinValue());
    GaussianFileNameGenerator.setMaxValue(GlobalVariables
      .getFileRequestGeneratorMaxValue());

    //nastaveni nahodneho generatoru
    RandomFileNameGenerator.setMaxValue(GlobalVariables
      .getFileRequestGeneratorMaxValue());
    RandomFileNameGenerator.setMinValue(GlobalVariables
      .getFileRequestGeneratorMinValue());

    //nastaveni nahodneho generatoru s preferencemi
    RandomFileNameGeneratorWithPrefences.setMaxValue(GlobalVariables
      .getFileRequestGeneratorMaxValue());
    RandomFileNameGeneratorWithPrefences.setMinValue(GlobalVariables
      .getFileRequestGeneratorMinValue());
    RandomFileNameGeneratorWithPrefences
      .setNonPreferenceFile(GlobalVariables
        .getFileRequestnNonPreferenceFile());
    RandomFileNameGeneratorWithPrefences.setPreferenceFile(GlobalVariables
      .getFileRequestPreferenceFile());
    RandomFileNameGeneratorWithPrefences.setPreferenceStep(GlobalVariables
      .getFileRequestPreferenceStep());
    ZipfFileNameGenerator.setAlpha(GlobalVariables.getZipfLambda());
    ZipfFileNameGenerator.setMinValue(GlobalVariables.getFileRequestGeneratorMinValue());
    ZipfFileNameGenerator.setMaxValue(GlobalVariables.getFileRequestGeneratorMaxValue());

  }
}
