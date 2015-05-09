/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.gui.configuration;

import javax.swing.SpinnerNumberModel;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LRDv2;

/**
 *
 * @author Pavel Bzoch
 * trida pro nastaveni LRD verze 2 policy
 */
@SuppressWarnings("serial")
public class LRDv2Panel extends javax.swing.JPanel {

    /**
     * Creates new form LRDv2Panel
     */
    public LRDv2Panel() {
        initComponents();
        intervalSpinner.setValue(LRDv2.getINTERVAL());
        SpinnerNumberModel modelDivisor = new SpinnerNumberModel(LRDv2.getK1(), 1.1, 20, 0.1);
        divisorSpinner.setModel(modelDivisor);    
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        lrdv2Label = new javax.swing.JLabel();
        intervalLabel = new javax.swing.JLabel();
        intervalSpinner = new javax.swing.JSpinner();
        divisorLabel = new javax.swing.JLabel();
        divisorSpinner = new javax.swing.JSpinner();

        setLayout(null);

        lrdv2Label.setText("LRDv2 settings");
        add(lrdv2Label);
        lrdv2Label.setBounds(130, 50, 170, 14);

        intervalLabel.setText("Interval for decrease refernce count:");
        add(intervalLabel);
        intervalLabel.setBounds(20, 100, 230, 14);

        intervalSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                intervalSpinnerStateChanged(evt);
            }
        });
        add(intervalSpinner);
        intervalSpinner.setBounds(230, 90, 102, 30);

        divisorLabel.setText("Divisor for reference count:");
        add(divisorLabel);
        divisorLabel.setBounds(60, 150, 160, 14);

        divisorSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                divisorSpinnerStateChanged(evt);
            }
        });
        add(divisorSpinner);
        divisorSpinner.setBounds(220, 140, 102, 30);
    }// </editor-fold>                        

    private void divisorSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_divisorSpinnerStateChanged
        LRDv2.setK1((Double) divisorSpinner.getValue());
    }//GEN-LAST:event_divisorSpinnerStateChanged

    private void intervalSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_intervalSpinnerStateChanged
        if ((Integer) intervalSpinner.getValue() <= 0){
            intervalSpinner.setValue(1);
        }
        else if ((Integer) intervalSpinner.getValue() >= 200){
            intervalSpinner.setValue(199);
        }
        LRDv2.setINTERVAL((Integer)intervalSpinner.getValue());
    }//GEN-LAST:event_intervalSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel divisorLabel;
    private javax.swing.JSpinner divisorSpinner;
    private javax.swing.JLabel intervalLabel;
    private javax.swing.JSpinner intervalSpinner;
    private javax.swing.JLabel lrdv2Label;
    // End of variables declaration//GEN-END:variables
}