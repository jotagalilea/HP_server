import java.net.Socket;
import java.util.HashMap;

public class ClientManager {

	private static ClientManager clientManager = null;
	private HashMap<Pair<String, Boolean>, Socket> clientSockets;
	
	
	public static ClientManager getInstance(){
		if (clientManager == null)
			return new ClientManager();
		return clientManager;
	}
	
	
	private ClientManager(){
		clientSockets = new HashMap<>();
	}
	
	public void addClientSocket(String str, Socket s, boolean b){
		Pair<String, Boolean> p = new Pair<>(str, new Boolean(b));
		clientSockets.put(p, s);
	}
	
	public Socket getUserSocket(String str, boolean b){
		Pair<String, Boolean> newPair = new Pair<>(str, b);
		if (clientSockets.containsKey(newPair))
			return clientSockets.get(newPair);
		else
			return null;
	}

}
