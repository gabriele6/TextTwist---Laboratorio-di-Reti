

import java.util.EventObject;

public class RegisterEvent extends EventObject {
	private String username;
	private String password;
	public RegisterEvent(Object source, String username, String password){
		super(source);
		this.username = username;
		this.password = password;
	}
	public String getUsername(){
		return username;
	}
	public String getPassword(){
		return password;
	}
}
