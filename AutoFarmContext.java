import java.util.ArrayList;
import com.premium.game.model.actor.instance.L2ItemInstance;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.premium.game.ai.CtrlIntention;
import com.premium.game.datatables.xml.SkillTable;
import com.premium.game.model.L2Object;
import com.premium.game.model.L2ShortCut;
import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.L2Summon;
import com.premium.game.model.actor.instance.L2MonsterInstance;
import com.premium.game.model.actor.instance.L2NpcInstance;
import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.actor.instance.L2RaidBossInstance;
import com.premium.game.model.world.L2World;
import com.premium.game.model.world.Location;
import com.premium.game.model.zone.L2Zone;
import com.premium.game.network.ThreadPoolManager;
import com.premium.game.network.serverpackets.MyTargetSelected;
import com.premium.game.network.serverpackets.TargetUnselected;
import com.premium.game.skills.AbnormalEffect;
import com.premium.game.templates.skills.L2SkillType;
import com.premium.tools.random.Rnd;
import com.premium.util.AbstractHardReference;
import com.premium.util.ArrayUtils;
import com.premium.util.HardReference;
import com.premium.game.model.actor.L2Attackable;
import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.L2Npc;
import com.premium.game.model.actor.L2Playable;
import com.premium.game.model.actor.instance.L2ChestInstance;
import com.premium.game.model.actor.instance.L2MinionInstance;

public class AutoFarmContext
{
    private List<Integer> aW;
    private  List<Integer> aX;
    private  List<Integer> aY;
    private  List<Integer> aZ;
    private  List<Integer> ba;
    private  List<Integer> bb;
    private  List<Integer> bc;
    private  List<Integer> bd;
    private  List<Integer> be;
    private  List<Integer> bf;
    private  List<Integer> bg;
    public L2Playable pl;
    private int iy;
    private int iz;
    private int iA;
    private int iB;
    private boolean ch;
    private int iC;
    private boolean ci;
    private int iD;
    private boolean cj;
    private int iE;
    private boolean ck;
    private int iF;
    private boolean cl;
    private int iG;
    private boolean cm;
    private int iH;
    private boolean cn;
    private int iI;
    private int iJ;
    private int iK;
    private int iL;
    private int iM;
    private int iN;
    private int iO;
    private boolean co;
    private boolean cp;
    private boolean cq;
    private boolean cr;
    private boolean cs;
    private boolean ct;
    private boolean cu;
    private boolean cv;
    private long bp;
    private long bq;
    private long br;
    private boolean cw;
    private Location B;
    private ScheduledFuture<?> T;
    private ScheduledFuture<?> U;
    private int iAcpHpPercent;
    private int iAcpMpPercent;
    private long lastHpPotionUse;
    private long lastMpPotionUse;
    
    protected static Logger _log = Logger.getLogger(AutoFarmContext.class);
    
    
	public void setInitial(L2Playable player) {
		
	     this.aW = Arrays.asList(0, 1, 2, 3);
	        this.aX = Arrays.asList(4, 5);
	        this.aY = Arrays.asList(6, 7, 8, 9);
	        this.aZ = Arrays.asList(10, 11);
	        this.ba = new ArrayList<Integer>();
	        this.bb = new ArrayList<Integer>();
	        this.bc = new ArrayList<Integer>();
	        this.bd = new ArrayList<Integer>();
	        this.be = new ArrayList<Integer>();
	        this.bf = new ArrayList<Integer>();
	        this.bg = new ArrayList<Integer>();
	        this.ch = false;
	        this.ci = false;
	        this.cj = false;
	        this.ck = false;
	        this.cl = false;
	        this.cm = false;
	        this.cn = false;
	        this.co = false;
	        this.cp = false;
	        this.cq = false;
	        this.cr = false;
	        this.cs = false;
	        this.ct = false;
	        this.cu = false;
	        this.cv = false;
	        this.bq = 0L;
	        this.br = 0L;
	        this.cw = false;
	        this.B = null;
	        this.pl = player;
	        this.iAcpHpPercent = 0;
	        this.iAcpMpPercent = 0;
	        this.lastHpPotionUse = 0L;
	        this.lastMpPotionUse = 0L;
    }
    
    private static boolean a(final L2Skill skill) {
        return skill.getSkillType() == L2SkillType.MANAHEAL || skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT;
    }
    
    private static boolean b(final L2Skill skill) {
        return skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.HEAL_PERCENT;
    }
    
    /**
     * Applies the configured cooldown reduction after an AutoFarm cast has
     * started. The core still validates cast state, MP and target conditions.
     */
    public void scheduleFastReuse(final L2Character caster, final L2Skill skill) {
        if (caster == null || skill == null || AutoFarmConfig.FAST_SKILL_REUSE <= 0) {
            return;
        }

        long normalReuse = skill.getReuseDelay();
        if (!skill.isStaticReuse() && !skill.isPotion()) {
            normalReuse *= skill.isMagic() ? caster.getStat().getMReuseRate(skill)
                    : caster.getStat().getPReuseRate(skill);
            normalReuse *= 333.0 / (skill.isMagic() ? caster.getMAtkSpd() : caster.getPAtkSpd());
        }
        if (normalReuse <= 10L) {
            return;
        }

        final long reducedReuse = Math.max(10L,
                normalReuse * (100L - AutoFarmConfig.FAST_SKILL_REUSE) / 100L);
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
            @Override
            public void run() {
                if (!caster.isAlikeDead()) {
                    caster.enableSkill(skill.getId());
                }
            }
        }, reducedReuse);
    }
    
    public L2Playable getP() {
        return this.pl;
    }
    
    public void setFarmTypeValue(int iy) {
        if (iy < 0) {
            iy = 0;
        }
        else if (iy > 4) {
            iy = 4;
        }
        this.iy = iy;
        if (this.isAutofarming()) {
            this.stopFarmTask(true);
        }
    }
    
    private int v() {
        return this.iy;
    }
    
    public void setRadiusValue(final int ia) {
        this.iA = ia;
    }
    
    public void setShortcutPageValue(int n) {
        if (n < 1) {
            n = 1;
        }
        else if (n > 10) {
            n = 10;
        }
        this.iz = n - 1;
    }
    
    public int getAttackPercent() {
        return this.iL;
    }
    
    public int getAttackChance() {
        return this.iB;
    }
    
    public int getChancePercent() {
        return this.iM;
    }
    
    public int getChanceChance() {
        return this.iC;
    }
    
    public int getSelfPercent() {
        return this.iN;
    }
    
    public int getSelfChance() {
        return this.iD;
    }
    
    public int getLifePercent() {
        return this.iO;
    }
    
    public int getLifeChance() {
        return this.iE;
    }
    
    public void setAttackSkillValue(final boolean b, final int n) {
        if (b) {
            this.iL = n;
        }
        else {
            this.iB = n;
        }
    }
    
    public void setChanceSkillValue(final boolean b, final int n) {
        if (b) {
            this.iM = n;
        }
        else {
            this.iC = n;
        }
    }
    
    public void setSelfSkillValue(final boolean b, final int n) {
        if (b) {
            this.iN = n;
        }
        else {
            this.iD = n;
        }
    }
    
    public void setLifeSkillValue(final boolean b, final int n) {
        if (b) {
            this.iO = n;
        }
        else {
            this.iE = n;
        }
    }
    
    public void restoreVariables(L2Playable player) {
    	
    	L2PcInstance pp = player.getActingPlayer();
    	
        this.setAttackSkillValue(false, pp.getVarInt("attackChanceSkills", AutoFarmConfig.ATTACK_SKILL_CHANCE));
        this.setAttackSkillValue(true, pp.getVarInt("attackSkillsPercent", AutoFarmConfig.ATTACK_SKILL_PERCENT));
        this.setChanceSkillValue(false, pp.getVarInt("chanceChanceSkills", AutoFarmConfig.CHANCE_SKILL_CHANCE));
        this.setChanceSkillValue(true, pp.getVarInt("chanceSkillsPercent", AutoFarmConfig.CHANCE_SKILL_PERCENT));
        this.setSelfSkillValue(false, pp.getVarInt("selfChanceSkills", AutoFarmConfig.SELF_SKILL_CHANCE));
        this.setSelfSkillValue(true, pp.getVarInt("selfSkillsPercent", AutoFarmConfig.SELF_SKILL_PERCENT));
        this.setLifeSkillValue(false, pp.getVarInt("healChanceSkills", AutoFarmConfig.HEAL_SKILL_CHANCE));
        this.setLifeSkillValue(true, pp.getVarInt("healSkillsPercent", AutoFarmConfig.HEAL_SKILL_PERCENT));
        this.setSummonAttackSkillValue(false, pp.getVarInt("attackSummonChanceSkills", AutoFarmConfig.SUMMON_ATTACK_SKILL_CHANCE));
        this.setSummonAttackSkillValue(true, pp.getVarInt("attackSummonSkillsPercent", AutoFarmConfig.SUMMON_ATTACK_SKILL_PERCENT));
        this.setSummonSelfSkillValue(false, pp.getVarInt("selfSummonChanceSkills", AutoFarmConfig.SUMMON_SELF_SKILL_CHANCE));
        this.setSummonSelfSkillValue(true, pp.getVarInt("selfSummonSkillsPercent", AutoFarmConfig.SUMMON_SELF_SKILL_PERCENT));
        this.setSummonLifeSkillValue(false, pp.getVarInt("healSummonChanceSkills", AutoFarmConfig.SUMMON_HEAL_SKILL_CHANCE));
        this.setSummonLifeSkillValue(true, pp.getVarInt("healSummonSkillsPercent", AutoFarmConfig.SUMMON_HEAL_SKILL_PERCENT));
        this.setShortcutPageValue(pp.getVarInt("shortcutPage", AutoFarmConfig.SHORTCUT_PAGE));
        this.setRadiusValue(pp.getVarInt("farmDistance", AutoFarmConfig.SEARCH_DISTANCE));
        this.setFarmTypeValue(pp.getVarInt("farmType", AutoFarmConfig.FARM_TYPE));
        this.setRndAttackSkills(pp.getVarB("farmRndAttackSkills", false), true);
        this.setRndChanceSkills(pp.getVarB("farmRndChanceSkills", false), true);
        this.setRndSelfSkills(pp.getVarB("farmRndSelfSkills", false), true);
        this.setRndLifeSkills(pp.getVarB("farmRndLifeSkills", false), true);
        this.setRndSummonAttackSkills(pp.getVarB("farmRndSummonAttackSkills", false), true);
        this.setRndSummonSelfSkills(pp.getVarB("farmRndSummonSelfSkills", false), true);
        this.setRndSummonLifeSkills(pp.getVarB("farmRndSummonLifeSkills", false), true);
        this.setLeaderAssist(pp.getVarB("farmLeaderAssist", false), true);
        this.setKeepLocation(pp.getLoc(), pp.getVarB("farmKeepLocation", false), true);
        this.setExDelaySkill(pp.getVarB("farmExDelaySkill", false), true);
        this.setExSummonDelaySkill(pp.getVarB("farmExSummonDelaySkill", false), true);
        this.setRunTargetCloseUp(pp.getVarB("farmRunTargetCloseUp", false), true);
        this.setUseSummonSkills(pp.getVarB("farmUseSummonSkills", false), true);
        this.setAssistMonsterAttack(pp.getVarB("farmAssistMonsterAttack", false), true);
        this.setTargetRestoreMp(pp.getVarB("farmTargetRestoreMp", false), true);
        this.setAcpHpPercent(pp.getVarInt("acpHpPercent", 0));
        this.setAcpMpPercent(pp.getVarInt("acpMpPercent", 0));
        this.D(player.getActingPlayer());
    }
    
    private void D(L2PcInstance player) {
        final String var = player.getVar("farmAttackSkills");
        if (var != null && !var.isEmpty()) {
            this.getAttackSpells().clear();
            for (final String s : var.split(";")) {
                if (s != null) {
                    final L2Skill knownSkill = player.getKnownSkill(Integer.parseInt(s));
                    if (knownSkill != null) {
                        this.getAttackSpells().add(knownSkill.getId());
                    }
                }
            }
        }
        final String var2 = player.getVar("farmChanceSkills");
        if (var2 != null && !var2.isEmpty()) {
            this.getChanceSpells().clear();
            for (final String s2 : var2.split(";")) {
                if (s2 != null) {
                    final L2Skill knownSkill2 = player.getKnownSkill(Integer.parseInt(s2));
                    if (knownSkill2 != null) {
                        this.getChanceSpells().add(knownSkill2.getId());
                    }
                }
            }
        }
        final String var3 = player.getVar("farmSelfSkills");
        if (var3 != null && !var3.isEmpty()) {
            this.getSelfSpells().clear();
            for (final String s3 : var3.split(";")) {
                if (s3 != null) {
                    final L2Skill knownSkill3 = player.getKnownSkill(Integer.parseInt(s3));
                    if (knownSkill3 != null) {
                        this.getSelfSpells().add(knownSkill3.getId());
                    }
                }
            }
        }
        final String var4 = player.getVar("farmHealSkills");
        if (var4 != null && !var4.isEmpty()) {
            this.getLowLifeSpells().clear();
            for (final String s4 : var4.split(";")) {
                if (s4 != null) {
                    final L2Skill knownSkill4 = player.getKnownSkill(Integer.parseInt(s4));
                    if (knownSkill4 != null) {
                        this.getLowLifeSpells().add(knownSkill4.getId());
                    }
                }
            }
        }
        final String var5 = player.getVar("farmAttackSummonSkills");
        if (var5 != null && !var5.isEmpty()) {
            this.getSummonAttackSpells().clear();
            for (final String s5 : var5.split(";")) {
                if (s5 != null) {
                    this.getSummonAttackSpells().add(Integer.parseInt(s5));
                }
            }
        }
        final String var6 = player.getVar("farmSelfSummonSkills");
        if (var6 != null && !var6.isEmpty()) {
            this.getSummonSelfSpells().clear();
            for (final String s6 : var6.split(";")) {
                if (s6 != null) {
                    this.getSummonSelfSpells().add(Integer.parseInt(s6));
                }
            }
        }
        final String var7 = player.getVar("farmHealSummonSkills");
        if (var7 != null && !var7.isEmpty()) {
            this.getSummonHealSpells().clear();
            for (final String s7 : var7.split(";")) {
                if (s7 != null) {
                    this.getSummonHealSpells().add(Integer.parseInt(s7));
                }
            }
        }
    }
    
    public void saveSkills(final String s) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player != null) {
            this.k(player, s);
        }
    }
    
    private void k(final L2PcInstance player, final String s) {
        List<Integer> list = null;
        switch (s) {
            case "farmAttackSkills": {
                list = this.getAttackSpells();
                break;
            }
            case "farmChanceSkills": {
                list = this.getChanceSpells();
                break;
            }
            case "farmSelfSkills": {
                list = this.getSelfSpells();
                break;
            }
            case "farmHealSkills": {
                list = this.getLowLifeSpells();
                break;
            }
            case "farmAttackSummonSkills": {
                list = this.getSummonAttackSpells();
                break;
            }
            case "farmSelfSummonSkills": {
                list = this.getSummonSelfSpells();
                break;
            }
            case "farmHealSummonSkills": {
                list = this.getSummonHealSpells();
                break;
            }
        }
        if (list != null) {
            if (list.isEmpty()) {
                player.unsetVar(s);
                return;
            }
            String string = "";
            final Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                string = string + (int)iterator.next() + ";";
            }
            player.setVar(s, string);
        }
    }
    
    public int getShortcutsIndex() {
        return this.iz;
    }
    
    public int getFarmRadius() {
        return this.iA;
    }
    
    private List<Integer> e(final List<Integer> list) {
    	
        final L2PcInstance player = getP().getActingPlayer();
        return (player == null) ? Collections.emptyList() : (List<Integer>)Arrays.<L2ShortCut>stream(player.getAllShortCuts()).filter(paramShortCut -> (paramShortCut.getPage() == getShortcutsIndex() && paramShortCut.getType() == 2 && list.contains(Integer.valueOf(paramShortCut.getSlot())))).map(L2ShortCut::getId).collect(Collectors.toList());
 
    }
    
    private void E(final L2PcInstance player) {
        this.bb.clear();
        final List<Integer> e = this.e(this.aX);
        if (!e.isEmpty()) {
            for (final int intValue : e) {
                final L2Skill knownSkill = player.getKnownSkill(intValue);
                if (knownSkill != null && (knownSkill.getSkillType() == L2SkillType.DOT || knownSkill.getSkillType() == L2SkillType.MDOT || knownSkill.getSkillType() == L2SkillType.POISON || knownSkill.getSkillType() == L2SkillType.BLEED || knownSkill.getSkillType() == L2SkillType.DEBUFF || knownSkill.getSkillType() == L2SkillType.SLEEP || knownSkill.getSkillType() == L2SkillType.ROOT || knownSkill.getSkillType() == L2SkillType.PARALYZE || knownSkill.getSkillType() == L2SkillType.MUTE || knownSkill.isSpoilSkill() || knownSkill.isSweepSkill() || knownSkill.getId() == 1263)) {
                    this.bb.add(intValue);
                }
            }
            this.k(player, "farmChanceSkills");
            e.clear();
        }
    }
    
    public List<Integer> getChanceSpells() {
        return this.bb;
    }
    
    private void F(final L2PcInstance player) {
        this.ba.clear();
        final List<Integer> e = this.e(this.aW);
   
        if (!e.isEmpty()) {
            for (final int intValue : e) {
                final L2Skill knownSkill = player.getKnownSkill(intValue);
                if (knownSkill != null && !knownSkill.isSpoilSkill() && !knownSkill.isSweepSkill() && knownSkill.getId() != 1263 && (knownSkill.getSkillType() == L2SkillType.AGGDAMAGE || knownSkill.getSkillType() == L2SkillType.PDAM || knownSkill.getSkillType() == L2SkillType.MANADAM || knownSkill.getSkillType() == L2SkillType.MDAM || knownSkill.getSkillType() == L2SkillType.DRAIN || knownSkill.getSkillType() == L2SkillType.CPDAM || knownSkill.getSkillType() == L2SkillType.STUN)) {
                    this.ba.add(intValue);
                }
            }
                        
            this.k(player, "farmAttackSkills");
            e.clear();
        }
    }
    
    public List<Integer> getAttackSpells() {
        return this.ba;
    }
    
    private void G(final L2PcInstance player) {
        this.bc.clear();
        final List<Integer> e = this.e(this.aY);
        if (!e.isEmpty()) {
            for (final int intValue : e) {
                final L2Skill knownSkill = player.getKnownSkill(intValue);
                if (knownSkill != null) {
                    if (!knownSkill.isToggle() && !knownSkill.isMusic() && knownSkill.getSkillType() != L2SkillType.BUFF && !knownSkill.isCubic()) {
                        continue;
                    }
                    this.bc.add(intValue);
                }
            }
            this.k(player, "farmSelfSkills");
            e.clear();
        }
    }
    
    public List<Integer> getSelfSpells() {
        return this.bc;
    }
    
    public void refreshLowLifeSkills(final L2PcInstance player) {
        this.bd.clear();
        final List<Integer> e = this.e(this.aZ);
        if (!e.isEmpty()) {
            for (final int intValue : e) {
                final L2Skill knownSkill = player.getKnownSkill(intValue);
                if (knownSkill != null) {
                    if (knownSkill.getSkillType() != L2SkillType.DRAIN && knownSkill.getSkillType() != L2SkillType.HEAL && knownSkill.getSkillType() != L2SkillType.HEAL_PERCENT && knownSkill.getSkillType() != L2SkillType.MANAHEAL && knownSkill.getSkillType() != L2SkillType.MANAHEAL_PERCENT) {
                        continue;
                    }
                    this.bd.add(intValue);
                }
            }
            this.k(player, "farmHealSkills");
            e.clear();
        }
    }
    
    public List<Integer> getLowLifeSpells() {
        return this.bd;
    }
    
    public void checkAllSlots() {
    	
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return;
        }
        this.E(player);
        this.F(player);
        this.G(player);
        this.refreshLowLifeSkills(player);
    }
    
    public void startFarmTask() {
    	
    	 L2PcInstance player = getP().getActingPlayer();
   
    	  if (player == null) {
    	     return;
    	  }
    	  
        if (this.isAutofarming() || !AutoFarmConfig.ALLOW_AUTO_FARM || (AutoFarmConfig.AUTO_FARM_FOR_PREMIUM && !player.isVip())) {
            player.sendMessage("AUTO FARM PREMIUM ONLY");
            return;
        }
       if (AutoFarmManager.getInstance().getActiveFarms(player.getHost()) <= 0 && !AutoFarmManager.getInstance().isNonCheckPlayer(player.getObjectId())) {
           player.sendMessage("EXCEEDED LIMIT ON USE OF SERVICE.");
           return;
       }
        if (!AutoFarmConfig.AUTO_FARM_ALLOW_FOR_CURSED_WEAPON && player.isCursedWeaponEquipped()) {
            player.sendMessage("AUTO HUNTING PROHIBITED CW.");
            return;
        }
        if (AutoFarmConfig.AUTO_FARM_LIMIT_ZONE_NAMES != null && !AutoFarmConfig.AUTO_FARM_LIMIT_ZONE_NAMES.isEmpty()) {
            final Iterator<L2Zone> iterator = player.getZones().iterator();
            while (iterator.hasNext()) {
                if (!AutoFarmConfig.AUTO_FARM_LIMIT_ZONE_NAMES.contains(iterator.next().getName())) {
                    continue;
                }
                player.sendMessage("AUTO HUNTING PROHIBITED.");
                return;
            }
        }
        try {
            if (this.T != null) {
                this.T.cancel(false);
                this.T = null;
            }
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
      
        
        AutoFarmManager.getInstance().addActiveFarm(player.getHost(), player.getObjectId());
        
        if (this.isKeepLocation()) {
            this.setKeepLocation(player.getLoc());
        }
        int farm_INTERVAL_TASK = AutoFarmConfig.FARM_INTERVAL_TASK;
        
        switch (this.v()) {
            case 0: {
                if (farm_INTERVAL_TASK <= 0) {
                    farm_INTERVAL_TASK = ((player.getPAtkSpd() > 1000) ? 500 : 1000);
                }
                this.T = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoPhysicalFarmTask(this), 1000L, farm_INTERVAL_TASK);
                break;
            }
            case 1: {
                if (farm_INTERVAL_TASK <= 0) {
                    farm_INTERVAL_TASK = ((player.getPAtkSpd() > 1000) ? 500 : 1000);
                }
                this.T = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoArcherFarmTask(this), 1000L, farm_INTERVAL_TASK);
                break;
            }
            case 2: {
                if (farm_INTERVAL_TASK <= 0) {
                    farm_INTERVAL_TASK = ((player.getMAtkSpd() > 1000) ? 500 : 1000);
                }
                this.T = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoMagicFarmTask(this), 1000L, farm_INTERVAL_TASK);
                break;
            }
            case 3: {
                if (farm_INTERVAL_TASK <= 0) {
                    if (player.getPAtkSpd() > player.getMAtkSpd()) {
                        farm_INTERVAL_TASK = ((player.getPAtkSpd() > 1000) ? 500 : 1000);
                    }
                    else {
                        farm_INTERVAL_TASK = ((player.getMAtkSpd() > 1000) ? 500 : 1000);
                    }
                }
                this.T = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoHealFarmTask(this), 1000L, farm_INTERVAL_TASK);
                break;
            }
            case 4: {
                if (player.getPet() == null) {
                    player.sendMessage("YOU HAVE NO SUMMON AUTOFARMING DEACTIVATE.");
                    AutoFarmManager.getInstance().removeActiveFarm(player.getHost(), player.getObjectId());
                    return;
                }
                if (farm_INTERVAL_TASK <= 0) {
                    if (player.getPAtkSpd() > player.getMAtkSpd()) {
                        farm_INTERVAL_TASK = ((player.getPAtkSpd() > 1000) ? 700 : 1000);
                    }
                    else {
                        farm_INTERVAL_TASK = ((player.getMAtkSpd() > 1000) ? 700 : 1000);
                    }
                }
                this.T = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSummonFarmTask(this), 1000L, farm_INTERVAL_TASK);
                break;
            }
        }
        final boolean b = AutoFarmConfig.AUTO_FARM_FREE || (AutoFarmConfig.PREMIUM_FARM_FREE && player.isVip());
        if (AutoFarmConfig.FARM_ONLINE_TYPE && !b) {
            final long n = player.getVarLong("activeFarmOnlineTask", 0L) - this.getLastFarmOnlineTime();
            try {
                if (this.U != null) {
                    this.U.cancel(false);
                    this.U = null;
                }
            }
            catch (Exception ex2) {}
        
            this.U = ThreadPoolManager.getInstance().scheduleGeneral(new AutoFarmEndTask(this), n);
            this.setFarmOnlineTime();
        }
        if (AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL != null && AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL != AbnormalEffect.NULL) {
            player.startAbnormalEffect(AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL);
        }
        if (AutoFarmConfig.SERVICE_AUTO_FARM_SET_RED_RING) {
            player.setTeam(2);
            player.broadcastUserInfo();
        }
        player.setIsAuto(true);
        player.sendMessage("Auto-farming activated");
    }
    
    public void stopFarmTask(final boolean b) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null || !AutoFarmConfig.ALLOW_AUTO_FARM) {
            return;
        }
        try {
            if (this.T != null) {
                this.T.cancel(false);
                this.T = null;
            }
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
        final boolean b2 = AutoFarmConfig.AUTO_FARM_FREE || (AutoFarmConfig.PREMIUM_FARM_FREE && player.isVip());
        if (AutoFarmConfig.FARM_ONLINE_TYPE && !b2) {
            try {
                if (this.U != null) {
                    this.U.cancel(false);
                    this.U = null;
                }
            }
            catch (Exception ex2) {}
            final long br = this.getLastFarmOnlineTime() + (System.currentTimeMillis() - this.getFarmOnlineTime());
            player.setVarLong("activeFarmOnlineTime", br);
            this.br = br;
            this.bq = 0L;
        }
    
        AutoFarmManager.getInstance().removeActiveFarm(player.getHost(), player.getObjectId());
        if (AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL != null && AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL != AbnormalEffect.NULL) {
            player.stopAbnormalEffect(AutoFarmConfig.SERVICES_AUTO_FARM_ABNORMAL);
        }
        if (AutoFarmConfig.SERVICE_AUTO_FARM_SET_RED_RING) {
            player.setTeam(0);
            player.broadcastUserInfo();
        }
        player.setTarget(null);
        player.sendPacket(new TargetUnselected(player));
        player.getAI().setIntention(CtrlIntention.ACTIVE);
        player.abortAttack();
        player.abortCast();
        player.setIsAuto(false);
        player.sendMessage("Auto-farming deactivated.");
        if (b) {
            this.startFarmTask();
        }
    }
    
    public boolean isAutofarming() {
        return this.T != null;
    }
    
    public void checkFarmTask() {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return;
        }
        if (AutoFarmConfig.FARM_ONLINE_TYPE) {
            final long varLong = player.getVarLong("activeFarmOnlineTask", 0L);
            if (player.getVarLong("activeFarmOnlineTime", 0L) <= varLong && varLong != 0L) {
                this.cw = true;
                this.br = player.getVarLong("activeFarmOnlineTime", 0L);
            }
            else {
                this.cw = false;
            }
        }
        else {
            final long varLong2 = player.getVarLong("activeFarmTask", 0L);
            if (varLong2 > System.currentTimeMillis()) {
                if (this.U == null) {
                    this.U = ThreadPoolManager.getInstance().scheduleGeneral(new AutoFarmEndTask(this), varLong2 - System.currentTimeMillis());
                }
                this.bp = varLong2;
            }
            else {
                this.bp = 0L;
            }
        }
    }
    
    public void setAutoFarmEndTask(final long bp) {
        if (bp == 0L && this.U != null) {
            this.U.cancel(false);
            this.U = null;
        }
        this.bp = bp;
    }
    
    public long getAutoFarmEnd() {
        return this.bp;
    }
    
    public boolean isActiveAutofarm() {
        final L2PcInstance player = getP().getActingPlayer();
        return player != null && (player.isGM() || this.U != null || AutoFarmConfig.AUTO_FARM_FREE || (AutoFarmConfig.PREMIUM_FARM_FREE && player.isVip()) || (AutoFarmConfig.FARM_ONLINE_TYPE && this.isActiveFarmOnlineTime()));
    }
    
    public boolean isActiveFarmTask() {
        return this.U != null;
    }
    
    public boolean isRndAttackSkills() {
        return this.ch;
    }
    
    public boolean isRndChanceSkills() {
        return this.ci;
    }
    
    public boolean isRndSelfSkills() {
        return this.cj;
    }
    
    public boolean isRndLifeSkills() {
        return this.ck;
    }
    
    public void setRndAttackSkills(final boolean ch, final boolean b) {
        this.ch = ch;
        if (!b) {
            this.b("farmRndAttackSkills", this.ch ? 1 : 0);
        }
    }
    
    public void setRndChanceSkills(final boolean ci, final boolean b) {
        this.ci = ci;
        if (!b) {
            this.b("farmRndChanceSkills", this.ci ? 1 : 0);
        }
    }
    
    public void setRndSelfSkills(final boolean cj, final boolean b) {
        this.cj = cj;
        if (!b) {
            this.b("farmRndSelfSkills", this.cj ? 1 : 0);
        }
    }
    
    public void setRndLifeSkills(final boolean ck, final boolean b) {
        this.ck = ck;
        if (!b) {
            this.b("farmRndLifeSkills", this.ck ? 1 : 0);
        }
    }
    
    public boolean isLeaderAssist() {
        return this.co;
    }
    
    public boolean isKeepLocation() {
        return this.cp;
    }
    
    public boolean isExtraDelaySkill() {
        return this.cq;
    }
    
    public boolean isExtraSummonDelaySkill() {
        return this.cr;
    }
    
    public boolean isRunTargetCloseUp() {
        return this.cs;
    }
    
    public boolean isUseSummonSkills() {
        return this.cv;
    }
    
    public void setLeaderAssist(final boolean co, final boolean b) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return;
        }
        if (player.getParty() != null && player.getParty().isLeader(player)) {
            this.co = false;
        }
        else {
            this.co = co;
        }
        if (!b) {
            this.b("farmLeaderAssist", this.co ? 1 : 0);
        }
    }
    
    public void setKeepLocation(final Location keepLocation, final boolean cp, final boolean b) {
        this.cp = cp;
        if (!b) {
            this.b("farmKeepLocation", this.cp ? 1 : 0);
            if (this.cp) {
                this.setKeepLocation(keepLocation);
            }
        }
    }
    
    public void setExDelaySkill(final boolean cq, final boolean b) {
        this.cq = cq;
        if (!b) {
            this.b("farmExDelaySkill", this.cq ? 1 : 0);
        }
    }
    
    public void setExSummonDelaySkill(final boolean cr, final boolean b) {
        this.cr = cr;
        if (!b) {
            this.b("farmExSummonDelaySkill", this.cr ? 1 : 0);
        }
    }
    
    public void setRunTargetCloseUp(final boolean cs, final boolean b) {
        this.cs = cs;
        if (!b) {
            this.b("farmRunTargetCloseUp", this.cs ? 1 : 0);
        }
    }
    
    public void setUseSummonSkills(final boolean cv, final boolean b) {
        this.cv = cv;
        if (!b) {
            this.b("farmUseSummonSkills", this.cv ? 1 : 0);
        }
    }
    
    public boolean isAssistMonsterAttack() {
        return this.ct;
    }
    
    public boolean isTargetRestoreMp() {
        return this.cu;
    }
    
    public void setAssistMonsterAttack(final boolean ct, final boolean b) {
        this.ct = ct;
        if (!b) {
            this.b("farmAssistMonsterAttack", this.ct ? 1 : 0);
        }
    }
    
    public void setTargetRestoreMp(final boolean cu, final boolean b) {
        this.cu = cu;
        if (!b) {
            this.b("farmTargetRestoreMp", this.cu ? 1 : 0);
        }
    }
    
    public List<Integer> getSummonAttackSpells() {
        return this.be;
    }
    
    public List<Integer> getSummonSelfSpells() {
        return this.bf;
    }
    
    public List<Integer> getSummonHealSpells() {
        return this.bg;
    }
    
    public int getSummonAttackPercent() {
        return this.iI;
    }
    
    public int getSummonAttackChance() {
        return this.iF;
    }
    
    public int getSummonSelfPercent() {
        return this.iJ;
    }
    
    public int getSummonSelfChance() {
        return this.iG;
    }
    
    public int getSummonLifePercent() {
        return this.iK;
    }
    
    public int getSummonLifeChance() {
        return this.iH;
    }
    
    public void setSummonAttackSkillValue(final boolean b, final int n) {
        if (b) {
            this.iI = n;
        }
        else {
            this.iF = n;
        }
    }
    
    public void setSummonSelfSkillValue(final boolean b, final int n) {
        if (b) {
            this.iJ = n;
        }
        else {
            this.iG = n;
        }
    }
    
    public void setSummonLifeSkillValue(final boolean b, final int n) {
        if (b) {
            this.iK = n;
        }
        else {
            this.iH = n;
        }
    }
    
    public boolean isRndSummonAttackSkills() {
        return this.cl;
    }
    
    public boolean isRndSummonSelfSkills() {
        return this.cm;
    }
    
    public boolean isRndSummonLifeSkills() {
        return this.cn;
    }
    
    public void setRndSummonAttackSkills(final boolean cl, final boolean b) {
        this.cl = cl;
        if (!b) {
            this.b("farmRndSummonAttackSkills", this.cl ? 1 : 0);
        }
    }
    
    public void setRndSummonSelfSkills(final boolean cm, final boolean b) {
        this.cm = cm;
        if (!b) {
            this.b("farmRndSummonSelfSkills", this.cm ? 1 : 0);
        }
    }
    
    public void setRndSummonLifeSkills(final boolean cn, final boolean b) {
        this.cn = cn;
        if (!b) {
            this.b("farmRndSummonLifeSkills", this.cn ? 1 : 0);
        }
    }
    
    private void b(final String s, final int n) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return;
        }
        player.setVarInt(s, n);
    }
    
    public final List<L2MonsterInstance> getAroundNpc(final L2PcInstance player, final Function<L2MonsterInstance, Boolean> function) {
    	
        ArrayList<L2MonsterInstance> list = new ArrayList<L2MonsterInstance>();

        for (final L2Object npc : L2World.getVisibleObjects(player, this.getFarmRadius())) {
        	
        	if (npc instanceof L2Attackable && (npc instanceof L2MonsterInstance))
			{
        		
        		L2MonsterInstance npcInstance = (L2MonsterInstance) npc;
        		
        	
            if (!npcInstance.isDead()
            		&& npcInstance.isVisible()
            		&& !(npcInstance instanceof L2ChestInstance)
            		&& (!(npcInstance instanceof L2MinionInstance) || !(((L2MinionInstance)npcInstance).getLeader() instanceof L2RaidBossInstance))
            		&& !npcInstance.isRaid()
            		&& function.apply((L2MonsterInstance) npcInstance)) {

                if (ArrayUtils.contains(AutoFarmConfig.AUTO_FARM_IGNORED_NPC_ID, npcInstance.getNpcId())) {
                    continue;
                }
                if (!npcInstance.hasAI()) {
                    continue;
                }
          
                if (npcInstance.getAttackByList().isEmpty() || npcInstance.getAttackByList().contains(player)) {
                    list.add(npcInstance);
                }
                else {
                    if (player.getParty() == null) {
                        continue;
                    }
                    for (final L2PcInstance player2 : player.getParty().getPartyMembers()) {
                        if (player2 != null && npcInstance.getAttackByList().contains(player2)) {
                            list.add(npcInstance);
                        }
                    }
                }
            }
			}
        }
        return list;
    }
    
    public L2Skill nextAttackSkill(final L2MonsterInstance target, final long n) {
    	
        final L2PcInstance player = getP().getActingPlayer();

        if (player == null || this.getAttackSpells().isEmpty() || !Rnd.chance(this.getAttackChance())) {
            return null;
        }
        if (this.isExtraDelaySkill() && n > System.currentTimeMillis()) {
            return null;
        }
        if (player.getCurrentMpPercents() < this.getAttackPercent()) {
            return null;
        }
        if (this.isRndAttackSkills()) {
            return this.aba(target);
        }
        
        final Iterator<Integer> iterator = this.getAttackSpells().iterator();
        while (iterator.hasNext()) {
            final L2Skill knownSkill = player.getKnownSkill(iterator.next());
            if (knownSkill == null) {
                continue;
            }
            // The attack rotation must never select buffs, heals or utility
            // skills. Those belong to their dedicated lists and otherwise can
            // leave a mage idle while surrounded by monsters.
            if (!knownSkill.isOffensive()) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, target, false)) {
                continue;
            }
            if (knownSkill.isOffensive() && knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE && target == null) {
                continue;
            }

            assert target != null;
            player.setTarget(target);
            player.sendPacket(new MyTargetSelected(target.getObjectId(), player.getLevel() - target.getLevel()));
            player.sendPacket(target.makeStatusUpdate(9, 10));
            return knownSkill;
        }
        return null;
    }
    
    private L2Skill aba(final L2Character target) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return null;
        }
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        L2Skill skill = null;
        final Iterator<Integer> iterator = this.getAttackSpells().iterator();
        while (iterator.hasNext()) {
            final L2Skill knownSkill = player.getKnownSkill(iterator.next());
            if (knownSkill == null) {
                continue;
            }
            if (!knownSkill.isOffensive()) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if(target.isPlayer() && target.getObjectId() == player.getObjectId()) {
            	player.sendMessage("You cannot attack yourself...");
            	continue;
            }
            if(player.getVar("targetUnskill") != null){
                String[] unt = player.getVar("targetUnskill").split("_");
                if(Integer.parseInt(unt[0]) == target.getObjectId() && Integer.parseInt(unt[1]) == knownSkill.getId()){
                    //player.sendMessage("target "+ target.getObjectId() + " ignored by "+ knownSkill.getId() + " in random cast");
                    continue;
                }
            }
            if (!knownSkill.checkCondition(player, target, false)) {
                player.setVar("targetUnskill", target.getObjectId() + "_" + knownSkill.getId() );
                continue;
            }
            if (knownSkill.isOffensive() && knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE && target == null) {
                continue;
            }
            list.add(knownSkill);
        }
        if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
            assert target != null;
            player.setTarget(target);
            player.sendPacket(new MyTargetSelected(target.getObjectId(), player.getLevel() - target.getLevel()));
            player.sendPacket(target.makeStatusUpdate(9, 10));
        }
        list.clear();
        return skill;
    }
    
    public L2Skill nextChanceSkill(final L2MonsterInstance npcInstance, final long n) {
    
  	  
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null || this.getChanceSpells().isEmpty() || !Rnd.chance(this.getChanceChance())) {
            return null;
        }
        if (this.isExtraDelaySkill() && n > System.currentTimeMillis()) {
            return null;
        }
     	 
        final double currentMpPercents = player.getCurrentMpPercents();
        if (npcInstance == null || currentMpPercents < this.getChancePercent()) {
            return null;
        }
        if (this.isRndChanceSkills()) {
            return this.b(npcInstance);
        }
        for (final int intValue : this.getChanceSpells()) {
            final L2Skill knownSkill = player.getKnownSkill(intValue);
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, npcInstance, false)) {
                continue;
            }
            if (knownSkill.isSpoilSkill() && npcInstance.isSpoil()) {
                continue;
            }
            if (knownSkill.isSweepSkill() && !npcInstance.isDead()) {
                continue;
            }
            if (npcInstance.getFirstEffect(knownSkill) != null) {
                continue;
            }
            return knownSkill;
        }
        return null;
    }
    
    private L2Skill b(final L2MonsterInstance npcInstance) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return null;
        }
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        L2Skill skill = null;
        for (final int intValue : this.getChanceSpells()) {
            final L2Skill knownSkill = player.getKnownSkill(intValue);
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, npcInstance, false)) {
                continue;
            }
            if (knownSkill.isSpoilSkill() && npcInstance.isSpoil()) {
                continue;
            }
            if (knownSkill.isSweepSkill() && !npcInstance.isDead()) {
                continue;
            }
            if (npcInstance.getFirstEffect(knownSkill) != null) {
                continue;
            }
            list.add(knownSkill);
        }
        if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        return skill;
    }
    
    public L2Skill nextSelfSkill(final L2Character target) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null || this.getSelfSpells().isEmpty() || !Rnd.chance(this.getSelfChance())) {
            return null;
        }
   
        if (player.getCurrentMpPercents() < this.getSelfPercent()) {
            return null;
        }
        if (this.isRndSelfSkills()) {
            return this.a(target);
        }
        for (final int intValue : this.getSelfSpells()) {
            final L2Skill knownSkill = player.getKnownSkill(intValue);
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition((L2Character)player, (target != null) ? target : player, false)) {
                continue;
            }
            if (knownSkill.isToggle() && player.getFirstEffect(knownSkill) == null) {
                return knownSkill;
            }
            if (target != null && target.getFirstEffect(knownSkill) == null && knownSkill.getTargetType() !=L2Skill.SkillTargetType.TARGET_SELF) {
                player.setTarget(target);
                return knownSkill;
            }
            if ((target == null || !(target instanceof L2Summon)) && player.getFirstEffect(knownSkill) == null && knownSkill.getTargetType() !=L2Skill.SkillTargetType.TARGET_PET) {
                player.setTarget(player);
                return knownSkill;
            }
        }
        return null;
    }
    
    private L2Skill a(final L2Character target) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return null;
        }
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        final ArrayList<L2Skill> list2 = new ArrayList<L2Skill>();
        L2Skill skill = null;
        for (final int intValue : this.getSelfSpells()) {
            final L2Skill knownSkill = player.getKnownSkill(intValue);
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, (target != null) ? target : player, false)) {
                continue;
            }
            
   
            if (knownSkill.isToggle() && player.getFirstEffect(knownSkill) == null) {
                list.add(knownSkill);
            }
            else {
                if (player.getFirstEffect(knownSkill) == null && knownSkill.getTargetType() !=L2Skill.SkillTargetType.TARGET_PET) {
                    list.add(knownSkill);
                }
                if (target == null || target.getFirstEffect(knownSkill) != null || knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                    continue;
                }
                list2.add(knownSkill);
            }
        }
        boolean b = true;
        if (!list2.isEmpty()) {
            skill = list2.get(Rnd.get(list2.size()));
            b = false;
        }
        else if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        list2.clear();
        if (skill == null) {
            return null;
        }
        if (target != null && !b) {
            player.setTarget(target);
        }
        else {
            player.setTarget(player);
        }
        return skill;
    }
    
    public L2Skill nextHealSkill(final L2MonsterInstance npcInstance, final L2Character paramCreature) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null || this.getLowLifeSpells().isEmpty() || !Rnd.chance(this.getLifeChance())) {
            return null;
        }
        final double currentHpPercents = player.getCurrentHpPercents();
        final double n = (paramCreature != null) ? paramCreature.getCurrentHpPercents() : 100.0;
        final double n2 = (paramCreature != null) ? paramCreature.getCurrentMpPercents() : 100.0;
        final boolean b = n < this.getLifePercent();
        final boolean b2 = currentHpPercents < this.getLifePercent();
        final boolean b3 = n2 < this.getLifePercent();
        if (!b && !b2 && !b3) {
            return null;
        }
        if (this.isRndLifeSkills()) {
            return this.a(npcInstance, paramCreature);
        }
        final Iterator<Integer> iterator = this.getLowLifeSpells().iterator();
        while (iterator.hasNext()) {
            final L2Skill knownSkill = player.getKnownSkill(iterator.next());
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, knownSkill.isOffensive() ? npcInstance : player, false)) {
                continue;
            }
            if (knownSkill.isOffensive() && npcInstance == null) {
                continue;
            }
            if (b(knownSkill)) {
                if (!b && !b2) {
                    continue;
                }
                if (b && paramCreature != null && !paramCreature.isDead() && knownSkill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) {
                    if (knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET && !(paramCreature instanceof L2Summon)) {
                        continue;
                    }
                    player.setTarget(paramCreature);
                    return knownSkill;
                }
                else {
                    if (!b2) {
                        return null;
                    }
                    if (knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET) {
                        continue;
                    }
                    player.setTarget(player);
                    return knownSkill;
                }
            }
            else {
                if (a(knownSkill) && this.isTargetRestoreMp() && paramCreature != null && b3 && !paramCreature.isDead() && knownSkill.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) {
                    player.setTarget(paramCreature);
                    return knownSkill;
                }
                return knownSkill;
            }
        }
        return null;
    }
    
    private L2Skill a(final L2MonsterInstance npcInstance, final L2Character target) {
        final L2PcInstance player = getP().getActingPlayer();
        if (player == null) {
            return null;
        }
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        L2Skill skill = null;
        final double currentHpPercents = player.getCurrentHpPercents();
        final double n = (target != null) ? target.getCurrentHpPercents() : 100.0;
        final double n2 = (target != null) ? target.getCurrentMpPercents() : 100.0;
        final boolean b = n < this.getLifePercent();
        final boolean b2 = currentHpPercents < this.getLifePercent();
        final boolean b3 = n2 < this.getLifePercent();
        if (!b && !b2 && !b3) {
            return null;
        }
        final Iterator<Integer> iterator = this.getLowLifeSpells().iterator();
        while (iterator.hasNext()) {
            final L2Skill knownSkill = player.getKnownSkill(iterator.next());
            if (knownSkill == null) {
                continue;
            }
            if (player.isSkillDisabled(knownSkill.getId())) {
                continue;
            }
            if (!knownSkill.checkCondition(player, knownSkill.isOffensive() ? npcInstance : player, false)) {
                continue;
            }
            if (knownSkill.isOffensive() && npcInstance == null) {
                continue;
            }
            if (b || b2) {
                if (!b(knownSkill)) {
                    continue;
                }
                if (b) {
                    if (target == null || target.isDead() || knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                        continue;
                    }
                    if (knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET && !(target instanceof L2Summon)) {
                        continue;
                    }
                    list.add(knownSkill);
                }
                else {
                    if (!b2) {
                        continue;
                    }
                    if (knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET) {
                        continue;
                    }
                    list.add(knownSkill);
                }
            }
            else {
                if (!b3 || !a(knownSkill) || !this.isTargetRestoreMp() || target == null || target.isDead() || knownSkill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                    continue;
                }
                list.add(knownSkill);
            }
        }
        if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        if (skill == null) {
            return null;
        }
        if (b || b3) {
            player.setTarget(target);
        }
        else {
            player.setTarget(player);
        }
        return skill;
    }
    
    public Location getKeepLocation() {
        return this.B;
    }
    
    public void setKeepLocation(final Location b) {
        this.B = b;
    }
    
    public L2Skill nextSummonAttackSkill(final L2MonsterInstance npcInstance, final L2Summon summon, final long n) {
        if (this.getSummonAttackSpells().isEmpty() || !Rnd.chance(this.getSummonAttackChance())) {
            return null;
        }
        if (this.isExtraSummonDelaySkill() && n > System.currentTimeMillis()) {
            return null;
        }
        if (summon.getCurrentMpPercents() < this.getSummonAttackPercent()) {
            return null;
        }
        if (this.isRndSummonAttackSkills()) {
            return this.a(npcInstance, summon);
        }
        for (final int intValue : this.getSummonAttackSpells()) {
                final L2Skill info = summon.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (summon.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(summon, npcInstance, false)) {
                    continue;
                }
                if (info.isOffensive() && info.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE && npcInstance == null) {
                    continue;
                }
                return info;
        }
        return null;
    }
    
    private L2Skill a(final L2NpcInstance npcInstance, final L2Summon summon) {
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        L2Skill skill = null;
        for (final int intValue : this.getSummonAttackSpells()) {
                final L2Skill info = summon.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (summon.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(summon, npcInstance, false)) {
                    continue;
                }
                if (info.isOffensive() && info.getTargetType() ==L2Skill.SkillTargetType.TARGET_ONE && npcInstance == null) {
                    continue;
                }
                list.add(info);
        }
        if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        return skill;
    }
    
    public L2Skill nextSummonSelfSkill(final L2Summon summon, final L2Character target) {
        if (this.getSummonSelfSpells().isEmpty() || !Rnd.chance(this.getSummonSelfChance())) {
            return null;
        }
        if (summon.getCurrentMpPercents() < this.getSummonSelfPercent()) {
            return null;
        }
        if (this.isRndSummonSelfSkills()) {
            return this.a(summon, target);
        }
        for (final int intValue : this.getSummonSelfSpells()) {
                final L2Skill info = summon.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (summon.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(summon, (target != null) ? target : summon, false)) {
                    continue;
                }
                if (info.isToggle() && summon.getFirstEffect(info) == null) {
                    return info;
                }
                if (target != null && target.getFirstEffect(info) == null && info.getTargetType() !=L2Skill.SkillTargetType.TARGET_SELF && info.getTargetType() !=L2Skill.SkillTargetType.TARGET_PET) {
                    summon.setTarget(target);
                    return info;
                }
                if (summon.getFirstEffect(info) == null) {
                    return info;
                }
                continue;
        }
        return null;
    }
    
    private L2Skill a(final L2Summon target, final L2Character target2) {
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        final ArrayList<L2Skill> list2 = new ArrayList<L2Skill>();
        L2Skill skill = null;
        for (final int intValue : this.getSummonSelfSpells()) {
                final L2Skill info = target.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (target.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(target, (target2 != null) ? target2 : target, false)) {
                    continue;
                }
                if (info.isToggle() && target.getFirstEffect(info) == null) {
                    list.add(info);
                }
                else {
                    if (target2 != null && target.getFirstEffect(info) == null && info.getTargetType() !=L2Skill.SkillTargetType.TARGET_SELF && info.getTargetType() !=L2Skill.SkillTargetType.TARGET_PET) {
                        list2.add(info);
                    }
                    if (target.getFirstEffect(info) != null) {
                        continue;
                    }
                    list.add(info);
                }
        }
        boolean b = true;
        if (!list2.isEmpty()) {
            skill = list2.get(Rnd.get(list2.size()));
            b = false;
        }
        else if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        list2.clear();
        if (skill == null) {
            return null;
        }
        if (target2 != null && !b) {
            target.setTarget(target2);
        }
        else {
            target.setTarget(target);
        }
        return skill;
    }
    
    public L2Skill nextSummonHealSkill(final L2MonsterInstance npcInstance, final L2Summon target, final L2Character L2Character) {
        if (this.getSummonHealSpells().isEmpty() || !Rnd.chance(this.getSummonLifeChance())) {
            return null;
        }
        final double currentHpPercents = target.getCurrentHpPercents();
        final double n = (L2Character != null) ? L2Character.getCurrentHpPercents() : 100.0;
        final double n2 = (L2Character != null) ? L2Character.getCurrentMpPercents() : 100.0;
        final boolean b = n < this.getSummonLifePercent();
        final boolean b2 = currentHpPercents < this.getSummonLifePercent();
        final boolean b3 = n2 < this.getSummonLifePercent();
        if (!b && !b2 && !b3) {
            return null;
        }
        if (this.isRndSummonLifeSkills()) {
            return this.a(npcInstance, target, L2Character);
        }
        for (final int intValue : this.getSummonHealSpells()) {
                final L2Skill info = target.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (target.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(target, (L2Character != null && b) ? L2Character : target, false)) {
                    continue;
                }
                if (info.isOffensive() && npcInstance == null) {
                    continue;
                }
                if (b(info)) {
                    if (!b && !b2) {
                        continue;
                    }
                    if (b && L2Character != null && !L2Character.isDead() && info.getTargetType() !=L2Skill.SkillTargetType.TARGET_SELF) {
                        if (info.getTargetType() == L2Skill.SkillTargetType.TARGET_PET && !(L2Character instanceof L2Summon)) {
                            continue;
                        }
                        target.setTarget(L2Character);
                        return info;
                    }
                    else {
                        if (b2) {
                            target.setTarget(target);
                            return info;
                        }
                        return null;
                    }
                }
                else {
                    if (a(info) && L2Character != null && b3 && !L2Character.isDead() && info.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) {
                        target.setTarget(L2Character);
                        return info;
                    }
                    return info;
                }
        }
        return null;
    }
    
    private L2Skill a(final L2MonsterInstance npcInstance, final L2Summon target, final L2Character target2) {
        final ArrayList<L2Skill> list = new ArrayList<L2Skill>();
        L2Skill skill = null;
        final double currentHpPercents = target.getCurrentHpPercents();
        final double n = (target2 != null) ? target2.getCurrentHpPercents() : 100.0;
        final double n2 = (target2 != null) ? target2.getCurrentMpPercents() : 100.0;
        final boolean b = n < this.getSummonLifePercent();
        final boolean b2 = currentHpPercents < this.getSummonLifePercent();
        final boolean b3 = n2 < this.getSummonLifePercent();
        if (!b && !b2 && !b3) {
            return null;
        }
        for (final int intValue : this.getSummonHealSpells()) {
                final L2Skill info = target.getKnownSkill(intValue);
                if (info == null) {
                    continue;
                }
                if (target.isSkillDisabled(info.getId())) {
                    continue;
                }
                if (!info.checkCondition(target, (target2 != null && b) ? target2 : target, false)) {
                    continue;
                }
                if (info.isOffensive() && npcInstance == null) {
                    continue;
                }
                if (b || b2) {
                    if (!b(info)) {
                        continue;
                    }
                    if (b) {
                        if (target2 == null || target2.isDead() || info.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                            continue;
                        }
                        if (info.getTargetType() == L2Skill.SkillTargetType.TARGET_PET && !(target2 instanceof L2Summon)) {
                            continue;
                        }
                        list.add(info);
                    }
                    else {
                        if (!b2) {
                            continue;
                        }
                        list.add(info);
                    }
                }
                else {
                    if (!b3 || !a(info) || target2 == null || target2.isDead() || info.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) {
                        continue;
                    }
                    list.add(info);
                }
        }
        if (!list.isEmpty()) {
            skill = list.get(Rnd.get(list.size()));
        }
        list.clear();
        if (skill == null) {
            return null;
        }
        if (b || b3) {
            target.setTarget(target2);
        }
        else {
            target.setTarget(target);
        }
        return skill;
    }
    
    public L2MonsterInstance getLeaderTarget(final L2PcInstance player) {
        if (player == null) {
            return null;
        }
        final L2Object target = player.getTarget();
        if (target instanceof L2MonsterInstance) {
            final L2MonsterInstance monster = (L2MonsterInstance) target;
            if (!monster.isDead() && monster.hasAI() && monster.isAutoAttackable(player)
                    && monster.getAttackByList().contains(player)) {
                return monster;
            }
        }
        return null;
    }
    
    public long getLastFarmOnlineTime() {
        return this.br;
    }
    
    public boolean isActiveFarmOnlineTime() {
        return this.cw;
    }
    
    public void setFarmOnlineTime() {
        this.bq = System.currentTimeMillis();
    }
    
    public void refreshFarmOnlineTime() {
        this.bq = 0L;
    }
    
    public long getFarmOnlineTime() {
        return this.bq;
    }
    
    public int getAcpHpPercent() {
        return this.iAcpHpPercent;
    }
    
    public void setAcpHpPercent(int percent) {
        this.iAcpHpPercent = Math.max(0, Math.min(100, percent));
        this.b("acpHpPercent", this.iAcpHpPercent);
    }
    
    public int getAcpMpPercent() {
        return this.iAcpMpPercent;
    }
    
    public void setAcpMpPercent(int percent) {
        this.iAcpMpPercent = Math.max(0, Math.min(100, percent));
        this.b("acpMpPercent", this.iAcpMpPercent);
    }
    
    public void checkAndUsePotions(final L2PcInstance player) {
        if (player == null || player.isDead()) {
            return;
        }
        final long now = System.currentTimeMillis();

        checkEmergencyHpRecovery(player);
        
        // HP Potion Check
        if (this.iAcpHpPercent > 0) {
            double currentHpPerc = player.getCurrentHpPercents();
            if (currentHpPerc <= this.iAcpHpPercent) {
                if (now - this.lastHpPotionUse >= 1000) {
                    boolean success = usePotion(player, AutoFarmConfig.ACP_HP_POTION_IDS);
                    if (success) {
                        this.lastHpPotionUse = now;
                    }
                }
            }
        }
        
        // MP Potion Check
        if (this.iAcpMpPercent > 0) {
            double currentMpPerc = player.getCurrentMpPercents();
            if (currentMpPerc <= this.iAcpMpPercent) {
                if (now - this.lastMpPotionUse >= 1000) {
                    boolean success = usePotion(player, AutoFarmConfig.ACP_MP_POTION_IDS);
                    if (success) {
                        this.lastMpPotionUse = now;
                    }
                }
            }
        }
    }

    /**
     * Free continuous recovery while HP remains at or below the configured
     * threshold. It does not consume an item and is independent from ACP.
     */
    private void checkEmergencyHpRecovery(final L2PcInstance player) {
        if (!AutoFarmConfig.EMERGENCY_HP_RECOVERY_ENABLED || AutoFarmConfig.EMERGENCY_HP_RECOVERY_AMOUNT <= 0) {
            return;
        }

        if (player.getCurrentHpPercents() <= AutoFarmConfig.EMERGENCY_HP_RECOVERY_PERCENT) {
            final double recoveredHp = Math.min(player.getMaxHp(),
                    player.getCurrentHp() + AutoFarmConfig.EMERGENCY_HP_RECOVERY_AMOUNT);
            player.setCurrentHp(recoveredHp);
        }
    }
    
    private boolean usePotion(final L2PcInstance player, final int[] potionIds) {
        if (potionIds == null) {
            return false;
        }
        for (final int itemId : potionIds) {
            final L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
            if (item != null && item.getCount() > 0) {
                final com.premium.game.handler.IItemHandler handler = com.premium.game.handler.ItemHandler.getInstance().getItemHandler(itemId);
                if (handler != null) {
                    handler.useItem(player, item);
                    return true;
                }
            }
        }
        return false;
    }
    
    public enum SpellType
    {
        ATTACK, 
        CHANCE, 
        SELF, 
        LOWLIFE;
    }
}
