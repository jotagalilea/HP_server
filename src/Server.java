import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	private DatagramSocket udpSocket;
	private ServerSocket tcpListenSocket;
	//private DatagramSocket outSocket;
	private final int udpPort = ;
	private final int tcpPort = ;
	
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
			udpSocket = new DatagramSocket(udpPort);
			udpSocket.setReuseAddress(true);
			tcpListenSocket = new ServerSocket(tcpPort);
			tcpListenSocket.setReuseAddress(true);
			//outSocket = new DatagramSocket();
			//outSocket.setReuseAddress(true);
			// TODO: ¿También se necesita una cola de espera? ¿O lanzo todos los hilos que se puedan?
			// TODO: Necesito que se lance un hilo cada vez que llegue una conexión.
			
			new Thread(new Runnable(){
				@Override
				public void run() {
					udpListen();
				}
			}).start();
			new Thread(new Runnable(){
				@Override
				public void run() {
					tcpListen();
				}
			}).start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void udpListen() {
		while (true){
			byte[] buffer = new byte[Utils.MAX_BUFF_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				udpSocket.receive(packet);
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
					
					if (buffer[2+nameLen] == Utils.IS_CLIENT_SOCKET){
						/* Si el usuario no está registrado se guarda la dirección del paquete recibido
						 * para tener al menos eso. Si ya está registrado se guarda la IP previamente
						 * recibida cuando se conectó el socket de la parte servidor.
						 */
						if (!usersInfo.existsUserWithName(userName))
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
						//if (addrString.charAt(0) == Utils.TAKE_IP_FROM_HEADER)
						if (buffer[nameLen+2] == Utils.TAKE_IP_FROM_HEADER)
							usersInfo.addUser(userName, packet.getAddress(), packet.getPort(), null);
						else {
							String addrString = new String(buffer).substring(2+nameLen, 6+nameLen);
							InetAddress addr = InetAddress.getByName(addrString);
							usersInfo.addUser(userName, addr, packet.getPort(), null);
						}
					}
					break;
					
				/* Saludo a un dispositivo. Si está en la lista de usuarios conectados al servidor se pasarán
				 * a cada uno la dirección y el puerto usados por el contrario para el futuro intercambio, si no
				 * se devolverá al origen NO_FRIEND.
				 */
				/*case Utils.HELLO:
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
					/*	Pair<InetAddress, Pair<Integer,Integer>> destInfo = usersInfo.getUserInfo(friendName);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] friendIP = destInfo.first.getAddress();
						byte[] friendPort = Utils.intToByteArray(destInfo.second.first);
						baos.write(friendIP);
						baos.write(friendPort);
						byte[] info_for_origin = baos.toByteArray();
						DatagramPacket to_origin = new DatagramPacket(info_for_origin, info_for_origin.length,
								packet.getAddress(), packet.getPort());
						udpSocket.send(to_origin);
						//outSocket.send(to_origin);
						
						// Se envía primero al proveedor (destino) el aviso de una nueva petición:
						/*byte[] req = {Utils.NEW_REQ};
						DatagramPacket p = new DatagramPacket(req, req.length, destInfo.first, destInfo.second.first);
						listenSocket.send(p);
						*/
						
						// IP+puerto del origen se manda al destino.
					/*	ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
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
					*/
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	private void tcpListen(){
		while (true){
			try {
				Socket sock = tcpListenSocket.accept();
				DataInputStream dis = new DataInputStream(sock.getInputStream());
				int buffSize = dis.readInt();
				byte[] buffer = new byte[buffSize];
				dis.readFully(buffer, 0, buffSize);
				
				switch (buffer[0]){
				case (Utils.HELLO):
					byte recipientNameSize = buffer[1];
					int recipientEndIndex = 2+recipientNameSize;
					String recipientName = new String(buffer).substring(2, recipientEndIndex);
					String clientName = new String(buffer).substring(recipientEndIndex);
					// Mirar si está registrado. Si lo está => paso 4
					if (usersInfo.existsUserWithName(recipientName)){
						Pair<InetAddress, Pair<Integer,Integer>> recipientInfo = usersInfo.getUserInfo(recipientName);
						InetAddress recipientAddr = recipientInfo.first;
						int recipientUDPport = recipientInfo.second.first;

						// TODO:
						/* Habrá que mandar al destinatario el nombre, dirección y puerto tcp del cliente
						 * que quiere iniciar la conexión. DE MOMENTO NO SE MANDA T0D0 PERO DEBERÍA HACERLO
						 * EXACTAMENTE AQUÍ.
						 */
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						//baos.write(Utils.NEW_REQ);
						//baos.write(clientName.length());
						//baos.write(clientName.getBytes());
						// De momento cojo la dirección del socket y no del stream.
						baos.write(sock.getInetAddress().getAddress());
						baos.write(Utils.intToByteArray(sock.getPort()));
						byte[] buf2Recipient = baos.toByteArray();
						
						// Se manda al sirviente la información del cliente para que abra el agujero.
						DatagramPacket dp = new DatagramPacket(buf2Recipient, 0, buf2Recipient.length,
								recipientAddr, recipientUDPport);
						udpSocket.send(dp);
						
						Socket auxTcpSocket = tcpListenSocket.accept();
						int recipientTCPport = auxTcpSocket.getPort();
						// TODO: El PASO 5 irá aquí:
						
						auxTcpSocket.close();
						
						/* TODO: Falta recibir CLOSE_SOCKET y cerrar la conexión TCP con el cliente,
						 * indicándole que inicie conexión directa con el destinatario.
						 */
						auxTcpSocket = tcpListenSocket.accept();
						DataInputStream auxdos = new DataInputStream(auxTcpSocket.getInputStream());
						byte resp = auxdos.readByte();
						if (resp == Utils.CLOSE_SOCKET){
							DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
							// TODO: Es aquí donde hay que mandar la info del destinatario al cliente:
							// La info se tratará en el connect_to_friend().
							ByteArrayOutputStream baos4client = new ByteArrayOutputStream(9);
							baos4client.write(Utils.CLOSE_SOCKET);
							baos4client.write(recipientAddr.getAddress());
							baos4client.write(Utils.intToByteArray(recipientTCPport));
							baos4client.writeTo(dos);
							sock.close();
							auxTcpSocket.close();
						}
						else {}
					}
					else {}
					break;
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
