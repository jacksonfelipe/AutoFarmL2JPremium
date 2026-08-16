
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
import com.premium.game.network.serverpackets.TargetUnselected;
import com.premium.game.util.Util;
import com.premium.util.AbstractHardReference;
import com.premium.util.HardReference;
import com.premium.util.HardReferences;

public abstract class BaseFarmTask implements Runnable {

	protected static Logger _log = Logger.getLogger(BaseFarmTask.class);

	protected static final int RUN_AWAY_STATIC_DISTANCE = 500;

	protected static final int RUN_AWAY_RANDOM_DISTANCE = 100;

	private final AutoFarmContext Nw;

	private L2MonsterInstance Ny = null;

	private HardReference<L2PcInstance> Nz = HardReferences.emptyRef();

	private HardReference<L2Summon> NA = HardReferences.emptyRef();

	private long NB = 0L;

	private Pair<ScheduledFuture<?>, Location> NC;

	// A failed/interrupted cast can leave the player AI in CAST state. The client
	// clears it when the player moves, but AutoFarm must recover on its own.
	private long ND = 0L;

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
            if (!committedTarget.isDead() && committedTarget.isVisible()
                    && committedTarget.isAutoAttackable(player)) {
                if (player.getTarget() != committedTarget) {
                    player.setTarget(committedTarget);
                    player.sendPacket(new MyTargetSelected(committedTarget.getObjectId(),
                            player.getLevel() - committedTarget.getLevel()));
                    player.sendPacket(committedTarget.makeStatusUpdate(9, 10));
                }
                
                return true;
            }
            this.setCommittedTarget(null);
            if (this.cD()) {
                player.setTarget(null);
                player.sendPacket(new TargetUnselected(player));
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
            if (leaderTarget != null && !leaderTarget.isDead()) {
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
                    player.setTarget(null);
                    player.sendPacket(new TargetUnselected(player));
                    return false;
                }
            }
        } else {
            if (this.getAutoFarmContext().isLeaderAssist()) {
                if (player.getParty() != null && player.getParty().getLeader() != null) {
                    L2MonsterInstance mobTarget = this.getAutoFarmContext().getLeaderTarget(player.getParty().getLeader());
                    if (mobTarget != null && GeoEngine.getInstance().canSeeTarget(player, mobTarget)) {
                        this.setCommittedTarget(mobTarget);
                        player.setTarget(mobTarget);
                        player.sendPacket(new MyTargetSelected(mobTarget.getObjectId(), 0));
                        player.sendPacket(mobTarget.makeStatusUpdate(9, 10));
                        return true;
                    }
                }
                this.setCommittedTarget(null);
                player.setTarget(null);
                player.sendPacket(new TargetUnselected(player));
                return false;
            }

            final List<L2MonsterInstance> aroundNpc = getAutoFarmContext().getAroundNpc(player,
                    npcInstance -> !npcInstance.isDead() && npcInstance.isAutoAttackable(player));

            if (aroundNpc.isEmpty()) {
                this.setCommittedTarget(null);
                this.cD();
                player.setTarget(null);
                player.sendPacket(new TargetUnselected(player));
                if (!player.isCastingNow() && !player.isCastingSimultaneouslyNow()) {
                    player.abortAttack();
                    player.getAI().setIntention(CtrlIntention.ACTIVE);
                }
                return false;
            }

            // Prefer mobs that are already in sight, but do not stop farming just
            // because a farther mob requires walking around an obstacle.
            L2MonsterInstance mob = null;
            boolean hasLineOfSight = false;
            boolean isAttackingPlayer = false;
            double shortestDistance = Double.MAX_VALUE;
            for (L2MonsterInstance candidate : aroundNpc) {
                final boolean canSeeCandidate = GeoEngine.getInstance().canSeeTarget(player, candidate);
                final boolean candidateIsAttackingPlayer = candidate.getTarget() == player;
                final double distance = player.getDistance(candidate.getX(), candidate.getY());
                // When several mobs are attacking, first remove the pressure
                // from monsters already focused on this player. This mirrors
                // retail Auto Hunt target priority and avoids chasing an idle
                // distant mob while being surrounded.
                if (mob == null || (candidateIsAttackingPlayer && !isAttackingPlayer)
                        || (candidateIsAttackingPlayer == isAttackingPlayer && canSeeCandidate && !hasLineOfSight)
                        || (candidateIsAttackingPlayer == isAttackingPlayer && canSeeCandidate == hasLineOfSight
                                && distance < shortestDistance)) {
                    mob = candidate;
                    hasLineOfSight = canSeeCandidate;
                    isAttackingPlayer = candidateIsAttackingPlayer;
                    shortestDistance = distance;
                }
            }
            
            if (mob != null && !mob.isDead()) {
                player.setTarget(this.setCommittedTarget(mob));
                player.sendPacket(new MyTargetSelected(mob.getObjectId(),
                        player.getLevel() - mob.getLevel()));
                player.sendPacket(mob.makeStatusUpdate(9, 10));
                return true;
            }
        }
        player.setTarget(null);
        player.sendPacket(new TargetUnselected(player));
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

		boolean skillStarted = false;
		if (paramBoolean && getCommittedTarget() != null)
			skillStarted = tryUseSpell(true);
		if (paramBoolean && getCommittedTarget() != null && getAutoFarmContext().isUseSummonSkills())
			tryUseSummonSpell();
		// useMagic() can accept a cast just before isCastingNow() becomes true.
		// Do not overwrite that fresh cast with an ATTACK intention in the same
		// farm tick; only fall back to the physical attack when no skill started.
		if (paramBoolean && getCommittedTarget() != null && !skillStarted && !player.isCastingNow()
				&& !player.isCastingSimultaneouslyNow())
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
			// Do not resend ATTACK for the same target on every farm tick. The player
			// AI treats that as ActionFailed, which interrupts an otherwise valid
			// auto-attack when the interval is short.
			if (player.getAI().getIntention() != CtrlIntention.ATTACK
					|| player.getAI().getAttackTarget() != getCommittedTarget()) {
				player.getAI().setIntention(CtrlIntention.ATTACK, getCommittedTarget());
			}
		}
	}

	protected void moveCloserToCommittedTarget(int desiredRange) {
		final L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		final L2MonsterInstance target = getCommittedTarget();
		if (player == null || target == null || target.isAlikeDead() || player.isCastingNow()
				|| player.isCastingSimultaneouslyNow() || player.isMoving()) {
			return;
		}

		if (!target.isInsideRadius(player, desiredRange, true, false)) {
			player.moveToLocation(target.getX(), target.getY(), target.getZ(), Math.max(50, desiredRange - 50));
		}
	}

	protected boolean doTryUseLowLifeSkillSpell() {
		L2Skill skill = getAutoFarmContext().nextHealSkill(getCommittedTarget(), null);
		if (skill != null) {
			return useMagicSkill(skill, !skill.isOffensive());
		}
		return false;
	}

	protected boolean doTryUseSelfSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextSelfSkill(null);
		if (skill != null) {
			return useMagicSkill(skill, true);
		}
		return false;
	}

	protected boolean doTryUseChanceSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextChanceSkill(getCommittedTarget(), getExtraDelay());

		if (skill != null) {
			return useMagicSkill(skill, false);
		}
		return false;
	}

	protected boolean doTryUseAttackSkillSpell() {

		L2Skill skill = getAutoFarmContext().nextAttackSkill(getCommittedTarget(), getExtraDelay());
		if (skill != null) {
			return useMagicSkill(skill, false);
		}
		return false;
	}

	protected boolean tryUseSpell(boolean paramBoolean) {

		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (player == null || player.isCastingNow())
			return false;
		if (paramBoolean && doTryUseChanceSkillSpell())
			return true;
		if (doTryUseLowLifeSkillSpell())
			return true;
		if (doTryUseSelfSkillSpell())
			return true;
		if (paramBoolean)
			return doTryUseAttackSkillSpell();
		return false;
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

	protected final Pair<ScheduledFuture<?>, Location> runAwayFromTargetAndThan(L2Object paramL2Object,
			L2Character paramCreature, int paramInt1, int paramInt2, Runnable paramRunnable) {
		double d = Math.toRadians(Util.calculateAngleFrom(paramL2Object, (L2Object) paramCreature));
		int i = paramCreature.getX();
		int j = paramCreature.getY();
		int k = i + (int) (paramInt1 * Math.cos(d));
		int m = j + (int) (paramInt1 * Math.sin(d));
		Location location = Location.findPointToStay(new Location(k, m, paramCreature.getZ()), paramInt2, 1);
//    for (byte b = 0; b < 10 && !GeoData.getInstance().canSeeTarget(paramL2Object, paramCreature)))
//      location = Location.findPointToStay(new Location(k, m, paramCreature.getZ()), paramInt2, 0); 
		return moveToAndThan(paramCreature, location, paramRunnable);
	}

	protected boolean preDoUseMagicSkill(L2Skill paramSkill, boolean paramBoolean) {
		return true;
	}

	private boolean recoverStuckCast(L2PcInstance player) {
		if (player == null) {
			return false;
		}

		if (!player.isCastingNow() && !player.isCastingSimultaneouslyNow()) {
			ND = 0L;
			if (player.getAI().getIntention() == CtrlIntention.CAST) {
				player.getAI().setIntention(CtrlIntention.ACTIVE);
			}
			return false;
		}

		final long now = System.currentTimeMillis();
		if (ND == 0L) {
			ND = now;
			return false;
		}

		long maximumCastTime = 2500L;
		if (player.getCurrentSkill() != null && player.getCurrentSkill().getSkill() != null) {
			final L2Skill currentSkill = player.getCurrentSkill().getSkill();
			maximumCastTime = Math.max(maximumCastTime,
					currentSkill.getHitTime() + currentSkill.getCoolTime() + 1500L);
		}
		maximumCastTime = Math.min(maximumCastTime, 10000L);

		if (now - ND < maximumCastTime) {
			return false;
		}

		// The scheduled cast did not finish. Abort it exactly as a player movement
		// would, restore a clean AI state and let the next farm tick choose a skill.
		player.abortCast();
		player.getAI().setIntention(CtrlIntention.ACTIVE);
		restoreTarget(player);
		ND = 0L;
		return true;
	}

	private void restoreTarget(L2PcInstance player) {
		if (player == null)
			return;
		L2MonsterInstance committedTarget = getCommittedTarget();
		if (committedTarget != null && !committedTarget.isAlikeDead()) {
			if (player.getTarget() != committedTarget) {
				player.setTarget(committedTarget);
				player.sendPacket(new MyTargetSelected(committedTarget.getObjectId(), player.getLevel() - committedTarget.getLevel()));
				player.sendPacket(committedTarget.makeStatusUpdate(9, 10));
			}
		} else {
			player.setTarget(null);
			player.sendPacket(new TargetUnselected(player));
		}
	}

	protected final boolean useMagicSkill(L2Skill paramSkill, boolean paramBoolean) {
	
		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (paramSkill == null || player == null || player.isOutOfControl()
				|| (paramSkill.isToggle() && player.isMounted()))
			return false;
		
		// AutoFarm owns the combat rotation: never queue a new cast, but interrupt
		// its own physical swing so a configured skill can be used immediately.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow()) {
			restoreTarget(player);
			return false;
		}
		if (player.isAttackingNow()) {
			player.abortAttack();
		}
		
		if (preDoUseMagicSkill(paramSkill, paramBoolean)) {
			if (b(paramSkill, paramBoolean)) {
				ND = System.currentTimeMillis();
				if (getAutoFarmContext().isExtraDelaySkill())
					setExtraDelay(System.currentTimeMillis() + AutoFarmConfig.SKILLS_EXTRA_DELAY);
				return true;
			}
		} else {
			restoreTarget(player);
		}
		return false;
	}

	private boolean b(L2Skill paramSkill, boolean paramBoolean) {

		L2PcInstance player = getAutoFarmContext().getP().getActingPlayer();
		if (paramSkill == null || player == null || player.isOutOfControl())
			return false;
		
		// The physical attack was already interrupted by useMagicSkill(). Do not
		// enqueue skills while a normal or simultaneous cast is still active.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow()) {
			restoreTarget(player);
			return false;
		}
		
		// The normal cooldown remains authoritative. Fast reuse schedules an earlier
		// enable only after a successful AutoFarm cast.
		if (player.isSkillDisabled(paramSkill.getId())) {
			restoreTarget(player);
			return false;
		}
		
		if (paramBoolean) {
			L2Object skillTarget = player.getTarget();
			if (skillTarget == null) {
				skillTarget = player;
			}
			
			player.setTarget(skillTarget);
			player.useMagic(paramSkill, true, false);
			getAutoFarmContext().scheduleFastReuse(player, paramSkill);
			
			restoreTarget(player);
			return true;
		}
		
		if (player.getTarget() == null) {
			restoreTarget(player);
			return false;
		}

		L2Character creature = paramSkill.getFirstOfTargetList(player);
		if (creature == null || creature.isAlikeDead() || !paramSkill.checkCondition(player, creature, false)) {
			restoreTarget(player);
			return false;
		}

		player.setTarget(creature);
		player.useMagic(paramSkill, true, false);
		getAutoFarmContext().scheduleFastReuse(player, paramSkill);
		
		restoreTarget(player);
		return true;
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
			if (player == null || player.isOnline() != 1 || !getAutoFarmContext().isAutofarming()) {
				if (getAutoFarmContext().isAutofarming()) {
					getAutoFarmContext().stopFarmTask(false);
				}
				return;
			}
			
			// Trava de sistema: Verifica se o farm exige VIP e se o jogador ainda possui o status
			if (AutoFarmConfig.AUTO_FARM_FOR_PREMIUM && !player.isVip()) {
				getAutoFarmContext().stopFarmTask(false);
				player.sendMessage("AutoFarm desativado: Beneficio VIP expirado.");
				return;
			}

			for (String str : AutoFarmConfig.AUTO_FARM_LIMIT_ZONE_NAMES) {
				if (player.isInsideZone(str)) {
					getAutoFarmContext().stopFarmTask(false);
					player.sendMessage("AUTO HUNTING PROHIBITED.");
					return;
				}
			}
			getAutoFarmContext().checkAndUsePotions(player);
			if (recoverStuckCast(player)) {
				return;
			}
			runImpl();
		} catch (Throwable throwable) {
			_log.error("Exception: RunnableImpl.run(): " + throwable, throwable);
		}
	}

	public abstract void runImpl() throws Exception;
}
