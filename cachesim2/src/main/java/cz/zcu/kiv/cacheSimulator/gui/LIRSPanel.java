/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.gui;

import cz.zcu.kiv.cacheSimulator.cachePolicies.LIRS;

/**
 *
 * @author Pavel Bzoch
 * trida pro nastaveni LIRS policy
 */
@SuppressWarnings("serial")
public class LIRSPanel extends javax.swing.JPanel {

    /**
     * Creates new form LIRSPanel
     */
    public LIRSPanel() {
        initComponents();
        lirCapSpinner.setValue((int) (LIRS.getLIR_CAPACITY() * 100));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        lirsLabel = new javax.swing.JLabel();
        lirCapSpinner = new javax.swing.JSpinner();
        lirCapLabel = new javax.swing.JLabel();

        lirsLabel.setText("Settings for LIRS policy");

        lirCapSpinner.setValue(0);
        lirCapSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lirCapSpinnerStateChanged(evt);
            }
        });

        lirCapLabel.setText("% of capacity for LIR:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(lirCapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lirCapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(lirsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(134, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(lirsLabel)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lirCapLabel)
                    .addComponent(lirCapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(179, Short.MAX_VALUE))
        );
    }// </editor-fold>                        

    private void lirCapSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lirCapSpinnerStateChanged
        if ((Integer)lirCapSpinner.getValue() <= 0){
            lirCapSpinner.setValue(1);
        } else if ((Integer)lirCapSpinner.getValue() >= 100){
            lirCapSpinner.setValue(99);
        }
        LIRS.setLIR_CAPACITY(((Integer)lirCapSpinner.getValue()) / 100.0);
    }//GEN-LAST:event_lirCapSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lirCapLabel;
    private javax.swing.JSpinner lirCapSpinner;
    private javax.swing.JLabel lirsLabel;
    // End of variables declaration//GEN-END:variables
}
