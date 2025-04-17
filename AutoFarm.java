import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.premium.game.cache.HtmCache;
import com.premium.game.datatables.sql.ItemTable;
import com.premium.game.datatables.xml.SkillTable;
import com.premium.game.handler.IVoicedCommandHandler;
import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.L2Summon;
import com.premium.game.model.actor.instance.L2ItemInstance;
import com.premium.game.model.actor.instance.L2NpcInstance;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.network.serverpackets.L2GameServerPacket;
import com.premium.game.network.serverpackets.NpcHtmlMessage;
import com.premium.game.templates.item.L2Item;
import com.premium.game.templates.skills.L2SkillType;
import com.premium.util.Strings;

public class AutoFarm implements IVoicedCommandHandler {

	protected static Logger _log = Logger.getLogger(AutoFarm.class);

	private static final String[] _voicedCommands = {
			"autofarm", "farmstart", "farmstop", "farmprofiles", "farmsettings", 
			"farmreports", "farmuse", "farmedit", "farmdelete", "farmnew",
			"farmsavesettings", "farmreportguild"
	};

	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String params) {
		if (!AutoFarmConfig.ALLOW_AUTO_FARM) {
			return false;
		}
		
		if (player == null) {
			return false;
		}
		
		AutoFarmContext farmSystem = PlayerA.getFarmSystem(player.getObjectId());
		
		switch (command) {
			case "autofarm":
				AutoFarmUI.showMainMenu(player);
				return true;
				
			case "farmstart":
				if (params == null || params.isEmpty()) {
					AutoFarmUI.showProfilesMenu(player);
					return true;
				}
				
				AutoFarmProfile.FarmProfile profile = AutoFarmProfile.getProfile(player, params);
				if (profile == null) {
					player.sendMessage("Perfil não encontrado.");
					return false;
				}
				
				// Inicia o farm com o perfil selecionado
				if (farmSystem.isActiveAutofarm()) {
					AutoFarmReport.startSession(player, profile.getName());
					applyProfile(player, farmSystem, profile);
					farmSystem.startFarmTask();
					player.sendMessage("Farm iniciado com o perfil: " + profile.getName());
				} else {
					player.sendMessage("Você precisa comprar o AutoFarm primeiro.");
				}
				return true;
				
			case "farmstop":
				if (farmSystem.isActiveAutofarm()) {
					farmSystem.stopFarmTask();
					AutoFarmReport.FarmSession session = AutoFarmReport.endSession(player);
					if (session != null) {
						player.sendMessage("Farm finalizado. Duração: " + (session.getDuration() / 60000) + " minutos");
					}
				}
				AutoFarmUI.showMainMenu(player);
				return true;
				
			case "farmprofiles":
				AutoFarmUI.showProfilesMenu(player);
				return true;
				
			case "farmsettings":
				AutoFarmUI.showSettingsMenu(player);
				return true;
				
			case "farmreports":
				AutoFarmUI.showReportsMenu(player);
				return true;
				
			case "farmuse":
				if (params == null || params.isEmpty()) {
					player.sendMessage("Perfil não especificado.");
					return false;
				}
				return useVoicedCommand("farmstart", player, params);
				
			case "farmedit":
				if (params == null || params.isEmpty()) {
					player.sendMessage("Perfil não especificado.");
					return false;
				}
				// TODO: Implementar edição de perfil
				return true;
				
			case "farmdelete":
				if (params == null || params.isEmpty()) {
					player.sendMessage("Perfil não especificado.");
					return false;
				}
				if (AutoFarmProfile.removeProfile(player, params)) {
					player.sendMessage("Perfil removido com sucesso.");
					AutoFarmProfile.saveProfiles(player);
				} else {
					player.sendMessage("Falha ao remover perfil.");
				}
				AutoFarmUI.showProfilesMenu(player);
				return true;
				
			case "farmnew":
				// TODO: Implementar criação de novo perfil
				return true;
				
			case "farmsavesettings":
				if (params == null) {
					return false;
				}
				String[] settings = params.split(" ");
				if (settings.length < 7) {
					return false;
				}
				
				try {
					int dist = Integer.parseInt(settings[0]);
					boolean useSummon = "Sim".equals(settings[1]);
					boolean autoLoot = "Sim".equals(settings[2]);
					int hp = Integer.parseInt(settings[3]);
					int mp = Integer.parseInt(settings[4]);
					boolean useParty = "Sim".equals(settings[5]);
					boolean useGuild = "Sim".equals(settings[6]);
					
					// Aplica as configurações
					farmSystem.setSearchDistance(dist);
					farmSystem.setUseSummon(useSummon);
					farmSystem.setAutoLoot(autoLoot);
					farmSystem.setMinHpPercent(hp);
					farmSystem.setMinMpPercent(mp);
					farmSystem.setUsePartySync(useParty);
					farmSystem.setUseGuildReports(useGuild);
					
					player.sendMessage("Configurações salvas com sucesso.");
				} catch (Exception e) {
					player.sendMessage("Erro ao salvar configurações.");
					_log.error("Erro ao salvar configurações do AutoFarm", e);
				}
				AutoFarmUI.showSettingsMenu(player);
				return true;
				
			case "farmreportguild":
				AutoFarmReport.FarmSession session = AutoFarmReport.getActiveSession(player);
				if (session != null && player.getClan() != null) {
					AutoFarmReport.sendGuildReport(player, session);
					player.sendMessage("Relatório enviado para a guild.");
				}
				return true;
		}
		
		return false;
	}
	
	/**
	 * Aplica as configurações de um perfil ao sistema de farm
	 */
	private void applyProfile(L2PcInstance player, AutoFarmContext farmSystem, AutoFarmProfile.FarmProfile profile) {
		farmSystem.setSearchDistance(profile.getSearchDistance());
		farmSystem.setUseSummon(profile.isUseSummon());
		farmSystem.setAutoLoot(profile.isAutoLoot());
		farmSystem.setMinHpPercent(profile.getMinHpPercent());
		farmSystem.setMinMpPercent(profile.getMinMpPercent());
		farmSystem.setUsePartySync(profile.isUsePartySync());
		
		// Configura as skills
		farmSystem.clearAllSkills();
		profile.getAttackSkills().forEach(farmSystem::addAttackSkill);
		profile.getBuffSkills().forEach(farmSystem::addBuffSkill);
		profile.getHealSkills().forEach(farmSystem::addHealSkill);
		
		if (profile.isUseSummon()) {
			profile.getSummonSkills().forEach(farmSystem::addSummonSkill);
		}
	}

	@Override
	public String[] getVoicedCommandList() {
		return _voicedCommands;
	}

	@Override
	public String getDescription(String arg0) {
	
		return null;
	}

}
