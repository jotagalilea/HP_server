import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
	private final int listenPort = 61516;
	private ArrayList<Connection> activeConnections;
	private ArrayList<User> users;
	/**
	 * Longitud máxima del buffer de datos que se manejará.
	 */
	private static final int MAX_BUFF_SIZE = 1024;
	
	
	
	public static Server getInstance(){
		if (server == null)
			server = new Server();
		return server;
	}
	
	
	private Server(){
		activeConnections = new ArrayList<>();
		users = new ArrayList<>();
		try {
			listenSocket = new DatagramSocket(listenPort);
			listenSocket.setReuseAddress(true);
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
			
			/* Pasos a seguir cuando un usuario se conecta:
			 * 
			 * 1º Comprobar si existe en el servidor. Si no existe añadirlo a la colección de usuarios.
			 * 2º 
			 * 
			 */			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void listen() {
		byte[] buffer = new byte[MAX_BUFF_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			listenSocket.receive(packet);
			/* Cuando un usuario comienza una conexión al servidor SIEMPRE debe enviar su identificador
			 * (en el caso de nuestra app se envía simplemente el nombre de usuario) y el número de bytes
			 * que ocupa. Este número de bytes están en buffer[0].
			 */
			byte nameLen = buffer[0];
			String userName = new String(buffer).substring(1, nameLen+1);
			User newUser = new User(userName, new InetSocketAddress(packet.getAddress(), packet.getPort()));
			if (!users.contains(newUser))
				users.add(newUser);
			ay
		} catch (IOException e) {
			e.printStackTrace();
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
