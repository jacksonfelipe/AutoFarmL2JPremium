
public class AutoMagicFarmTask extends BaseFarmTask implements Runnable {
  public AutoMagicFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    if (selectRandomTarget() && !doTryUseAttackSkillSpell())
      moveCloserToCommittedTarget(600);
  }
  
}

