import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONObject;

public class User {
	
	public String username;
	private String passwd;
	private String salt;
	private boolean loggedIn = false;
	private long score = 0;
	private TwistClientInterface userStub = null;
	public CopyOnWriteArrayList<GameRoom> gamesList;
	public ArrayList<String> wordsInCurrentMatch = null;
	public ObjectInputStream in = null;
	public ObjectOutputStream out = null;
	public Socket socket = null;
	public boolean inGame = false;
	
	public User(String username){
		this.username=username;
		this.loggedIn = false;
		gamesList = new CopyOnWriteArrayList<GameRoom>();
	}
	
	public void setPassword(String pass){
		this.passwd = pass;}
	
	public void setScore(long l){
		this.score = l;}
	
	public void setStub(TwistClientInterface t){
		this.userStub = t;}
	
	public void setSalt(String s){
		this.salt = s;}
	
	public String getSalt(){
		return this.salt;}
	
	public boolean setConnection(Socket socket, ObjectInputStream in, ObjectOutputStream out){
		if((in == null) || (out == null) || (socket == null))
			return false;
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.inGame = true;
		return true;
	}
	public boolean resetConnection(){
		try {
			if(this.socket!=null)
				this.socket.close();		
			if(this.in!=null)
				this.in.close();
			if(this.out!=null)
				this.out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.inGame = false;
		return true;
	}
	
	public TwistClientInterface getStub(){
		return this.userStub;}
	
	public String getHashedPass(){
		return this.passwd;}
	
	public String getName(){
		return this.username;}
	
	public long getScore(){
		return this.score;}
	
	public boolean isLoggedIn(){
		return this.loggedIn;}
	
	public boolean login(){
		if (!this.loggedIn){
			this.loggedIn = true;
			return true;
		}
		return false;
	}
	
	public boolean logout(){
		if (this.loggedIn){
			this.loggedIn = false;
			this.setStub(null);
			this.resetConnection();
			return true;
		}
		return false;
	}
	
    public JSONObject toJson(){
    	synchronized(this){
	    	JSONObject Juser= new JSONObject();
	    	Juser.put("User", this.username);
	    	Juser.put("Pass", this.passwd);
	    	Juser.put("Salt", this.salt);
	    	Juser.put("Score", this.score);
	    	return Juser;
    	}
    }
    
    public static User fromJson(JSONObject object) {
    	User u = new User(
    			(String)object.get("User"));
    	u.setPassword((String)object.get("Pass"));
    	u.setScore((long)object.get("Score"));
    	u.setSalt((String)object.get("Salt"));
    	return u;
    	}
}
