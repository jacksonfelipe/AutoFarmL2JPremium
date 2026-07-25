
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.premium.L2DatabaseFactory;
import com.premium.game.network.ThreadPoolManager;
import com.premium.game.datatables.sql.ServerData;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.world.L2World;
import com.premium.game.model.world.Location;

import org.apache.log4j.Logger;

public final class AutoFarmManager {

	protected static Logger _log = Logger.getLogger(AutoFarmManager.class);

	private final Map<String, Integer> aqZ = new HashMap<>();

	private final List<Integer> ara = new ArrayList<>();

	private ScheduledFuture<?> Nj = null;

	protected AutoFarmManager() {
		this.aqZ.clear();
		this.ara.clear();
		dy();
	}

	public int getActiveFarms(String paramString) {
		return (AutoFarmConfig.FARM_ACTIVE_LIMITS < 0) ? Integer.MAX_VALUE
				: (this.aqZ.containsKey(paramString)
						? (AutoFarmConfig.FARM_ACTIVE_LIMITS - ((Integer) this.aqZ.get(paramString)).intValue())
						: AutoFarmConfig.FARM_ACTIVE_LIMITS);
	}

	public void addActiveFarm(String paramString, int paramInt) {
		if (AutoFarmConfig.FARM_ACTIVE_LIMITS < 0)
			return;
		if (this.aqZ.containsKey(paramString)) {
			if (isNonCheckPlayer(paramInt))
				return;
			int i = ((Integer) this.aqZ.get(paramString)).intValue() + 1;
			this.aqZ.put(paramString, Integer.valueOf(i));
			return;
		}
		this.aqZ.put(paramString, Integer.valueOf(1));
	}

	public void removeActiveFarm(String paramString, int paramInt) {
		if (AutoFarmConfig.FARM_ACTIVE_LIMITS < 0)
			return;
		if (this.aqZ.containsKey(paramString)) {
			if (isNonCheckPlayer(paramInt))
				return;
			int i = ((Integer) this.aqZ.get(paramString)).intValue() - 1;
			this.aqZ.put(paramString, Integer.valueOf(i));
		}
	}

	public void addNonCheckPlayer(int paramInt) {
		if (AutoFarmConfig.FARM_ACTIVE_LIMITS < 0)
			return;
		if (!this.ara.contains(Integer.valueOf(paramInt)))
			this.ara.add(Integer.valueOf(paramInt));
	}

	public boolean isNonCheckPlayer(int paramInt) {
		return this.ara.contains(Integer.valueOf(paramInt));
	}

	private void dy() {
		if (AutoFarmConfig.REFRESH_FARM_TIME) {
			long l = 0L;
			try {
				l = ServerData.getInstance().getData().getLong("Farm_FreeTime");
			} catch (IllegalArgumentException e) {
				l = 0L;
			}
			if (System.currentTimeMillis() > l) {
				dz();
			} else {
				this.Nj = ThreadPoolManager.getInstance().scheduleGeneral((Runnable) new ClearFarmFreeTime(),
						l - System.currentTimeMillis());
			}
		}
	}

	private void dz() {
		if (!AutoFarmConfig.REFRESH_FARM_TIME)
			return;
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(true);
		calendar.set(11, 6);
		calendar.set(12, 30);
		calendar.add(5, 1);
		ServerData.getInstance().getData().set("Farm_FreeTime", calendar.getTimeInMillis());
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = L2DatabaseFactory.getInstance().getConnection();
			statement = connection.prepareStatement("DELETE FROM character_variables WHERE name = ?");
			statement.setString(1, "farmFreeTime");
			statement.execute();
		} catch (SQLException e) {
			_log.error("Error clearing farm free time: " + e.getMessage(), e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				_log.error("Error closing statement: " + e.getMessage(), e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				_log.error("Error closing connection: " + e.getMessage(), e);
			}
		}

		for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
			if (player != null)
				player.unsetVar("farmFreeTime");
		}
		if (this.Nj != null) {
			this.Nj.cancel(false);
			this.Nj = null;
		}
		this.Nj = ThreadPoolManager.getInstance().scheduleGeneral((Runnable) new ClearFarmFreeTime(),
				calendar.getTimeInMillis() - System.currentTimeMillis());
		_log.info("AutoFarmManager: AutoFarm free times reshresh completed.");
		_log.info("AutoFarmManager: Next autoFarm free times reshresh at: "
				+ TimeUtils.formatTime((int) (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000, false));
	}

	public static final AutoFarmManager getInstance() {
		return SingletonHolder._instance;
	}

	private static class SingletonHolder {
		protected static final AutoFarmManager _instance = new AutoFarmManager();
	}

	private class ClearFarmFreeTime implements Runnable {
		private ClearFarmFreeTime() {
		}

		public void run() {
			AutoFarmManager.this.dz();
		}

	}
}
