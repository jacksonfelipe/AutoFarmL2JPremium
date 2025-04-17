import com.premium.game.listener.actor.player.OnPlayerEnterListener;
import com.premium.game.model.actor.L2Playable;

public class PlayerEnter implements OnPlayerEnterListener  {
	
	@Override
	public void onPlayerEnter(L2Playable player) {
		PlayerA.setA(player);
	}

}