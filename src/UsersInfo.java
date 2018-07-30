import java.net.InetAddress;
import java.util.HashMap;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase que aglutina las direcciones y puertos de cada usuario.
 */


public class UsersInfo {

	/* Nombre del usuario, dirección y puertos. El primer puerto del par interno
	 * corresponde al de la parte servidor y el segundo al de la parte cliente.
	 */
	private HashMap<String, Pair<InetAddress, Pair<Integer,Integer>>> usersMap;
	
	
	public UsersInfo(){
		usersMap = new HashMap<>();
	}
	
	
	/**
	 * Acción que añade un nuevo usuario a la colección.
	 * Si ya existe usuario entonces se actualiza la dirección y el puerto.
	 * 
	 * @param name
	 * @param addr
	 * @param servPort
	 * @param cliPort
	 */
	public void addUser(String name, InetAddress addr, Integer servPort, Integer cliPort){
		Pair<Integer, Integer> ports = null;
		if (cliPort == null){
			Integer oldCliPort = null;
			try{
				oldCliPort = usersMap.get(name).second.second;
			} catch (NullPointerException e){}
			ports = new Pair<>(servPort, oldCliPort);
		}
		if (servPort == null){
			Integer oldServPort = null;
			try{
				oldServPort = usersMap.get(name).second.first;
			} catch (NullPointerException e){}
			ports = new Pair<>(oldServPort, cliPort);
		}
		
		Pair<InetAddress, Pair<Integer,Integer>> info = new Pair<>(addr, ports);
		usersMap.put(name, info);
	}
	
	
	/**
	 * Acción que modifica la dirección y/o el puerto de un usuario.
	 * Si no existe un usuario con el nombre pasado como parámetro el método no tiene efecto.
	 * 
	 * @param name
	 * @param addr
	 * @param servPort
	 * @param cliPort
	 */
	public void editUserInfo(String name, InetAddress addr, int servPort, int cliPort){
		Pair<Integer, Integer> ports = new Pair<>(servPort, cliPort);
		usersMap.replace(name, new Pair<InetAddress, Pair<Integer,Integer>>(addr, ports));
	}
	
	
	/**
	 * Modifica el nombre de un usuario en el servidor.
	 * 
	 * @param old
	 * @param newName
	 */
	public void editUserName(String old, String newName){
		if (usersMap.containsKey(old)){
			Pair<InetAddress, Pair<Integer,Integer>> info = usersMap.get(old);
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
	public Pair<InetAddress, Pair<Integer,Integer>> getUserInfo(String user){
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
	
	
	/**
	 * Devuelve la dirección de un usuario. Si no existe el usuario se devuelve null.
	 * 
	 * @param name
	 * @return Dirección del usuario.
	 */
	public InetAddress getUserAddr(String name){
		Pair<InetAddress, Pair<Integer,Integer>> pair = this.getUserInfo(name);
		if (pair == null)
			return null;
		else
			return pair.first;
	}
	
	
}
