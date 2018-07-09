import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase principal.
 */


public class Server {
	
	private static Server server = null;
	private DatagramSocket listenSocket;
	//private DatagramSocket outSocket;
	private final int listenPort = 61516;
	
	//private ArrayList<Connection> activeConnections;
	private UsersInfo usersInfo;
	
	
	
	public static Server getInstance(){
		if (server == null)
			server = new Server();
		return server;
	}
	
	
	private Server(){
		//activeConnections = new ArrayList<>();
		usersInfo = new UsersInfo();
		try {
			listenSocket = new DatagramSocket(listenPort);
			listenSocket.setReuseAddress(true);
			//outSocket = new DatagramSocket();
			//outSocket.setReuseAddress(true);
			// TODO: ¿También se necesita una cola de espera? ¿O lanzo todos los hilos que se puedan?
			// TODO: Necesito un hilo que espere a conexiones entrantes.
			
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
				/* Cuando un usuario comienza una conexión al servidor SIEMPRE debe enviar su identificador
				 * (en el caso de nuestra app se envía simplemente el nombre de usuario) y el número de bytes
				 * que ocupa. Este número de bytes están en buffer[0].
				 */
				
				byte type = buffer[0];
				switch (type){
				case Utils.SERVER_CONNECT:
					// Registro del usuario en el Servidor en memoria.
					byte nameLen = buffer[1];
					String userName = new String(buffer).substring(2, 2+nameLen);
					//User newUser = new User(userName, packet.getAddress(), packet.getPort());
					if (!usersInfo.existsUserWithName(userName))
						usersInfo.addUser(userName, packet.getAddress(), packet.getPort());
					break;
					
				/* Saludo a un dispositivo. Si está en la lista de usuarios conectados al servidor se pasarán
				 * a cada uno la dirección y el puerto usados por el contrario para el futuro intercambio, si no
				 * se devolverá al origen NO_FRIEND.
				 */
				case Utils.HELLO:
					byte friendNameLen = buffer[1];
					String friendName = new String(buffer).substring(2, 2+friendNameLen);
					// Se comprueba si el usuario destino está conectado con su nombre:
					if (usersInfo.existsUserWithName(friendName)){
						/* En caso afirmativo:
						 * 1- Se avisa al origen y se pasa IP+puerto (externos) del destino.
						 * 2- Se pasa IP+puerto (externos) del destino al origen.
						 *    Ambos pasos se realizan para que se comuniquen origen y destino
						 *    de forma directa entre ellos.
						 */
						// IP+puerto del destino se manda al origen.
						Pair<InetAddress, Integer> destInfo = usersInfo.getUserInfo(friendName);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] friendIP = destInfo.first.getAddress();
						byte[] friendPort = Utils.intToByteArray(destInfo.second);
						baos.write(friendIP);
						baos.write(friendPort);
						byte[] info_for_origin = baos.toByteArray();
						DatagramPacket to_origin = new DatagramPacket(info_for_origin, info_for_origin.length,
								packet.getAddress(), packet.getPort());
						listenSocket.send(to_origin);
						//outSocket.send(to_origin);
						
						// IP+puerto del origen se manda al destino.
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						baos2.write(packet.getAddress().getAddress());
						baos2.write(Utils.intToByteArray(packet.getPort()));
						byte[] info_for_destination = baos2.toByteArray();
						DatagramPacket to_destination = new DatagramPacket(info_for_destination,
								info_for_destination.length, destInfo.first, destInfo.second);
						listenSocket.send(to_destination);
						//outSocket.send(to_destination);
						
						/*Pair<InetAddress, Integer> info = usersInfo.getUserInfo(friendName);
						DatagramPacket outPack = new DatagramPacket(buffer, buffer.length, info.first, info.second);
						outSocket.send(outPack);*/
					}
					else{
						// Si no, se devuelve respuesta negativa al origen con NO_FRIEND.
						byte[] refuseBuff = {Utils.NO_FRIEND};
						DatagramPacket p = new DatagramPacket(refuseBuff, refuseBuff.length,
								packet.getAddress(), packet.getPort());
						//outSocket.send(p);
						listenSocket.send(p);
					}
					break;
				/*
				// Si los pares son amigos se crea un objeto Connection.
				case Utils.HELLO_FRIEND:
					break;
				// Si no son amigos:
				case Utils.NO_FRIEND:
					break;
				// Si es una petición se pasa al destino tal cual, O BIEN podría hacer que se comunicaran
				// sin pasar más por el servidor, por lo que:
				// TODO: el caso default podría sobrar.
				default:
					break;
				*/
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}
