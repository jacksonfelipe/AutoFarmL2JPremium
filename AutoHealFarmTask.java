import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.world.Location;
import com.premium.game.model.actor.L2Character;

public class AutoHealFarmTask extends BaseFarmTask implements Runnable {
  public AutoHealFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    tryAttack(selectRandomTarget());
  }
  
  protected boolean doTryUseLowLifeSkillSpell() {
	  L2Skill skill = getAutoFarmContext().nextHealSkill(getCommittedTarget(), (L2Character)getCommittedOwner());
    if (skill != null) {
      useMagicSkill(skill, (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF));
      return true;
    } 
    return false;
  }
  
  protected boolean doTryUseSelfSkillSpell() {
	  L2Skill skill = getAutoFarmContext().nextSelfSkill((L2Character)getCommittedOwner());
    if (skill != null) {
      useMagicSkill(skill, (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF));
      return true;
    } 
    return false;
  }
  
  protected void physicalAttack() {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return; 
    if (getCommittedOwner() != null && player.getDistance(getCommittedOwner().getX(), getCommittedOwner().getY()) > 200) {
      player.setTarget(null);
      Location location = Location.findPointToStay(getCommittedOwner().getLoc(), 100, 200);
      if (location != null)
        player.moveToLocation(location.getX(), location.getY(), location.getZ(), 0); 
      return;
    } 
    if (player.isCastingNow())
      return; 
    if (getAutoFarmContext().isAssistMonsterAttack() || !getAutoFarmContext().isLeaderAssist())
      super.physicalAttack(); 
  }
  
  protected boolean canAutoAssist() {
    return false;
  }
}

