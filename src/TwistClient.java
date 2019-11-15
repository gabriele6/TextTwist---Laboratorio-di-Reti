
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TwistClient implements TwistClientInterface{
	
	private static InetAddress ADDRESS = null;
	private CopyOnWriteArrayList<String> requests = null;
	public String username;
	private String password;
	private boolean inGame = false;
	private String currentGame = null;
	private InviteList i = null;
    private boolean exitGame=false;
    private boolean loggedIn=false;
    
    //lista utenti da invitare durante la creazione della partita
    public ArrayList<String> users = null;
    public ArrayList<Long> scores = null;
    
    //lista invitati all'interno della partita corrente
    public ArrayList<String> invitedList;
    
    private Mainframe frame = null;
    
    //stub del server
    private TwistServerInterface stub = null;
    //stub del client
    private TwistClientInterface mystub = null;
    
    //connessione salvata
	private Socket socket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	
	//array di caratteri per la partita
	private char[] chars = null;
	
    public TwistClient(Mainframe frame) {
    	this.frame = frame;
    	requests = new CopyOnWriteArrayList<String>();
    	try {
    		//CAMBIARE!
			ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }
    
    public CopyOnWriteArrayList<String> pendingRequests(){
    	return requests;
    }
    
    public boolean login(String username, String password){
    	
    	if((username==null) || (password==null) || (username.length()<1) || (password.length()<1))
    		return false;
    	
    	try {
	    	if(!loggedIn){
	    		Registry registry = LocateRegistry.getRegistry("localhost"/*host*/, TwistServer.REGISTRY_PORT);
	
	    		//controllo di non avere già una connessione aperta
	    		if(stub==null)
	    			stub = (TwistServerInterface) registry.lookup(TwistServerInterface.exportName);
		        
	    		if(mystub==null)
	    			mystub = (TwistClientInterface) UnicastRemoteObject.exportObject(this, 0);

	        	this.username = username;
	            this.password = password;
	        	
	        	String res = null;
				res = stub.login(username, password, mystub);
	        	if(res.equals("OK")){
	        		System.out.println("connesso!");
	        		loggedIn=true;
	        		this.i = new InviteList(username);
	        		this.username=username;
	        		return true;}
	        	else{
	        		System.out.println("NON connesso!");
	        		return false;}
	        }
	        else {
	        	System.out.println("effettua logout prima di fare un altro login!");
	        	return false;
	        }
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		}
	    return false;
    }
    
    public boolean register(String username, String password){
    	
    	if((username==null) || (password==null) || (username.length()<1) || (password.length()<1))
    		return false;
    	
    	try {
    		Registry registry = LocateRegistry.getRegistry("localhost"/*host*/, TwistServer.REGISTRY_PORT);

    		if(stub==null)
    			stub = (TwistServerInterface) registry.lookup(TwistServerInterface.exportName);
	        
    		if(mystub==null)
    			mystub = (TwistClientInterface) UnicastRemoteObject.exportObject(this, 0);
	        
			if(stub.register(username, password))
				System.out.println("Registrato!");
			else
				System.out.println("NON registrato!");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
    	return true;
    }
    
    public boolean logout(){
    	if(!this.loggedIn)
    		return false;
    	    	
    	try {
			if (stub.logout(this.username, this.mystub)){
		    	this.loggedIn = false;
		    	this.mystub = null;
		    	this.stub = null;
		    	this.invitedList = null;
		    	this.requests = new CopyOnWriteArrayList<String>();
		    	this.clearConnection();
		    	System.out.println("Disconnesso dal server.");}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        //in.close();
    	try {
			UnicastRemoteObject.unexportObject(this, false);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
    	return !this.loggedIn;
    }
    
    public boolean startGame(String user, ArrayList<String> invited){
    	if((user==null)||(invited==null))
    		return false;
    	if(invited.isEmpty())
    		return false;
    	
    	Boolean response = false;
    	//apro connessione TCP con il server
    	try{
    		Socket socket = new Socket(TwistClient.ADDRESS, TwistServer.SOCKET_PORT);
        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        	//genero il pacchetto da inviare
    		InviteList.Operation op = InviteList.Operation.NEWMATCH;    		
    		InviteList packet = new InviteList(user, invited);
    		packet.stub = this.mystub;
    		
    		//invio il pacchetto
    		out.writeObject(op);
    		out.writeObject(packet);
    		out.flush();
    		
    		//attendo la risposta del server
    		
    		response = in.readBoolean();
    		
    		//se la partita è avviata salvo la connessione nel client
    		if(response.booleanValue())
    			saveConnection(socket, in, out);
    		
    		System.out.print("Partita creata: " + response);
    	}
    	catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	if(response)
    		this.currentGame = this.username;
    	return response;
    }
    
    public boolean getRanking(){
    	int nUsers = 0;
    	//apro connessione TCP con il server
    	try(Socket socket = new Socket(TwistClient.ADDRESS, TwistServer.SOCKET_PORT);
    	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());){
    		
    		//invio la richiesta al server
    		InviteList.Operation op = InviteList.Operation.RANKING;    		
    		out.writeObject(op);
    		
    		//attendo la risposta del server
    		nUsers = in.readInt();
    		users = (ArrayList<String>) in.readObject();
    		scores = (ArrayList<Long>) in.readObject();
    		if(users.size()!=nUsers)
    			return false;
    		if(scores.size()!=nUsers)
    			return false;
    		return true;
    	}
    	catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

    	return false;
    }
    
    public static void main(String[] args) {
    	//String host = (args.length < 1) ? null : args[0];
        try {
        	//CREO LA GUI
        	SwingUtilities.invokeLater(new Runnable() {
    			public void run(){
    				Mainframe frame = new Mainframe("TextTwist");
    				frame.setSize(245, 140);
    				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    				frame.setVisible(true);
    				frame.setContentPane(frame.loginPanel());
    				/*
    				//ASSOCIO LE FUNZIONI AI TASTI PREMUTI
    				((LoginPanel)(frame.getPanel())).addLoginListener(new LoginListener(){

    					//UTENTE CLICCA LOGIN
    					public void loginEventOccurred(LoginEvent loginEvent, Mainframe frame) {
    						if(client.login(loginEvent.getUsername(), loginEvent.getPassword())){
        						frame.setSize(500, 400);
        						frame.setContentPane(frame.mainPanel());
    							;//ALERT O MESSAGGIO CON ESITO POSITIVO
    						}
    						else
    							;//ALERT O MESSAGGIO CON ESITO NEGATIVO

    					}

						//UTENTE CLICCA REGISTER
    					public void registerEventOccurred(RegisterEvent registerEvent){
    		            	if(client.register(registerEvent.getUsername(), registerEvent.getPassword()))
    		            		; //MESSAGGIO SUCCESSO
    		            	else
    		            		; //MESSAGGIO ERRORE
    		        	}
    					
    					//UTENTE CLICCA LOGOUT
    					public void logoutEventOccurred(LogoutEvent logoutEvent){
    		            	if(client.logout()){
    		            		frame.setContentPane(frame.loginPanel(frame));
    		            		; //MESSAGGIO SUCCESSO
    		            	}
    		            	else
    		            		; //MESSAGGIO ERRORE

    					}
    					/*
    					public void exitEventOccurred(ExitEvent exitEvent){
    		            	System.out.println("Uscendo...");
    		            	exitGame = true;
    					}
    					
    					public void newMatchEventOccurred (MatchEvent matchEvent){
    		            	ArrayList<String> array = new ArrayList<String>();
    		            	
    		            	if(loggedIn) System.out.println("Nuova partita:\r\nInserisci gli username degli invitati ('END' per terminare):");
    		            	while(loggedIn){
    		            		String command = in.readLine();
    		            		if(!command.equalsIgnoreCase("END")){
    		            			array.add(command);
    		            		}
    		            		else break;
    		            	}
    		            	if(!loggedIn)
    		            		System.out.println("Devi connetterti per avviare una partita!");
    		            	else{
    			            	System.out.println("Avvio partita con gli utenti " + array.toString() + " in corso...");
    			            	InviteList i = new InviteList(username, array);
    			            	+
    			            	//avvio la partita con la lista di username presa
    		            	}

    					}*/

    				//});
    			}
    		});

            

            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
	public boolean acceptGame(String username){
    	try{
    		Socket socket = new Socket(TwistClient.ADDRESS, TwistServer.SOCKET_PORT);
        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    		//invio la richiesta al server
    		InviteList.Operation op = InviteList.Operation.RESPONSE;    		
    		out.writeObject(op);
    		out.writeBoolean(true);
    		out.writeObject(username); //il nome utente del creatore della stanza
    		out.writeObject(this.username); //il nome utente del client che accetta
    		out.flush();
    		Boolean b = in.readBoolean();
    		if(b.booleanValue())
    			System.out.print("Partita accettata!");
    		else{
    			System.out.print("Non posso accettare la richiesta!");
    			return false;}
    		this.saveConnection(socket, in, out);
    		this.currentGame = username;
    		
    		for (String s : this.requests){
    			System.out.print("\n" + s + "\n");
    			if(!s.equals(username))
    				rejectGame(s);}
    		
    		this.requests = new CopyOnWriteArrayList<String>();
    		this.requests.add(username);
    		this.inGame=true;
    		
    	} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	
    	return true;
	}
	
	public void rejectGame(String username) {
    	try(Socket socket = new Socket(TwistClient.ADDRESS, TwistServer.SOCKET_PORT);
    	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());){
    		this.requests.remove(username);
    		
    		//comunico il rifiuto al server
    		InviteList.Operation op = InviteList.Operation.RESPONSE;    		
    		out.writeObject(op);
    		out.writeBoolean(false);
    		out.writeObject(username); //il nome utente del creatore della stanza
    		out.writeObject(this.username); //il nome utente del client che rifiuta
    		Boolean b = in.readBoolean();
    		if(b.booleanValue())
    			System.out.print("Partita rifiutata!");
    	} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean getChars() {
		try {
			//leggo un intero e n caratteri
			int n = in.readInt();
			chars = new char[n+1];
			for(int i=0; i<n-1; i++){
				chars[i]=in.readChar();
				System.out.println(chars[i]);
				}
			System.out.println("Caratteri ricevuti: " + new String(chars));
			if(chars!=null)
				if(chars.length > 0)
					return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void saveConnection(Socket socket, ObjectInputStream in, ObjectOutputStream out){
		this.socket  = socket;
		this.in = in;
		this.out = out;
		this.inGame = true;
	}
	public void clearConnection(){
		this.socket  = null;
		this.in = null;
		this.out = null;
		this.inGame = false;
	}
	
	@Override
	public boolean isAlive() throws RemoteException {
		return true;
	}

	@Override
	public void ping() throws RemoteException {
	}

	@Override
	public void notify(String username) throws RemoteException {
		//se ho chiesto io la partita
		if(this.inGame)
			return;
		if(username.compareTo(this.username)==0)
			;//this.acceptGame(username);
		
		else if(!requests.contains(username))
			requests.add(username);
		//frame.sendAlert(username);
		frame.refresh();
		System.out.println(username + " ti ha invitato a giocare!");
		
	}
	
	@Override
	public void refresh() throws RemoteException{
		frame.refresh();
	}

	@Override
	public void gameFailed(String creator) throws RemoteException {
		//se la partita fallita non è la corrente non fare niente
		System.out.println("Notifica eliminazione partita di " + creator);
		if(this.currentGame==null)
			this.requests.remove(creator);
		else if(!this.currentGame.equals(creator)){
			this.requests.remove(creator);
			System.out.println("Partita di " + creator + " annullata.");
		}
		else{
			this.requests.remove(creator);
			this.currentGame = null;
			System.out.println("La partita avviata è stata annullata!");
			if(this.inGame){
				this.frame.panel = this.frame.mainPanel();
				this.frame.setCurrentPanel();
			}
			this.inGame=false;
		}
	}
}

