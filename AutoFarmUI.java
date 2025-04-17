import java.util.List;

import org.apache.log4j.Logger;

import com.premium.game.model.actor.instance.L2PcInstance;
import com.premium.game.network.serverpackets.NpcHtmlMessage;

/**
 * Interface gráfica do sistema AutoFarm.
 * Fornece uma interface amigável para configuração e monitoramento.
 */
public class AutoFarmUI {
    private static final Logger _log = Logger.getLogger(AutoFarmUI.class);
    
    /**
     * Mostra o menu principal do AutoFarm
     */
    public static void showMainMenu(L2PcInstance player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html><body>");
        sb.append("<center><font color=\"LEVEL\">Sistema AutoFarm</font></center><br>");
        
        // Status atual
        AutoFarmReport.FarmSession session = AutoFarmReport.getActiveSession(player);
        if (session != null) {
            sb.append("<table width=270 border=0>");
            sb.append("<tr><td>Status:</td><td><font color=\"00FF00\">Ativo</font></td></tr>");
            sb.append("<tr><td>Perfil:</td><td>").append(session.getProfileName()).append("</td></tr>");
            sb.append("<tr><td>Tempo:</td><td>").append(session.getDuration() / 60000).append(" minutos</td></tr>");
            sb.append("<tr><td>Kills:</td><td>").append(session.getTotalKills()).append("</td></tr>");
            sb.append("</table><br>");
        } else {
            sb.append("<table width=270 border=0>");
            sb.append("<tr><td>Status:</td><td><font color=\"FF0000\">Inativo</font></td></tr>");
            sb.append("</table><br>");
        }
        
        // Botões principais
        sb.append("<center>");
        if (session == null) {
            sb.append("<button value=\"Iniciar Farm\" action=\"bypass -h voice .farmstart\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        } else {
            sb.append("<button value=\"Parar Farm\" action=\"bypass -h voice .farmstop\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        }
        sb.append("<button value=\"Perfis\" action=\"bypass -h voice .farmprofiles\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        sb.append("<button value=\"Configurações\" action=\"bypass -h voice .farmsettings\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        sb.append("<button value=\"Relatórios\" action=\"bypass -h voice .farmreports\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
        sb.append("</center>");
        
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }
    
    /**
     * Mostra o menu de perfis
     */
    public static void showProfilesMenu(L2PcInstance player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html><body>");
        sb.append("<center><font color=\"LEVEL\">Perfis de Farm</font></center><br>");
        
        List<AutoFarmProfile.FarmProfile> profiles = AutoFarmProfile.getPlayerProfiles(player);
        
        if (profiles.isEmpty()) {
            sb.append("Você não tem perfis salvos.<br><br>");
        } else {
            sb.append("<table width=270 border=0>");
            for (AutoFarmProfile.FarmProfile profile : profiles) {
                sb.append("<tr><td>").append(profile.getName()).append("</td>");
                sb.append("<td><button value=\"Usar\" action=\"bypass -h voice .farmuse ").append(profile.getName())
                  .append("\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
                sb.append("<td><button value=\"Editar\" action=\"bypass -h voice .farmedit ").append(profile.getName())
                  .append("\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
                sb.append("<td><button value=\"Excluir\" action=\"bypass -h voice .farmdelete ").append(profile.getName())
                  .append("\" width=65 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
            }
            sb.append("</table><br>");
        }
        
        sb.append("<center>");
        sb.append("<button value=\"Novo Perfil\" action=\"bypass -h voice .farmnew\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        sb.append("<button value=\"Voltar\" action=\"bypass -h voice .autofarm\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
        sb.append("</center>");
        
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }
    
    /**
     * Mostra o menu de configurações
     */
    public static void showSettingsMenu(L2PcInstance player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html><body>");
        sb.append("<center><font color=\"LEVEL\">Configurações do Farm</font></center><br>");
        
        // Configurações gerais
        sb.append("<table width=270 border=0>");
        sb.append("<tr><td>Distância de Busca:</td><td><edit var=\"dist\" width=30 height=12></td></tr>");
        sb.append("<tr><td>Usar Summon:</td><td><combobox width=30 var=\"summon\" list=\"Sim;Não\"></td></tr>");
        sb.append("<tr><td>Auto Loot:</td><td><combobox width=30 var=\"loot\" list=\"Sim;Não\"></td></tr>");
        sb.append("<tr><td>HP Mínimo (%):</td><td><edit var=\"hp\" width=30 height=12></td></tr>");
        sb.append("<tr><td>MP Mínimo (%):</td><td><edit var=\"mp\" width=30 height=12></td></tr>");
        sb.append("</table><br>");
        
        // Configurações de party/guild
        sb.append("<table width=270 border=0>");
        sb.append("<tr><td>Sincronizar com Party:</td><td><combobox width=30 var=\"party\" list=\"Sim;Não\"></td></tr>");
        sb.append("<tr><td>Relatórios para Guild:</td><td><combobox width=30 var=\"guild\" list=\"Sim;Não\"></td></tr>");
        sb.append("</table><br>");
        
        sb.append("<center>");
        sb.append("<button value=\"Salvar\" action=\"bypass -h voice .farmsavesettings $dist $summon $loot $hp $mp $party $guild\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        sb.append("<button value=\"Voltar\" action=\"bypass -h voice .autofarm\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
        sb.append("</center>");
        
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }
    
    /**
     * Mostra o menu de relatórios
     */
    public static void showReportsMenu(L2PcInstance player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html><body>");
        sb.append("<center><font color=\"LEVEL\">Relatórios de Farm</font></center><br>");
        
        // Sessão atual
        AutoFarmReport.FarmSession session = AutoFarmReport.getActiveSession(player);
        if (session != null) {
            sb.append(AutoFarmReport.generateHtmlReport(session));
        }
        
        sb.append("<center>");
        sb.append("<button value=\"Atualizar\" action=\"bypass -h voice .farmreports\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        if (session != null && player.getClan() != null) {
            sb.append("<button value=\"Enviar para Guild\" action=\"bypass -h voice .farmreportguild\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
        }
        sb.append("<button value=\"Voltar\" action=\"bypass -h voice .autofarm\" width=200 height=31 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
        sb.append("</center>");
        
        sb.append("</body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }
} 