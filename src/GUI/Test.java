package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.GridBagLayout;
import javax.swing.JList;
import javax.swing.JToolBar;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Label;
import javax.swing.ScrollPaneConstants;
import java.awt.Rectangle;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.ListSelectionModel;

public class Test extends JFrame {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test frame = new Test();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Test() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("Account");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Logout");
		mnFile.add(mntmExit);
		
		JMenuItem mntmEsci = new JMenuItem("Esci");
		mnFile.add(mntmEsci);
		
		Object title = BorderFactory.createTitledBorder("Descrizione");
		((TitledBorder) title).setTitleJustification(TitledBorder.CENTER);
		
		DefaultTableModel model = new DefaultTableModel();
    	//SE LI HO GIÀ, INUTILE FARE UN'ALTRA RICHIESTA!
    	System.out.println("Prendo ranking");
	    //client.getRanking();
	    System.out.println("Presi!");
	    
	    model.addColumn("Username"); 
	    model.addColumn("Accettato");
	    
	    int rows = 0;
	    //ArrayList<String> invitedList = client.getInvited();
		/*for (int i=0; i<client.invitedList.size(); i++){
			//table.setValueAt(s, rows, 0);
		    model.addRow(new Object[]{client.invitedList.get(i), new Boolean(false)});
		}*/
		System.out.println("righe aggiunte");
		getContentPane().setLayout(null);
		
		System.out.println("creata tabella");

	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
