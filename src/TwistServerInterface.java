import java.rmi.Remote;
import Client.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface TwistServerInterface extends Remote{
	public static final String exportName = "TwistServerInterface";
    public static enum StartResult {ERRNULL, ERRINGAME, ERRINVGAME, ERROFFLINE, ERRUSER, ERRNOUSER, ERRNOLIST, ERRCONN, OK};
    public static enum LoginResult {ERR_NULL, ERR_NO_USER, ERR_LOGGED, ERR_INV_PASS, ERR_CONN, OK};

	
	public boolean register(String username, String password) throws RemoteException;
	public String login(String username, String password, TwistClientInterface stub) throws RemoteException;
	public boolean logout(String username, TwistClientInterface stub) throws RemoteException;
	public String testConnection() throws RemoteException;
	public String testHash(String password) throws RemoteException;
}
