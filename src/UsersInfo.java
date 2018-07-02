import java.net.InetAddress;
import java.util.HashMap;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase que representa el dispositivo de un usuario.
 */


public class UsersInfo {

	// Nombre del usuario, dirección y puerto.
	private HashMap<String, Pair<InetAddress, Integer>> usersMap;
	
	
	public UsersInfo(){
		usersMap = new HashMap<>();
	}
	
	
	/**
	 * Acción que añade un nuevo usuario a la colección.
	 * Si ya existe un usuario con el mismo nombre el método no tiene efecto.
	 * 
	 * @param name
	 * @param addr
	 * @param port
	 */
	public void addUser(String name, InetAddress addr, int port){
		if (!usersMap.containsKey(name))
			usersMap.put(name, new Pair<InetAddress, Integer>(addr, port));
	}
	
	
	/**
	 * Acción que modifica la dirección y/o el puerto de un usuario.
	 * Si no existe un usuario con el nombre pasado como parámetro el método no tiene efecto.
	 * 
	 * @param name
	 * @param addr
	 * @param port
	 */
	public void editUserInfo(String name, InetAddress addr, int port){
		usersMap.replace(name, new Pair<InetAddress, Integer>(addr, port));
	}
	
	
	/**
	 * Modifica el nombre de un usuario en el servidor.
	 * 
	 * @param old
	 * @param newName
	 */
	public void editUserName(String old, String newName){
		if (usersMap.containsKey(old)){
			Pair<InetAddress, Integer> info = usersMap.get(old);
			usersMap.remove(old);
			usersMap.put(newName, info);
		}
	}
	
	
	/**
	 * Comprueba si hay un usuario en la colección con el nombre pasado como parámetro.
	 * 
	 * @param name
	 * @return 
	 */
	public boolean existsUserWithName(String name){
		return this.usersMap.containsKey(name);
	}
	
	
	/**
	 * Devuelve la dirección y el puerto del usuario con nombre user.
	 * 
	 * @param user
	 * @return
	 */
	// TODO: Leer:
	/* pensar cómo puedo solucionar el timeout para obtener la info correcta de un
	 * usuario destino que se conectó hace tiempo. Debo evitar que la info del móvil
	 * de destino en su tabla NAT se pierda por TIMEOUT. Quizá podría hacer ping todo el rato.
	 */
	public Pair<InetAddress, Integer> getUserInfo(String user){
		if (!this.existsUserWithName(user))
			return null;
		return this.usersMap.get(user);
	}
	
	
	
	/**
	 * Método que cargará de la base de datos los usuarios que se hayan conectado anteriormente.
	 * 
	 * En caso de que la base de datos esté vacía no hará nada.
	 */
	public void loadUsers(){
		// TODO
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsersInfo other = (UsersInfo) obj;
		if (usersMap == null) {
			if (other.usersMap != null)
				return false;
		} else if (!usersMap.equals(other.usersMap))
			return false;
		return true;
	}
	
	
	
}
