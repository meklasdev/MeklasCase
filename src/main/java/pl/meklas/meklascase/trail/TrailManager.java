package pl.meklas.meklascase.trail;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.meklas.meklascase.MeklasCasePlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TrailManager {
    
    private final MeklasCasePlugin plugin;
    private final Map<UUID, Trail> trails;
    private final Map<UUID, List<Trail>> playerTrails;
    
    public TrailManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.trails = new ConcurrentHashMap<>();
        this.playerTrails = new ConcurrentHashMap<>();
        loadTrails();
    }
    
    // CRUD Operations
    public Trail createTrail(String name, TrailType type, Player owner) {
        Trail trail = new Trail(name, type, owner.getUniqueId());
        trails.put(trail.getId(), trail);
        
        playerTrails.computeIfAbsent(owner.getUniqueId(), k -> new ArrayList<>()).add(trail);
        
        saveTrails();
        return trail;
    }
    
    public Trail getTrail(UUID id) {
        return trails.get(id);
    }
    
    public Trail getTrailByName(String name) {
        return trails.values().stream()
                .filter(trail -> trail.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    public List<Trail> getAllTrails() {
        return new ArrayList<>(trails.values());
    }
    
    public List<Trail> getPlayerTrails(UUID playerId) {
        return playerTrails.getOrDefault(playerId, new ArrayList<>());
    }
    
    public List<Trail> getPublicTrails() {
        return trails.values().stream()
                .filter(Trail::isPublicTrail)
                .collect(Collectors.toList());
    }
    
    public List<Trail> getTrailsByType(TrailType type) {
        return trails.values().stream()
                .filter(trail -> trail.getType() == type)
                .collect(Collectors.toList());
    }
    
    public List<Trail> getTrailsByCategory(String category) {
        return trails.values().stream()
                .filter(trail -> trail.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    public List<Trail> getActiveTrails() {
        return trails.values().stream()
                .filter(Trail::isActive)
                .collect(Collectors.toList());
    }
    
    public boolean updateTrail(Trail trail) {
        if (trails.containsKey(trail.getId())) {
            trails.put(trail.getId(), trail);
            saveTrails();
            return true;
        }
        return false;
    }
    
    public boolean deleteTrail(UUID id) {
        Trail trail = trails.remove(id);
        if (trail != null) {
            // Remove from player trails
            List<Trail> playerTrailList = playerTrails.get(trail.getOwner());
            if (playerTrailList != null) {
                playerTrailList.removeIf(t -> t.getId().equals(id));
            }
            saveTrails();
            return true;
        }
        return false;
    }
    
    // Trail Management Operations
    public boolean activateTrail(UUID id) {
        Trail trail = trails.get(id);
        if (trail != null) {
            trail.setActive(true);
            saveTrails();
            return true;
        }
        return false;
    }
    
    public boolean deactivateTrail(UUID id) {
        Trail trail = trails.get(id);
        if (trail != null) {
            trail.setActive(false);
            saveTrails();
            return true;
        }
        return false;
    }
    
    public void addPointToTrail(UUID trailId, Location location) {
        Trail trail = trails.get(trailId);
        if (trail != null) {
            trail.addPoint(location);
            saveTrails();
        }
    }
    
    public void removePointFromTrail(UUID trailId, int pointIndex) {
        Trail trail = trails.get(trailId);
        if (trail != null) {
            trail.removePoint(pointIndex);
            saveTrails();
        }
    }
    
    // Search and Filter Operations
    public List<Trail> searchTrails(String query) {
        String lowerQuery = query.toLowerCase();
        return trails.values().stream()
                .filter(trail -> 
                    trail.getName().toLowerCase().contains(lowerQuery) ||
                    (trail.getDescription() != null && trail.getDescription().toLowerCase().contains(lowerQuery)) ||
                    trail.getCategory().toLowerCase().contains(lowerQuery) ||
                    trail.getType().getDisplayName().toLowerCase().contains(lowerQuery)
                )
                .collect(Collectors.toList());
    }
    
    public List<Trail> getTrailsSortedByPriority() {
        return trails.values().stream()
                .sorted(Comparator.comparingInt(Trail::getPriority).reversed())
                .collect(Collectors.toList());
    }
    
    public List<Trail> getTrailsSortedByDate(boolean ascending) {
        Comparator<Trail> comparator = Comparator.comparingLong(Trail::getCreatedAt);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return trails.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    public List<Trail> getTrailsSortedByName() {
        return trails.values().stream()
                .sorted(Comparator.comparing(Trail::getName))
                .collect(Collectors.toList());
    }
    
    // Statistics
    public int getTotalTrailCount() {
        return trails.size();
    }
    
    public int getActiveTrailCount() {
        return (int) trails.values().stream().filter(Trail::isActive).count();
    }
    
    public int getPlayerTrailCount(UUID playerId) {
        return playerTrails.getOrDefault(playerId, new ArrayList<>()).size();
    }
    
    public Map<TrailType, Long> getTrailCountByType() {
        return trails.values().stream()
                .collect(Collectors.groupingBy(Trail::getType, Collectors.counting()));
    }
    
    public Set<String> getAllCategories() {
        return trails.values().stream()
                .map(Trail::getCategory)
                .collect(Collectors.toSet());
    }
    
    // Permission checks
    public boolean canPlayerEditTrail(Player player, Trail trail) {
        return player.hasPermission("meklascase.trail.admin") || 
               player.getUniqueId().equals(trail.getOwner());
    }
    
    public boolean canPlayerViewTrail(Player player, Trail trail) {
        return trail.isPublicTrail() || 
               player.hasPermission("meklascase.trail.admin") || 
               player.getUniqueId().equals(trail.getOwner());
    }
    
    // Data persistence
    private void loadTrails() {
        // TODO: Implement loading from configuration file
        // This would load trails from a YAML or JSON file
        plugin.getLogger().info("Loading trails from storage...");
    }
    
    private void saveTrails() {
        // TODO: Implement saving to configuration file
        // This would save trails to a YAML or JSON file
        plugin.getLogger().fine("Saving trails to storage...");
    }
    
    public void reload() {
        trails.clear();
        playerTrails.clear();
        loadTrails();
    }
    
    public void shutdown() {
        saveTrails();
        trails.clear();
        playerTrails.clear();
    }
}