
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

	@Override
	public boolean useVoicedCommand(String s, final L2PcInstance player, String s2) {

		if (s.startsWith("voice_")) {
			s = s.substring(6);
		}

		if (!AutoFarmConfig.ALLOW_AUTO_FARM ) {
			return false;
		}

		if(player == null) {
			return false;
		}

		AutoFarmContext farmSystem = PlayerA.getFarmSystem(player.getObjectId());

		int varInt = player.getVarInt("farmType", AutoFarmConfig.FARM_TYPE);
		if (varInt < 0 || varInt > 4) {
			varInt = 0;
			player.setVarInt("farmType", 0);
		}
		if (s.equalsIgnoreCase("farmstart")) {
			final String[] split = s2.split(" ");
			String s3 = "attack";
			try {
				s3 = split[0];
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (s3 == null || s3.isEmpty()) {
				s3 = "attack";
			}
			if (farmSystem.isActiveAutofarm()) {
				farmSystem.startFarmTask();
			} else {
				player.sendMessage("Cant activate AutoFarm. You have to purchase it.");
			}
			this.amain(player, farmSystem, null, varInt, s3);
			return true;
		}
		if (s.equalsIgnoreCase("editSummonSkills")) {
			final String[] split2 = s2.split(" ");
			String s4 = "attack";
			String s5 = "attack";
			try {
				s5 = split2[0];
			} catch (Exception ex2) {
			}
			try {
				s4 = split2[1];
			} catch (Exception ex3) {
			}
			if (s4 == null || s4.isEmpty()) {
				s4 = "attack";
			}
			if (s5 == null || s5.isEmpty()) {
				s5 = "attack";
			}
			if (!farmSystem.isUseSummonSkills()) {
				player.sendMessage("You cant edit settings. The option is disabled.");
				this.amain(player, farmSystem, null, varInt, s4);
				return false;
			}
			if (player.getPet() == null) {
				player.sendMessage("You cant use this option without a pet.");
				this.amain(player, farmSystem, null, varInt, s4);
				return false;
			}
			this.a(player, farmSystem, null, varInt, s5, s4);
			return true;
		} else {
			if (s.equalsIgnoreCase("changeSkillType")) {
				final String[] split3 = s2.split(" ");
				String s6 = "attack";
				try {
					s6 = split3[0];
				} catch (Exception ex4) {
				}
				if (s6 == null || s6.isEmpty()) {
					s6 = "attack";
				}
				this.amain(player, farmSystem, null, varInt, s6);
				return true;
			}
			if (s.equalsIgnoreCase("refreshSkills")) {
				final String[] split4 = s2.split(" ");
				String s7 = "attack";
				String s8 = "1";
				try {
					s7 = split4[0];
				} catch (Exception ex5) {
					ex5.printStackTrace();
				}
				try {
					s8 = split4[1];
				} catch (Exception ex6) {
					ex6.printStackTrace();
				}
				if (s7 == null || s7.isEmpty()) {
					s7 = "attack";
				}
				if (s8 == null || s8.isEmpty()) {
					s8 = "1";
				}
				try {
					farmSystem.setShortcutPageValue(Integer.parseInt(s8.trim()));
				} catch (Exception e) {
					_log.error("AutoFarm: Error parsing shortcut page: " + s8);
				}
				farmSystem.checkAllSlots();
				this.amain(player, farmSystem, null, varInt, s7);
				return true;
			}
			if (s.equalsIgnoreCase("removeSkill")) {
				final String[] split5 = s2.split(" ");
				String s9 = null;
				String s10 = null;
				try {
					s9 = split5[0];
				} catch (Exception ex7) {
				}
				try {
					s10 = split5[1];
				} catch (Exception ex8) {
				}
				if (s9 == null || s9.isEmpty()) {
					s9 = "attack";
				}
				if (s9 != null && s10 != null) {
					List<Integer> list = null;
					final String s11 = s9;
					switch (s11) {
					case "attack": {
						list = farmSystem.getAttackSpells();
						break;
					}
					case "chance": {
						list = farmSystem.getChanceSpells();
						break;
					}
					case "self": {
						list = farmSystem.getSelfSpells();
						break;
					}
					case "heal": {
						list = farmSystem.getLowLifeSpells();
						break;
					}
					}
					int skillId = -1;
					try {
						skillId = Integer.parseInt(s10.trim());
					} catch (Exception e) {}
					
					if (list != null && skillId > 0 && list.contains(skillId)) {
						list.remove((Object) skillId);
						final String s12 = s9;
						switch (s12) {
						case "attack": {
							farmSystem.saveSkills("farmAttackSkills");
							break;
						}
						case "chance": {
							farmSystem.saveSkills("farmChanceSkills");
							break;
						}
						case "self": {
							farmSystem.saveSkills("farmSelfSkills");
							break;
						}
						case "heal": {
							farmSystem.saveSkills("farmHealSkills");
							break;
						}
						}
					}
				}
				this.amain(player, farmSystem, null, varInt, s9);
				return true;
			}
			if (s.equalsIgnoreCase("removeSummonSkill")) {
				final String[] split6 = s2.split(" ");
				String s13 = null;
				String s14 = null;
				String s15 = null;
				try {
					s13 = split6[0];
				} catch (Exception ex9) {
				}
				try {
					s14 = split6[1];
				} catch (Exception ex10) {
				}
				try {
					s15 = split6[2];
				} catch (Exception ex11) {
				}
				if (s13 != null && s14 != null && s15 != null) {
					List<Integer> list2 = null;
					final String s16 = s13;
					switch (s16) {
					case "attack": {
						list2 = farmSystem.getSummonAttackSpells();
						break;
					}
					case "self": {
						list2 = farmSystem.getSummonSelfSpells();
						break;
					}
					case "heal": {
						list2 = farmSystem.getSummonHealSpells();
						break;
					}
					}
					int skillId = -1;
					try {
						skillId = Integer.parseInt(s14.trim());
					} catch (Exception e) {}
					
					if (list2 != null && skillId > 0 && list2.contains(skillId)) {
						list2.remove((Object) skillId);
						final String s17 = s15;
						switch (s17) {
						case "attack": {
							farmSystem.saveSkills("farmAttackSummonSkills");
							break;
						}
						case "self": {
							farmSystem.saveSkills("farmSelfSummonSkills");
							break;
						}
						case "heal": {
							farmSystem.saveSkills("farmHealSummonSkills");
							break;
						}
						}
					}
				}
				this.a(player, farmSystem, null, varInt, s13, s15);
				return true;
			}
			if (s.equalsIgnoreCase("addSkill")) {
				final String[] split7 = s2.split(" ");
				String s18 = null;
				String s19 = "1";
				try {
					s18 = split7[0];
				} catch (Exception ex12) {
				}
				try {
					s19 = split7[1];
				} catch (Exception ex13) {
				}
				if (s18 != null) {
					try {
						this.axo(player, farmSystem, s18, Integer.parseInt(s19.trim()));
					} catch (Exception e) {
						_log.error("AutoFarm: Error parsing skill index: " + s19);
					}
				}
				return true;
			}
			if (s.equalsIgnoreCase("addSummonSkill")) {
				final String[] split8 = s2.split(" ");
				String s20 = null;
				String s21 = null;
				String s22 = "1";
				try {
					s20 = split8[0];
				} catch (Exception ex14) {
				}
				try {
					s21 = split8[1];
				} catch (Exception ex15) {
				}
				try {
					s22 = split8[2];
				} catch (Exception ex16) {
				}
				if (s20 != null && s21 != null) {
					try {
						this.astt(player, farmSystem, varInt, s20, s21, Integer.parseInt(s22.trim()));
					} catch (Exception e) {
						_log.error("AutoFarm: Error parsing summon skill index: " + s22);
					}
				}
				return true;
			}
			if (s.equalsIgnoreCase("addNewSkill")) {
				final String[] split9 = s2.split(" ");
				String s23 = null;
				String s24 = null;
				try {
					s23 = split9[0];
				} catch (Exception ex17) {
				}
				try {
					s24 = split9[1];
				} catch (Exception ex18) {
				}
				if (s24 == null || s24.isEmpty()) {
					s24 = "attack";
				}
				if (s23 != null && s24 != null) {
					int skillId = -1;
					try {
						skillId = Integer.parseInt(s23.trim());
					} catch (Exception e) {}
					
					final L2Skill knownSkill = player.getKnownSkill(skillId);
					if (knownSkill != null) {
						final String s25 = s24;
						switch (s25) {
						case "attack": {
							if (farmSystem.getAttackSpells().size() >= 8) {
								this.amain(player, farmSystem, null, varInt, s24);
								return false;
							}
							if (!knownSkill.isSpoilSkill() && !knownSkill.isSweepSkill() && knownSkill.getId() != 1263
									&& (knownSkill.getSkillType() == L2SkillType.AGGDAMAGE
											|| knownSkill.getSkillType() == L2SkillType.PDAM
											|| knownSkill.getSkillType() == L2SkillType.MANADAM
											|| knownSkill.getSkillType() == L2SkillType.MDAM
											|| knownSkill.getSkillType() == L2SkillType.DRAIN
											|| knownSkill.getSkillType() == L2SkillType.CPDAM
											|| knownSkill.getSkillType() == L2SkillType.STUN)) {
								farmSystem.getAttackSpells().add(knownSkill.getId());
								farmSystem.saveSkills("farmAttackSkills");
								break;
							}
							break;
						}
						case "chance": {
							if (farmSystem.getChanceSpells().size() >= 8) {
								this.amain(player, farmSystem, null, varInt, s24);
								return false;
							}
							if (knownSkill.getSkillType() == L2SkillType.DOT
									|| knownSkill.getSkillType() == L2SkillType.MDOT
									|| knownSkill.getSkillType() == L2SkillType.POISON
									|| knownSkill.getSkillType() == L2SkillType.BLEED
									|| knownSkill.getSkillType() == L2SkillType.DEBUFF
									|| knownSkill.getSkillType() == L2SkillType.SLEEP
									|| knownSkill.getSkillType() == L2SkillType.ROOT
									|| knownSkill.getSkillType() == L2SkillType.PARALYZE
									|| knownSkill.getSkillType() == L2SkillType.MUTE || knownSkill.isSpoilSkill()
									|| knownSkill.isSweepSkill() || knownSkill.getId() == 1263) {
								farmSystem.getChanceSpells().add(knownSkill.getId());
								farmSystem.saveSkills("farmChanceSkills");
								break;
							}
							break;
						}
						case "self": {
							if (farmSystem.getSelfSpells().size() >= 8) {
								this.amain(player, farmSystem, null, varInt, s24);
								return false;
							}
							if (!knownSkill.isToggle() && !knownSkill.isMagic()
									&& knownSkill.getSkillType() != L2SkillType.BUFF && !knownSkill.isCubic()) {
								return false;
							}
							farmSystem.getSelfSpells().add(knownSkill.getId());
							farmSystem.saveSkills("farmSelfSkills");
							break;
						}
						case "heal": {
							if (farmSystem.getLowLifeSpells().size() >= 8) {
								this.amain(player, farmSystem, null, varInt, s24);
								return false;
							}
							if (knownSkill.getSkillType() != L2SkillType.DRAIN
									&& knownSkill.getSkillType() != L2SkillType.HEAL
									&& knownSkill.getSkillType() != L2SkillType.HEAL_PERCENT
									&& knownSkill.getSkillType() != L2SkillType.MANAHEAL
									&& knownSkill.getSkillType() != L2SkillType.MANAHEAL_PERCENT) {
								return false;
							}
							farmSystem.getLowLifeSpells().add(knownSkill.getId());
							farmSystem.saveSkills("farmHealSkills");
							break;
						}
						}
					}
				}
				this.amain(player, farmSystem, null, varInt, s24);
				return true;
			}
			if (s.equalsIgnoreCase("addNewSummonSkill")) {
				final String[] split10 = s2.split(" ");
				String s26 = null;
				String s27 = null;
				String s28 = null;
				try {
					s26 = split10[0];
				} catch (Exception ex19) {
				}
				try {
					s27 = split10[1];
				} catch (Exception ex20) {
				}
				try {
					s28 = split10[2];
				} catch (Exception ex21) {
				}
				if (s26 != null && s27 != null && s28 != null) {
					if (player.getPet() == null) {
						player.sendMessage("You cant use this option.");
						this.a(player, farmSystem, null, varInt, s27, s28);
						return false;
					}
					final L2Summon pet = player.getPet();
					if (pet.getPet() != null && pet.getLevel() - player.getLevel() > 20) {
						player.sendMessage("YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL");
						this.a(player, farmSystem, null, varInt, s27, s28);
						return false;
					}
					final int availableSkillLevel = pet.getAllSkills().length;
					if (availableSkillLevel > 0) {
						int skillId = -1;
						try {
							skillId = Integer.parseInt(s26.trim());
						} catch (Exception e) {}
						
						final L2Skill info = SkillTable.getInstance().getInfo(skillId,
								availableSkillLevel);
						if (info != null) {
							final String s29 = s27;
							switch (s29) {
							case "attack": {
								if (farmSystem.getSummonAttackSpells().size() >= 8) {
									this.a(player, farmSystem, null, varInt, s27, s28);
									return false;
								}
								if (info.getSkillType() == L2SkillType.AGGDAMAGE
										|| info.getSkillType() == L2SkillType.PDAM
										|| info.getSkillType() == L2SkillType.MANADAM
										|| info.getSkillType() == L2SkillType.MDAM
										|| info.getSkillType() == L2SkillType.DRAIN
										|| info.getSkillType() == L2SkillType.CPDAM
										|| info.getSkillType() == L2SkillType.STUN) {
									farmSystem.getSummonAttackSpells().add(info.getId());
									farmSystem.saveSkills("farmAttackSummonSkills");
									break;
								}
								break;
							}
							case "self": {
								if (farmSystem.getSummonSelfSpells().size() >= 8) {
									this.a(player, farmSystem, null, varInt, s27, s28);
									return false;
								}
								if (!info.isToggle() && !info.isMusic() && info.getSkillType() != L2SkillType.BUFF
										&& !info.isCubic()) {
									return false;
								}
								farmSystem.getSummonSelfSpells().add(info.getId());
								farmSystem.saveSkills("farmSelfSummonSkills");
								break;
							}
							case "heal": {
								if (farmSystem.getSummonHealSpells().size() >= 8) {
									this.a(player, farmSystem, null, varInt, s27, s28);
									return false;
								}
								if (info.getSkillType() != L2SkillType.DRAIN && info.getSkillType() != L2SkillType.HEAL
										&& info.getSkillType() != L2SkillType.HEAL_PERCENT
										&& info.getSkillType() != L2SkillType.MANAHEAL
										&& info.getSkillType() != L2SkillType.MANAHEAL_PERCENT) {
									return false;
								}
								farmSystem.getSummonHealSpells().add(info.getId());
								farmSystem.saveSkills("farmHealSummonSkills");
								break;
							}
							}
						}
					}
				}
				this.a(player, farmSystem, null, varInt, s27, s28);
				return true;
			}
			if (s.equalsIgnoreCase("editFarmOption")) {
				final String[] split11 = s2.split(" ");
				String s30 = "attack";
				String s31 = null;
				try {
					s30 = split11[0];
				} catch (Exception ex22) {
				}
				try {
					s31 = split11[1];
				} catch (Exception ex23) {
				}
				if (s30 != null && s31 != null) {
					boolean b = false;
					switch (varInt) {
					case 0: 
						if (s31.equalsIgnoreCase("farmLeaderAssist")) {
							farmSystem.setLeaderAssist(!farmSystem.isLeaderAssist(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmAssistMonsterAttack")) {
							farmSystem.setAssistMonsterAttack(!farmSystem.isAssistMonsterAttack(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmKeepLocation")) {
							farmSystem.setKeepLocation(player.getLoc(), !farmSystem.isKeepLocation(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmDelaySkills")) {
							farmSystem.setExDelaySkill(!farmSystem.isExtraDelaySkill(), false);
							b = true;
							break;
						}
						break;
					
					case 1: 
						if (s31.equalsIgnoreCase("farmLeaderAssist")) {
							farmSystem.setLeaderAssist(!farmSystem.isLeaderAssist(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmAssistMonsterAttack")) {
							farmSystem.setAssistMonsterAttack(!farmSystem.isAssistMonsterAttack(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmKeepLocation")) {
							farmSystem.setKeepLocation(player.getLoc(), !farmSystem.isKeepLocation(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmDelaySkills")) {
							farmSystem.setExDelaySkill(!farmSystem.isExtraDelaySkill(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmRunTargetCloseUp")) {
							farmSystem.setRunTargetCloseUp(!farmSystem.isRunTargetCloseUp(), false);
							b = true;
							break;
						}
						break;
					
					case 2: 
						if (s31.equalsIgnoreCase("farmLeaderAssist")) {
							farmSystem.setLeaderAssist(!farmSystem.isLeaderAssist(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmAssistMonsterAttack")) {
							farmSystem.setAssistMonsterAttack(!farmSystem.isAssistMonsterAttack(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmKeepLocation")) {
							farmSystem.setKeepLocation(player.getLoc(), !farmSystem.isKeepLocation(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmRunTargetCloseUp")) {
							farmSystem.setRunTargetCloseUp(!farmSystem.isRunTargetCloseUp(), false);
							b = true;
							break;
						}
						break;
					
					case 3: 
						if (s31.equalsIgnoreCase("farmLeaderAssist")) {
							farmSystem.setLeaderAssist(!farmSystem.isLeaderAssist(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmAssistMonsterAttack")) {
							farmSystem.setAssistMonsterAttack(!farmSystem.isAssistMonsterAttack(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmTargetRestoreMp")) {
							farmSystem.setTargetRestoreMp(!farmSystem.isTargetRestoreMp(), false);
							b = true;
							break;
						}
						break;
					
					case 4: 
						if (s31.equalsIgnoreCase("farmLeaderAssist")) {
							farmSystem.setLeaderAssist(!farmSystem.isLeaderAssist(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmAssistMonsterAttack")) {
							farmSystem.setAssistMonsterAttack(!farmSystem.isAssistMonsterAttack(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmKeepLocation")) {
							farmSystem.setKeepLocation(player.getLoc(), !farmSystem.isKeepLocation(), false);
							b = true;
							break;
						}
						if (s31.equalsIgnoreCase("farmUseSummonSkills")) {
							if (player.getPet() == null) {
								player.sendMessage("YOU CANT USE THIS OPTION.");
								this.amain(player, farmSystem, null, varInt, s30);
								return true;
							}
							farmSystem.setUseSummonSkills(!farmSystem.isUseSummonSkills(), false);
							b = true;
							break;
						} else {
							if (s31.equalsIgnoreCase("farmDelaySkills")) {
								farmSystem.setExDelaySkill(!farmSystem.isExtraDelaySkill(), false);
								b = true;
								break;
							}
							break;
						}
					}
					if (b) {
					
						this.amain(player, farmSystem, null, varInt, s30);
						return true;
					}
					final String s32 = s30;
					switch (s32) {
					case "attack": {
						if (s31.equalsIgnoreCase("farmRndAttackSkills")) {
							farmSystem.setRndAttackSkills(!farmSystem.isRndAttackSkills(), false);
						}
						this.amain(player, farmSystem, null, varInt, s30);
						return true;
					}
					case "chance": {
						if (s31.equalsIgnoreCase("farmRndChanceSkills")) {
							farmSystem.setRndChanceSkills(!farmSystem.isRndChanceSkills(), false);
						}
						this.amain(player, farmSystem, null, varInt, s30);
						return true;
					}
					case "self": {
						if (s31.equalsIgnoreCase("farmRndSelfSkills")) {
							farmSystem.setRndSelfSkills(!farmSystem.isRndSelfSkills(), false);
						}
						this.amain(player, farmSystem, null, varInt, s30);
						return true;
					}
					case "heal": {
						if (s31.equalsIgnoreCase("farmRndLifeSkills")) {
							farmSystem.setRndLifeSkills(!farmSystem.isRndLifeSkills(), false);
						}
						this.amain(player, farmSystem, null, varInt, s30);
						return true;
					}
					}
				}
				this.amain(player, farmSystem, null, varInt, s30);
				return true;
			}
			if (s.equalsIgnoreCase("editSummonFarmOption")) {
				final String[] split12 = s2.split(" ");
				String s33 = "attack";
				String s34 = "attack";
				String s35 = null;
				try {
					s33 = split12[0];
				} catch (Exception ex24) {
				}
				try {
					s35 = split12[1];
				} catch (Exception ex25) {
				}
				try {
					s34 = split12[2];
				} catch (Exception ex26) {
				}
				if (s33 != null && s35 != null) {
					if (s35.equalsIgnoreCase("farmSummonDelaySkills")) {
						farmSystem.setExSummonDelaySkill(!farmSystem.isExtraSummonDelaySkill(), false);
						this.a(player, farmSystem, null, varInt, s33, s34);
						return true;
					}
					final String s36 = s33;
					switch (s36) {
					case "attack": {
						if (s35.equalsIgnoreCase("farmRndSummonAttackSkills")) {
							farmSystem.setRndSummonAttackSkills(!farmSystem.isRndSummonAttackSkills(), false);
						}
						this.a(player, farmSystem, null, varInt, s33, s34);
						return true;
					}
					case "self": {
						if (s35.equalsIgnoreCase("farmRndSummonSelfSkills")) {
							farmSystem.setRndSummonSelfSkills(!farmSystem.isRndSummonSelfSkills(), false);
						}
						this.a(player, farmSystem, null, varInt, s33, s34);
						return true;
					}
					case "heal": {
						if (s35.equalsIgnoreCase("farmRndSummonLifeSkills")) {
							farmSystem.setRndSummonLifeSkills(!farmSystem.isRndSummonLifeSkills(), false);
						}
						this.a(player, farmSystem, null, varInt, s33, s34);
						return true;
					}
					}
				}
				this.a(player, farmSystem, null, varInt, s33, s34);
				return true;
			}
			if (s.equalsIgnoreCase("farmstop")) {
	
				farmSystem.stopFarmTask(false);
				this.amain(player, farmSystem, null, varInt, "attack");
			} else if (s.equalsIgnoreCase("expendLimit")) {
				if (farmSystem.isAutofarming()) {
					this.amain(player, farmSystem, null, varInt, "attack");
					return false;
				}
				if (AutoFarmManager.getInstance().isNonCheckPlayer(player.getObjectId())) {
					player.sendMessage("You have alread use this service.");
					this.amain(player, farmSystem, null, varInt, "attack");
					return false;
				}
				if (AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[0] != 0) {
					L2ItemInstance itemI = player.getInventory()
							.getItemByItemId(AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[0]);
					if (itemI == null
							|| player.getInventory().getItemByItemId(AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[0])
									.getCount() < AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[1]) {

						final L2Item template = ItemTable.getInstance()
								.getTemplate(AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[0]);
						if (template != null) {
							player.sendMessage("To use the service you must have "
									+ AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[1] + template.getName());
						}
						this.amain(player, farmSystem, null, varInt, "attack");
						return false;
					}
					player.getInventory().destroyItem("Autofarm consume", itemI.getObjectId(),
							AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[1], player, null);
					_log.warn(player.getName() + " bought expend period " + AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[0]
							+ " amount " + AutoFarmConfig.FARM_EXPEND_LIMIT_PRICE[1]);
				}
				AutoFarmManager.getInstance().addNonCheckPlayer(player.getObjectId());
				player.sendMessage("You character is excluded from limits list.");
				this.amain(player, farmSystem, null, varInt, "attack");
			} else if (s.equalsIgnoreCase("buyfarm")) {
				if (farmSystem.isActiveAutofarm()) {
					player.sendMessage("You alread have active autofarm time.");
					this.amain(player, farmSystem, null, varInt, "attack");
					return false;
				}
				this.abuy(player, farmSystem);
				return true;
			} else if (s.startsWith("tryFreeTime")) {
				final String[] split13 = s2.split(" ");
				String s37 = "attack";
				try {
					s37 = split13[0];
				} catch (Exception ex27) {
				}
				if (s37 == null || s37.isEmpty()) {
					s37 = "attack";
				}
				final long varLong = player.getVarLong("farmFreeTime", 0L);
				if (!AutoFarmConfig.ALLOW_FARM_FREE_TIME || varLong > 0L) {
					player.sendMessage("Function is not available.");
					this.abuy(player, farmSystem);
					return false;
				}
				final long autoFarmEndTask = System.currentTimeMillis() + AutoFarmConfig.FARM_FREE_TIME * 3600000L;
				if (AutoFarmConfig.FARM_ONLINE_TYPE) {
					player.setVarLong("activeFarmOnlineTask", autoFarmEndTask - System.currentTimeMillis());
					player.setVarLong("activeFarmOnlineTime", 0);
					farmSystem.refreshFarmOnlineTime();
				} else {
					player.setVarLong("activeFarmTask", autoFarmEndTask);
					farmSystem.setAutoFarmEndTask(autoFarmEndTask);
				}
				player.setVarLong("farmFreeTime", autoFarmEndTask);
				farmSystem.checkFarmTask();
				this.amain(player, farmSystem, null, varInt, s37);
				player.sendMessage("YOU HAVE SUCCESSFULLY ACTIVATE AUTO FARM FREE TIME SERVICE.");
			} else {
				if (s.equalsIgnoreCase("buyfarmTime")) {
					final String[] split14 = s2.split(" ");
					String s38 = null;
					String s39 = "attack";
					try {
						s38 = split14[0];
					} catch (Exception ex28) {

					}
					try {
						s39 = split14[1];
					} catch (Exception ex29) {
					}
					if (s38 != null) {
						final int int1 = Integer.parseInt(s38);
						boolean b2 = false;
						int int2 = 0;
						long long1 = 0L;
						final long autoFarmEndTask2 = System.currentTimeMillis() + int1 * 3600000L;
						for (final int intValue : AutoFarmConfig.AUTO_FARM_PRICES.keySet()) {
							if (intValue == int1) {
								b2 = true;
								final String[] split15 = AutoFarmConfig.AUTO_FARM_PRICES.get(intValue).split(":");
								if (split15 != null && split15.length == 2) {
									int2 = Integer.parseInt(split15[0]);
									long1 = Long.parseLong(split15[1]);
									break;
								}
								break;
							}
						}
						if (b2) {
							if (int2 != 0) {
								L2ItemInstance itemI = player.getInventory().getItemByItemId(int2);
								if (player.getInventory().getItemByItemId(int2) == null || itemI.getCount() < long1) {
									final L2Item template2 = ItemTable.getInstance().getTemplate(int2);
									if (template2 != null) {
										player.sendMessage(
												"TO USE THE SERVICE YOU MUST HAVE" + long1 + template2.getName());
									}
									this.abuy(player, farmSystem);
									return false;
								}
								player.getInventory().destroyItem("Autofarm consume", itemI.getObjectId(), (int) long1,
										player, null);
								_log.warn(player + "bought farm period " + int2 + " amount " + long1);
							}
							if (AutoFarmConfig.FARM_ONLINE_TYPE) {
								player.setVarLong("activeFarmOnlineTask",
										autoFarmEndTask2 - System.currentTimeMillis());
								player.setVarLong("activeFarmOnlineTime", 0);
								farmSystem.refreshFarmOnlineTime();
							} else {
								player.setVarLong("activeFarmTask", autoFarmEndTask2);
								farmSystem.setAutoFarmEndTask(autoFarmEndTask2);
							}
							farmSystem.checkFarmTask();
							this.amain(player, farmSystem, null, varInt, s39);
							player.sendMessage("YOU HAVE SUCCESSFULLY PURCHASED THE AUTO FARM SERVICE.");
						}
					}
					return true;
				}
				if (s.equalsIgnoreCase("autofarm")) {
					
				    final String[] split16 = s2.split(" ");
					farmSystem.checkFarmTask();
					String s40 = "attack";
					if (split16.length >= 2 && split16[0].equalsIgnoreCase("edit_farm")) {
						try {
							s40 = split16[2];
						} catch (Exception ex30) {
							ex30.printStackTrace();
						}
		
						this.amain(player, farmSystem, split16[1], varInt, s40);
					} else if (split16.length >= 2 && split16[0].equals("edit_farmType")) {
						String s41 = null;
						try {
							s41 = split16[1];
						} catch (Exception ex31) {
							ex31.printStackTrace();
						}
						try {
							s40 = split16[2];
						} catch (Exception ex32) {
							ex32.printStackTrace();
						}
						if (s41 != null) {
							int int3 = Integer.parseInt(s41);
							if (int3 >= 4) {
								int3 = 4;
							} else if (int3 < 0) {
								int3 = 0;
							}
							player.setVarInt("farmType", int3);
							farmSystem.setFarmTypeValue(int3);
							
							this.amain(player, farmSystem, null, int3, s40);

							
							return true;
						}
					
						this.amain(player, farmSystem, null, varInt, s40);
					} else if (split16.length >= 2) {
						try {
							s40 = split16[2];
						} catch (Exception ex33) {
						}
						if (split16[0].equals("set_attackSkills")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_chanceSkills")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_selfSkills")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_healSkills")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_attackSkillsPercent")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_chanceSkillsPercent")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_selfSkillsPercent")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_healSkillsPercent")) {
							this.awer(player, farmSystem, split16, 100, 1);
						} else if (split16[0].equals("set_distance")) {
							this.awer(player, farmSystem, split16, AutoFarmConfig.SEARCH_DISTANCE, 1);
						} else if (split16[0].equals("set_shortcutPage")) {
							this.awer(player, farmSystem, split16, AutoFarmConfig.SHORTCUT_PAGE, 1);
						}
						
						this.amain(player, farmSystem, null, varInt, s40);
					} else {
							try {
								s40 = split16[2];
							} catch (Exception ex34) {
							
							}
						
						this.amain(player, farmSystem, null, varInt, s40);
					}

					return true;
				}
				if (s.equalsIgnoreCase("farmacp")) {
					final String[] split16 = s2.split(" ");
					if (split16.length >= 2) {
						String type = split16[0];
						String action = split16[1];
						if (type.equalsIgnoreCase("hp")) {
							int current = farmSystem.getAcpHpPercent();
							if (action.equalsIgnoreCase("dec")) {
								farmSystem.setAcpHpPercent(current - 5);
							} else if (action.equalsIgnoreCase("inc")) {
								farmSystem.setAcpHpPercent(current + 5);
							} else {
								try {
									String clean = action.replaceAll("[^0-9]", "");
									if (!clean.isEmpty()) {
										farmSystem.setAcpHpPercent(Integer.parseInt(clean));
									}
								} catch (Exception e) {}
							}
						} else if (type.equalsIgnoreCase("mp")) {
							int current = farmSystem.getAcpMpPercent();
							if (action.equalsIgnoreCase("dec")) {
								farmSystem.setAcpMpPercent(current - 5);
							} else if (action.equalsIgnoreCase("inc")) {
								farmSystem.setAcpMpPercent(current + 5);
							} else {
								try {
									String clean = action.replaceAll("[^0-9]", "");
									if (!clean.isEmpty()) {
										farmSystem.setAcpMpPercent(Integer.parseInt(clean));
									}
								} catch (Exception e) {}
							}
						}
					}
					this.showAcpPage(player, farmSystem);
					return true;
				}
				if (s.equalsIgnoreCase("autosummonfarm")) {
					final String[] split17 = s2.split(" ");
					String s42 = "attack";
					if (split17.length >= 2 && split17[0].equalsIgnoreCase("edit_farm")) {
						try {
							s42 = split17[2];
						} catch (Exception ex35) {
						}
						this.a(player, farmSystem, split17[1], varInt, s42, "attack");
					} else if (split17.length >= 2) {
						try {
							s42 = split17[2];
						} catch (Exception ex36) {
						}
						if (split17[0].equals("set_attackSummonSkills")) {
							this.awer(player, farmSystem, split17, 100, 1);
						} else if (split17[0].equals("set_selfSummonSkills")) {
							this.awer(player, farmSystem, split17, 100, 1);
						} else if (split17[0].equals("set_healSummonSkills")) {
							this.awer(player, farmSystem, split17, 100, 1);
						} else if (split17[0].equals("set_attackSummonSkillsPercent")) {
							this.awer(player, farmSystem, split17, 100, 1);
						} else if (split17[0].equals("set_selfSummonSkillsPercent")) {
							this.awer(player, farmSystem, split17, 100, 1);
						} else if (split17[0].equals("set_healSummonSkillsPercent")) {
							this.awer(player, farmSystem, split17, 100, 1);
						}
						this.a(player, farmSystem, null, varInt, s42, "attack");
					} else {
						try {
							s42 = split17[2];
						} catch (Exception ex37) {
						}
						this.a(player, farmSystem, null, varInt, s42, "attack");
					}
					return true;
				}
			}
			return true;
		}
	}

	private void awer(final L2PcInstance player, final AutoFarmContext autoFarmContext, final String[] array, final int n,
			final int n2) {
		String s = null;
		try {
			s = array[1];
		} catch (Exception ex) {
		}
		if (s != null) {
			int n3 = 0;
			try {
				n3 = Integer.parseInt(s);
				if (n3 > n) {
					n3 = n;
				}
				if (n3 < n2) {
					n3 = n2;
				}
			} catch (NumberFormatException ex2) {
				if (array[0].equals("set_attackSkills")) {
					n3 = player.getVarInt("attackChanceSkills", AutoFarmConfig.ATTACK_SKILL_CHANCE);
				} else if (array[0].equals("set_chanceSkills")) {
					n3 = player.getVarInt("chanceChanceSkills", AutoFarmConfig.CHANCE_SKILL_CHANCE);
				} else if (array[0].equals("set_selfSkills")) {
					n3 = player.getVarInt("selfChanceSkills", AutoFarmConfig.SELF_SKILL_CHANCE);
				} else if (array[0].equals("set_healSkills")) {
					n3 = player.getVarInt("healChanceSkills", AutoFarmConfig.HEAL_SKILL_CHANCE);
				} else if (array[0].equals("set_attackSkillsPercent")) {
					n3 = player.getVarInt("attackSkillsPercent", AutoFarmConfig.ATTACK_SKILL_PERCENT);
				} else if (array[0].equals("set_chanceSkillsPercent")) {
					n3 = player.getVarInt("chanceSkillsPercent", AutoFarmConfig.CHANCE_SKILL_PERCENT);
				} else if (array[0].equals("set_selfSkillsPercent")) {
					n3 = player.getVarInt("selfSkillsPercent", AutoFarmConfig.SELF_SKILL_PERCENT);
				} else if (array[0].equals("set_healSkillsPercent")) {
					n3 = player.getVarInt("healSkillsPercent", AutoFarmConfig.HEAL_SKILL_PERCENT);
				} else if (array[0].equals("set_distance")) {
					n3 = player.getVarInt("farmDistance", AutoFarmConfig.SEARCH_DISTANCE);
				} else if (array[0].equals("set_shortcutPage")) {
					n3 = player.getVarInt("shortcutPage", AutoFarmConfig.SHORTCUT_PAGE);
				} else if (array[0].equals("set_attackSummonSkills")) {
					n3 = player.getVarInt("attackSummonChanceSkills", AutoFarmConfig.SUMMON_ATTACK_SKILL_CHANCE);
				} else if (array[0].equals("set_selfSummonSkills")) {
					n3 = player.getVarInt("selfSummonChanceSkills", AutoFarmConfig.SUMMON_SELF_SKILL_CHANCE);
				} else if (array[0].equals("set_healSummonSkills")) {
					n3 = player.getVarInt("healSummonChanceSkills", AutoFarmConfig.SUMMON_HEAL_SKILL_CHANCE);
				} else if (array[0].equals("set_attackSummonSkillsPercent")) {
					n3 = player.getVarInt("attackSummonSkillsPercent", AutoFarmConfig.SUMMON_ATTACK_SKILL_PERCENT);
				} else if (array[0].equals("set_selfSummonSkillsPercent")) {
					n3 = player.getVarInt("selfSummonSkillsPercent", AutoFarmConfig.SUMMON_SELF_SKILL_PERCENT);
				} else if (array[0].equals("set_healSummonSkillsPercent")) {
					n3 = player.getVarInt("healSummonSkillsPercent", AutoFarmConfig.SUMMON_HEAL_SKILL_PERCENT);
				}
			}
			if (array[0].equals("set_attackSkills")) {
				player.setVarInt("attackChanceSkills", n3);
				autoFarmContext.setAttackSkillValue(false, n3);
			} else if (array[0].equals("set_chanceSkills")) {
				player.setVarInt("chanceChanceSkills", n3);
				autoFarmContext.setChanceSkillValue(false, n3);
			} else if (array[0].equals("set_selfSkills")) {
				player.setVarInt("selfChanceSkills", n3);
				autoFarmContext.setSelfSkillValue(false, n3);
			} else if (array[0].equals("set_healSkills")) {
				player.setVarInt("healChanceSkills", n3);
				autoFarmContext.setLifeSkillValue(false, n3);
			} else if (array[0].equals("set_attackSkillsPercent")) {
				player.setVarInt("attackSkillsPercent", n3);
				autoFarmContext.setAttackSkillValue(true, n3);
			} else if (array[0].equals("set_chanceSkillsPercent")) {
				player.setVarInt("chanceSkillsPercent", n3);
				autoFarmContext.setChanceSkillValue(true, n3);
			} else if (array[0].equals("set_selfSkillsPercent")) {
				player.setVarInt("selfSkillsPercent", n3);
				autoFarmContext.setSelfSkillValue(true, n3);
			} else if (array[0].equals("set_healSkillsPercent")) {
				player.setVarInt("healSkillsPercent", n3);
				autoFarmContext.setLifeSkillValue(true, n3);
			} else if (array[0].equals("set_distance")) {
				player.setVarInt("farmDistance", n3);
				autoFarmContext.setRadiusValue(n3);
			} else if (array[0].equals("set_shortcutPage")) {
				player.setVarInt("shortcutPage", n3);
				autoFarmContext.setShortcutPageValue(n3);
			} else if (array[0].equals("set_attackSummonSkills")) {
				player.setVarInt("attackSummonChanceSkills", n3);
				autoFarmContext.setSummonAttackSkillValue(false, n3);
			} else if (array[0].equals("set_selfSummonSkills")) {
				player.setVarInt("selfSummonChanceSkills", n3);
				autoFarmContext.setSummonSelfSkillValue(false, n3);
			} else if (array[0].equals("set_healSummonSkills")) {
				player.setVarInt("healSummonChanceSkills", n3);
				autoFarmContext.setSummonLifeSkillValue(false, n3);
			} else if (array[0].equals("set_attackSummonSkillsPercent")) {
				player.setVarInt("attackSummonSkillsPercent", n3);
				autoFarmContext.setSummonAttackSkillValue(true, n3);
			} else if (array[0].equals("set_selfSummonSkillsPercent")) {
				player.setVarInt("selfSummonSkillsPercent", n3);
				autoFarmContext.setSummonSelfSkillValue(true, n3);
			} else if (array[0].equals("set_healSummonSkillsPercent")) {
				player.setVarInt("healSummonSkillsPercent", n3);
				autoFarmContext.setSummonLifeSkillValue(true, n3);
			}
		}
	}

	private void amain(final L2PcInstance player, final AutoFarmContext autoFarmContext, final String s, int n,
			final String str) {
		if (n < 0 || n > 4) {
			n = 0;
		}
		String s2 = null;

		switch (n) {
		case 0: 
			final String notNull = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-fighter.htm");
			String s3;
			if (autoFarmContext.isLeaderAssist()) {
				s3 = notNull.replace("%assist_img%", "L2UI.CheckBox_checked");
			} else {
				s3 = notNull.replace("%assist_img%", "L2UI.CheckBox");
			}
			final String replace = s3.replace("%assist_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmLeaderAssist");
			String s4;
			if (autoFarmContext.isAssistMonsterAttack()) {
				s4 = replace.replace("%assistMAttack_img%", "L2UI.CheckBox_checked");
			} else {
				s4 = replace.replace("%assistMAttack_img%", "L2UI.CheckBox");
			}
			final String replace2 = s4.replace("%assistMAttack_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmAssistMonsterAttack");
			String s5;
			if (autoFarmContext.isKeepLocation()) {
				s5 = replace2.replace("%keepLoc_img%", "L2UI.CheckBox_checked");
			} else {
				s5 = replace2.replace("%keepLoc_img%", "L2UI.CheckBox");
			}
			final String replace3 = s5.replace("%keepLoc_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmKeepLocation");
			String s6;
			if (autoFarmContext.isExtraDelaySkill()) {
				s6 = replace3.replace("%delaySk_img%", "L2UI.CheckBox_checked");
			} else {
				s6 = replace3.replace("%delaySk_img%", "L2UI.CheckBox");
			}
			s2 = s6.replace("%delaySk_bypass%", "bypass -h voice_editFarmOption " + str + " farmDelaySkills");
			
			break;
		
		case 1: 
			final String notNull2 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-archer.htm");
			String s7;
			if (autoFarmContext.isLeaderAssist()) {
				s7 = notNull2.replace("%assist_img%", "L2UI.CheckBox_checked");
			} else {
				s7 = notNull2.replace("%assist_img%", "L2UI.CheckBox");
			}
			final String replace4 = s7.replace("%assist_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmLeaderAssist");
			String s8;
			if (autoFarmContext.isAssistMonsterAttack()) {
				s8 = replace4.replace("%assistMAttack_img%", "L2UI.CheckBox_checked");
			} else {
				s8 = replace4.replace("%assistMAttack_img%", "L2UI.CheckBox");
			}
			final String replace5 = s8.replace("%assistMAttack_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmAssistMonsterAttack");
			String s9;
			if (autoFarmContext.isKeepLocation()) {
				s9 = replace5.replace("%keepLoc_img%", "L2UI.CheckBox_checked");
			} else {
				s9 = replace5.replace("%keepLoc_img%", "L2UI.CheckBox");
			}
			final String replace6 = s9.replace("%keepLoc_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmKeepLocation");
			String s10;
			if (autoFarmContext.isExtraDelaySkill()) {
				s10 = replace6.replace("%delaySk_img%", "L2UI.CheckBox_checked");
			} else {
				s10 = replace6.replace("%delaySk_img%", "L2UI.CheckBox");
			}
			final String replace7 = s10.replace("%delaySk_bypass%", "bypass -h voice_editFarmOption " + str + " farmDelaySkills");
			
			String s11;
			
			if (autoFarmContext.isRunTargetCloseUp()) {
				s11 = replace7.replace("%runCloseUp_img%", "L2UI.CheckBox_checked");
			} else {
				s11 = replace7.replace("%runCloseUp_img%", "L2UI.CheckBox");
			}
			s2 = s11.replace("%runCloseUp_bypass%", "bypass -h voice_editFarmOption " + str + " farmRunTargetCloseUp");

			break;
		
		case 2: 
			final String notNull3 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-mage.htm");
			String s12;
			if (autoFarmContext.isLeaderAssist()) {
				s12 = notNull3.replace("%assist_img%", "L2UI.CheckBox_checked");
			} else {
				s12 = notNull3.replace("%assist_img%", "L2UI.CheckBox");
			}
			final String replace8 = s12.replace("%assist_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmLeaderAssist");
			String s13;
			if (autoFarmContext.isAssistMonsterAttack()) {
				s13 = replace8.replace("%assistMAttack_img%", "L2UI.CheckBox_checked");
			} else {
				s13 = replace8.replace("%assistMAttack_img%", "L2UI.CheckBox");
			}
			final String replace9 = s13.replace("%assistMAttack_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmAssistMonsterAttack");
			String s14;
			if (autoFarmContext.isKeepLocation()) {
				s14 = replace9.replace("%keepLoc_img%", "L2UI.CheckBox_checked");
			} else {
				s14 = replace9.replace("%keepLoc_img%", "L2UI.CheckBox");
			}
			final String replace10 = s14.replace("%keepLoc_bypass%", "bypass -h voice_editFarmOption " + str + " farmKeepLocation");
			
			String s15;
			
			if (autoFarmContext.isRunTargetCloseUp()) {
				s15 = replace10.replace("%runCloseUp_img%", "L2UI.CheckBox_checked");
			} else {
				s15 = replace10.replace("%runCloseUp_img%", "L2UI.CheckBox");
			}
			
			s2 = s15.replace("%runCloseUp_bypass%", "bypass -h voice_editFarmOption " + str + " farmRunTargetCloseUp");
			break;
		
		case 3: 
			final String notNull4 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-heal.htm");
			String s16;
			if (autoFarmContext.isLeaderAssist()) {
				s16 = notNull4.replace("%assist_img%", "L2UI.CheckBox_checked");
			} else {
				s16 = notNull4.replace("%assist_img%", "L2UI.CheckBox");
			}
			final String replace11 = s16.replace("%assist_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmLeaderAssist");
			String s17;
			if (autoFarmContext.isAssistMonsterAttack()) {
				s17 = replace11.replace("%assistMAttack_img%", "L2UI.CheckBox_checked");
			} else {
				s17 = replace11.replace("%assistMAttack_img%", "L2UI.CheckBox");
			}
			final String replace12 = s17.replace("%assistMAttack_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmAssistMonsterAttack");
			String s18;
			if (autoFarmContext.isTargetRestoreMp()) {
				s18 = replace12.replace("%tgRestoreMp_img%", "L2UI.CheckBox_checked");
			} else {
				s18 = replace12.replace("%tgRestoreMp_img%", "L2UI.CheckBox");
			}
			s2 = s18.replace("%tgRestoreMp_bypass%", "bypass -h voice_editFarmOption " + str + " farmTargetRestoreMp");
			break;
		
		case 4: 
			final String notNull5 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-summon.htm");
			String s19;
			if (autoFarmContext.isLeaderAssist()) {
				s19 = notNull5.replace("%assist_img%", "L2UI.CheckBox_checked");
			} else {
				s19 = notNull5.replace("%assist_img%", "L2UI.CheckBox");
			}
			final String replace13 = s19.replace("%assist_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmLeaderAssist");
			String s20;
			if (autoFarmContext.isAssistMonsterAttack()) {
				s20 = replace13.replace("%assistMAttack_img%", "L2UI.CheckBox_checked");
			} else {
				s20 = replace13.replace("%assistMAttack_img%", "L2UI.CheckBox");
			}
			final String replace14 = s20.replace("%assistMAttack_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmAssistMonsterAttack");
			String s21;
			if (autoFarmContext.isKeepLocation()) {
				s21 = replace14.replace("%keepLoc_img%", "L2UI.CheckBox_checked");
			} else {
				s21 = replace14.replace("%keepLoc_img%", "L2UI.CheckBox");
			}
			final String replace15 = s21.replace("%keepLoc_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmKeepLocation");
			String s22;
			if (autoFarmContext.isUseSummonSkills()) {
				s22 = replace15.replace("%useSummonSk_img%", "L2UI.CheckBox_checked");
			} else {
				s22 = replace15.replace("%useSummonSk_img%", "L2UI.CheckBox");
			}
			final String replace16 = s22.replace("%useSummonSk_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmUseSummonSkills");
			String s23;
			if (autoFarmContext.isExtraDelaySkill()) {
				s23 = replace16.replace("%delaySk_img%", "L2UI.CheckBox_checked");
			} else {
				s23 = replace16.replace("%delaySk_img%", "L2UI.CheckBox");
			}
			s2 = s23.replace("%delaySk_bypass%", "bypass -h voice_editFarmOption " + str + " farmDelaySkills");
			break;
		
		}
		String b = "";
		String b2 = "";
		String b3 = "";
		String b4 = "";
		String b5 = "";
		String b6 = "";
		String b7 = "";
		String b8 = "";
		List<Integer> list = null;
		CharSequence replacement = null;

		switch (str) {
		case "attack":
			list = autoFarmContext.getAttackSpells();
			final String notNull6 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-skill_attack.htm");
			b = b(player, (s != null && s.equals("editAttackSkills")) ? s : null, "editAttackSkills",
					"attackChanceSkills", AutoFarmConfig.ATTACK_SKILL_CHANCE, str);
			b2 = b(player, (s != null && s.equals("editAttackPercent")) ? s : null, "editAttackPercent",
					"attackSkillsPercent", AutoFarmConfig.ATTACK_SKILL_PERCENT, str);
			String s24;
			if (autoFarmContext.isRndAttackSkills()) {
				s24 = notNull6.replace("%rndAttackSk_img%", "L2UI.CheckBox_checked");
			} else {
				s24 = notNull6.replace("%rndAttackSk_img%", "L2UI.CheckBox");
			}
			replacement = s24.replace("%rndAttackSk_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmRndAttackSkills");
			break;
		
		case "chance": 
			list = autoFarmContext.getChanceSpells();
			final String notNull7 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-skill_chance.htm");
			b3 = b(player, (s != null && s.equals("editChanceSkills")) ? s : null, "editChanceSkills",
					"chanceChanceSkills", AutoFarmConfig.CHANCE_SKILL_CHANCE, str);
			b4 = b(player, (s != null && s.equals("editChancePercent")) ? s : null, "editChancePercent",
					"chanceSkillsPercent", AutoFarmConfig.CHANCE_SKILL_PERCENT, str);
			String s25;
			if (autoFarmContext.isRndChanceSkills()) {
				s25 = notNull7.replace("%rndChanceSk_img%", "L2UI.CheckBox_checked");
			} else {
				s25 = notNull7.replace("%rndChanceSk_img%", "L2UI.CheckBox");
			}
			replacement = s25.replace("%rndChanceSk_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmRndChanceSkills");
			break;
		
		case "self": 
			list = autoFarmContext.getSelfSpells();
			final String notNull8 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-skill_self.htm");
			b5 = b(player, (s != null && s.equals("editSelfSkills")) ? s : null, "editSelfSkills", "selfChanceSkills",
					AutoFarmConfig.SELF_SKILL_CHANCE, str);
			b6 = b(player, (s != null && s.equals("editSelfPercent")) ? s : null, "editSelfPercent",
					"selfSkillsPercent", AutoFarmConfig.SELF_SKILL_PERCENT, str);
			String s26;
			if (autoFarmContext.isRndSelfSkills()) {
				s26 = notNull8.replace("%rndSelfSk_img%", "L2UI.CheckBox_checked");
			} else {
				s26 = notNull8.replace("%rndSelfSk_img%", "L2UI.CheckBox");
			}
			replacement = s26.replace("%rndSelfSk_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmRndSelfSkills");
			break;
		
		case "heal": 
			list = autoFarmContext.getLowLifeSpells();
			final String notNull9 = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-skill_heal.htm");
			b7 = b(player, (s != null && s.equals("editHealSkills")) ? s : null, "editHealSkills", "healChanceSkills",
					AutoFarmConfig.HEAL_SKILL_CHANCE, str);
			b8 = b(player, (s != null && s.equals("editLowHealPercent")) ? s : null, "editLowHealPercent",
					"healSkillsPercent", AutoFarmConfig.HEAL_SKILL_PERCENT, str);
			String s27;
			if (autoFarmContext.isRndLifeSkills()) {
				s27 = notNull9.replace("%rndLifeSk_img%", "L2UI.CheckBox_checked");
			} else {
				s27 = notNull9.replace("%rndLifeSk_img%", "L2UI.CheckBox");
			}
			replacement = s27.replace("%rndLifeSk_bypass%",
					"bypass -h voice_editFarmOption " + str + " farmRndLifeSkills");
			break;
		
		}
		String string = "";
		if (list != null) {
			final ArrayList<Integer> list2 = new ArrayList<Integer>();
			for (final int intValue : list) {
				if (intValue > 0 && player.getKnownSkill(intValue) == null) {
					list2.add(intValue);
				}
			}
			if (!list2.isEmpty()) {
				final Iterator<Integer> iterator2 = list2.iterator();
				while (iterator2.hasNext()) {
					list.remove(iterator2.next());
				}
				list2.clear();
			}
			final String notNull10 = HtmCache.getInstance().getHtm("data/html/command/autofarm/skill-template.htm");
			for (int i = 0; i < 8; ++i) {
				String str2;
				if (list.size() > 0 && i < list.size()) {
					final int intValue2 = list.get(i);
					if (intValue2 > 0) {
						final L2Skill knownSkill = player.getKnownSkill(intValue2);
						if (knownSkill != null) {
							str2 = notNull10.replace("%icon%", knownSkill.getIcon()).replace("%background%", "").replace("[Edit]", "[Del]")
									.replace("%bypass%",
											"bypass -h voice_removeSkill " + str + " " + knownSkill.getId() + "");
						} else {
							str2 = notNull10.replace("%icon%", "icon.skill0000").replace("%bypass%",
									"bypass -h voice_addSkill " + str + "");
						}
					} else {
						str2 = notNull10.replace("%icon%", "icon.skill0000").replace("%bypass%",
								"bypass -h voice_addSkill " + str + "");
					}
				} else {
					str2 = notNull10.replace("%icon%", "icon.skill0000").replace("%bypass%",
							"bypass -h voice_addSkill " + str + "");
				}
				string += str2;
			}
		}
		final String a = aabb(player, (s != null && s.equals("editDistance")) ? s : null, "editDistance", "farmDistance",
				AutoFarmConfig.SEARCH_DISTANCE, str);
		final String b9 = b(player, (s != null && s.equals("editShortcutPage")) ? s : null, "editShortcutPage",
				"shortcutPage", AutoFarmConfig.SHORTCUT_PAGE, str);

		if(replacement == null) {
			replacement = "";
		}

		if (s2 == null) {
			return;
		}
		
		String mainhtml = s2.replace("%skillList%", string)
				.replace("%skillsParam%", replacement)
				.replace("%status%",
						autoFarmContext.isAutofarming() ? "<font color=\"00FF00\">ON</font>"
								: "<font color=\"00FF00\">OFF</font>")
				.replace("%activeHwids%", this.getActivateHwids(player))
				.replace("%button%", autoFarmContext.isAutofarming()
						? "<button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_farmstop\" value=\"OFF\">"
						: "<button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_farmstart\" value=\"ON\">")
				.replace("%skillType%", str).replace("%activate%", this.a1(player, autoFarmContext))
				.replace("%distance%", a.equals("") ? "" : a).replace("%shortcutPage%", b9.equals("") ? "" : b9)
				.replace("%farmType%", this.ayyuu(player, n, str))
				.replace("%chanceAttack%", b.equals("") ? "" : b).replace("%chanceChance%", b3.equals("") ? "" : b3)
				.replace("%chanceSelf%", b5.equals("") ? "" : b5).replace("%chanceLowHeal%", b7.equals("") ? "" : b7)
				.replace("%percentAttack%", b2.equals("") ? "" : b2).replace("%percentChance%", b4.equals("") ? "" : b4)
				.replace("%percentSelf%", b6.equals("") ? "" : b6).replace("%percentLowHeal%", b8.equals("") ? "" : b8)
				.replace("%refreshSkills%", "bypass -h voice_refreshSkills " + str + "");
				 
		show(mainhtml, player, null, new Object[0]);
		
	}

	public String getActivateHwids(final L2PcInstance player) {
		if (AutoFarmConfig.FARM_ACTIVE_LIMITS < 0) {
			return "<font color=\"LEVEL\">-<font>";
		}
		final int activeFarms = AutoFarmManager.getInstance().getActiveFarms("127.0.0.1");
		if (activeFarms > 0) {
			return "<font color=\"LEVEL\">" + activeFarms + "<font>";
		}
		if (activeFarms <= 0 && AutoFarmManager.getInstance().isNonCheckPlayer(player.getObjectId())) {
			return "<font color=\"00FF00\">" + activeFarms + "<font>";
		}
		return "<a action=\"bypass -h voice_expendLimit\"><font color=\"FF0000\">" + activeFarms + "<font></a>";
	}

	private String a1(final L2PcInstance player, final AutoFarmContext autoFarmContext) {
		
		final String notNull = "<font color=FF6600>No Limit Time</font>";
		final String notNull2 = "<a action=\"bypass -h voice_buyfarm\"><font color=\"ff6755\">Buy Auto Farm Time</font></a>";
		if (AutoFarmConfig.AUTO_FARM_FREE || (AutoFarmConfig.PREMIUM_FARM_FREE && player.isVip())) {
			return notNull;
		}
		if (!autoFarmContext.isActiveAutofarm()) {
			return notNull2;
		}
		if (AutoFarmConfig.FARM_ONLINE_TYPE) {
			long n;
			if (!autoFarmContext.isActiveFarmTask()) {
				n = (player.getVarLong("activeFarmOnlineTask", 0L) - autoFarmContext.getLastFarmOnlineTime()) / 1000L;
			} else {
				n = (player.getVarLong("activeFarmOnlineTask", 0L) - (autoFarmContext.getLastFarmOnlineTime()
						+ (System.currentTimeMillis() - autoFarmContext.getFarmOnlineTime()))) / 1000L;
			}
			return "<font color=\"E6D0AE\">" + TimeUtils.formatTime((int) n, false) + "</font>";
		}
		return "<font color=\"E6D0AE\">" + TimeUtils.formatTime(
				(int) ((autoFarmContext.getAutoFarmEnd() - System.currentTimeMillis()) / 1000L), false) + "</font>";
	}

	private void abuy(final L2PcInstance player, final AutoFarmContext autoFarmContext) {
		final String notNull = HtmCache.getInstance().getHtm("data/html/command/autofarm/buy.htm");
		String replacement = "";
		final ArrayList<Integer> list = new ArrayList<Integer>();
		final Iterator<Integer> iterator = AutoFarmConfig.AUTO_FARM_PRICES.keySet().iterator();
		while (iterator.hasNext()) {
			list.add((int) iterator.next());
		}
		// Collections.sort((List<Integer>)list, (Comparator<? super Object>)new
		// SortTimeInfo());
		int n = 0;
		for (final int intValue : list) {
			if (n > 0) {
				replacement += ";";
			}
			replacement = replacement + "" + intValue + "";
			++n;
		}
		final String replace = notNull.replace("%time%", replacement).replace("%freeUse%",
				this.b(player, autoFarmContext));
		list.clear();
		show(replace, player, null, new Object[0]);
	}

	private String b(final L2PcInstance player, final AutoFarmContext autoFarmContext) {
		if (autoFarmContext.isActiveAutofarm() || !AutoFarmConfig.ALLOW_FARM_FREE_TIME) {
			return "";
		}
		if (player.getVarLong("farmFreeTime", 0L) <= 0L) {
			return "<button value=\"Try free for " + AutoFarmConfig.FARM_FREE_TIME
					+ " hour(s)\" action=\"bypass -h voice_tryFreeTime\" width=120 height=25 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"";
		}
		return "";
	}

	private String ayyuu(final L2PcInstance player, final int n, final String str) {
		String s = "<td aling=center width=20>";
		int i = 0;
		String str2 = "";
		switch (n) {
		case 0: {
			s += "<img src=\"L2UI_CH3.party_styleicon1_3\" width=16 height=16></td>";
			str2 = "Fighter";
			++i;
			break;
		}
		case 1: {
			s += "<img src=\"L2UI_CH3.party_styleicon2_3\" width=16 height=16></td>";
			str2 = "Archer";
			i = 2;
			break;
		}
		case 2: {
			s += "<img src=\"L2UI_CH3.party_styleicon5_3\" width=16 height=16></td>";
			str2 = "Magic";
			i = 3;
			break;
		}
		case 3: {
			s += "<img src=\"L2UI_CH3.party_styleicon6_3\" width=16 height=16></td>";
			str2 = "Healer";
			i = 4;
			break;
		}
		case 4: {
			s += "<img src=\"L2UI_CH3.party_styleicon7_3\" width=16 height=16></td>";
			str2 = "Summon";
			break;
		}
		}
		return s + "<td width=90>" + str2
				+ "</td><td width=60><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm edit_farmType "
				+ i + " " + str + "\" value=\"Switch\"></td>";
	}

	private static String aabb(final L2PcInstance player, final String str, final String str2, final String s, final int n,
			final String s2) {
		String s3 = "";
		if (str != null && !str.isEmpty()) {
			if (str.equals("editDistance")) {
				s3 = s3 + "<td width=50><edit var=\"" + str + "\" width=40 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_distance $editDistance "
						+ s2 + "\" value=\"Save\"></td>";
			}
		} else {
			s3 = s3 + "<td aling=center width=50><font color=c1b33a>" + player.getVarInt(s, n) + "</font></td>"
					+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm edit_farm "
					+ str2 + " " + s2 + "\" value=\"Edit\"></td>";
		}
		return s3;
	}

	private static String b(final L2PcInstance player, final String str, final String str2, final String s, final int n,
			final String s2) {
		String str3 = "";
		if (str != null && !str.isEmpty()) {
			if (str.equals("editAttackSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_attackSkills $editAttackSkills "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editChanceSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_chanceSkills $editChanceSkills "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editSelfSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_selfSkills $editSelfSkills "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editHealSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_healSkills $editHealSkills "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editAttackPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_attackSkillsPercent $editAttackPercent "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editChancePercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_chanceSkillsPercent $editChancePercent "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editSelfPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_selfSkillsPercent $editSelfPercent "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editLowHealPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_healSkillsPercent $editLowHealPercent "
						+ s2 + "\" value=\"Save\"></td>";
			} else if (str.equals("editShortcutPage")) {
				str3 = str3 + "<td width=43><edit var=\"" + str + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm set_shortcutPage $editShortcutPage "
						+ s2 + "\" value=\"Save\"></td>";
			}
		} else {
			String str4;
			if (s.equals("shortcutPage")) {
				str4 = str3 + "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(s, n) + "</font></td>";
			} else {
				str4 = str3 + "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(s, n)
						+ "%</font></td>";
			}
			str3 = str4
					+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autofarm edit_farm "
					+ str2 + " " + s2 + "\" value=\"Edit\"></td>";
		}
		return str3;
	}

	private static String c(final L2PcInstance player, final String s, final String str, final String s2, final int n,
			final String str2) {
		String str3 = "";
		if (s != null && !s.isEmpty()) {
			if (s.equals("editSummonAttackSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_attackSummonSkills $editSummonAttackSkills "
						+ str2 + "\" value=\"Save\"></td>";
			} else if (s.equals("editSummonSelfSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_selfSummonSkills $editSummonSelfSkills "
						+ str2 + "\" value=\"Save\"></td>";
			} else if (s.equals("editSummonHealSkills")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_healSummonSkills $editSummonHealSkills "
						+ str2 + "\" value=\"Save\"></td>";
			} else if (s.equals("editSummonAttackPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_attackSummonSkillsPercent $editSummonAttackPercent "
						+ str2 + "\" value=\"Save\"></td>";
			} else if (s.equals("editSummonSelfPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_selfSummonSkillsPercent $editSummonSelfPercent "
						+ str2 + "\" value=\"Save\"></td>";
			} else if (s.equals("editSummonLowHealPercent")) {
				str3 = str3 + "<td width=43><edit var=\"" + s + "\" width=34 height=12></td>"
						+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm set_healSummonSkillsPercent $editSummonLowHealPercent "
						+ str2 + "\" value=\"Save\"></td>";
			}
		} else {
			str3 = str3 + "<td aling=center width=45><font color=c1b33a>" + player.getVarInt(s2, n) + "%</font></td>"
					+ "<td width=40><button width=40 height=20 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" action=\"bypass -h voice_autosummonfarm edit_farm "
					+ str + " " + str2 + "\" value=\"Edit\"></td>";
		}
		return str3;
	}

	private void axo(final L2PcInstance player, final AutoFarmContext autoFarmContext, final String str, final int i) {
		List<Integer> list = null;
		final ArrayList<L2Skill> list2 = new ArrayList<L2Skill>();
		switch (str) {
		case "attack": {
			list = autoFarmContext.getAttackSpells();
			for (final L2Skill skill : player.getAllSkills()) {
				if (skill != null && !skill.isPassive() && !skill.isSpoilSkill() && !skill.isSweepSkill()
						&& skill.getId() != 1263
						&& (skill.getSkillType() == L2SkillType.AGGDAMAGE || skill.getSkillType() == L2SkillType.PDAM
								|| skill.getSkillType() == L2SkillType.MANADAM
								|| skill.getSkillType() == L2SkillType.MDAM || skill.getSkillType() == L2SkillType.DRAIN
								|| skill.getSkillType() == L2SkillType.CPDAM
								|| skill.getSkillType() == L2SkillType.STUN)) {
					list2.add(skill);
				}
			}
			break;
		}
		case "chance": {
			list = autoFarmContext.getChanceSpells();
			for (final L2Skill skill2 : player.getAllSkills()) {
				if (skill2 != null && !skill2.isPassive() && (skill2.getSkillType() == L2SkillType.DOT
						|| skill2.getSkillType() == L2SkillType.MDOT || skill2.getSkillType() == L2SkillType.POISON
						|| skill2.getSkillType() == L2SkillType.BLEED || skill2.getSkillType() == L2SkillType.DEBUFF
						|| skill2.getSkillType() == L2SkillType.SLEEP || skill2.getSkillType() == L2SkillType.ROOT
						|| skill2.getSkillType() == L2SkillType.PARALYZE || skill2.getSkillType() == L2SkillType.MUTE
						|| skill2.isSpoilSkill() || skill2.isSweepSkill() || skill2.getId() == 1263)) {
					list2.add(skill2);
				}
			}
			break;
		}
		case "self": {
			list = autoFarmContext.getSelfSpells();
			for (final L2Skill skill3 : player.getAllSkills()) {
				if ((skill3 != null && !skill3.isPassive()
						&& (skill3.isToggle() || skill3.isMusic() || skill3.getSkillType() == L2SkillType.BUFF))
						|| skill3.isCubic()) {
					list2.add(skill3);
				}
			}
			break;
		}
		case "heal": {
			list = autoFarmContext.getLowLifeSpells();
			for (final L2Skill skill4 : player.getAllSkills()) {
				if (skill4 != null && !skill4.isPassive()
						&& (skill4.getSkillType() == L2SkillType.DRAIN || skill4.getSkillType() == L2SkillType.HEAL
								|| skill4.getSkillType() == L2SkillType.HEAL_PERCENT
								|| skill4.getSkillType() == L2SkillType.MANAHEAL
								|| skill4.getSkillType() == L2SkillType.MANAHEAL_PERCENT)) {
					list2.add(skill4);
				}
			}
			break;
		}
		}
		if (list2.isEmpty()) {
			player.sendMessage("You have no valid skills!");
			this.amain(player, autoFarmContext, null, player.getVarInt("farmType", AutoFarmConfig.FARM_TYPE), str);
			return;
		}
		if (list.size() > 0) {
			final ArrayList<L2Skill> list3 = new ArrayList<L2Skill>();
			for (final L2Skill skill5 : list2) {
				if (list.contains(skill5.getId())) {
					list3.add(skill5);
				}
			}
			if (!list3.isEmpty()) {
				final Iterator<L2Skill> iterator6 = list3.iterator();
				while (iterator6.hasNext()) {
					list2.remove(iterator6.next());
				}
				list3.clear();
			}
		}
		final String notNull = HtmCache.getInstance().getHtm("data/html/command/autofarm/player_skills.htm");
		final String notNull2 = HtmCache.getInstance().getHtm("data/html/command/autofarm/player_skills_template.htm");
		String string = "";
		int n2 = 0;
		final int size = list2.size();
		final boolean b = size > 5;
		for (int j = (i - 1) * 5; j < size; ++j) {
			final L2Skill skill6 = list2.get(j);
			if (skill6 != null) {
				string += notNull2.replace("%name%", skill6.getName()).replace("%icon%", skill6.getIcon())
						.replace("%bypass%", "bypass -h voice_addNewSkill " + skill6.getId() + " " + str + "");
			}
			if (++n2 >= 5) {
				break;
			}
		}
		show(notNull.replace("%list%", string).replace("%page%", String.valueOf(i)).replace("%skillType%", str).replace(
				"%navigation%",
				getNavigationBlock((int) Math.ceil(size / 5.0), i, size, 5, b, "voice_addSkill " + str + " %s")),
				player, null, new Object[0]);
		list2.clear();
	}

	private void a(final L2PcInstance player, final AutoFarmContext autoFarmContext, final String s, final int n,
			final String str, final String str2) {
		if (player.getPet() == null) {
			player.sendMessage("You can't use this option!");
			this.a(player, autoFarmContext, null, n, str, str2);
			return;
		}
		final L2Summon pet = player.getPet();
		if (pet != null && pet.getLevel() - player.getLevel() > 20) {
			player.sendMessage("YOUR PET IS TOO HIGH LEVEL TO CONTROL");
			this.a(player, autoFarmContext, null, n, str, str2);
			return;
		}
		final String notNull = HtmCache.getInstance().getHtm("data/html/command/autofarm/index-summonSkills.htm");
		List<Integer> list = null;
		CharSequence replacement = null;
		String c = "";
		String c2 = "";
		String c3 = "";
		String c4 = "";
		String c5 = "";
		String c6 = "";
		switch (str) {
		case "attack": {
			list = autoFarmContext.getSummonAttackSpells();
			final String notNull2 = HtmCache.getInstance()
					.getHtm("data/html/command/autofarm/index-summon_skill_attack.htm");
			c = c(player, (s != null && s.equals("editSummonAttackSkills")) ? s : null, "editSummonAttackSkills",
					"attackSummonChanceSkills", AutoFarmConfig.SUMMON_ATTACK_SKILL_CHANCE, str);
			c2 = c(player, (s != null && s.equals("editSummonAttackPercent")) ? s : null, "editSummonAttackPercent",
					"attackSummonSkillsPercent", AutoFarmConfig.SUMMON_ATTACK_SKILL_PERCENT, str);
			String s2;
			if (autoFarmContext.isRndSummonAttackSkills()) {
				s2 = notNull2.replace("%rndAttackSk_img%", "L2UI.CheckBox_checked");
			} else {
				s2 = notNull2.replace("%rndAttackSk_img%", "L2UI.CheckBox");
			}
			replacement = s2.replace("%rndAttackSk_bypass%",
					"bypass -h voice_editSummonFarmOption " + str + " farmRndSummonAttackSkills " + str2);
			break;
		}
		case "self": {
			list = autoFarmContext.getSummonSelfSpells();
			final String notNull3 = HtmCache.getInstance()
					.getHtm("data/html/command/autofarm/index-summon_skill_self.htm");
			c3 = c(player, (s != null && s.equals("editSummonSelfSkills")) ? s : null, "editSummonSelfSkills",
					"selfSummonChanceSkills", AutoFarmConfig.SUMMON_SELF_SKILL_CHANCE, str);
			c4 = c(player, (s != null && s.equals("editSummonSelfPercent")) ? s : null, "editSummonSelfPercent",
					"selfSummonSkillsPercent", AutoFarmConfig.SUMMON_SELF_SKILL_PERCENT, str);
			String s3;
			if (autoFarmContext.isRndSummonSelfSkills()) {
				s3 = notNull3.replace("%rndSelfSk_img%", "L2UI.CheckBox_checked");
			} else {
				s3 = notNull3.replace("%rndSelfSk_img%", "L2UI.CheckBox");
			}
			replacement = s3.replace("%rndSelfSk_bypass%",
					"bypass -h voice_editSummonFarmOption " + str + " farmRndSummonSelfSkills " + str2);
			break;
		}
		case "heal": {
			list = autoFarmContext.getSummonHealSpells();
			final String notNull4 = HtmCache.getInstance()
					.getHtm("data/html/command/autofarm/index-summon_skill_heal.htm");
			c5 = c(player, (s != null && s.equals("editSummonHealSkills")) ? s : null, "editSummonHealSkills",
					"healSummonChanceSkills", AutoFarmConfig.SUMMON_HEAL_SKILL_CHANCE, str);
			c6 = c(player, (s != null && s.equals("editSummonLowHealPercent")) ? s : null, "editSummonLowHealPercent",
					"healSummonSkillsPercent", AutoFarmConfig.SUMMON_HEAL_SKILL_PERCENT, str);
			String s4;
			if (autoFarmContext.isRndSummonLifeSkills()) {
				s4 = notNull4.replace("%rndLifeSk_img%", "L2UI.CheckBox_checked");
			} else {
				s4 = notNull4.replace("%rndLifeSk_img%", "L2UI.CheckBox");
			}
			replacement = s4.replace("%rndLifeSk_bypass%",
					"bypass -h voice_editSummonFarmOption " + str + " farmRndSummonLifeSkills " + str2);
			break;
		}
		}
		String string = "";

		if (list != null && !list.isEmpty()) {
			final ArrayList<Object> list2 = new ArrayList<Object>();
			for (final int intValue : list) {
				if (intValue > 0 && player.getPet().getAllSkills().length == 0) {
					list2.add(intValue);
				}
			}
			if (!list2.isEmpty()) {
				final Iterator<Object> iterator2 = list2.iterator();
				while (iterator2.hasNext()) {
					list.remove(iterator2.next());
				}
				list2.clear();
			}
		}
		final String notNull5 = HtmCache.getInstance().getHtm("data/html/command/autofarm/summon_skill-template.htm");
		if (notNull5 == null) {
			return;
		}
		for (int i = 0; i < 8; ++i) {
			final String s5 = notNull5;
			String str3;
			if (list != null && list.size() > 0 && i < list.size()) {
				final int intValue2 = list.get(i);
				if (intValue2 > 0) {
					final int availableSkillLevel = player.getPet().getAllSkills().length;
					if (availableSkillLevel > 0) {
						str3 = s5
								.replace("%icon%",
										SkillTable.getInstance().getInfo(intValue2, availableSkillLevel).getIcon())
								.replace("%background%", "").replace("%bypass%",
										"bypass -h voice_removeSummonSkill " + str + " " + intValue2 + " " + str2);
					} else {
						str3 = s5.replace("%icon%", "icon.high_tab")
								.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"")
								.replace("%bypass%", "bypass -h voice_addSummonSkill " + str + " " + str2);
					}
				} else {
					str3 = s5.replace("%icon%", "icon.high_tab")
							.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"")
							.replace("%bypass%", "bypass -h voice_addSummonSkill " + str + " " + str2);
				}
			} else {
				str3 = s5.replace("%icon%", "icon.high_tab")
						.replace("%background%", "background=\"l2ui_ch3.multisell_plusicon\"")
						.replace("%bypass%", "bypass -h voice_addSummonSkill " + str + " " + str2);
			}
			string += str3;
		}
		String s6;
		if (autoFarmContext.isExtraSummonDelaySkill()) {
			s6 = notNull.replace("%delaySk_img%", "L2UI.CheckBox_checked");
		} else {
			s6 = notNull.replace("%delaySk_img%", "L2UI.CheckBox");
		}
		show(s6.replace("%delaySk_bypass%",
				"bypass -h voice_editSummonFarmOption " + str + " farmSummonDelaySkills " + str2)
				.replace("%skillType%", str2).replace("%skillsParam%", replacement).replace("%summonSkillType%", str)
				.replace("%skillList%", string).replace("%chanceAttack%", c.equals("") ? "" : c)
				.replace("%chanceSelf%", c3.equals("") ? "" : c3).replace("%chanceLowHeal%", c5.equals("") ? "" : c5)
				.replace("%percentAttack%", c2.equals("") ? "" : c2).replace("%percentSelf%", c4.equals("") ? "" : c4)
				.replace("%percentLowHeal%", c6.equals("") ? "" : c6), player, null, new Object[0]);
	}

	public static void show(String paramString, L2PcInstance paramPlayer, L2NpcInstance paramNpcInstance,
			Object... paramVarArgs) {
		if (paramString == null || paramPlayer == null)
			return;
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		if (paramString.endsWith(".html") || paramString.endsWith(".htm")) {
			npcHtmlMessage.setFile(paramString);
		} else {
			npcHtmlMessage.setHtml(Strings.bbParse(paramString));
		}
		if (paramVarArgs != null && paramVarArgs.length % 2 == 0) {
			byte b;
			for (b = 0; b < paramVarArgs.length; b = 2)
				npcHtmlMessage.replace(String.valueOf(paramVarArgs[b]), String.valueOf(paramVarArgs[b + 1]));
		}
		paramPlayer.sendPacket((L2GameServerPacket) npcHtmlMessage);
	}

	public static String getNavigationBlock(int paramInt1, int paramInt2, int paramInt3, int paramInt4,
			boolean paramBoolean, String paramString) {
		String str1 = "";
		boolean bool1 = false;
		boolean bool2 = false;
		boolean bool3 = false;
		String str2 = paramString;
		String str3 = paramString;
		for (byte b = 1; b <= paramInt1; b++) {
			if (!bool1) {
				if (paramInt2 == 1) {
					str1 = str1 + "<td width=80 align=left valign=top>&nbsp;</td>";
				} else {
					str2 = String.format("" + str2 + "", new Object[] { Integer.valueOf(paramInt2 - 1) });
					str1 = str1 + "<td width=80 align=left valign=top><button action=\"bypass -h " + str2
							+ "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\"></td>";
				}
				bool1 = true;
			}
			if (!bool2 && b == paramInt2) {
				if (paramInt3 <= paramInt4) {
					str1 = str1 + "<td width=50 align=center valign=top>&nbsp;</td>";
				} else {
					str1 = str1 + "<td width=50 align=center valign=top>[ " + b + " ]</td>";
				}
				bool2 = true;
			}
			if (!bool3 && b == paramInt2) {
				if (paramBoolean && paramInt1 >= paramInt2 + 1) {
					str3 = String.format("" + str3 + "", new Object[] { Integer.valueOf(paramInt2 + 1) });
					str1 = str1 + "<td width=80 align=right valign=top><button action=\"bypass -h " + str3
							+ "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\"></td>";
				} else {
					str1 = str1 + "<td width=80 align=right valign=top>&nbsp;</td>";
				}
				bool3 = true;
			}
		}
		if (str1.equals(""))
			str1 = "<td width=30 align=center valign=top>&nbsp;</td>";
		return str1;
	}

	private void astt(final L2PcInstance player, final AutoFarmContext autoFarmContext, final int n, final String str,
			final String str2, final int i) {
		if (player.getPet() == null) {
			player.sendMessage("You can't use this option!");
			this.a(player, autoFarmContext, null, n, str, str2);
			return;
		}
		final L2Summon pet = player.getPet();
		if (pet.getPet() != null && pet.getLevel() - player.getLevel() > 20) {
			player.sendMessage("YOUR PET IS TOO HIGH LEVEL TO CONTROL");
			this.a(player, autoFarmContext, null, n, str, str2);
			return;
		}
		List<Integer> list = null;
		final ArrayList<L2Skill> list2 = new ArrayList<L2Skill>();
		switch (str) {
		case "attack": {

			list = autoFarmContext.getSummonAttackSpells();
			for (final L2Skill sk : player.getPet().getAllSkills()) {

				if (sk.getLevel() > 0) {
					final L2Skill info = sk;
					if (info == null || (info.getSkillType() != L2SkillType.AGGDAMAGE
							&& info.getSkillType() != L2SkillType.PDAM && info.getSkillType() != L2SkillType.MANADAM
							&& info.getSkillType() != L2SkillType.MDAM && info.getSkillType() != L2SkillType.DRAIN
							&& info.getSkillType() != L2SkillType.CPDAM && info.getSkillType() != L2SkillType.STUN)) {
						continue;
					}
					list2.add(info);
				}
			}
			break;
		}
		case "self": {
			list = autoFarmContext.getSummonSelfSpells();
			for (final L2Skill info2 : pet.getAllSkills()) {
				final int availableSkillLevel2 = pet.getAllSkills().length;
				if (availableSkillLevel2 > 0) {

					if ((info2 == null
							|| (!info2.isToggle() && !info2.isMusic() && info2.getSkillType() != L2SkillType.BUFF))
							&& !info2.isCubic()) {
						continue;
					}
					list2.add(info2);
				}
			}
			break;
		}
		case "heal": {
			list = autoFarmContext.getSummonHealSpells();
			for (final L2Skill info3 : pet.getAllSkills()) {
				final int availableSkillLevel3 = pet.getAllSkills().length;
				if (availableSkillLevel3 > 0) {

					if (info3 == null
							|| (info3.getSkillType() != L2SkillType.DRAIN && info3.getSkillType() != L2SkillType.HEAL
									&& info3.getSkillType() != L2SkillType.HEAL_PERCENT
									&& info3.getSkillType() != L2SkillType.MANAHEAL
									&& info3.getSkillType() != L2SkillType.MANAHEAL_PERCENT)) {
						continue;
					}
					list2.add(info3);
				}
			}
			break;
		}
		}
		if (list.size() > 0) {
			final ArrayList<L2Skill> list3 = new ArrayList<L2Skill>();
			for (final L2Skill skill : list2) {
				if (list.contains(skill.getId())) {
					list3.add(skill);
				}
			}
			if (!list3.isEmpty()) {
				final Iterator<L2Skill> iterator5 = list3.iterator();
				while (iterator5.hasNext()) {
					list2.remove(iterator5.next());
				}
				list3.clear();
			}
		}
		if (list2.isEmpty()) {
			player.sendMessage("Your summon has no valid skills!");
			this.a(player, autoFarmContext, null, n, str, str2);
			return;
		}
		final String notNull = HtmCache.getInstance().getHtm("data/html/command/autofarm/summon_skills.htm");
		final String notNull2 = HtmCache.getInstance().getHtm("data/html/command/autofarm/summon_skills_template.htm");
		String string = "";
		int n3 = 0;
		final int size = list2.size();
		final boolean b = size > 5;
		for (int j = (i - 1) * 5; j < size; ++j) {
			final L2Skill skill2 = list2.get(j);
			if (skill2 != null) {
				string += notNull2.replace("%name%", skill2.getName()).replace("%icon%", skill2.getIcon()).replace(
						"%bypass%",
						"bypass -h voice_addNewSummonSkill " + skill2.getId() + " " + str + " " + str2 + "");
			}
			if (++n3 >= 5) {
				break;
			}
		}
		show(notNull.replace("%list%", string).replace("%page%", String.valueOf(i)).replace("%summonSkillType%", str)
				.replace("%skillType%", str2).replace("%navigation%", getNavigationBlock((int) Math.ceil(size / 5.0), i,
						size, 5, b, "voice_addSummonSkill " + str + " " + str2 + " %s")),
				player, null, new Object[0]);
		list2.clear();
	}

	private void showAcpPage(final L2PcInstance player, final AutoFarmContext farmSystem) {
		String acpHtml = HtmCache.getInstance().getHtm("data/html/command/autofarm/acp.htm");
		if (acpHtml == null) {
			player.sendMessage("ACP HTML template not found!");
			return;
		}
		
		acpHtml = acpHtml
				.replace("%hp_percent%", String.valueOf(farmSystem.getAcpHpPercent()) + "%")
				.replace("%mp_percent%", String.valueOf(farmSystem.getAcpMpPercent()) + "%")
				.replace("%hp_dec_bypass%", "bypass -h voice_farmacp hp dec")
				.replace("%hp_inc_bypass%", "bypass -h voice_farmacp hp inc")
				.replace("%mp_dec_bypass%", "bypass -h voice_farmacp mp dec")
				.replace("%mp_inc_bypass%", "bypass -h voice_farmacp mp inc")
				.replace("%back_bypass%", "bypass -h voice_autofarm");
				
		show(acpHtml, player, null, new Object[0]);
	}

	@Override
	public String[] getVoicedCommandList() {
		return _voicedCommands;
	}

	private static String[] _voicedCommands = { "autofarm", "autosummonfarm", "farmstart", "farmstop", "buyfarm",
			"buyfarmTime", "tryFreeTime", "expendLimit", "changeSkillType", "refreshSkills", "removeSkill", "addSkill",
			"addNewSkill", "editFarmOption", "editSummonSkills", "removeSummonSkill", "addSummonSkill",
			"addNewSummonSkill", "editSummonFarmOption", "farmacp",
			"voice_autofarm", "voice_autosummonfarm", "voice_farmstart", "voice_farmstop", "voice_buyfarm",
			"voice_buyfarmTime", "voice_tryFreeTime", "voice_expendLimit", "voice_changeSkillType", "voice_refreshSkills", "voice_removeSkill", "voice_addSkill",
			"voice_addNewSkill", "voice_editFarmOption", "voice_editSummonSkills", "voice_removeSummonSkill", "voice_addSummonSkill",
			"voice_addNewSummonSkill", "voice_editSummonFarmOption", "voice_farmacp" };

	@Override
	public String getDescription(String arg0) {
	
		return null;
	}

}
