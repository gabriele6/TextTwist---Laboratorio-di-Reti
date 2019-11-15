import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class RequestHandler implements Runnable{
	private Socket client;
	private TwistServer server;
	
	public RequestHandler(Socket client, TwistServer server) {
		this.client = client;
		this.server = server;
	}

	@Override
	public void run() {
		try{
			//non posso usare il try con risorse perché devo salvare la connessione
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			
			InviteList.Operation op = (InviteList.Operation) in.readObject();
			if (op==null) return;

			//richiesta partita
			if(op == InviteList.Operation.NEWMATCH){
				InviteList il = (InviteList) in.readObject();
				String result;
				if(il==null) return;
				//ACQUISISCI LOCK AVVIO-PARTITA
				System.out.println("Richiesta:\nUsername: " + il.user + " invitati:" + il.invited + " \nUser stub: " + il.stub);
				result = server.startGame(il.user, il.stub, il.invited, in, out, client);
				
				System.out.println("Funzione richiamata!\nRisultato: " + result);
				if(result.equals("OK")){
					out.writeBoolean(new Boolean(true));
					server.sendChars(il.user, il.user);
				}
				else
					out.writeBoolean(new Boolean(false));
				//potrei anche inviare una stringa per segnalare cosa di preciso è andato storto
				out.flush();
			}
			
			//accetta/rifiuta invito
			else if(op == InviteList.Operation.RESPONSE){
				//se accetta
				Boolean il = in.readBoolean();
				if(il){
					String creator = (String) in.readObject();
					String user = (String) in.readObject();
					boolean result = false;
					if((creator!=null) && (user!=null))
					//modifica il flag nella partita
						result = server.accept(creator, user, in, out, client);
					System.out.println("Accetto: " + creator + " " + user + "\nRisultato: " + result);
					out.writeBoolean(new Boolean(result));
					//se esito positivo invia i caratteri della partita
					if(result)
						server.sendChars(creator, user);
					out.flush();
				}
				else{
					//elimina la partita
					String creator = (String) in.readObject();
					String user = (String) in.readObject();
					server.reject(creator, user);
					out.writeBoolean(true);
					out.flush();
					in.close();
					out.close();
					System.out.println("Rifiuto: " + creator + " " + user);
					client.close();
				}
			}
			
			//richiesta classifica
			else if(op == InviteList.Operation.RANKING){
				//ordino la lista utenti e la spedisco via TCP
				ArrayList<String> hs = server.getRanking();
				ArrayList<Long> is = server.getScores(hs);
				out.writeInt(hs.size());
				out.writeObject(hs);
				out.writeObject(is);
				out.flush();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
