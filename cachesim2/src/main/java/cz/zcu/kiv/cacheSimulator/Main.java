package cz.zcu.kiv.cacheSimulator;

import java.io.File;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.ConfigReaderWriter;

/**
 * trida, ktera obsahuje metodu main, vstupni bod simulace
 * 
 * class with main method, entry point to the simulation
 * 
 * @author Pavel Bzoch
 * 
 */
public class Main {
    
	/**
	 * constant for config file
	 */
    static final String CONFIG_FILE = "config.xml";

	/**
	 * vstupni bod programu (simulace) - zobrazeni gui
	 * 
	 * entry point to the simulation, reads confgi file, shows gui
	 * 
	 * @param args
	 *            argumenty prikazove radky / the command line arguments
	 *
     * 
     */
    public static void main(final String[] args) {
        //nacteni konfigurace
        final File config = new File(CONFIG_FILE);
        if ((config.exists() && config.isFile())){
            ConfigReaderWriter.read();    
        }
        
        //nastaveni look and feel
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (final Exception ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
       
        //vytvoreni a zobrazeni main GUI
      java.awt.EventQueue.invokeLater(() -> MainGUI.getInstance().setVisible(true));
    }
}
