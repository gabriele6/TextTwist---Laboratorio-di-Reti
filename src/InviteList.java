import java.io.Serializable;
import java.util.ArrayList;

public class InviteList implements Serializable{

	private static final long serialVersionUID = 1L;
    public static enum Operation {NEWMATCH, RESPONSE, RANKING};
	
	public ArrayList<String> invited;
	public String user;
	public boolean accepted;
	public TwistClientInterface stub;
	
	public InviteList(String username){
		this.user=username;
		this.invited = new ArrayList<String>();
	}
	
	public InviteList(String username, ArrayList<String> invited){
		this.user=username;
		this.invited = invited;
	}
	
	public boolean newInvite(String user){
		if(user==null) 
			return false;
		if(invited.add(user))
			return true;
		return false;
	}
	
	public boolean acceptInvite(String user){
		if(user==null)
			return false;
		if(invited.indexOf(user)==-1)
			return false;

		invited = new ArrayList<String>();
		return true;
	}
	
}
