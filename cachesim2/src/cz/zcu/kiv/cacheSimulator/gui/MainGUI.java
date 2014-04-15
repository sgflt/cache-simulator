package cz.zcu.kiv.cacheSimulator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.ui.RefineryUtilities;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.AccessSimulation;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

/**
 * 
 * @author Pavel Bzoch trida pro zobrazeni hlavniho gui aplikace
 */
@SuppressWarnings("serial")
public class MainGUI extends javax.swing.JFrame implements Observer {

	/**
	 * staticky ukazatel na hlavni okno programu - singleton instance
	 */
	private static MainGUI gui = null;

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof Integer) {
			this.simulationProgressBar.setValue((Integer) arg1);
			this.simulationProgressBar.setString("Simulation in progress... "
					+ (Integer) arg1 + "%");
		} else {
			this.simulationProgressBar.setValue(this.simulationProgressBar
					.getValue() + 1);
			this.simulationProgressBar.setString(arg1.toString());
		}
	}

	/**
	 * promenna pro uchovani buttonu pro nastaveni cache
	 */
	private ArrayList<javax.swing.JCheckBox> cacheCheckBoxes = null;

	/**
	 * promenna pro uchovani buttonu pro nastaveni consistency control
	 */

	private ArrayList<javax.swing.JCheckBox> consistencyCheckBoxes = null;

	/**
	 * promenna pro uchovani vysledku mereni cache
	 */
	public ArrayList<UserStatistics> cacheResults = null;

	/**
	 * promenna pro uchovani jmen panelu pro nastaveni
	 */
	private ArrayList<JPanel> settingsPanels = null;

	/**
	 * promenna pro uchovani odkazu na simulacni vlakno pro pripadne preruseni
	 * simulace
	 */
	private AccessSimulation simulationThread = null;
	
	/**
	 * promenna pro uchovani vzsledku z mereni konzistentnosti dat
	 */
	private IConsistencySimulation consSimulation = null;

	/**
	 * singleton konstruktor
	 * 
	 * @return instance okna
	 */
	public static MainGUI getInstance() {
		if (gui == null) {
			gui = new MainGUI();
			gui.setVisible(true);
		}
		return gui;
	}

	/**
	 * metoda pro zviditelneni komponent pro vstup ze souboru
	 */
	private void showInputFile() {
		simConsistencyCheck.setVisible(true);
		randomFileSizeCheckBox.setEnabled(true);
		maenValueGaussSpinner.setVisible(false);
		maxFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		maxGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		meanValueGaussLabel.setVisible(false);
		minFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		minGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		nonPrefLabel.setVisible(false);
		nonPrefSpinner.setVisible(false);
		pathLabel.setVisible(true);
		pathTextField.setVisible(true);
		pathTextField.setText(GlobalVariables.getLogAFSFIleName());
		preferenceDivisibleLabel.setVisible(false);
		preferenceDivisibleSpinner.setVisible(false);
		randomFileSizeCheckBox.setVisible(true);
		randomFileSizeCheckBox.setSelected(GlobalVariables
				.isRandomFileSizesForLoggedData());
		stepPrefLabel.setVisible(false);
		stepPrefSpinner.setVisible(false);
		dispersionLabel.setVisible(false);
		dispersionSpinner.setVisible(false);
		generateFileLabel.setVisible(false);
		generateFileSpinner.setVisible(false);
		unitsLabel1.setVisible(randomFileSizeCheckBox.isSelected());
		unitsLabel2.setVisible(randomFileSizeCheckBox.isSelected());
		requestCountSpinner.setVisible(false);
		requestsCountLabel.setVisible(false);
		requestCountSpinner.setVisible(false);
		requestsCountLabel.setVisible(false);
		zipfLambdaLabel.setVisible(false);
		zipfLamdbaSpinner.setVisible(false);
	}

	/**
	 * metoda pro zobrazeni nastaveni random vstupu
	 */
	private void showRandomInput() {
		simConsistencyCheck.setVisible(false);
		simConsistencyCheck.setSelected(false);
		randomFileSizeCheckBox.setEnabled(false);
		randomFileSizeCheckBox.setSelected(true);
		maenValueGaussSpinner.setVisible(false);
		meanValueGaussLabel.setVisible(false);
		maxFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		maxGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		minFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		minGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		nonPrefLabel.setVisible(false);
		nonPrefSpinner.setVisible(false);
		pathLabel.setVisible(false);
		pathTextField.setVisible(false);
		preferenceDivisibleLabel.setVisible(false);
		preferenceDivisibleSpinner.setVisible(false);
		randomFileSizeCheckBox.setVisible(true);
		stepPrefLabel.setVisible(false);
		stepPrefSpinner.setVisible(false);
		dispersionLabel.setVisible(false);
		dispersionSpinner.setVisible(false);
		generateFileLabel.setVisible(true);
		generateFileSpinner.setVisible(true);
		unitsLabel1.setVisible(randomFileSizeCheckBox.isSelected());
		unitsLabel2.setVisible(randomFileSizeCheckBox.isSelected());
		requestCountSpinner.setVisible(true);
		requestsCountLabel.setVisible(true);
		zipfLambdaLabel.setVisible(false);
		zipfLamdbaSpinner.setVisible(false);
	}

	/**
	 * metoda pro zobrazeni ovladacich prvku pro gaussovske nastaveni
	 */
	private void showGaussInput() {
		simConsistencyCheck.setVisible(false);
		simConsistencyCheck.setSelected(false);
		randomFileSizeCheckBox.setEnabled(false);
		randomFileSizeCheckBox.setSelected(true);
		maenValueGaussSpinner.setVisible(true);
		meanValueGaussLabel.setVisible(true);
		maxFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		maxGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		minFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		minGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		nonPrefLabel.setVisible(false);
		nonPrefSpinner.setVisible(false);
		pathLabel.setVisible(false);
		pathTextField.setVisible(false);
		preferenceDivisibleLabel.setVisible(false);
		preferenceDivisibleSpinner.setVisible(false);
		randomFileSizeCheckBox.setVisible(true);
		stepPrefLabel.setVisible(false);
		stepPrefSpinner.setVisible(false);
		dispersionLabel.setVisible(true);
		dispersionSpinner.setVisible(true);
		generateFileLabel.setVisible(true);
		generateFileSpinner.setVisible(true);
		unitsLabel1.setVisible(randomFileSizeCheckBox.isSelected());
		unitsLabel2.setVisible(randomFileSizeCheckBox.isSelected());
		requestCountSpinner.setVisible(true);
		zipfLambdaLabel.setVisible(false);
		zipfLamdbaSpinner.setVisible(false);
		requestsCountLabel.setVisible(true);
	}

	/**
	 * metoda pro zobrazeni tlacitek pro vstup pro nahodny vstup s preferencemi
	 */
	private void showPrefRandomInput() {
		simConsistencyCheck.setVisible(false);
		simConsistencyCheck.setSelected(false);
		randomFileSizeCheckBox.setEnabled(false);
		randomFileSizeCheckBox.setSelected(true);
		maenValueGaussSpinner.setVisible(false);
		meanValueGaussLabel.setVisible(false);
		maxFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		maxGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		minFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		minGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		nonPrefLabel.setVisible(true);
		nonPrefSpinner.setVisible(true);
		pathLabel.setVisible(false);
		pathTextField.setVisible(false);
		preferenceDivisibleLabel.setVisible(true);
		preferenceDivisibleSpinner.setVisible(true);
		randomFileSizeCheckBox.setVisible(true);
		stepPrefLabel.setVisible(true);
		stepPrefSpinner.setVisible(true);
		dispersionLabel.setVisible(false);
		dispersionSpinner.setVisible(false);
		generateFileLabel.setVisible(true);
		generateFileSpinner.setVisible(true);
		unitsLabel1.setVisible(randomFileSizeCheckBox.isSelected());
		unitsLabel2.setVisible(randomFileSizeCheckBox.isSelected());
		requestCountSpinner.setVisible(true);
		requestsCountLabel.setVisible(true);
		zipfLambdaLabel.setVisible(false);
		zipfLamdbaSpinner.setVisible(false);
	}

	/**
	 * metoda pro schovani vsech ovladacich prvku pro vstupni pozadavky
	 */
	private void hideAllInput() {
		simConsistencyCheck.setVisible(false);
		simConsistencyCheck.setSelected(false);
		maenValueGaussSpinner.setVisible(false);
		maxFileSizeLabel.setVisible(false);
		maxGenFileSizejSpinner.setVisible(false);
		meanValueGaussLabel.setVisible(false);
		minFileSizeLabel.setVisible(false);
		minGenFileSizejSpinner.setVisible(false);
		nonPrefLabel.setVisible(false);
		nonPrefSpinner.setVisible(false);
		pathLabel.setVisible(false);
		pathTextField.setVisible(false);
		preferenceDivisibleLabel.setVisible(false);
		preferenceDivisibleSpinner.setVisible(false);
		randomFileSizeCheckBox.setVisible(false);
		stepPrefLabel.setVisible(false);
		stepPrefSpinner.setVisible(false);
		dispersionLabel.setVisible(false);
		dispersionSpinner.setVisible(false);
		generateFileLabel.setVisible(false);
		generateFileSpinner.setVisible(false);
		unitsLabel1.setVisible(false);
		unitsLabel2.setVisible(false);
		requestCountSpinner.setVisible(false);
		requestsCountLabel.setVisible(false);
		zipfLambdaLabel.setVisible(false);
		zipfLamdbaSpinner.setVisible(false);
	}

	/**
	 * metoda pro zobrazeni nastaveni zipf generatoru vstupu
	 */
	private void showZipfInput() {
		simConsistencyCheck.setVisible(false);
		simConsistencyCheck.setSelected(false);
		randomFileSizeCheckBox.setEnabled(false);
		randomFileSizeCheckBox.setSelected(true);
		maenValueGaussSpinner.setVisible(false);
		meanValueGaussLabel.setVisible(false);
		maxFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		maxGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		minFileSizeLabel.setVisible(randomFileSizeCheckBox.isSelected());
		minGenFileSizejSpinner.setVisible(randomFileSizeCheckBox.isSelected());
		nonPrefLabel.setVisible(false);
		nonPrefSpinner.setVisible(false);
		pathLabel.setVisible(false);
		pathTextField.setVisible(false);
		preferenceDivisibleLabel.setVisible(false);
		preferenceDivisibleSpinner.setVisible(false);
		randomFileSizeCheckBox.setVisible(true);
		stepPrefLabel.setVisible(false);
		stepPrefSpinner.setVisible(false);
		dispersionLabel.setVisible(false);
		dispersionSpinner.setVisible(false);
		generateFileLabel.setVisible(true);
		generateFileSpinner.setVisible(true);
		unitsLabel1.setVisible(randomFileSizeCheckBox.isSelected());
		unitsLabel2.setVisible(randomFileSizeCheckBox.isSelected());
		requestCountSpinner.setVisible(true);
		requestsCountLabel.setVisible(true);
		zipfLambdaLabel.setVisible(true);
		zipfLamdbaSpinner.setVisible(true);
	}
	
	/**
	 * metoda pro znepristupneni komponent pro simulaci
	 */
	public void disableComponentsForSimulation() {
		this.simulateButton.setEnabled(false);
		this.simCancelButton.setEnabled(true);
		this.panelsPane.setEnabledAt(3, false);
		this.panelsPane.setEnabledAt(4, false);
		this.panelsPane.setSelectedIndex(0);
		this.panelsPane.setEnabled(false);
		this.settingsPane.setEnabled(false);
		this.requestsSettingsPanel.setEnabled(false);
		for (Component c : requestsSettingsPanel.getComponents()) {
			c.setEnabled(false);
		}
		this.cacheCapacityPanel.setEnabled(false);
		for (Component c : cacheCapacityPanel.getComponents()) {
			c.setEnabled(false);
		}
		this.othersSettingsPanel.setEnabled(false);
		for (Component c : othersSettingsPanel.getComponents()) {
			c.setEnabled(false);
		}
		this.menuBar.setEnabled(false);
		this.fileMenu.setEnabled(false);
		this.simulationMenu.setEnabled(false);
		this.helpMenu.setEnabled(false);
		this.saveCSVButton.setEnabled(false);
		this.saveXLSButton.setEnabled(false);
		this.saveCSVMenuItem.setEnabled(false);
		this.saveXLSMenuItem.setEnabled(false);
		this.saveConsoleMenuItem.setEnabled(false);
		this.printConsoleButton.setEnabled(false);
		this.printConsoleConsistencyButton.setEnabled(false);
		this.saveXLSConsistencyButton.setEnabled(false);
		this.saveCSVConsistencyButton.setEnabled(false);
		this.saveConsoleConsistencyMenuItem.setEnabled(false);
		this.saveXLSConsistencyMenuItem.setEnabled(false);
		this.saveConsoleMenuItem.setEnabled(false);
	}

	/**
	 * metoda pro obnoveni pouzitelnosti komponent po simulaci
	 * 
	 * @param isSimSuccesfull
	 *            prepinac pro udani, zda byla simulace uspesna
	 */
	public void enableComponentsAfterSimulaton(boolean isSimSuccesfull) {
		for (Component c : requestsSettingsPanel.getComponents()) {
			c.setEnabled(true);
		}
		for (Component c : cacheCapacityPanel.getComponents()) {
			c.setEnabled(true);
		}
		for (Component c : othersSettingsPanel.getComponents()) {
			c.setEnabled(true);
		}

		if (requestsInputComboBox.getSelectedIndex() != 1) {
			randomFileSizeCheckBox.setEnabled(false);
		} else {
			randomFileSizeCheckBox.setEnabled(true);
		}
		this.requestsSettingsPanel.setEnabled(true);
		this.cacheCapacityPanel.setEnabled(true);
		this.othersSettingsPanel.setEnabled(true);
		this.settingsPane.setEnabled(true);
		this.fileMenu.setEnabled(true);
		this.helpMenu.setEnabled(true);
		this.menuBar.setEnabled(true);
		this.simulationMenu.setEnabled(true);
		this.panelsPane.setEnabled(true);
		this.simulateButton.setEnabled(true);
		this.simCancelButton.setEnabled(false);
		this.simulationProgressBar.setVisible(false);
		this.saveCSVButton.setEnabled(isSimSuccesfull);
		this.saveXLSButton.setEnabled(isSimSuccesfull);
		this.saveCSVMenuItem.setEnabled(isSimSuccesfull);
		this.saveXLSMenuItem.setEnabled(isSimSuccesfull);
		this.saveConsoleMenuItem.setEnabled(isSimSuccesfull);
		this.printConsoleButton.setEnabled(isSimSuccesfull);
		this.printConsoleConsistencyButton.setEnabled(false);
		this.saveXLSConsistencyButton.setEnabled(false);
		this.saveCSVConsistencyButton.setEnabled(false);
		this.saveConsoleConsistencyMenuItem.setEnabled(false);
		this.saveXLSConsistencyMenuItem.setEnabled(false);
		this.saveConsoleMenuItem.setEnabled(false);
		if (isSimSuccesfull) {
			panelsPane.setEnabledAt(3, true);
			panelsPane.setSelectedIndex(3);
			panelsPane.setEnabled(true);
			if (consSimulation != null){
				panelsPane.setEnabledAt(4, true);
				printConsoleConsistencyButton.setEnabled(true);
				saveXLSConsistencyButton.setEnabled(true);
				saveCSVConsistencyButton.setEnabled(true);
				saveConsoleConsistencyMenuItem.setEnabled(true);
				saveXLSConsistencyMenuItem.setEnabled(true);
				saveConsoleMenuItem.setEnabled(true);
			}
		} else {
			panelsPane.setEnabledAt(3, false);
			panelsPane.setEnabledAt(4, false);
			panelsPane.setSelectedIndex(0);
			panelsPane.setEnabled(true);
		}
	}

	/**
	 * metoda nahraje z global variables nastaveni
	 */
	private void loadValuesFromGlobalVar() {
		slidingWindwowSpinner.setValue((int) (GlobalVariables
				.getCacheCapacityForDownloadWindow() * 100));
		networkSpeedSpinner.setValue(GlobalVariables.getAverageNetworkSpeed());
		statLimitSpinner.setValue(GlobalVariables.getLimitForStatistics());
		maxGenFileSizejSpinner.setValue(GlobalVariables
				.getMaxGeneratedFileSize());
		minGenFileSizejSpinner.setValue(GlobalVariables
				.getMinGeneratedFileSize());
		nonPrefSpinner.setValue(GlobalVariables
				.getFileRequestnNonPreferenceFile());
		stepPrefSpinner
				.setValue(GlobalVariables.getFileRequestPreferenceStep());
		preferenceDivisibleSpinner.setValue(GlobalVariables
				.getFileRequestPreferenceFile());
		dispersionSpinner.setValue(GlobalVariables
				.getFileRequestGeneratorDispersion());
		maenValueGaussSpinner.setValue(GlobalVariables
				.getFileRequestGeneratorMeanValue());
		generateFileSpinner.setValue(GlobalVariables
				.getFileRequestGeneratorMaxValue());
		requestCountSpinner.setValue(GlobalVariables
				.getRequestCountForRandomGenerator());
	}

	/**
	 * metoda pro nacteni consistency control algoritmu
	 */
	private void loadConsistencyAlgorithms() {
		consistencyCheckBoxes = new ArrayList<JCheckBox>();
		String path = MainGUI.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		List<String> list;
		if (path.endsWith(".jar")) {
			list = loadClassInfoFromJar(path,
					"cz/zcu/kiv/cacheSimulator/consistency/");
		} else {
			String sep = System.getProperty("file.separator");
			list = loadClassInfo(path + sep, "cz" + sep + "zcu" + sep + "kiv"
					+ sep + "cacheSimulator" + sep + "consistency" + sep);
		}
		for (String name : list) {
			String[] names = name.split(";");
			javax.swing.JCheckBox novy = new javax.swing.JCheckBox(names[1],
					false);
			novy.setName(names[0]);
			novy.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					consistencyCheckBoxActionPerformed(evt);
				}
			});
			novy.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					consistencyCheckBoxMouseMove(e);
				}
			});
			novy.setSelected(false);
			consistencyPanel.add(novy);
			consistencyCheckBoxes.add(novy);
		}
	}

	/**
	 * metoda pro nahrani zaskrtavatek pro cache algoritmy
	 */
	private void loadCaches() {
		cacheCheckBoxes = new ArrayList<JCheckBox>();
		String path = MainGUI.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		List<String> list;
		if (path.endsWith(".jar")) {
			list = loadClassInfoFromJar(path,
					"cz/zcu/kiv/cacheSimulator/cachePolicies/");
		} else {
			String sep = System.getProperty("file.separator");
			list = loadClassInfo(path + sep, "cz" + sep + "zcu" + sep + "kiv"
					+ sep + "cacheSimulator" + sep + "cachePolicies" + sep);
		}

		// nacteni cache policies do checkboxlistu
		for (String name : list) {
			String[] names = name.split(";");
			javax.swing.JCheckBox novy = new javax.swing.JCheckBox(names[1],
					false);
			novy.setName(names[0]);
			novy.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					cacheCheckBoxActionPerformed(evt);
				}
			});
			novy.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					cacheCheckBoxMouseMove(e);
				}
			});
			novy.setSelected(true);
			cachePanel.add(novy);
			cacheCheckBoxes.add(novy);
		}

		cachePanel.invalidate();
		cachePanel.repaint();
	}

	/**
	 * metoda pro nacteni jmen trid, ktere vykresluji panely pro ruzna nastaveni
	 */
	private void loadSettingsPanelNames() {
		this.settingsPanels = new ArrayList<JPanel>();
		List<String> list;
		String path = MainGUI.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		if (path.endsWith(".jar")) {
			list = loadClassInfoFromJar(path, "cz/zcu/kiv/cacheSimulator/gui/");
		} else {
			String sep = System.getProperty("file.separator");
			list = (ArrayList<String>) loadClassInfo(path + sep, "cz" + sep
					+ "zcu" + sep + "kiv" + sep + "cacheSimulator" + sep
					+ "gui" + sep);
		}
		for (String panelName : list) {
			try {
				settingsPanels.add((JPanel) Class.forName(panelName)
						.newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * metoda pro nacteni cache simulatoru z jar souboru
	 * 
	 * @param path
	 *            cesta k jar souboru
	 */
	private List<String> loadClassInfoFromJar(String path, String packageName) {
		List<String> classNames = GlobalMethods.getJarClassNames(path,
				packageName);
		List<String> classInfo = new ArrayList<String>();
		for (String className : classNames) {
			if (className.contains("ICache")
					|| className.contains("IConsistency")
					|| className.contains("Data")) {
				continue;
			}
			try {
				URL url = new URL("jar:file:/" + path + "!/");
				URLClassLoader ucl = new URLClassLoader(new URL[] { url });

				Class<?> myClass = Class.forName(className.replace("/", "."),
						true, ucl);
				Object newObject = null;
				// chceme pouze tridy s konstruktory bez parametru
				Constructor<?>[] cons = myClass.getConstructors();
				if (cons.length == 1 && cons[0].getParameterTypes().length == 0) {
					newObject = myClass.newInstance();
				} else {
					continue;
				}

				newObject = myClass.newInstance();
				if (newObject instanceof ICache) {
					classInfo.add(((ICache) newObject).cacheInfo());
				}
				if (newObject instanceof IConsistencySimulation) {
					classInfo.add(((IConsistencySimulation) newObject)
							.getInfo());
				}
				if (className.contains("Panel")) {
					classInfo.add(myClass.getName());
				}
			} catch (InstantiationException ex) {
				Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (IllegalAccessException ex) {
				Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE,
						null, ex);
				ex.printStackTrace();
			}
		}
		return classInfo;
	}

	/**
	 * metoda pro nacteni cache policies z adresare
	 * 
	 * @param path
	 *            cesta
	 */
	private List<String> loadClassInfo(String path, String packageName) {
		List<String> classInfo = new ArrayList<String>();
		String pathToPackage = (path + packageName);
		File dir = new File(pathToPackage);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile() && !file.getName().startsWith("I")
					&& !file.getName().contains("$")
					&& !file.getName().contains("Data")
					&& !file.getName().contains("MainGUI")) {
				try {
					try {
						Class<?> myClass = Class.forName(packageName.replace(
								'/', '.').replace('\\', '.')
								+ file.getName().substring(0,
										file.getName().lastIndexOf(".class")));
						Object newObject = null;
						// chceme pouze tridy s konstruktory bez parametru
						Constructor<?>[] cons = myClass.getConstructors();
						if (cons.length == 1
								&& cons[0].getParameterTypes().length == 0) {
							newObject = myClass.newInstance();
						} else {
							continue;
						}

						if (newObject instanceof ICache) {
							classInfo.add(((ICache) newObject).cacheInfo());
						}
						if (newObject instanceof IConsistencySimulation) {
							classInfo.add(((IConsistencySimulation) newObject)
									.getInfo());
						}
						if (file.getName().contains("Panel")) {
							classInfo.add(myClass.getName());
						}
					} catch (InstantiationException ex) {
						Logger.getLogger(MainGUI.class.getName()).log(
								Level.SEVERE, null, ex);
					} catch (IllegalAccessException ex) {
						Logger.getLogger(MainGUI.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				} catch (ClassNotFoundException ex) {
					Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE,
							null, ex);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return classInfo;
	}

	/**
	 * Creates new form MainGUI
	 */
	private MainGUI() {
		initComponents();
		hideAllInput();
		loadCaches();
		loadConsistencyAlgorithms();
		loadValuesFromGlobalVar();
		loadSettingsPanelNames();
		centerOnScreen();
		simulationProgressBar.setVisible(false);
	}

	/**
	 * metoda pro vycentrovani framu na obrazovce
	 */
	private void centerOnScreen() {
		// center frame on screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Determine the new location of the window
		int w = this.getSize().width;
		int h = this.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;

		// Move the window
		this.setLocation(x, y);
	}
	
	/**
	 * obsluha udalosti pro spusteni simulace
	 * @param evt
	 */
	private void simulateActionPerformed(java.awt.event.ActionEvent evt) {
		runSimulation();
	}
	
	/**
	 * obsluha udalosti predcasneho ukonceni simulace
	 * @param evt
	 */
	private void simCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (simulationThread != null) {
			simulationThread.stopSimulation();
		}

		//simulationThread = null;
		//cacheResults = null;

		//Server.getInstance().hardReset();

		// nastaveni nahodnych generatoru cisel
	//	GlobalMethods.setGenerators();

		enableComponentsAfterSimulaton(false);
	}
	

	/**
	 * metoda pro obsluhu stisku tlacitka pro ukonceni simulatoru
	 * @param evt
	 */
	private void exitActionPerformed(java.awt.event.ActionEvent evt) {
		ConfigReaderWriter.write();
		System.exit(0);
	}
	
	/**
	 * obsluha udalosti uzavirani formulare
	 * @param evt
	 */
	private void formWindowClosing(java.awt.event.WindowEvent evt) {
		ConfigReaderWriter.write();
	}

	/**
	 * meotoda pro spusteni simualce
	 */
	private void runSimulation() {
		if (checkSettings()) {
			simulationThread = null;
			cacheResults = null;
			consSimulation = null;

			Server.getInstance().hardReset();

			// nastaveni nahodnych generatoru cisel
			GlobalMethods.setGenerators();

			// vytvoreni objektu generatoru pristupovanych souboru
			IFileQueue fileQueue = null;
			if (requestsInputComboBox.getSelectedIndex() == 2) {
				fileQueue = new GaussianFileNameGenerator(
						GlobalVariables.getRequestCountForRandomGenerator());
			} else if (requestsInputComboBox.getSelectedIndex() == 3) {
				fileQueue = new RandomFileNameGenerator(
						GlobalVariables.getRequestCountForRandomGenerator());
			} else if (requestsInputComboBox.getSelectedIndex() == 4) {
				fileQueue = new RandomFileNameGeneratorWithPrefences(
						GlobalVariables.getRequestCountForRandomGenerator());
			} else if (requestsInputComboBox.getSelectedIndex() == 5) {
				fileQueue = new ZipfFileNameGenerator(
						GlobalVariables.getRequestCountForRandomGenerator());
			} else if (GlobalVariables.isLoadDataFromLog()) {
				fileQueue = new LogReaderAFS(
						!randomFileSizeCheckBox.isSelected());
			}

			consSimulation = selectConsistencyControl();

			
			// velikosti cache
			final Integer[] sizes = ((myCacheCapacityListModel) cacheCapacityList
					.getModel()).getArray();

			// nastaveni progress baru
			simulationProgressBar.setMinimum(0);
			simulationProgressBar.setMaximum(sizes.length);
			simulationProgressBar.setValue(0);
			simulationProgressBar.setVisible(true);
			simulationProgressBar.setStringPainted(true);
			simulationProgressBar.setString("Simulation in progress... 0%");

			// vlakno pro spusteni simulace - kvuli updatu progressbaru
			simulationThread = new AccessSimulation(fileQueue,
					consSimulation, this);			

			// spusteni vlakna simulace
			simulationThread.start();
		}
	}
	
	/**
	 * Metoda pro test chyb v nastaveni
	 * 
	 * @return true, pokud vse v poradku
	 */
	private boolean checkSettings() {
		// kontrola vyberu vstupni metody
		if (requestsInputComboBox.getSelectedItem().equals("-- Choose one --")) {
			JOptionPane.showMessageDialog(this,
					"You have to select input request method!", "Alert",
					JOptionPane.ERROR_MESSAGE);
			panelsPane.setSelectedIndex(0);
			return false;
		} else if (requestsInputComboBox.getSelectedItem().equals(
				"From AFS log file")) {
			File f = new File(GlobalVariables.getLogAFSFIleName());
			if (!f.isFile()) {
				JOptionPane.showMessageDialog(this,
						"You have to select AFS log file!", "Alert",
						JOptionPane.ERROR_MESSAGE);
				panelsPane.setSelectedIndex(0);
				return false;
			}
		}

		// kontrola zaskrtavatek u cache algoritmu
		if (getCachesNames() == null || getCachesNames().length == 0) {
			JOptionPane.showMessageDialog(this,
					"You have to select simulated cache algorithms!", "Alert",
					JOptionPane.ERROR_MESSAGE);
			panelsPane.setSelectedIndex(1);
			return false;
		}

		// kontrola consistency control
		if (simConsistencyCheck.isSelected()) {
			boolean selected = false;
			for (JCheckBox check : consistencyCheckBoxes) {
				if (check.isSelected()) {
					selected = true;
					break;
				}
			}
			if (!selected) {
				JOptionPane.showMessageDialog(this,
						"You have to select consistency control algorithm!",
						"Alert", JOptionPane.ERROR_MESSAGE);
				panelsPane.setSelectedIndex(2);
				return false;
			}
		}
		return true;
	}


	/**
	 * metoda pro nahrani vysledku mereni konsistentnosti do tabulky
	 * @param cons konsistentnost
	 */
	@SuppressWarnings("unchecked")
	public void loadConResultsToPanel(IConsistencySimulation cons) {
		if (cons == null)
			return;
		userListConsistency.setModel(new myUsersListModel());	
		cachePolList.setModel(new myCacheNamesListModel());
		
		userListConsistency.setSelectedIndex(0);
		cachePolList.setSelectedIndex(0);
	}
	
	/**
	 * metoda pro nahrani vysledku do panelu cache results
	 */
	@SuppressWarnings("unchecked")
	public void loadResultsToPanel() {
		// nacteni uzivatelu
		userList.setModel(new myUsersListModel());
		userList.setSelectedIndex(0);

		// oznaceni prvniho
		resultsChangeCombo.setSelectedIndex(0);

		// otevreni panelu s vysledky
		panelsPane.setEnabledAt(3, true);

		// nahrani vysledku do tabulky
		loadResultsToTable();
	}
	
	/**
	 * metoda pro nacteni vysledku do tabulky
	 */
	private void loadResultsToTable() {
		if (cacheResults == null || cacheResults.isEmpty())
			return;
		if (userList.getSelectedIndex() < 0)
			return;
		totalNetTextField
				.setText(Long.toString(cacheResults.get(
						userList.getSelectedIndex()).getTotalNetworkBandwidth() / 1024 / 1024));
		totalReqTextField.setText(Long.toString(cacheResults.get(
				userList.getSelectedIndex()).getFileAccessed()));
		UserStatistics stat = cacheResults.get(userList.getSelectedIndex());

		String[] cacheNames = stat.getCacheNames();

		String names[] = new String[getCacheSizes().length + 1];
		names[0] = "Cache Policy";
		int index = 1;
		for (Integer cap : stat.getCacheSizes()) {
			names[index++] = cap + "MB";
		}

		Object[] row;
		int length = cacheNames.length;
		Object[][] rowData = new Object[length][stat.getCacheSizes().length + 1];

		for (int i = 0; i < length; i++) {
			if (resultsChangeCombo.getSelectedIndex() == 0) {

				row = stat.getCacheHitRatios(cacheNames[i]);
			} else if (resultsChangeCombo.getSelectedIndex() == 1) {
				row = stat.getCacheHits(cacheNames[i]);
			} else if (resultsChangeCombo.getSelectedIndex() == 2) {
				row = stat.getCacheSavedBytesRatio(cacheNames[i]);
			} else if ((resultsChangeCombo.getSelectedIndex() == 3)) {
				row = stat.getSavedBytes(cacheNames[i]);
			} else if ((resultsChangeCombo.getSelectedIndex() == 5)) {
				row = stat.getDataTransferDegrease(cacheNames[i]);
			} else {
				row = stat.getDataTransferDegreaseRatio(cacheNames[i]);
			}
			rowData[i][0] = cacheNames[i];

			for (int j = 1; j < names.length; j++) {
				rowData[i][j] = row[j - 1];
			}
		}

		TableModel tm = new DefaultTableModel(rowData, names);
		resultsTable.setModel(tm);
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tm);
		resultsTable.setRowSorter(sorter);
	}

	/**
	 * metoda pro ziskani consistency control algoritmu
	 * 
	 * @return vybrana metoda
	 */
	private IConsistencySimulation selectConsistencyControl() {
		if (!simConsistencyCheck.isSelected())
			return null;
		String consName = "";
		for (JCheckBox check : consistencyCheckBoxes) {
			if (check.isSelected()) {
				consName = check.getName();
				break;
			}
		}
		if (consName.length() == 0)
			return null;
		try {
			return (IConsistencySimulation) Class.forName(
					"cz.zcu.kiv.cacheSimulator.consistency." + consName)
					.newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * obslua udalosti pro vyber vstupniho logovaciho souboru z AFS
	 * @param evt
	 */
	private void pathTextFieldMouseClicked(java.awt.event.MouseEvent evt) {
		chooseAFSFile();
	}
	
	/**
	 * metoda pro vybrani souboru pro vstup
	 */
	private void chooseAFSFile() {
		JFileChooser fc = new JFileChooser(GlobalVariables.getActDir());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Open AFS log file...");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pathTextField.setText(fc.getSelectedFile().getAbsolutePath());
			GlobalVariables.setLogAFSFIleName(fc.getSelectedFile()
					.getAbsolutePath());
			GlobalVariables.setActDir(fc.getSelectedFile().getPath());
		} else {
			JOptionPane
					.showMessageDialog(
							this,
							"You have to choose input file before starting simulation!\nYou can choose input file by clicking Path text area",
							"Alert", JOptionPane.OK_OPTION);
		}
	}

	/**
	 * obsluha udalosti zmena moznosti generovani nahodne vel�ikosti souboru (zaskrtavatko)
	 * @param evt
	 */
	private void randomFileSizeCheckBoxItemStateChanged(
			java.awt.event.ItemEvent evt) {
		GlobalVariables.setRandomFileSizesForLoggedData(randomFileSizeCheckBox
				.isSelected());
		if (randomFileSizeCheckBox.isSelected()) {
			minFileSizeLabel.setVisible(true);
			maxFileSizeLabel.setVisible(true);
			unitsLabel2.setVisible(true);
			unitsLabel1.setVisible(true);
			minGenFileSizejSpinner.setVisible(true);
			maxGenFileSizejSpinner.setVisible(true);
		} else {
			minFileSizeLabel.setVisible(false);
			maxFileSizeLabel.setVisible(false);
			unitsLabel2.setVisible(false);
			unitsLabel1.setVisible(false);
			minGenFileSizejSpinner.setVisible(false);
			maxGenFileSizejSpinner.setVisible(false);
		}
	}

	/**
	 * obsluha udalosti vyber vstupu simulator (soubor x random)
	 * @param evt
	 */
	private void requestsInputComboBoxItemStateChanged(
			java.awt.event.ItemEvent evt) {

		if (evt.getStateChange() == ItemEvent.DESELECTED) {
			return;
		}
		String ret = (String) evt.getItem();

		if (ret.equalsIgnoreCase("-- Choose one --")) {
			hideAllInput();
			GlobalVariables.setLoadDataFromLog(false);
		} else if (ret.equalsIgnoreCase("From afs log file")) {
			GlobalVariables.setLoadDataFromLog(true);
			showInputFile();
			chooseAFSFile();
		} else if (ret.equalsIgnoreCase("Gaussian random")) {
			GlobalVariables.setLoadDataFromLog(false);
			GlobalVariables.setRandomFileSizesForLoggedData(true);
			showGaussInput();
		} else if (ret.equalsIgnoreCase("Random with preference")) {
			GlobalVariables.setLoadDataFromLog(false);
			GlobalVariables.setRandomFileSizesForLoggedData(true);
			showPrefRandomInput();
		} else if (ret.equalsIgnoreCase("Zipf random")) {
			GlobalVariables.setLoadDataFromLog(false);
			GlobalVariables.setRandomFileSizesForLoggedData(true);
			showZipfInput();
		} else {
			GlobalVariables.setLoadDataFromLog(false);
			GlobalVariables.setRandomFileSizesForLoggedData(true);
			showRandomInput();
		}
	}

	/**
	 * obsluha udalosti pridani nove kapacity cache
	 * @param evt
	 */
	@SuppressWarnings("unchecked")
	private void cachePlusButtonActionPerformed(java.awt.event.ActionEvent evt) {
		Integer newCacheCap = (Integer) cacheCapSpinner.getValue();
		if (newCacheCap <= 0) {
			JOptionPane.showMessageDialog(this,
					"You have to insert positive integer!", "Alert",
					JOptionPane.OK_OPTION);
			return;
		}
		myCacheCapacityListModel model = (myCacheCapacityListModel) cacheCapacityList
				.getModel();
		Integer[] array = model.getArray();
		for (int i = 0; i < array.length; i++) {
			if (newCacheCap.compareTo(array[i]) == 0) {
				JOptionPane.showMessageDialog(this,
						"You have to insert different value!", "Alert",
						JOptionPane.OK_OPTION);
				return;
			}
		}
		Integer newArray[] = new Integer[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[newArray.length - 1] = new Integer(newCacheCap);
		Arrays.sort(newArray);

		cacheCapacityList.setModel(new myCacheCapacityListModel(newArray));
		cacheCapacityList.invalidate();
		cacheCapacityList.repaint();

	}

	/**
	 * metoda pro odebrani polozky z cache capacity
	 * 
	 * @param evt
	 */
	private void cacheMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (cacheCapacityList.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(this,
					"You have to select cache capacity to dismiss!", "Alert",
					JOptionPane.OK_OPTION);
			return;
		}
		myCacheCapacityListModel model = (myCacheCapacityListModel) cacheCapacityList
				.getModel();
		model.remove(cacheCapacityList.getSelectedIndex());
		cacheCapacityList.invalidate();
		cacheCapacityList.repaint();
	}

	/**
	 * obsluha udalosti zmena poctu pozadavku, aby byl vysledek zahrnut
	 * @param evt
	 */
	private void statLimitSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		if ((Integer) statLimitSpinner.getValue() < 1) {
			statLimitSpinner.setValue(1);
		}
		GlobalVariables.setLimitForStatistics((Integer) statLimitSpinner
				.getValue());
	}

	/**
	 * obsluha udalosti zmena rychlosti site - pro soubory, ktere jsou vetsi nez cache
	 * @param evt
	 */
	private void networkSpeedSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) networkSpeedSpinner.getValue() < 1) {
			networkSpeedSpinner.setValue(1);
		} else if ((Integer) networkSpeedSpinner.getValue() > 10000) {
			networkSpeedSpinner.setValue(10000);

		}
		GlobalVariables.setAverageNetworkSpeed((Integer) networkSpeedSpinner
				.getValue());
	}

	/**
	 * obsluha udalosti zmena velisti klouzajiciho okenka - pro soubory, ktere jseou vetsi nez cache
	 * @param evt
	 */
	private void slidingWindwowSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) slidingWindwowSpinner.getValue() < 0) {
			slidingWindwowSpinner.setValue(0);
		} else if ((Integer) slidingWindwowSpinner.getValue() > 75) {
			slidingWindwowSpinner.setValue(75);
		}
		GlobalVariables
				.setCacheCapacityForDownloadWindow(((Integer) slidingWindwowSpinner
						.getValue()).intValue());
	}

	/**
	 * obsluha udalosti zmena maximalni velikosti generovanych souboru
	 * @param evt
	 */
	private void maxGenFileSizejSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) maxGenFileSizejSpinner.getValue() < 0) {
			maxGenFileSizejSpinner.setValue(0);
		} else if ((Integer) maxGenFileSizejSpinner.getValue() <= (Integer) minGenFileSizejSpinner
				.getValue()) {
			maxGenFileSizejSpinner.setValue((Long) minGenFileSizejSpinner
					.getValue() + 1);
		}
		GlobalVariables
				.setMaxGeneratedFileSize((Integer) maxGenFileSizejSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena minimalni velikosti generovanych souboru
	 * @param evt
	 */
	private void minGenFileSizejSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) minGenFileSizejSpinner.getValue() < 0) {
			minGenFileSizejSpinner.setValue(0);
		} else if ((Integer) maxGenFileSizejSpinner.getValue() <= (Integer) minGenFileSizejSpinner
				.getValue()) {
			minGenFileSizejSpinner.setValue((Integer) maxGenFileSizejSpinner
					.getValue() - 1);
		}
		GlobalVariables
				.setMinGeneratedFileSize((Integer) minGenFileSizejSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena zaskrtnuti policka pro nahodne generovani velikosti souboru
	 * @param evt
	 */
	private void randomFileSizeCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		GlobalVariables.setRandomFileSizesForLoggedData(randomFileSizeCheckBox
				.isSelected());
	}

	/**
	 * obsluha udalosti zmena spinneru u nepreferovanych souboru v pref. generatoru
	 * @param evt
	 */
	private void nonPrefSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) nonPrefSpinner.getValue() <= 0) {
			nonPrefSpinner.setValue(1);
		}
		GlobalVariables
				.setFileRequestnNonPreferenceFile((Integer) nonPrefSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru kroku v pref. generatoru
	 * @param evt
	 */
	private void stepPrefSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		if ((Integer) stepPrefSpinner.getValue() <= 0) {
			stepPrefSpinner.setValue(1);
		}
		GlobalVariables.setFileRequestPreferenceFile((Integer) stepPrefSpinner
				.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru u delitelnosti pref. souboru souboru v pref. generatoru
	 * @param evt
	 */
	private void preferenceDivisibleSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) preferenceDivisibleSpinner.getValue() <= 0) {
			preferenceDivisibleSpinner.setValue(1);
		}
		GlobalVariables
				.setFileRequestPreferenceStep((Integer) preferenceDivisibleSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru pro disperzi u Gauss. generatoru
	 * @param evt
	 */
	private void dispersionSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		if ((Integer) dispersionSpinner.getValue() <= 0) {
			dispersionSpinner.setValue(1);
		}
		GlobalVariables
				.setFileRequestGeneratorDispersion((Integer) dispersionSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru pro stredni hodnotu u Gauss. generatoru
	 * @param evt
	 */
	private void maenValueGaussSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) maenValueGaussSpinner.getValue() <= 0) {
			maenValueGaussSpinner.setValue(1);
		}
		GlobalVariables
				.setFileRequestGeneratorMeanValue((Integer) maenValueGaussSpinner
						.getValue());
	}
	

	/**
	 * obsluha udalosti zmeny alfa spinneru u zipf generatoru
	 * @param evt
	 */
	private void zipfLamdbaSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
		GlobalVariables.setZipfLambda((Double) zipfLamdbaSpinner.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru pro zmenu poctu generovanch souboru
	 * @param evt
	 */
	private void generateFileSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) generateFileSpinner.getValue() <= 0) {
			generateFileSpinner.setValue(1);
		}
		GlobalVariables
				.setFileRequestGeneratorMaxValue((Integer) generateFileSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti zmena spinneru pro zmenu postu generovanych pozadavku
	 * @param evt
	 */
	private void requestCountSpinnerStateChanged(
			javax.swing.event.ChangeEvent evt) {
		if ((Integer) requestCountSpinner.getValue() <= 0) {
			requestCountSpinner.setValue(1);
		}
		GlobalVariables
				.setRequestCountForRandomGenerator((Integer) requestCountSpinner
						.getValue());
	}

	/**
	 * obsluha udalosti vyberu cachovaci politiky (zaskrtnuti)
	 * @param evt
	 */
	private void cachePolListValueChanged(ListSelectionEvent evt) {
		int cacheIndex = 0, userIndex = 0;
		if (cachePolList.getSelectedIndex() >= 0) cacheIndex = cachePolList.getSelectedIndex();
		if (userListConsistency.getSelectedIndex() >= 0) userIndex = userListConsistency.getSelectedIndex();
		String cache = ((myCacheNamesListModel)cachePolList.getModel()).cacheClass[cacheIndex];
		long userID = ((myUsersListModel)userListConsistency.getModel()).userIDs[userIndex];
		
		Object[][] data = consSimulation.getData(cache, userID);
		TableModel tm = null;
		if (data != null)
			tm = new DefaultTableModel(data, consSimulation.getHeaders());
		else{
			DefaultTableModel dtm = new DefaultTableModel(0, 0);
			dtm.addColumn("No data for visualisation!");
			tm = dtm;
		}
		consistencyTable.setModel(tm);
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tm);
		consistencyTable.setRowSorter(sorter);
	}

	/**
	 * obsluha udalosti vyberu consistency control (zaskrtnuti)
	 * @param evt
	 */
	private void userListConsistencyValueChanged(ListSelectionEvent evt) {
		int cacheIndex = 0, userIndex = 0;
		if (cachePolList.getSelectedIndex() >= 0) cacheIndex = cachePolList.getSelectedIndex();
		if (userListConsistency.getSelectedIndex() >= 0) userIndex = userListConsistency.getSelectedIndex();
		String cache = ((myCacheNamesListModel)cachePolList.getModel()).cacheClass[cacheIndex];
		long userID = ((myUsersListModel)userListConsistency.getModel()).userIDs[userIndex];
		
		Object[][] data = consSimulation.getData(cache, userID);
		TableModel tm = null;
		if (data != null)
			tm = new DefaultTableModel(data, consSimulation.getHeaders());
		else{
			DefaultTableModel dtm = new DefaultTableModel(0, 0);
			dtm.addColumn("No data for visualisation!");
			tm = dtm;
		}
		consistencyTable.setModel(tm);
		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tm);
		consistencyTable.setRowSorter(sorter);
	}

	/**
	 * obsluha udalosti vyberu vysledku u cache policies (ration, saved bytes...)
	 * @param evt
	 */
	private void resultsChangeComboActionPerformed(
			java.awt.event.ActionEvent evt) {
		loadResultsToTable();
	}

	/**
	 * obsluha udalosti vyberu vysledku simulovaneho uzivatele
	 * @param evt
	 */
	private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		loadResultsToTable();
	}

	/**
	 * obsluha udalosti zaskrtnuti tlkacitka, kdy se nejpre nactou statistiky na server a az pak se simuluje
	 * @param evt
	 */
	private void loadServStatActionPerformed(java.awt.event.ActionEvent evt) {
		GlobalVariables.setLoadServerStatistic(loadServStat.isSelected());
	}

	/**
	 * obsluha udalosti zaskrtnuti moznosti simulace consistency control
	 * @param evt
	 */
	private void simConsistencyCheckedChanged(java.awt.event.ItemEvent evt) {
		panelsPane.setEnabledAt(2, simConsistencyCheck.isSelected());
		if (simConsistencyCheck.isSelected()) {
			loadServStat.setEnabled(false);
			loadServStat.setSelected(true);
			GlobalVariables.setLoadServerStatistic(loadServStat.isSelected());
		} else {
			loadServStat.setEnabled(true);
			GlobalVariables.setLoadServerStatistic(loadServStat.isSelected());
		}
	}


	/**
	 * metoda pro obsluhu udalosti stisknuti tlacitka pro ulozeni vysledku policies do CVS souboru  
	 * @param evt
	 */
	private void saveCSVActionPerformed(java.awt.event.ActionEvent evt) {
		String fName = getFileNameForSavingResults(".csv", "CVS Files");
		if (fName.length() == 0)
			return;
		if (!fName.endsWith(".csv")) {
			fName = fName + ".csv";
		}
		Output.saveStatToCSV(fName, this.cacheResults);
		GlobalVariables.setActDir(fName);
	}

	/**
	 * metoda pro obsluhu udalosti stisknuti tlacitka pro ulozeni vysledku policies do XLS souboru  
	 * @param evt
	 */
	private void saveXLSActionPerformed(java.awt.event.ActionEvent evt) {
		String fName = getFileNameForSavingResults(".xls", "XLS Files");
		if (fName.length() == 0)
			return;
		if (!fName.endsWith(".xls")) {
			fName = fName + ".xls";
		}
		GlobalVariables.setActDir(fName);
		Output.saveStatToXLS(fName, this.cacheResults);
	}

	/**
	 * metoda pro obsluhu vytisteni vysledku policies do konzole
	 * @param evt
	 */
	private void printConsoleActionPerformed(
			java.awt.event.ActionEvent evt) {
		Output.printAllStatConsole(cacheResults);
	}
	
	/**
	 * metoda pro obsluhu stisku tlacitek pro tisk vysledku consistency do konzole 
	 * @param evt
	 */
	protected void printConsoleConsistencyActionPerformed(ActionEvent evt) {
		Output.printAllStatConsoleConsistency(consSimulation, ((myCacheNamesListModel)cachePolList.getModel()).cacheClass, ((myCacheNamesListModel)cachePolList.getModel()).cacheNames, ((myUsersListModel)userListConsistency.getModel()).userIDs);
	}

	/**
	 * metoda pro obsluhu stisku tlacitek pro ulozenivysledku consistency do XLS souboru 
	 * @param evt
	 */
	protected void saveXLSConsistencyActionPerformed(ActionEvent evt) {
		String fName = getFileNameForSavingResults(".xls", "XLS files");
		if (fName.length() == 0)
			return;
		if (!fName.endsWith(".xls")) {
			fName = fName + ".xls";
		}
		Output.saveConsistencyControlXLS(consSimulation, ((myCacheNamesListModel)cachePolList.getModel()).cacheClass, ((myCacheNamesListModel)cachePolList.getModel()).cacheNames, ((myUsersListModel)userListConsistency.getModel()).userIDs, fName);
		GlobalVariables.setActDir(fName);
	}

	/**
	 * metoda pro obsluhu stisku tlacitek pro ulozenivysledku consistency do CSV souboru 
	 * @param evt
	 */
	protected void saveCSVConsistencyActionPerformed(ActionEvent evt) {
		String fName = getFileNameForSavingResults(".csv", "CSV files");
		if (fName.length() == 0)
			return;
		if (!fName.endsWith(".csv")) {
			fName = fName + ".csv";
		}
		Output.saveConsistencyControlCSV(consSimulation, ((myCacheNamesListModel)cachePolList.getModel()).cacheClass, ((myCacheNamesListModel)cachePolList.getModel()).cacheNames, ((myUsersListModel)userListConsistency.getModel()).userIDs, fName);
		GlobalVariables.setActDir(fName);
	}
	
	/**
	 * metoda pro nacteni jmena souboru pto ulozeni vysledku
	 * @param endings koncovka souboru
	 * @param description popis souboru
	 * @return jmeno suoboru vcetne cesty pro ulozeni
	 */
	private String getFileNameForSavingResults(final String endings, final String description){
		JFileChooser fc = new JFileChooser(GlobalVariables.getActDir()) {
			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if (f.exists() && getDialogType() == SAVE_DIALOG) {
					int result = JOptionPane.showConfirmDialog(this,
							"The file exists, overwrite?", "Existing file",
							JOptionPane.YES_NO_CANCEL_OPTION);
					switch (result) {
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						cancelSelection();
						return;
					}
				} else if (!f.exists()) {
					super.approveSelection();
				}
			}
		};
		fc.setDialogTitle("Save results as");
		FileFilter ff = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isFile()) {
					return f.getAbsolutePath().endsWith(endings);
				}
				return true;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
		fc.setFileFilter(ff);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getAbsolutePath(); 
		}
		return "";
	}

	/**
	 * obsluha udalosti vyberu vstupu z AFS logu z menu
	 * @param evt
	 */
	private void inputAFSMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		panelsPane.setSelectedIndex(0);
		requestsInputComboBox.setSelectedIndex(1);
	}

	/**
	 * obsluha udalosti vyberu vstupu pomoci Gauss. random z menu
	 * @param evt
	 */
	private void inputGaussianMenuItemActionPerformed(
			java.awt.event.ActionEvent evt) {
		panelsPane.setSelectedIndex(0);
		requestsInputComboBox.setSelectedIndex(2);
	}

	/**
	 * obsluha udalosti vyberu vstupu pomoci uniformly random z menu
	 * @param evt
	 */
	private void inputUniformlyMenuItemActionPerformed(
			java.awt.event.ActionEvent evt) {
		panelsPane.setSelectedIndex(0);
		requestsInputComboBox.setSelectedIndex(3);
	}

	/**
	 * obsluha udalosti vyberu vstupu pomoci random s preferenci menu
	 * @param evt
	 */
	private void inputRandomPrefMenuItemActionPerformed(
			java.awt.event.ActionEvent evt) {
		panelsPane.setSelectedIndex(0);
		requestsInputComboBox.setSelectedIndex(4);
	}

	/**
	 * obsluha udalosti vyberu vstupu pomoci zipf random z menu
	 * @param evt
	 */
	private void inputZipfMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		panelsPane.setSelectedIndex(0);
		requestsInputComboBox.setSelectedIndex(5);
	}

	/**
	 * reakce na stisk tlacitka About - vytisteni informaci o autorovi
	 * @param evt
	 */
	private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		JOptionPane
				.showMessageDialog(
						this,
						"<html><center><b>Cache Simulator</b>"
								+ "<p>"
								+ "<b>Pavel Bzoch, 2012, 2013<br/>Department of Computer Science and Engineering<br/>University of West Bohemia<br/>www.kiv.zcu.zcu, www.fav.zcu.cz<br/>pbzoch@kiv.zcu.cz</b>",
						"About", JOptionPane.INFORMATION_MESSAGE);

	}

	/**
	 * obsluha udalosti stisku tlacitka pro vizualizaci sloupcovym grafem
	 * @param evt
	 */
	private void barChartButtonActionPerformed(java.awt.event.ActionEvent evt) {
		final BarChart chart = new BarChart(resultsChangeCombo
				.getSelectedItem().toString(), cacheResults.get(userList
				.getSelectedIndex()), resultsChangeCombo.getSelectedIndex());
		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}
	
	/**
	 * obsluha udalosti stisku tlacitka pro vizualizaci spojnicovym grafem
	 * @param evt
	 */
	private void lineChartButtonActionPerformed(java.awt.event.ActionEvent evt) {
		final LineChart chart = new LineChart(resultsChangeCombo
				.getSelectedItem().toString(), cacheResults.get(userList
				.getSelectedIndex()), resultsChangeCombo.getSelectedIndex());
		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}

	/**
	 * obsluha zmeny tlacitka pro vyber cache algoritmu
	 * 
	 * @param evt
	 */
	private void cacheCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
		String name = ((JCheckBox) evt.getSource()).getName() + "Panel";
		JPanel cacheSettings = noSettingsPanelCache;
		for (JPanel panel : settingsPanels) {
			if (panel.getClass().getName().contains(name)) {
				cacheSettings = panel;
				break;
			}
		}
		cachePane.setRightComponent(cacheSettings);
		cachePane.invalidate();
		cachePane.repaint();
	}

	/**
	 * obsluha udalosti prejeti mysi po zaskrtavatku pro cache simulator
	 * 
	 * @param evt
	 */
	private void cacheCheckBoxMouseMove(java.awt.event.MouseEvent evt) {
		String name = ((JCheckBox) evt.getSource()).getName() + "Panel";
		JPanel cacheSettings = noSettingsPanelCache;
		for (JPanel panel : settingsPanels) {
			if (panel.getClass().getName().contains(name)) {
				cacheSettings = panel;
				break;
			}
		}
		cachePane.setRightComponent(cacheSettings);
		cachePane.invalidate();
		cachePane.repaint();
	}

	/**
	 * obsluha zmeny tlacitka pro vyber consistency control algoritmu
	 * 
	 * @param evt
	 */
	private void consistencyCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		JCheckBox act = null;
		for (JCheckBox check : consistencyCheckBoxes) {
			if (check.isSelected()) {
				act = check;
				break;
			}
		}
		if (act == null) {
			for (JCheckBox check : consistencyCheckBoxes) {
				check.setEnabled(true);
			}
		} else {
			for (JCheckBox check : consistencyCheckBoxes) {
				if (check == act) {
					continue;
				}
				check.setEnabled(false);
			}
		}
	}

	/**
	 * obsluha udalosti prejeti mysi po zaskrtavatku pro consistency control
	 * 
	 * @param evt
	 */
	private void consistencyCheckBoxMouseMove(java.awt.event.MouseEvent evt) {
		String name = ((JCheckBox) evt.getSource()).getName() + "Panel";
		JPanel consistencySettings = noSettingsPanelConsistency;
		for (JPanel panel : settingsPanels) {
			if (panel.getClass().getName().contains(name)) {
				consistencySettings = panel;
				break;
			}
		}
		if (consistencySettings instanceof MMWPBatchConsistencyPanel){
			((MMWPBatchConsistencyPanel) consistencySettings).updateValues();
		}
		else if (consistencySettings instanceof MMWPConsistencyPanel){
			((MMWPConsistencyPanel) consistencySettings).updateValues();
		}
		ConsistencyControlPane.setRightComponent(consistencySettings);
		consistencyPanel.invalidate();
		consistencyPanel.repaint();
	}
	
	/**
	 * trida pro prezentaci modelu pro pole velikosti
	 */
	@SuppressWarnings("rawtypes")
	private class myCacheCapacityListModel extends javax.swing.AbstractListModel {
		Integer[] array;

		@Override
		public int getSize() {
			return array.length;
		}

		@Override
		public Object getElementAt(int i) {
			return array[i];
		}

		/**
		 * metoda pro odebrani polozky
		 * 
		 * @param index
		 *            index polozky
		 */
		public void remove(int index) {
			if (index < 0 || index >= array.length)
				return;
			Integer newAarray[] = new Integer[array.length - 1];
			for (int i = 0; i < array.length; i++) {
				if (i < index) {
					newAarray[i] = array[i];
					continue;
				} else if (i == index)
					continue;
				else
					newAarray[i - 1] = array[i];
			}
			array = newAarray;
		}

		public myCacheCapacityListModel() {
			this.array = new Integer[] { 16, 32, 64, 128, 256, 512, 1024 };
		}

		public myCacheCapacityListModel(Integer[] array) {
			this.array = array;
		}

		/**
		 * metoda pro ziskani pole velikosti
		 * 
		 * @return pole velikosti
		 */
		public Integer[] getArray() {
			return this.array;
		}
	}
	
	/**
	 * trida pro prezentaci modelu pro pole velikosti
	 */
	@SuppressWarnings("rawtypes")
	private class myUsersListModel extends javax.swing.AbstractListModel {
		long[] userIDs;
		String[] userNames;

		@Override
		public int getSize() {
			return userNames.length;
		}

		@Override
		public Object getElementAt(int i) {
			return userNames[i];
		}

		public myUsersListModel() {
			userIDs = new long[cacheResults.size()];
			userNames = new String[cacheResults.size()];
			for (int i = 0; i < cacheResults.size(); i++) {
				userIDs[i] = cacheResults.get(i).getUserID();
				if (userIDs[i] == 0) {
					userNames[i] = "Simulated user";
				} else {
					long id = userIDs[i] >> 32;
					userNames[i] = id + ", ip: "
							+ (GlobalMethods.intToIp(userIDs[i] - (id << 32)));
				}
			}
		}
	}
	
	/**
	 * trida pro prezentaci modelu pro pole velikosti
	 */
	@SuppressWarnings("rawtypes")
	private class myCacheNamesListModel extends javax.swing.AbstractListModel {
		String[] cacheNames;
		String[] cacheClass;

		@Override
		public int getSize() {
			return cacheNames.length;
		}

		@Override
		public Object getElementAt(int i) {
			return cacheNames[i];
		}

		public myCacheNamesListModel() {
			int count = 0;
			for (JCheckBox box: cacheCheckBoxes){
				if (box.isSelected()) count++;
			}
			cacheNames = new String[count];
			cacheClass = new String[count];
			count = 0;
			for (JCheckBox box: cacheCheckBoxes){
				if (box.isSelected()){
					cacheNames[count] = box.getText();
					cacheClass[count] = box.getName();
					count++;
				}
			}
		}
	}

	/**
	 * metoda pro vraceni jmen trid cachi, ktere jsou zaskrtnuty
	 * 
	 * @return pole nazvu cache trid
	 */
	public String[] getCachesNames() {
		if (cacheCheckBoxes == null) {
			return null;
		}
		ArrayList<String> names = new ArrayList<String>();
		for (javax.swing.JCheckBox box : cacheCheckBoxes) {
			if (box.isSelected()) {
				names.add(box.getName());
			}
		}
		String[] ret = new String[names.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = names.get(i);
		}
		return ret;
	}

	/**
	 * metoda pro vraceni pole velikosti cachi
	 * @return velikosti cache
	 */
	public Integer[] getCacheSizes() {
		return ((myCacheCapacityListModel) cacheCapacityList.getModel())
				.getArray();
	}

	// promenne pro kontrolky na gui
	private javax.swing.JSplitPane ConsistencyControlPane;
	private javax.swing.JCheckBox loadServStat;
	private javax.swing.JCheckBox simConsistencyCheck;
	private javax.swing.JMenuItem aboutMenuItem;
	private javax.swing.JButton barChartButton;
	private javax.swing.JSpinner cacheCapSpinner;
	@SuppressWarnings("rawtypes")
	private javax.swing.JList cacheCapacityList;
	private javax.swing.JPanel cacheCapacityPanel;
	private javax.swing.JButton cacheMinusButton;
	private javax.swing.JSplitPane cachePane;
	private javax.swing.JPanel cachePanel;
	private javax.swing.JPanel consistencyPanel;
	private javax.swing.JButton cachePlusButton;
	private javax.swing.JLabel chooseResultsLabel;
	private javax.swing.JLabel dispersionLabel;
	private javax.swing.JSpinner dispersionSpinner;
	private javax.swing.JButton exitButton;
	private javax.swing.JMenuItem exitMenuItem;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JPopupMenu.Separator fileMenuSeparator;
	private javax.swing.JLabel generateFileLabel;
	private javax.swing.JSpinner generateFileSpinner;
	private javax.swing.JMenu helpMenu;
	private javax.swing.JMenuItem inputAFSMenuItem;
	private javax.swing.JMenuItem inputGaussianMenuItem;
	private javax.swing.JMenuItem inputRandomPrefMenuItem;
	private javax.swing.JMenuItem inputZipfMenuItem;
	private javax.swing.JLabel inputRequestLabel;
	private javax.swing.JMenuItem inputUniformlyMenuItem;
	private javax.swing.JLabel unitsLabel3;
	private javax.swing.JScrollPane cacheCapScrollPane;
	private javax.swing.JScrollPane userListScrollPane;
	private javax.swing.JScrollPane resultsTableScrollPane;
	private javax.swing.JToolBar.Separator toolbarSeparator1;
	private javax.swing.JToolBar.Separator toolbarSeparator2;
	private javax.swing.JButton lineChartButton;
	private javax.swing.JSpinner maenValueGaussSpinner;
	private javax.swing.JLabel maxFileSizeLabel;
	private javax.swing.JSpinner maxGenFileSizejSpinner;
	private javax.swing.JLabel meanValueGaussLabel;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JLabel minFileSizeLabel;
	private javax.swing.JSpinner minGenFileSizejSpinner;
	private javax.swing.JLabel networkSpeedLabel;
	private javax.swing.JSpinner networkSpeedSpinner;
	private javax.swing.JLabel networkSpeesLabel2;
	private javax.swing.JLabel noSettingsLabelCache;
	private javax.swing.JPanel noSettingsPanelCache;
	private javax.swing.JLabel noSettingsLabelConsistency;
	private javax.swing.JPanel noSettingsPanelConsistency;
	private javax.swing.JLabel nonPrefLabel;
	private javax.swing.JSpinner nonPrefSpinner;
	private javax.swing.JPanel othersSettingsPanel;
	private javax.swing.JTabbedPane panelsPane;
	private javax.swing.JLabel pathLabel;
	private javax.swing.JTextField pathTextField;
	private javax.swing.JLabel preferenceDivisibleLabel;
	private javax.swing.JSpinner preferenceDivisibleSpinner;
	private javax.swing.JButton printConsoleButton;
	private javax.swing.JCheckBox randomFileSizeCheckBox;
	private javax.swing.JSpinner requestCountSpinner;
	private javax.swing.JMenu requestMenu;
	private javax.swing.JLabel requestsCountLabel;
	@SuppressWarnings("rawtypes")
	private javax.swing.JComboBox requestsInputComboBox;
	private javax.swing.JPanel requestsSettingsPanel;
	@SuppressWarnings("rawtypes")
	private javax.swing.JComboBox resultsChangeCombo;
	private javax.swing.JPanel resultsPane;
	private javax.swing.JTable resultsTable;
	private javax.swing.JButton saveCSVButton;
	private javax.swing.JMenuItem saveCSVMenuItem;
	private javax.swing.JMenuItem saveConsoleMenuItem;
	private javax.swing.JButton saveXLSButton;
	private javax.swing.JMenuItem saveXLSMenuItem;
	private javax.swing.JPanel settingsPane;
	private javax.swing.JButton simCancelButton;
	private javax.swing.JButton simulateButton;
	private javax.swing.JMenuItem simulateMenuItem;
	private javax.swing.JMenu simulationMenu;
	public javax.swing.JProgressBar simulationProgressBar;
	private javax.swing.JToolBar simulatorToolbar;
	private javax.swing.JLabel slidingWindowLabel;
	private javax.swing.JSpinner slidingWindwowSpinner;
	private javax.swing.JLabel statLimitLabel;
	private javax.swing.JSpinner statLimitSpinner;
	private javax.swing.JPanel statusPanel;
	private javax.swing.JLabel stepPrefLabel;
	private javax.swing.JSpinner stepPrefSpinner;
	private javax.swing.JLabel totalNetLabel;
	private javax.swing.JTextField totalNetTextField;
	private javax.swing.JLabel totalReqLabel;
	private javax.swing.JTextField totalReqTextField;
	private javax.swing.JLabel unitsLabel1;
	private javax.swing.JLabel unitsLabel2;
	private javax.swing.JLabel userLabel;
	@SuppressWarnings("rawtypes")
	private javax.swing.JList userList;
	private javax.swing.JLabel zipfLambdaLabel;
	private javax.swing.JSpinner zipfLamdbaSpinner;
	
	//promenne pro kontrolky na zalozku s vysledky consistency control
	private javax.swing.JTable consistencyTable;
	private javax.swing.JScrollPane tablePane;
    private javax.swing.JPanel ConReultsPanel;
    @SuppressWarnings("rawtypes")
	private javax.swing.JList userListConsistency;
    private javax.swing.JLabel userListLabel;
    private javax.swing.JLabel cachePolLabel;
    @SuppressWarnings("rawtypes")
	private javax.swing.JList cachePolList;
    private javax.swing.JScrollPane cachePolScroll;
    private javax.swing.JScrollPane userListConScroll;
    
    //promenne pro kontrolky pro ukladani vysledku mereni konzistentnosti
    private javax.swing.JButton saveCSVConsistencyButton;
	private javax.swing.JMenuItem saveCSVConsistencyMenuItem;
	private javax.swing.JMenuItem saveConsoleConsistencyMenuItem;
	private javax.swing.JButton saveXLSConsistencyButton;
	private javax.swing.JMenuItem saveXLSConsistencyMenuItem;
	private javax.swing.JButton printConsoleConsistencyButton;
	private javax.swing.JToolBar.Separator toolbarSeparator3;
	private javax.swing.JPopupMenu.Separator fileMenuSeparator2;
	
	/**
	 * metoda pro vytvoreni a vykresleni vsech komponent vcetne registrace udalosti a reakci na ne
	 * metoda je automaticky vytvorena -> hodne dlouha
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initComponents() {

		ConsistencyControlPane = new javax.swing.JSplitPane();
		simulatorToolbar = new javax.swing.JToolBar();
		simulateButton = new javax.swing.JButton();
		simCancelButton = new javax.swing.JButton();
		toolbarSeparator1 = new javax.swing.JToolBar.Separator();
		saveCSVButton = new javax.swing.JButton();
		saveXLSButton = new javax.swing.JButton();
		printConsoleButton = new javax.swing.JButton();
		toolbarSeparator2 = new javax.swing.JToolBar.Separator();
		exitButton = new javax.swing.JButton();
		panelsPane = new javax.swing.JTabbedPane();
		settingsPane = new javax.swing.JPanel();
		cacheCapacityPanel = new javax.swing.JPanel();
		cacheCapScrollPane = new javax.swing.JScrollPane();
		cacheCapacityList = new javax.swing.JList();
		cacheCapSpinner = new javax.swing.JSpinner();
		cachePlusButton = new javax.swing.JButton();
		cacheMinusButton = new javax.swing.JButton();
		othersSettingsPanel = new javax.swing.JPanel();
		statLimitLabel = new javax.swing.JLabel();
		statLimitSpinner = new javax.swing.JSpinner();
		networkSpeedLabel = new javax.swing.JLabel();
		networkSpeesLabel2 = new javax.swing.JLabel();
		networkSpeedSpinner = new javax.swing.JSpinner();
		slidingWindowLabel = new javax.swing.JLabel();
		loadServStat = new javax.swing.JCheckBox();
		simConsistencyCheck = new javax.swing.JCheckBox();
		slidingWindwowSpinner = new javax.swing.JSpinner();
		requestsSettingsPanel = new javax.swing.JPanel();
		requestsInputComboBox = new javax.swing.JComboBox();
		inputRequestLabel = new javax.swing.JLabel();
		randomFileSizeCheckBox = new javax.swing.JCheckBox();
		minFileSizeLabel = new javax.swing.JLabel();
		maxFileSizeLabel = new javax.swing.JLabel();
		unitsLabel2 = new javax.swing.JLabel();
		unitsLabel1 = new javax.swing.JLabel();
		minGenFileSizejSpinner = new javax.swing.JSpinner();
		maxGenFileSizejSpinner = new javax.swing.JSpinner();
		pathTextField = new javax.swing.JTextField();
		pathLabel = new javax.swing.JLabel();
		generateFileSpinner = new javax.swing.JSpinner();
		generateFileLabel = new javax.swing.JLabel();
		meanValueGaussLabel = new javax.swing.JLabel();
		maenValueGaussSpinner = new javax.swing.JSpinner();
		preferenceDivisibleLabel = new javax.swing.JLabel();
		preferenceDivisibleSpinner = new javax.swing.JSpinner();
		stepPrefLabel = new javax.swing.JLabel();
		stepPrefSpinner = new javax.swing.JSpinner();
		nonPrefLabel = new javax.swing.JLabel();
		nonPrefSpinner = new javax.swing.JSpinner();
		dispersionLabel = new javax.swing.JLabel();
		dispersionSpinner = new javax.swing.JSpinner();
		requestsCountLabel = new javax.swing.JLabel();
		requestCountSpinner = new javax.swing.JSpinner();
		zipfLambdaLabel = new javax.swing.JLabel();
		SpinnerNumberModel zipfModel = new SpinnerNumberModel(0.75, 0.01, 10,
				0.01);
		zipfLamdbaSpinner = new javax.swing.JSpinner();
		cachePane = new javax.swing.JSplitPane();
		cachePanel = new javax.swing.JPanel();
		consistencyPanel = new javax.swing.JPanel();
		noSettingsPanelCache = new javax.swing.JPanel();
		noSettingsLabelCache = new javax.swing.JLabel();
		noSettingsPanelConsistency = new javax.swing.JPanel();
		noSettingsLabelConsistency = new javax.swing.JLabel();
		resultsPane = new javax.swing.JPanel();
		userListScrollPane = new javax.swing.JScrollPane();
		userList = new javax.swing.JList();
		userLabel = new javax.swing.JLabel();
		resultsChangeCombo = new javax.swing.JComboBox();
		resultsTableScrollPane = new javax.swing.JScrollPane();
		resultsTable = new javax.swing.JTable();
		totalReqLabel = new javax.swing.JLabel();
		totalReqTextField = new javax.swing.JTextField();
		totalNetLabel = new javax.swing.JLabel();
		totalNetTextField = new javax.swing.JTextField();
		unitsLabel3 = new javax.swing.JLabel();
		chooseResultsLabel = new javax.swing.JLabel();
		barChartButton = new javax.swing.JButton();
		lineChartButton = new javax.swing.JButton();
		statusPanel = new javax.swing.JPanel();
		simulationProgressBar = new javax.swing.JProgressBar();
		menuBar = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		saveCSVMenuItem = new javax.swing.JMenuItem();
		saveXLSMenuItem = new javax.swing.JMenuItem();
		saveConsoleMenuItem = new javax.swing.JMenuItem();
		fileMenuSeparator = new javax.swing.JPopupMenu.Separator();
		exitMenuItem = new javax.swing.JMenuItem();
		simulationMenu = new javax.swing.JMenu();
		requestMenu = new javax.swing.JMenu();
		inputAFSMenuItem = new javax.swing.JMenuItem();
		inputGaussianMenuItem = new javax.swing.JMenuItem();
		inputUniformlyMenuItem = new javax.swing.JMenuItem();
		inputRandomPrefMenuItem = new javax.swing.JMenuItem();
		inputZipfMenuItem = new javax.swing.JMenuItem();
		simulateMenuItem = new javax.swing.JMenuItem();
		helpMenu = new javax.swing.JMenu();
		aboutMenuItem = new javax.swing.JMenuItem();
		
        userListConScroll = new javax.swing.JScrollPane();
		consistencyTable = new javax.swing.JTable();
		tablePane = new javax.swing.JScrollPane();
	    ConReultsPanel = new javax.swing.JPanel();
	    userListConsistency = new javax.swing.JList();
	    userListLabel = new javax.swing.JLabel();
	    cachePolLabel = new javax.swing.JLabel();
	    cachePolList = new javax.swing.JList();
	    cachePolScroll = new javax.swing.JScrollPane();
	    
	    toolbarSeparator3 = new javax.swing.JToolBar.Separator();
	    saveCSVConsistencyButton = new javax.swing.JButton();
		printConsoleConsistencyButton = new  javax.swing.JButton();
		saveXLSConsistencyButton = new  javax.swing.JButton();
		saveCSVConsistencyMenuItem = new  javax.swing.JMenuItem();
		saveConsoleConsistencyMenuItem = new  javax.swing.JMenuItem();
		saveXLSConsistencyMenuItem = new  javax.swing.JMenuItem();
		fileMenuSeparator2 = new javax.swing.JPopupMenu.Separator();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Cache Simulator v2.0");
		setIconImage(new javax.swing.ImageIcon(getClass().getResource(
				"/cz/zcu/kiv/cacheSimulator/ico/simulation.png")).getImage());
		setLocationByPlatform(true);
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		simulatorToolbar.setFloatable(false);
		simulatorToolbar.setRollover(true);
		simulatorToolbar.setName("SimulatorToolar");
		simulatorToolbar.addSeparator();

		simulateButton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/cz/zcu/kiv/cacheSimulator/ico/run.png")));
		simulateButton.setToolTipText("Simulate!");
		simulateButton.setFocusable(false);
		simulateButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		simulateButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		simulateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				simulateActionPerformed(evt);
			}
		});
		simulatorToolbar.add(simulateButton);

		simCancelButton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/cz/zcu/kiv/cacheSimulator/ico/cancel.png")));
		simCancelButton.setToolTipText("Cancel Simulation!");
		simCancelButton.setEnabled(false);
		simCancelButton.setFocusable(false);
		simCancelButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		simCancelButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		simCancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				simCancelButtonActionPerformed(evt);
			}
		});
		simulatorToolbar.add(simCancelButton);
		simulatorToolbar.add(toolbarSeparator1);

		saveCSVButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/cz/zcu/kiv/cacheSimulator/ico/csv.png")));
		saveCSVButton.setToolTipText("Save Caching Policie Results to CSV");
		saveCSVButton.setEnabled(false);
		saveCSVButton.setFocusable(false);
		saveCSVButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		saveCSVButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		saveCSVButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveCSVActionPerformed(evt);
			}
		});
		simulatorToolbar.add(saveCSVButton);

		saveXLSButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/cz/zcu/kiv/cacheSimulator/ico/xls.png"))); // NOI18N
		saveXLSButton.setToolTipText("Save Caching Policies Results to XLS");
		saveXLSButton.setEnabled(false);
		saveXLSButton.setFocusable(false);
		saveXLSButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		saveXLSButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		saveXLSButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveXLSActionPerformed(evt);
			}
		});
		simulatorToolbar.add(saveXLSButton);

		printConsoleButton.setIcon(new javax.swing.ImageIcon(
				getClass().getResource(
						"/cz/zcu/kiv/cacheSimulator/ico/console.png")));
		printConsoleButton.setToolTipText("Print Caching Policie results to console");
		printConsoleButton.setEnabled(false);
		printConsoleButton.setFocusable(false);
		printConsoleButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		printConsoleButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		printConsoleButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						printConsoleActionPerformed(evt);
					}
				});
		simulatorToolbar.add(printConsoleButton);
		simulatorToolbar.add(toolbarSeparator2);
		
	    saveCSVConsistencyButton.setIcon(new javax.swing.ImageIcon(
				getClass().getResource(
						"/cz/zcu/kiv/cacheSimulator/ico/csv.png")));
	    saveCSVConsistencyButton.setToolTipText("Save Consistency Control results to CSV");
	    saveCSVConsistencyButton.setEnabled(false);
	    saveCSVConsistencyButton.setFocusable(false);
	    saveCSVConsistencyButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	    saveCSVConsistencyButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
	    saveCSVConsistencyButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						saveCSVConsistencyActionPerformed(evt);
					}
				});
	    simulatorToolbar.add(saveCSVConsistencyButton);
	    
	    saveXLSConsistencyButton.setIcon(new javax.swing.ImageIcon(
				getClass().getResource(
						"/cz/zcu/kiv/cacheSimulator/ico/xls.png"))); // NOI18N
	    saveXLSConsistencyButton.setToolTipText("Save Consistency Control results to XLS");
	    saveXLSConsistencyButton.setEnabled(false);
	    saveXLSConsistencyButton.setFocusable(false);
	    saveXLSConsistencyButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	    saveXLSConsistencyButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
	    saveXLSConsistencyButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						saveXLSConsistencyActionPerformed(evt);
					}
				});
	    simulatorToolbar.add(saveXLSConsistencyButton);
	    
	    printConsoleConsistencyButton.setIcon(new javax.swing.ImageIcon(
				getClass().getResource(
						"/cz/zcu/kiv/cacheSimulator/ico/console.png"))); // NOI18N
	    printConsoleConsistencyButton.setToolTipText("Print Consistency Control results to console");
	    printConsoleConsistencyButton.setEnabled(false);
	    printConsoleConsistencyButton.setFocusable(false);
	    printConsoleConsistencyButton
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	    printConsoleConsistencyButton
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
	    printConsoleConsistencyButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						printConsoleConsistencyActionPerformed(evt);
					}
				});
	    simulatorToolbar.add(printConsoleConsistencyButton);
		

		simulatorToolbar.add(toolbarSeparator3);

		exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/cz/zcu/kiv/cacheSimulator/ico/exit.png"))); 
		exitButton.setToolTipText("Exit");
		exitButton.setFocusable(false);
		exitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		exitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitActionPerformed(evt);
			}
		});
		simulatorToolbar.add(exitButton);

		panelsPane.setName("Simulation"); 

		settingsPane.setName("SettingsPanel"); 
		settingsPane.setLayout(null);

		cacheCapacityPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Cache capacities [MB]"));

		cacheCapacityList.setFont(new java.awt.Font("Tahoma", 0, 12));
		cacheCapacityList.setModel(new myCacheCapacityListModel());
		cacheCapacityList
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		cacheCapScrollPane.setViewportView(cacheCapacityList);

		cacheCapSpinner.setValue(8);

		cachePlusButton.setText("+");
		cachePlusButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cachePlusButtonActionPerformed(evt);
			}
		});

		cacheMinusButton.setText("-");
		cacheMinusButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cacheMinusButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout cacheCapacityPanelLayout = new javax.swing.GroupLayout(
				cacheCapacityPanel);
		cacheCapacityPanel.setLayout(cacheCapacityPanelLayout);
		cacheCapacityPanelLayout.setHorizontalGroup(cacheCapacityPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						cacheCapacityPanelLayout.createSequentialGroup()
								.addContainerGap().addComponent(cacheCapScrollPane)
								.addContainerGap())
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						cacheCapacityPanelLayout
								.createSequentialGroup()
								.addContainerGap(70, Short.MAX_VALUE)
								.addComponent(cacheCapSpinner,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										121,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(cachePlusButton)
								.addGap(18, 18, 18)
								.addComponent(cacheMinusButton,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										38,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(52, 52, 52)));
		cacheCapacityPanelLayout
				.setVerticalGroup(cacheCapacityPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								cacheCapacityPanelLayout
										.createSequentialGroup()
										.addComponent(
												cacheCapScrollPane,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												196, Short.MAX_VALUE)
										.addGap(18, 18, 18)
										.addGroup(
												cacheCapacityPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																cacheCapSpinner,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																cachePlusButton)
														.addComponent(
																cacheMinusButton))
										.addContainerGap()));

		settingsPane.add(cacheCapacityPanel);
		cacheCapacityPanel.setBounds(369, 11, 372, 270);

		othersSettingsPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Others"));
		othersSettingsPanel.setLayout(null);

		statLimitLabel.setText("Minimum request count for including result:");
		othersSettingsPanel.add(statLimitLabel);
		statLimitLabel.setBounds(25, 36, 250, 18);

		statLimitSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
		statLimitSpinner.setValue(30);
		statLimitSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						statLimitSpinnerStateChanged(evt);
					}
				});
		othersSettingsPanel.add(statLimitSpinner);
		statLimitSpinner.setBounds(280, 36, 60, 18);

		networkSpeedLabel.setText("Average network speed:");
		othersSettingsPanel.add(networkSpeedLabel);
		networkSpeedLabel.setBounds(60, 72, 149, 18);

		networkSpeesLabel2.setText("Mbit/s");
		othersSettingsPanel.add(networkSpeesLabel2);
		networkSpeesLabel2.setBounds(310, 72, 40, 18);

		networkSpeedSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
		networkSpeedSpinner.setValue(80);
		networkSpeedSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						networkSpeedSpinnerStateChanged(evt);
					}
				});
		othersSettingsPanel.add(networkSpeedSpinner);
		networkSpeedSpinner.setBounds(210, 72, 90, 18);

		slidingWindowLabel
				.setText("Sliding window capacity (%from cache capacity):");
		othersSettingsPanel.add(slidingWindowLabel);
		slidingWindowLabel.setBounds(16, 108, 280, 18);

		slidingWindwowSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
		slidingWindwowSpinner.setValue(25);
		slidingWindwowSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						slidingWindwowSpinnerStateChanged(evt);
					}
				});

		othersSettingsPanel.add(slidingWindwowSpinner);
		slidingWindwowSpinner.setBounds(296, 108, 60, 18);

		loadServStat.setSelected(GlobalVariables.isLoadServerStatistic());
		loadServStat.setText("Pre-load server statistics");
		loadServStat.setFont(new java.awt.Font("Tahoma", 0, 11));
		loadServStat.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadServStatActionPerformed(evt);
			}
		});
		othersSettingsPanel.add(loadServStat);
		loadServStat.setBounds(120, 144, 160, 18);

		settingsPane.add(othersSettingsPanel);
		othersSettingsPanel.setBounds(370, 290, 370, 180);

		requestsSettingsPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Requests"));
		requestsSettingsPanel.setLayout(null);

		requestsInputComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "-- Choose one --", "From AFS log file",
						"Gaussian random", "Uniformly Random", "Random with preference",
						"Zipf random" }));
		requestsInputComboBox
				.addItemListener(new java.awt.event.ItemListener() {
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						requestsInputComboBoxItemStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(requestsInputComboBox);
		requestsInputComboBox.setBounds(180, 30, 156, 22);

		inputRequestLabel.setText("Requests input method:");
		requestsSettingsPanel.add(inputRequestLabel);
		inputRequestLabel.setBounds(30, 30, 173, 20);

		randomFileSizeCheckBox.setText("Generate random file sizes");
		randomFileSizeCheckBox
				.addItemListener(new java.awt.event.ItemListener() {
					public void itemStateChanged(java.awt.event.ItemEvent evt) {
						randomFileSizeCheckBoxItemStateChanged(evt);
					}
				});
		randomFileSizeCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						randomFileSizeCheckBoxActionPerformed(evt);
					}
				});
		requestsSettingsPanel.add(randomFileSizeCheckBox);
		randomFileSizeCheckBox.setBounds(80, 350, 190, 23);

		simConsistencyCheck.setText("Simulate Consistency");
		simConsistencyCheck.setSelected(false);
		simConsistencyCheck.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				simConsistencyCheckedChanged(evt);
			}
		});
		requestsSettingsPanel.add(simConsistencyCheck);
		simConsistencyCheck.setBounds(110, 140, 180, 14);

		minFileSizeLabel.setText("Minimum generated file size: ");
		requestsSettingsPanel.add(minFileSizeLabel);
		minFileSizeLabel.setBounds(10, 380, 180, 14);

		maxFileSizeLabel.setText("Maximum generated file size:");
		requestsSettingsPanel.add(maxFileSizeLabel);
		maxFileSizeLabel.setBounds(10, 410, 180, 20);

		unitsLabel2.setText("KBytes");
		requestsSettingsPanel.add(unitsLabel2);
		unitsLabel2.setBounds(282, 413, 71, 14);

		unitsLabel1.setText("KBytes");
		requestsSettingsPanel.add(unitsLabel1);
		unitsLabel1.setBounds(282, 383, 71, 14);

		minGenFileSizejSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		minGenFileSizejSpinner.setValue(500);
		minGenFileSizejSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						minGenFileSizejSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(minGenFileSizejSpinner);
		minGenFileSizejSpinner.setBounds(190, 381, 88, 18);

		maxGenFileSizejSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		maxGenFileSizejSpinner.setValue(32000);
		maxGenFileSizejSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						maxGenFileSizejSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(maxGenFileSizejSpinner);
		maxGenFileSizejSpinner.setBounds(190, 411, 88, 18);

		pathTextField.setEditable(false);
		pathTextField.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				pathTextFieldMouseClicked(evt);
			}
		});
		requestsSettingsPanel.add(pathTextField);
		pathTextField.setBounds(60, 80, 270, 30);

		pathLabel.setText("Path:");
		requestsSettingsPanel.add(pathLabel);
		pathLabel.setBounds(16, 88, 50, 14);

		generateFileSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		generateFileSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
		generateFileSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						generateFileSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(generateFileSpinner);
		generateFileSpinner.setBounds(220, 120, 80, 20);

		generateFileLabel.setText("Generate files:");
		requestsSettingsPanel.add(generateFileLabel);
		generateFileLabel.setBounds(40, 130, 170, 10);

		meanValueGaussLabel.setText("Mean value (Gauss):");
		requestsSettingsPanel.add(meanValueGaussLabel);
		meanValueGaussLabel.setBounds(40, 160, 160, 10);

		maenValueGaussSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		maenValueGaussSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
		maenValueGaussSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						maenValueGaussSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(maenValueGaussSpinner);
		maenValueGaussSpinner.setBounds(220, 150, 80, 20);

		preferenceDivisibleLabel.setText("Prefenced file's ID divisible by:");
		requestsSettingsPanel.add(preferenceDivisibleLabel);
		preferenceDivisibleLabel.setBounds(20, 190, 180, 20);

		preferenceDivisibleSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		preferenceDivisibleSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						preferenceDivisibleSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(preferenceDivisibleSpinner);
		preferenceDivisibleSpinner.setBounds(220, 190, 80, 20);

		stepPrefLabel.setText("Step for generate preferenced file:");
		requestsSettingsPanel.add(stepPrefLabel);
		stepPrefLabel.setBounds(20, 240, 190, 10);

		stepPrefSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		stepPrefSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						stepPrefSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(stepPrefSpinner);
		stepPrefSpinner.setBounds(220, 240, 80, 20);

		nonPrefLabel.setText("Non preferenced file's ID divisible by:");
		requestsSettingsPanel.add(nonPrefLabel);
		nonPrefLabel.setBounds(20, 280, 190, 10);

		nonPrefSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		nonPrefSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						nonPrefSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(nonPrefSpinner);
		nonPrefSpinner.setBounds(219, 280, 80, 20);

		dispersionLabel.setText("Dispersion:");
		requestsSettingsPanel.add(dispersionLabel);
		dispersionLabel.setBounds(40, 190, 170, 14);

		dispersionSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		dispersionSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						dispersionSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(dispersionSpinner);
		dispersionSpinner.setBounds(220, 190, 80, 18);

		requestsCountLabel.setText("Requests count:");
		requestsSettingsPanel.add(requestsCountLabel);
		requestsCountLabel.setBounds(40, 100, 170, 14);

		requestCountSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		requestCountSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
		requestCountSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						requestCountSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(requestCountSpinner);
		requestCountSpinner.setBounds(220, 90, 80, 20);

		zipfLambdaLabel.setText("Zipf generator alfa:");
		requestsSettingsPanel.add(zipfLambdaLabel);
		zipfLambdaLabel.setBounds(40, 190, 150, 14);

		zipfLamdbaSpinner.setModel(zipfModel);
		zipfLamdbaSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		zipfLamdbaSpinner
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						zipfLamdbaSpinnerStateChanged(evt);
					}
				});
		requestsSettingsPanel.add(zipfLamdbaSpinner);
		zipfLamdbaSpinner.setBounds(220, 190, 80, 18);

		settingsPane.add(requestsSettingsPanel);
		requestsSettingsPanel.setBounds(10, 10, 353, 456);

		panelsPane.addTab("Simulation Settings", settingsPane);

		cachePane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cachePane.setName("CacheAlgPanel"); // NOI18N

		cachePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 6,
				10, 6));
		cachePanel.setLayout(new javax.swing.BoxLayout(cachePanel,
				javax.swing.BoxLayout.Y_AXIS));
		cachePane.setLeftComponent(cachePanel);

		consistencyPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				10, 6, 10, 6));
		consistencyPanel.setLayout(new javax.swing.BoxLayout(consistencyPanel,
				javax.swing.BoxLayout.Y_AXIS));
		ConsistencyControlPane.setLeftComponent(consistencyPanel);

		noSettingsPanelCache.setLayout(null);

		noSettingsLabelCache
				.setText("This cache policy does not have any settings!");
		noSettingsPanelCache.add(noSettingsLabelCache);
		noSettingsLabelCache.setBounds(110, 60, 410, 14);

		cachePane.setRightComponent(noSettingsPanelCache);

		noSettingsPanelConsistency.setLayout(null);
		noSettingsLabelConsistency
				.setText("This consistency control does not have any settings!");
		noSettingsPanelConsistency.add(noSettingsLabelConsistency);
		noSettingsLabelConsistency.setBounds(110, 60, 410, 14);

		ConsistencyControlPane.setRightComponent(noSettingsPanelConsistency);

		panelsPane.addTab("Cache Policies", cachePane);

		panelsPane.addTab("Consistency Control", ConsistencyControlPane);
		panelsPane.setEnabledAt(2, false);

		resultsPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1,
				1, 1));
		resultsPane.setName("ResultsPanel"); // NOI18N
		resultsPane.setLayout(null);

		userList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				userListValueChanged(evt);
			}
		});
		userListScrollPane.setViewportView(userList);

		resultsPane.add(userListScrollPane);
		userListScrollPane.setBounds(11, 51, 136, 256);

		userLabel.setText("Simulated user");
		resultsPane.add(userLabel);
		userLabel.setBounds(37, 30, 110, 14);

		resultsChangeCombo.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		resultsChangeCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Read Hit Ratio [%]", "Read Hit Count",
						"Saved Bytes Ratio [%]", "Saved Bytes [MB]",
						"Data Transfer Degrease Ratio [%]",
						"Data Transfer Degrease [MB]" }));
		resultsChangeCombo.setMaximumSize(new java.awt.Dimension(192, 22));
		resultsChangeCombo
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						resultsChangeComboActionPerformed(evt);
					}
				});
		resultsPane.add(resultsChangeCombo);
		resultsChangeCombo.setBounds(11, 338, 140, 21);

		resultsTable
				.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
		resultsTable.setEnabled(false);
		resultsTable.setRowSelectionAllowed(false);
		resultsTableScrollPane.setViewportView(resultsTable);

		resultsPane.add(resultsTableScrollPane);
		resultsTableScrollPane.setBounds(165, 51, 573, 411);

		totalReqLabel.setText("Total requested files:");
		resultsPane.add(totalReqLabel);
		totalReqLabel.setBounds(140, 16, 116, 14);

		totalReqTextField.setEditable(false);
		totalReqTextField.setFont(new java.awt.Font("Tahoma", 0, 10));

		resultsPane.add(totalReqTextField);
		totalReqTextField.setBounds(260, 13, 91, 19);

		totalNetLabel.setText("Total network traffic (without cache):");
		resultsPane.add(totalNetLabel);
		totalNetLabel.setBounds(389, 16, 200, 14);

		totalNetTextField.setEditable(false);
		totalNetTextField.setFont(new java.awt.Font("Tahoma", 0, 10));

		resultsPane.add(totalNetTextField);
		totalNetTextField.setBounds(597, 13, 80, 19);

		unitsLabel3.setText("MB");
		resultsPane.add(unitsLabel3);
		unitsLabel3.setBounds(690, 16, 40, 14);

		chooseResultsLabel.setText("Choose results");
		resultsPane.add(chooseResultsLabel);
		chooseResultsLabel.setBounds(40, 320, 120, 14);

		barChartButton.setText("Bar Chart");
		barChartButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				barChartButtonActionPerformed(evt);
			}
		});
		resultsPane.add(barChartButton);
		barChartButton.setBounds(10, 390, 136, 23);

		lineChartButton.setText("Line Chart");
		lineChartButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				lineChartButtonActionPerformed(evt);
			}
		});
		resultsPane.add(lineChartButton);
		lineChartButton.setBounds(10, 430, 136, 23);

		panelsPane.addTab("Policies Results", resultsPane);
		
        ConReultsPanel.setName("ResultsPanel"); 
        ConReultsPanel.setLayout(null);

		tablePane.setViewportView(consistencyTable);

		ConReultsPanel.add(tablePane);
		tablePane.setBounds(180, 11, 560, 460);

		userListConsistency.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        userListConScroll.setViewportView(userListConsistency);
        
		userListConsistency
		.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(
					javax.swing.event.ListSelectionEvent evt) {
				userListConsistencyValueChanged(evt);
			}
		});
		
		ConReultsPanel.add(userListConScroll);
	    userListConScroll.setBounds(19, 43, 140, 192);

		userListLabel.setText("User List");
		ConReultsPanel.add(userListLabel);
		userListLabel.setBounds(63, 21, 65, 14);

		cachePolLabel.setText("Caching Policy");
		ConReultsPanel.add(cachePolLabel);
		cachePolLabel.setBounds(50, 261, 86, 14);

	
		cachePolList
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		cachePolList
				.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						cachePolListValueChanged(evt);
					}
				});
		cachePolScroll.setViewportView(cachePolList);

		ConReultsPanel.add(cachePolScroll);
		cachePolScroll.setBounds(19, 285, 140, 186);

		panelsPane.addTab("Consistency Results", ConReultsPanel);

		statusPanel.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.LOWERED));

		javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(
				statusPanel);
		statusPanel.setLayout(statusPanelLayout);
		statusPanelLayout.setHorizontalGroup(statusPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(simulationProgressBar,
						javax.swing.GroupLayout.DEFAULT_SIZE, 748,
						Short.MAX_VALUE));
		statusPanelLayout
				.setVerticalGroup(statusPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								statusPanelLayout
										.createSequentialGroup()
										.addGap(0, 0, Short.MAX_VALUE)
										.addComponent(
												simulationProgressBar,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		fileMenu.setMnemonic('F');
		fileMenu.setText("File");

		saveCSVMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_C, 0));
		saveCSVMenuItem.setMnemonic('C');
		saveCSVMenuItem.setText("Save Policies Results to CSV");
		saveCSVMenuItem.setToolTipText("Saves tables with caching policies results to CSV file");
		saveCSVMenuItem.setEnabled(false);
		saveCSVMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveCSVActionPerformed(evt);
			}
		});
		fileMenu.add(saveCSVMenuItem);

		saveXLSMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_L, 0));
		saveXLSMenuItem.setMnemonic('L');
		saveXLSMenuItem.setText("Save Policies Results to XLS");
		saveXLSMenuItem.setToolTipText("Saves tables with caching policies results to XLS file");
		saveXLSMenuItem.setEnabled(false);
		saveXLSMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveXLSActionPerformed(evt);
			}
		});
		fileMenu.add(saveXLSMenuItem);

		saveConsoleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_P, 0));
		saveConsoleMenuItem.setMnemonic('P');
		saveConsoleMenuItem.setText("Print Policies Results to Console");
		saveConsoleMenuItem.setEnabled(false);
		saveConsoleMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						printConsoleActionPerformed(evt);
					}
				});
		fileMenu.add(saveConsoleMenuItem);
		fileMenu.add(fileMenuSeparator);
		
		saveCSVConsistencyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_A, 0));
		saveCSVConsistencyMenuItem.setMnemonic('A');
		saveCSVConsistencyMenuItem.setText("Save Consistency Results to CSV");
		saveCSVConsistencyMenuItem.setToolTipText("Saves tables with consistency control results to CSV file");
		saveCSVConsistencyMenuItem.setEnabled(false);
		saveCSVConsistencyMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveCSVConsistencyActionPerformed(evt);
			}
		});
		fileMenu.add(saveCSVConsistencyMenuItem);
		

		saveXLSConsistencyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_S, 0));
		saveXLSConsistencyMenuItem.setMnemonic('S');
		saveXLSConsistencyMenuItem.setText("Save Consistency Results to XLS");
		saveXLSConsistencyMenuItem.setToolTipText("Saves tables with consistency control results to XLS file");
		saveXLSConsistencyMenuItem.setEnabled(false);
		saveXLSConsistencyMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveXLSConsistencyActionPerformed(evt);
			}
		});
		fileMenu.add(saveXLSConsistencyMenuItem);
		
		saveConsoleConsistencyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_D, 0));
		saveConsoleConsistencyMenuItem.setMnemonic('D');
		saveConsoleConsistencyMenuItem.setText("Print Consistency Results to Console");
		saveConsoleConsistencyMenuItem.setToolTipText("Print tables with consistency control results to console");
		saveConsoleConsistencyMenuItem.setEnabled(false);
		saveConsoleConsistencyMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				printConsoleConsistencyActionPerformed(evt);
			}
		});
		fileMenu.add(saveConsoleConsistencyMenuItem);
		
		fileMenu.add(fileMenuSeparator2);

		exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_X, 0));
		exitMenuItem.setMnemonic('X');
		exitMenuItem.setText("Exit");
		exitMenuItem.setToolTipText("Terminate simulator");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitActionPerformed(evt);
			}
		});
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		simulationMenu.setMnemonic('S');
		simulationMenu.setText("Simulation");

		requestMenu.setMnemonic('R');
		requestMenu.setText("Request input method");
		requestMenu.setToolTipText("Change requests input method");

		inputAFSMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_F, 0));
		inputAFSMenuItem.setMnemonic('F');
		inputAFSMenuItem.setText("From AFS log file");
		inputAFSMenuItem
				.setToolTipText("Requests to the files are from AFS log file");
		inputAFSMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				inputAFSMenuItemActionPerformed(evt);
			}
		});
		requestMenu.add(inputAFSMenuItem);

		inputGaussianMenuItem.setAccelerator(javax.swing.KeyStroke
				.getKeyStroke(java.awt.event.KeyEvent.VK_G, 0));
		inputGaussianMenuItem.setMnemonic('G');
		inputGaussianMenuItem.setText("Gaussian random");
		inputGaussianMenuItem
				.setToolTipText("Requests to the files are from Gaussian distribution");
		inputGaussianMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						inputGaussianMenuItemActionPerformed(evt);
					}
				});
		requestMenu.add(inputGaussianMenuItem);

		inputUniformlyMenuItem.setAccelerator(javax.swing.KeyStroke
				.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
		inputUniformlyMenuItem.setMnemonic('R');
		inputUniformlyMenuItem.setText("Uniformly random");
		inputUniformlyMenuItem
				.setToolTipText("Requests to the files are from uniformly random distribution");
		inputUniformlyMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						inputUniformlyMenuItemActionPerformed(evt);
					}
				});
		requestMenu.add(inputUniformlyMenuItem);

		inputRandomPrefMenuItem.setAccelerator(javax.swing.KeyStroke
				.getKeyStroke(java.awt.event.KeyEvent.VK_P, 0));
		inputRandomPrefMenuItem.setMnemonic('P');
		inputRandomPrefMenuItem.setText("Random with preferences");
		inputRandomPrefMenuItem
				.setToolTipText("Requests to the files are from random distribution with preferention of files");
		inputRandomPrefMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						inputRandomPrefMenuItemActionPerformed(evt);
					}
				});
		requestMenu.add(inputRandomPrefMenuItem);

		inputZipfMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_Z, 0));
		inputZipfMenuItem.setMnemonic('Z');
		inputZipfMenuItem.setText("Zipf random");
		inputZipfMenuItem
				.setToolTipText("Requests to the files are from Zipf distribution");
		inputZipfMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						inputZipfMenuItemActionPerformed(evt);
					}
				});
		requestMenu.add(inputZipfMenuItem);

		simulationMenu.add(requestMenu);

		simulateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_I, 0));
		simulateMenuItem.setMnemonic('I');
		simulateMenuItem.setText("Simulate");
		simulateMenuItem.setToolTipText("Run simulation");
		simulateMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				simulateActionPerformed(evt);
			}
		});
		simulationMenu.add(simulateMenuItem);

		menuBar.add(simulationMenu);

		helpMenu.setMnemonic('H');
		helpMenu.setText("Help");

		aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_A, 0));
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.setText("About");
		aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				aboutMenuItemActionPerformed(evt);
			}
		});
		helpMenu.add(aboutMenuItem);

		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(simulatorToolbar,
						javax.swing.GroupLayout.DEFAULT_SIZE, 754,
						Short.MAX_VALUE)
				.addComponent(panelsPane)
				.addGroup(
						layout.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(
										statusPanel,
										javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(simulatorToolbar,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										40,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(panelsPane,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										499,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(27, Short.MAX_VALUE))
				.addGroup(
						layout.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(
										javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup()
												.addGap(0, 550, Short.MAX_VALUE)
												.addComponent(
														statusPanel,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))));

		
		panelsPane.setEnabledAt(3, false);
		panelsPane.setEnabledAt(4, false);
		pack();
	}
}
