import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.premium.game.model.actor.L2Character;
import com.premium.game.model.actor.instance.L2PcInstance;

/**
 * Sistema de cache para alvos do AutoFarm.
 * Implementa um cache com expiração para reduzir buscas repetitivas.
 */
public class AutoFarmTargetCache {
    private static final Logger _log = Logger.getLogger(AutoFarmTargetCache.class);
    
    private static class CachedTargets {
        final List<L2Character> targets;
        final long expirationTime;
        
        CachedTargets(List<L2Character> targets, long expirationTime) {
            this.targets = targets;
            this.expirationTime = expirationTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
    
    // Cache por jogador
    private static final Map<Integer, CachedTargets> targetCache = new ConcurrentHashMap<>();
    
    // Cache espacial otimizado usando QuadTree
    private static class SpatialNode {
        final int x1, y1, x2, y2;
        final List<L2Character> targets;
        final Map<String, SpatialNode> children;
        
        SpatialNode(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.targets = Collections.synchronizedList(new ArrayList<>());
            this.children = new ConcurrentHashMap<>();
        }
        
        boolean contains(int x, int y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }
        
        void subdivide() {
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;
            
            children.put("nw", new SpatialNode(x1, y1, midX, midY));
            children.put("ne", new SpatialNode(midX + 1, y1, x2, midY));
            children.put("sw", new SpatialNode(x1, midY + 1, midX, y2));
            children.put("se", new SpatialNode(midX + 1, midY + 1, x2, y2));
        }
    }
    
    private static final SpatialNode worldTree = new SpatialNode(-1000000, -1000000, 1000000, 1000000);
    
    /**
     * Obtém alvos do cache ou busca novos se necessário
     * @param player Jogador
     * @param searchDistance Distância de busca
     * @return Lista de alvos encontrados
     */
    public static List<L2Character> getTargets(L2PcInstance player, int searchDistance) {
        if (!AutoFarmConfig.USE_TARGET_CACHE) {
            return searchNewTargets(player, searchDistance);
        }
        
        int playerId = player.getObjectId();
        CachedTargets cached = targetCache.get(playerId);
        
        if (cached != null && !cached.isExpired()) {
            return new ArrayList<>(cached.targets);
        }
        
        List<L2Character> newTargets = searchNewTargets(player, searchDistance);
        targetCache.put(playerId, new CachedTargets(
            newTargets,
            System.currentTimeMillis() + AutoFarmConfig.TARGET_CACHE_DURATION_MS
        ));
        
        return newTargets;
    }
    
    /**
     * Busca novos alvos usando particionamento espacial
     */
    private static List<L2Character> searchNewTargets(L2PcInstance player, int searchDistance) {
        int x = player.getX();
        int y = player.getY();
        int z = player.getZ();
        
        List<L2Character> results = new ArrayList<>();
        searchNode(worldTree, x, y, searchDistance, player, z, results);
        
        return results;
    }
    
    private static void searchNode(SpatialNode node, int x, int y, int searchDistance,
                                 L2PcInstance player, int z, List<L2Character> results) {
        if (!node.contains(x - searchDistance, y - searchDistance) &&
            !node.contains(x + searchDistance, y + searchDistance)) {
            return;
        }
        
        // Verificar alvos neste nó
        for (L2Character target : node.targets) {
            if (isValidTarget(player, target, x, y, z, searchDistance)) {
                results.add(target);
            }
        }
        
        // Recursivamente verificar sub-nós
        if (!node.children.isEmpty()) {
            for (SpatialNode child : node.children.values()) {
                searchNode(child, x, y, searchDistance, player, z, results);
            }
        }
    }
    
    /**
     * Verifica se um alvo é válido baseado na distância e outros critérios
     */
    private static boolean isValidTarget(L2PcInstance player, L2Character target,
                                      int x, int y, int z, int searchDistance) {
        if (target == null || target.isDead()) {
            return false;
        }
        
        // Verifica distância
        int dx = target.getX() - x;
        int dy = target.getY() - y;
        int dz = target.getZ() - z;
        
        return (dx * dx + dy * dy + dz * dz) <= searchDistance * searchDistance;
    }
    
    /**
     * Atualiza a posição de um alvo no cache espacial
     */
    public static void updateTargetPosition(L2Character target) {
        if (!AutoFarmConfig.USE_TARGET_CACHE) {
            return;
        }
        
        insertIntoTree(worldTree, target);
    }
    
    private static void insertIntoTree(SpatialNode node, L2Character target) {
        int x = target.getX();
        int y = target.getY();
        
        if (!node.contains(x, y)) {
            return;
        }
        
        // Se o nó tem poucos alvos, adicionar diretamente
        if (node.targets.size() < 10 && node.children.isEmpty()) {
            node.targets.add(target);
            return;
        }
        
        // Se necessário, subdividir o nó
        if (node.children.isEmpty()) {
            node.subdivide();
            // Redistribuir alvos existentes
            for (L2Character existing : node.targets) {
                insertIntoTree(node, existing);
            }
            node.targets.clear();
        }
        
        // Inserir no sub-nó apropriado
        for (SpatialNode child : node.children.values()) {
            if (child.contains(x, y)) {
                insertIntoTree(child, target);
                break;
            }
        }
    }
    
    /**
     * Limpa o cache de um jogador específico
     */
    public static void clearCache(L2PcInstance player) {
        if (player != null) {
            targetCache.remove(player.getObjectId());
        }
    }
    
    /**
     * Limpa todo o cache
     */
    public static void clearAllCache() {
        targetCache.clear();
        worldTree.targets.clear();
        worldTree.children.clear();
    }
} 