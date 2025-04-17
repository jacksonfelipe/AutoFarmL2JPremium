
public class AutoPhysicalFarmTask extends BaseFarmTask implements Runnable {
  public AutoPhysicalFarmTask(AutoFarmContext paramAutoFarmContext) {
    super(paramAutoFarmContext);
  }
  
  public void runImpl() throws Exception {
    tryAttack(selectRandomTarget());
  }
}


