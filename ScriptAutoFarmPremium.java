
import org.apache.log4j.Logger;

import com.premium.game.handler.VoicedCommandHandler;
import com.premium.game.model.actor.listener.CharListenerList;
import com.premium.game.network.ThreadPoolManager;
import com.premium.protect.Guard;


public class ScriptAutoFarmPremium {

	private static final Logger _log = Logger.getLogger(ScriptAutoFarmPremium.class.getName());

	public ScriptAutoFarmPremium() {

		_log.info("AutoFarmPremium Loaded.");

		try {

			AutoFarmConfig.load();

			/*
			_log.info("AutoFarmPremium License Key");
			
			if(AutoFarmConfig.LICENSE_CODE.isEmpty()) {
				
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
					public void run() {
						
						 _log.error("=== AutoFarmPremium ===");
						 _log.error("Dev License expired.");
					     System.exit(0);
					}
				},1800000);
				
			}else {
				
				Guard.getInstance("eccbc87e4b5ce2fe28308fd9f2a7baf3", AutoFarmConfig.LICENSE_CODE, true);
							
			}
			*/
			
			if (AutoFarmConfig.ALLOW_AUTO_FARM) {

				CharListenerList.addGlobal(new PlayerEnter());
				VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new AutoFarm());
		

			}
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			_log.info("AutoFarmPremium: Error while reading config", e);

		}

	}

}
