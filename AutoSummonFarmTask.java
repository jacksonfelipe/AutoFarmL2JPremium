import com.premium.game.ai.CtrlIntention;
import com.premium.game.geodata.GeoEngine;
import com.premium.game.model.L2Object;
import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.L2Summon;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.world.Location;

public class AutoSummonFarmTask extends BaseFarmTask implements Runnable {
  private long Nx = 0L;
  
  public AutoSummonFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return; 
    if (player.getPet() == null)
      setCommittedSummon(null); 
    tryAttack(selectRandomTarget());
  }
  
  protected boolean doTryUseAttackSkillSpell() {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return false; 
     L2Summon summon = getCommittedSummon();
    boolean bool = super.doTryUseAttackSkillSpell();
    if ((this.Nx == 0L || this.Nx > System.currentTimeMillis()) && summon != null && !summon.isDead() && getCommittedTarget() != null && getAutoFarmContext().isUseSummonSkills()) {
      L2Skill skill = getAutoFarmContext().nextSummonAttackSkill(getCommittedTarget(), getCommittedSummon(), this.Nx);
      if (skill != null)
        a(skill, false); 
    } 
    return bool;
  }
  
  protected boolean doTryUseLowLifeSkillSpell() {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return false; 
     L2Summon summon = getCommittedSummon();
    L2Skill skill = getAutoFarmContext().nextHealSkill(getCommittedTarget(), (L2Character)getCommittedSummon());
    boolean bool = false;
    if (skill != null) {
      useMagicSkill(skill, !skill.isOffensive());
      bool = true;
    } 
    if ((this.Nx == 0L || this.Nx > System.currentTimeMillis()) && summon != null && !summon.isDead() && getCommittedTarget() != null && getAutoFarmContext().isUseSummonSkills()) {
    	L2Skill skill1 = getAutoFarmContext().nextSummonHealSkill(getCommittedTarget(), getCommittedSummon(), (L2Character)player);
      if (skill1 != null)
        a(skill1, (skill1.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)); 
    } 
    return bool;
  }
  
  protected boolean doTryUseSelfSkillSpell() {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
    if (player == null)
      return false; 
     L2Summon summon = getCommittedSummon();
     L2Skill skill = getAutoFarmContext().nextSelfSkill((L2Character)getCommittedSummon());
    boolean bool = false;
    if (skill != null) {
      useMagicSkill(skill, true);
      bool = true;
    } 
    if ((this.Nx == 0L || this.Nx > System.currentTimeMillis()) && summon != null && !summon.isDead() && getCommittedTarget() != null && getAutoFarmContext().isUseSummonSkills()) {
      L2Skill skill1 = getAutoFarmContext().nextSummonSelfSkill(getCommittedSummon(), (L2Character)player);
      if (skill1 != null)
        a(skill1, (skill1.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)); 
    } 
    return bool;
  }
  
  protected void physicalAttack() {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
     L2Summon summon = getCommittedSummon();
    if (player == null)
      return; 
    if (getCommittedTarget() != null && getCommittedTarget().isAutoAttackable((L2Character)player) && !getCommittedTarget().isAlikeDead()) {
      player.setTarget((L2Object)getCommittedTarget());
      if (GeoEngine.getInstance().canSeeTarget(player, getCommittedTarget())) {
        player.setTarget(getCommittedTarget());
        player.getAI().setIntention(CtrlIntention.ATTACK);
       
        if (summon != null && player.getPet() != null && !summon.isDead())
        summon.setTarget(getCommittedTarget());
        summon.getAI().setIntention(CtrlIntention.ATTACK);
      } else {
        Location location = getCommittedTarget().getLoc();
        if (summon != null && player.getPet() != null && !summon.isDead())
        summon.followOwner();
        player.moveToLocation(location.getX(), location.getY(), location.getZ(), 0);
      } 
    } 
  }
  
  private void a(L2Skill paramSkill, boolean paramBoolean) {
    L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
     L2Summon summon = getCommittedSummon();
    if (player == null || paramSkill == null || summon == null)
      return; 
    if (summon.isOutOfControl())
      return; 
    if (getAutoFarmContext().isExtraSummonDelaySkill())
      this.Nx = System.currentTimeMillis() + AutoFarmConfig.SKILLS_EXTRA_DELAY; 
    trySummonUseMagic(paramSkill, paramBoolean);
  }
  
  public void trySummonUseMagic(L2Skill paramSkill, boolean paramBoolean) {
     L2Summon summon = getCommittedSummon();
    if (summon == null || paramSkill == null)
      return; 
    if (paramBoolean) {
    L2Object gameObject = summon.getTarget();
      summon.setTarget((L2Object)summon);
      summon.doCast(paramSkill);
      summon.setTarget(gameObject);
      return;
    } 
    if (summon.getTarget() == null)
      return; 
    L2Character creature = paramSkill.getFirstOfTargetList(summon);
    summon.setTarget(creature);
    summon.doCast(paramSkill);
  }
}


