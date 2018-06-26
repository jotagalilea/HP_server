import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Julio on 02/06/2018.
 *
 * Clase que guarda la información relevante de la conexión entre 2 dispositivos.
 */


public class Connection {

	private String client;
	private DatagramSocket clientSocket;
	private InetAddress clientAddr;
	private int clientPort;
	
	private String servant;
	private DatagramSocket servantSocket;
	private InetAddress servantAddr;
	private int servantPort;
	
	private byte[] mainBuffer;
	
	
	public Connection(String c, String s){
		try {
			this.mainBuffer = new byte[Utils.MAX_BUFF_SIZE];
			this.client = c;
			this.servant = s;
			this.clientSocket = new DatagramSocket();
			
			// TODO: sin terminar.
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
}
