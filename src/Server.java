import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase principal.
 */


public class Server {
	
	private static Server server = null;
	private DatagramSocket listenSocket;
	private DatagramSocket outSocket;
	private final int listenPort = 61516;
	
	private ArrayList<Connection> activeConnections;
	private UsersInfo usersInfo;
	
	
	
	public static Server getInstance(){
		if (server == null)
			server = new Server();
		return server;
	}
	
	
	private Server(){
		activeConnections = new ArrayList<>();
		usersInfo = new UsersInfo();
		try {
			listenSocket = new DatagramSocket(listenPort);
			listenSocket.setReuseAddress(true);
			outSocket = new DatagramSocket();
			outSocket.setReuseAddress(true);
			// TODO: ¿También se necesita una cola de espera? ¿O lanzo todos los hilos que se puedan?
			//TODO: Necesito un hilo que espere a conexiones entrantes.
			/* POR DEFECTO, TODOS LOS USUARIOS QUE SE CONECTAN SON SIRVIENTES, ya que
			 * se conectan al servidor en cuanto inician la app.
			 */
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					listen();
				}
			}).start();
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	// TODO: Debería haber varios hilos ejecutando listen para atender varias peticiones a la vez.
	private void listen() {
		while (true){
			byte[] buffer = new byte[Utils.MAX_BUFF_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				listenSocket.receive(packet);
				// TODO: REVISAR ESTE COMENTARIO, puede que no sea necesario enviar el número de bytes del nombre.
				/* Cuando un usuario comienza una conexión al servidor SIEMPRE debe enviar su identificador
				 * (en el caso de nuestra app se envía simplemente el nombre de usuario) y el número de bytes
				 * que ocupa. Este número de bytes están en buffer[0].
				 */
				
				// TODO: Habrá que distinguir qué tipo de conexión hace el cliente.
				byte requestType = buffer[0];
				switch (requestType){
				case Utils.SERVER_CONNECT:
					// Registro del usuario en el Servidor en memoria.
					byte nameLen = buffer[1];
					String userName = new String(buffer).substring(1, nameLen+1);
					//User newUser = new User(userName, packet.getAddress(), packet.getPort());
					if (!usersInfo.existsUserWithName(userName))
						usersInfo.addUser(userName, packet.getAddress(), packet.getPort());
					break;
					
				/* Saludo a un dispositivo. Si está en la lista de usuarios conectados al servidor se pasará
				 * el paquete tal cual al dispositivo de destino, si no se devolverá NO_FRIEND.
				 */
				case Utils.HELLO:
					byte friendNameLen = buffer[1];
					String friendName = new String(buffer).substring(2, 2+friendNameLen);
					// Se comprueba si el usuario destino está conectado con su nombre:
					if (usersInfo.existsUserWithName(friendName)){
						// En caso afirmativo se pasa el paquete a su destino.
						Pair<InetAddress, Integer> info = usersInfo.getUserInfo(friendName);
						DatagramPacket outPack = new DatagramPacket(buffer, buffer.length, info.first, info.second);
						outSocket.send(outPack);
					}
					else{
						// Si no se devuelve respuesta negativa al origen con NO_FRIEND.
						byte[] refuseBuff = {Utils.NO_FRIEND};
						DatagramPacket p = new DatagramPacket(refuseBuff, refuseBuff.length, packet.getAddress(), packet.getPort());
						outSocket.send(p);
					}
					break;
					
				// Si los pares son amigos se crea un objeto Connection.
				case Utils.HELLO_FRIEND:
					
					break;
				// Si no son amigos 
				case Utils.NO_FRIEND:
					break;
				// Si es una petición se pasa al destino tal cual.
				default:
					break;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
		}
		}
	}
	
	
	private void createConnection(User client, User servant){
		Connection
		//crear socket
	}
	
	
	public int getListenPort(){
		return this.listenPort;
	}
	
}
