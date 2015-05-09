package cz.zcu.kiv.cacheSimulator.gui.model;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;


/**
 * @author Lukáš Kvídera
 * @version 2.1
 * 9. 5. 2015
 */
public class PolicyResultTable extends DefaultTableModel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param rowData
   * @param names
   */
  public PolicyResultTable(final Object[][] rowData, final String[] names) {
    super(rowData, names);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return ((Vector<?>)this.dataVector.get(0)).get(columnIndex).getClass();
  }

}
