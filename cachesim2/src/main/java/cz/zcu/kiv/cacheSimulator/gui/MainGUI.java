package cz.zcu.kiv.cacheSimulator.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.ui.RefineryUtilities;

import cz.zcu.kiv.cacheSimulator.ClassLoader;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.gui.chart.BarChart;
import cz.zcu.kiv.cacheSimulator.gui.chart.LineChart;
import cz.zcu.kiv.cacheSimulator.gui.configuration.MMWPBatchConsistencyPanel;
import cz.zcu.kiv.cacheSimulator.gui.configuration.MMWPConsistencyPanel;
import cz.zcu.kiv.cacheSimulator.gui.model.PolicyResultTable;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.AccessSimulation;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

/**
 * @author Pavel Bzoch trida pro zobrazeni hlavniho gui aplikace
 */
@SuppressWarnings("serial")
public class MainGUI extends JFrame implements Observer {

  /**
   * staticky ukazatel na hlavni okno programu - singleton instance
   */
  private static MainGUI gui = null;


  @Override
  public void update(final Observable arg0, final Object arg1) {
    if (arg1 instanceof Integer) {
      this.simulationProgressBar.setValue((Integer) arg1);
      this.simulationProgressBar.setString("Simulation in progress... " + arg1 + "%");
    } else {
      this.simulationProgressBar.setValue(this.simulationProgressBar.getValue() + 1);
      this.simulationProgressBar.setString(arg1.toString());
    }
  }

  /**
   * promenna pro uchovani buttonu pro nastaveni cache
   */
  private ArrayList<JCheckBox> cacheCheckBoxes = null;

  /**
   * promenna pro uchovani buttonu pro nastaveni consistency control
   */

  private ArrayList<JCheckBox> consistencyCheckBoxes = null;

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
    this.simConsistencyCheck.setVisible(true);
    this.randomFileSizeCheckBox.setEnabled(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.meanValueGaussLabel.setVisible(false);
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefSpinner.setVisible(false);
    this.pathLabel.setVisible(true);
    this.pathTextField.setVisible(true);
    this.pathTextField.setText(GlobalVariables.getLogAFSFIleName());
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(true);
    this.randomFileSizeCheckBox.setSelected(GlobalVariables.isRandomFileSizesForLoggedData());
    this.stepPrefLabel.setVisible(false);
    this.stepPrefSpinner.setVisible(false);
    this.dispersionLabel.setVisible(false);
    this.dispersionSpinner.setVisible(false);
    this.generateFileLabel.setVisible(false);
    this.generateFileSpinner.setVisible(false);
    this.unitsLabel1.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.unitsLabel2.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.requestCountSpinner.setVisible(false);
    this.requestsCountLabel.setVisible(false);
    this.requestCountSpinner.setVisible(false);
    this.requestsCountLabel.setVisible(false);
    this.zipfLambdaLabel.setVisible(false);
    this.zipfLamdbaSpinner.setVisible(false);
  }


  /**
   * metoda pro zobrazeni nastaveni random vstupu
   */
  private void showRandomInput() {
    this.simConsistencyCheck.setVisible(false);
    this.simConsistencyCheck.setSelected(false);
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefSpinner.setVisible(false);
    this.pathLabel.setVisible(false);
    this.pathTextField.setVisible(false);
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(true);
    this.stepPrefLabel.setVisible(false);
    this.stepPrefSpinner.setVisible(false);
    this.dispersionLabel.setVisible(false);
    this.dispersionSpinner.setVisible(false);
    this.generateFileLabel.setVisible(true);
    this.generateFileSpinner.setVisible(true);
    this.unitsLabel1.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.unitsLabel2.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.requestCountSpinner.setVisible(true);
    this.requestsCountLabel.setVisible(true);
    this.zipfLambdaLabel.setVisible(false);
    this.zipfLamdbaSpinner.setVisible(false);
  }


  /**
   * metoda pro zobrazeni ovladacich prvku pro gaussovske nastaveni
   */
  private void showGaussInput() {
    this.simConsistencyCheck.setVisible(false);
    this.simConsistencyCheck.setSelected(false);
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(true);
    this.meanValueGaussLabel.setVisible(true);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefSpinner.setVisible(false);
    this.pathLabel.setVisible(false);
    this.pathTextField.setVisible(false);
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(true);
    this.stepPrefLabel.setVisible(false);
    this.stepPrefSpinner.setVisible(false);
    this.dispersionLabel.setVisible(true);
    this.dispersionSpinner.setVisible(true);
    this.generateFileLabel.setVisible(true);
    this.generateFileSpinner.setVisible(true);
    this.unitsLabel1.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.unitsLabel2.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.requestCountSpinner.setVisible(true);
    this.zipfLambdaLabel.setVisible(false);
    this.zipfLamdbaSpinner.setVisible(false);
    this.requestsCountLabel.setVisible(true);
  }


  /**
   * metoda pro zobrazeni tlacitek pro vstup pro nahodny vstup s preferencemi
   */
  private void showPrefRandomInput() {
    this.simConsistencyCheck.setVisible(false);
    this.simConsistencyCheck.setSelected(false);
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(true);
    this.nonPrefSpinner.setVisible(true);
    this.pathLabel.setVisible(false);
    this.pathTextField.setVisible(false);
    this.preferenceDivisibleLabel.setVisible(true);
    this.preferenceDivisibleSpinner.setVisible(true);
    this.randomFileSizeCheckBox.setVisible(true);
    this.stepPrefLabel.setVisible(true);
    this.stepPrefSpinner.setVisible(true);
    this.dispersionLabel.setVisible(false);
    this.dispersionSpinner.setVisible(false);
    this.generateFileLabel.setVisible(true);
    this.generateFileSpinner.setVisible(true);
    this.unitsLabel1.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.unitsLabel2.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.requestCountSpinner.setVisible(true);
    this.requestsCountLabel.setVisible(true);
    this.zipfLambdaLabel.setVisible(false);
    this.zipfLamdbaSpinner.setVisible(false);
  }


  /**
   * metoda pro schovani vsech ovladacich prvku pro vstupni pozadavky
   */
  private void hideAllInput() {
    this.simConsistencyCheck.setVisible(false);
    this.simConsistencyCheck.setSelected(false);
    this.maenValueGaussSpinner.setVisible(false);
    this.maxFileSizeLabel.setVisible(false);
    this.maxGenFileSizejSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.minFileSizeLabel.setVisible(false);
    this.minGenFileSizejSpinner.setVisible(false);
    this.nonPrefLabel.setVisible(false);
    this.nonPrefSpinner.setVisible(false);
    this.pathLabel.setVisible(false);
    this.pathTextField.setVisible(false);
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(false);
    this.stepPrefLabel.setVisible(false);
    this.stepPrefSpinner.setVisible(false);
    this.dispersionLabel.setVisible(false);
    this.dispersionSpinner.setVisible(false);
    this.generateFileLabel.setVisible(false);
    this.generateFileSpinner.setVisible(false);
    this.unitsLabel1.setVisible(false);
    this.unitsLabel2.setVisible(false);
    this.requestCountSpinner.setVisible(false);
    this.requestsCountLabel.setVisible(false);
    this.zipfLambdaLabel.setVisible(false);
    this.zipfLamdbaSpinner.setVisible(false);
  }


  /**
   * metoda pro zobrazeni nastaveni zipf generatoru vstupu
   */
  private void showZipfInput() {
    this.simConsistencyCheck.setVisible(false);
    this.simConsistencyCheck.setSelected(false);
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefSpinner.setVisible(false);
    this.pathLabel.setVisible(false);
    this.pathTextField.setVisible(false);
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(true);
    this.stepPrefLabel.setVisible(false);
    this.stepPrefSpinner.setVisible(false);
    this.dispersionLabel.setVisible(false);
    this.dispersionSpinner.setVisible(false);
    this.generateFileLabel.setVisible(true);
    this.generateFileSpinner.setVisible(true);
    this.unitsLabel1.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.unitsLabel2.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.requestCountSpinner.setVisible(true);
    this.requestsCountLabel.setVisible(true);
    this.zipfLambdaLabel.setVisible(true);
    this.zipfLamdbaSpinner.setVisible(true);
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
    for (final Component c : this.requestsSettingsPanel.getComponents()) {
      c.setEnabled(false);
    }
    this.cacheCapacityPanel.setEnabled(false);
    for (final Component c : this.cacheCapacityPanel.getComponents()) {
      c.setEnabled(false);
    }
    this.othersSettingsPanel.setEnabled(false);
    for (final Component c : this.othersSettingsPanel.getComponents()) {
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
   *          prepinac pro udani, zda byla simulace uspesna
   */
  public void enableComponentsAfterSimulaton(final boolean isSimSuccesfull) {
    for (final Component c : this.requestsSettingsPanel.getComponents()) {
      c.setEnabled(true);
    }
    for (final Component c : this.cacheCapacityPanel.getComponents()) {
      c.setEnabled(true);
    }
    for (final Component c : this.othersSettingsPanel.getComponents()) {
      c.setEnabled(true);
    }

    if (this.requestsInputComboBox.getSelectedIndex() != 1) {
      this.randomFileSizeCheckBox.setEnabled(false);
    } else {
      this.randomFileSizeCheckBox.setEnabled(true);
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
      this.panelsPane.setEnabledAt(3, true);
      this.panelsPane.setSelectedIndex(3);
      this.panelsPane.setEnabled(true);
      if (this.consSimulation != null) {
        this.panelsPane.setEnabledAt(4, true);
        this.printConsoleConsistencyButton.setEnabled(true);
        this.saveXLSConsistencyButton.setEnabled(true);
        this.saveCSVConsistencyButton.setEnabled(true);
        this.saveConsoleConsistencyMenuItem.setEnabled(true);
        this.saveXLSConsistencyMenuItem.setEnabled(true);
        this.saveConsoleMenuItem.setEnabled(true);
      }
    } else {
      this.panelsPane.setEnabledAt(3, false);
      this.panelsPane.setEnabledAt(4, false);
      this.panelsPane.setSelectedIndex(0);
      this.panelsPane.setEnabled(true);
    }
  }


  /**
   * metoda nahraje z global variables nastaveni
   */
  private void loadValuesFromGlobalVar() {
    this.slidingWindwowSpinner
        .setValue((int) (GlobalVariables.getCacheCapacityForDownloadWindow() * 100));
    this.networkSpeedSpinner.setValue(GlobalVariables.getAverageNetworkSpeed());
    this.statLimitSpinner.setValue(GlobalVariables.getLimitForStatistics());
    this.maxGenFileSizejSpinner.setValue(GlobalVariables.getMaxGeneratedFileSize());
    this.minGenFileSizejSpinner.setValue(GlobalVariables.getMinGeneratedFileSize());
    this.nonPrefSpinner.setValue(GlobalVariables.getFileRequestnNonPreferenceFile());
    this.stepPrefSpinner.setValue(GlobalVariables.getFileRequestPreferenceStep());
    this.preferenceDivisibleSpinner.setValue(GlobalVariables.getFileRequestPreferenceFile());
    this.dispersionSpinner.setValue(GlobalVariables.getFileRequestGeneratorDispersion());
    this.maenValueGaussSpinner.setValue(GlobalVariables.getFileRequestGeneratorMeanValue());
    this.generateFileSpinner.setValue(GlobalVariables.getFileRequestGeneratorMaxValue());
    this.requestCountSpinner.setValue(GlobalVariables.getRequestCountForRandomGenerator());
  }


  /**
   * metoda pro nacteni consistency control algoritmu
   */
  private void loadConsistencyAlgorithms() {
    this.consistencyCheckBoxes = new ArrayList<>();
    final String path = MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    final List<String> list = ClassLoader.loadClassInfo(path, "cz/zcu/kiv/cacheSimulator/consistency");

    for (final String name : list) {
      final String[] names = name.split(";");
      final JCheckBox novy = new JCheckBox(names[1], false);
      novy.setName(names[0]);
      novy.addActionListener(MainGUI.this::consistencyCheckBoxActionPerformed);
      novy.addMouseMotionListener(new MouseMotionAdapter() {

        @Override
        public void mouseMoved(final MouseEvent e) {
          MainGUI.this.consistencyCheckBoxMouseMove(e);
        }
      });

      this.consistencyPanel.add(novy);
      this.consistencyCheckBoxes.add(novy);
    }
  }


  /**
   * metoda pro nahrani zaskrtavatek pro cache algoritmy
   */
  private void loadCaches() {
    this.cacheCheckBoxes = new ArrayList<>();
    final String path = MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    final List<String> cachingAlgorithms = ClassLoader.loadClassInfo(path, "cz/zcu/kiv/cacheSimulator/cachePolicies");

    // nacteni cache policies do checkboxlistu
    for (final String name : cachingAlgorithms) {
      final String[] names = name.split(";");
      final JCheckBox cacheCheckbox = new JCheckBox(names[1], true);

      cacheCheckbox.setName(names[0]);
      cacheCheckbox.addActionListener(MainGUI.this::cacheCheckBoxActionPerformed);
      cacheCheckbox.addMouseMotionListener(new MouseMotionAdapter() {

        @Override
        public void mouseMoved(final MouseEvent e) {
          MainGUI.this.cacheCheckBoxMouseMove(e);
        }
      });

      this.cachePanel.add(cacheCheckbox);
      this.cacheCheckBoxes.add(cacheCheckbox);
    }

    this.cachePanel.invalidate();
    this.cachePanel.repaint();
  }


  /**
   * metoda pro nacteni jmen trid, ktere vykresluji panely pro ruzna nastaveni
   */
  private void loadSettingsPanelNames() {
    this.settingsPanels = new ArrayList<>();
    final String path = MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    final List<String> list = ClassLoader.loadClassInfo(path, "cz/zcu/kiv/cacheSimulator/gui/configuration");


    for (final String panelName : list) {
      try {
        this.settingsPanels.add((JPanel) Class.forName(panelName).newInstance());
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * Creates new form MainGUI
   */
  private MainGUI() {
    this.initComponents();
    this.hideAllInput();
    this.loadCaches();
    this.loadConsistencyAlgorithms();
    this.loadValuesFromGlobalVar();
    this.loadSettingsPanelNames();
    this.centerOnScreen();
    this.simulationProgressBar.setVisible(false);
  }


  /**
   * metoda pro vycentrovani framu na obrazovce
   */
  private void centerOnScreen() {
    // center frame on screen
    final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    // Determine the new location of the window
    final int w = this.getSize().width;
    final int h = this.getSize().height;
    final int x = (dim.width - w) / 2;
    final int y = (dim.height - h) / 2;

    // Move the window
    this.setLocation(x, y);
  }


  /**
   * obsluha udalosti pro spusteni simulace
   *
   * @param evt
   */
  private void simulateActionPerformed(final ActionEvent evt) {
    this.runSimulation();
  }


  /**
   * obsluha udalosti predcasneho ukonceni simulace
   *
   * @param evt
   */
  private void simCancelButtonActionPerformed(final ActionEvent evt) {
    if (this.simulationThread != null) {
      this.simulationThread.stopSimulation();
    }

    Server.getInstance().hardReset();

    this.enableComponentsAfterSimulaton(false);
  }


  /**
   * metoda pro obsluhu stisku tlacitka pro ukonceni simulatoru
   *
   * @param evt
   */
  private void exitActionPerformed(final ActionEvent evt) {
    ConfigReaderWriter.write();
    System.exit(0);
  }


  /**
   * obsluha udalosti uzavirani formulare
   *
   * @param evt
   */
  private void formWindowClosing(final WindowEvent evt) {
    ConfigReaderWriter.write();
  }


  /**
   * meotoda pro spusteni simualce
   */
  private void runSimulation() {
    if (this.checkSettings()) {
      this.simulationThread = null;
      this.cacheResults = null;
      this.consSimulation = null;

      Server.getInstance().hardReset();

      // nastaveni nahodnych generatoru cisel
      GlobalMethods.setGenerators();

      // vytvoreni objektu generatoru pristupovanych souboru
      IFileQueue fileQueue = null;
      if (this.requestsInputComboBox.getSelectedIndex() == 2) {
        fileQueue = new GaussianFileNameGenerator(
            GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 3) {
        fileQueue = new RandomFileNameGenerator(GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 4) {
        fileQueue = new RandomFileNameGeneratorWithPrefences(
            GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 5) {
        fileQueue = new ZipfFileNameGenerator(GlobalVariables.getRequestCountForRandomGenerator());
      } else if (GlobalVariables.isLoadDataFromLog()) {
        fileQueue = new LogReaderAFS(!this.randomFileSizeCheckBox.isSelected());
      }

      this.consSimulation = this.selectConsistencyControl();

      // velikosti cache
      final Integer[] sizes = ((CacheCapacityListModel) this.cacheCapacityList.getModel())
          .getArray();

      // nastaveni progress baru
      this.simulationProgressBar.setMinimum(0);
      this.simulationProgressBar.setMaximum(sizes.length);
      this.simulationProgressBar.setValue(0);
      this.simulationProgressBar.setVisible(true);
      this.simulationProgressBar.setStringPainted(true);
      this.simulationProgressBar.setString("Simulation in progress... 0%");

      // vlakno pro spusteni simulace - kvuli updatu progressbaru
      this.simulationThread = new AccessSimulation(fileQueue, this.consSimulation, this);

      // spusteni vlakna simulace
      new Thread(this.simulationThread).start();
    }
  }


  /**
   * Metoda pro test chyb v nastaveni
   *
   * @return true, pokud vse v poradku
   */
  private boolean checkSettings() {
    // kontrola vyberu vstupni metody
    if (this.requestsInputComboBox.getSelectedItem().equals("-- Choose one --")) {
      JOptionPane.showMessageDialog(this, "You have to select input request method!", "Alert",
          JOptionPane.ERROR_MESSAGE);
      this.panelsPane.setSelectedIndex(0);
      return false;
    } else if (this.requestsInputComboBox.getSelectedItem().equals("From AFS log file")) {
      final File f = new File(GlobalVariables.getLogAFSFIleName());
      if (!f.isFile()) {
        JOptionPane.showMessageDialog(this, "You have to select AFS log file!", "Alert",
            JOptionPane.ERROR_MESSAGE);
        this.panelsPane.setSelectedIndex(0);
        return false;
      }
    }

    // kontrola zaskrtavatek u cache algoritmu
    final List<String> cachenames = this.getCachesNames();
    if (cachenames == null || cachenames.size() == 0) {
      JOptionPane.showMessageDialog(this, "You have to select simulated cache algorithms!",
          "Alert", JOptionPane.ERROR_MESSAGE);
      this.panelsPane.setSelectedIndex(1);
      return false;
    }

    // kontrola consistency control
    if (this.simConsistencyCheck.isSelected()) {
      boolean selected = false;
      for (final JCheckBox check : this.consistencyCheckBoxes) {
        if (check.isSelected()) {
          selected = true;
          break;
        }
      }
      if (!selected) {
        JOptionPane.showMessageDialog(this, "You have to select consistency control algorithm!",
            "Alert", JOptionPane.ERROR_MESSAGE);
        this.panelsPane.setSelectedIndex(2);
        return false;
      }
    }
    return true;
  }


  /**
   * metoda pro nahrani vysledku mereni konsistentnosti do tabulky
   *
   * @param cons konsistentnost
   */
  @SuppressWarnings("unchecked")
  public void loadConResultsToPanel(final IConsistencySimulation cons) {
    if (cons == null)
      return;
    this.userListConsistency.setModel(new UserListModel(this));
    this.cachePolList.setModel(new CacheNamesListModel());

    this.userListConsistency.setSelectedIndex(0);
    this.cachePolList.setSelectedIndex(0);
  }


  /**
   * metoda pro nahrani vysledku do panelu cache results
   */
  @SuppressWarnings("unchecked")
  public void loadResultsToPanel() {
    // nacteni uzivatelu
    this.userList.setModel(new UserListModel(this));
    this.userList.setSelectedIndex(0);

    // oznaceni prvniho
    this.resultsChangeCombo.setSelectedIndex(0);

    // otevreni panelu s vysledky
    this.panelsPane.setEnabledAt(3, true);

    // nahrani vysledku do tabulky
    this.loadResultsToTable();
  }


  /**
   * metoda pro nacteni vysledku do tabulky
   */
  private void loadResultsToTable() {
    if (this.cacheResults == null || this.cacheResults.isEmpty())
      return;
    if (this.userList.getSelectedIndex() < 0)
      return;
    this.totalNetTextField.setText(Long.toString(this.cacheResults.get(
        this.userList.getSelectedIndex()).getTotalNetworkBandwidth() / 1024 / 1024));
    this.totalReqTextField.setText(Long.toString(this.cacheResults.get(
        this.userList.getSelectedIndex()).getFileAccessed()));
    final UserStatistics stat = this.cacheResults.get(this.userList.getSelectedIndex());

    final String[] cacheNames = stat.getCacheNames();

    final String names[] = new String[this.getCacheSizes().length + 1];
    names[0] = "Cache Policy";
    int index = 1;
    for (final Integer cap : stat.getCacheSizes()) {
      names[index++] = cap + "MB";
    }

    Object[] row;
    final int length = cacheNames.length;
    final Object[][] rowData = new Object[length][stat.getCacheSizes().length + 1];

    for (int i = 0; i < length; i++) {
      if (this.resultsChangeCombo.getSelectedIndex() == 0) {

        row = stat.getCacheHitRatios(cacheNames[i]);
      } else if (this.resultsChangeCombo.getSelectedIndex() == 1) {
        row = stat.getCacheHits(cacheNames[i]);
      } else if (this.resultsChangeCombo.getSelectedIndex() == 2) {
        row = stat.getCacheSavedBytesRatio(cacheNames[i]);
      } else if ((this.resultsChangeCombo.getSelectedIndex() == 3)) {
        row = stat.getSavedBytes(cacheNames[i]);
      } else if ((this.resultsChangeCombo.getSelectedIndex() == 5)) {
        row = stat.getDataTransferDegrease(cacheNames[i]);
      } else {
        row = stat.getDataTransferDegreaseRatio(cacheNames[i]);
      }
      rowData[i][0] = cacheNames[i];

      for (int j = 1; j < names.length; j++) {
        rowData[i][j] = row[j - 1];
      }
    }

    final TableModel tm = new PolicyResultTable(rowData, names);

    this.resultsTable.setModel(tm);
    final RowSorter<TableModel> sorter = new TableRowSorter<>(tm);

    this.resultsTable.setRowSorter(sorter);
  }


  /**
   * metoda pro ziskani consistency control algoritmu
   *
   * @return vybrana metoda
   */
  private IConsistencySimulation selectConsistencyControl() {
    if (!this.simConsistencyCheck.isSelected())
      return null;
    String consName = "";
    for (final JCheckBox check : this.consistencyCheckBoxes) {
      if (check.isSelected()) {
        consName = check.getName();
        break;
      }
    }
    if (consName.length() == 0)
      return null;
    try {
      return (IConsistencySimulation) Class.forName(
          "cz.zcu.kiv.cacheSimulator.consistency." + consName).newInstance();
    } catch (final InstantiationException e) {
      return null;
    } catch (final IllegalAccessException e) {
      return null;
    } catch (final ClassNotFoundException e) {
      return null;
    }
  }


  /**
   * obslua udalosti pro vyber vstupniho logovaciho souboru z AFS
   *
   * @param evt
   */
  private void pathTextFieldMouseClicked(final MouseEvent evt) {
    this.chooseAFSFile();
  }


  /**
   * metoda pro vybrani souboru pro vstup
   */
  private void chooseAFSFile() {
    final JFileChooser fc = new JFileChooser(GlobalVariables.getActDir());
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setDialogTitle("Open AFS log file...");
    final int returnVal = fc.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      this.pathTextField.setText(fc.getSelectedFile().getAbsolutePath());
      GlobalVariables.setLogAFSFIleName(fc.getSelectedFile().getAbsolutePath());
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
   *
   * @param evt
   */
  private void randomFileSizeCheckBoxItemStateChanged(final ItemEvent evt) {
    GlobalVariables.setRandomFileSizesForLoggedData(this.randomFileSizeCheckBox.isSelected());
    if (this.randomFileSizeCheckBox.isSelected()) {
      this.minFileSizeLabel.setVisible(true);
      this.maxFileSizeLabel.setVisible(true);
      this.unitsLabel2.setVisible(true);
      this.unitsLabel1.setVisible(true);
      this.minGenFileSizejSpinner.setVisible(true);
      this.maxGenFileSizejSpinner.setVisible(true);
    } else {
      this.minFileSizeLabel.setVisible(false);
      this.maxFileSizeLabel.setVisible(false);
      this.unitsLabel2.setVisible(false);
      this.unitsLabel1.setVisible(false);
      this.minGenFileSizejSpinner.setVisible(false);
      this.maxGenFileSizejSpinner.setVisible(false);
    }
  }


  /**
   * obsluha udalosti vyber vstupu simulator (soubor x random)
   *
   * @param evt
   */
  private void requestsInputComboBoxItemStateChanged(final ItemEvent evt) {

    if (evt.getStateChange() == ItemEvent.DESELECTED) {
      return;
    }
    final String ret = (String) evt.getItem();

    if (ret.equalsIgnoreCase("-- Choose one --")) {
      this.hideAllInput();
      GlobalVariables.setLoadDataFromLog(false);
    } else if (ret.equalsIgnoreCase("From afs log file")) {
      GlobalVariables.setLoadDataFromLog(true);
      this.showInputFile();
      this.chooseAFSFile();
    } else if (ret.equalsIgnoreCase("Gaussian random")) {
      GlobalVariables.setLoadDataFromLog(false);
      GlobalVariables.setRandomFileSizesForLoggedData(true);
      this.showGaussInput();
    } else if (ret.equalsIgnoreCase("Random with preference")) {
      GlobalVariables.setLoadDataFromLog(false);
      GlobalVariables.setRandomFileSizesForLoggedData(true);
      this.showPrefRandomInput();
    } else if (ret.equalsIgnoreCase("Zipf random")) {
      GlobalVariables.setLoadDataFromLog(false);
      GlobalVariables.setRandomFileSizesForLoggedData(true);
      this.showZipfInput();
    } else {
      GlobalVariables.setLoadDataFromLog(false);
      GlobalVariables.setRandomFileSizesForLoggedData(true);
      this.showRandomInput();
    }
  }


  /**
   * obsluha udalosti pridani nove kapacity cache
   *
   * @param evt
   */
  private void cachePlusButtonActionPerformed(final ActionEvent evt) {
    final Integer newCacheCap = (Integer) this.cacheCapSpinner.getValue();

    if (newCacheCap <= 0) {
      JOptionPane.showMessageDialog(this, "You have to insert positive integer!", "Alert",
          JOptionPane.OK_OPTION);
      return;
    }

    final CacheCapacityListModel model = (CacheCapacityListModel) this.cacheCapacityList.getModel();

    try {
      model.add(newCacheCap);
      this.cacheCapacityList.invalidate();
      this.cacheCapacityList.repaint();
    } catch (final Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);
    }
  }


  /**
   * metoda pro odebrani polozky z cache capacity
   *
   * @param evt
   */
  private void cacheMinusButtonActionPerformed(final ActionEvent evt) {
    if (this.cacheCapacityList.getSelectedIndex() == -1) {
      JOptionPane.showMessageDialog(this, "You have to select cache capacity to dismiss!", "Alert",
          JOptionPane.OK_OPTION);
      return;
    }
    final CacheCapacityListModel model = (CacheCapacityListModel) this.cacheCapacityList.getModel();
    model.remove(this.cacheCapacityList.getSelectedIndex());
    this.cacheCapacityList.invalidate();
    this.cacheCapacityList.repaint();
  }


  /**
   * obsluha udalosti zmena poctu pozadavku, aby byl vysledek zahrnut
   *
   * @param evt
   */
  private void statLimitSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.statLimitSpinner.getValue() < 1) {
      this.statLimitSpinner.setValue(1);
    }
    GlobalVariables.setLimitForStatistics((Integer) this.statLimitSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena rychlosti site - pro soubory, ktere jsou vetsi nez cache
   *
   * @param evt
   */
  private void networkSpeedSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.networkSpeedSpinner.getValue() < 1) {
      this.networkSpeedSpinner.setValue(1);
    } else if ((Integer) this.networkSpeedSpinner.getValue() > 10000) {
      this.networkSpeedSpinner.setValue(10000);

    }
    GlobalVariables.setAverageNetworkSpeed((Integer) this.networkSpeedSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena velisti klouzajiciho okenka - pro soubory, ktere jseou vetsi nez cache
   *
   * @param evt
   */
  private void slidingWindwowSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.slidingWindwowSpinner.getValue() < 0) {
      this.slidingWindwowSpinner.setValue(0);
    } else if ((Integer) this.slidingWindwowSpinner.getValue() > 75) {
      this.slidingWindwowSpinner.setValue(75);
    }
    GlobalVariables.setCacheCapacityForDownloadWindow(((Integer) this.slidingWindwowSpinner
        .getValue()).intValue());
  }


  /**
   * obsluha udalosti zmena maximalni velikosti generovanych souboru
   *
   * @param evt
   */
  private void maxGenFileSizejSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.maxGenFileSizejSpinner.getValue() < 0) {
      this.maxGenFileSizejSpinner.setValue(0);
    } else if ((Integer) this.maxGenFileSizejSpinner.getValue() <= (Integer) this.minGenFileSizejSpinner
        .getValue()) {
      this.maxGenFileSizejSpinner.setValue((Long) this.minGenFileSizejSpinner.getValue() + 1);
    }
    GlobalVariables.setMaxGeneratedFileSize((Integer) this.maxGenFileSizejSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena minimalni velikosti generovanych souboru
   *
   * @param evt
   */
  private void minGenFileSizejSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.minGenFileSizejSpinner.getValue() < 0) {
      this.minGenFileSizejSpinner.setValue(0);
    } else if ((Integer) this.maxGenFileSizejSpinner.getValue() <= (Integer) this.minGenFileSizejSpinner
        .getValue()) {
      this.minGenFileSizejSpinner.setValue((Integer) this.maxGenFileSizejSpinner.getValue() - 1);
    }
    GlobalVariables.setMinGeneratedFileSize((Integer) this.minGenFileSizejSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena zaskrtnuti policka pro nahodne generovani velikosti souboru
   *
   * @param evt
   */
  private void randomFileSizeCheckBoxActionPerformed(final ActionEvent evt) {
    GlobalVariables.setRandomFileSizesForLoggedData(this.randomFileSizeCheckBox.isSelected());
  }


  /**
   * obsluha udalosti zmena spinneru u nepreferovanych souboru v pref. generatoru
   *
   * @param evt
   */
  private void nonPrefSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.nonPrefSpinner.getValue() <= 0) {
      this.nonPrefSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestnNonPreferenceFile((Integer) this.nonPrefSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena spinneru kroku v pref. generatoru
   *
   * @param evt
   */
  private void stepPrefSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.stepPrefSpinner.getValue() <= 0) {
      this.stepPrefSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestPreferenceFile((Integer) this.stepPrefSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena spinneru u delitelnosti pref. souboru souboru v pref. generatoru
   *
   * @param evt
   */
  private void preferenceDivisibleSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.preferenceDivisibleSpinner.getValue() <= 0) {
      this.preferenceDivisibleSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestPreferenceStep((Integer) this.preferenceDivisibleSpinner
        .getValue());
  }


  /**
   * obsluha udalosti zmena spinneru pro disperzi u Gauss. generatoru
   *
   * @param evt
   */
  private void dispersionSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.dispersionSpinner.getValue() <= 0) {
      this.dispersionSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestGeneratorDispersion((Integer) this.dispersionSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena spinneru pro stredni hodnotu u Gauss. generatoru
   *
   * @param evt
   */
  private void maenValueGaussSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.maenValueGaussSpinner.getValue() <= 0) {
      this.maenValueGaussSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestGeneratorMeanValue((Integer) this.maenValueGaussSpinner
        .getValue());
  }


  /**
   * obsluha udalosti zmeny alfa spinneru u zipf generatoru
   *
   * @param evt
   */
  private void zipfLamdbaSpinnerStateChanged(final ChangeEvent evt) {
    GlobalVariables.setZipfLambda((Double) this.zipfLamdbaSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena spinneru pro zmenu poctu generovanch souboru
   *
   * @param evt
   */
  private void generateFileSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.generateFileSpinner.getValue() <= 0) {
      this.generateFileSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestGeneratorMaxValue((Integer) this.generateFileSpinner.getValue());
  }


  /**
   * obsluha udalosti zmena spinneru pro zmenu postu generovanych pozadavku
   *
   * @param evt
   */
  private void requestCountSpinnerStateChanged(final ChangeEvent evt) {
    if ((Integer) this.requestCountSpinner.getValue() <= 0) {
      this.requestCountSpinner.setValue(1);
    }
    GlobalVariables
        .setRequestCountForRandomGenerator((Integer) this.requestCountSpinner.getValue());
  }


  /**
   * obsluha udalosti vyberu cachovaci politiky (zaskrtnuti)
   *
   * @param evt
   */
  private void cachePolListValueChanged(final ListSelectionEvent evt) {
    int cacheIndex = 0, userIndex = 0;
    if (this.cachePolList.getSelectedIndex() >= 0)
      cacheIndex = this.cachePolList.getSelectedIndex();
    if (this.userListConsistency.getSelectedIndex() >= 0)
      userIndex = this.userListConsistency.getSelectedIndex();
    final String cache = ((CacheNamesListModel) this.cachePolList.getModel()).cacheClass[cacheIndex];
    final long userID = ((UserListModel) this.userListConsistency.getModel()).userIDs[userIndex];

    final Object[][] data = this.consSimulation.getData(cache, userID);
    TableModel tm = null;
    if (data != null)
      tm = new DefaultTableModel(data, this.consSimulation.getHeaders());
    else {
      final DefaultTableModel dtm = new DefaultTableModel(0, 0);
      dtm.addColumn("No data for visualisation!");
      tm = dtm;
    }
    this.consistencyTable.setModel(tm);
    final RowSorter<TableModel> sorter = new TableRowSorter<>(tm);
    this.consistencyTable.setRowSorter(sorter);
  }


  /**
   * obsluha udalosti vyberu consistency control (zaskrtnuti)
   *
   * @param evt
   */
  private void userListConsistencyValueChanged(final ListSelectionEvent evt) {
    int cacheIndex = 0, userIndex = 0;
    if (this.cachePolList.getSelectedIndex() >= 0)
      cacheIndex = this.cachePolList.getSelectedIndex();
    if (this.userListConsistency.getSelectedIndex() >= 0)
      userIndex = this.userListConsistency.getSelectedIndex();
    final String cache = ((CacheNamesListModel) this.cachePolList.getModel()).cacheClass[cacheIndex];
    final long userID = ((UserListModel) this.userListConsistency.getModel()).userIDs[userIndex];

    final Object[][] data = this.consSimulation.getData(cache, userID);
    TableModel tm = null;
    if (data != null)
      tm = new DefaultTableModel(data, this.consSimulation.getHeaders());
    else {
      final DefaultTableModel dtm = new DefaultTableModel(0, 0);
      dtm.addColumn("No data for visualisation!");
      tm = dtm;
    }
    this.consistencyTable.setModel(tm);
    final RowSorter<TableModel> sorter = new TableRowSorter<>(tm);
    this.consistencyTable.setRowSorter(sorter);
  }


  /**
   * obsluha udalosti vyberu vysledku u cache policies (ration, saved bytes...)
   *
   * @param evt
   */
  private void resultsChangeComboActionPerformed(final ActionEvent evt) {
    this.loadResultsToTable();
  }


  /**
   * obsluha udalosti vyberu vysledku simulovaneho uzivatele
   *
   * @param evt
   */
  private void userListValueChanged(final ListSelectionEvent evt) {
    this.loadResultsToTable();
  }


  /**
   * obsluha udalosti zaskrtnuti tlkacitka, kdy se nejpre nactou statistiky na server a az pak se
   * simuluje
   *
   * @param evt
   */
  private void loadServStatActionPerformed(final ActionEvent evt) {
    GlobalVariables.setLoadServerStatistic(this.loadServStat.isSelected());
  }


  /**
   * obsluha udalosti zaskrtnuti moznosti simulace consistency control
   *
   * @param evt
   */
  private void simConsistencyCheckedChanged(final ItemEvent evt) {
    this.panelsPane.setEnabledAt(2, this.simConsistencyCheck.isSelected());
    if (this.simConsistencyCheck.isSelected()) {
      this.loadServStat.setEnabled(false);
      this.loadServStat.setSelected(true);
      GlobalVariables.setLoadServerStatistic(this.loadServStat.isSelected());
    } else {
      this.loadServStat.setEnabled(true);
      GlobalVariables.setLoadServerStatistic(this.loadServStat.isSelected());
    }
  }


  /**
   * metoda pro obsluhu udalosti stisknuti tlacitka pro ulozeni vysledku policies do CVS souboru
   *
   * @param evt
   */
  private void saveCSVActionPerformed(final ActionEvent evt) {
    String fName = this.getFileNameForSavingResults(".csv", "CVS Files");
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
   *
   * @param evt
   */
  private void saveXLSActionPerformed(final ActionEvent evt) {
    String fName = this.getFileNameForSavingResults(".xls", "XLS Files");
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
   *
   * @param evt
   */
  private void printConsoleActionPerformed(final ActionEvent evt) {
    Output.printAllStatConsole(this.cacheResults);
  }


  /**
   * metoda pro obsluhu stisku tlacitek pro tisk vysledku consistency do konzole
   *
   * @param evt
   */
  protected void printConsoleConsistencyActionPerformed(final ActionEvent evt) {
    Output.printAllStatConsoleConsistency(this.consSimulation,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheClass,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheNames,
        ((UserListModel) this.userListConsistency.getModel()).userIDs);
  }


  /**
   * metoda pro obsluhu stisku tlacitek pro ulozenivysledku consistency do XLS souboru
   *
   * @param evt
   */
  protected void saveXLSConsistencyActionPerformed(final ActionEvent evt) {
    String fName = this.getFileNameForSavingResults(".xls", "XLS files");
    if (fName.length() == 0)
      return;
    if (!fName.endsWith(".xls")) {
      fName = fName + ".xls";
    }
    Output.saveConsistencyControlXLS(this.consSimulation,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheClass,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheNames,
        ((UserListModel) this.userListConsistency.getModel()).userIDs, fName);
    GlobalVariables.setActDir(fName);
  }


  /**
   * metoda pro obsluhu stisku tlacitek pro ulozenivysledku consistency do CSV souboru
   *
   * @param evt
   */
  protected void saveCSVConsistencyActionPerformed(final ActionEvent evt) {
    String fName = this.getFileNameForSavingResults(".csv", "CSV files");
    if (fName.length() == 0)
      return;
    if (!fName.endsWith(".csv")) {
      fName = fName + ".csv";
    }
    Output.saveConsistencyControlCSV(this.consSimulation,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheClass,
        ((CacheNamesListModel) this.cachePolList.getModel()).cacheNames,
        ((UserListModel) this.userListConsistency.getModel()).userIDs, fName);
    GlobalVariables.setActDir(fName);
  }


  /**
   * metoda pro nacteni jmena souboru pto ulozeni vysledku
   *
   * @param endings koncovka souboru
   * @param description popis souboru
   * @return jmeno suoboru vcetne cesty pro ulozeni
   */
  private String getFileNameForSavingResults(final String endings, final String description) {
    final JFileChooser fc = new JFileChooser(GlobalVariables.getActDir()) {

      @Override
      public void approveSelection() {
        final File f = this.getSelectedFile();
        if (f.exists() && this.getDialogType() == SAVE_DIALOG) {
          final int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?",
              "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
          switch (result) {
            case JOptionPane.YES_OPTION:
              super.approveSelection();
              return;
            case JOptionPane.NO_OPTION:
              return;
            case JOptionPane.CLOSED_OPTION:
              return;
            case JOptionPane.CANCEL_OPTION:
              this.cancelSelection();
              return;
          }
        } else if (!f.exists()) {
          super.approveSelection();
        }
      }
    };
    fc.setDialogTitle("Save results as");
    final FileFilter ff = new FileFilter() {

      @Override
      public boolean accept(final File f) {
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
    final int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      return fc.getSelectedFile().getAbsolutePath();
    }
    return "";
  }


  /**
   * obsluha udalosti vyberu vstupu z AFS logu z menu
   *
   * @param evt
   */
  private void inputAFSMenuItemActionPerformed(final ActionEvent evt) {
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(1);
  }


  /**
   * obsluha udalosti vyberu vstupu pomoci Gauss. random z menu
   *
   * @param evt
   */
  private void inputGaussianMenuItemActionPerformed(final ActionEvent evt) {
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(2);
  }


  /**
   * obsluha udalosti vyberu vstupu pomoci uniformly random z menu
   *
   * @param evt
   */
  private void inputUniformlyMenuItemActionPerformed(final ActionEvent evt) {
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(3);
  }


  /**
   * obsluha udalosti vyberu vstupu pomoci random s preferenci menu
   *
   * @param evt
   */
  private void inputRandomPrefMenuItemActionPerformed(final ActionEvent evt) {
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(4);
  }


  /**
   * obsluha udalosti vyberu vstupu pomoci zipf random z menu
   *
   * @param evt
   */
  private void inputZipfMenuItemActionPerformed(final ActionEvent evt) {
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(5);
  }


  /**
   * reakce na stisk tlacitka About - vytisteni informaci o autorovi
   *
   * @param evt
   */
  private void aboutMenuItemActionPerformed(final ActionEvent evt) {
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
   *
   * @param evt
   */
  private void barChartButtonActionPerformed(final ActionEvent evt) {
    final BarChart chart = new BarChart(this.resultsChangeCombo.getSelectedItem().toString(),
        this.cacheResults.get(this.userList.getSelectedIndex()),
        this.resultsChangeCombo.getSelectedIndex());
    chart.pack();
    RefineryUtilities.centerFrameOnScreen(chart);
    chart.setVisible(true);
  }


  /**
   * obsluha udalosti stisku tlacitka pro vizualizaci spojnicovym grafem
   *
   * @param evt
   */
  private void lineChartButtonActionPerformed(final ActionEvent evt) {
    final LineChart chart = new LineChart(this.resultsChangeCombo.getSelectedItem().toString(),
        this.cacheResults.get(this.userList.getSelectedIndex()),
        this.resultsChangeCombo.getSelectedIndex());
    chart.pack();
    RefineryUtilities.centerFrameOnScreen(chart);
    chart.setVisible(true);
  }


  /**
   * obsluha zmeny tlacitka pro vyber cache algoritmu
   *
   * @param evt
   */
  private void cacheCheckBoxActionPerformed(final ActionEvent evt) {
    final String name = ((JCheckBox) evt.getSource()).getName() + "Panel";
    JPanel cacheSettings = this.noSettingsPanelCache;
    for (final JPanel panel : this.settingsPanels) {
      if (panel.getClass().getName().contains(name)) {
        cacheSettings = panel;
        break;
      }
    }
    this.cachePane.setRightComponent(cacheSettings);
    this.cachePane.invalidate();
    this.cachePane.repaint();
  }


  /**
   * obsluha udalosti prejeti mysi po zaskrtavatku pro cache simulator
   *
   * @param evt
   */
  private void cacheCheckBoxMouseMove(final MouseEvent evt) {
    final String name = ((JCheckBox) evt.getSource()).getName() + "Panel";

    JPanel cacheSettings = this.noSettingsPanelCache;
    for (final JPanel panel : this.settingsPanels) {
      if (panel.getClass().getName().contains(name)) {
        cacheSettings = panel;
        break;
      }
    }

    this.cachePane.setRightComponent(cacheSettings);
    this.cachePane.invalidate();
    this.cachePane.repaint();
  }


  /**
   * obsluha zmeny tlacitka pro vyber consistency control algoritmu
   *
   * @param evt
   */
  private void consistencyCheckBoxActionPerformed(final ActionEvent evt) {
    JCheckBox act = null;
    for (final JCheckBox check : this.consistencyCheckBoxes) {
      if (check.isSelected()) {
        act = check;
        break;
      }
    }
    if (act == null) {
      for (final JCheckBox check : this.consistencyCheckBoxes) {
        check.setEnabled(true);
      }
    } else {
      for (final JCheckBox check : this.consistencyCheckBoxes) {
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
  private void consistencyCheckBoxMouseMove(final MouseEvent evt) {
    final String name = ((JCheckBox) evt.getSource()).getName() + "Panel";
    JPanel consistencySettings = this.noSettingsPanelConsistency;
    for (final JPanel panel : this.settingsPanels) {
      if (panel.getClass().getName().contains(name)) {
        consistencySettings = panel;
        break;
      }
    }
    if (consistencySettings instanceof MMWPBatchConsistencyPanel) {
      ((MMWPBatchConsistencyPanel) consistencySettings).updateValues();
    } else if (consistencySettings instanceof MMWPConsistencyPanel) {
      ((MMWPConsistencyPanel) consistencySettings).updateValues();
    }
    this.ConsistencyControlPane.setRightComponent(consistencySettings);
    this.consistencyPanel.invalidate();
    this.consistencyPanel.repaint();
  }

  /**
   * trida pro prezentaci modelu pro pole velikosti
   */
  private static class CacheCapacityListModel extends AbstractListModel<Integer> {

    List<Integer> cacheSizes;


    @Override
    public int getSize() {
      return this.cacheSizes.size();
    }


    @Override
    public Integer getElementAt(final int i) {
      return this.cacheSizes.get(i);
    }


    public void add(final Integer i) {
      if (this.cacheSizes.contains(i)) {
        throw new RuntimeException("You have to insert different value!");
      } else if (this.cacheSizes.size() > 6) {
        throw new RuntimeException("Maximum count of sizes reached.");
      }

      this.cacheSizes.add(i);
      Collections.sort(this.cacheSizes);
    }


    /**
     * metoda pro odebrani polozky
     *
     * @param index
     *          index polozky
     */
    public void remove(final int index) {
      this.cacheSizes.remove(index);
    }


    public CacheCapacityListModel() {
      this.cacheSizes = new LinkedList<>(Arrays.asList(16, 32, 64, 128, 256, 512, 1024));
    }


    /**
     * metoda pro ziskani pole velikosti
     *
     * @return pole velikosti
     */
    public Integer[] getArray() {
      return this.cacheSizes.toArray(new Integer[this.cacheSizes.size()]);
    }
  }

  /**
   * trida pro prezentaci modelu pro pole velikosti
   */
  private static class UserListModel extends AbstractListModel<String> {

    long[] userIDs;
    String[] userNames;


    @Override
    public int getSize() {
      return this.userNames.length;
    }


    @Override
    public String getElementAt(final int i) {
      return this.userNames[i];
    }


    public UserListModel(final MainGUI gui) {
      this.userIDs = new long[gui.cacheResults.size()];
      this.userNames = new String[gui.cacheResults.size()];
      for (int i = 0; i < gui.cacheResults.size(); i++) {
        this.userIDs[i] = gui.cacheResults.get(i).getUserID();
        if (this.userIDs[i] == 0) {
          this.userNames[i] = "Simulated user";
        } else {
          final long id = this.userIDs[i] >> 32;
          this.userNames[i] = id + ", ip: " + (GlobalMethods.intToIp(this.userIDs[i] - (id << 32)));
        }
      }
    }
  }

  /**
   * trida pro prezentaci modelu pro pole velikosti
   */
  @SuppressWarnings("rawtypes")
  private class CacheNamesListModel extends AbstractListModel {

    String[] cacheNames;
    String[] cacheClass;


    @Override
    public int getSize() {
      return this.cacheNames.length;
    }


    @Override
    public Object getElementAt(final int i) {
      return this.cacheNames[i];
    }


    public CacheNamesListModel() {
      int count = 0;
      for (final JCheckBox box : MainGUI.this.cacheCheckBoxes) {
        if (box.isSelected())
          count++;
      }
      this.cacheNames = new String[count];
      this.cacheClass = new String[count];
      count = 0;
      for (final JCheckBox box : MainGUI.this.cacheCheckBoxes) {
        if (box.isSelected()) {
          this.cacheNames[count] = box.getText();
          this.cacheClass[count] = box.getName();
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
  public List<String> getCachesNames() {
    if (this.cacheCheckBoxes == null) {
      return null;
    }

    return this.cacheCheckBoxes.stream()
        .filter(checkbox -> checkbox.isSelected())
        .map(checkbox -> checkbox.getName())
        .collect(Collectors.toList());
  }


  /**
   * metoda pro vraceni pole velikosti cachi
   *
   * @return velikosti cache
   */
  public Integer[] getCacheSizes() {
    return ((CacheCapacityListModel) this.cacheCapacityList.getModel()).getArray();
  }

  // promenne pro kontrolky na gui
  private JSplitPane ConsistencyControlPane;
  private JCheckBox loadServStat;
  private JCheckBox simConsistencyCheck;
  private JMenuItem aboutMenuItem;
  private JButton barChartButton;
  private JSpinner cacheCapSpinner;
  @SuppressWarnings("rawtypes")
  private JList cacheCapacityList;
  private JPanel cacheCapacityPanel;
  private JButton cacheMinusButton;
  private JSplitPane cachePane;
  private JPanel cachePanel;
  private JPanel consistencyPanel;
  private JButton cachePlusButton;
  private JLabel chooseResultsLabel;
  private JLabel dispersionLabel;
  private JSpinner dispersionSpinner;
  private JButton exitButton;
  private JMenuItem exitMenuItem;
  private JMenu fileMenu;
  private JPopupMenu.Separator fileMenuSeparator;
  private JLabel generateFileLabel;
  private JSpinner generateFileSpinner;
  private JMenu helpMenu;
  private JMenuItem inputAFSMenuItem;
  private JMenuItem inputGaussianMenuItem;
  private JMenuItem inputRandomPrefMenuItem;
  private JMenuItem inputZipfMenuItem;
  private JLabel inputRequestLabel;
  private JMenuItem inputUniformlyMenuItem;
  private JLabel unitsLabel3;
  private JScrollPane cacheCapScrollPane;
  private JScrollPane userListScrollPane;
  private JScrollPane resultsTableScrollPane;
  private JToolBar.Separator toolbarSeparator1;
  private JToolBar.Separator toolbarSeparator2;
  private JButton lineChartButton;
  private JSpinner maenValueGaussSpinner;
  private JLabel maxFileSizeLabel;
  private JSpinner maxGenFileSizejSpinner;
  private JLabel meanValueGaussLabel;
  private JMenuBar menuBar;
  private JLabel minFileSizeLabel;
  private JSpinner minGenFileSizejSpinner;
  private JLabel networkSpeedLabel;
  private JSpinner networkSpeedSpinner;
  private JLabel networkSpeesLabel2;
  private JLabel noSettingsLabelCache;
  private JPanel noSettingsPanelCache;
  private JLabel noSettingsLabelConsistency;
  private JPanel noSettingsPanelConsistency;
  private JLabel nonPrefLabel;
  private JSpinner nonPrefSpinner;
  private JPanel othersSettingsPanel;
  private JTabbedPane panelsPane;
  private JLabel pathLabel;
  private JTextField pathTextField;
  private JLabel preferenceDivisibleLabel;
  private JSpinner preferenceDivisibleSpinner;
  private JButton printConsoleButton;
  private JCheckBox randomFileSizeCheckBox;
  private JSpinner requestCountSpinner;
  private JMenu requestMenu;
  private JLabel requestsCountLabel;
  @SuppressWarnings("rawtypes")
  private JComboBox requestsInputComboBox;
  private JPanel requestsSettingsPanel;
  @SuppressWarnings("rawtypes")
  private JComboBox resultsChangeCombo;
  private JPanel resultsPane;
  private JTable resultsTable;
  private JButton saveCSVButton;
  private JMenuItem saveCSVMenuItem;
  private JMenuItem saveConsoleMenuItem;
  private JButton saveXLSButton;
  private JMenuItem saveXLSMenuItem;
  private JPanel settingsPane;
  private JButton simCancelButton;
  private JButton simulateButton;
  private JMenuItem simulateMenuItem;
  private JMenu simulationMenu;
  public JProgressBar simulationProgressBar;
  private JToolBar simulatorToolbar;
  private JLabel slidingWindowLabel;
  private JSpinner slidingWindwowSpinner;
  private JLabel statLimitLabel;
  private JSpinner statLimitSpinner;
  private JPanel statusPanel;
  private JLabel stepPrefLabel;
  private JSpinner stepPrefSpinner;
  private JLabel totalNetLabel;
  private JTextField totalNetTextField;
  private JLabel totalReqLabel;
  private JTextField totalReqTextField;
  private JLabel unitsLabel1;
  private JLabel unitsLabel2;
  private JLabel userLabel;
  @SuppressWarnings("rawtypes")
  private JList userList;
  private JLabel zipfLambdaLabel;
  private JSpinner zipfLamdbaSpinner;

  // promenne pro kontrolky na zalozku s vysledky consistency control
  private JTable consistencyTable;
  private JScrollPane tablePane;
  private JPanel ConReultsPanel;
  @SuppressWarnings("rawtypes")
  private JList userListConsistency;
  private JLabel userListLabel;
  private JLabel cachePolLabel;
  @SuppressWarnings("rawtypes")
  private JList cachePolList;
  private JScrollPane cachePolScroll;
  private JScrollPane userListConScroll;

  // promenne pro kontrolky pro ukladani vysledku mereni konzistentnosti
  private JButton saveCSVConsistencyButton;
  private JMenuItem saveCSVConsistencyMenuItem;
  private JMenuItem saveConsoleConsistencyMenuItem;
  private JButton saveXLSConsistencyButton;
  private JMenuItem saveXLSConsistencyMenuItem;
  private JButton printConsoleConsistencyButton;
  private JToolBar.Separator toolbarSeparator3;
  private JPopupMenu.Separator fileMenuSeparator2;


  /**
   * metoda pro vytvoreni a vykresleni vsech komponent vcetne registrace udalosti a reakci na ne
   * metoda je automaticky vytvorena -> hodne dlouha
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void initComponents() {

    this.ConsistencyControlPane = new JSplitPane();
    this.simulatorToolbar = new JToolBar();
    this.simulateButton = new JButton();
    this.simCancelButton = new JButton();
    this.toolbarSeparator1 = new JToolBar.Separator();
    this.saveCSVButton = new JButton();
    this.saveXLSButton = new JButton();
    this.printConsoleButton = new JButton();
    this.toolbarSeparator2 = new JToolBar.Separator();
    this.exitButton = new JButton();
    this.panelsPane = new JTabbedPane();
    this.settingsPane = new JPanel();
    this.cacheCapacityPanel = new JPanel();
    this.cacheCapScrollPane = new JScrollPane();
    this.cacheCapacityList = new JList();
    this.cacheCapSpinner = new JSpinner();
    this.cachePlusButton = new JButton();
    this.cacheMinusButton = new JButton();
    this.othersSettingsPanel = new JPanel();
    this.statLimitLabel = new JLabel();
    this.statLimitSpinner = new JSpinner();
    this.networkSpeedLabel = new JLabel();
    this.networkSpeesLabel2 = new JLabel();
    this.networkSpeedSpinner = new JSpinner();
    this.slidingWindowLabel = new JLabel();
    this.loadServStat = new JCheckBox();
    this.simConsistencyCheck = new JCheckBox();
    this.slidingWindwowSpinner = new JSpinner();
    this.requestsSettingsPanel = new JPanel();
    this.requestsInputComboBox = new JComboBox();
    this.inputRequestLabel = new JLabel();
    this.randomFileSizeCheckBox = new JCheckBox();
    this.minFileSizeLabel = new JLabel();
    this.maxFileSizeLabel = new JLabel();
    this.unitsLabel2 = new JLabel();
    this.unitsLabel1 = new JLabel();
    this.minGenFileSizejSpinner = new JSpinner();
    this.maxGenFileSizejSpinner = new JSpinner();
    this.pathTextField = new JTextField();
    this.pathLabel = new JLabel();
    this.generateFileSpinner = new JSpinner();
    this.generateFileLabel = new JLabel();
    this.meanValueGaussLabel = new JLabel();
    this.maenValueGaussSpinner = new JSpinner();
    this.preferenceDivisibleLabel = new JLabel();
    this.preferenceDivisibleSpinner = new JSpinner();
    this.stepPrefLabel = new JLabel();
    this.stepPrefSpinner = new JSpinner();
    this.nonPrefLabel = new JLabel();
    this.nonPrefSpinner = new JSpinner();
    this.dispersionLabel = new JLabel();
    this.dispersionSpinner = new JSpinner();
    this.requestsCountLabel = new JLabel();
    this.requestCountSpinner = new JSpinner();
    this.zipfLambdaLabel = new JLabel();
    final SpinnerNumberModel zipfModel = new SpinnerNumberModel(0.75, 0.01, 10, 0.01);
    this.zipfLamdbaSpinner = new JSpinner();
    this.cachePane = new JSplitPane();
    this.cachePanel = new JPanel();
    this.consistencyPanel = new JPanel();
    this.noSettingsPanelCache = new JPanel();
    this.noSettingsLabelCache = new JLabel();
    this.noSettingsPanelConsistency = new JPanel();
    this.noSettingsLabelConsistency = new JLabel();
    this.resultsPane = new JPanel();
    this.userListScrollPane = new JScrollPane();
    this.userList = new JList();
    this.userLabel = new JLabel();
    this.resultsChangeCombo = new JComboBox();
    this.resultsTableScrollPane = new JScrollPane();
    this.resultsTable = new JTable();
    this.totalReqLabel = new JLabel();
    this.totalReqTextField = new JTextField();
    this.totalNetLabel = new JLabel();
    this.totalNetTextField = new JTextField();
    this.unitsLabel3 = new JLabel();
    this.chooseResultsLabel = new JLabel();
    this.barChartButton = new JButton();
    this.lineChartButton = new JButton();
    this.statusPanel = new JPanel();
    this.simulationProgressBar = new JProgressBar();
    this.menuBar = new JMenuBar();
    this.fileMenu = new JMenu();
    this.saveCSVMenuItem = new JMenuItem();
    this.saveXLSMenuItem = new JMenuItem();
    this.saveConsoleMenuItem = new JMenuItem();
    this.fileMenuSeparator = new JPopupMenu.Separator();
    this.exitMenuItem = new JMenuItem();
    this.simulationMenu = new JMenu();
    this.requestMenu = new JMenu();
    this.inputAFSMenuItem = new JMenuItem();
    this.inputGaussianMenuItem = new JMenuItem();
    this.inputUniformlyMenuItem = new JMenuItem();
    this.inputRandomPrefMenuItem = new JMenuItem();
    this.inputZipfMenuItem = new JMenuItem();
    this.simulateMenuItem = new JMenuItem();
    this.helpMenu = new JMenu();
    this.aboutMenuItem = new JMenuItem();

    this.userListConScroll = new JScrollPane();
    this.consistencyTable = new JTable();
    this.tablePane = new JScrollPane();
    this.ConReultsPanel = new JPanel();
    this.userListConsistency = new JList();
    this.userListLabel = new JLabel();
    this.cachePolLabel = new JLabel();
    this.cachePolList = new JList();
    this.cachePolScroll = new JScrollPane();

    this.toolbarSeparator3 = new JToolBar.Separator();
    this.saveCSVConsistencyButton = new JButton();
    this.printConsoleConsistencyButton = new JButton();
    this.saveXLSConsistencyButton = new JButton();
    this.saveCSVConsistencyMenuItem = new JMenuItem();
    this.saveConsoleConsistencyMenuItem = new JMenuItem();
    this.saveXLSConsistencyMenuItem = new JMenuItem();
    this.fileMenuSeparator2 = new JPopupMenu.Separator();

    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setTitle("Cache Simulator v2.1");
    this.setIconImage(new ImageIcon(this.getClass().getResource("/ico/simulation.png")).getImage());
    this.setLocationByPlatform(true);
    this.setResizable(false);
    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(final WindowEvent evt) {
        MainGUI.this.formWindowClosing(evt);
      }
    });

    this.simulatorToolbar.setFloatable(false);
    this.simulatorToolbar.setRollover(true);
    this.simulatorToolbar.setName("SimulatorToolar");
    this.simulatorToolbar.addSeparator();

    this.simulateButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/run.png")));
    this.simulateButton.setToolTipText("Simulate!");
    this.simulateButton.setFocusable(false);
    this.simulateButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.simulateButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.simulateButton.addActionListener(evt -> MainGUI.this.simulateActionPerformed(evt));
    this.simulatorToolbar.add(this.simulateButton);

    this.simCancelButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/cancel.png")));
    this.simCancelButton.setToolTipText("Cancel Simulation!");
    this.simCancelButton.setEnabled(false);
    this.simCancelButton.setFocusable(false);
    this.simCancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.simCancelButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.simCancelButton.addActionListener(evt -> MainGUI.this.simCancelButtonActionPerformed(evt));
    this.simulatorToolbar.add(this.simCancelButton);
    this.simulatorToolbar.add(this.toolbarSeparator1);

    this.saveCSVButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/csv.png")));
    this.saveCSVButton.setToolTipText("Save Caching Policie Results to CSV");
    this.saveCSVButton.setEnabled(false);
    this.saveCSVButton.setFocusable(false);
    this.saveCSVButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.saveCSVButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.saveCSVButton.addActionListener(evt -> MainGUI.this.saveCSVActionPerformed(evt));
    this.simulatorToolbar.add(this.saveCSVButton);

    this.saveXLSButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/xls.png"))); // NOI18N
    this.saveXLSButton.setToolTipText("Save Caching Policies Results to XLS");
    this.saveXLSButton.setEnabled(false);
    this.saveXLSButton.setFocusable(false);
    this.saveXLSButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.saveXLSButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.saveXLSButton.addActionListener(evt -> MainGUI.this.saveXLSActionPerformed(evt));
    this.simulatorToolbar.add(this.saveXLSButton);

    this.printConsoleButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/console.png")));
    this.printConsoleButton.setToolTipText("Print Caching Policie results to console");
    this.printConsoleButton.setEnabled(false);
    this.printConsoleButton.setFocusable(false);
    this.printConsoleButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.printConsoleButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.printConsoleButton.addActionListener(evt -> MainGUI.this.printConsoleActionPerformed(evt));
    this.simulatorToolbar.add(this.printConsoleButton);
    this.simulatorToolbar.add(this.toolbarSeparator2);

    this.saveCSVConsistencyButton
        .setIcon(new ImageIcon(this.getClass().getResource("/ico/csv.png")));
    this.saveCSVConsistencyButton.setToolTipText("Save Consistency Control results to CSV");
    this.saveCSVConsistencyButton.setEnabled(false);
    this.saveCSVConsistencyButton.setFocusable(false);
    this.saveCSVConsistencyButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.saveCSVConsistencyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.saveCSVConsistencyButton.addActionListener(evt -> MainGUI.this
        .saveCSVConsistencyActionPerformed(evt));
    this.simulatorToolbar.add(this.saveCSVConsistencyButton);

    this.saveXLSConsistencyButton
        .setIcon(new ImageIcon(this.getClass().getResource("/ico/xls.png"))); // NOI18N
    this.saveXLSConsistencyButton.setToolTipText("Save Consistency Control results to XLS");
    this.saveXLSConsistencyButton.setEnabled(false);
    this.saveXLSConsistencyButton.setFocusable(false);
    this.saveXLSConsistencyButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.saveXLSConsistencyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.saveXLSConsistencyButton.addActionListener(evt -> MainGUI.this
        .saveXLSConsistencyActionPerformed(evt));
    this.simulatorToolbar.add(this.saveXLSConsistencyButton);

    this.printConsoleConsistencyButton.setIcon(new ImageIcon(this.getClass().getResource(
        "/ico/console.png"))); // NOI18N
    this.printConsoleConsistencyButton
        .setToolTipText("Print Consistency Control results to console");
    this.printConsoleConsistencyButton.setEnabled(false);
    this.printConsoleConsistencyButton.setFocusable(false);
    this.printConsoleConsistencyButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.printConsoleConsistencyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.printConsoleConsistencyButton.addActionListener(evt -> MainGUI.this
        .printConsoleConsistencyActionPerformed(evt));
    this.simulatorToolbar.add(this.printConsoleConsistencyButton);

    this.simulatorToolbar.add(this.toolbarSeparator3);

    this.exitButton.setIcon(new ImageIcon(this.getClass().getResource("/ico/exit.png")));
    this.exitButton.setToolTipText("Exit");
    this.exitButton.setFocusable(false);
    this.exitButton.setHorizontalTextPosition(SwingConstants.CENTER);
    this.exitButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.exitButton.addActionListener(evt -> MainGUI.this.exitActionPerformed(evt));
    this.simulatorToolbar.add(this.exitButton);

    this.panelsPane.setName("Simulation");

    this.settingsPane.setName("SettingsPanel");
    this.settingsPane.setLayout(null);

    this.cacheCapacityPanel.setBorder(BorderFactory.createTitledBorder("Cache capacities [MB]"));

    this.cacheCapacityList.setFont(new java.awt.Font("Tahoma", 0, 12));
    this.cacheCapacityList.setModel(new CacheCapacityListModel());
    this.cacheCapacityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.cacheCapScrollPane.setViewportView(this.cacheCapacityList);

    this.cacheCapSpinner.setValue(8);

    this.cachePlusButton.setText("+");
    this.cachePlusButton.addActionListener(evt -> MainGUI.this.cachePlusButtonActionPerformed(evt));

    this.cacheMinusButton.setText("-");
    this.cacheMinusButton.addActionListener(evt -> MainGUI.this
        .cacheMinusButtonActionPerformed(evt));

    final GroupLayout cacheCapacityPanelLayout = new GroupLayout(this.cacheCapacityPanel);
    this.cacheCapacityPanel.setLayout(cacheCapacityPanelLayout);
    cacheCapacityPanelLayout.setHorizontalGroup(cacheCapacityPanelLayout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(
            cacheCapacityPanelLayout.createSequentialGroup().addContainerGap()
                .addComponent(this.cacheCapScrollPane).addContainerGap())
        .addGroup(
            GroupLayout.Alignment.TRAILING,
            cacheCapacityPanelLayout
                .createSequentialGroup()
                .addContainerGap(70, Short.MAX_VALUE)
                .addComponent(this.cacheCapSpinner, GroupLayout.PREFERRED_SIZE, 121,
                    GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(this.cachePlusButton)
                .addGap(18, 18, 18)
                .addComponent(this.cacheMinusButton, GroupLayout.PREFERRED_SIZE, 38,
                    GroupLayout.PREFERRED_SIZE).addGap(52, 52, 52)));
    cacheCapacityPanelLayout.setVerticalGroup(cacheCapacityPanelLayout.createParallelGroup(
        GroupLayout.Alignment.LEADING).addGroup(
        cacheCapacityPanelLayout
            .createSequentialGroup()
            .addComponent(this.cacheCapScrollPane, GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addGroup(
                cacheCapacityPanelLayout
                    .createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(this.cacheCapSpinner, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(this.cachePlusButton).addComponent(this.cacheMinusButton))
            .addContainerGap()));

    this.settingsPane.add(this.cacheCapacityPanel);
    this.cacheCapacityPanel.setBounds(369, 11, 372, 270);

    this.othersSettingsPanel.setBorder(BorderFactory.createTitledBorder("Others"));
    this.othersSettingsPanel.setLayout(null);

    this.statLimitLabel.setText("Minimum request count for including result:");
    this.othersSettingsPanel.add(this.statLimitLabel);
    this.statLimitLabel.setBounds(25, 36, 250, 18);

    this.statLimitSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
    this.statLimitSpinner.setValue(30);
    this.statLimitSpinner.addChangeListener(evt -> MainGUI.this.statLimitSpinnerStateChanged(evt));
    this.othersSettingsPanel.add(this.statLimitSpinner);
    this.statLimitSpinner.setBounds(280, 36, 60, 18);

    this.networkSpeedLabel.setText("Average network speed:");
    this.othersSettingsPanel.add(this.networkSpeedLabel);
    this.networkSpeedLabel.setBounds(60, 72, 149, 18);

    this.networkSpeesLabel2.setText("Mbit/s");
    this.othersSettingsPanel.add(this.networkSpeesLabel2);
    this.networkSpeesLabel2.setBounds(310, 72, 40, 18);

    this.networkSpeedSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
    this.networkSpeedSpinner.setValue(80);
    this.networkSpeedSpinner.addChangeListener(evt -> MainGUI.this
        .networkSpeedSpinnerStateChanged(evt));
    this.othersSettingsPanel.add(this.networkSpeedSpinner);
    this.networkSpeedSpinner.setBounds(210, 72, 90, 18);

    this.slidingWindowLabel.setText("Sliding window capacity (%from cache capacity):");
    this.othersSettingsPanel.add(this.slidingWindowLabel);
    this.slidingWindowLabel.setBounds(16, 108, 280, 18);

    this.slidingWindwowSpinner.setFont(new java.awt.Font("Tahoma", 0, 10));
    this.slidingWindwowSpinner.setValue(25);
    this.slidingWindwowSpinner.addChangeListener(evt -> MainGUI.this
        .slidingWindwowSpinnerStateChanged(evt));

    this.othersSettingsPanel.add(this.slidingWindwowSpinner);
    this.slidingWindwowSpinner.setBounds(296, 108, 60, 18);

    this.loadServStat.setSelected(GlobalVariables.isLoadServerStatistic());
    this.loadServStat.setText("Pre-load server statistics");
    this.loadServStat.setFont(new java.awt.Font("Tahoma", 0, 11));
    this.loadServStat.addActionListener(evt -> MainGUI.this.loadServStatActionPerformed(evt));
    this.othersSettingsPanel.add(this.loadServStat);
    this.loadServStat.setBounds(120, 144, 160, 18);

    this.settingsPane.add(this.othersSettingsPanel);
    this.othersSettingsPanel.setBounds(370, 290, 370, 180);

    this.requestsSettingsPanel.setBorder(BorderFactory.createTitledBorder("Requests"));
    this.requestsSettingsPanel.setLayout(null);

    this.requestsInputComboBox.setModel(new DefaultComboBoxModel(new String[] {"-- Choose one --",
        "From AFS log file", "Gaussian random", "Uniformly Random", "Random with preference",
        "Zipf random"}));
    this.requestsInputComboBox.addItemListener(evt -> MainGUI.this
        .requestsInputComboBoxItemStateChanged(evt));
    this.requestsSettingsPanel.add(this.requestsInputComboBox);
    this.requestsInputComboBox.setBounds(180, 30, 156, 22);

    this.inputRequestLabel.setText("Requests input method:");
    this.requestsSettingsPanel.add(this.inputRequestLabel);
    this.inputRequestLabel.setBounds(30, 30, 173, 20);

    this.randomFileSizeCheckBox.setText("Generate random file sizes");
    this.randomFileSizeCheckBox.addItemListener(evt -> MainGUI.this
        .randomFileSizeCheckBoxItemStateChanged(evt));
    this.randomFileSizeCheckBox.addActionListener(evt -> MainGUI.this
        .randomFileSizeCheckBoxActionPerformed(evt));
    this.requestsSettingsPanel.add(this.randomFileSizeCheckBox);
    this.randomFileSizeCheckBox.setBounds(80, 350, 190, 23);

    this.simConsistencyCheck.setText("Simulate Consistency");
    this.simConsistencyCheck.setSelected(false);
    this.simConsistencyCheck.addItemListener(evt -> MainGUI.this.simConsistencyCheckedChanged(evt));
    this.requestsSettingsPanel.add(this.simConsistencyCheck);
    this.simConsistencyCheck.setBounds(110, 140, 180, 14);

    this.minFileSizeLabel.setText("Minimum generated file size: ");
    this.requestsSettingsPanel.add(this.minFileSizeLabel);
    this.minFileSizeLabel.setBounds(10, 380, 180, 14);

    this.maxFileSizeLabel.setText("Maximum generated file size:");
    this.requestsSettingsPanel.add(this.maxFileSizeLabel);
    this.maxFileSizeLabel.setBounds(10, 410, 180, 20);

    this.unitsLabel2.setText("KBytes");
    this.requestsSettingsPanel.add(this.unitsLabel2);
    this.unitsLabel2.setBounds(282, 413, 71, 14);

    this.unitsLabel1.setText("KBytes");
    this.requestsSettingsPanel.add(this.unitsLabel1);
    this.unitsLabel1.setBounds(282, 383, 71, 14);

    this.minGenFileSizejSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.minGenFileSizejSpinner.setValue(500);
    this.minGenFileSizejSpinner.addChangeListener(evt -> MainGUI.this
        .minGenFileSizejSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.minGenFileSizejSpinner);
    this.minGenFileSizejSpinner.setBounds(190, 381, 88, 18);

    this.maxGenFileSizejSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.maxGenFileSizejSpinner.setValue(32000);
    this.maxGenFileSizejSpinner.addChangeListener(evt -> MainGUI.this
        .maxGenFileSizejSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.maxGenFileSizejSpinner);
    this.maxGenFileSizejSpinner.setBounds(190, 411, 88, 18);

    this.pathTextField.setEditable(false);
    this.pathTextField.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent evt) {
        MainGUI.this.pathTextFieldMouseClicked(evt);
      }
    });
    this.requestsSettingsPanel.add(this.pathTextField);
    this.pathTextField.setBounds(60, 80, 270, 30);

    this.pathLabel.setText("Path:");
    this.requestsSettingsPanel.add(this.pathLabel);
    this.pathLabel.setBounds(16, 88, 50, 14);

    this.generateFileSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.generateFileSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
    this.generateFileSpinner.addChangeListener(evt -> MainGUI.this
        .generateFileSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.generateFileSpinner);
    this.generateFileSpinner.setBounds(220, 120, 80, 20);

    this.generateFileLabel.setText("Generate files:");
    this.requestsSettingsPanel.add(this.generateFileLabel);
    this.generateFileLabel.setBounds(40, 130, 170, 10);

    this.meanValueGaussLabel.setText("Mean value (Gauss):");
    this.requestsSettingsPanel.add(this.meanValueGaussLabel);
    this.meanValueGaussLabel.setBounds(40, 160, 160, 10);

    this.maenValueGaussSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.maenValueGaussSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
    this.maenValueGaussSpinner.addChangeListener(evt -> MainGUI.this
        .maenValueGaussSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.maenValueGaussSpinner);
    this.maenValueGaussSpinner.setBounds(220, 150, 80, 20);

    this.preferenceDivisibleLabel.setText("Prefenced file's ID divisible by:");
    this.requestsSettingsPanel.add(this.preferenceDivisibleLabel);
    this.preferenceDivisibleLabel.setBounds(20, 190, 180, 20);

    this.preferenceDivisibleSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.preferenceDivisibleSpinner.addChangeListener(evt -> MainGUI.this
        .preferenceDivisibleSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.preferenceDivisibleSpinner);
    this.preferenceDivisibleSpinner.setBounds(220, 190, 80, 20);

    this.stepPrefLabel.setText("Step for generate preferenced file:");
    this.requestsSettingsPanel.add(this.stepPrefLabel);
    this.stepPrefLabel.setBounds(20, 240, 190, 10);

    this.stepPrefSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.stepPrefSpinner.addChangeListener(evt -> MainGUI.this.stepPrefSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.stepPrefSpinner);
    this.stepPrefSpinner.setBounds(220, 240, 80, 20);

    this.nonPrefLabel.setText("Non preferenced file's ID divisible by:");
    this.requestsSettingsPanel.add(this.nonPrefLabel);
    this.nonPrefLabel.setBounds(20, 280, 190, 10);

    this.nonPrefSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.nonPrefSpinner.addChangeListener(evt -> MainGUI.this.nonPrefSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.nonPrefSpinner);
    this.nonPrefSpinner.setBounds(219, 280, 80, 20);

    this.dispersionLabel.setText("Dispersion:");
    this.requestsSettingsPanel.add(this.dispersionLabel);
    this.dispersionLabel.setBounds(40, 190, 170, 14);

    this.dispersionSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.dispersionSpinner
        .addChangeListener(evt -> MainGUI.this.dispersionSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.dispersionSpinner);
    this.dispersionSpinner.setBounds(220, 190, 80, 18);

    this.requestsCountLabel.setText("Requests count:");
    this.requestsSettingsPanel.add(this.requestsCountLabel);
    this.requestsCountLabel.setBounds(40, 100, 170, 14);

    this.requestCountSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.requestCountSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
    this.requestCountSpinner.addChangeListener(evt -> MainGUI.this
        .requestCountSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.requestCountSpinner);
    this.requestCountSpinner.setBounds(220, 90, 80, 20);

    this.zipfLambdaLabel.setText("Zipf generator alfa:");
    this.requestsSettingsPanel.add(this.zipfLambdaLabel);
    this.zipfLambdaLabel.setBounds(40, 190, 150, 14);

    this.zipfLamdbaSpinner.setModel(zipfModel);
    this.zipfLamdbaSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.zipfLamdbaSpinner
        .addChangeListener(evt -> MainGUI.this.zipfLamdbaSpinnerStateChanged(evt));
    this.requestsSettingsPanel.add(this.zipfLamdbaSpinner);
    this.zipfLamdbaSpinner.setBounds(220, 190, 80, 18);

    this.settingsPane.add(this.requestsSettingsPanel);
    this.requestsSettingsPanel.setBounds(10, 10, 353, 456);

    this.panelsPane.addTab("Simulation Settings", this.settingsPane);

    this.cachePane.setBorder(BorderFactory.createEtchedBorder());
    this.cachePane.setName("CacheAlgPanel"); // NOI18N

    this.cachePanel.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
    this.cachePanel.setLayout(new BoxLayout(this.cachePanel, BoxLayout.Y_AXIS));

    this.cachePane.setLeftComponent(this.cachePanel);

    this.consistencyPanel.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
    this.consistencyPanel.setLayout(new BoxLayout(this.consistencyPanel, BoxLayout.Y_AXIS));
    this.ConsistencyControlPane.setLeftComponent(this.consistencyPanel);

    this.noSettingsPanelCache.setLayout(null);

    this.noSettingsLabelCache.setText("This cache policy does not have any settings!");
    this.noSettingsPanelCache.add(this.noSettingsLabelCache);
    this.noSettingsLabelCache.setBounds(110, 60, 410, 14);

    this.cachePane.setRightComponent(this.noSettingsPanelCache);

    this.noSettingsPanelConsistency.setLayout(null);
    this.noSettingsLabelConsistency.setText("This consistency control does not have any settings!");
    this.noSettingsPanelConsistency.add(this.noSettingsLabelConsistency);
    this.noSettingsLabelConsistency.setBounds(110, 60, 410, 14);

    this.ConsistencyControlPane.setRightComponent(this.noSettingsPanelConsistency);

    this.panelsPane.addTab("Cache Policies", this.cachePane);

    this.panelsPane.addTab("Consistency Control", this.ConsistencyControlPane);
    this.panelsPane.setEnabledAt(2, false);

    this.resultsPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    this.resultsPane.setName("ResultsPanel"); // NOI18N
    this.resultsPane.setLayout(null);

    this.userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.userList.addListSelectionListener(evt -> MainGUI.this.userListValueChanged(evt));
    this.userListScrollPane.setViewportView(this.userList);

    this.resultsPane.add(this.userListScrollPane);
    this.userListScrollPane.setBounds(11, 51, 136, 256);

    this.userLabel.setText("Simulated user");
    this.resultsPane.add(this.userLabel);
    this.userLabel.setBounds(37, 30, 110, 14);

    this.resultsChangeCombo.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.resultsChangeCombo.setModel(new DefaultComboBoxModel(new String[] {"Read Hit Ratio [%]",
        "Read Hit Count", "Saved Bytes Ratio [%]", "Saved Bytes [MB]",
        "Data Transfer Degrease Ratio [%]", "Data Transfer Degrease [MB]"}));
    this.resultsChangeCombo.setMaximumSize(new java.awt.Dimension(192, 22));
    this.resultsChangeCombo.addActionListener(evt -> MainGUI.this
        .resultsChangeComboActionPerformed(evt));
    this.resultsPane.add(this.resultsChangeCombo);
    this.resultsChangeCombo.setBounds(11, 338, 140, 21);

    this.resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    this.resultsTable.setEnabled(false);
    this.resultsTable.setRowSelectionAllowed(false);
    this.resultsTableScrollPane.setViewportView(this.resultsTable);

    this.resultsPane.add(this.resultsTableScrollPane);
    this.resultsTableScrollPane.setBounds(165, 51, 573, 411);

    this.totalReqLabel.setText("Total requested files:");
    this.resultsPane.add(this.totalReqLabel);
    this.totalReqLabel.setBounds(140, 16, 116, 14);

    this.totalReqTextField.setEditable(false);
    this.totalReqTextField.setFont(new java.awt.Font("Tahoma", 0, 10));

    this.resultsPane.add(this.totalReqTextField);
    this.totalReqTextField.setBounds(260, 13, 91, 19);

    this.totalNetLabel.setText("Total network traffic (without cache):");
    this.resultsPane.add(this.totalNetLabel);
    this.totalNetLabel.setBounds(389, 16, 200, 14);

    this.totalNetTextField.setEditable(false);
    this.totalNetTextField.setFont(new java.awt.Font("Tahoma", 0, 10));

    this.resultsPane.add(this.totalNetTextField);
    this.totalNetTextField.setBounds(597, 13, 80, 19);

    this.unitsLabel3.setText("MB");
    this.resultsPane.add(this.unitsLabel3);
    this.unitsLabel3.setBounds(690, 16, 40, 14);

    this.chooseResultsLabel.setText("Choose results");
    this.resultsPane.add(this.chooseResultsLabel);
    this.chooseResultsLabel.setBounds(40, 320, 120, 14);

    this.barChartButton.setText("Bar Chart");
    this.barChartButton.addActionListener(evt -> MainGUI.this.barChartButtonActionPerformed(evt));
    this.resultsPane.add(this.barChartButton);
    this.barChartButton.setBounds(10, 390, 136, 23);

    this.lineChartButton.setText("Line Chart");
    this.lineChartButton.addActionListener(evt -> MainGUI.this.lineChartButtonActionPerformed(evt));
    this.resultsPane.add(this.lineChartButton);
    this.lineChartButton.setBounds(10, 430, 136, 23);

    this.panelsPane.addTab("Policies Results", this.resultsPane);

    this.ConReultsPanel.setName("ResultsPanel");
    this.ConReultsPanel.setLayout(null);

    this.tablePane.setViewportView(this.consistencyTable);

    this.ConReultsPanel.add(this.tablePane);
    this.tablePane.setBounds(180, 11, 560, 460);

    this.userListConsistency.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.userListConScroll.setViewportView(this.userListConsistency);

    this.userListConsistency.addListSelectionListener(evt -> MainGUI.this
        .userListConsistencyValueChanged(evt));

    this.ConReultsPanel.add(this.userListConScroll);
    this.userListConScroll.setBounds(19, 43, 140, 192);

    this.userListLabel.setText("User List");
    this.ConReultsPanel.add(this.userListLabel);
    this.userListLabel.setBounds(63, 21, 65, 14);

    this.cachePolLabel.setText("Caching Policy");
    this.ConReultsPanel.add(this.cachePolLabel);
    this.cachePolLabel.setBounds(50, 261, 86, 14);

    this.cachePolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.cachePolList.addListSelectionListener(evt -> MainGUI.this.cachePolListValueChanged(evt));
    this.cachePolScroll.setViewportView(this.cachePolList);

    this.ConReultsPanel.add(this.cachePolScroll);
    this.cachePolScroll.setBounds(19, 285, 140, 186);

    this.panelsPane.addTab("Consistency Results", this.ConReultsPanel);

    this.statusPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));

    final GroupLayout statusPanelLayout = new GroupLayout(this.statusPanel);
    this.statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(statusPanelLayout.createParallelGroup(
        GroupLayout.Alignment.LEADING).addComponent(this.simulationProgressBar,
        GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE));
    statusPanelLayout.setVerticalGroup(statusPanelLayout.createParallelGroup(
        GroupLayout.Alignment.LEADING).addGroup(
        GroupLayout.Alignment.TRAILING,
        statusPanelLayout
            .createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(this.simulationProgressBar, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));

    this.fileMenu.setMnemonic('F');
    this.fileMenu.setText("File");

    this.saveCSVMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
    this.saveCSVMenuItem.setMnemonic('C');
    this.saveCSVMenuItem.setText("Save Policies Results to CSV");
    this.saveCSVMenuItem.setToolTipText("Saves tables with caching policies results to CSV file");
    this.saveCSVMenuItem.setEnabled(false);
    this.saveCSVMenuItem.addActionListener(evt -> MainGUI.this.saveCSVActionPerformed(evt));
    this.fileMenu.add(this.saveCSVMenuItem);

    this.saveXLSMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
    this.saveXLSMenuItem.setMnemonic('L');
    this.saveXLSMenuItem.setText("Save Policies Results to XLS");
    this.saveXLSMenuItem.setToolTipText("Saves tables with caching policies results to XLS file");
    this.saveXLSMenuItem.setEnabled(false);
    this.saveXLSMenuItem.addActionListener(evt -> MainGUI.this.saveXLSActionPerformed(evt));
    this.fileMenu.add(this.saveXLSMenuItem);

    this.saveConsoleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
    this.saveConsoleMenuItem.setMnemonic('P');
    this.saveConsoleMenuItem.setText("Print Policies Results to Console");
    this.saveConsoleMenuItem.setEnabled(false);
    this.saveConsoleMenuItem
        .addActionListener(evt -> MainGUI.this.printConsoleActionPerformed(evt));
    this.fileMenu.add(this.saveConsoleMenuItem);
    this.fileMenu.add(this.fileMenuSeparator);

    this.saveCSVConsistencyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
    this.saveCSVConsistencyMenuItem.setMnemonic('A');
    this.saveCSVConsistencyMenuItem.setText("Save Consistency Results to CSV");
    this.saveCSVConsistencyMenuItem
        .setToolTipText("Saves tables with consistency control results to CSV file");
    this.saveCSVConsistencyMenuItem.setEnabled(false);
    this.saveCSVConsistencyMenuItem.addActionListener(evt -> MainGUI.this
        .saveCSVConsistencyActionPerformed(evt));
    this.fileMenu.add(this.saveCSVConsistencyMenuItem);

    this.saveXLSConsistencyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
    this.saveXLSConsistencyMenuItem.setMnemonic('S');
    this.saveXLSConsistencyMenuItem.setText("Save Consistency Results to XLS");
    this.saveXLSConsistencyMenuItem
        .setToolTipText("Saves tables with consistency control results to XLS file");
    this.saveXLSConsistencyMenuItem.setEnabled(false);
    this.saveXLSConsistencyMenuItem.addActionListener(evt -> MainGUI.this
        .saveXLSConsistencyActionPerformed(evt));
    this.fileMenu.add(this.saveXLSConsistencyMenuItem);

    this.saveConsoleConsistencyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
    this.saveConsoleConsistencyMenuItem.setMnemonic('D');
    this.saveConsoleConsistencyMenuItem.setText("Print Consistency Results to Console");
    this.saveConsoleConsistencyMenuItem
        .setToolTipText("Print tables with consistency control results to console");
    this.saveConsoleConsistencyMenuItem.setEnabled(false);
    this.saveConsoleConsistencyMenuItem.addActionListener(evt -> MainGUI.this
        .printConsoleConsistencyActionPerformed(evt));
    this.fileMenu.add(this.saveConsoleConsistencyMenuItem);

    this.fileMenu.add(this.fileMenuSeparator2);

    this.exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));
    this.exitMenuItem.setMnemonic('X');
    this.exitMenuItem.setText("Exit");
    this.exitMenuItem.setToolTipText("Terminate simulator");
    this.exitMenuItem.addActionListener(evt -> MainGUI.this.exitActionPerformed(evt));
    this.fileMenu.add(this.exitMenuItem);

    this.menuBar.add(this.fileMenu);

    this.simulationMenu.setMnemonic('S');
    this.simulationMenu.setText("Simulation");

    this.requestMenu.setMnemonic('R');
    this.requestMenu.setText("Request input method");
    this.requestMenu.setToolTipText("Change requests input method");

    this.inputAFSMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0));
    this.inputAFSMenuItem.setMnemonic('F');
    this.inputAFSMenuItem.setText("From AFS log file");
    this.inputAFSMenuItem.setToolTipText("Requests to the files are from AFS log file");
    this.inputAFSMenuItem.addActionListener(evt -> MainGUI.this
        .inputAFSMenuItemActionPerformed(evt));
    this.requestMenu.add(this.inputAFSMenuItem);

    this.inputGaussianMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
    this.inputGaussianMenuItem.setMnemonic('G');
    this.inputGaussianMenuItem.setText("Gaussian random");
    this.inputGaussianMenuItem
        .setToolTipText("Requests to the files are from Gaussian distribution");
    this.inputGaussianMenuItem.addActionListener(evt -> MainGUI.this
        .inputGaussianMenuItemActionPerformed(evt));
    this.requestMenu.add(this.inputGaussianMenuItem);

    this.inputUniformlyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
    this.inputUniformlyMenuItem.setMnemonic('R');
    this.inputUniformlyMenuItem.setText("Uniformly random");
    this.inputUniformlyMenuItem
        .setToolTipText("Requests to the files are from uniformly random distribution");
    this.inputUniformlyMenuItem.addActionListener(evt -> MainGUI.this
        .inputUniformlyMenuItemActionPerformed(evt));
    this.requestMenu.add(this.inputUniformlyMenuItem);

    this.inputRandomPrefMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
    this.inputRandomPrefMenuItem.setMnemonic('P');
    this.inputRandomPrefMenuItem.setText("Random with preferences");
    this.inputRandomPrefMenuItem
        .setToolTipText("Requests to the files are from random distribution with preferention of files");
    this.inputRandomPrefMenuItem.addActionListener(evt -> MainGUI.this
        .inputRandomPrefMenuItemActionPerformed(evt));
    this.requestMenu.add(this.inputRandomPrefMenuItem);

    this.inputZipfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));
    this.inputZipfMenuItem.setMnemonic('Z');
    this.inputZipfMenuItem.setText("Zipf random");
    this.inputZipfMenuItem.setToolTipText("Requests to the files are from Zipf distribution");
    this.inputZipfMenuItem.addActionListener(evt -> MainGUI.this
        .inputZipfMenuItemActionPerformed(evt));
    this.requestMenu.add(this.inputZipfMenuItem);

    this.simulationMenu.add(this.requestMenu);

    this.simulateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
    this.simulateMenuItem.setMnemonic('I');
    this.simulateMenuItem.setText("Simulate");
    this.simulateMenuItem.setToolTipText("Run simulation");
    this.simulateMenuItem.addActionListener(evt -> MainGUI.this.simulateActionPerformed(evt));
    this.simulationMenu.add(this.simulateMenuItem);

    this.menuBar.add(this.simulationMenu);

    this.helpMenu.setMnemonic('H');
    this.helpMenu.setText("Help");

    this.aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
    this.aboutMenuItem.setMnemonic('A');
    this.aboutMenuItem.setText("About");
    this.aboutMenuItem.addActionListener(evt -> MainGUI.this.aboutMenuItemActionPerformed(evt));
    this.helpMenu.add(this.aboutMenuItem);

    this.menuBar.add(this.helpMenu);

    this.setJMenuBar(this.menuBar);

    final GroupLayout layout = new GroupLayout(this.getContentPane());
    this.getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(this.simulatorToolbar, GroupLayout.DEFAULT_SIZE, 754, Short.MAX_VALUE)
        .addComponent(this.panelsPane)
        .addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
                this.statusPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    layout.setVerticalGroup(layout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(
            layout
                .createSequentialGroup()
                .addComponent(this.simulatorToolbar, GroupLayout.PREFERRED_SIZE, 40,
                    GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(this.panelsPane, GroupLayout.PREFERRED_SIZE, 499,
                    GroupLayout.PREFERRED_SIZE).addContainerGap(27, Short.MAX_VALUE))
        .addGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
                GroupLayout.Alignment.TRAILING,
                layout
                    .createSequentialGroup()
                    .addGap(0, 550, Short.MAX_VALUE)
                    .addComponent(this.statusPanel, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));

    this.panelsPane.setEnabledAt(3, false);
    this.panelsPane.setEnabledAt(4, false);
    this.pack();
  }
}
