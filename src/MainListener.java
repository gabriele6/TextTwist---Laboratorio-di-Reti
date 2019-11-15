import java.util.EventListener;

public interface MainListener extends EventListener {
	public void logoutEventOccurred(LogoutEvent logoutEvent);
}
