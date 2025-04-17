import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.model.items.L2ItemInstance;

/**
 * Sistema de relatórios para AutoFarm.
 * Registra estatísticas e gera relatórios de farm.
 */
public class AutoFarmReport {
    private static final Logger _log = Logger.getLogger(AutoFarmReport.class);
    
    private static final Map<Integer, FarmSession> activeSessions = new ConcurrentHashMap<>();
    
    public static class FarmSession {
        private final long startTime;
        private long endTime;
        private int totalKills;
        private long totalExp;
        private long totalSp;
        private final Map<Integer, Integer> itemsCollected; // itemId -> quantidade
        private int skillsUsed;
        private int potionsUsed;
        private int deaths;
        private final String profileName;
        private final List<String> events;
        
        public FarmSession(String profileName) {
            this.startTime = System.currentTimeMillis();
            this.profileName = profileName;
            this.itemsCollected = new HashMap<>();
            this.events = new ArrayList<>();
        }
        
        public void addKill() {
            totalKills++;
        }
        
        public void addExp(long exp) {
            totalExp += exp;
        }
        
        public void addSp(long sp) {
            totalSp += sp;
        }
        
        public void addItem(int itemId, int count) {
            itemsCollected.merge(itemId, count, Integer::sum);
        }
        
        public void addSkillUse() {
            skillsUsed++;
        }
        
        public void addPotionUse() {
            potionsUsed++;
        }
        
        public void addDeath() {
            deaths++;
        }
        
        public void addEvent(String event) {
            events.add(event);
        }
        
        public void end() {
            endTime = System.currentTimeMillis();
        }
        
        public long getDuration() {
            return (endTime > 0 ? endTime : System.currentTimeMillis()) - startTime;
        }
        
        // Getters
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public int getTotalKills() { return totalKills; }
        public long getTotalExp() { return totalExp; }
        public long getTotalSp() { return totalSp; }
        public Map<Integer, Integer> getItemsCollected() { return itemsCollected; }
        public int getSkillsUsed() { return skillsUsed; }
        public int getPotionsUsed() { return potionsUsed; }
        public int getDeaths() { return deaths; }
        public String getProfileName() { return profileName; }
        public List<String> getEvents() { return events; }
    }
    
    /**
     * Inicia uma nova sessão de farm para o jogador
     */
    public static void startSession(L2PcInstance player, String profileName) {
        activeSessions.put(player.getObjectId(), new FarmSession(profileName));
    }
    
    /**
     * Finaliza a sessão de farm do jogador
     */
    public static FarmSession endSession(L2PcInstance player) {
        FarmSession session = activeSessions.remove(player.getObjectId());
        if (session != null) {
            session.end();
            saveSession(player, session);
        }
        return session;
    }
    
    /**
     * Obtém a sessão ativa do jogador
     */
    public static FarmSession getActiveSession(L2PcInstance player) {
        return activeSessions.get(player.getObjectId());
    }
    
    /**
     * Gera um relatório HTML da sessão
     */
    public static String generateHtmlReport(FarmSession session) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<center><font color=\"LEVEL\">Relatório de Farm</font></center><br>");
        
        long durationMinutes = session.getDuration() / 60000;
        html.append("Duração: ").append(durationMinutes).append(" minutos<br>");
        html.append("Perfil: ").append(session.getProfileName()).append("<br>");
        html.append("Total de Kills: ").append(session.getTotalKills()).append("<br>");
        html.append("EXP Ganha: ").append(session.getTotalExp()).append("<br>");
        html.append("SP Ganho: ").append(session.getTotalSp()).append("<br>");
        html.append("Skills Usadas: ").append(session.getSkillsUsed()).append("<br>");
        html.append("Poções Usadas: ").append(session.getPotionsUsed()).append("<br>");
        html.append("Mortes: ").append(session.getDeaths()).append("<br><br>");
        
        if (!session.getItemsCollected().isEmpty()) {
            html.append("Itens Coletados:<br>");
            session.getItemsCollected().forEach((itemId, count) -> {
                html.append("- ").append(getItemName(itemId)).append(": ").append(count).append("<br>");
            });
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Salva a sessão no banco de dados
     */
    private static void saveSession(L2PcInstance player, FarmSession session) {
        // TODO: Implementar persistência das sessões
        _log.info("Saving farm session for player: " + player.getName());
    }
    
    /**
     * Obtém o nome de um item pelo ID
     */
    private static String getItemName(int itemId) {
        // TODO: Implementar busca do nome do item
        return "Item #" + itemId;
    }
    
    /**
     * Envia relatório para a guild do jogador
     */
    public static void sendGuildReport(L2PcInstance player, FarmSession session) {
        if (!AutoFarmConfig.ENABLE_GUILD_REPORTS || player.getClan() == null) {
            return;
        }
        
        String report = generateHtmlReport(session);
        // TODO: Implementar envio do relatório para a guild
        _log.info("Sending guild report for player: " + player.getName());
    }
} 