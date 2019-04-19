package cz.zcu.kiv.cacheSimulator.gui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

/**
 * trida pro konstrukci panelu pro ovladani cache algoritmu LRFU-SS
 * @author Pavel Bzoch
 */
@SuppressWarnings("serial")
public class LRFU_SSPanel extends JPanel{
    
    /**
     * ovladaci prvky
     */
    private JLabel K1Label, K2Label, cacheLabel;
    private JSpinner K1Spinner, K2Spinner;
    private JCheckBox statBox;

    /**
     * konstruktor - iniciace panelu
     */
    public LRFU_SSPanel() {
        this.setLayout(null);
        cacheLabel = new JLabel();
        cacheLabel.setText("Settings for LRFU-SS");
        this.add(cacheLabel);
        cacheLabel.setBounds(110, 60, 410, 14);
        
        K1Label = new JLabel("K1:  ");
        this.add(K1Label);
        K1Label.setBounds(110, 122, 410, 14);
        
        SpinnerNumberModel modelK1 = new SpinnerNumberModel(cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS.getK1(), 0, 50, 0.01);
        
        K1Spinner = new JSpinner(modelK1);
        this.add(K1Spinner);
        K1Spinner.setBounds(150, 118, 100, 25);
        
        K1Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                K1SpinnerStateChanged(evt);
            }
        });
     
        
        K2Label = new JLabel("K2:  ");
        this.add(K2Label);
        K2Label.setBounds(110, 165, 410, 14);
        
        SpinnerNumberModel modelK2 = new SpinnerNumberModel(cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS.getK2(), 0, 50, 0.01);
        
        K2Spinner = new JSpinner(modelK2);
        this.add(K2Spinner);
        K2Spinner.setBounds(150, 158, 100, 25);
        
        K2Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                K2SpinnerStateChanged(evt);
            }
        });
        
        statBox = new JCheckBox("Send statistics back to server");
        statBox.setSelected(GlobalVariables.isSendStatisticsToServerLRFUSS());
        this.add(statBox);
        statBox.setBounds(90, 200, 200,25);
        
        statBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
               statBoxCheckBoxItemStateChanged(evt);
            }
        });
        
    }
    
    /**
     * obsluha udalosti - zmena spineru
     * @param evt 
     */
    private void K1SpinnerStateChanged(javax.swing.event.ChangeEvent evt){
        cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS.setK1((Double) K1Spinner.getValue());
    }
    
    /**
     * obsluha udalosti - zmena spineru
     * @param evt 
     */
    private void K2SpinnerStateChanged(javax.swing.event.ChangeEvent evt){
        cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS.setK2((Double) K2Spinner.getValue());
    }
    
    /**
     * obsluha udalosti zmena zaskrtavatka
     * @param evt 
     */
    private void statBoxCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {                                                        
        GlobalVariables.setSendStatisticsToServerLRFUSS(statBox.isSelected());
    }  
    
}
