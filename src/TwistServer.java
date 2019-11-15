import java.awt.List;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import Client.*;

public class TwistServer implements TwistServerInterface{
	
	public final static	int PORT = 2000;
	public final static	int REGISTRY_PORT = 1515;
	public final static	int SOCKET_PORT = 1600;
	public final static int BLOCK_SIZE = 1024;
	public final static int MAX_USER_LENGTH = 10;
	private static final int N_THREADS = 32;
	public static int N_MANAGERS = 16;
	private static final int N_CHARS = 20;
	//public static int MAXTIME = 7*1000*60; //7 minuti
	public static int MAXTIME = 1*1000*60; //1 minuto
	private static int SALTLENGTH = 10;
    public static enum StartResult {ERRNULL, ERRINGAME, ERRINVGAME, ERROFFLINE, ERRUSER, ERRNOUSER, ERRNOLIST, ERRCONN, OK};
    public static enum LoginResult {ERR_NULL, ERR_NO_USER, ERR_LOGGED, ERR_INV_PASS, ERR_CONN, OK};
	
	private int userCount = 0;
	private ConcurrentHashMap<String, User> usersList;
	//private ArrayList<User> keepAliveArray;
	public ConcurrentHashMap<String, GameRoom> gameRooms = null;
	private ArrayList<String> dictionary = null;
	
	public TwistServer(){
		//LEGGE IL FILE DI CONFIGURAZIONE!
		
		//inizializza il server
		//crea lista vuota di utenti
		usersList = new ConcurrentHashMap<String, User>();
		//keepAliveArray = new ArrayList<User>();
		gameRooms = new ConcurrentHashMap<String, GameRoom>();
		dictionary = new ArrayList<String>();
		
		//leggo il file utenti
		JSONParser parser = new JSONParser();
    	try(FileReader registro = new FileReader("users.json");){
    		JSONArray array = (JSONArray) parser.parse(registro);
    		System.out.println("NAME, SCORE, PASSWORD");
    		for (Object jo: array){
    			usersList.put(User.fromJson((JSONObject)jo).username, User.fromJson((JSONObject)jo));
    			//Set<String> set = usersList.keySet();
    			User us = User.fromJson((JSONObject)jo);
    			System.out.println(usersList.get(us.username).getName() + " " +usersList.get(us.username).getScore() + " " + usersList.get(us.username).getHashedPass());
    		}
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	//carico in memoria il dizionario
    	String fileName = "dictionary.txt";

		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			stream.forEach(dictionary::add);
			//dictionary.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean register(String username, String password) throws RemoteException{
		if((username!=null) && (password!=null)){
			
			System.out.println(username);

			//se il nome utente è troppo corto o troppo lungo non registrarlo
			if(username.length()<1)
				return false;
			if(username.length()>MAX_USER_LENGTH)
				return false;
			
			//se l'username non è già registrato
			User user = findUser(username);
			if(user==null){
				user = new User(username);
				
				//applico SHA-256 alla password
				MessageDigest digest;
				try {
					digest = MessageDigest.getInstance("SHA-256");
					
					//genero casualmente il salt
					char[] s = generateCharacters(SALTLENGTH);
					StringBuilder strBuilder = new StringBuilder();
					for (int i = 0; i < s.length; i++) {
					   strBuilder.append(s[i]);
					}
					String salt = strBuilder.toString();

					//System.out.println(salt);
					//aggiungo il salt alla password e genero l'hash
					password.concat(salt);
					byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
					user.setPassword(convertBytes(hash));
					user.setSalt(salt);
					usersList.put(user.username, user);
					userCount++;
					
					//aggiorno il file quando si aggiungono utenti
					fileWriteJson();
					
					return true;
				}
				catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
		else return false;
	}
	
	public String login(String username, String password, TwistClientInterface stub) throws RemoteException{
		//se user è nel database connettilo
		if((username!=null) && (password!=null)){
			String userPass;
			//userPass = password utente prelvata dal database
			User user = findUser(username);
			if(user==null)
				return new String("ERR_NO_USER");
			synchronized(user){		
				if(user.isLoggedIn())
					return new String("ERR_LOGGED");
				
				MessageDigest digest;
				try {
					digest = MessageDigest.getInstance("SHA-256");
					//aggiungo il salt alla password e calcolo l'hash
					password.concat(user.getSalt());
					byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
					userPass = user.getHashedPass();
					System.out.println("user: " + user.getName());
					System.out.println("user password: " + userPass);
					System.out.println("given password: " + convertBytes(hash));
					if(userPass.compareTo(convertBytes(hash))==0){
						user.setStub(stub);
						//lock acquire
						//keepAliveArray.add(user);
						user.login();
						//System.out.println(keepAliveArray);
						return new String("OK");
					}
					else
						return new String("ERR_INV_PASS");
					
				}
				catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return new String("ERR_CONN");
	
				}
			}
		}
		return new String("ERR_NULL");
	}
	
	public boolean logout(String username, TwistClientInterface stub) throws RemoteException{
		User user = findUser(username);
		if(user==null) return false;
		
		synchronized(user){
			if ((user.getStub()!=null)&&(user.getStub().toString().equals(stub.toString()))){
				if(user.inGame)
					user.inGame = false;
				for (GameRoom gr : user.gamesList)
					try{
					synchronized(gr){
						boolean ready = gr.isReady();
						if(!ready)
							this.reject(gr.creator.username, user.username);
							
						user.gamesList.remove(gr);
					}
					}catch(NullPointerException e){}

				user.logout();
				//keepAliveArray.remove(user);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String testConnection() throws RemoteException{
		String out = new String("Connected.");
		return out;
	}
	
	public String testHash(String password) throws RemoteException{
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			String hash = convertBytes(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
			return hash;
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private User findUser(String username){
		if(username==null)
			return null;
		
		User user = null;
		//scorre la lista per trovare l'utente
		Set<String> set = usersList.keySet();
		for (String s : set){
			if(username.compareTo(s)==0)
				return usersList.get(s);
		}
		return user;
	}
	
	public void endGame(GameRoom game){
		
	}
	
	public String startGame(String username, TwistClientInterface stub, ArrayList<String> array, 
										ObjectInputStream in, ObjectOutputStream out, Socket socket) 
										throws RemoteException {
		
		if((username==null) || (stub==null) || (array==null))
			return new String("ERRNULL");
		
		CopyOnWriteArrayList<User> partecipants = new CopyOnWriteArrayList<User>();
		boolean isSomeoneOffline = false;
		
		//se l'utente ha già una partita avviata
		if(gameRooms.containsKey(username))
			return new String("ERRINGAME");
		
		//se l'utente esiste ed è colui che ha avviato la partita, e se gli altri utenti esistono e sono connessi
		User user = findUser(username);
		System.out.println("User: " + user);
		GameRoom game;
		
		if(user!=null){
			
			//se l'array invitati contiene il creatore, rimuovilo
			while(array.contains(username))
				array.remove(username);
			//se dopo la rimozione l'array è vuoto, errore
			if(array.size()==0)
				return new String("ERRLIST");
			
			if(user.getStub().toString().compareTo(stub.toString())==0){
				System.out.println("User esiste");
				
				synchronized(user){
					user.setConnection(socket, in, out);}
				
				partecipants.add(user);
				for(String friend : array){
					User f = findUser(friend);
					if(f==null)
						return new String("ERRINVNAME");
					else if(f.inGame)
						return new String("ERRINGAME");
					else if(f.isLoggedIn())
						partecipants.add(f);
					else
						isSomeoneOffline = true;
				}
				if(isSomeoneOffline)
					return new String("ERROFFLINE");
				
				//ho l'array degli utenti, creo la stanza
				System.out.println("Provo a creare la stanza con user = " + user.username + ", partecipanti = " + partecipants + " (" + partecipants.get(0).username + ", " + partecipants.get(1).username + ")");
				game = new GameRoom(user, partecipants, this);
				//acquisisco la lock della partita e la aggiungo alle lista, così il gestore delle partite non troverà uno stato incompleto
				synchronized(game){
					System.out.println("Stanza creata!");
					game.setTimer(System.currentTimeMillis());
					game.generateString(N_CHARS);
					System.out.println("Stringhe generate!");
					game.creator.inGame = true;
					gameRooms.put(game.creator.username, game);
					try{
						//sono tutti online, notifico a tutti gli utenti la richiesta
						for(User u : partecipants){
							u.gamesList.add(game);
							//u.inGame=true;
							System.out.println("Provo a notificare!");
							u.getStub().notify(username);
							System.out.println("Notificato!");
						}
					}catch(RemoteException e){
						gameRooms.remove(game.creator.username);
						for(User u : game.users){
							u.gamesList.remove(game);
						}
						return new String("ERRCONN");
					}
				}
			}
			else{
				System.out.println("Stub: " + user.getStub() + "\nStub: " + stub);
				return new String("ERRUSER");}
		}
		else
			return new String("ERRNOUSER");
		
		if(partecipants.size()<=1)
			return new String("ERRNOLIST");
		
		//rifiuto tutti gli inviti ricevuti prima
		synchronized(user){
			for(GameRoom gr : user.gamesList)
				if(gr!=game)
					this.reject(gr.creator.username, user.username);
		}
		return new String("OK");
	}
	
	public boolean accept(String creator, String user, ObjectInputStream in, ObjectOutputStream out, Socket socket) {
		//cerco la partita, se la trovo cerco l'utente, se lo trovo gli assegno la connessione
		GameRoom gr = gameRooms.get(creator);
		User u = findUser(user);

		if(gr != null){
			try{
				if(u!=null){
					synchronized(u){
						synchronized(gr){
							if(gr.users.contains(u)){
								u.inGame = true;
								u.setConnection(socket, in, out);
								System.out.println("Partita accettata da " + user);
							
								//gr.checked = false;
								int index = gr.users.indexOf(u);
								gr.flags.remove(index);
								gr.flags.add(index, new Boolean(true));
	        					/*if(u.gamesList.size()>1)
        							rejectAllGames(u.username);*/
							}
							return true;
						}
					}
				}
				else
					System.out.println("L'utente non esiste!");
			}
			catch(NullPointerException e){
				System.out.println("La partita era già stata cancellata prima della accept!");
			}
		}
		else
			System.out.println("Partita nulla!");
		return false;
	}
	
	//setto il flag 
	public void reject(String creator, String user) {
		GameRoom gr = gameRooms.get(creator);
		if(gr != null){
			//try-catch perché il gestore della partita potrebbe già averla eliminata
			try{
				synchronized(gr){
					User u = findUser(user);
					//se ho trovato la partita giusta
					if(gr.creator.username.compareTo(creator)==0)
						if(gr.users.contains(u))
							//se la partita non è stata ancora avviata annullala
							if(!gr.gameStarted){
								gr.removeUser(u);
								gr.cancelGame();
								gameRooms.remove(gr);
							//se è già stata avviata rimuovi l'utente ma continuala
							}else{
								gr.removeUser(u);
							}
				}
			}
			catch(NullPointerException e){
				System.out.println("La partita era già stata eliminata");}
		}
	}
	
	public void rejectAllGames(String user){
		User u = findUser(user);
		for(GameRoom gr : u.gamesList)
			//try-catch perché il gestore della partita potrebbe già averla eliminata
			try{
				synchronized(gr){
					if(gr.users.contains(u)){
						//u.getStub().refresh();
						if (gr.isReady())
							gr.removeUser(u);
						else
							this.reject(gr.creator.username, u.username);
						gameRooms.remove(gr);}
				}
			}
			catch(NullPointerException e){
				System.out.println("La partita era già stata eliminata");
		}
	}
	
	public boolean sendChars(String creator, String user) {
		User u = this.findUser(user);
		if(u!=null)
			synchronized(u){
				GameRoom gr = gameRooms.get(creator);
				if(gr!=null)
					try{
						synchronized(gr){
							System.out.println(new String(gr.chars));
							u.out.writeInt(gr.chars.length);
							for(int i=0; i<gr.chars.length; i++)
								u.out.writeChar(gr.chars[i]);
							u.out.flush();
						}
					}
					catch(NullPointerException | IOException e){
						return false;
					}
			}
		return true;
	}

	
	public ArrayList<String> getRanking(){
		//metto gli utenti in un arraylist
		synchronized(usersList){
			ArrayList<String> values = new ArrayList<String>(usersList.keySet());
			//ordino l'array secondo il punteggio degli utenti
			Collections.sort(values, new Comparator<String>() {
			  public int compare(String a, String b) {
			    //calcolo il punteggio più alto dei due utenti
			    return (int) (usersList.get(a).getScore() - usersList.get(b).getScore());
			  }
			});
			return values;
		}
	}
	
	public ArrayList<Long> getScores(ArrayList<String> users){
		ArrayList<Long> scores = new ArrayList<Long>();
		for (String s : users){
			scores.add(usersList.get(s).getScore());
		}
		return scores;
	}
	public String getWord(){
		return dictionary.get((int) (Math.random()*dictionary.size()));
	}
	public String getWord(int n){
		return dictionary.get(n);
	}
 	
    public static void main(String args[]) {
    	final TwistServer obj = new TwistServer();

        try {
            TwistServerInterface stub = (TwistServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            
            registry.rebind(TwistServerInterface.exportName, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        
        ExecutorService connectionsHandler = Executors.newFixedThreadPool(N_THREADS);
        ExecutorService gamesManager = Executors.newSingleThreadExecutor();
        //creo un handler per le connessioni UDP
        
        ExecutorService keepAliveThread = Executors.newSingleThreadExecutor();
        GamesManager gm = new GamesManager(obj);
        gamesManager.submit(gm);
        
        //thread keepalive (controlla periodicamente che gli utenti siano ancora connessi)
        keepAliveThread.submit(() -> {
        	while(true){
	        	Set<String> set = obj.usersList.keySet();
	        	
	        	if(set.size()==0)
	        		System.out.println("Nessun utente connesso");
	        	
	        	for (String s : set){
	        		User u = obj.usersList.get(s);
	        		try{
        				try{
        					u.getStub().ping();
        					System.out.println("Utente " + u.username + " online");
        				}
        				catch(RemoteException e){
        					System.out.println("Utente " + u.username + " non raggiungibile!");
        					//se è in partita rimuovilo
        					obj.logout(u.username, u.getStub());/*
        					if(u.inGame)
        						try{
        							System.out.println("Prendo la lock dell'utente " + u.username);
        							GameRoom gr = u.gamesList.get(0);
	        						synchronized(u){
	        							System.out.println("Prendo la lock della partita");
	        							synchronized(gr){
	        								System.out.println("Lock prese");
		        							//se tutti hanno accettato rimuovo l'utente e gli altri continuano a giocare
		        							gr.removeUser(u);
		        							u.inGame = false;
		        							if(!gr.isReady()){
			        							gr.cancelGame();}
		        							u.gamesList.clear();
	        							}
	        						}
        						}
        						catch(NullPointerException exception){
        							System.out.println("Oh shit");
        						}
        					
        					//se ha richieste in sospeso rifiutale e rimuovile
        					synchronized(u){
	        					if(u.gamesList.size()>0)
        							obj.rejectAllGames(u.username);
	        					
	        					//disconnettilo
	        					u.gamesList.clear();
	        					u.logout();
        					}*/
        				}
	        		}catch(NullPointerException e){
	        			System.out.println("Oh shit 2");
	        		}
	        	}
	        	
	        	//aspetta 10 secondi
	        	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
		});

        //handler delle connessioni
        try(ServerSocket socket = new ServerSocket(SOCKET_PORT)){
        	while(true){
        		System.out.println("Aspetto un client via TCP.");
        		Socket client = socket.accept();
        		System.out.println("Client TCP arrivato.");
        		RequestHandler handler = new RequestHandler(client, obj);
        		connectionsHandler.submit(handler);
        	}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        //accetto le connessioni TCP in ingresso
   		/*try(ServerSocketChannel server = ServerSocketChannel.open()){
   			server.bind(new InetSocketAddress(InetAddress.getLocalHost(), TwistServer.PORT));
   			Selector selector = Selector.open();
   			server.configureBlocking(false);
   			server.register(selector, SelectionKey.OP_ACCEPT);
   			
   			while(true){
   				selector.selectedKeys().clear();
   				selector.select();
   				for(SelectionKey key : selector.selectedKeys()){
   					if(key.isAcceptable()){
   						try{
   							SocketChannel client = ((ServerSocketChannel)key.channel()).accept();
   							System.out.println("Client request received.");
   							client.configureBlocking(false);
   							ByteBuffer[] attachments = new ByteBuffer[2];
   							attachments[0] = ByteBuffer.allocate(Integer.BYTES);
   							attachments[1] = ByteBuffer.allocate(TwistServer.BLOCK_SIZE);
   							client.register(selector, SelectionKey.OP_READ, attachments);
   							System.out.println("Client request accepted.");
   						}
   						catch(IOException e){
   							System.out.println("Error accepting client request: " + e.getMessage());
   						}

   						if(key.isReadable()){
   							try{
   								SocketChannel client = (SocketChannel) key.channel();
   								ByteBuffer[] buffers = (ByteBuffer[]) key.attachment();
   								long response = client.read(buffers);
   								
   								if(response == -1){
   									client.close();
   									key.cancel();
   									continue;
   								}
   								
   								if(!buffers[0].hasRemaining()){
   									buffers[0].flip();
   									int length = buffers[0].getInt();
   									if(length == buffers[1].position()){
   										
   										//controllo se ho letto tutto
   										
   										//operazioni da fare dopo la ricezione del messaggio
   										//prelevo l'operazione
   										int op = buffers[1].getInt(0);
   										String msg = new String(buffers[1].array(), 4/*sizeof int, buffers[1].position());
   										
   										System.out.println("Received OP " + op + "and string " + msg);
   										
   										switch(op){
   										case 0:
   											break;
   										case 1:
   											break;
   										case 2:
   											break;
   										}

   									}
   								}
   							}
   							catch(IOException e){
   								key.cancel();
   							}
   						}
   						
   						if(key.isWritable()){
   							
   						}
   					}
   				}
   			}
   		}
        catch(IOException e){
        	System.err.println("Error: " + e.getMessage());
        }*/
    }
    
	public char[] generateCharacters(int n){
		char[] chars = new char[n];
		char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		int position;
		for (int i=0; i<n; i++)
			chars[i]=alphabet[(int) (Math.random()*26)];
		return chars;
	}
    
    //converte da byte[] a String
    private static String convertBytes(byte[] array) {
        StringBuffer result = new StringBuffer();
        for (byte b : array) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
    
    private synchronized void fileWriteJson(){
    	JSONArray jUsers= new JSONArray();
    	Set<String> set = usersList.keySet();
    	for (String s : set){
    		jUsers.add(usersList.get(s).toJson());
    	}
    	try(FileWriter registro= new FileWriter("users.json");){
    		jUsers.writeJSONString(registro);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
