package cz.zcu.kiv.cacheSimulator.shared;

import java.io.FileInputStream;
import java.io.IOException;
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
 * @author Pavel Bzoch
 *
 */
public class GlobalMethods {
	
	/**
	 * metoda pro konverzi ip adresy na cislo
	 * @param ip ip adresa jako retezec
	 * @return ip adresa jako 32bit cislo
	 */
	public static long ipToInt(String ip){
		String ips[] = ip.split("\\.");
		long result = 0;  
		for (String str: ips)  
		{  
		    result = result << 8 | ((Integer.valueOf(str)) & 0xFF);  
		}
		return result;
	}

	/**
	 * metoda pro prevod int > String
	 * @param cislo cislo pro prevod
	 * @return retezec prezentujici IP adresu
	 */
	public static String intToIp(long cislo){
		String ret = "";
		String hex = Integer.toHexString((int)cislo);
		if (hex.length() != 8)
			return hex;
		for (int i = 0; i < 4; i++){
			ret += Integer.parseInt(hex.substring(i*2, i*2+2), 16);
			if (i < 3)
				ret += ".";
		}
		return ret;
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
        
    /**
     * Metoda pro ziskani jmen trid z jar souboru
     * 
     * @param jarLocation cesta k jar souboru
     * @param packageName jmeno baliku, ktery v jar souboru hledame
     * @return seznam trid v dane package
     */
    public static List<String> getJarClassNames(String jarLocation, String packageName) {
        List<String> files = new ArrayList<String>();
  
        if (jarLocation == null) {
            return files; // Empty.
        }
        
        // Lets stream the jar file 
        JarInputStream jarInputStream = null;
        try {
            jarInputStream = new JarInputStream(new FileInputStream(jarLocation));
            JarEntry jarEntry;
            
            // Iterate the jar entries within that jar. Then make sure it follows the
            // filter given from the user.
            do {
                jarEntry = jarInputStream.getNextJarEntry();
                if (jarEntry != null) {
                    String fileName = jarEntry.getName();
                    if (fileName.contains(packageName) && !fileName.contains("$") && !fileName.equalsIgnoreCase(packageName)){
                        fileName = fileName.substring(0, fileName.lastIndexOf(".class"));
                        files.add(fileName);  
                    }
                }
            }
            while (jarEntry != null);
            jarInputStream.close();
        }
        catch (IOException ioe) {
            throw new RuntimeException("Unable to get Jar input stream from '" + jarLocation + "'", ioe);
        }
        return files;
    }
}
