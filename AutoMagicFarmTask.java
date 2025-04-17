
import java.util.concurrent.ScheduledFuture;

import com.premium.game.model.L2Object;
import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.instance.L2MonsterInstance;
import com.premium.game.model.actor.instance.L2NpcInstance;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.world.Location;

import javafx.util.Pair;

public class AutoMagicFarmTask extends BaseFarmTask implements Runnable {
  public AutoMagicFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    tryUseSpell(selectRandomTarget());
  }
  
  protected boolean preDoUseMagicSkill(L2Skill paramSkill, boolean paramBoolean) {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return false; 
    if (getAutoFarmContext().isRunTargetCloseUp() && !paramBoolean) {
    	L2MonsterInstance npcInstance = getCommittedTarget();
      if (npcInstance != null && player.getDistance(npcInstance.getX(), npcInstance.getY()) < AutoFarmConfig.RUN_CLOSE_UP_DISTANCE) {
        Pair<ScheduledFuture<?>, Location> pair = runAwayFromTargetAndThan((L2Object)npcInstance, (L2Character)player, 500, 100, this);
        if (pair != null) {
          if (getMoveToPair() != null && getMoveToPair().getKey() != null)
            ((ScheduledFuture)getMoveToPair().getKey()).cancel(false); 
          setMoveToPair(pair);
          return false;
        } 
      } 
    } 
    return super.preDoUseMagicSkill(paramSkill, paramBoolean);
  }
}

