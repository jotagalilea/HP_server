import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase principal.
 */


public class Server {
	
	private static Server server = null;
	//private DatagramSocket listenSocket;
	private ServerSocket listenSocket;
	private Socket socket;
	//private DatagramSocket outSocket;
	private final int listenPort = ;
	
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
			//listenSocket = new DatagramSocket(listenPort);
			listenSocket = new ServerSocket(listenPort);
			listenSocket.setReuseAddress(true);
			//outSocket = new DatagramSocket();
			//outSocket.setReuseAddress(true);
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					listen();
				}
			}).start();
			
		} catch (/*SocketException*/ IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void listen() {
		while (true){
			byte[] buffer = new byte[Utils.MAX_BUFF_SIZE];
			//DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				//listenSocket.receive(packet);
				socket = listenSocket.accept();
				/* Cuando un usuario comienza una conexión al servidor SIEMPRE debe enviar su identificador
				 * (en el caso de nuestra app se envía simplemente el nombre de usuario) y el número de bytes
				 * que ocupa. Este número de bytes están en buffer[0].
				 */
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				int bufSize = dis.readInt();
				dis.readFully(buffer, 0, bufSize);
				////////////
				byte type = buffer[0];
				switch (type){
				case Utils.SERVER_CONNECT:
					// Registro del usuario en el Servidor en memoria.
					byte nameLen = buffer[1];
					String userName = new String(buffer).substring(2, 2+nameLen);
					
					if (buffer[2+nameLen] == Utils.IS_CLIENT_SOCKET){
						/* Si el usuario no está registrado se guarda la dirección del paquete recibido
						 * para tener al menos eso. Si ya está registrado se guarda la IP previamente
						 * recibida cuando se conectó el socket de la parte servidor.
						 */
						if (!usersInfo.existsUserWithName(userName))
							//usersInfo.addUser(userName, packet.getAddress(), null, packet.getPort());
							usersInfo.addUser(userName, socket.getInetAddress(), null, socket.getPort(), socket);
						else {
							InetAddress addr = usersInfo.getUserAddr(userName);
							//usersInfo.addUser(userName, addr, null, packet.getPort());
							usersInfo.addUser(userName, addr, null, socket.getPort(), socket);
						}
					}
					else {
						/* Si se conecta el socket de la parte Servidor (es decir, la primera parte
						 * que se conecta) se está recibiendo la IP de dicha parte. La IP son 4 bytes.
						 */
						String addrString = new String(buffer).substring(3+nameLen, 7+nameLen);
						if (addrString.charAt(0) == Utils.TAKE_IP_FROM_HEADER)
							//usersInfo.addUser(userName, packet.getAddress(), packet.getPort(), null);
							usersInfo.addUser(userName, socket.getInetAddress(), socket.getPort(), null, socket);
						else {
							InetAddress addr = InetAddress.getByName(addrString);
							//usersInfo.addUser(userName, addr, packet.getPort(), null);
							usersInfo.addUser(userName, addr, socket.getPort(), null, socket);
						}
					}
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
						Pair<InetAddress, Pair<Integer,Integer>> destInfo = usersInfo.getUserInfo(friendName);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] friendIP = destInfo.first.getAddress();
						byte[] friendPort = Utils.intToByteArray(destInfo.second.first);
						baos.write(friendIP);
						baos.write(friendPort);
						
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						baos.writeTo(dos);
						dos.close();
						
						/*byte[] info_for_origin = baos.toByteArray();
						DatagramPacket to_origin = new DatagramPacket(info_for_origin, info_for_origin.length,
								packet.getAddress(), packet.getPort());*/
						//listenSocket.send(to_origin);
						
						// Se envía primero al proveedor (destino) el aviso de una nueva petición:
						InetSocketAddress destAddr = new InetSocketAddress(destInfo.first, destInfo.second.first);
						socket.connect(destAddr);
						dos = new DataOutputStream(socket.getOutputStream());
						dos.writeByte(Utils.NEW_REQ);
						/*byte[] req = {Utils.NEW_REQ};
						DatagramPacket p = new DatagramPacket(req, req.length, destInfo.first, destInfo.second.first);
						listenSocket.send(p);*/
						
						// IP+puerto del origen se manda al destino.
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						baos2.write(socket.getInetAddress().getAddress());
						baos2.write(Utils.intToByteArray(socket.getPort()));
						baos2.writeTo(dos);
						dos.close();
						/*baos2.write(packet.getAddress().getAddress());
						baos2.write(Utils.intToByteArray(packet.getPort()));
						byte[] info_for_destination = baos2.toByteArray();
						DatagramPacket to_destination = new DatagramPacket(info_for_destination,
								info_for_destination.length, destInfo.first, destInfo.second.first);*/
						//listenSocket.send(to_destination);
					}
					else{
						// Si no, se devuelve respuesta negativa al origen con NO_FRIEND.
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeByte(Utils.NO_FRIEND);
						dos.close();
						/*byte[] refuseBuff = {Utils.NO_FRIEND};
						DatagramPacket p = new DatagramPacket(refuseBuff, refuseBuff.length,
								packet.getAddress(), packet.getPort());*/
						//outSocket.send(p);
						//listenSocket.send(p);
					}
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*private void listen() {
		while (true){
			byte[] buffer = new byte[Utils.MAX_BUFF_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				listenSocket.receive(packet);
				/* Cuando un usuario comienza una conexión al servidor SIEMPRE debe enviar su identificador
				 * (en el caso de nuestra app se envía simplemente el nombre de usuario) y el número de bytes
				 * que ocupa. Este número de bytes están en buffer[0].
				 */
				
		/*		byte type = buffer[0];
				switch (type){
				case Utils.SERVER_CONNECT:
					// Registro del usuario en el Servidor en memoria.
					byte nameLen = buffer[1];
					String userName = new String(buffer).substring(2, 2+nameLen);
					
					if (buffer[2+nameLen] == Utils.IS_CLIENT_SOCKET){
						/* Si el usuario no está registrado se guarda la dirección del paquete recibido
						 * para tener al menos eso. Si ya está registrado se guarda la IP previamente
						 * recibida cuando se conectó el socket de la parte servidor.
						 */
		/*				if (!usersInfo.existsUserWithName(userName))
							usersInfo.addUser(userName, packet.getAddress(), null, packet.getPort());
						else {
							InetAddress addr = usersInfo.getUserAddr(userName);
							usersInfo.addUser(userName, addr, null, packet.getPort());
						}
					}
					else {
						/* Si se conecta el socket de la parte Servidor (es decir, la primera parte
						 * que se conecta) se está recibiendo la IP de dicha parte. La IP son 4 bytes.
						 */
		/*				String addrString = new String(buffer).substring(3+nameLen, 7+nameLen);
						if (addrString.charAt(0) == Utils.TAKE_IP_FROM_PACKET)
							usersInfo.addUser(userName, packet.getAddress(), packet.getPort(), null);
						else {
							InetAddress addr = InetAddress.getByName(addrString);
							usersInfo.addUser(userName, addr, packet.getPort(), null);
						}
					}
					break;
					
				/* Saludo a un dispositivo. Si está en la lista de usuarios conectados al servidor se pasarán
				 * a cada uno la dirección y el puerto usados por el contrario para el futuro intercambio, si no
				 * se devolverá al origen NO_FRIEND.
				 */
		/*		case Utils.HELLO:
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
		/*				Pair<InetAddress, Pair<Integer,Integer>> destInfo = usersInfo.getUserInfo(friendName);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] friendIP = destInfo.first.getAddress();
						byte[] friendPort = Utils.intToByteArray(destInfo.second.first);
						baos.write(friendIP);
						baos.write(friendPort);
						byte[] info_for_origin = baos.toByteArray();
						DatagramPacket to_origin = new DatagramPacket(info_for_origin, info_for_origin.length,
								packet.getAddress(), packet.getPort());
						listenSocket.send(to_origin);
						//outSocket.send(to_origin);
						
						// Se envía primero al proveedor (destino) el aviso de una nueva petición:
						byte[] req = {Utils.NEW_REQ};
						DatagramPacket p = new DatagramPacket(req, req.length, destInfo.first, destInfo.second.first);
						listenSocket.send(p);
						
						// IP+puerto del origen se manda al destino.
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						baos2.write(packet.getAddress().getAddress());
						baos2.write(Utils.intToByteArray(packet.getPort()));
						byte[] info_for_destination = baos2.toByteArray();
						DatagramPacket to_destination = new DatagramPacket(info_for_destination,
								info_for_destination.length, destInfo.first, destInfo.second.first);
						listenSocket.send(to_destination);
						//outSocket.send(to_destination);
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
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	*/
	
	
}
