
import java.util.concurrent.ScheduledFuture;

import com.premium.game.model.L2Object;
import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.instance.L2MonsterInstance;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.world.Location;

public class AutoArcherFarmTask extends BaseFarmTask implements Runnable {
  public AutoArcherFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    tryAttack(selectRandomTarget());
  }
  
  protected void physicalAttack() {
    AutoFarmContext autoFarmContext = getAutoFarmContext();
    if (autoFarmContext == null)
      return; 
    L2PcInstance player = autoFarmContext.getP().getActingPlayer();
    if (player == null)
      return; 
    if (autoFarmContext.isRunTargetCloseUp()) {
    	L2MonsterInstance npcInstance = getCommittedTarget();
      if (npcInstance != null && player.getDistance(npcInstance.getX(),npcInstance.getY()) < AutoFarmConfig.RUN_CLOSE_UP_DISTANCE) {
        Pair<ScheduledFuture<?>, Location> pair = runAwayFromTargetAndThan((L2Object)npcInstance, (L2Character)player, 500, 100, this);
        if (pair != null) {
          if (getMoveToPair() != null && getMoveToPair().getKey() != null)
            (getMoveToPair().getKey()).cancel(false); 
          setMoveToPair(pair);
          return;
        } 
      } 
    } 
    super.physicalAttack();
  }
}
