import org.apache.log4j.Logger;


public class AutoFarmEndTask implements Runnable {
  private final AutoFarmContext Nw;
  protected static Logger _log = Logger.getLogger(AutoFarmEndTask.class);

  public AutoFarmEndTask(AutoFarmContext paramAutoFarmContext) {
    this.Nw = paramAutoFarmContext;
  }
  
  public void run() {
    if (this.Nw != null) {
      this.Nw.setAutoFarmEndTask(0L);
      _log.info("Endtime autofarm.");
      this.Nw.stopFarmTask(false);
      if (AutoFarmConfig.FARM_ONLINE_TYPE)
        this.Nw.checkFarmTask(); 
    } 
  }
}

