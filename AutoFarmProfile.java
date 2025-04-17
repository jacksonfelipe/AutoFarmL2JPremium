import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.premium.game.model.actor.instance.L2PcInstance;

/**
 * Sistema de perfis para AutoFarm.
 * Permite que jogadores salvem diferentes configurações de farm.
 */
public class AutoFarmProfile {
    private static final Logger _log = Logger.getLogger(AutoFarmProfile.class);
    
    private static final Map<Integer, List<FarmProfile>> playerProfiles = new HashMap<>();
    
    public static class FarmProfile {
        private String name;
        private List<Integer> attackSkills;
        private List<Integer> buffSkills;
        private List<Integer> healSkills;
        private int searchDistance;
        private boolean useSummon;
        private List<Integer> summonSkills;
        private int targetPriority; // 0 = nearest, 1 = highest level, 2 = lowest level
        private boolean autoLoot;
        private int minHpPercent;
        private int minMpPercent;
        private boolean usePartySync;
        
        public FarmProfile(String name) {
            this.name = name;
            this.attackSkills = new ArrayList<>();
            this.buffSkills = new ArrayList<>();
            this.healSkills = new ArrayList<>();
            this.summonSkills = new ArrayList<>();
            this.searchDistance = AutoFarmConfig.MIN_SEARCH_DISTANCE;
            this.useSummon = false;
            this.targetPriority = 0;
            this.autoLoot = true;
            this.minHpPercent = 70;
            this.minMpPercent = 30;
            this.usePartySync = false;
        }
        
        // Getters e Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public List<Integer> getAttackSkills() { return attackSkills; }
        public void setAttackSkills(List<Integer> skills) { this.attackSkills = skills; }
        
        public List<Integer> getBuffSkills() { return buffSkills; }
        public void setBuffSkills(List<Integer> skills) { this.buffSkills = skills; }
        
        public List<Integer> getHealSkills() { return healSkills; }
        public void setHealSkills(List<Integer> skills) { this.healSkills = skills; }
        
        public int getSearchDistance() { return searchDistance; }
        public void setSearchDistance(int distance) {
            this.searchDistance = Math.max(AutoFarmConfig.MIN_SEARCH_DISTANCE,
                                         Math.min(distance, AutoFarmConfig.MAX_SEARCH_DISTANCE));
        }
        
        public boolean isUseSummon() { return useSummon; }
        public void setUseSummon(boolean use) { this.useSummon = use; }
        
        public List<Integer> getSummonSkills() { return summonSkills; }
        public void setSummonSkills(List<Integer> skills) { this.summonSkills = skills; }
        
        public int getTargetPriority() { return targetPriority; }
        public void setTargetPriority(int priority) { this.targetPriority = priority; }
        
        public boolean isAutoLoot() { return autoLoot; }
        public void setAutoLoot(boolean autoLoot) { this.autoLoot = autoLoot; }
        
        public int getMinHpPercent() { return minHpPercent; }
        public void setMinHpPercent(int percent) {
            this.minHpPercent = Math.max(1, Math.min(percent, 99));
        }
        
        public int getMinMpPercent() { return minMpPercent; }
        public void setMinMpPercent(int percent) {
            this.minMpPercent = Math.max(1, Math.min(percent, 99));
        }
        
        public boolean isUsePartySync() { return usePartySync; }
        public void setUsePartySync(boolean use) { this.usePartySync = use; }
    }
    
    /**
     * Obtém todos os perfis de um jogador
     */
    public static List<FarmProfile> getPlayerProfiles(L2PcInstance player) {
        return playerProfiles.computeIfAbsent(player.getObjectId(), k -> new ArrayList<>());
    }
    
    /**
     * Adiciona um novo perfil para o jogador
     */
    public static boolean addProfile(L2PcInstance player, FarmProfile profile) {
        List<FarmProfile> profiles = getPlayerProfiles(player);
        
        if (profiles.size() >= AutoFarmConfig.MAX_PROFILES_PER_PLAYER) {
            return false;
        }
        
        profiles.add(profile);
        return true;
    }
    
    /**
     * Remove um perfil do jogador
     */
    public static boolean removeProfile(L2PcInstance player, String profileName) {
        List<FarmProfile> profiles = getPlayerProfiles(player);
        return profiles.removeIf(p -> p.getName().equals(profileName));
    }
    
    /**
     * Obtém um perfil específico do jogador
     */
    public static FarmProfile getProfile(L2PcInstance player, String profileName) {
        List<FarmProfile> profiles = getPlayerProfiles(player);
        return profiles.stream()
                      .filter(p -> p.getName().equals(profileName))
                      .findFirst()
                      .orElse(null);
    }
    
    /**
     * Salva os perfis do jogador
     */
    public static void saveProfiles(L2PcInstance player) {
        // TODO: Implementar persistência dos perfis
        _log.info("Saving profiles for player: " + player.getName());
    }
    
    /**
     * Carrega os perfis do jogador
     */
    public static void loadProfiles(L2PcInstance player) {
        // TODO: Implementar carregamento dos perfis
        _log.info("Loading profiles for player: " + player.getName());
    }
} 