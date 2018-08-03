import java.nio.ByteBuffer;

public class Utils {
	
	/**
	 * Longitud máxima del buffer de datos que se manejará.
	 */
	public static final int MAX_BUFF_SIZE = 1024;
	/**
	 * Cuando el servidor recibe una petición tiene que comprobar si procede de un amigo.
	 * En caso afirmativo envía de vuelta HELLO_FRIEND. En caso negativo envía NO_FRIEND.
	 */
	public static final byte HELLO_FRIEND = 6;
	public static final byte NO_FRIEND = 7;
	/**
	 * Identificador para la primera comunicación entre 2 dispositivos, también llamado PAQUETE DE SALUDO.
	 * Es relevante para la aplicación en el servidor, que debe saber que se trata del inicio de una
	 * comunicación y no una petición.
	 * El segundo dispositivo responderá con HELLO_FRIEND o bien NO_FRIEND.
	 */
	public static final byte HELLO = 8;
	/**
	 * Identificador que la aplicación del servidor debe enviar en primer lugar cuando se detecte que
	 * el cliente quiere realizar algún tipo de petición.
	 */
	public static final byte NEW_REQ = 9;
	/**
	 * Identificador utilizado cuando un dispositivo inicia la app y se conecta al servidor.
	 */
	public static final byte SERVER_CONNECT = 10;
	public static final byte IS_CLIENT_SOCKET = 11;
	public static final byte TAKE_IP_FROM_PACKET = 0;
	
	
	
	/**
	 * Transforma un int en un array de 4 bytes.
	 *
	 * @param n
	 * @return
	 */
	public static byte[] intToByteArray(int n) {
		return new byte[] {
			(byte)(n >>> 24),
			(byte)(n >>> 16),
			(byte)(n >>> 8),
			(byte) n};
	}


	/**
	 * Transforma un byte[] en un int.
	 *
	 * @param array
	 * @return
	 */
	public static int byteArrayToInt(byte[] array){
		return ByteBuffer.wrap(array).getInt();
	}
}
