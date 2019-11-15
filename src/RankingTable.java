import javax.swing.table.AbstractTableModel;

class RankingTable extends AbstractTableModel {
    private String[] columnNames = {"Username",
            "Punteggio",
            "Invita"};
    
    private Object[][] data = {
    		{"Mary", new Integer(5), new Boolean(false)},
    		{"Alison", new Integer(3), new Boolean(true)},
    		{"Kathy", new Integer(2), new Boolean(false)},
    		{"Sharon", new Integer(20), new Boolean(true)},
    		{"Philip", new Integer(10), new Boolean(false)}
    		};

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}