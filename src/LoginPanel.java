import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

public class LoginPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6915622549267792262L;
	private boolean loggedIn = false;
	private Mainframe frame = null;
	private TwistClient client = null;
	
	private EventListenerList listenerList = new EventListenerList();
	
	public LoginPanel(TwistClient client, Mainframe frame){
		this.client = client;
		this.frame = frame;
		Dimension size = getPreferredSize();
		size.width = 210;
		size.height = 100;
		setPreferredSize(size);
		
		setBorder(BorderFactory.createTitledBorder("Login"));
		
		JLabel usernameLabel = new JLabel("Username: ");
		JLabel passwordLabel = new JLabel("Password: ");
		final JTextField usernameField = new JTextField(10);
		final JTextField passwordField = new JTextField(10);
		
		//JButton addBtn = new JButton("Add");
		JButton loginBtn = new JButton("Login");
		JButton registerBtn = new JButton("Register");
		/*addBtn.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				String username = usernameField.getText();
				String password = passwordField.getText();
				
				String text = username + ": " + password + "\n";
				fireLoginEvent(new LoginEvent(this, text));
			}
		});*/
		loginBtn.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				String username = usernameField.getText();
				String password = passwordField.getText();
				
				//String text = username + ": " + password + "\n";
				//fireLoginEvent(new LoginEvent(this, username, password));
				if(client.login(username, password)){
					frame.setLayout(new BorderLayout());
					frame.setSize(450, 300);
					frame.setContentPane(frame.mainPanel());
					frame.sendAlert("Connesso!");
				}
				else
					frame.sendAlert("Errore!");
			}


		});
		registerBtn.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				String username = usernameField.getText();
				String password = passwordField.getText();
				
				//String text = username + ": " + password + "\n";
				//fireRegisterEvent(new RegisterEvent(this, username, password));
				if(client.register(username, password))
					frame.sendAlert("Registrato!");
            	else
            		frame.sendAlert("Errore!");			}
		});
		
		
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		
		// first column
		gc.anchor = GridBagConstraints.LINE_END;
		gc.weightx = 0.5;
		gc.weighty = 0.5;
		gc.gridx = 0;
		gc.gridy = 0;
		
		add(usernameLabel, gc);
		
		gc.gridx = 0;
		gc.gridy = 1;
		add(passwordLabel, gc);
		
		//second column
		gc.anchor = GridBagConstraints.LINE_START; 
		gc.gridx = 1;
		gc.gridy = 0;
		add(usernameField, gc);
		
		gc.gridx = 1;
		gc.gridy = 1;
		add(passwordField, gc);
		
		//final row
		gc.weighty = 10;
		//gc.fill = GridBagConstraints.BOTH;
		gc.anchor = GridBagConstraints.FIRST_LINE_END;
		gc.gridx = 0;
		gc.gridy = 2;
		add(loginBtn, gc);
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.gridx = 1;
		gc.gridy = 2;
		add(registerBtn, gc);		
	}
}
