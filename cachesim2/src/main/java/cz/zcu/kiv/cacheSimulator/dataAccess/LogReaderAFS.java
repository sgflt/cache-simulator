package cz.zcu.kiv.cacheSimulator.dataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
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
  private static final String BEGIN_LISTEN_READ = "SRXAFS_FetchData, Fid =";

  /**
   * konstanta, ktera udava, na ktere radce mame zacit davat pozor na ulozeni souboru
   */
  private static final String BEGIN_LISTEN_STORE = "StoreData: Fid =";

  /**
   * konstanta, ktera udava, na ktere radce mame skoncit se ctenim souboru
   */
  private static final String END_LISTEN_READ = "SRXAFS_FetchData returns";

  /**
   * konstanta, ktera udava, na ktere radce mame skoncit s ulozenim souboru
   */
  private static final String END_LISTEN_STORE = "SAFS_StoreData\treturns";

  /**
   * konstanta pro nalezeni fid souboru pri ukladani souboru
   */
  private static final String STORE_FID = "StoreData: Fid = ";

  /**
   * konstanta pro identifikaci retezce se jmenem souboru
   */
  private static final String FID = "Fid = ";

  /**
   * konstanta pro urceni vyskytu retezce file size
   */
  private static final String FILE_SIZE = "file size ";

  /**
   * konstanta pro zjisteni velikosti souboru pri zapisu do souboru
   */
  private static final String FILE_LENGHT = "Length ";

  /**
   * konstanta pro retezec host
   */
  private static final String HOST = "Host ";

  /**
   * konstanta pro retezec host id
   */
  private static final String HOST_ID = "Id ";


  private static final int PRELOAD_COUNT = 100;

  private final List<RequestedFile> files = new LinkedList<>();

  private final DateFormat fmt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy",  Locale.ENGLISH);

  /**
   * promenna pro uchovani casti prave zpracovavaneho logu
   */
  private final String[] linesRead, linesStore;

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
      final String fileName =  GlobalVariables.getLogAFSFIleName();
      is = new FileInputStream(fileName);

      /* Java 7 */
      //String mimeType = Files.probeContentType(Paths.get(GlobalVariables.getLogAFSFIleName()));

      /* Java 6 */
      final String mimeType = fileName.substring(fileName.length() - 2);

      /* přidat čtečku xz archivu */
      if( mimeType.equals( XZ_MIME ) ) {
        is = new XZInputStream( is );
      }

      this.bf = new BufferedReader( new InputStreamReader( is ) );
      this.fileSize = new File(GlobalVariables.getLogAFSFIleName()).length();
      this.modulo = this.fileSize / 100;
    }
    catch( final FileNotFoundException e1 ){
      // TODO Auto-generated catch block
      e1.printStackTrace();
      this.bf = null;
    }
    catch( final IOException e ){
      // TODO Auto-generated catch block
      e.printStackTrace();
      this.bf = null;
    }
  }

  /**
   * konstruktor - overeni existence souboru
   */
  public LogReaderAFS(final boolean readFSize) {
    GlobalVariables.setActualTime(0);
    this.linesRead = new String[6];
    this.linesStore = new String[7];

    this.openLog();

    this.fileReadPos = 0;
    this.procent = 0;
    this.addObserver(MainGUI.getInstance());
    this.readFSize = readFSize;
  }

  /**
   * konstruktor - overeni existence souboru, spousteni pouze lokalne z metody main
   */
  private LogReaderAFS() {
    this.linesRead = new String[6];
    this.linesStore = new String[7];

    this.openLog();

    this.fileReadPos = 0;
    this.procent = 0;
    this.readFSize = true;
  }

  /**
   * znovu otevre soubor pro cteni logu
   */
  @Override
  public void resetQueue() {
    if (this.bf != null) {
      try {
        this.bf.close();
      } catch (final IOException e) {
        this.bf = null;
      }
    }

    this.openLog();

    this.procent = 0;
    this.fileReadPos = 0;
    this.setChanged();
    this.notifyObservers(0);
  }

  @Override
  public RequestedFile getNextServerFile() {
    return this.getNextServerFile(true);
  }

  /**
   * metoda pro cteni souboru s urcenim, zda se maji preskakovat soubory
   * @param skipFile prepinac pro preskakovani
   * @return nacteny soubor
   */
  public RequestedFile getNextServerFile(final boolean skipFile) {

    if (this.files.size() > 0){
      if (this.fileReadPos / this.modulo > this.procent){
        this.procent++;
        this.setChanged();
        this.notifyObservers(this.info + " " + this.procent + "%");
      }
    } else {
      while (this.files.size() < PRELOAD_COUNT) {
        final RequestedFile f = this.getNextFile(skipFile);

        if (f == null)
          break;

        this.files.add(f);
      }
    }

    return this.files.size() > 0 ? this.files.remove(0) : null;
  }

  /**
   * pouze pro lokalni pouziti
   */
  private RequestedFile getNextServerFileWithOutput() {
    final RequestedFile f = this.getNextFile(true);
    if (f != null){
      if (this.fileReadPos / this.modulo > this.procent){
        this.procent++;
        System.out.println(this.procent + "% completed");
      }
    }
    return f;

  }

  /**
   * metoda pro nacteni dalsiho zaznamu z logu
   * @param skipFiles parametr pro utceni, jestli se maji preskakovat soubory
   * @return zaznam z logu
   */
  private RequestedFile getNextFile(final boolean skipFiles) {
    if (this.bf == null)
      return null;

    // pomocne promenne pro parsovani
    String line, ip, id, fname;
    String[] splittedLine;
    long accessTime, fileSize, userID;

    try {
      line = this.bf.readLine();
      while (line != null) {
        this.fileReadPos += line.length();

        //log cteni souboru
        if (line.contains(BEGIN_LISTEN_READ)) {
          splittedLine = line.split(", ");
          // jsme tam, kde chceme byt
          if (splittedLine.length == 2) {
            this.linesRead[0] = line;

            // nacteni 5 radek z logu do
            for (int i = 1; i < this.linesRead.length; i++) {
              line = this.bf.readLine();
              if (line == null)
                return null;
              this.fileReadPos += line.length();
              this.linesRead[i] = line;
              // pokud nektera z radek obsahuje koncovou formuli a
              // neni posledni -
              // pokracujeme v nacitani souboru
              if ((i < 4 && this.linesRead[i].contains(END_LISTEN_READ)))
                continue;
            }
            if (!this.linesRead[5].contains(END_LISTEN_READ))
              continue;
            //kontrola, zda je pritomna velikost souboru
            if  (!this.linesRead[4].contains(FILE_SIZE))
              continue;

            // zde mame v poli nacten cely log
            splittedLine = this.linesRead[4].split(", ");

            // zjisteni casu pristupu, jmena souboru (identifikator)
            // a velikosti souboru
            accessTime = this.fmt.parse(this.linesRead[1].substring(0, 24)).getTime();
            fname = this.linesRead[0].substring(this.linesRead[0].lastIndexOf(FID)
                + FID.length());

            if (this.readFSize){
              try{
              fileSize = Long
                .parseLong(splittedLine[splittedLine.length - 1]
                    .substring(splittedLine[splittedLine.length - 1]
                        .lastIndexOf(FILE_SIZE)
                        + FILE_SIZE.length()));
              }
              catch (final Exception e){
                e.printStackTrace();
                fileSize = -1;
              }
            }
            else{
              fileSize = -1;
            }
            // zjisteni ip adresy a id uzivatele
            splittedLine = this.linesRead[1].split(", ");
            ip = splittedLine[2].substring(
                splittedLine[2].indexOf(HOST)
                    + HOST.length(),
                splittedLine[2].lastIndexOf(":"));

            id = splittedLine[3].substring(splittedLine[3]
                .lastIndexOf(HOST_ID) + HOST_ID.length());

            userID = Long.parseLong(id);

//            if (skipFiles){
//              if (!ip.equalsIgnoreCase("147.228.67.30") || userID != 58261298)
//              //if (!ip.equalsIgnoreCase("147.228.63.10") || userID != 32766)
//              //if (!ip.equalsIgnoreCase("147.228.67.31") || userID != 32766)
//              //if (!ip.equalsIgnoreCase("147.228.67.32") || userID != 32766)
//              //if (!ip.equalsIgnoreCase("147.228.67.33") || userID != 32766)
//              continue;
//            }

            userID = userID << 32;
            userID += GlobalMethods.ipToInt(ip);

            GlobalVariables.setActualTime(accessTime + RequestedFile.getAddTime());
            return new RequestedFile(fname, accessTime, fileSize,userID, true);

          }
        }

        //log zapis souboru
        if (line.contains(BEGIN_LISTEN_STORE)){
          if (line.contains(STORE_FID)) {
            this.linesStore[0] = line;
          }
          else
            continue;

          //nacteni 6 radek z logu do
          for (int i = 1; i < this.linesStore.length; i++) {
            line = this.bf.readLine();
            if (line == null)
              return null;
            this.fileReadPos += line.length();
            this.linesStore[i] = line;
            // pokud nektera z radek obsahuje koncovou formuli a
            // neni posledni -
            // pokracujeme v nacitani souboru
            if ((i < 5 && this.linesStore[i].contains(END_LISTEN_STORE)))
              continue;
          }

          if (!this.checkLogSave(this.linesStore)){
            continue;
          }


          //zde mame v poli nacten cely log
          splittedLine = this.linesStore[5].split(", ");

          // zjisteni casu pristupu, jmena souboru (identifikator)
          // a velikosti souboru
          accessTime = this.fmt.parse(this.linesStore[1].substring(0, 24)).getTime();
          fname = this.linesStore[0].substring(this.linesStore[0].lastIndexOf(STORE_FID)
              + STORE_FID.length());

          fileSize = Long
              .parseLong(splittedLine[splittedLine.length - 1]
                  .substring(splittedLine[splittedLine.length - 1]
                      .lastIndexOf(FILE_LENGHT)
                      + FILE_LENGHT.length()));



          // zjisteni ip adresy a id uzivatele
          splittedLine = this.linesStore[1].split(", ");
          ip = splittedLine[1].substring(
              splittedLine[1].indexOf(HOST)
                  + HOST.length(),
              splittedLine[1].lastIndexOf(":"));

          id = splittedLine[2].substring(splittedLine[2]
              .lastIndexOf(HOST_ID) + HOST_ID.length());

          userID = Long.parseLong(id);
          userID = userID << 32;
          userID += GlobalMethods.ipToInt(ip);

          GlobalVariables.setActualTime(accessTime);
          return new RequestedFile(fname, accessTime, fileSize,userID, false);
        }

        line = this.bf.readLine();
      }

    } catch (final IOException e) {
      try {
        this.bf.close();
      } catch (final IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      return null;
    } catch (final ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      this.bf.close();
    } catch (final IOException e) {
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
  private boolean checkLogSave(final String[] linesStore) {
    if (linesStore.length != 7) return false;
    if (!linesStore[0].contains(BEGIN_LISTEN_STORE)) return false;
    if (!linesStore[1].contains(BEGIN_LISTEN_STORE)) return false;
    if (!linesStore[2].contains("CheckRights")) return false;
    if (!linesStore[3].contains("BCB: BreakCallBack")) return false;
    if (!linesStore[4].contains("StoreData_RXStyle")) return false;
    if (!linesStore[5].contains("StoreData_RXStyle")) return false;
    if (!linesStore[6].contains(END_LISTEN_STORE)) return false;
    return true;
  }

  @Override
  public void setInfo(final String info) {
    this.info = info;
  }

  /**
   * metoda main - pro pokusy se ctenymi soubory
   * @param args parametry prikazove radky
   */
  public static void main(final String args[]) {

    final LogReaderAFS reader = new LogReaderAFS();
//    totalReadAndWrites(reader);
//    if (1==1)return;
    RequestedFile f = reader.getNextServerFileWithOutput();
    final Hashtable<String, FileOnServer> table = new Hashtable<>(100000);
    final Hashtable<Long, Pair<Integer, Integer>> userReads = new Hashtable<>(10000);
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
          versions = new Pair<>(0, 0);
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
          final FileOnServer fOnSer = new FileOnServer(f.getFname(), f.getfSize());
          table.put(f.getFname(), fOnSer);
          fOnSer.updateFileToVer(f.getfSize());
        }
        else{
          final FileOnServer fOnSer = table.get(f.getFname());
          fOnSer.updateFile(f.getfSize());
        }
      }
      f = reader.getNextServerFileWithOutput();
    }
    System.out.println("\nVysledky cteni souboru");
    System.out.println("Celkem cteni: " + cteni + ", cteni verze>1 : "+cteniVetsiVerze+", celkem zapisu: " + zapisu + ", z toho pred ctenim souboru: " + zapisuPoprve);
    System.out.println("Velikost ctenych souboru: " + fSizes/1024/1024);
    for (final Entry<Long, Pair<Integer, Integer>> entry : userReads.entrySet()){
      final Long userIDs = entry.getKey();
      final Pair<Integer, Integer> pair = entry.getValue();
      if (pair.getSecond() > 0){
        final long id = userIDs >> 32;
                final String ip = (GlobalMethods.intToIp(userIDs - (id << 32)));
        System.out.println("User [" + id + ", ip:" + ip + "]  " + pair.getFirst() + " with version=1, " +  pair.getSecond() + " with version>1");
      }
    }

    System.out.println("\n\nVysledky jednotlivych souboru:");
    for (final FileOnServer file:table.values()){
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
  private static void totalReadAndWrites(final LogReaderAFS reader){
    final Hashtable<String, Pair<Long, Long>> tableRead = new Hashtable<String, Pair<Long, Long>>();
    final Hashtable<String, Pair<Long, Long>> tableStore = new Hashtable<String, Pair<Long, Long>>();
    final Hashtable<Long, Integer> numberOfStores = new Hashtable<Long, Integer>();
    final Hashtable<Long, Integer> numberOfReads = new Hashtable<Long, Integer>();
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
    for (final Pair<Long, Long> pairOut : tableRead.values()) {
      accesses += pairOut.getFirst();
    /*  if (pairOut.getFirst() > 20)
        System.out.println(pairOut.getFirst() + ";" + pairOut.getSecond());*/
    }
    long writeAccesses = 0;
    for (final Pair<Long, Long> pairOut : tableStore.values()) {
      writeAccesses += pairOut.getFirst();
      //if (pairOut.getFirst() > 20)
    //  System.out.println(pairOut.getFirst() + ";" + pairOut.getSecond());
    }

    for (final Entry<Long, Integer> entry : numberOfStores.entrySet()){
      final Long userID = entry.getKey();
      final Integer count = entry.getValue();
      System.out.println(GlobalMethods.intToIp(userID) + ":" + (userID >> 32) + "     " + count + "    " + numberOfReads.get(userID));
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
