

import Events.*;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 6915622549267792262L;
	private boolean loggedIn = false;
	private Mainframe frame = null;
	private TwistClient client = null;
	private JPanel contentPane;
	public String selection = null;
	public JList list;
	
	private EventListenerList listenerList = new EventListenerList();
	
	public MainPanel(TwistClient client, Mainframe frame){
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 450, 300);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);
		
		frame.setJMenuBar(frame.menuBar);
		//panel.add(menuBar);
		
		System.out.println("aggiunta roba");

		JLabel lblInviti = new JLabel("Inviti in attesa");
		lblInviti.setBounds(336, 5, 102, 15);
		add(lblInviti);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(331, 25, 107, 140);
		add(scrollPane);
		
		JList list = createList(client.pendingRequests());

		scrollPane.setViewportView(list);
		
		JButton btnRifiuta = new JButton("Rifiuta");
		btnRifiuta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnRifiuta.setBounds(331, 202, 106, 32);
		add(btnRifiuta);
		
		JButton btnClassifica = new JButton("Classifica / Nuova Partita");
		btnClassifica.setBounds(50, 202, 215, 32);
		add(btnClassifica);
		
		Object title = BorderFactory.createTitledBorder("Descrizione");
		((TitledBorder) title).setTitleJustification(TitledBorder.CENTER);
		
		/*TextArea textArea = new TextArea("", 10, 10, TextArea.SCROLLBARS_NONE);
		contentPane.add(textArea);
		textArea.setText("TextTwist è un gioco asdasdlasdlaosdla\n
		Ogni giocatore asadlosdlasdaoslasdoas\nAsdasdowfoglsfnseifascvlsdlfapepafll\n\nLolxDasasdapefcasaSDfsfsdfF.
		\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nlollolasdaosfowefo\nsgfslgeorg\n\nlrgoroohoroolf\n\n\nlfogdogoroorohordhodhllfgldlgrdgo");
		textArea.setFont(new Font("Andale Mono", Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setBounds(15, 20, 303, 172);
		//textArea.SCROLLBARS_NONE);*/
		
		JPanel panel = new JPanel();
		panel.setBounds(12, 5, 309, 190);
		add(panel);
		panel.setBorder((Border) title);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Aasdaksoasfosak\\n\nAAssdf\n\nSfkodofkefkoskvsvok");
		lblNewLabel.setVerticalTextPosition(SwingConstants.TOP);
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setFont(new Font("Andale Mono", Font.PLAIN, 12));
		lblNewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		lblNewLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		lblNewLabel.setBounds(5, 30, 285, 15);
		panel.add(lblNewLabel);
		
		JLabel label = new JLabel("Aasdaksoasfosak\\n\nAAssdf\n\nSfkodofkefkoskvsvok");
		label.setVerticalTextPosition(SwingConstants.TOP);
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setFont(new Font("Andale Mono", Font.PLAIN, 12));
		label.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		label.setBounds(5, 15, 285, 15);
		panel.add(label);
		
		JLabel label_1 = new JLabel("Aasdaksoasfosak\\n\nAAssdf\n\nSfkodofkefkoskvsvok");
		label_1.setVerticalTextPosition(SwingConstants.TOP);
		label_1.setVerticalAlignment(SwingConstants.TOP);
		label_1.setHorizontalTextPosition(SwingConstants.LEFT);
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setFont(new Font("Andale Mono", Font.PLAIN, 12));
		label_1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		label_1.setBounds(5, 45, 285, 15);
		panel.add(label_1);
		
		JButton btnAccetta = new JButton("Accetta");
		btnAccetta.setBounds(331, 167, 106, 32);
		add(btnAccetta);
		
		btnClassifica.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				frame.setLayout(new BorderLayout());
				frame.setSize(450, 300);
				frame.setContentPane(frame.highscorePanel());

			}


		});
		btnRifiuta.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				//prendo nome utente
				ListModel l = list.getModel();
				if(list.getSelectedIndex()!=-1){
					selection = (String)l.getElementAt(list.getSelectedIndex());
					System.out.println("Rifiuto invito di " + selection);
					client.rejectGame(selection);
					frame.panel=frame.mainPanel();
					frame.setContentPane(frame.panel);
					frame.invalidate();
					frame.repaint();
				}
				
			}
		});
		
		btnAccetta.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				//prendo nome utente
				ListModel l = list.getModel();
				if(list.getSelectedIndex()!=-1){
					selection = (String)l.getElementAt(list.getSelectedIndex());
					//invio al server
					if(client.acceptGame(selection)){
						System.out.println("Partita accettata! Provo a prendere i caratteri");
						if(client.getChars()){
							System.out.println("Caratteri presi!");
							frame.setContentPane(frame.lobbyPanel());
						System.out.println("Lobby creata!");}
						frame.invalidate();
						frame.repaint();
					}
					else
					{
						frame.sendAlert("Errore!");
					}
				}
			}
		});
	}
	
	private JList createList(CopyOnWriteArrayList<String> a) {
		JList newlist = new JList();
		newlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		newlist.setModel(new AbstractListModel() {
			CopyOnWriteArrayList<String> values = a;
			public int getSize() {
				return values.size();
			}
			public String getElementAt(int index) {
				return values.get(index);
			}
		});
		//newlist.setBounds(344, 20, 98, 187);
		return newlist;
	}
}
