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

	public static long SKILLS_EXTRA_DELAY;

	public static int FAST_SKILL_REUSE;

	public static long KEEP_LOCATION_DELAY;

	public static long RUN_CLOSE_UP_DELAY;

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

	public static int[] ACP_HP_POTION_IDS;

	public static int[] ACP_MP_POTION_IDS;

	public static AbnormalEffect SERVICES_AUTO_FARM_ABNORMAL = AbnormalEffect.NULL;

	public static boolean SERVICE_AUTO_FARM_SET_RED_RING;

	public static Set<String> AUTO_FARM_LIMIT_ZONE_NAMES = new HashSet<>();

	public static String LICENSE_CODE;

	public static void load() {

		try {
			L2Properties exProperties = new L2Properties(AUTO_FARM_CONFIG_FILE);

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
			SKILLS_EXTRA_DELAY = Long.parseLong(exProperties.getProperty("SkillsExtraDelay", "5")) * 1000L;
			FAST_SKILL_REUSE = Integer.parseInt(exProperties.getProperty("FastSkillReuse", "90"));
			KEEP_LOCATION_DELAY = Long.parseLong(exProperties.getProperty("KeepLocationDelay", "5")) * 1000L;
			RUN_CLOSE_UP_DELAY = Long.parseLong(exProperties.getProperty("RunCloseUpDelay", "2")) * 1000L;
			RUN_CLOSE_UP_DISTANCE = Integer.parseInt(exProperties.getProperty("RunCloseUpDistance", "100"));
			ALLOW_FARM_FREE_TIME = Boolean.parseBoolean(exProperties.getProperty("AllowFarmFreeTime", "False"));
			REFRESH_FARM_TIME = Boolean.parseBoolean(exProperties.getProperty("AllowRefreshFarmTime", "False"));
			FARM_FREE_TIME = Integer.parseInt(exProperties.getProperty("FarmFreeTime", "3"));
			ALLOW_CHECK_HWID_LIMIT = Boolean.parseBoolean(exProperties.getProperty("AllowCheckHwidLimits", "False"));
			FARM_ACTIVE_LIMITS = Integer.parseInt(exProperties.getProperty("FarmActiveLimits", "3"));

			
			String[] npcs = exProperties.getProperty("AutoFarmIgnoreMobIds").split(",");

			AUTO_FARM_IGNORED_NPC_ID = new int[npcs.length];
			for (int i = 0; i < npcs.length; i++) {
				AUTO_FARM_IGNORED_NPC_ID[i] = Integer.parseInt(npcs[i]);
			}

			String abnormalEffectName = exProperties.getProperty("AutoFarmAbnormalEffectName", "null");
			// "dummy", "null", "none" or empty = no effect
			if (abnormalEffectName == null || abnormalEffectName.isEmpty() || 
				abnormalEffectName.equalsIgnoreCase("null") || 
				abnormalEffectName.equalsIgnoreCase("none") ||
				abnormalEffectName.equalsIgnoreCase("dummy")) {
				SERVICES_AUTO_FARM_ABNORMAL = AbnormalEffect.NULL;
			} else {
				try {
					SERVICES_AUTO_FARM_ABNORMAL = AbnormalEffect.getByName(abnormalEffectName);
				} catch (Exception e) {
					SERVICES_AUTO_FARM_ABNORMAL = AbnormalEffect.NULL;
				}
			}

			SERVICE_AUTO_FARM_SET_RED_RING = Boolean
					.parseBoolean(exProperties.getProperty("AutoFarmSetRedRing", "false"));
			AUTO_FARM_LIMIT_ZONE_NAMES = new HashSet<>(Arrays.asList(
					exProperties.getProperty("AutoFarmProhibitedZones", "[giran_town_peace1],[giran_town_peace2]")));
			String[] arrayOfString2 = exProperties.getProperty("FarmExpendLimitPrice", "4037,1").split(",");

			FARM_EXPEND_LIMIT_PRICE[0] = Integer.parseInt(arrayOfString2[0]);
			FARM_EXPEND_LIMIT_PRICE[1] = Integer.parseInt(arrayOfString2[1]);

			String[] hpPotions = exProperties.getProperty("AcpHpPotionIds", "1540,1539,1060,8627,8626,8625,8624,8623,8622").split(",");
			ACP_HP_POTION_IDS = new int[hpPotions.length];
			for (int i = 0; i < hpPotions.length; i++) {
				ACP_HP_POTION_IDS[i] = Integer.parseInt(hpPotions[i].trim());
			}

			String[] mpPotions = exProperties.getProperty("AcpMpPotionIds", "728,726,8639,8638,8637,8636,8635,8634").split(",");
			ACP_MP_POTION_IDS = new int[mpPotions.length];
			for (int i = 0; i < mpPotions.length; i++) {
				ACP_MP_POTION_IDS[i] = Integer.parseInt(mpPotions[i].trim());
			}

		 } catch (Exception e) {
			_log.error(e.getMessage(), e);
			_log.info("AutoFarmPremium: Error while reading config", e);

		}

	}
}
