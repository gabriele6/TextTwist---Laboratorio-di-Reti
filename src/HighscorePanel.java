

import Events.*;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;


public class HighscorePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6915622549267792262L;
	private boolean loggedIn = false;
	private Mainframe frame = null;
	private TwistClient client = null;

	//CopyOnWriteArrayList<String> addList = new CopyOnWriteArrayList<String>();
	JList list;
	
	public HighscorePanel(TwistClient client, Mainframe frame){
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 20, 450, 300);
		frame.setJMenuBar(frame.menuBar);

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		JButton removeButton = new JButton("Elimina");
		removeButton.setBounds(344, 210, 98, 28);
		add(removeButton);
		System.out.println("Creo lista");
		list = createList(frame.addList);
		add(list);
		
		JButton addButton = new JButton("Aggiungi");
		addButton.setBounds(8, 180, 319, 25);
		add(addButton);
		
		JLabel lblInvitati = new JLabel("Invitati:");
		lblInvitati.setBounds(346, 2, 55, 15);
		add(lblInvitati);
	    
	    DefaultTableModel model = new DefaultTableModel(); 
	    JTable table = new JTable(model){
	    	private static final long serialVersionUID = 1L;
	    	
	    	public boolean isCellEditable(int row, int col) {
	            if (col < 2) {
	                return false;
	            } else {
	                return true;
	            }
	        }
	    	@Override
	    	public Class getColumnClass(int column) {
	    		switch (column) {
            		case 0:
                    	return String.class;
                    case 1:
                        return Integer.class;
                    default:
                        return Boolean.class;
	    		}
    		}
    	}; 
    	//SE LI HO GIÀ, INUTILE FARE UN'ALTRA RICHIESTA!
    	System.out.println("Prendo ranking");
	    client.getRanking();
	    System.out.println("Presi!");
	    
	    model.addColumn("Username"); 
	    model.addColumn("Punteggio");
	    model.addColumn("Invita");
	    
	    int rows = 0;
		for (int i=0; i<client.users.size(); i++){
			//table.setValueAt(s, rows, 0);
		    model.addRow(new Object[]{client.users.get(i), client.scores.get(i), new Boolean(false)});
		}
		System.out.println("righe aggiunte");
		
		table.setSize(200, 10);
		table.setLocation(25, 105);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setSize(320, 168);
		scrollPane.setLocation(8, 8);
		table.setPreferredScrollableViewportSize(new Dimension(100, 100));
		add(scrollPane);
		
		System.out.println("creata tabella");
		
		JButton startButton = new JButton("Avvia partita");
		startButton.setBounds(8, 210, 319, 28);
		add(startButton);
		
		System.out.println("boh");
		
		removeButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				//rimuovi l'elemento selezionato dalla lista
				{
					System.out.println("indice: " + list.getSelectedIndex());
					if((!frame.addList.isEmpty())&&(list.getSelectedIndex()!=-1))
						frame.addList.remove(list.getSelectedIndex());
					
					frame.panel = frame.highscorePanel();
					frame.setContentPane(frame.panel);
					invalidate();
					repaint();
				}
			}


		});
		
		addButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				//prendo la tabella
				TableModel m = table.getModel();
				//controllo le checkbox attive e aggiorno la lista
				for(int i=0; i<table.getRowCount(); i++){
					if((Boolean)m.getValueAt(i, 2)==true){
						String un = (String)m.getValueAt(i, 0);
						if(!frame.addList.contains(un))
							frame.addList.add(un);
						m.setValueAt(false, i, 2);
					}
				}
				System.out.println(frame.addList);
				frame.panel = frame.highscorePanel();
				frame.setContentPane(frame.panel);
				invalidate();
				repaint();
			}
		});
		startButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				//avvio la partita

				ArrayList<String> a = new ArrayList<String>();
				for(String s : frame.addList)
					a.add(s);
				Boolean result = client.startGame(client.username, a);
				System.out.println("Risultato creazione partita: " + result);
				if(result.booleanValue()){
					if(client.getChars())
						System.out.println("Caratteri presi!");
					frame.panel = frame.matchPanel();
					frame.setContentPane(frame.panel);
					frame.addList.clear();
				}
			}
		});
		invalidate();
		repaint();
	}
	
	private JList createList(CopyOnWriteArrayList<String> a) {
		JList newlist = new JList();
		newlist.setModel(new AbstractListModel() {
			CopyOnWriteArrayList<String> values = a;
			public int getSize() {
				return values.size();
			}
			public String getElementAt(int index) {
				return values.get(index);
			}
		});
		newlist.setBounds(344, 20, 98, 187);
		return newlist;
	}
}
