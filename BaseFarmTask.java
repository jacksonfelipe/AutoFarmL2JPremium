import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import org.apache.log4j.Logger;

import com.premium.game.ai.CtrlIntention;
import com.premium.game.geodata.GeoData;
import com.premium.game.geodata.GeoEngine;
import com.premium.game.model.L2Object;
import com.premium.game.model.L2Skill;
import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.L2Npc;
import com.premium.game.model.actor.L2Summon;
import com.premium.game.model.actor.instance.L2MonsterInstance;
import com.premium.game.model.actor.instance.L2NpcInstance;
import com.premium.game.model.actor.instance.L2PcInstance;

import com.premium.game.model.world.Location;
import com.premium.game.network.ThreadPoolManager;
import com.premium.game.network.serverpackets.L2GameServerPacket;
import com.premium.game.network.serverpackets.MyTargetSelected;
import com.premium.game.util.Util;
import com.premium.util.AbstractHardReference;
import com.premium.util.HardReference;
import com.premium.util.HardReferences;

import javafx.util.Pair;

public abstract class BaseFarmTask implements Runnable {

	protected static Logger _log = Logger.getLogger(BaseFarmTask.class);

	protected static final int RUN_AWAY_STATIC_DISTANCE = 500;

	protected static final int RUN_AWAY_RANDOM_DISTANCE = 100;

	protected static final int MAX_CONSECUTIVE_FAILURES = 3;

	private int consecutiveFailures = 0;

	private final AutoFarmContext Nw;

	private L2MonsterInstance Ny = null;

	private HardReference<L2PcInstance> Nz = HardReferences.emptyRef();

	private HardReference<L2Summon> NA = HardReferences.emptyRef();

	private long NB = 0L;

	private Pair<ScheduledFuture<?>, Location> NC;

	public BaseFarmTask(AutoFarmContext paramAutoFarmContext) {
		this.Nw = paramAutoFarmContext;
	}

	protected AutoFarmContext getAutoFarmContext() {
		return this.Nw;
	}

	public L2PcInstance getCommittedOwner() {
		return (L2PcInstance) this.Nz.get();
	}

	public void setCommittedOwner(L2PcInstance paramPlayer) {
		this.Nz = (paramPlayer != null) ? new AbstractHardReference(paramPlayer) : HardReferences.emptyRef();
	}

	public long getExtraDelay() {
		return this.NB;
	}

	public void setExtraDelay(long paramLong) {
		this.NB = paramLong;
	}

	public Pair<ScheduledFuture<?>, Location> getMoveToPair() {
		return this.NC;
	}

	public void setMoveToPair(Pair<ScheduledFuture<?>, Location> paramPair) {
		this.NC = paramPair;
	}

	protected boolean canAutoAssist() {
		return true;
	}

	private boolean cD() {
		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (player == null)
			return false;
		if (getAutoFarmContext().isKeepLocation() && getAutoFarmContext().getKeepLocation() != null) {
			Pair<ScheduledFuture<?>, Location> pair = moveToAndThan((L2Character) player,
					getAutoFarmContext().getKeepLocation(), this);
			if (pair != null) {
				if (getMoveToPair() != null && getMoveToPair().getKey() != null)
					((ScheduledFuture) getMoveToPair().getKey()).cancel(false);
				setMoveToPair(pair);
				if (getCommittedSummon() != null)
					getCommittedSummon().followOwner();
				return true;
			}
		}
		return false;
	}

	protected boolean selectRandomTarget() {

		final L2PcInstance player = this.getAutoFarmContext().getP().getActingPlayer();

		if (player == null || player.isCastingNow()) {
			return false;
		}
		final L2MonsterInstance committedTarget = this.getCommittedTarget();
		if (committedTarget != null && (this.canAutoAssist() || this.getAutoFarmContext().isAssistMonsterAttack()
				|| !this.getAutoFarmContext().isLeaderAssist())) {
			if (this.spoilCheck()) {
				return false;
			}
			if (!committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(player, committedTarget)) {
				if (player.getTarget() != committedTarget) {
					player.setTarget(committedTarget);
					player.sendPacket(new MyTargetSelected(committedTarget.getObjectId(),
							player.getLevel() - committedTarget.getLevel()));
					player.sendPacket(committedTarget.makeStatusUpdate(9, 10));
				}
				
				return true;
			}
			this.setCommittedTarget(null);
			player.setTarget(null);
			if (this.cD()) {
				return false;
			}
		}

		if (this.getAutoFarmContext().isLeaderAssist()) {
			if (player.getParty() == null) {
				this.setCommittedOwner(null);
				this.getAutoFarmContext().setLeaderAssist(false, false);
			} else {
				if(player.getParty().getLeader() != null) {
					this.setCommittedOwner(player.getParty().getLeader());
				}else {
					this.setCommittedOwner(null);
				}
			}
		}
		if (this.getCommittedSummon() == null) {
			this.setCommittedSummon((player.getPet() != null) ? player.getPet() : null);
		}
		if (this.getCommittedOwner() != null && !this.getCommittedOwner().isDead()
				&& this.getAutoFarmContext().isAssistMonsterAttack()) {
			final L2MonsterInstance leaderTarget;
			this.setCommittedTarget(leaderTarget = this.getAutoFarmContext().getLeaderTarget(this.getCommittedOwner()));
			if (leaderTarget != null) {
				return true;
			}
			if (leaderTarget != null && player.getActingSummon().getDistance(leaderTarget.getX(),
					leaderTarget.getY()) < AutoFarmConfig.RUN_CLOSE_UP_DISTANCE) {
				final Pair<ScheduledFuture<?>, Location> runAwayFromTargetAndThan = this
						.runAwayFromTargetAndThan(leaderTarget, player, 500, 100, this);
				if (runAwayFromTargetAndThan != null) {
					if (this.getMoveToPair() != null && this.getMoveToPair().getKey() != null) {
						((ScheduledFuture) this.getMoveToPair().getKey()).cancel(false);
					}
					this.setMoveToPair(runAwayFromTargetAndThan);
					if (this.getCommittedSummon() != null) {
						// this.getCommittedSummon().getActingSummon().moveToLocation(runAwayFromTargetAndThan.getValue().getX(),
						// runAwayFromTargetAndThan.getValue().getY(),
						// runAwayFromTargetAndThan.getValue().getZ(), 0);
					}
					return false;
				}
			}
		} else {
			if (this.getAutoFarmContext().isLeaderAssist()) {

				if (player.getParty() != null && player.getParty().getLeader().getTarget() != null) {

					L2Object mobTarget = player.getParty().getLeader().getTarget();

					if (mobTarget instanceof L2MonsterInstance) {
						this.setCommittedTarget((L2MonsterInstance) mobTarget);
						player.setTarget(mobTarget);
						player.sendPacket(new MyTargetSelected(mobTarget.getObjectId(), 0));
						player.makeStatusUpdate(9, 10);
					}
				}

				return true;
			}

			final List<L2MonsterInstance> aroundNpc = getAutoFarmContext().getAroundNpc(player,
					npcInstance -> GeoEngine.getInstance().canSeeTarget(player, npcInstance) && !npcInstance.isDead());

			if (aroundNpc.isEmpty() && this.cD() || aroundNpc.size() == 0) {
				return false;
			}

			ArrayList<Double> dists = new ArrayList<Double>();

			for (L2Npc npc : aroundNpc) {
				dists.add(player.getDistance(npc.getX(), npc.getY()));
			}

			int minIndex = minIndex(dists);
			L2MonsterInstance mob = aroundNpc.get(minIndex);
			
			if (!mob.isDead()) {
				player.setTarget(this.setCommittedTarget(mob));
				player.sendPacket(new MyTargetSelected(mob.getObjectId(),
						player.getLevel() - mob.getLevel()));
				player.makeStatusUpdate(9, 10);
				return true;
			}
		}
		return false;

	}

	public static int minIndex(ArrayList<Double> list) {
		return list.indexOf(Collections.min(list));
	}

	protected boolean spoilCheck() {
		L2MonsterInstance npcInstance = getCommittedTarget();
		return (npcInstance != null && npcInstance.isDead() && npcInstance.isMob() && cF() && npcInstance.isSpoil());
	}

	private boolean cE() {
		L2Npc npcInstance = getCommittedTarget().getNpc();
		return (npcInstance != null && npcInstance.isDead() && npcInstance instanceof L2MonsterInstance
				&& ((L2MonsterInstance) npcInstance).isSweepActive());
	}

	protected void tryAttack(boolean paramBoolean) {
	 	
		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();

		if (player == null)
			return;

		if (paramBoolean && getCommittedTarget() != null) {

			physicalAttack();
			tryUseSpell(paramBoolean);

		}
		if (paramBoolean && getCommittedTarget() != null && getAutoFarmContext().isUseSummonSkills())
			tryUseSummonSpell();
		if (paramBoolean && getCommittedTarget() != null)
			physicalAttack();
	}

	protected void physicalAttack() {

		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();

		if (player != null && getCommittedTarget() != null
				&& getCommittedTarget().isAutoAttackable((L2Character) player) && !getCommittedTarget().isAlikeDead()) {
			if (player.getTarget() != getCommittedTarget()) {
				player.setTarget((L2Object) getCommittedTarget());
				player.sendPacket((L2GameServerPacket) new MyTargetSelected(getCommittedTarget().getObjectId(),
						player.getLevel() - getCommittedTarget().getLevel()));
				player.sendPacket((L2GameServerPacket) getCommittedTarget().makeStatusUpdate(new int[] { 9, 10 }));
			}
			if (GeoData.getInstance().canSeeTarget((L2Object) player, (L2Object) getCommittedTarget())) {
				player.getAI().setIntention(CtrlIntention.ATTACK, getCommittedTarget());
			} else if (!getCommittedTarget().isInsideRadius(player, 200, true, false)
					&& player.getAI().getIntention() != CtrlIntention.INTERACT) {
				player.getAI().setIntention(CtrlIntention.INTERACT, getCommittedTarget(), null);
			}
		}
	}

	protected boolean doTryUseLowLifeSkillSpell() {
		L2Skill skill = getAutoFarmContext().nextHealSkill(getCommittedTarget(), null);
		if (skill != null) {
			useMagicSkill(skill, !skill.isOffensive());
			return true;
		}
		return false;
	}

	protected boolean doTryUseSelfSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextSelfSkill(null);
		if (skill != null) {
			useMagicSkill(skill, true);
			return true;
		}
		return false;
	}

	protected boolean doTryUseChanceSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextChanceSkill(getCommittedTarget(), getExtraDelay());

		if (skill != null) {
			useMagicSkill(skill, false);
			return true;
		}
		return false;
	}

	protected boolean doTryUseAttackSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextAttackSkill(getCommittedTarget(), getExtraDelay());
		if (skill != null) {
			useMagicSkill(skill, false);
			return true;
		}
		return false;
	}

	protected void tryUseSpell(boolean paramBoolean) {

		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (player == null || player.isCastingNow())
			return;
		if (paramBoolean)
			doTryUseChanceSkillSpell();
		if (doTryUseLowLifeSkillSpell())
			return;
		if (doTryUseSelfSkillSpell())
			return;
		if (paramBoolean)
			doTryUseAttackSkillSpell();
	}

	protected void tryUseSummonSpell() {
	}

	protected final Pair<ScheduledFuture<?>, Location> moveToAndThan(L2Character paramCreature, Location paramLocation,
			Runnable paramRunnable) {
		if (paramLocation != null && !paramCreature.isOutOfControl()) {
			if (paramCreature.isMoving())
				paramCreature.stopMove();
			double d = paramCreature.getDistance(paramLocation.getX(), paramLocation.getY(), paramLocation.getZ());
			long l = (long) (d
					/ (paramCreature.isRunning() ? paramCreature.getRunSpeed() : paramCreature.getWalkSpeed())
					* 1000.0D);
			if (paramCreature.getActingPlayer().moveToLocation(paramLocation.getX(), paramLocation.getY(),
					paramLocation.getZ(), 0))
				return new Pair<>(ThreadPoolManager.getInstance().scheduleGeneral(paramRunnable,
						Math.max(1500L, 333L + l + AutoFarmConfig.RUN_CLOSE_UP_DELAY)), paramLocation);
		}
		return null;
	}

	protected final Pair<ScheduledFuture<?>, Location> runAwayFromTargetAndThan(
			L2Object target, L2Character character, 
			int distance, int variance, Runnable callback) {
			
		Location location = null;
		try {
			double angle = Math.toRadians(
				Util.calculateAngleFrom(target, character));
			int baseX = character.getX();
			int baseY = character.getY();
			int targetX = baseX + (int)(distance * Math.cos(angle));
			int targetY = baseY + (int)(distance * Math.sin(angle));
			
			location = new Location(targetX, targetY, character.getZ());
			Location safeLocation = Location.findPointToStay(location, variance, 1);
			if (safeLocation == null) {
				return null;
			}
			
			ScheduledFuture<?> future = ThreadPoolManager.getInstance().scheduleAi(() -> {
				character.getAI().setIntention(CtrlIntention.MOVE_TO, safeLocation);
				if (callback != null) {
					callback.run();
				}
			}, 100);
			
			return new Pair<>(future, safeLocation);
			
		} catch (Exception e) {
			_log.error("Failed to calculate runaway position", e);
			return null;
		}
	}

	protected boolean preDoUseMagicSkill(L2Skill paramSkill, boolean paramBoolean) {
		return true;
	}

	protected final void useMagicSkill(L2Skill paramSkill, boolean paramBoolean) {
	
		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (paramSkill == null || player == null || player.isOutOfControl()
				|| (paramSkill.isToggle() && player.isMounted()))
			return;
		if (preDoUseMagicSkill(paramSkill, paramBoolean)) {
			if (getAutoFarmContext().isExtraDelaySkill())
				setExtraDelay(System.currentTimeMillis() + AutoFarmConfig.SKILLS_EXTRA_DELAY);
			b(paramSkill, paramBoolean);
		}
	}

	private void b(L2Skill paramSkill, boolean paramBoolean) {

		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (paramSkill == null || player == null || player.isOutOfControl())
			return;
		if (paramBoolean) {
			L2Object L2Object = player.getTarget();
			
			player.setTarget(player);
			player.useMagic(paramSkill, true, false);
			player.setTarget(L2Object);

			return;
		}
		
		if (player.getTarget() == null)
			return;

		L2Character creature = paramSkill.getFirstOfTargetList(player);
			if(!paramSkill.checkCondition(player, creature)){
				return;
			}

		player.setTarget(creature);
		player.useMagic(paramSkill, true, false);
		
	}

	protected L2MonsterInstance getCommittedTarget() {
		return this.Ny;
	}

	protected L2MonsterInstance setCommittedTarget(L2MonsterInstance paramNpcInstance) {
		return this.Ny = paramNpcInstance;
	}

	public L2Summon getCommittedSummon() {
		return (L2Summon) this.NA.get();
	}

	public void setCommittedSummon(L2Summon paramSummon) {
		this.NA = (paramSummon != null) ? new AbstractHardReference(paramSummon) : HardReferences.emptyRef();
	}

	private boolean cF() {
		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (player == null)
			return false;
		L2Skill skill1 = player.getKnownSkill(42);
		L2Skill skill2 = player.getKnownSkill(444);
		if (skill1 == null && skill2 == null)
			return false;
		if (cE()) {

			useMagicSkill((skill2 != null) ? skill2 : skill1, false);
			return true;
		}
		return false;
	}

	public void run() {
		try {

			L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
			if (player == null)
				return;
			for (String str : AutoFarmConfig.AUTO_FARM_LIMIT_ZONE_NAMES) {
				if (player.isInsideZone(str)) {
					getAutoFarmContext().stopFarmTask(false);
					player.sendMessage("AUTO HUNTING PROHIBITED.");
					return;
				}
			}
			runImpl();
		} catch (Throwable throwable) {
			_log.error("Exception: RunnableImpl.run(): " + throwable, throwable);
		}
	}

	public abstract void runImpl() throws Exception;

	private void handleFailure(String reason) {
		consecutiveFailures++;
		if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
			_log.error("Too many consecutive failures", new RuntimeException(reason));
			getAutoFarmContext().stopFarmTask(false);
			consecutiveFailures = 0;
		}
	}
}
