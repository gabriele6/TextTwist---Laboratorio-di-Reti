import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TwistClientInterface extends Remote{
	public boolean isAlive() throws RemoteException;
	public void ping() throws RemoteException;
	public void notify(String username) throws RemoteException;
	public void gameFailed(String creator) throws RemoteException;
	void refresh() throws RemoteException;
}
