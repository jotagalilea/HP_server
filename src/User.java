import java.net.InetSocketAddress;


/**
 * Created by Julio on 02/06/2018.
 *
 * Clase que representa el dispositivo de un usuario.
 */


public class User {

	private final String name;
	private final InetSocketAddress address;
	
	public User(String name, InetSocketAddress addr){
		this.name = name;
		this.address = addr;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
