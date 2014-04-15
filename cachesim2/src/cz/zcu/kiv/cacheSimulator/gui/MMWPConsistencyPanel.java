package cz.zcu.kiv.cacheSimulator.gui;

import cz.zcu.kiv.cacheSimulator.consistency.MMWPConsistency;

/**
 *
 * @author Pavel Bzoch
 * trida pro nastaveni MMWP consistency
 */
@SuppressWarnings("serial")
public class MMWPConsistencyPanel extends javax.swing.JPanel {

    /**
     * Creates new form MMWPConsistencyPanel
     */
    public MMWPConsistencyPanel() {
        initComponents();
    }
    
    /**
     * updates values in dialogue
     */
    public void updateValues(){
        hits2Spinner.setValue(MMWPConsistency.getHits2());
        ttl2Spinner.setValue(MMWPConsistency.getTtl2());
        hits3Spinner.setValue(MMWPConsistency.getHits3());
        ttl3Spinner.setValue(MMWPConsistency.getTtl3());
        hits4Spinner.setValue(MMWPConsistency.getHits4());
        ttl4Spinner.setValue(MMWPConsistency.getTtl4());
        hits5Spinner.setValue(MMWPConsistency.getHits5());
        ttl5Spinner.setValue(MMWPConsistency.getTtl5());
        ttl1Spinner.setValue(MMWPConsistency.getTtl1());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        HeadingsLabel = new javax.swing.JLabel();
        number1Label = new javax.swing.JLabel();
        hits2Spinner = new javax.swing.JSpinner();
        hits2Spinner.setValue(MMWPConsistency.getHits2());
        TTLLabel = new javax.swing.JLabel();
        ttl2Spinner = new javax.swing.JSpinner();
        ttl2Spinner.setValue(MMWPConsistency.getTtl2());
        number1Label1 = new javax.swing.JLabel();
        hits3Spinner = new javax.swing.JSpinner();
        hits3Spinner.setValue(MMWPConsistency.getHits3());
        TTLLabel1 = new javax.swing.JLabel();
        ttl3Spinner = new javax.swing.JSpinner();
        ttl3Spinner.setValue(MMWPConsistency.getTtl3());
        number1Label2 = new javax.swing.JLabel();
        hits4Spinner = new javax.swing.JSpinner();
        hits4Spinner.setValue(MMWPConsistency.getHits4());
        TTLLabel2 = new javax.swing.JLabel();
        ttl4Spinner = new javax.swing.JSpinner();
        ttl4Spinner.setValue(MMWPConsistency.getTtl4());
        number1Label3 = new javax.swing.JLabel();
        hits5Spinner = new javax.swing.JSpinner();
        hits5Spinner.setValue(MMWPConsistency.getHits5());
        TTLLabel3 = new javax.swing.JLabel();
        ttl5Spinner = new javax.swing.JSpinner();
        ttl5Spinner.setValue(MMWPConsistency.getTtl5());
        TTLLabel4 = new javax.swing.JLabel();
        ttl1Spinner = new javax.swing.JSpinner();
        ttl1Spinner.setValue(MMWPConsistency.getTtl1());
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setLayout(null);

        HeadingsLabel.setText("MMWP consistency control settings");
        add(HeadingsLabel);
        HeadingsLabel.setBounds(90, 40, 250, 14);

        number1Label.setText("Number of hits:");
        add(number1Label);
        number1Label.setBounds(30, 120, 90, 14);

        hits2Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        hits2Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hits2SpinnerStateChanged(evt);
            }
        });
        add(hits2Spinner);
        hits2Spinner.setBounds(130, 120, 50, 20);

        TTLLabel.setText("TTL:");
        add(TTLLabel);
        TTLLabel.setBounds(210, 120, 40, 14);

        ttl2Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        ttl2Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ttl2SpinnerStateChanged(evt);
            }
        });
        add(ttl2Spinner);
        ttl2Spinner.setBounds(250, 120, 50, 20);

        number1Label1.setText("Number of hits:");
        add(number1Label1);
        number1Label1.setBounds(30, 150, 90, 14);

        hits3Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        hits3Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hits3SpinnerStateChanged(evt);
            }
        });
        add(hits3Spinner);
        hits3Spinner.setBounds(130, 150, 50, 20);

        TTLLabel1.setText("TTL:");
        add(TTLLabel1);
        TTLLabel1.setBounds(210, 150, 40, 14);

        ttl3Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        ttl3Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ttl3SpinnerStateChanged(evt);
            }
        });
        add(ttl3Spinner);
        ttl3Spinner.setBounds(250, 150, 50, 20);

        number1Label2.setText("Number of hits:");
        add(number1Label2);
        number1Label2.setBounds(30, 180, 90, 14);

        hits4Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        hits4Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hits4SpinnerStateChanged(evt);
            }
        });
        add(hits4Spinner);
        hits4Spinner.setBounds(130, 180, 50, 20);

        TTLLabel2.setText("TTL:");
        add(TTLLabel2);
        TTLLabel2.setBounds(210, 180, 40, 14);

        ttl4Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        ttl4Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ttl4SpinnerStateChanged(evt);
            }
        });
        add(ttl4Spinner);
        ttl4Spinner.setBounds(250, 180, 50, 20);

        number1Label3.setText("Number of hits:");
        add(number1Label3);
        number1Label3.setBounds(30, 210, 90, 14);

        hits5Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        hits5Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                hits5SpinnerStateChanged(evt);
            }
        });
        add(hits5Spinner);
        hits5Spinner.setBounds(130, 210, 50, 20);

        TTLLabel3.setText("TTL:");
        add(TTLLabel3);
        TTLLabel3.setBounds(210, 210, 40, 14);

        ttl5Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        ttl5Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ttl5SpinnerStateChanged(evt);
            }
        });
        add(ttl5Spinner);
        ttl5Spinner.setBounds(250, 210, 50, 20);

        TTLLabel4.setText("TTL:");
        add(TTLLabel4);
        TTLLabel4.setBounds(210, 90, 40, 14);

        ttl1Spinner.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        ttl1Spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ttl1SpinnerStateChanged(evt);
            }
        });
        add(ttl1Spinner);
        ttl1Spinner.setBounds(250, 90, 50, 20);

        jLabel1.setText("Least write hits");
        add(jLabel1);
        jLabel1.setBounds(100, 90, 100, 14);

        jLabel2.setText("[s]");
        add(jLabel2);
        jLabel2.setBounds(310, 210, 20, 14);

        jLabel3.setText("[s]");
        add(jLabel3);
        jLabel3.setBounds(310, 90, 20, 14);

        jLabel4.setText("[s]");
        add(jLabel4);
        jLabel4.setBounds(310, 120, 20, 14);

        jLabel5.setText("[s]");
        add(jLabel5);
        jLabel5.setBounds(310, 150, 20, 14);

        jLabel6.setText("[s]");
        add(jLabel6);
        jLabel6.setBounds(310, 180, 20, 14);
    }

    private void ttl1SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)ttl1Spinner.getValue() < (Integer)ttl2Spinner.getValue()){
        	ttl1Spinner.setValue((Integer)ttl2Spinner.getValue());
        }
        if ((Integer)ttl1Spinner.getValue() > 999){
        	ttl1Spinner.setValue(999);
        }
        MMWPConsistency.setTtl1((Integer)ttl1Spinner.getValue());
    }

    private void ttl2SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)ttl2Spinner.getValue() < (Integer)ttl3Spinner.getValue()){
        	ttl2Spinner.setValue((Integer)ttl3Spinner.getValue());
        }
        if ((Integer)ttl2Spinner.getValue() > (Integer)ttl1Spinner.getValue()){
        	ttl1Spinner.setValue( (Integer)ttl1Spinner.getValue());
        }
        MMWPConsistency.setTtl2((Integer)ttl2Spinner.getValue());
    }

    private void ttl3SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)ttl3Spinner.getValue() < (Integer)ttl4Spinner.getValue()){
        	ttl3Spinner.setValue((Integer)ttl4Spinner.getValue());
        }
        if ((Integer)ttl3Spinner.getValue() > (Integer)ttl2Spinner.getValue()){
        	ttl3Spinner.setValue( (Integer)ttl2Spinner.getValue());
        }
        MMWPConsistency.setTtl3((Integer)ttl3Spinner.getValue());
    }

    private void ttl4SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)ttl4Spinner.getValue() < (Integer)ttl5Spinner.getValue()){
        	ttl4Spinner.setValue((Integer)ttl5Spinner.getValue());
        }
        if ((Integer)ttl4Spinner.getValue() > (Integer)ttl3Spinner.getValue()){
        	ttl4Spinner.setValue( (Integer)ttl3Spinner.getValue());
        }
        MMWPConsistency.setTtl4((Integer)ttl4Spinner.getValue());
    }

    private void ttl5SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)ttl5Spinner.getValue() <= 0){
        	ttl5Spinner.setValue(1);
        }
        if ((Integer)ttl5Spinner.getValue() > (Integer)ttl4Spinner.getValue()){
        	ttl5Spinner.setValue( (Integer)ttl4Spinner.getValue());
        }
        MMWPConsistency.setTtl5((Integer)ttl5Spinner.getValue());
    }

    private void hits2SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)hits2Spinner.getValue() <= 0){
        	hits2Spinner.setValue(1);
        }
        if ((Integer)hits2Spinner.getValue() >= (Integer)hits3Spinner.getValue()){
        	hits2Spinner.setValue( (Integer)hits3Spinner.getValue() -1);
        }
        MMWPConsistency.setHits2((Integer)hits2Spinner.getValue());
    }

    private void hits3SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)hits3Spinner.getValue() <= (Integer)hits2Spinner.getValue()){
        	hits3Spinner.setValue((Integer)hits2Spinner.getValue()+1);
        }
        if ((Integer)hits3Spinner.getValue() >= (Integer)hits4Spinner.getValue()){
        	hits3Spinner.setValue((Integer)hits4Spinner.getValue() -1);
        }
        MMWPConsistency.setHits3((Integer)hits3Spinner.getValue());
    }

    private void hits4SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)hits4Spinner.getValue() <= (Integer)hits3Spinner.getValue()){
        	hits4Spinner.setValue((Integer)hits3Spinner.getValue()+1);
        }
        if ((Integer)hits4Spinner.getValue() >= (Integer)hits5Spinner.getValue()){
        	hits4Spinner.setValue((Integer)hits5Spinner.getValue() -1);
        }
        MMWPConsistency.setHits4((Integer)hits4Spinner.getValue());
    }

    private void hits5SpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if ((Integer)hits5Spinner.getValue() <= (Integer)hits4Spinner.getValue()){
        	hits5Spinner.setValue((Integer)hits4Spinner.getValue()+1);
        }
        if ((Integer)hits5Spinner.getValue() >= 999){
        	hits5Spinner.setValue(999);
        }
        MMWPConsistency.setHits5((Integer)hits5Spinner.getValue());
    }

    private javax.swing.JLabel HeadingsLabel;
    private javax.swing.JLabel TTLLabel;
    private javax.swing.JLabel TTLLabel1;
    private javax.swing.JLabel TTLLabel2;
    private javax.swing.JLabel TTLLabel3;
    private javax.swing.JLabel TTLLabel4;
    private javax.swing.JSpinner hits2Spinner;
    private javax.swing.JSpinner hits3Spinner;
    private javax.swing.JSpinner hits4Spinner;
    private javax.swing.JSpinner hits5Spinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel number1Label;
    private javax.swing.JLabel number1Label1;
    private javax.swing.JLabel number1Label2;
    private javax.swing.JLabel number1Label3;
    private javax.swing.JSpinner ttl1Spinner;
    private javax.swing.JSpinner ttl2Spinner;
    private javax.swing.JSpinner ttl3Spinner;
    private javax.swing.JSpinner ttl4Spinner;
    private javax.swing.JSpinner ttl5Spinner;
}
