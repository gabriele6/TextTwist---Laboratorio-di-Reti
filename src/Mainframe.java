import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Mainframe extends JFrame {
	public JPanel panel;
	private TwistClient client = null;
	public ArrayList<String> namesList;
	public JMenuBar menuBar;
	public Mainframe frame = null;
	CopyOnWriteArrayList<String> addList = new CopyOnWriteArrayList<String>();
	
	public JPanel getPanel(){
		return panel;
	}
	
	public Mainframe(String title){
		super(title);
		client = new TwistClient(this);
		namesList = new ArrayList<String>();
		setResizable(false);
		setBounds(100, 100, 450, 300);
		frame = this;

	}
	public void sendAlert(String msg){
		JOptionPane.showMessageDialog(this.panel, msg);
	}

	
	public LoginPanel loginPanel(){
		this.setSize(240, 135);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("Account");
		frame.menuBar.add(mnFile);
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mnFile.add(mntmLogout);
		
		JMenuItem mntmEsci = new JMenuItem("Esci");
		mnFile.add(mntmEsci);
		
		mntmLogout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				{
					client.logout();
					frame.setContentPane(frame.loginPanel());
					frame.invalidate();
					frame.repaint();
				}
			}
		});
		
		menuBar.setVisible(false);
		//set layout manager
		setLayout(new BorderLayout());
						
		panel = new LoginPanel(client, this);

		return (LoginPanel) panel;
	}
	
	//GENERA LA SCHERMATA PRINCIPALE DEL GIOCO
	public MainPanel mainPanel(){
		panel = new MainPanel(client, this);
		menuBar.setVisible(true);
		
		return (MainPanel) panel;
	}
	
	//GENERA LA SCHERMATA DELLA CLASSIFICA
	public HighscorePanel highscorePanel(){
		menuBar.setVisible(true);
		panel = new HighscorePanel(client, this);
		return (HighscorePanel) panel;
	}
	
	//GENERA LA SCHERMATA DELLA LOBBY
	public LobbyPanel lobbyPanel(){
		menuBar.setVisible(true);
		panel = new LobbyPanel(client, this);
		return (LobbyPanel) panel;
	}
	
	//GENERA LA SCHERMATA DELLA PARTITA
	public MatchPanel matchPanel(){
		setLayout(new BorderLayout());

		panel = new MatchPanel(client, this);
		
		return (MatchPanel) panel;
	}

	public void refresh() {
		panel = mainPanel();
		setCurrentPanel();
		invalidate();
		repaint();
	}

	public synchronized void setCurrentPanel() {
		if(panel!=null)
			setContentPane(panel);
	}
}
