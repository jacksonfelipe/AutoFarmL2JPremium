
import java.util.HashMap;
import java.util.Map;

import com.premium.game.model.actor.L2Playable;


public class PlayerA{
	
	private static Map<Integer, AutoFarmContext> playerAutos = new HashMap<Integer, AutoFarmContext>();

	public static void setA(L2Playable p) {
		AutoFarmContext Jy = playerAutos.get(p.getObjectId());
		if (Jy == null) {
			Jy = new AutoFarmContext();
			Jy.setInitial(p);
			Jy.restoreVariables(p);
			playerAutos.put(p.getObjectId(), Jy);
		} else {
			Jy.pl = p;
			Jy.restoreVariables(p);
		}
	}	
	public static AutoFarmContext getFarmSystem(int idObj) {

		return playerAutos.get(idObj);
	}

	public static int getCurrentFarms() {

		return playerAutos.size();
	}
	

}	