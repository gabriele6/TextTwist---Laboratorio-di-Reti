import java.util.EventListener;

public interface LoginListener extends EventListener {

	public void loginEventOccurred(LoginEvent loginEvent, Mainframe frame);

	public void registerEventOccurred(RegisterEvent registerEvent);
}
