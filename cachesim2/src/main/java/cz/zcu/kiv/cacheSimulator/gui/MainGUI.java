package cz.zcu.kiv.cacheSimulator.gui;


import cz.zcu.kiv.cacheSimulator.dataAccess.GaussianFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.dataAccess.RandomFileNameGeneratorWithPrefences;
import cz.zcu.kiv.cacheSimulator.dataAccess.ZipfFileNameGenerator;
import cz.zcu.kiv.cacheSimulator.output.Output;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.CacheLoader;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.AccessSimulation;
import cz.zcu.kiv.cacheSimulator.simulation.UserStatistics;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Pavel Bzoch trida pro zobrazeni hlavniho gui aplikace
 */
@SuppressWarnings("serial")
public class MainGUI extends javax.swing.JFrame implements Observer {

  public static final String ALERT = "Alert";
  public static final String CHOOSE_ONE = "-- Choose one --";
  /**
   * singleton instance
   */
  private static MainGUI gui = null;

  @Override
  public void update(final Observable arg0, final Object arg1) {
    this.simulationProgressBar.setValue((Integer) arg1);
    this.simulationProgressBar.setString(String.format("Simulation in progress... %s %%", String.valueOf(arg1)));
  }

  /**
   * promenna pro uchovani buttonu pro nastaveni cache
   */
  private ArrayList<JCheckBox> cacheCheckBoxes = null;

  /**
   * promenna pro uchovani vysledku mereni cache
   */
  private List<UserStatistics> cacheResults = null;

  /**
   * promenna pro uchovani odkazu na simulacni vlakno pro pripadne preruseni
   * simulace
   */
  private Thread simulationThread = null;

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
    this.randomFileSizeCheckBox.setEnabled(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.meanValueGaussLabel.setVisible(false);
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefLabelSpinner.setVisible(false);
    this.pathLabel.setVisible(true);
    this.pathTextField.setVisible(true);
    this.preferenceDivisibleLabel.setVisible(false);
    this.preferenceDivisibleSpinner.setVisible(false);
    this.randomFileSizeCheckBox.setVisible(true);
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
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefLabelSpinner.setVisible(false);
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
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(true);
    this.meanValueGaussLabel.setVisible(true);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefLabelSpinner.setVisible(false);
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
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(true);
    this.nonPrefLabelSpinner.setVisible(true);
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
    this.maenValueGaussSpinner.setVisible(false);
    this.maxFileSizeLabel.setVisible(false);
    this.maxGenFileSizejSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.minFileSizeLabel.setVisible(false);
    this.minGenFileSizejSpinner.setVisible(false);
    this.nonPrefLabel.setVisible(false);
    this.nonPrefLabelSpinner.setVisible(false);
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
    this.randomFileSizeCheckBox.setEnabled(false);
    this.randomFileSizeCheckBox.setSelected(true);
    this.maenValueGaussSpinner.setVisible(false);
    this.meanValueGaussLabel.setVisible(false);
    this.maxFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.maxGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minFileSizeLabel.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.minGenFileSizejSpinner.setVisible(this.randomFileSizeCheckBox.isSelected());
    this.nonPrefLabel.setVisible(false);
    this.nonPrefLabelSpinner.setVisible(false);
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
   * metoda nahraje z global variables nastaveni
   */
  private void loadValuesFromGlobalVar() {
    this.slidingWindwowSpinner.setValue((int) (GlobalVariables
      .getCacheCapacityForDownloadWindow() * 100));
    this.networkSpeedSpinner.setValue(GlobalVariables.getAverageNetworkSpeed());
    this.statLimitSpinner.setValue(GlobalVariables.getLimitForStatistics());
    this.maxGenFileSizejSpinner.setValue(GlobalVariables
      .getMaxGeneratedFileSize());
    this.minGenFileSizejSpinner.setValue(GlobalVariables
      .getMinGeneratedFileSize());
    this.nonPrefLabelSpinner.setValue(GlobalVariables
      .getFileRequestnNonPreferenceFile());
    this.stepPrefSpinner
      .setValue(GlobalVariables.getFileRequestPreferenceStep());
    this.preferenceDivisibleSpinner.setValue(GlobalVariables
      .getFileRequestPreferenceFile());
    this.dispersionSpinner.setValue(GlobalVariables
      .getFileRequestGeneratorDispersion());
    this.maenValueGaussSpinner.setValue(GlobalVariables
      .getFileRequestGeneratorMeanValue());
    this.generateFileSpinner.setValue(GlobalVariables
      .getFileRequestGeneratorMaxValue());
    this.requestCountSpinner.setValue(GlobalVariables
      .getRequestCountForRandomGenerator());
  }

  /**
   * metoda pro nahrani zaskrtavatek pro cache algoritmy
   */
  private void loadCaches() {
    this.cacheCheckBoxes = new ArrayList<>();
    final String path = MainGUI.class.getProtectionDomain().getCodeSource()
      .getLocation().getPath();
    if (path.endsWith(".jar")) {
      loadCachesFromJar(Paths.get(path));
    } else {
      loadCaches(Paths.get(path));
    }
    this.cachePanel.invalidate();
    this.cachePanel.repaint();
  }

  /**
   * metoda pro nacteni cache simulatoru z jar souboru
   *
   * @param path cesta k jar souboru
   */
  private void loadCachesFromJar(final Path path) {
    final var cacheInfos = CacheLoader.loadCachesFromJar(path);
    for (final var cacheInfo : cacheInfos) {
      bindCacheToCheckbox(cacheInfo);
    }
  }

  /**
   * metoda pro nacteni cache policies z adresare
   *
   * @param path cesta
   */
  private void loadCaches(final Path path) {
    final var cacheInfos = CacheLoader.loadCaches(path);
    for (final String cacheInfo : cacheInfos) {
      bindCacheToCheckbox(cacheInfo);
    }
  }

  private void bindCacheToCheckbox(final String cacheInfo) {
    final String[] names = cacheInfo.split(";");
    final JCheckBox novy = new JCheckBox(names[1], false);
    novy.setName(names[0]);
    novy.addActionListener(this::cacheCheckBoxActionPerformed);
    novy.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

      @Override
      public void mouseMoved(final MouseEvent e) {
        cacheCheckBoxMouseMove(e);
      }
    });
    novy.setSelected(true);
    this.cachePanel.add(novy);
    this.cacheCheckBoxes.add(novy);
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
      GlobalVariables.setLogAFSFIleName(fc.getSelectedFile()
        .getAbsolutePath());
      GlobalVariables.setActDir(fc.getSelectedFile().getPath());
    } else {
      JOptionPane
        .showMessageDialog(
          this,
          "You have to choose input file before starting simulation!\nYou can choose input file by clicking Path text area",
          ALERT, JOptionPane.OK_OPTION);
    }
  }

  /**
   * Creates new form MainGUI
   */
  private MainGUI() {
    initComponents();
    hideAllInput();
    loadCaches();
    loadValuesFromGlobalVar();
    centerOnScreen();
    this.simulationProgressBar.setVisible(false);
    this.panelsPane.setEnabledAt(2, false);
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
   * Metoda pro test chyb v nastaveni
   *
   * @return true, pokud vse v poradku
   */
  private boolean checkSettings() {
    // kontrola vyberu vstupni metody
    if (this.requestsInputComboBox.getSelectedItem().equals(CHOOSE_ONE)) {
      JOptionPane.showMessageDialog(this,
        "You have to select input request method!", ALERT,
        JOptionPane.ERROR_MESSAGE);
      this.panelsPane.setSelectedIndex(0);
      return false;
    } else if (this.requestsInputComboBox.getSelectedItem().equals(
      "From AFS log file")) {
      final File f = new File(GlobalVariables.getLogAFSFIleName());
      if (!f.isFile()) {
        JOptionPane.showMessageDialog(this,
          "You have to select AFS log file!", ALERT,
          JOptionPane.ERROR_MESSAGE);
        this.panelsPane.setSelectedIndex(0);
        return false;
      }
    }

    // kontrola zaskrtavatek u cache algoritmu
    if (getCachesNames() == null || getCachesNames().length == 0) {
      JOptionPane.showMessageDialog(this,
        "You have to select simulated cache algorithms!", ALERT,
        JOptionPane.ERROR_MESSAGE);
      this.panelsPane.setSelectedIndex(1);
      return false;
    }
    return true;
  }

  /**
   * metoda pro nacteni vysledku do tabulky
   */
  private void loadResultsToTable() {
    if (this.cacheResults == null || this.cacheResults.isEmpty()) {
      return;
    }
    if (this.userList.getSelectedIndex() < 0) {
      return;
    }
    this.totalNetTextField
      .setText(Long.toString(this.cacheResults.get(
        this.userList.getSelectedIndex()).getTotalNetworkBandwidth() / 1024 / 1024));
    this.totalReqTextField.setText(Long.toString(this.cacheResults.get(
      this.userList.getSelectedIndex()).getFileAccessed()));
    final UserStatistics stat = this.cacheResults.get(this.userList.getSelectedIndex());

    final String[] cacheNames = stat.getCacheNames();

    final String[] names = new String[getCacheSizes().length + 1];
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

    final TableModel tm = new DefaultTableModel(rowData, names);
    this.resultsTable.setModel(tm);
    final RowSorter<TableModel> sorter = new TableRowSorter<>(tm);
    this.resultsTable.setRowSorter(sorter);
  }

  /**
   * trida pro prezentaci modelu pro pole velikosti
   */
  @SuppressWarnings("rawtypes")
  private class CapacityAbstractModel extends javax.swing.AbstractListModel {
    Integer[] array;

    @Override
    public int getSize() {
      return this.array.length;
    }

    @Override
    public Object getElementAt(final int i) {
      return this.array[i];
    }

    /**
     * metoda pro odebrani polozky
     *
     * @param index index polozky
     */
    public void remove(final int index) {
      if (index < 0 || index >= this.array.length) {
        return;
      }
      final Integer[] newAarray = new Integer[this.array.length - 1];
      for (int i = 0; i < this.array.length; i++) {
        if (i < index) {
          newAarray[i] = this.array[i];
        } else if (i == index) {
        } else {
          newAarray[i - 1] = this.array[i];
        }
      }
      this.array = newAarray;
    }

    CapacityAbstractModel() {
      this.array = new Integer[]{8, 16, 32, 64, 128, 256, 512, 1024};
    }

    CapacityAbstractModel(final Integer[] array) {
      this.array = array;
    }

    /**
     * metoda pro ziskani pole velikosti
     *
     * @return pole velikosti
     */
    Integer[] getArray() {
      return this.array;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  // <editor-fold defaultstate="collapsed"
  // desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    this.simulatorToolbar = new javax.swing.JToolBar();
    this.simulateButton = new javax.swing.JButton();
    this.simCancelButton = new javax.swing.JButton();
    this.jSeparator1 = new javax.swing.JToolBar.Separator();
    this.saveCSVButton = new javax.swing.JButton();
    this.saveXLSButton = new javax.swing.JButton();
    this.printConsoleButton = new javax.swing.JButton();
    this.jSeparator2 = new javax.swing.JToolBar.Separator();
    this.exitButton = new javax.swing.JButton();
    this.panelsPane = new javax.swing.JTabbedPane();
    this.settingsPane = new javax.swing.JPanel();
    this.cacheCapacityPanel = new javax.swing.JPanel();
    this.jScrollPane1 = new javax.swing.JScrollPane();
    this.cacheCapacityList = new javax.swing.JList();
    this.cacheCapSpinner = new javax.swing.JSpinner();
    this.cachePlusButton = new javax.swing.JButton();
    this.cacheMinusButton = new javax.swing.JButton();
    this.othersSettingsPanel = new javax.swing.JPanel();
    this.statLimitLabel = new javax.swing.JLabel();
    this.statLimitSpinner = new javax.swing.JSpinner();
    this.networkSpeedLabel = new javax.swing.JLabel();
    this.networkSpeesLabel2 = new javax.swing.JLabel();
    this.networkSpeedSpinner = new javax.swing.JSpinner();
    this.slidingWindowLabel = new javax.swing.JLabel();
    this.slidingWindwowSpinner = new javax.swing.JSpinner();
    this.requestsSettingsPanel = new javax.swing.JPanel();
    this.requestsInputComboBox = new javax.swing.JComboBox();
    this.inputRequestLabel = new javax.swing.JLabel();
    this.randomFileSizeCheckBox = new javax.swing.JCheckBox();
    this.minFileSizeLabel = new javax.swing.JLabel();
    this.maxFileSizeLabel = new javax.swing.JLabel();
    this.unitsLabel2 = new javax.swing.JLabel();
    this.unitsLabel1 = new javax.swing.JLabel();
    this.minGenFileSizejSpinner = new javax.swing.JSpinner();
    this.maxGenFileSizejSpinner = new javax.swing.JSpinner();
    this.pathTextField = new javax.swing.JTextField();
    this.pathLabel = new javax.swing.JLabel();
    this.generateFileSpinner = new javax.swing.JSpinner();
    this.generateFileLabel = new javax.swing.JLabel();
    this.meanValueGaussLabel = new javax.swing.JLabel();
    this.maenValueGaussSpinner = new javax.swing.JSpinner();
    this.preferenceDivisibleLabel = new javax.swing.JLabel();
    this.preferenceDivisibleSpinner = new javax.swing.JSpinner();
    this.stepPrefLabel = new javax.swing.JLabel();
    this.stepPrefSpinner = new javax.swing.JSpinner();
    this.nonPrefLabel = new javax.swing.JLabel();
    this.nonPrefLabelSpinner = new javax.swing.JSpinner();
    this.dispersionLabel = new javax.swing.JLabel();
    this.dispersionSpinner = new javax.swing.JSpinner();
    this.requestsCountLabel = new javax.swing.JLabel();
    this.requestCountSpinner = new javax.swing.JSpinner();
    this.zipfLambdaLabel = new javax.swing.JLabel();
    final SpinnerNumberModel zipfModel = new SpinnerNumberModel(0.75, 0.01, 10,
      0.01);
    this.zipfLamdbaSpinner = new javax.swing.JSpinner();
    this.cachePane = new javax.swing.JSplitPane();
    this.cachePanel = new javax.swing.JPanel();
    this.noSettingsPanel = new javax.swing.JPanel();
    this.noSettingsLabel = new javax.swing.JLabel();
    this.resultsPane = new javax.swing.JPanel();
    this.jScrollPane2 = new javax.swing.JScrollPane();
    this.userList = new javax.swing.JList();
    this.userLabel = new javax.swing.JLabel();
    this.resultsChangeCombo = new javax.swing.JComboBox();
    this.jScrollPane3 = new javax.swing.JScrollPane();
    this.resultsTable = new javax.swing.JTable();
    this.totalReqLabel = new javax.swing.JLabel();
    this.totalReqTextField = new javax.swing.JTextField();
    this.totalNetLabel = new javax.swing.JLabel();
    this.totalNetTextField = new javax.swing.JTextField();
    this.jLabel1 = new javax.swing.JLabel();
    this.chooseResultsLabel = new javax.swing.JLabel();
    this.barChartButton = new javax.swing.JButton();
    this.lineChartButton = new javax.swing.JButton();
    this.statusPanel = new javax.swing.JPanel();
    this.simulationProgressBar = new javax.swing.JProgressBar();
    this.menuBar = new javax.swing.JMenuBar();
    this.fileMenu = new javax.swing.JMenu();
    this.saveCSVMenuItem = new javax.swing.JMenuItem();
    this.saveXLSMenuItem = new javax.swing.JMenuItem();
    this.saveConsoleMenuItem = new javax.swing.JMenuItem();
    this.fileMenuSeparator = new javax.swing.JPopupMenu.Separator();
    this.exitMenuItem = new javax.swing.JMenuItem();
    this.simulationMenu = new javax.swing.JMenu();
    this.requestMenu = new javax.swing.JMenu();
    this.inputAFSMenuItem = new javax.swing.JMenuItem();
    this.inputGaussianMenuItem = new javax.swing.JMenuItem();
    this.inputUniformlyMenuItem = new javax.swing.JMenuItem();
    this.inputRandomPrefMenuItem = new javax.swing.JMenuItem();
    this.inputZipfMenuItem = new javax.swing.JMenuItem();
    this.simulateMenuItem = new javax.swing.JMenuItem();
    this.helpMenu = new javax.swing.JMenu();
    this.aboutMenuItem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Cache Simulator");
    setIconImage(new javax.swing.ImageIcon(getClass().getResource(
      "/ico/simulation.png")).getImage());
    setLocationByPlatform(true);
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(final java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    this.simulatorToolbar.setFloatable(false);
    this.simulatorToolbar.setRollover(true);
    this.simulatorToolbar.setName("SimulatorToolar");
    this.simulatorToolbar.addSeparator();

    this.simulateButton.setIcon(new javax.swing.ImageIcon(getClass()
      .getResource("/ico/run.png"))); // NOI18N
    this.simulateButton.setToolTipText("Simulate!");
    this.simulateButton.setFocusable(false);
    this.simulateButton
      .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.simulateButton
      .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.simulateButton.addActionListener(this::simulateButtonActionPerformed);
    this.simulatorToolbar.add(this.simulateButton);

    this.simCancelButton.setIcon(new javax.swing.ImageIcon(getClass()
      .getResource("/ico/cancel.png"))); // NOI18N
    this.simCancelButton.setToolTipText("Cancel Simulation!");
    this.simCancelButton.setEnabled(false);
    this.simCancelButton.setFocusable(false);
    this.simCancelButton
      .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.simCancelButton
      .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.simCancelButton.addActionListener(this::simCancelButtonActionPerformed);
    this.simulatorToolbar.add(this.simCancelButton);
    this.simulatorToolbar.add(this.jSeparator1);

    this.saveCSVButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
      "/ico/csv.png"))); // NOI18N
    this.saveCSVButton.setToolTipText("Save Results to CSV");
    this.saveCSVButton.setEnabled(false);
    this.saveCSVButton.setFocusable(false);
    this.saveCSVButton
      .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.saveCSVButton
      .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.saveCSVButton.addActionListener(this::saveCSVButtonActionPerformed);
    this.simulatorToolbar.add(this.saveCSVButton);

    this.saveXLSButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
      "/ico/xls.png"))); // NOI18N
    this.saveXLSButton.setToolTipText("Save Results to XLS");
    this.saveXLSButton.setEnabled(false);
    this.saveXLSButton.setFocusable(false);
    this.saveXLSButton
      .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.saveXLSButton
      .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.saveXLSButton.addActionListener(this::saveXLSButtonActionPerformed);
    this.simulatorToolbar.add(this.saveXLSButton);

    this.printConsoleButton.setIcon(new javax.swing.ImageIcon(getClass()
      .getResource("/ico/console.png"))); // NOI18N
    this.printConsoleButton.setToolTipText("Print results to console");
    this.printConsoleButton.setEnabled(false);
    this.printConsoleButton.setFocusable(false);
    this.printConsoleButton
      .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.printConsoleButton
      .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.printConsoleButton
      .addActionListener(this::printConsoleButtonActionPerformed);
    this.simulatorToolbar.add(this.printConsoleButton);
    this.simulatorToolbar.add(this.jSeparator2);

    this.exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
      "/ico/exit.png"))); // NOI18N
    this.exitButton.setToolTipText("Exit");
    this.exitButton.setFocusable(false);
    this.exitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    this.exitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.exitButton.addActionListener(MainGUI::exitButtonActionPerformed);
    this.simulatorToolbar.add(this.exitButton);

    this.panelsPane.setName("Simulation"); // NOI18N

    this.settingsPane.setName("SettingsPanel"); // NOI18N
    this.settingsPane.setLayout(null);

    this.cacheCapacityPanel.setBorder(javax.swing.BorderFactory
      .createTitledBorder("Cache capacities [MB]"));

    this.cacheCapacityList.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
    this.cacheCapacityList.setModel(new CapacityAbstractModel());
    this.cacheCapacityList
      .setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    this.jScrollPane1.setViewportView(this.cacheCapacityList);

    this.cacheCapSpinner.setValue(8);

    this.cachePlusButton.setText("+");
    this.cachePlusButton.addActionListener(this::cachePlusButtonActionPerformed);

    this.cacheMinusButton.setText("-");
    this.cacheMinusButton.addActionListener(this::cacheMinusButtonActionPerformed);

    final javax.swing.GroupLayout cacheCapacityPanelLayout = new javax.swing.GroupLayout(
      this.cacheCapacityPanel);
    this.cacheCapacityPanel.setLayout(cacheCapacityPanelLayout);
    cacheCapacityPanelLayout.setHorizontalGroup(cacheCapacityPanelLayout
      .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(
        cacheCapacityPanelLayout.createSequentialGroup()
          .addContainerGap().addComponent(this.jScrollPane1)
          .addContainerGap())
      .addGroup(
        javax.swing.GroupLayout.Alignment.TRAILING,
        cacheCapacityPanelLayout
          .createSequentialGroup()
          .addContainerGap(70, Short.MAX_VALUE)
          .addComponent(this.cacheCapSpinner,
            javax.swing.GroupLayout.PREFERRED_SIZE,
            121,
            javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGap(18, 18, 18)
          .addComponent(this.cachePlusButton)
          .addGap(18, 18, 18)
          .addComponent(this.cacheMinusButton,
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
              this.jScrollPane1,
              javax.swing.GroupLayout.DEFAULT_SIZE,
              196, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addGroup(
              cacheCapacityPanelLayout
                .createParallelGroup(
                  javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(
                  this.cacheCapSpinner,
                  javax.swing.GroupLayout.PREFERRED_SIZE,
                  javax.swing.GroupLayout.DEFAULT_SIZE,
                  javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(
                  this.cachePlusButton)
                .addComponent(
                  this.cacheMinusButton))
            .addContainerGap()));

    this.settingsPane.add(this.cacheCapacityPanel);
    this.cacheCapacityPanel.setBounds(369, 11, 372, 270);

    this.othersSettingsPanel.setBorder(javax.swing.BorderFactory
      .createTitledBorder("Others"));
    this.othersSettingsPanel.setLayout(null);

    this.statLimitLabel.setText("Minimum request count for including result:");
    this.othersSettingsPanel.add(this.statLimitLabel);
    this.statLimitLabel.setBounds(16, 27, 250, 32);

    this.statLimitSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.statLimitSpinner.setValue(30);
    this.statLimitSpinner.addChangeListener(this::statLimitSpinnerStateChanged);
    this.othersSettingsPanel.add(this.statLimitSpinner);
    this.statLimitSpinner.setBounds(296, 33, 60, 18);

    this.networkSpeedLabel.setText("Average network speed:");
    this.othersSettingsPanel.add(this.networkSpeedLabel);
    this.networkSpeedLabel.setBounds(60, 89, 149, 14);

    this.networkSpeesLabel2.setText("Mbit/s");
    this.othersSettingsPanel.add(this.networkSpeesLabel2);
    this.networkSpeesLabel2.setBounds(310, 90, 40, 14);

    this.networkSpeedSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.networkSpeedSpinner.setValue(80);
    this.networkSpeedSpinner.addChangeListener(this::networkSpeedSpinnerStateChanged);
    this.othersSettingsPanel.add(this.networkSpeedSpinner);
    this.networkSpeedSpinner.setBounds(210, 90, 90, 18);

    this.slidingWindowLabel
      .setText("Sliding window capacity (%from cache capacity):");
    this.othersSettingsPanel.add(this.slidingWindowLabel);
    this.slidingWindowLabel.setBounds(16, 138, 280, 14);

    this.slidingWindwowSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.slidingWindwowSpinner.setValue(25);
    this.slidingWindwowSpinner.addChangeListener(this::slidingWindwowSpinnerStateChanged);
    this.othersSettingsPanel.add(this.slidingWindwowSpinner);
    this.slidingWindwowSpinner.setBounds(296, 135, 60, 18);

    this.settingsPane.add(this.othersSettingsPanel);
    this.othersSettingsPanel.setBounds(370, 290, 370, 180);

    this.requestsSettingsPanel.setBorder(javax.swing.BorderFactory
      .createTitledBorder("Requests"));
    this.requestsSettingsPanel.setLayout(null);

    this.requestsInputComboBox.setModel(new javax.swing.DefaultComboBoxModel(
      new String[]{CHOOSE_ONE, "From AFS log file",
        "Gaussian random", "Random", "Random with preference",
        "Zipf random"}));
    this.requestsInputComboBox.addItemListener(this::requestsInputComboBoxItemStateChanged);
    this.requestsInputComboBox.addActionListener(this::requestsInputComboBoxActionPerformed);
    this.requestsSettingsPanel.add(this.requestsInputComboBox);
    this.requestsInputComboBox.setBounds(180, 30, 156, 22);

    this.inputRequestLabel.setText("Requests input method:");
    this.requestsSettingsPanel.add(this.inputRequestLabel);
    this.inputRequestLabel.setBounds(30, 30, 173, 20);

    this.randomFileSizeCheckBox.setText("Generate random file sizes");
    this.randomFileSizeCheckBox.addItemListener(this::randomFileSizeCheckBoxItemStateChanged);
    this.randomFileSizeCheckBox.addActionListener(this::randomFileSizeCheckBoxActionPerformed);
    this.requestsSettingsPanel.add(this.randomFileSizeCheckBox);
    this.randomFileSizeCheckBox.setBounds(80, 350, 190, 23);

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
    this.minGenFileSizejSpinner.addChangeListener(this::minGenFileSizejSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.minGenFileSizejSpinner);
    this.minGenFileSizejSpinner.setBounds(190, 381, 88, 18);

    this.maxGenFileSizejSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.maxGenFileSizejSpinner.setValue(32000);
    this.maxGenFileSizejSpinner.addChangeListener(this::maxGenFileSizejSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.maxGenFileSizejSpinner);
    this.maxGenFileSizejSpinner.setBounds(190, 411, 88, 18);

    this.pathTextField.setEditable(false);
    this.pathTextField.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(final java.awt.event.MouseEvent evt) {
        pathTextFieldMouseClicked(evt);
      }
    });
    this.requestsSettingsPanel.add(this.pathTextField);
    this.pathTextField.setBounds(60, 80, 270, 30);

    this.pathLabel.setText("Path:");
    this.requestsSettingsPanel.add(this.pathLabel);
    this.pathLabel.setBounds(16, 88, 50, 14);

    this.generateFileSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.generateFileSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
    this.generateFileSpinner.addChangeListener(this::generateFileSpinnerStateChanged);
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
    this.maenValueGaussSpinner.addChangeListener(this::maenValueGaussSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.maenValueGaussSpinner);
    this.maenValueGaussSpinner.setBounds(220, 150, 80, 20);

    this.preferenceDivisibleLabel.setText("Prefenced file's ID divisible by:");
    this.requestsSettingsPanel.add(this.preferenceDivisibleLabel);
    this.preferenceDivisibleLabel.setBounds(20, 190, 180, 20);

    this.preferenceDivisibleSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.preferenceDivisibleSpinner.addChangeListener(this::preferenceDivisibleSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.preferenceDivisibleSpinner);
    this.preferenceDivisibleSpinner.setBounds(220, 190, 80, 20);

    this.stepPrefLabel.setText("Step for generate preferenced file:");
    this.requestsSettingsPanel.add(this.stepPrefLabel);
    this.stepPrefLabel.setBounds(20, 240, 190, 10);

    this.stepPrefSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.stepPrefSpinner.addChangeListener(this::stepPrefSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.stepPrefSpinner);
    this.stepPrefSpinner.setBounds(220, 240, 80, 20);

    this.nonPrefLabel.setText("Non preferenced file's ID divisible by:");
    this.requestsSettingsPanel.add(this.nonPrefLabel);
    this.nonPrefLabel.setBounds(20, 280, 190, 10);

    this.nonPrefLabelSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.nonPrefLabelSpinner.addChangeListener(this::nonPrefLabelSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.nonPrefLabelSpinner);
    this.nonPrefLabelSpinner.setBounds(219, 280, 80, 20);

    this.dispersionLabel.setText("Dispersion:");
    this.requestsSettingsPanel.add(this.dispersionLabel);
    this.dispersionLabel.setBounds(40, 190, 170, 14);

    this.dispersionSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.dispersionSpinner.addChangeListener(this::dispersionSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.dispersionSpinner);
    this.dispersionSpinner.setBounds(220, 190, 80, 18);

    this.requestsCountLabel.setText("Requests count:");
    this.requestsSettingsPanel.add(this.requestsCountLabel);
    this.requestsCountLabel.setBounds(40, 100, 170, 14);

    this.requestCountSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.requestCountSpinner.setMinimumSize(new java.awt.Dimension(30, 20));
    this.requestCountSpinner.addChangeListener(this::requestCountSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.requestCountSpinner);
    this.requestCountSpinner.setBounds(220, 90, 80, 20);

    this.zipfLambdaLabel.setText("Zipf generator lambda:");
    this.requestsSettingsPanel.add(this.zipfLambdaLabel);
    this.zipfLambdaLabel.setBounds(40, 190, 150, 14);

    this.zipfLamdbaSpinner.setModel(zipfModel);
    this.zipfLamdbaSpinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.zipfLamdbaSpinner.addChangeListener(this::zipfLamdbaSpinnerStateChanged);
    this.requestsSettingsPanel.add(this.zipfLamdbaSpinner);
    this.zipfLamdbaSpinner.setBounds(220, 190, 80, 18);

    this.settingsPane.add(this.requestsSettingsPanel);
    this.requestsSettingsPanel.setBounds(10, 10, 353, 456);

    this.panelsPane.addTab("Simulation Settings", this.settingsPane);

    this.cachePane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    this.cachePane.setName("CacheAlgPanel"); // NOI18N

    this.cachePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 6,
      10, 6));
    this.cachePanel.setLayout(new javax.swing.BoxLayout(this.cachePanel,
      javax.swing.BoxLayout.Y_AXIS));
    this.cachePane.setLeftComponent(this.cachePanel);

    this.noSettingsPanel.setLayout(null);

    this.noSettingsLabel
      .setText("This cache policy does not have any settings!");
    this.noSettingsPanel.add(this.noSettingsLabel);
    this.noSettingsLabel.setBounds(110, 60, 410, 14);

    this.cachePane.setRightComponent(this.noSettingsPanel);

    this.panelsPane.addTab("Cache Algorithms", this.cachePane);

    this.resultsPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1,
      1, 1));
    this.resultsPane.setName("ResultsPanel"); // NOI18N
    this.resultsPane.setLayout(null);

    this.userList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    this.userList.addListSelectionListener(this::userListValueChanged);
    this.jScrollPane2.setViewportView(this.userList);

    this.resultsPane.add(this.jScrollPane2);
    this.jScrollPane2.setBounds(11, 51, 136, 256);

    this.userLabel.setText("Simulated user:");
    this.resultsPane.add(this.userLabel);
    this.userLabel.setBounds(24, 16, 110, 14);

    this.resultsChangeCombo.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.resultsChangeCombo.setModel(new javax.swing.DefaultComboBoxModel(
      new String[]{"Read Hit Ratio [%]", "Read Hit Count",
        "Saved Bytes Ratio [%]", "Saved Byted [MB]",
        "Data Transfer Degrease Ratio [%]",
        "Data Transfer Degrease [MB]"}));
    this.resultsChangeCombo.setMaximumSize(new java.awt.Dimension(192, 22));
    this.resultsChangeCombo.addActionListener(this::resultsChangeComboActionPerformed);
    this.resultsPane.add(this.resultsChangeCombo);
    this.resultsChangeCombo.setBounds(11, 338, 140, 21);

    this.resultsTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object[][]{{null, null, null, null},
        {null, null, null, null}, {null, null, null, null},
        {null, null, null, null}}, new String[]{"Title 1",
      "Title 2", "Title 3", "Title 4"}));
    this.resultsTable
      .setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    this.resultsTable.setEnabled(false);
    this.resultsTable.setRowSelectionAllowed(false);
    this.jScrollPane3.setViewportView(this.resultsTable);

    this.resultsPane.add(this.jScrollPane3);
    this.jScrollPane3.setBounds(165, 51, 573, 411);

    this.totalReqLabel.setText("Total requested files:");
    this.resultsPane.add(this.totalReqLabel);
    this.totalReqLabel.setBounds(140, 16, 116, 14);

    this.totalReqTextField.setEditable(false);
    this.totalReqTextField.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.totalReqTextField.addActionListener(this::totalReqTextFieldActionPerformed);
    this.resultsPane.add(this.totalReqTextField);
    this.totalReqTextField.setBounds(260, 13, 91, 19);

    this.totalNetLabel.setText("Total network traffic (without cache):");
    this.resultsPane.add(this.totalNetLabel);
    this.totalNetLabel.setBounds(389, 16, 200, 14);

    this.totalNetTextField.setEditable(false);
    this.totalNetTextField.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
    this.totalNetTextField.addActionListener(this::totalNetTextFieldActionPerformed);
    this.resultsPane.add(this.totalNetTextField);
    this.totalNetTextField.setBounds(597, 13, 80, 19);

    this.jLabel1.setText("MB");
    this.resultsPane.add(this.jLabel1);
    this.jLabel1.setBounds(690, 16, 40, 14);

    this.chooseResultsLabel.setText("Choose results:");
    this.resultsPane.add(this.chooseResultsLabel);
    this.chooseResultsLabel.setBounds(29, 318, 120, 14);

    this.barChartButton.setText("Bar Chart");
    this.barChartButton.addActionListener(this::barChartButtonActionPerformed);
    this.resultsPane.add(this.barChartButton);
    this.barChartButton.setBounds(10, 390, 136, 23);

    this.lineChartButton.setText("Line Chart");
    this.lineChartButton.addActionListener(this::lineChartButtonActionPerformed);
    this.resultsPane.add(this.lineChartButton);
    this.lineChartButton.setBounds(10, 430, 136, 23);

    this.panelsPane.addTab("Results", this.resultsPane);

    this.statusPanel.setBorder(new javax.swing.border.SoftBevelBorder(
      javax.swing.border.BevelBorder.LOWERED));

    final javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(
      this.statusPanel);
    this.statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(statusPanelLayout
      .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(this.simulationProgressBar,
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
              this.simulationProgressBar,
              javax.swing.GroupLayout.PREFERRED_SIZE,
              javax.swing.GroupLayout.DEFAULT_SIZE,
              javax.swing.GroupLayout.PREFERRED_SIZE)));

    this.fileMenu.setMnemonic('F');
    this.fileMenu.setText("File");

    this.saveCSVMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_C, 0));
    this.saveCSVMenuItem.setMnemonic('C');
    this.saveCSVMenuItem.setText("Save Results to CSV");
    this.saveCSVMenuItem.setToolTipText("Saves tables with results to CSV file");
    this.saveCSVMenuItem.setEnabled(false);
    this.saveCSVMenuItem.addActionListener(this::saveCSVMenuItemActionPerformed);
    this.fileMenu.add(this.saveCSVMenuItem);

    this.saveXLSMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_L, 0));
    this.saveXLSMenuItem.setMnemonic('L');
    this.saveXLSMenuItem.setText("Save Results to XLS");
    this.saveXLSMenuItem.setToolTipText("Saves tables with results to XLS file");
    this.saveXLSMenuItem.setEnabled(false);
    this.saveXLSMenuItem.addActionListener(this::saveXLSMenuItemActionPerformed);
    this.fileMenu.add(this.saveXLSMenuItem);

    this.saveConsoleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_P, 0));
    this.saveConsoleMenuItem.setMnemonic('P');
    this.saveConsoleMenuItem.setText("Print results to console");
    this.saveConsoleMenuItem.setEnabled(false);
    this.saveConsoleMenuItem
      .addActionListener(this::saveConsoleMenuItemActionPerformed);
    this.fileMenu.add(this.saveConsoleMenuItem);
    this.fileMenu.add(this.fileMenuSeparator);

    this.exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_X, 0));
    this.exitMenuItem.setMnemonic('X');
    this.exitMenuItem.setText("Exit");
    this.exitMenuItem.setToolTipText("Terminate simulator");
    this.exitMenuItem.addActionListener(MainGUI::exitMenuItemActionPerformed);
    this.fileMenu.add(this.exitMenuItem);

    this.menuBar.add(this.fileMenu);

    this.simulationMenu.setMnemonic('S');
    this.simulationMenu.setText("Simulation");

    this.requestMenu.setMnemonic('R');
    this.requestMenu.setText("Request input method");
    this.requestMenu.setToolTipText("Change requests input method");

    this.inputAFSMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_F, 0));
    this.inputAFSMenuItem.setMnemonic('F');
    this.inputAFSMenuItem.setText("From AFS log file");
    this.inputAFSMenuItem
      .setToolTipText("Requests to the files are from AFS log file");
    this.inputAFSMenuItem.addActionListener(this::inputAFSMenuItemActionPerformed);
    this.requestMenu.add(this.inputAFSMenuItem);

    this.inputGaussianMenuItem.setAccelerator(javax.swing.KeyStroke
      .getKeyStroke(java.awt.event.KeyEvent.VK_G, 0));
    this.inputGaussianMenuItem.setMnemonic('G');
    this.inputGaussianMenuItem.setText("Gaussian random");
    this.inputGaussianMenuItem
      .setToolTipText("Requests to the files are from Gaussian distribution");
    this.inputGaussianMenuItem
      .addActionListener(this::inputGaussianMenuItemActionPerformed);
    this.requestMenu.add(this.inputGaussianMenuItem);

    this.inputUniformlyMenuItem.setAccelerator(javax.swing.KeyStroke
      .getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
    this.inputUniformlyMenuItem.setMnemonic('R');
    this.inputUniformlyMenuItem.setText("Uniformly random");
    this.inputUniformlyMenuItem
      .setToolTipText("Requests to the files are from uniformly random distribution");
    this.inputUniformlyMenuItem.addActionListener(this::inputUniformlyMenuItemActionPerformed);
    this.requestMenu.add(this.inputUniformlyMenuItem);

    this.inputRandomPrefMenuItem.setAccelerator(javax.swing.KeyStroke
      .getKeyStroke(java.awt.event.KeyEvent.VK_P, 0));
    this.inputRandomPrefMenuItem.setMnemonic('P');
    this.inputRandomPrefMenuItem.setText("Random with preferences");
    this.inputRandomPrefMenuItem
      .setToolTipText("Requests to the files are from random distribution with preferention of files");
    this.inputRandomPrefMenuItem.addActionListener(this::inputRandomPrefMenuItemActionPerformed);
    this.requestMenu.add(this.inputRandomPrefMenuItem);

    this.inputZipfMenuItem.setAccelerator(javax.swing.KeyStroke
      .getKeyStroke(java.awt.event.KeyEvent.VK_Z, 0));
    this.inputZipfMenuItem.setMnemonic('Z');
    this.inputZipfMenuItem.setText("Zipf random");
    this.inputZipfMenuItem
      .setToolTipText("Requests to the files are from Zipf distribution");
    this.inputZipfMenuItem.addActionListener(this::inputZipfMenuItemActionPerformed);
    this.requestMenu.add(this.inputZipfMenuItem);

    this.simulationMenu.add(this.requestMenu);

    this.simulateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_I, 0));
    this.simulateMenuItem.setMnemonic('I');
    this.simulateMenuItem.setText("Simulate");
    this.simulateMenuItem.setToolTipText("Run simulation");
    this.simulateMenuItem.addActionListener(this::simulateMenuItemActionPerformed);
    this.simulationMenu.add(this.simulateMenuItem);

    this.menuBar.add(this.simulationMenu);

    this.helpMenu.setMnemonic('H');
    this.helpMenu.setText("Help");

    this.aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
      java.awt.event.KeyEvent.VK_A, 0));
    this.aboutMenuItem.setMnemonic('A');
    this.aboutMenuItem.setText("About");
    this.aboutMenuItem.addActionListener(this::aboutMenuItemActionPerformed);
    this.helpMenu.add(this.aboutMenuItem);

    this.menuBar.add(this.helpMenu);

    setJMenuBar(this.menuBar);

    final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
      getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout
      .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(this.simulatorToolbar,
        javax.swing.GroupLayout.DEFAULT_SIZE, 754,
        Short.MAX_VALUE)
      .addComponent(this.panelsPane)
      .addGroup(
        layout.createParallelGroup(
          javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(
            this.statusPanel,
            javax.swing.GroupLayout.Alignment.TRAILING,
            javax.swing.GroupLayout.DEFAULT_SIZE,
            javax.swing.GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE)));
    layout.setVerticalGroup(layout
      .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(
        layout.createSequentialGroup()
          .addComponent(this.simulatorToolbar,
            javax.swing.GroupLayout.PREFERRED_SIZE,
            40,
            javax.swing.GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(
            javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(this.panelsPane,
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
                this.statusPanel,
                javax.swing.GroupLayout.PREFERRED_SIZE,
                javax.swing.GroupLayout.DEFAULT_SIZE,
                javax.swing.GroupLayout.PREFERRED_SIZE))));

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void pathTextFieldMouseClicked(final java.awt.event.MouseEvent evt) {// GEN-FIRST:event_pathTextFieldMouseClicked
    chooseAFSFile();
  }// GEN-LAST:event_pathTextFieldMouseClicked

  private void randomFileSizeCheckBoxItemStateChanged(
    final java.awt.event.ItemEvent evt) {// GEN-FIRST:event_randomFileSizeCheckBoxItemStateChanged
    GlobalVariables.setRandomFileSizesForLoggedData(this.randomFileSizeCheckBox
      .isSelected());
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
  }// GEN-LAST:event_randomFileSizeCheckBoxItemStateChanged

  private void requestsInputComboBoxActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_requestsInputComboBoxActionPerformed
    // TODO add your handling code here:
  }// GEN-LAST:event_requestsInputComboBoxActionPerformed

  private void requestsInputComboBoxItemStateChanged(
    final java.awt.event.ItemEvent evt) {// GEN-FIRST:event_requestsInputComboBoxItemStateChanged

    if (evt.getStateChange() == ItemEvent.DESELECTED) {
      return;
    }
    final String ret = (String) evt.getItem();

    if (ret.equalsIgnoreCase(CHOOSE_ONE)) {
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
  }// GEN-LAST:event_requestsInputComboBoxItemStateChanged

  @SuppressWarnings("unchecked")
  private void cachePlusButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cachePlusButtonActionPerformed
    final Integer newCacheCap = (Integer) this.cacheCapSpinner.getValue();
    if (newCacheCap <= 0) {
      JOptionPane.showMessageDialog(this,
        "You have to insert positive integer!", ALERT,
        JOptionPane.OK_OPTION);
      return;
    }
    final CapacityAbstractModel model = (CapacityAbstractModel) this.cacheCapacityList
      .getModel();
    final Integer[] array = model.getArray();
    for (int i = 0; i < array.length; i++) {
      if (newCacheCap.compareTo(array[i]) == 0) {
        JOptionPane.showMessageDialog(this,
          "You have to insert different value!", ALERT,
          JOptionPane.OK_OPTION);
        return;
      }
    }
    final Integer[] newArray = new Integer[array.length + 1];
    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[newArray.length - 1] = Integer.valueOf(newCacheCap);
    Arrays.sort(newArray);

    this.cacheCapacityList.setModel(new CapacityAbstractModel(newArray));
    this.cacheCapacityList.invalidate();
    this.cacheCapacityList.repaint();

  }// GEN-LAST:event_cachePlusButtonActionPerformed

  /**
   * metoda pro odebrani polozky z cache capacity
   *
   * @param evt
   */
  private void cacheMinusButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cacheMinusButtonActionPerformed
    if (this.cacheCapacityList.getSelectedIndex() == -1) {
      JOptionPane.showMessageDialog(this,
        "You have to select cache capacity to dismiss!", ALERT,
        JOptionPane.OK_OPTION);
      return;
    }
    final CapacityAbstractModel model = (CapacityAbstractModel) this.cacheCapacityList
      .getModel();
    model.remove(this.cacheCapacityList.getSelectedIndex());
    this.cacheCapacityList.invalidate();
    this.cacheCapacityList.repaint();
  }// GEN-LAST:event_cacheMinusButtonActionPerformed

  private void statLimitSpinnerStateChanged(final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_statLimitSpinnerStateChanged
    if ((Integer) this.statLimitSpinner.getValue() < 1) {
      this.statLimitSpinner.setValue(1);
    }
    GlobalVariables.setLimitForStatistics((Integer) this.statLimitSpinner
      .getValue());
  }// GEN-LAST:event_statLimitSpinnerStateChanged

  private void networkSpeedSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_networkSpeedSpinnerStateChanged
    if ((Integer) this.networkSpeedSpinner.getValue() < 1) {
      this.networkSpeedSpinner.setValue(1);
    } else if ((Integer) this.networkSpeedSpinner.getValue() > 10000) {
      this.networkSpeedSpinner.setValue(10000);

    }
    GlobalVariables.setAverageNetworkSpeed((Integer) this.networkSpeedSpinner
      .getValue());
  }// GEN-LAST:event_networkSpeedSpinnerStateChanged

  private void slidingWindwowSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_slidingWindwowSpinnerStateChanged
    if ((Integer) this.slidingWindwowSpinner.getValue() < 0) {
      this.slidingWindwowSpinner.setValue(0);
    } else if ((Integer) this.slidingWindwowSpinner.getValue() > 75) {
      this.slidingWindwowSpinner.setValue(75);
    }
    GlobalVariables
      .setCacheCapacityForDownloadWindow(((Integer) this.slidingWindwowSpinner
        .getValue()).intValue());
  }// GEN-LAST:event_slidingWindwowSpinnerStateChanged

  private void maxGenFileSizejSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_maxGenFileSizejSpinnerStateChanged
    if ((Integer) this.maxGenFileSizejSpinner.getValue() < 0) {
      this.maxGenFileSizejSpinner.setValue(0);
    } else if ((Integer) this.maxGenFileSizejSpinner.getValue() <= (Integer) this.minGenFileSizejSpinner
      .getValue()) {
      this.maxGenFileSizejSpinner.setValue((Long) this.minGenFileSizejSpinner
        .getValue() + 1);
    }
    GlobalVariables
      .setMaxGeneratedFileSize((Integer) this.maxGenFileSizejSpinner
        .getValue());
  }// GEN-LAST:event_maxGenFileSizejSpinnerStateChanged

  private void minGenFileSizejSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_minGenFileSizejSpinnerStateChanged
    if ((Integer) this.minGenFileSizejSpinner.getValue() < 0) {
      this.minGenFileSizejSpinner.setValue(0);
    } else if ((Integer) this.maxGenFileSizejSpinner.getValue() <= (Integer) this.minGenFileSizejSpinner
      .getValue()) {
      this.minGenFileSizejSpinner.setValue((Integer) this.maxGenFileSizejSpinner
        .getValue() - 1);
    }
    GlobalVariables
      .setMinGeneratedFileSize((Integer) this.minGenFileSizejSpinner
        .getValue());
  }// GEN-LAST:event_minGenFileSizejSpinnerStateChanged

  private void randomFileSizeCheckBoxActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_randomFileSizeCheckBoxActionPerformed
    GlobalVariables.setRandomFileSizesForLoggedData(this.randomFileSizeCheckBox
      .isSelected());
  }// GEN-LAST:event_randomFileSizeCheckBoxActionPerformed

  private void nonPrefLabelSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_nonPrefLabelSpinnerStateChanged
    if ((Integer) this.nonPrefLabelSpinner.getValue() <= 0) {
      this.nonPrefLabelSpinner.setValue(1);
    }
    GlobalVariables
      .setFileRequestnNonPreferenceFile((Integer) this.nonPrefLabelSpinner
        .getValue());
  }// GEN-LAST:event_nonPrefLabelSpinnerStateChanged

  private void stepPrefSpinnerStateChanged(final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_stepPrefSpinnerStateChanged
    if ((Integer) this.stepPrefSpinner.getValue() <= 0) {
      this.stepPrefSpinner.setValue(1);
    }
    GlobalVariables.setFileRequestPreferenceFile((Integer) this.stepPrefSpinner
      .getValue());
  }// GEN-LAST:event_stepPrefSpinnerStateChanged

  private void preferenceDivisibleSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_preferenceDivisibleSpinnerStateChanged
    if ((Integer) this.preferenceDivisibleSpinner.getValue() <= 0) {
      this.preferenceDivisibleSpinner.setValue(1);
    }
    GlobalVariables
      .setFileRequestPreferenceStep((Integer) this.preferenceDivisibleSpinner
        .getValue());
  }// GEN-LAST:event_preferenceDivisibleSpinnerStateChanged

  private void dispersionSpinnerStateChanged(final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_dispersionSpinnerStateChanged
    if ((Integer) this.dispersionSpinner.getValue() <= 0) {
      this.dispersionSpinner.setValue(1);
    }
    GlobalVariables
      .setFileRequestGeneratorDispersion((Integer) this.dispersionSpinner
        .getValue());
  }// GEN-LAST:event_dispersionSpinnerStateChanged

  private void maenValueGaussSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_maenValueGaussSpinnerStateChanged
    if ((Integer) this.maenValueGaussSpinner.getValue() <= 0) {
      this.maenValueGaussSpinner.setValue(1);
    }
    GlobalVariables
      .setFileRequestGeneratorMeanValue((Integer) this.maenValueGaussSpinner
        .getValue());
  }// GEN-LAST:event_maenValueGaussSpinnerStateChanged

  private void generateFileSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_generateFileSpinnerStateChanged
    if ((Integer) this.generateFileSpinner.getValue() <= 0) {
      this.generateFileSpinner.setValue(1);
    }
    GlobalVariables
      .setFileRequestGeneratorMaxValue((Integer) this.generateFileSpinner
        .getValue());
  }// GEN-LAST:event_generateFileSpinnerStateChanged

  private void requestCountSpinnerStateChanged(
    final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_requestCountSpinnerStateChanged
    if ((Integer) this.requestCountSpinner.getValue() <= 0) {
      this.requestCountSpinner.setValue(1);
    }
    GlobalVariables
      .setRequestCountForRandomGenerator((Integer) this.requestCountSpinner
        .getValue());
  }// GEN-LAST:event_requestCountSpinnerStateChanged

  private void simulateButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_simulateButtonActionPerformed
    runSimulation();
  }// GEN-LAST:event_simulateButtonActionPerformed

  /**
   * meotoda pro spusteni simualce
   */
  private void runSimulation() {
    if (checkSettings()) {
      this.cacheResults = null;

      // nastaveni nahodnych generatoru cisel
      GlobalMethods.setGenerators();

      // vytvoreni objektu generatoru pristupovanych souboru
      IFileQueue fileQueue = null;
      if (this.requestsInputComboBox.getSelectedIndex() == 2) {
        fileQueue = new GaussianFileNameGenerator(
          GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 3) {
        fileQueue = new RandomFileNameGenerator(
          GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 4) {
        fileQueue = new RandomFileNameGeneratorWithPrefences(
          GlobalVariables.getRequestCountForRandomGenerator());
      } else if (this.requestsInputComboBox.getSelectedIndex() == 5) {
        fileQueue = new ZipfFileNameGenerator(
          GlobalVariables.getRequestCountForRandomGenerator());
      } else if (GlobalVariables.isLoadDataFromLog()) {
        fileQueue = new LogReaderAFS();
      }

      // objekt simulatoru
      final AccessSimulation simulation = new AccessSimulation(fileQueue);

      // velikosti cache
      final Integer[] sizes = ((CapacityAbstractModel) this.cacheCapacityList
        .getModel()).getArray();

      // nastaveni progress baru
      this.simulationProgressBar.setMinimum(0);
      this.simulationProgressBar.setMaximum(sizes.length);
      this.simulationProgressBar.setValue(0);
      this.simulationProgressBar.setVisible(true);
      this.simulationProgressBar.setStringPainted(true);
      this.simulationProgressBar.setString("Simulation in progress... 0%");

      // vlakno pro spusteni simulace - kvuli updatu progressbaru
      this.simulationThread = new Thread(() -> {
        disableComponentsForSimulation();
        Server.getInstance().hardReset();
        MainGUI.this.simulationProgressBar.setMaximum(100);

        // spusteni simulace
        if (GlobalVariables.isLoadDataFromLog()
          && GlobalVariables.isRandomFileSizesForLoggedData()) {
          simulation.simulateRandomFileSizes();
        } else if (GlobalVariables.isLoadDataFromLog()) {
          simulation.simulateFromLogFile();
        } else {
          simulation.simulateRandomFileSizes();
        }

        MainGUI.this.simulationProgressBar.setVisible(false);
        MainGUI.this.cacheResults = simulation.getResults();
        if (MainGUI.this.cacheResults != null && !MainGUI.this.cacheResults.isEmpty()) {
          loadResultsToPanel();
          enableComponentsAfterSimulaton(true);
        } else {
          JOptionPane.showMessageDialog(MainGUI.getInstance(),
            "There are no results!", "Error",
            JOptionPane.ERROR_MESSAGE);
          enableComponentsAfterSimulaton(false);
        }

      });

      // spusteni vlakna simulace
      this.simulationThread.start();
    }
  }

  /**
   * metoda pro znepristupneni komponent pro simulaci
   */
  private void disableComponentsForSimulation() {
    this.simulateButton.setEnabled(false);
    this.simCancelButton.setEnabled(true);
    this.panelsPane.setEnabledAt(2, false);
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
  }

  /**
   * metoda pro obnoveni pouzitelnosti komponent po simulaci
   *
   * @param isSimSuccesfull prepinac pro udani, zda byla simulace uspesna
   */
  private void enableComponentsAfterSimulaton(final boolean isSimSuccesfull) {
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
    if (isSimSuccesfull) {
      this.panelsPane.setEnabledAt(2, true);
      this.panelsPane.setSelectedIndex(2);
      this.panelsPane.setEnabled(true);
    } else {
      this.panelsPane.setEnabledAt(2, false);
      this.panelsPane.setSelectedIndex(0);
      this.panelsPane.setEnabled(true);
    }
  }

  private void resultsChangeComboActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_resultsChangeComboActionPerformed
    loadResultsToTable();
  }// GEN-LAST:event_resultsChangeComboActionPerformed

  private void userListValueChanged(final javax.swing.event.ListSelectionEvent evt) {// GEN-FIRST:event_userListValueChanged
    loadResultsToTable();
  }// GEN-LAST:event_userListValueChanged

  private void totalReqTextFieldActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_totalReqTextFieldActionPerformed
    // TODO add your handling code here:
  }// GEN-LAST:event_totalReqTextFieldActionPerformed

  private void totalNetTextFieldActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_totalNetTextFieldActionPerformed
    // TODO add your handling code here:
  }// GEN-LAST:event_totalNetTextFieldActionPerformed

  @SuppressWarnings("deprecation")
  private void simCancelButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_simCancelButtonActionPerformed
    if (this.simulationThread != null) {
      this.simulationThread.interrupt();
      this.simulationThread.stop();
      this.simulationThread = null;
    }
    enableComponentsAfterSimulaton(false);
  }// GEN-LAST:event_simCancelButtonActionPerformed

  private void saveCSVButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveCSVButtonActionPerformed
    saveToCSV();
  }// GEN-LAST:event_saveCSVButtonActionPerformed

  private void saveXLSButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveXLSButtonActionPerformed
    saveToXLS();
  }// GEN-LAST:event_saveXLSButtonActionPerformed

  /**
   * metoda pro ulozeni vysledne tabulky do csv
   */
  private void saveToCSV() {
    final JFileChooser fc = new JFileChooser(GlobalVariables.getActDir()) {
      @Override
      public void approveSelection() {
        final File f = getSelectedFile();
        if (f.exists() && getDialogType() == SAVE_DIALOG) {
          final int result = JOptionPane.showConfirmDialog(this,
            "The file exists, overwrite?", "Existing file",
            JOptionPane.YES_NO_CANCEL_OPTION);
          switch (result) {
            case JOptionPane.YES_OPTION:
              super.approveSelection();
              return;
            case JOptionPane.NO_OPTION:
              return;
            case JOptionPane.CLOSED_OPTION:
              cancelSelection();
              return;
            case JOptionPane.CANCEL_OPTION:
              cancelSelection();
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
          return f.getAbsolutePath().endsWith(".csv");
        }
        return true;
      }

      @Override
      public String getDescription() {
        return "CSV files";
      }
    };
    fc.setFileFilter(ff);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    final int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      String fName = fc.getSelectedFile().getAbsolutePath();
      if (!fName.endsWith(".csv")) {
        fName = fName + ".csv";
      }
      Output.saveStatToCSV(fName, this.cacheResults);
      GlobalVariables.setActDir(fc.getSelectedFile().getPath());
    }
  }

  /**
   * metoda pro ulozeni vysledne tabulky do XLS
   */
  private void saveToXLS() {
    final JFileChooser fc = new JFileChooser(GlobalVariables.getActDir()) {
      @Override
      public void approveSelection() {
        final File f = getSelectedFile();
        if (f.exists() && getDialogType() == SAVE_DIALOG) {
          final int result = JOptionPane.showConfirmDialog(this,
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
    final FileFilter ff = new FileFilter() {

      @Override
      public boolean accept(final File f) {
        if (f.isFile()) {
          return f.getAbsolutePath().endsWith(".xls");
        }
        return true;
      }

      @Override
      public String getDescription() {
        return "XLS files";
      }
    };
    fc.setFileFilter(ff);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    final int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      String fName = fc.getSelectedFile().getAbsolutePath();
      if (!fName.endsWith(".xls")) {
        fName = fName + ".xls";
      }
      GlobalVariables.setActDir(fc.getSelectedFile().getPath());
      Output.saveStatToXLS(fName, this.cacheResults);
    }
  }

  /**
   * metoda pro tisk vysledku do konzole
   */
  private void printToConsole() {
    Output.printAllStatConsole(this.cacheResults);
  }

  private static void exitMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitMenuItemActionPerformed
    ConfigReaderWriter.write();
    System.exit(0);
  }// GEN-LAST:event_exitMenuItemActionPerformed

  private void inputAFSMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_inputAFSMenuItemActionPerformed
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(1);
  }// GEN-LAST:event_inputAFSMenuItemActionPerformed

  private void inputGaussianMenuItemActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_inputGaussianMenuItemActionPerformed
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(2);
  }// GEN-LAST:event_inputGaussianMenuItemActionPerformed

  private void inputUniformlyMenuItemActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_inputUniformlyMenuItemActionPerformed
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(3);
  }// GEN-LAST:event_inputUniformlyMenuItemActionPerformed

  private void inputRandomPrefMenuItemActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_inputRandomPrefMenuItemActionPerformed
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(4);
  }// GEN-LAST:event_inputRandomPrefMenuItemActionPerformed

  private void inputZipfMenuItemActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_inputRandomPrefMenuItemActionPerformed
    this.panelsPane.setSelectedIndex(0);
    this.requestsInputComboBox.setSelectedIndex(5);
  }// GEN-LAST:event_inputRandomPrefMenuItemActionPerformed

  private void simulateMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_simulateMenuItemActionPerformed
    runSimulation();
  }// GEN-LAST:event_simulateMenuItemActionPerformed

  private void saveXLSMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveXLSMenuItemActionPerformed
    saveToXLS();
  }// GEN-LAST:event_saveXLSMenuItemActionPerformed

  private void saveCSVMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveCSVMenuItemActionPerformed
    saveToCSV();
  }// GEN-LAST:event_saveCSVMenuItemActionPerformed

  private void aboutMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_aboutMenuItemActionPerformed
    JOptionPane
      .showMessageDialog(
        this,
        "<html><center><b>Cache Simulator</b>"
          + "<p>"
          + "<b>Pavel Bzoch, 2012<br/>Department of Computer Science and Engineering<br/>University of West Bohemia<br/>www.kiv.zcu.zcu, www.fav.zcu.cz<br/>pbzoch@kiv.zcu.cz</b>",
        "About", JOptionPane.INFORMATION_MESSAGE);

  }// GEN-LAST:event_aboutMenuItemActionPerformed

  private static void exitButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitButtonActionPerformed
    ConfigReaderWriter.write();
    System.exit(0);
  }// GEN-LAST:event_exitButtonActionPerformed

  private void barChartButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_barChartButtonActionPerformed
    final BarChart chart = new BarChart(this.resultsChangeCombo
      .getSelectedItem().toString(), this.cacheResults.get(this.userList
      .getSelectedIndex()), this.resultsChangeCombo.getSelectedIndex());
    chart.pack();
    chart.setVisible(true);
  }// GEN-LAST:event_barChartButtonActionPerformed

  private static void formWindowClosing(final java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
    ConfigReaderWriter.write();
  }// GEN-LAST:event_formWindowClosing

  private void lineChartButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_lineChartButtonActionPerformed
    final LineChart chart = new LineChart(this.resultsChangeCombo
      .getSelectedItem().toString(), this.cacheResults.get(this.userList
      .getSelectedIndex()), this.resultsChangeCombo.getSelectedIndex());
    chart.pack();
    chart.setVisible(true);
  }// GEN-LAST:event_lineChartButtonActionPerformed

  private void printConsoleButtonActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_printConsoleButtonActionPerformed
    printToConsole();
  }// GEN-LAST:event_printConsoleButtonActionPerformed

  private void saveConsoleMenuItemActionPerformed(
    final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveConsoleMenuItemActionPerformed
    printToConsole();
  }// GEN-LAST:event_saveConsoleMenuItemActionPerformed

  private void zipfLamdbaSpinnerStateChanged(final javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_zipfLamdbaSpinnerStateChanged
    GlobalVariables.setZipfLambda((Double) this.zipfLamdbaSpinner.getValue());
  }// GEN-LAST:event_zipfLamdbaSpinnerStateChanged

  /**
   * trida pro prezentaci modelu pro pole velikosti
   */
  @SuppressWarnings("rawtypes")
  private class myUsersListModel extends javax.swing.AbstractListModel {
    long[] userIDs;
    String[] userNames;

    @Override
    public int getSize() {
      return this.userNames.length;
    }

    @Override
    public Object getElementAt(final int i) {
      return this.userNames[i];
    }

    public myUsersListModel() {
      this.userIDs = new long[MainGUI.this.cacheResults.size()];
      this.userNames = new String[MainGUI.this.cacheResults.size()];
      for (int i = 0; i < MainGUI.this.cacheResults.size(); i++) {
        this.userIDs[i] = MainGUI.this.cacheResults.get(i).getUserID();
        if (this.userIDs[i] == 0) {
          this.userNames[i] = "Simulated user";
        } else {
          final long id = this.userIDs[i] >> 32;
          this.userNames[i] = id + ", ip: "
            + (GlobalMethods.intToIp(this.userIDs[i] - (id << 32)));
        }
      }
    }

  }

  /**
   * metoda pro nahrani vysledku do panelu results
   */
  @SuppressWarnings("unchecked")
  private void loadResultsToPanel() {
    // nacteni uzivatelu
    this.userList.setModel(new myUsersListModel());
    this.userList.setSelectedIndex(0);

    // oznaceni prvniho
    this.resultsChangeCombo.setSelectedIndex(0);

    // otevreni panelu s vysledky
    this.panelsPane.setEnabledAt(2, true);

    // nahrani vysledku do tabulky
    loadResultsToTable();
  }

  /**
   * obsluha zmeny tlacitka pro vyber cache algoritmu
   *
   * @param evt
   */
  private void cacheCheckBoxActionPerformed(final java.awt.event.ActionEvent evt) {
    final String name = ((JCheckBox) evt.getSource()).getName();
    JPanel cacheSettings;
    try {
      cacheSettings = (JPanel) Class.forName(
        "cacheSimulator.gui." + name + "Panel").newInstance();
    } catch (final Exception ex) {
      cacheSettings = this.noSettingsPanel;
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
  private void cacheCheckBoxMouseMove(final java.awt.event.MouseEvent evt) {
    final String name = ((JCheckBox) evt.getSource()).getName();
    JPanel cacheSettings;
    try {
      cacheSettings = (JPanel) Class.forName(
        "cz.zcu.kiv.cacheSimulator.gui." + name + "Panel").newInstance();
    } catch (final Exception ex) {
      cacheSettings = this.noSettingsPanel;
    }
    this.cachePane.setRightComponent(cacheSettings);
    this.cachePane.invalidate();
    this.cachePane.repaint();
  }

  /**
   * metoda pro vraceni jmen trid cachi, ktere jsou zaskrtnuty
   *
   * @return pole nazcu trid
   */
  public String[] getCachesNames() {
    if (this.cacheCheckBoxes == null) {
      return new String[0];
    }
    final ArrayList<String> names = new ArrayList<>();
    for (final javax.swing.JCheckBox box : this.cacheCheckBoxes) {
      if (box.isSelected()) {
        names.add(box.getName());
      }
    }
    final String[] ret = new String[names.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = names.get(i);
    }
    return ret;
  }

  /**
   * metoda pro vraceni pole velikosti cachi
   *
   * @return velikosti cache
   */
  public Integer[] getCacheSizes() {
    return ((CapacityAbstractModel) this.cacheCapacityList.getModel())
      .getArray();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutMenuItem;
  private javax.swing.JButton barChartButton;
  private javax.swing.JSpinner cacheCapSpinner;
  private javax.swing.JList cacheCapacityList;
  private javax.swing.JPanel cacheCapacityPanel;
  private javax.swing.JButton cacheMinusButton;
  private javax.swing.JSplitPane cachePane;
  private javax.swing.JPanel cachePanel;
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
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JToolBar.Separator jSeparator1;
  private javax.swing.JToolBar.Separator jSeparator2;
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
  private javax.swing.JLabel noSettingsLabel;
  private javax.swing.JPanel noSettingsPanel;
  private javax.swing.JLabel nonPrefLabel;
  private javax.swing.JSpinner nonPrefLabelSpinner;
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
  private javax.swing.JComboBox requestsInputComboBox;
  private javax.swing.JPanel requestsSettingsPanel;
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
  private javax.swing.JProgressBar simulationProgressBar;
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
  private javax.swing.JList userList;
  private javax.swing.JLabel zipfLambdaLabel;
  private javax.swing.JSpinner zipfLamdbaSpinner;
  // End of variables declaration//GEN-END:variables
}
