import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


public class GamesManager implements Runnable{
	public TwistServer server;
	public ExecutorService executor;
	
	public GamesManager(TwistServer server){
		this.server = server;
	}
	
	public void run(){
		//prendo una partita per volta, controllo se è ancora valida, se non lo è la chiudo, se può essere avviata la avvio
		executor = Executors.newFixedThreadPool(TwistServer.N_MANAGERS);
		while(true){
			Set<String> set = server.gameRooms.keySet();
			for(String s : set){
				GameRoom gr = server.gameRooms.get(s);
				if(gr!=null)
				try{
					synchronized(gr){
						if(gr.checked);//System.out.println("Checked");
						//se la partita è pronta per essere avviata
						else if(gr.isReady()){
							gr.gameStarted = true;
							gr.check();
							System.out.println("La partita è pronta!");
							executor.submit(() -> {
								synchronized(gr){
									gr.setupGame();
							    }
							});
	
						}
						else if (!gr.checkValid()){
							gr.check();
							//creo un thread che avvisa gli utenti e chiude la partita
							executor.submit(() -> {
								synchronized(gr){
									gr.cancelGame();
							    }
							});
						}
					}
				}
				catch(NullPointerException e){
					System.out.println("La partita era già stata cancellata dal gestore delle risposte");
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
