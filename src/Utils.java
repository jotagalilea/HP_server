
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
	 * Identificador para la primera comunicación entre 2 dispositivos. Es relevante para la aplicación
	 * en el servidor, que debe saber que se trata del inicio de una comunicación y no una petición.
	 * El segundo dispositivo responderá con HELLO_FRIEND o bien NO_FRIEND.
	 */
	public static final byte HELLO = 8;
	/**
	 * Identificador utilizado cuando un dispositivo inicia la app y se conecta al servidor.
	 */
	public static final byte SERVER_CONNECT = 10;


}
