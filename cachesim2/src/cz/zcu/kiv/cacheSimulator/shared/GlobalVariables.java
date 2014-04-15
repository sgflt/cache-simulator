package cz.zcu.kiv.cacheSimulator.shared;

/**
 * trida pro uchovani globalnich nastaveni simulace
 * 
 * @author Pavel Bzoch
 * 
 */
public class GlobalVariables {

	/**
	 * promenna pro uchovani aktualniho adresare
	 */
	private static String actDir = ".";

	/**
	 * promenna pro urceni lambdy pro zipf random generator
	 */
	private static double zipfLambda = 0.75;

	/**
	 * prommena pro urceni poctu pozadavku pri nahodnem generovani pozadavku
	 */
	private static int requestCountForRandomGenerator = 100000;

	/**
	 * promenna urcujici rozptyl pro gaussovsky nahodny generator cisel
	 */
	private static int fileRequestGeneratorDispersion = 100;

	/**
	 * promenna urcujici stredni hodnotu pro gaussovsky nahodny generator cisel
	 */
	private static int fileRequestGeneratorMeanValue = 5000;

	/**
	 * promenna pro urceni, jake nasobky jsou preferovane
	 */
	private static int fileRequestPreferenceFile = 100;

	/**
	 * promenna pro urceni kolikaty krok ma vygenerovat preferovany soubor
	 */
	private static int fileRequestPreferenceStep = 4;

	/**
	 * nasobky tohoto cisla nebudou generovany
	 */
	private static int fileRequestnNonPreferenceFile = 7;

	/**
	 * promenna urcujici minimalni hodnotu pro gaussovsky nahodny generator
	 * cisel
	 */
	private static int fileRequestGeneratorMinValue = 1;

	/**
	 * promenna urcujici maximalni hodnotu pro gaussovsky nahodny generator
	 * cisel
	 */
	private static int fileRequestGeneratorMaxValue = 10000;

	/**
	 * promenna pro urceni minimalni velikosti generovanych souboru
	 */
	private static int minGeneratedFileSize = 1024;

	/**
	 * promenna pro urceni maximalni velikosti generovanych souboru
	 */
	private static int maxGeneratedFileSize = 5 * 1024 * 1024;

	/**
	 * prepinac na urceni, zda se maji data o pristupech nacitat ze souboru
	 */
	private static boolean loadDataFromLog = false;
	
	/**
	 * prepinac pro urceni, jestli se maji nahravat statistikz na server
	 */
	private static boolean loadServerStatistic = true;
	
	/**
	 * prepipnac pro urceni, zda se maji pro soubory z logu generovat nahodne
	 * velikosti
	 */
	private static boolean randomFileSizesForLoggedData = true;

	/**
	 * promenna pro urceni vstupniho souboru s logy
	 */
	private static String logAFSFIleName = "/tmp/afs.xz";

	/**
	 * 
	 * promenna pro uchovani. zda se ma u LFU-SS, ktere vyzaduji statistiky
	 */
	private static boolean sendStatisticsToServerLFU = true;

	/**
	 * 
	 * promenna pro uchovani. zda se ma u LRFU-SS, ktere vyzaduji statistiky ze
	 * serveru, posilat info o kazdem pristupovanem souboru
	 */
	private static boolean sendStatisticsToServerLRFU = true;

	/**
	 * promenna pro stanoveni limit poctu pristupu k souboru pro dalsi
	 * zpracovani dat
	 */
	private static int limitForStatistics = 20;

	/**
	 * promenna pro urceni prumerne rychlosti site - pouziva se u souboru
	 * vetsich nech je kapacita cache
	 */
	private static int averageNetworkSpeed = 80; // Mbit/s

	/**
	 * promenna pro urceni, kolik kapacity cache se ma pouzit na stahovaci
	 * okenko pro soubory, ktere jsou vetsi nez cache
	 */
	private static int cacheCapacityForDownloadWindow = 25;

	/**
	 * promenna pro uchovani aktualniho casu
	 */
	private static long actualTime = 0;

	public static int getFileRequestPreferenceFile() {
		return fileRequestPreferenceFile;
	}

	public static void setFileRequestPreferenceFile(
			int fileRequestPreferenceFile) {
		GlobalVariables.fileRequestPreferenceFile = fileRequestPreferenceFile;
	}

	public static int getFileRequestPreferenceStep() {
		return fileRequestPreferenceStep;
	}

	public static void setFileRequestPreferenceStep(
			int fileRequestPreferenceStep) {
		GlobalVariables.fileRequestPreferenceStep = fileRequestPreferenceStep;
	}

	public static int getFileRequestnNonPreferenceFile() {
		return fileRequestnNonPreferenceFile;
	}

	public static void setFileRequestnNonPreferenceFile(
			int fileRequestnNonPreferenceFile) {
		GlobalVariables.fileRequestnNonPreferenceFile = fileRequestnNonPreferenceFile;
	}

	public static long getActualTime() {
		return actualTime;
	}

	public static void setActualTime(long actualTime) {
		GlobalVariables.actualTime = actualTime;
	}

	public static int getAverageNetworkSpeed() {
		return averageNetworkSpeed;
	}

	public static void setAverageNetworkSpeed(int averageNetworkSpeed) {
		GlobalVariables.averageNetworkSpeed = averageNetworkSpeed;
	}

	public static double getCacheCapacityForDownloadWindow() {
		return cacheCapacityForDownloadWindow / 100.0;
	}

	public static void setCacheCapacityForDownloadWindow(
			int cacheCapacityForDownloadWindow) {
		GlobalVariables.cacheCapacityForDownloadWindow = cacheCapacityForDownloadWindow;
	}

	public static int getLimitForStatistics() {
		return limitForStatistics;
	}

	public static void setLimitForStatistics(int limitForStatistics) {
		GlobalVariables.limitForStatistics = limitForStatistics;
	}

	public static boolean isLoadDataFromLog() {
		return loadDataFromLog;
	}

	public static void setLoadDataFromLog(boolean loadDataFromLog) {
		GlobalVariables.loadDataFromLog = loadDataFromLog;
	}

	public static boolean isRandomFileSizesForLoggedData() {
		return randomFileSizesForLoggedData;
	}

	public static void setRandomFileSizesForLoggedData(
			boolean randomFileSizesForLoggedData) {
		GlobalVariables.randomFileSizesForLoggedData = randomFileSizesForLoggedData;
	}

	public static String getLogAFSFIleName() {
		return logAFSFIleName;
	}

	public static void setLogAFSFIleName(String logAFSFIleName) {
		GlobalVariables.logAFSFIleName = logAFSFIleName;
	}

	public static int getFileRequestGeneratorDispersion() {
		return fileRequestGeneratorDispersion;
	}

	public static void setFileRequestGeneratorDispersion(
			int fileRequestGeneratorDispersion) {
		GlobalVariables.fileRequestGeneratorDispersion = fileRequestGeneratorDispersion;
	}

	public static int getFileRequestGeneratorMeanValue() {
		return fileRequestGeneratorMeanValue;
	}

	public static void setFileRequestGeneratorMeanValue(
			int fileRequestGeneratorMeanValue) {
		GlobalVariables.fileRequestGeneratorMeanValue = fileRequestGeneratorMeanValue;
	}

	public static int getFileRequestGeneratorMinValue() {
		return fileRequestGeneratorMinValue;
	}

	public static void setFileRequestGeneratorMinValue(
			int fileRequestGeneratorMinValue) {
		GlobalVariables.fileRequestGeneratorMinValue = fileRequestGeneratorMinValue;
	}

	public static int getFileRequestGeneratorMaxValue() {
		return fileRequestGeneratorMaxValue;
	}

	public static void setFileRequestGeneratorMaxValue(
			int fileRequestGeneratorMaxValue) {
		GlobalVariables.fileRequestGeneratorMaxValue = fileRequestGeneratorMaxValue;
	}

	public static boolean isSendStatisticsToServerLFUSS() {
		return sendStatisticsToServerLFU;
	}

	public static void setSendStatisticsToServerLFUSS(
			boolean sendStatisticsToServer) {
		GlobalVariables.sendStatisticsToServerLFU = sendStatisticsToServer;
	}

	public static boolean isSendStatisticsToServerLRFUSS() {
		return sendStatisticsToServerLRFU;
	}

	public static void setSendStatisticsToServerLRFUSS(
			boolean sendStatisticsToServer) {
		GlobalVariables.sendStatisticsToServerLRFU = sendStatisticsToServer;
	}

	public static int getMinGeneratedFileSize() {
		return minGeneratedFileSize;
	}

	public static void setMinGeneratedFileSize(int minGeneratedFileSize) {
		GlobalVariables.minGeneratedFileSize = minGeneratedFileSize;
	}

	public static int getMaxGeneratedFileSize() {
		return maxGeneratedFileSize;
	}

	public static void setMaxGeneratedFileSize(int maxGeneratedFileSize) {
		GlobalVariables.maxGeneratedFileSize = maxGeneratedFileSize;
	}

	public static int getRequestCountForRandomGenerator() {
		return requestCountForRandomGenerator;
	}

	public static void setRequestCountForRandomGenerator(
			int requestCountForRandomGenerator) {
		GlobalVariables.requestCountForRandomGenerator = requestCountForRandomGenerator;
	}

	public static String getActDir() {
		return actDir;
	}

	public static void setActDir(String actDir) {
		GlobalVariables.actDir = actDir;
	}

	public static double getZipfLambda() {
		return GlobalVariables.zipfLambda;
	}

	public static void setZipfLambda(double zipfLambda) {
		GlobalVariables.zipfLambda = zipfLambda;
	}

	public static boolean isLoadServerStatistic() {
		return GlobalVariables.loadServerStatistic;
	}

	public static void setLoadServerStatistic(boolean loadserverStatistic) {
		GlobalVariables.loadServerStatistic = loadserverStatistic;
	}

}
