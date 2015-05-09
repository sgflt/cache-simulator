
package cz.zcu.kiv.cacheSimulator.gui.configuration;

import javax.swing.SpinnerNumberModel;
import cz.zcu.kiv.cacheSimulator.consistency.TTLConsistency;

/**
 *
 * @author Pavel Bzoch
 * trida pro vykresleni panel pro nastaveni TTL consistency control
 */
@SuppressWarnings("serial")
public class TTLConsistencyPanel extends javax.swing.JPanel {


	/**
     * Creates new form TTLConsistencyPanel
     */
    public TTLConsistencyPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {

        ConsistencyControlDescLabel = new javax.swing.JLabel();
        TTLLabel = new javax.swing.JLabel();
        TTLSpinner = new javax.swing.JSpinner(new SpinnerNumberModel(TTLConsistency.getTtl() / 1000, 1, 500, 1));
        TimeLabel = new javax.swing.JLabel();

        setLayout(null);

        ConsistencyControlDescLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ConsistencyControlDescLabel.setText("Constant TTL consistency control settings");
        add(ConsistencyControlDescLabel);
        ConsistencyControlDescLabel.setBounds(50, 30, 230, 20);

        TTLLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TTLLabel.setText("TTL:");
        add(TTLLabel);
        TTLLabel.setBounds(70, 70, 34, 20);

        TTLSpinner.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TTLSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                TTLSpinnerStateChanged(evt);
            }
        });
        add(TTLSpinner);
        TTLSpinner.setBounds(110, 70, 60, 20);

        TimeLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TimeLabel.setText("[s]");
        add(TimeLabel);
        TimeLabel.setBounds(180, 70, 34, 20);
    }

    private void TTLSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_TTLSpinnerStateChanged
        TTLConsistency.setTtl((Integer)TTLSpinner.getValue() * 1000);
    }

    private javax.swing.JLabel ConsistencyControlDescLabel;
    private javax.swing.JLabel TTLLabel;
    private javax.swing.JSpinner TTLSpinner;
    private javax.swing.JLabel TimeLabel;
}
