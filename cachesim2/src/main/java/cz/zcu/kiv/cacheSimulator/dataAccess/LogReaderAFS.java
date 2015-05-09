package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.Hashtable;
import java.util.Observable;

import org.tukaani.xz.XZInputStream;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;


/**
 * trida pro cteni z logovaciho souboru
 * 
 * @author Pavel Bzoch
 * 
 */
public class LogReaderAFS extends Observable implements IFileQueue {

	/**
	 * promenna slouzi pro ulozeni reference na objekt pro cteni ze souboru
	 */
	private BufferedReader bf = null;

	/**
	 * konstanta, ktera udava, na ktere radce mame zacit davat pozor na cteni souboru
	 */
	private final String beginListenRead = "SRXAFS_FetchData, Fid =";
	
	/**
	 * konstanta, ktera udava, na ktere radce mame zacit davat pozor na ulozeni souboru
	 */
	private final String beginListenStore = "StoreData: Fid =";

	/**
	 * konstanta, ktera udava, na ktere radce mame skoncit se ctenim souboru
	 */
	private final String endListenRead = "SRXAFS_FetchData returns";
	
	/**
	 * konstanta, ktera udava, na ktere radce mame skoncit s ulozenim souboru
	 */
	private final String endListenStore = "SAFS_StoreData\treturns";
	
	/**
	 * konstanta pro nalezeni fid souboru pri ukladani souboru
	 */
	private final String storeFid = "StoreData: Fid = "; 

	/**
	 * konstanta pro identifikaci retezce se jmenem souboru
	 */
	private final String fid = "Fid = ";

	/**
	 * konstanta pro urceni vyskytu retezce file size
	 */
	private final String fsize = "file size ";
	
	/**
	 * konstanta pro zjisteni velikosti souboru pri zapisu do souboru
	 */
	private final String fLength = "Length ";

	/**
	 * konstanta pro retezec host
	 */
	private final String hostString = "Host ";

	/**
	 * konstanta pro retezec host id
	 */
	private final String hostID = "Id ";
	
	/**
	 * promenna pro uchovani casti prave zpracovavaneho logu
	 */
	private String[] linesRead, linesStore;

	/**
	 * promenne pro urceni, kde v souboru aktualne jsme
	 */
	private long fileSize, fileReadPos;
	
	/**
	 * promenna pro urceni, jak casto se ma posilat do gui informace o progesu
	 */
	private long modulo = 1;
	
	/**
	 * promenna pro urceni, jak daleko jsme s nacitanim
	 */
	private int procent = 0;
	
	/**
	 * retezec, ktery se posila do gui
	 */
	private String info = "";
	
	/**
	 * promenna pro uchovani, jestli se ma nacitat i velikost souboru
	 */
	private boolean readFSize = true;

	
	private void openLog()
	{
		InputStream is;
		final String XZ_MIME = "xz"; // "application/x-xz" Java 7
		
		try{
			String fileName =  GlobalVariables.getLogAFSFIleName();
			is = new FileInputStream(fileName);
			
			/* Java 7 */
			//String mimeType = Files.probeContentType(Paths.get(GlobalVariables.getLogAFSFIleName()));
			
			/* Java 6 */
			String mimeType = fileName.substring(fileName.length() - 2);
			
			/* přidat čtečku xz archivu */
			if( mimeType.equals( XZ_MIME ) ) {				
				is = new XZInputStream( is );
			}
			
			this.bf = new BufferedReader( new InputStreamReader( is ) );
			this.fileSize = new File(GlobalVariables.getLogAFSFIleName()).length();
			this.modulo = fileSize / 100;
		}
		catch( FileNotFoundException e1 ){
			// TODO Auto-generated catch block
			e1.printStackTrace();
			bf = null;
		}
		catch( IOException e ){
			// TODO Auto-generated catch block
			e.printStackTrace();
			bf = null;
		}
	}
	
	/**
	 * konstruktor - overeni existence souboru
	 */
	public LogReaderAFS(boolean readFSize) {
		super();
		GlobalVariables.setActualTime(0);
		linesRead = new String[6];
		linesStore = new String[7];
	
		openLog();
		
		this.fileReadPos = 0;
		this.procent = 0;
		this.addObserver(MainGUI.getInstance());
		this.readFSize = readFSize;
	}
	
	/**
	 * konstruktor - overeni existence souboru, spousteni pouze lokalne z metody main
	 */
	private LogReaderAFS() {
		linesRead = new String[6];
		linesStore = new String[7];
		
		openLog();
		
		this.fileReadPos = 0;
		this.procent = 0;
		this.readFSize = true;
	}

	/**
	 * znovu otevre soubor pro cteni logu
	 */
	@Override
	public void resetQueue() {
		if (bf != null) {
			try {
				bf.close();
			} catch (IOException e) {
				bf = null;
			}
		}

		openLog();
		
		this.procent = 0;
		this.fileReadPos = 0;
		setChanged();
		notifyObservers(new Integer(0));
	}

	@Override
	public RequestedFile getNextServerFile() {
		RequestedFile f = getNextFile(true);
		if (f != null){
			if (fileReadPos / modulo > procent){
				procent++;
				setChanged();
				notifyObservers(info + " " + procent + "%");
			}
		}
		return f;
	}
	
	/**
	 * metoda pro cteni souboru s urcenim, zda se maji preskakovat soubory
	 * @param skipFile prepinac pro preskakovani
	 * @return nacteny soubor
	 */
	public RequestedFile getNextServerFile(boolean skipFile) {
		RequestedFile f = getNextFile(skipFile);
		if (f != null){
			if (fileReadPos / modulo > procent){
				procent++;
				setChanged();
				notifyObservers(info + " " + procent + "%");
			}
		}
		return f;
	}
	
	/**
	 * pouze pro lokalni pouziti
	 */
	private RequestedFile getNextServerFileWithOutput() {
		RequestedFile f = getNextFile(true);
		if (f != null){
			if (fileReadPos / modulo > procent){
				procent++;
				System.out.println(procent + "% completed");
			}
		}
		return f;

	}

	/**
	 * metoda pro nacteni dalsiho zaznamu z logu
	 * @param skipFiles parametr pro utceni, jestli se maji preskakovat soubory
	 * @return zaznam z logu
	 */
	@SuppressWarnings("deprecation")
	private RequestedFile getNextFile(boolean skipFiles) {
		if (bf == null)
			return null;

		// pomocne promenne pro parsovani
		String line, ip, id, fname;
		String[] splittedLine;
		long accessTime, fileSize, userID;

		try {
			line = bf.readLine();
			while (line != null) {
				fileReadPos += line.length();

				//log cteni souboru
				if (line.contains(beginListenRead)) {
					splittedLine = line.split(", ");
					// jsme tam, kde chceme byt
					if (splittedLine.length == 2) {
						linesRead[0] = line;

						// nacteni 5 radek z logu do
						for (int i = 1; i < linesRead.length; i++) {
							line = bf.readLine();							
							if (line == null)
								return null;
							fileReadPos += line.length();
							linesRead[i] = line;
							// pokud nektera z radek obsahuje koncovou formuli a
							// neni posledni -
							// pokracujeme v nacitani souboru
							if ((i < 4 && linesRead[i].contains(endListenRead)))
								continue;
						}
						if (!linesRead[5].contains(endListenRead))
							continue;
						//kontrola, zda je pritomna velikost souboru
						if  (!linesRead[4].contains(fsize))
							continue;

						// zde mame v poli nacten cely log
						splittedLine = linesRead[4].split(", ");

						// zjisteni casu pristupu, jmena souboru (identifikator)
						// a velikosti souboru
						accessTime = (long) Date.parse(linesRead[1]
								.substring(0, 24));
						fname = linesRead[0].substring(linesRead[0].lastIndexOf(fid)
								+ fid.length());
						
						if (readFSize){
							try{
							fileSize = Long
								.parseLong(splittedLine[splittedLine.length - 1]
										.substring(splittedLine[splittedLine.length - 1]
												.lastIndexOf(fsize)
												+ fsize.length()));
							}
							catch (Exception e){
								e.printStackTrace();
								fileSize = -1;
							}
						}
						else{
							fileSize = -1;
						}
						// zjisteni ip adresy a id uzivatele
						splittedLine = linesRead[1].split(", ");
						ip = splittedLine[2].substring(
								splittedLine[2].indexOf(hostString)
										+ hostString.length(),
								splittedLine[2].lastIndexOf(":"));
					
						id = splittedLine[3].substring(splittedLine[3]
								.lastIndexOf(hostID) + hostID.length());
						
						userID = Long.parseLong(id);
						
//						if (skipFiles){
//							if (!ip.equalsIgnoreCase("147.228.67.30") || userID != 58261298)
//							//if (!ip.equalsIgnoreCase("147.228.63.10") || userID != 32766)
//							//if (!ip.equalsIgnoreCase("147.228.67.31") || userID != 32766)
//							//if (!ip.equalsIgnoreCase("147.228.67.32") || userID != 32766)
//							//if (!ip.equalsIgnoreCase("147.228.67.33") || userID != 32766)
//							continue;
//						}
						
						userID = userID << 32;
						userID += GlobalMethods.ipToInt(ip);
						
						GlobalVariables.setActualTime(accessTime + RequestedFile.getAddTime());
						return new RequestedFile(fname, accessTime, fileSize,userID, true);

					}
				}
				
				//log zapis souboru
				if (line.contains(beginListenStore)){
					if (line.contains(storeFid)) {
						linesStore[0] = line;
					}
					else
						continue;

					//nacteni 6 radek z logu do
					for (int i = 1; i < linesStore.length; i++) {
						line = bf.readLine();
						if (line == null)
							return null;
						fileReadPos += line.length();
						linesStore[i] = line;
						// pokud nektera z radek obsahuje koncovou formuli a
						// neni posledni -
						// pokracujeme v nacitani souboru
						if ((i < 5 && linesStore[i].contains(endListenStore)))
							continue;
					}
					
					if (!checkLogSave(linesStore)){
						continue;
					}
				
					
					//zde mame v poli nacten cely log
					splittedLine = linesStore[5].split(", ");

					// zjisteni casu pristupu, jmena souboru (identifikator)
					// a velikosti souboru
					accessTime = (long) Date.parse(linesStore[1]
							.substring(0, 24));
					fname = linesStore[0].substring(linesStore[0].lastIndexOf(storeFid)
							+ storeFid.length());
					
					fileSize = Long
							.parseLong(splittedLine[splittedLine.length - 1]
									.substring(splittedLine[splittedLine.length - 1]
											.lastIndexOf(fLength)
											+ fLength.length()));
					
					
					
					// zjisteni ip adresy a id uzivatele
					splittedLine = linesStore[1].split(", ");
					ip = splittedLine[1].substring(
							splittedLine[1].indexOf(hostString)
									+ hostString.length(),
							splittedLine[1].lastIndexOf(":"));
				
					id = splittedLine[2].substring(splittedLine[2]
							.lastIndexOf(hostID) + hostID.length());
					
					userID = Long.parseLong(id);
					userID = userID << 32;
					userID += GlobalMethods.ipToInt(ip);
					
					GlobalVariables.setActualTime(accessTime);
					return new RequestedFile(fname, accessTime, fileSize,userID, false);
				}
				
				line = bf.readLine();
			}

		} catch (IOException e) {
			try {
				bf.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		try {
			bf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	/**
	 * metoda pro otestovani, zda je nacteny kus logu spravny
	 * @param linesStore2
	 * @return
	 */
	private boolean checkLogSave(String[] linesStore) {
		if (linesStore.length != 7) return false;
		if (!linesStore[0].contains(beginListenStore)) return false;
		if (!linesStore[1].contains(beginListenStore)) return false;
		if (!linesStore[2].contains("CheckRights")) return false;
		if (!linesStore[3].contains("BCB: BreakCallBack")) return false;
		if (!linesStore[4].contains("StoreData_RXStyle")) return false;
		if (!linesStore[5].contains("StoreData_RXStyle")) return false;
		if (!linesStore[6].contains(endListenStore)) return false;
		return true;
	}

	@Override
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * metoda main - pro pokusy se ctenymi soubory
	 * @param args parametry prikazove radky
	 */
	public static void main(String args[]) {
		
		LogReaderAFS reader = new LogReaderAFS();
//		totalReadAndWrites(reader);
//		if (1==1)return;
		RequestedFile f = reader.getNextServerFileWithOutput();
		Hashtable<String, FileOnServer> table = new Hashtable<String, FileOnServer>(100000);
		Hashtable<Long, Pair<Integer, Integer>> userReads = new Hashtable<Long, Pair<Integer,Integer>>(10000);
		int zapisu = 0;
		int cteni = 0;
		int zapisuPoprve = 0;
		int cteniVetsiVerze = 0;
		
		long fSizes = 0;
		
		
		while (f != null) {
		
			//soubor je na cteni - aktualizujeme informace, jestli je ve verzi jedna nebo vyssi 
			if (f.isRead()){
				cteni++;
				fSizes+=f.getfSize();
				Pair<Integer, Integer> versions = userReads.get(f.getUserID());
				if (versions == null){
					versions = new Pair<Integer, Integer>(0, 0);
				}
				//soubor jeste nebyl cten - zalozime jej
				if (!table.containsKey(f.getFname())){
					table.put(f.getFname(), new FileOnServer(f.getFname(), f.getfSize()));
					versions.setFirst(versions.getFirst() + 1);
				}
				else{
					//nastavime, ze byl soubor cten od posledniho casu zapisu
					table.get(f.getFname()).setWasReadSinceLastWrite();
					//soubor ma vyssi verzi
					if (table.get(f.getFname()).getVersion() > 1){
						versions.setSecond(versions.getSecond() + 1);
						cteniVetsiVerze++;
					}
					//soubor ma verzi 1
					else{
						versions.setFirst(versions.getFirst() + 1);
					}
				}
				userReads.put(f.getUserID(), versions);
			}
			//soubor je zapisovan - zvysime verzi
			else{
				zapisu++;
				if (!table.containsKey(f.getFname())){
					zapisuPoprve++;
					FileOnServer fOnSer = new FileOnServer(f.getFname(), f.getfSize());
					table.put(f.getFname(), fOnSer);
					fOnSer.updateFileToVer(f.getfSize());
				}
				else{
					FileOnServer fOnSer = table.get(f.getFname());
					fOnSer.updateFile(f.getfSize());
				}
			}
			f = reader.getNextServerFileWithOutput();
		}
		System.out.println("\nVysledky cteni souboru");
		System.out.println("Celkem cteni: " + cteni + ", cteni verze>1 : "+cteniVetsiVerze+", celkem zapisu: " + zapisu + ", z toho pred ctenim souboru: " + zapisuPoprve);
		System.out.println("Velikost ctenych souboru: " + fSizes/1024/1024);
		for (Long userIDs : userReads.keySet()){
			Pair<Integer, Integer> pair = userReads.get(userIDs);
			if (pair.getSecond() > 0){
				long id = userIDs >> 32;
                String ip = (GlobalMethods.intToIp(userIDs - (id << 32)));
				System.out.println("User [" + id + ", ip:" + ip + "]  " + pair.getFirst() + " with version=1, " +  pair.getSecond() + " with version>1");
			}
		}
		
		System.out.println("\n\nVysledky jednotlivych souboru:");
		for (FileOnServer file:table.values()){
			if (file.getVersion() > 2){
				System.out.println(file);
			}
		}
		
	}
	
	/**
	 * metoda pro vyhodnoceni poctu ctenych a zapisovanych souboru
	 * @param reader reference na ctecku souboru
	 */
	@SuppressWarnings("unused")
	private static void totalReadAndWrites(LogReaderAFS reader){
		Hashtable<String, Pair<Long, Long>> tableRead = new Hashtable<String, Pair<Long, Long>>();
		Hashtable<String, Pair<Long, Long>> tableStore = new Hashtable<String, Pair<Long, Long>>();
		Hashtable<Long, Integer> numberOfStores = new Hashtable<Long, Integer>();
		Hashtable<Long, Integer> numberOfReads = new Hashtable<Long, Integer>();
		RequestedFile fions = reader.getNextFile(false);
		Pair<Long, Long> pair = null;
		long readHit = 0, writeHit = 0;
		while (fions != null) {
			if (fions.isRead())
			{
				readHit++;
				
				pair = tableRead.get(fions.getFname());
				if (pair == null) {
					tableRead.put(fions.getFname(),
						new Pair<Long, Long>(1L, fions.getfSize()));
				} else {
					pair.setFirst(pair.getFirst() + 1);
				}
				
				if (numberOfReads.get(fions.getUserID()) == null){
					numberOfReads.put(fions.getUserID(), 1);
				}
				else{
					numberOfReads.put(fions.getUserID(), numberOfReads.get(fions.getUserID()) + 1);
				}

			}
			else{
				writeHit++;
				pair = tableStore.get(fions.getFname());
				if (pair == null) {
					tableStore.put(fions.getFname(),
						new Pair<Long, Long>(1L, fions.getfSize()));
				} else {
					pair.setFirst(pair.getFirst() + 1);
				}
				
				if (numberOfStores.get(fions.getUserID()) == null){
					numberOfStores.put(fions.getUserID(), 1);
				}
				else{
					numberOfStores.put(fions.getUserID(), numberOfStores.get(fions.getUserID()) + 1);
				}
			}
			fions = reader.getNextFile(false);
		}
		long accesses = 0;
		for (Pair<Long, Long> pairOut : tableRead.values()) {
			accesses += pairOut.getFirst();
		/*	if (pairOut.getFirst() > 20)
				System.out.println(pairOut.getFirst() + ";" + pairOut.getSecond());*/
		}
		long writeAccesses = 0;
		for (Pair<Long, Long> pairOut : tableStore.values()) {
			writeAccesses += pairOut.getFirst();
			//if (pairOut.getFirst() > 20)
		//	System.out.println(pairOut.getFirst() + ";" + pairOut.getSecond());
		}
		
		for (Long userID: numberOfStores.keySet()){
			System.out.println(GlobalMethods.intToIp(userID) + ":" + (userID >> 32) + "     " + numberOfStores.get(userID) + "    " + numberOfReads.get(userID));
		}
		System.out.println("Total number of users: " +  numberOfStores.keySet().size());
		System.out.println("Total writes: " + writeAccesses + " to " + tableStore.values().size() + " files.  " + writeHit);
		System.out.println("Total reads: " + accesses + " to " + tableRead.values().size() + "files.  " + readHit);
	}
	
	@Override
	public void cleanUp() {
		this.deleteObservers();
	}
}
