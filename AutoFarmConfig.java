import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.premium.annotations.L2Properties;
import com.premium.game.skills.AbnormalEffect;


public class AutoFarmConfig {

	private static final Logger _log = Logger.getLogger(AutoFarmConfig.class.getName());

	private static final String AUTO_FARM_CONFIG_FILE = "./config/custom/AutoFarm.properties";

	public static boolean FARM_ONLINE_TYPE;

	public static boolean AUTO_FARM_ALLOW_FOR_CURSED_WEAPON;

	public static boolean PREMIUM_FARM_FREE;

	public static boolean ALLOW_AUTO_FARM;

	public static boolean AUTO_FARM_FOR_PREMIUM;

	public static boolean AUTO_FARM_FREE;

	public static Map<Integer, String> AUTO_FARM_PRICES;

	public static int ATTACK_SKILL_CHANCE;

	public static int ATTACK_SKILL_PERCENT;

	public static int CHANCE_SKILL_CHANCE;

	public static int CHANCE_SKILL_PERCENT;

	public static int SELF_SKILL_CHANCE;

	public static int SELF_SKILL_PERCENT;

	public static int HEAL_SKILL_CHANCE;

	public static int HEAL_SKILL_PERCENT;

	public static int SUMMON_ATTACK_SKILL_CHANCE;

	public static int SUMMON_ATTACK_SKILL_PERCENT;

	public static int SUMMON_SELF_SKILL_CHANCE;

	public static int SUMMON_SELF_SKILL_PERCENT;

	public static int SUMMON_HEAL_SKILL_CHANCE;

	public static int SUMMON_HEAL_SKILL_PERCENT;

	public static long SKILLS_EXTRA_DELAY = Long.parseLong(exProperties.getProperty("SkillsExtraDelay", "5")) * 1000L;

	public static long KEEP_LOCATION_DELAY = Long.parseLong(exProperties.getProperty("KeepLocationDelay", "5")) * 1000L;

	public static long RUN_CLOSE_UP_DELAY = Long.parseLong(exProperties.getProperty("RunCloseUpDelay", "2")) * 1000L;

	public static int RUN_CLOSE_UP_DISTANCE;

	public static int SHORTCUT_PAGE;

	public static int SEARCH_DISTANCE;

	public static int FARM_TYPE;

	public static int FARM_INTERVAL_TASK;

	public static boolean ALLOW_FARM_FREE_TIME;

	public static boolean REFRESH_FARM_TIME;

	public static int FARM_FREE_TIME;

	public static boolean ALLOW_CHECK_HWID_LIMIT;

	public static int FARM_ACTIVE_LIMITS;

	public static int[] FARM_EXPEND_LIMIT_PRICE = new int[2];

	public static int[] AUTO_FARM_IGNORED_NPC_ID;

	public static AbnormalEffect SERVICES_AUTO_FARM_ABNORMAL;

	public static boolean SERVICE_AUTO_FARM_SET_RED_RING;

	public static Set<String> AUTO_FARM_LIMIT_ZONE_NAMES;

	public static String LICENSE_CODE;

	// Sistema de Cache e Performance
	public static boolean USE_DYNAMIC_DELAYS = true;
	public static boolean USE_TARGET_CACHE = true;
	public static int TARGET_CACHE_DURATION_MS = 5000;
	public static int MAX_CACHED_TARGETS = 100;
	
	// Delays Dinâmicos
	public static int MIN_SKILLS_DELAY = 1000;
	public static int MAX_SKILLS_DELAY = 5000;
	public static int MIN_LOCATION_DELAY = 1000;
	public static int MAX_LOCATION_DELAY = 5000;
	
	// Configuração de Busca Espacial
	public static int MIN_SEARCH_DISTANCE = 500;
	public static int MAX_SEARCH_DISTANCE = 3000;
	public static int SEARCH_PARTITION_SIZE = 500;
	
	// Sistema de Perfis
	public static boolean ENABLE_FARM_PROFILES = true;
	public static int MAX_PROFILES_PER_PLAYER = 5;
	
	// Sistema de Relatórios
	public static boolean ENABLE_FARM_REPORTS = true;
	public static int REPORT_INTERVAL_MINUTES = 30;
	
	// Sistema de Party/Guild
	public static boolean ENABLE_PARTY_SYNC = true;
	public static boolean ENABLE_GUILD_REPORTS = true;

	public static void load() {

		try {
			L2Properties exProperties = new L2Properties(AUTO_FARM_CONFIG_FILE);

			if (exProperties == null) {
				_log.error("Failed to load " + AUTO_FARM_CONFIG_FILE);
				return;
			}

			LICENSE_CODE = exProperties.getProperty("ProtectKey", "");
			ALLOW_AUTO_FARM = Boolean.parseBoolean(exProperties.getProperty("AllowAutoFarm", "True"));
			FARM_ONLINE_TYPE = Boolean.parseBoolean(exProperties.getProperty("AutoFarmOnlineType", "True"));
			AUTO_FARM_FOR_PREMIUM = Boolean.parseBoolean(exProperties.getProperty("AutoFarmOnlyForPremium", "False"));
			AUTO_FARM_FREE = Boolean.parseBoolean(exProperties.getProperty("AutoFarmIsFree", "True"));
			PREMIUM_FARM_FREE = Boolean.parseBoolean(exProperties.getProperty("AutoFarmIsFreeForPremium", "True"));
			String[] arrayOfString1 = exProperties.getProperty("AutoFarmPriceList", "1,4037:10;3,4037:15;").split(";");
			AUTO_FARM_PRICES = new HashMap<>(arrayOfString1.length);
			for (String str : arrayOfString1) {
				String[] arrayOfString = str.split(",");
				if (arrayOfString.length == 2)
					try {
						AUTO_FARM_PRICES.put(Integer.valueOf(Integer.parseInt(arrayOfString[0])), arrayOfString[1]);
					} catch (NumberFormatException numberFormatException) {
						_log.info(numberFormatException.getMessage());
					}
			}

			ATTACK_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("AttackSkillChance", "100"));
			ATTACK_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("AttackSkillPercent", "5"));
			CHANCE_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("ChanceSkillChance", "100"));
			CHANCE_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("ChanceSkillPercent", "5"));
			SELF_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("SelfSkillChance", "100"));
			SELF_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("SelfSkillPercent", "5"));
			HEAL_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("HealSkillChance", "100"));
			HEAL_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("HealSkillPercent", "30"));
			SUMMON_ATTACK_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("SummonAttackSkillChance", "100"));
			SUMMON_ATTACK_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("SummonAttackSkillPercent", "5"));
			SUMMON_SELF_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("SummonSelfSkillChance", "100"));
			SUMMON_SELF_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("SummonSelfSkillPercent", "5"));
			SUMMON_HEAL_SKILL_CHANCE = Integer.parseInt(exProperties.getProperty("SummonHealSkillChance", "100"));
			SUMMON_HEAL_SKILL_PERCENT = Integer.parseInt(exProperties.getProperty("SummonHealSkillPercent", "30"));

			SHORTCUT_PAGE = Integer.parseInt(exProperties.getProperty("ShortCutPage", "10"));
			SEARCH_DISTANCE = Integer.parseInt(exProperties.getProperty("SearchDistance", "3000"));
			FARM_TYPE = Integer.parseInt(exProperties.getProperty("AutoFarmType", "0"));
			FARM_INTERVAL_TASK = Integer.parseInt(exProperties.getProperty("AutoFarmIntervalTask", "600"));
			RUN_CLOSE_UP_DISTANCE = Integer.parseInt(exProperties.getProperty("RunCloseUpDistance", "100"));
			ALLOW_FARM_FREE_TIME = Boolean.parseBoolean(exProperties.getProperty("AllowFarmFreeTime", "False"));
			REFRESH_FARM_TIME = Boolean.parseBoolean(exProperties.getProperty("AllowRefreshFarmTime", "False"));
			FARM_FREE_TIME = Integer.parseInt(exProperties.getProperty("FarmFreeTime", "3"));
			ALLOW_CHECK_HWID_LIMIT = Boolean.parseBoolean(exProperties.getProperty("AllowCheckHwidLimits", "False"));
			FARM_ACTIVE_LIMITS = Integer.parseInt(exProperties.getProperty("FarmActiveLimits", "3"));

			String[] npcs = exProperties.getProperty("AutoFarmIgnoreMobIds", "").split(",");

			if (npcs.length > 0 && !npcs[0].isEmpty()) {
				AUTO_FARM_IGNORED_NPC_ID = new int[npcs.length];
				for (int i = 0; i < npcs.length; i++) {
					try {
						AUTO_FARM_IGNORED_NPC_ID[i] = Integer.parseInt(npcs[i].trim());
					} catch (NumberFormatException e) {
						_log.warn("Invalid NPC ID in AutoFarmIgnoreMobIds: " + npcs[i]);
						AUTO_FARM_IGNORED_NPC_ID[i] = 0;
					}
				}
			} else {
				AUTO_FARM_IGNORED_NPC_ID = new int[0];
			}

			SERVICES_AUTO_FARM_ABNORMAL = AbnormalEffect
					.getByName(exProperties.getProperty("AutoFarmAbnormalEffectName", "null"));

			SERVICE_AUTO_FARM_SET_RED_RING = Boolean
					.parseBoolean(exProperties.getProperty("AutoFarmSetRedRing", "false"));
			AUTO_FARM_LIMIT_ZONE_NAMES = new HashSet<>(Arrays.asList(
					exProperties.getProperty("AutoFarmProhibitedZones", "[giran_town_peace1],[giran_town_peace2]")));
			String[] limitPriceArray = exProperties.getProperty("FarmExpendLimitPrice", "4037,1").split(",");

			if (limitPriceArray.length >= 2) {
				try {
					FARM_EXPEND_LIMIT_PRICE[0] = Integer.parseInt(limitPriceArray[0].trim());
					FARM_EXPEND_LIMIT_PRICE[1] = Integer.parseInt(limitPriceArray[1].trim());
				} catch (NumberFormatException e) {
					_log.warn("Invalid FarmExpendLimitPrice format. Using defaults: 4037,1");
					FARM_EXPEND_LIMIT_PRICE[0] = 4037;
					FARM_EXPEND_LIMIT_PRICE[1] = 1;
				}
			} else {
				_log.warn("Invalid FarmExpendLimitPrice format. Using defaults: 4037,1");
				FARM_EXPEND_LIMIT_PRICE[0] = 4037;
				FARM_EXPEND_LIMIT_PRICE[1] = 1;
			}

			// Carregando novas configurações
			USE_DYNAMIC_DELAYS = Boolean.parseBoolean(exProperties.getProperty("UseDynamicDelays", "true"));
			USE_TARGET_CACHE = Boolean.parseBoolean(exProperties.getProperty("UseTargetCache", "true"));
			TARGET_CACHE_DURATION_MS = Integer.parseInt(exProperties.getProperty("TargetCacheDuration", "5000"));
			MAX_CACHED_TARGETS = Integer.parseInt(exProperties.getProperty("MaxCachedTargets", "100"));
			
			MIN_SKILLS_DELAY = Integer.parseInt(exProperties.getProperty("MinSkillsDelay", "1000"));
			MAX_SKILLS_DELAY = Integer.parseInt(exProperties.getProperty("MaxSkillsDelay", "5000"));
			MIN_LOCATION_DELAY = Integer.parseInt(exProperties.getProperty("MinLocationDelay", "1000"));
			MAX_LOCATION_DELAY = Integer.parseInt(exProperties.getProperty("MaxLocationDelay", "5000"));
			
			MIN_SEARCH_DISTANCE = Integer.parseInt(exProperties.getProperty("MinSearchDistance", "500"));
			MAX_SEARCH_DISTANCE = Integer.parseInt(exProperties.getProperty("MaxSearchDistance", "3000"));
			SEARCH_PARTITION_SIZE = Integer.parseInt(exProperties.getProperty("SearchPartitionSize", "500"));
			
			ENABLE_FARM_PROFILES = Boolean.parseBoolean(exProperties.getProperty("EnableFarmProfiles", "true"));
			MAX_PROFILES_PER_PLAYER = Integer.parseInt(exProperties.getProperty("MaxProfilesPerPlayer", "5"));
			
			ENABLE_FARM_REPORTS = Boolean.parseBoolean(exProperties.getProperty("EnableFarmReports", "true"));
			REPORT_INTERVAL_MINUTES = Integer.parseInt(exProperties.getProperty("ReportIntervalMinutes", "30"));
			
			ENABLE_PARTY_SYNC = Boolean.parseBoolean(exProperties.getProperty("EnablePartySync", "true"));
			ENABLE_GUILD_REPORTS = Boolean.parseBoolean(exProperties.getProperty("EnableGuildReports", "true"));

		} catch (Exception e) {
			_log.error("Error loading AutoFarm config: " + e.getMessage(), e);
			setDefaultValues();
		}
	}

	private static void setDefaultValues() {
		SKILLS_EXTRA_DELAY = 5000L;
		KEEP_LOCATION_DELAY = 5000L;
		RUN_CLOSE_UP_DELAY = 2000L;
		AUTO_FARM_IGNORED_NPC_ID = new int[0];
		FARM_EXPEND_LIMIT_PRICE = new int[]{4037, 1};
		ALLOW_AUTO_FARM = true;
		FARM_ONLINE_TYPE = true;
		AUTO_FARM_FOR_PREMIUM = false;
		AUTO_FARM_FREE = true;
		PREMIUM_FARM_FREE = true;
		ALLOW_FARM_FREE_TIME = false;
		REFRESH_FARM_TIME = false;
		FARM_FREE_TIME = 3;
		ALLOW_CHECK_HWID_LIMIT = false;
		FARM_ACTIVE_LIMITS = 3;
		SHORTCUT_PAGE = 10;
		SEARCH_DISTANCE = 3000;
		FARM_TYPE = 0;
		FARM_INTERVAL_TASK = 600;
		RUN_CLOSE_UP_DISTANCE = 100;
		SERVICE_AUTO_FARM_SET_RED_RING = false;
		SERVICES_AUTO_FARM_ABNORMAL = null;
		AUTO_FARM_LIMIT_ZONE_NAMES = new HashSet<>(Arrays.asList("[giran_town_peace1],[giran_town_peace2]"));
	}
}
