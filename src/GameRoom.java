import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameRoom {
	static int NCHARS = 15;

	public User creator;
	public CopyOnWriteArrayList<User> users;
	public CopyOnWriteArrayList<Boolean> flags;
	public CopyOnWriteArrayList<Integer> scores;
	//ogni utente ha un array di parole
	
	public TwistServer server;
	public long time;
	public boolean gameStarted = false;
	public boolean isValid = true; //se false, qualcuno ha rifiutato o qualche utente è offline -> segnalare agli utenti e eliminare la partita
	public char[] chars = null;
	public boolean checked = false;

	public GameRoom(TwistServer server){
		super();
		this.server=server;
	}
	
	public synchronized boolean check(){
		synchronized(this){
			this.checked = true;
		}
		return this.checked;
	}
	
	public GameRoom(User creator, CopyOnWriteArrayList<User> users, TwistServer server){
		this.creator = creator;
		this.users = users;
		this.server = server;
		this.scores = new CopyOnWriteArrayList<Integer>();
		this.flags = new CopyOnWriteArrayList<Boolean>();
		this.time = System.currentTimeMillis();
		System.out.println("Provo a inizializzare l'array delle parole");
		
		System.out.println("Inizializzo i punteggi");
		for(User u : users){
			u.wordsInCurrentMatch = new ArrayList<String>();
			System.out.println("Array utente inizializzato");
			flags.add(new Boolean(false));
			System.out.println("Flag utente inizializzato");
			scores.add(new Integer(0));
			System.out.println("Punteggio utente inizializzato");
		}
		
		//il primo utente è il creatore, accetta automaticamente la partita
		User u = users.get(0);
		synchronized(u){
			u.gamesList = new CopyOnWriteArrayList<GameRoom>();
			u.inGame = true;
			flags.remove(0);
			flags.add(0, new Boolean(true));
		}
	}
	

	public boolean isReady() {
		boolean canStart = true;
		synchronized(flags){
			for(Boolean b : flags)
				if(b.booleanValue()==false)
					canStart=false;
		}
		//System.out.println(flags);
		return canStart;
	}
	
	public synchronized boolean checkValid(){
		if(isValid == false)
			return isValid;
		//se un utente è offline la partita non è valida
		for(User u : users){
			if(!u.isLoggedIn())
				isValid = false;
		}
		//se un utente non ha ancora accettato ed è scaduto il timer, la partita non è valida
		//System.out.println(System.currentTimeMillis() -  this.time);
		if(System.currentTimeMillis() > this.time + TwistServer.MAXTIME)
			synchronized(flags){
				for (Boolean b : flags)
					if(b.booleanValue()==false){
						isValid=false;
						System.out.println("Tempo scaduto!");
						break;}
			}
		return isValid;
	}
	
	public void setFlag(User user){
		if(user!=null){
			int index = users.indexOf(user);
			if(index!=-1){
				synchronized(flags){
					flags.remove(index);
					flags.add(index, new Boolean(true));
				}
			}
		}
	}
	
	public void addWord(String username, String word){
		User user = null;
		synchronized(users){
			for(User u : users)
				if(u.username.compareTo(username)==0){
					user = u;
					break;}
			if(user!=null){
				synchronized(user){
					if(!user.wordsInCurrentMatch.contains(word))
						user.wordsInCurrentMatch.add(word);
				}
			}
		}
	}
	
	public void setupGame(){
		//chars = generateString(NCHARS);
		System.out.println("Preparo la partita dell'utente " + this.creator.username + " con le lettere " + new String(chars));
		//pulisco l'array di parole per ogni giocatore
		synchronized(users){
			//invio le parole al creatore della partita
			sendChars(chars, users.get(0));
			for(User u : users){
				synchronized(u){
					u.wordsInCurrentMatch.clear();
					//sendChars(chars, u);
					//System.out.println("Ho inviato le lettere all'utente " + u.username);
				}
			}
		}
	}
	
	private boolean sendChars(char[] chars, User u) {
		synchronized(u){
			try {
				u.out.writeInt(chars.length);
				for(int i=0; i<chars.length; i++)
					u.out.writeChar(chars[i]);
				u.out.flush();
				u.resetConnection();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	public char[] generateString(int n){
		char[] array = new char[n+1];
		//prendo 2 parole casuali, ci aggiungo dei caratteri casuali per arrivare a n
		System.out.println("Provo a prendere le parole");
		
		char[] s1, s2;
		do{
			s1 = server.getWord().toCharArray();
			s2 = server.getWord().toCharArray();		
		}
		while(s1.length + s2.length >= n);
		
		System.out.println("Provo a generare caratteri rimanenti");
		char[] randomChars = generateCharacters(n - s1.length - s2.length +1);
		System.out.println("Caratteri rimanenti generati");
		
		//copio tutti i caratteri nell'array principale
		int i = 0;
		int j = 0;
		for (i=0; i<s1.length; i++)
			array[i] = s1[i];
		for (j=0; j<s2.length; j++)
			array[i++] = s2[j];
		for (j=0; j<randomChars.length-1; j++)
			array[i++] = randomChars[j];
		System.out.println(array);
		
		//"mischio" le lettere
		int index1, index2;
		for (i=0; i< n*2; i++){
			index1 = (int)(Math.random()*(n));
			index2 = (int)(Math.random()*(n));
			char c = array[index1];
			array[index1] = array[index2];
			array[index2] = c;
		}
		
		chars = array;
		return array;
	}
	
	//genera n lettere casuali
	public char[] generateCharacters(int n){
		System.out.println(n);
		char[] chars = new char[n];
		char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		int position;
		for (int i=0; i<n; i++){
			int rnd = (int) (Math.random()*26);
			System.out.println("RND: " + rnd);
			chars[i]=alphabet[rnd];
			System.out.println("Generato " + chars[i]);}
		return chars;
	}
	
	public void setTimer(long time){
		this.time = time;
	}
	
	public int calculateScore(ArrayList<String> words){
		int score = 0;
		
		for(int i=0; i<words.size(); i++){
			//if (words.get(i) appartiene al database){
				score += words.get(i).length();
		}
		return score;
	}
	
	public boolean cancelGame(){
		for (int i=0; i<users.size(); i++){
			User u = users.get(i);
			try {
				//se avevo accettato, ho salvato la socket dell'utente e quindi devo chiuerla
				//partita da annullare, chiudo le socket e notifico l'errore
				System.out.println("Notifico all'utente " + u.username + " che la partita di " + creator.username + " è stata annullata");
				u.getStub().gameFailed(creator.username);
				u.gamesList.remove(this);
				if(flags.get(i)==true)
					u.resetConnection();
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
		}
		flags=null;
		scores=null;
		server.gameRooms.remove(this.creator.username);
		return true;
	}
	
	public int endGame(){
		int n = 0;
		for (User u : users){
			synchronized(u){
				int sc = calculateScore(u.wordsInCurrentMatch);
				scores.remove(n);
				scores.add(n, sc);
				n++;
			}
		}
		n=0;
		for (User u : users){
			u.setScore(u.getScore() + scores.get(n));
			n++;
		}
		return 0;
	}

	public void removeUser(User user) {
		synchronized(users){
		synchronized(user){
			int index = this.users.indexOf(user);
			this.users.remove(index);
			this.flags.remove(index);
			this.scores.remove(index);
		}}
		
	}
}
