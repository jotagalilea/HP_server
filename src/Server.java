import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	private ClientManager clientManager;
	
	
	
	public static Server getInstance(){
		if (server == null)
			server = new Server();
		return server;
	}
	
	
	private Server(){
		//activeConnections = new ArrayList<>();
		usersInfo = new UsersInfo();
		clientManager = ClientManager.getInstance();
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
				// TODO: No está definido el comportamiento en caso de no ser SERVER_CONNECT.
				if (type == Utils.SERVER_CONNECT){
					// Registro del usuario en el Servidor en memoria.
					boolean isServer = false;
					byte nameLen = buffer[1];
					String userName = new String(buffer).substring(2, 2+nameLen);
					
					if (buffer[2+nameLen] == Utils.IS_CLIENT_SOCKET){
						/* Si el usuario no está registrado se guarda la dirección del paquete recibido
						 * para tener al menos eso. Si ya está registrado se guarda la IP previamente
						 * recibida cuando se conectó el socket de la parte servidor.
						 */
						if (!usersInfo.existsUserWithName(userName))
							//usersInfo.addUser(userName, packet.getAddress(), null, packet.getPort());
							usersInfo.addUser(userName, socket.getInetAddress(), null, socket.getPort());
						else {
							InetAddress addr = usersInfo.getUserAddr(userName);
							//usersInfo.addUser(userName, addr, null, packet.getPort());
							usersInfo.addUser(userName, addr, null, socket.getPort());
						}
					}
					else {
						isServer = true;
						/* Si se conecta el socket de la parte Servidor (es decir, la primera parte
						 * que se conecta) se está recibiendo la IP de dicha parte. La IP son 4 bytes.
						 */
						String addrString = new String(buffer).substring(3+nameLen, 7+nameLen);
						if (addrString.charAt(0) == Utils.TAKE_IP_FROM_HEADER)
							//usersInfo.addUser(userName, packet.getAddress(), packet.getPort(), null);
							usersInfo.addUser(userName, socket.getInetAddress(), socket.getPort(), null);
						else {
							InetAddress addr = InetAddress.getByName(addrString);
							//usersInfo.addUser(userName, addr, packet.getPort(), null);
							usersInfo.addUser(userName, addr, socket.getPort(), null);
						}
					}
					/* Es importante saber si es un socket a la parte servidor del usuario o no para
					 * facilitar luego su recuperación cuando un cliente quiera conectarse a él.
					 */
					clientManager.addClientSocket(userName, socket, isServer);
				}
				
				// Lanzamiento del hilo que atiende al socket creado justo ahora:
				new Thread(new Runnable(){
					@Override
					public void run() {
						serveClient(socket, dis);
					}
				}).start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	private void serveClient(Socket s, DataInputStream dis){
		try {
			boolean exit = false;
			//////////////////////////////////////////////////////////////////
			String friendName = "Manolito";
			//////////////////////////////////////////////////////////////////
			Socket sock2peer = null;
			while (!exit){
				byte[] buffer = new byte[Utils.MAX_BUFF_SIZE];
				int bufSize = dis.readInt();
				dis.readFully(buffer, 0, bufSize);
				byte type = buffer[0];
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				
				switch (type){
				case Utils.HELLO:
					/* Saludo a un dispositivo. Si está en la lista de usuarios conectados al servidor se pasarán
					 * a cada uno la dirección y el puerto usados por el contrario para el futuro intercambio, si no
					 * se devolverá al origen NO_FRIEND.
					 */
					byte friendNameLen = buffer[1];
					friendName = new String(buffer).substring(2, 2+friendNameLen);
					// Se comprueba si el usuario destino está conectado con su nombre:
					if (usersInfo.existsUserWithName(friendName)){
						/* En caso afirmativo:
						 * 1- Se avisa al origen y se pasa IP+puerto del destino.
						 * 2- Se pasa IP+puerto del destino al origen.
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
						
						baos.writeTo(dos);
						
						// Se envía primero al proveedor (destino) el aviso de una nueva petición:
						//InetSocketAddress destAddr = new InetSocketAddress(destInfo.first, destInfo.second.second);
						Socket sock2servPart = clientManager.getUserSocket(friendName, true);
						//socket.connect(destAddr);
						OutputStream os2Serv = sock2servPart.getOutputStream();
						byte[] newreq = {Utils.NEW_REQ};
						os2Serv.write(newreq);
						//writeByte(Utils.NEW_REQ);
						
						// Luego se le envía IP+puerto del origen al destino.
						ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
						baos2.write(sock2servPart.getInetAddress().getAddress());
						baos2.write(Utils.intToByteArray(socket.getPort()));
						baos2.writeTo(os2Serv);
					}
					else{
						// Si no, se devuelve respuesta negativa al origen con NO_FRIEND.
						dos.writeByte(Utils.NO_FRIEND);
					}
					break;
					
					
				case Utils.CLOSE_SOCKET:
					// El cliente va a cerrar la conexión, así que aquí también hay que cerrarla.
					//Coge el socket del cliente, dile al cliente que intente conectarse al amigo y cierra el socket.
					sock2peer = clientManager.getUserSocket(friendName, false);
					OutputStream os = sock2peer.getOutputStream();
					os.write(Utils.TRY_CONNECT);
					s.close();
					sock2peer.close();
					exit = true;
					break;
				}
				System.out.println("Borrar este print");
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		
}
