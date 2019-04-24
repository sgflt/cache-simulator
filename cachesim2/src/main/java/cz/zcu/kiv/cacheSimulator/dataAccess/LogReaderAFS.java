package cz.zcu.kiv.cacheSimulator.dataAccess;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.util.Hashtable;
import java.util.Observable;


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
	 * konstanta, ktera udava, na ktere radce mame zacit davat pozor
	 */
	private final String beginListen = "SRXAFS_FetchData, Fid =";

	/**
	 * konstanta, ktera udava, na ktere radce mame skoncit
	 */
	private final String endListen = "SRXAFS_FetchData returns";

	/**
	 * konstanta pro identifikaci retezce se jmenem souboru
	 */
	private final String fid = "Fid = ";

	/**
	 * konstanta pro urceni vyskytu retezce file size
	 */
	private final String fsize = "file size ";

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
	private final String[] lines;

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
	 * konstruktor - overeni existence souboru
	 */
	public LogReaderAFS() {
		super();
		this.lines = new String[6];
		try {
			this.bf = new BufferedReader(new FileReader(
					GlobalVariables.getLogAFSFIleName()));
			this.fileSize = new File(GlobalVariables.getLogAFSFIleName()).length();
			this.modulo = this.fileSize / 100;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			this.bf = null;
		}
		this.fileReadPos = 0;
		this.procent = 0;
		this.addObserver(MainGUI.getInstance());
	}

	/**
	 * znovu otevre soubor pro cteni logu
	 */
	public void resetQueue() {
		if (this.bf != null) {
			try {
				this.bf.close();
			} catch (final IOException e) {
				this.bf = null;
			}
		}
		try {
			this.bf = new BufferedReader(new FileReader(
					GlobalVariables.getLogAFSFIleName()));
		} catch (final FileNotFoundException e) {
			this.bf = null;
		}
	}

	@Override
	public Triplet<String, Long, Long> getNextFileName() {
		final RequestedFile f = getNextFile();
		if (f != null){
			if (this.fileReadPos / this.modulo > this.procent){
				this.procent++;
				setChanged();
				notifyObservers(this.procent);
			}
			return new Triplet<>(f.getFname(), f.getAccessTime(), f.getUserID());
		}
		else {
			return null;
		}
	}

	@Override
	public Quartet<String, Long, Long, Long> getNextFileNameWithFSize() {
		final RequestedFile f = getNextFile();
		if (f != null){
			if (this.fileReadPos / this.modulo > this.procent){
				this.procent++;
				setChanged();
				notifyObservers(this.procent);
			}
			return new Quartet<>(f.getFname(), f.getfSize(), f.getAccessTime(), f.getUserID());
		}
		else {
			return null;
		}
	}

	@Override
	public void reset() {

	}

	/**
	 * metoda pro nacteni dalsiho zaznamu z logu
	 * 
	 * @return zaznam z logu
	 */
	@SuppressWarnings("deprecation")
	private RequestedFile getNextFile() {
		if (this.bf == null) {
			return null;
		}

		// pomocne promenne pro parsovani
		String line;
		final String ip;
		final String id;
		final String fname;
		String[] splittedLine;
		final long accessTime;
		final long fileSize;
		long userID;

		try {
			line = this.bf.readLine();
			while (line != null) {
				this.fileReadPos += line.length() + 1;
				if (line.contains(this.beginListen)) {
					splittedLine = line.split(", ");
					// jsme tam, kde chceme byt
					if (splittedLine.length == 2) {
						this.lines[0] = line;

						// nacteni 5 radek z logu do
						for (int i = 1; i < 6; i++) {
							line = this.bf.readLine();
							if (line == null) {
								return null;
							}
							this.fileReadPos += line.length() + 1;
							this.lines[i] = line;
							// pokud nektera z radek obsahuje koncovou formuli a
							// neni posledni -
							// pokracujeme v nacitani souboru
							if ((i < 4 && this.lines[i].contains(this.endListen))) {
								continue;
							}
						}
						if (!this.lines[5].contains(this.endListen)) {
							continue;
						}

						// zde mame v poli nacten cely log
						splittedLine = this.lines[4].split(", ");

						// zjisteni casu pristupu, jmena souboru (identifikator)
						// a velikosti souboru
						accessTime = (long) Date.parse(this.lines[1]
								.substring(0, 24));
						fname = this.lines[0].substring(this.lines[0].lastIndexOf(this.fid)
								+ this.fid.length());
						fileSize = Long
								.parseLong(splittedLine[splittedLine.length - 1]
										.substring(splittedLine[splittedLine.length - 1]
												.lastIndexOf(this.fsize)
												+ this.fsize.length()));

						// zjisteni ip adresy a id uzivatele
						splittedLine = this.lines[1].split(", ");
						ip = splittedLine[2].substring(
								splittedLine[2].indexOf(this.hostString)
										+ this.hostString.length(),
								splittedLine[2].lastIndexOf(":"));
						id = splittedLine[3].substring(splittedLine[3]
								.lastIndexOf(this.hostID) + this.hostID.length());
						
						userID = Long.parseLong(id);
						userID = userID << 32;
						userID += GlobalMethods.ipToInt(ip);
						
						GlobalVariables.setActualTime(accessTime);
						return new RequestedFile(fname, accessTime, fileSize, userID);

					}
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
		}
		try {
			this.bf.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void main(final String[] args) {
		final LogReaderAFS read = new LogReaderAFS();
		final Hashtable<String, Pair<Long, Long>> table = new Hashtable<String, Pair<Long, Long>>();
		RequestedFile fions = read.getNextFile();
		Pair<Long, Long> readHit = null;
		while (fions != null) {
			readHit = table.get(fions.getFname());
			if (readHit == null) {
				table.put(fions.getFname(),
						new Pair<Long, Long>(1L, fions.getfSize()));
			} else {
				readHit.setFirst(readHit.getFirst() + 1);
			}
			fions = read.getNextFile();
		}
		long accesses = 0;
		for (final Pair<Long, Long> pair : table.values()) {
			accesses += pair.getFirst();
			if (pair.getFirst() > 20) {
				System.out.println(pair.getFirst() + ";" + pair.getSecond());
			}
		}
		System.out.println(accesses);
		System.out.println(table.values().size());
	}
}
