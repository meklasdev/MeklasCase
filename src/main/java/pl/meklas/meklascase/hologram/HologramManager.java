package pl.meklas.meklascase.hologram;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.RotationProfile;
import pl.meklas.meklascase.rotation.RotationManager;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    
    private final MeklasCasePlugin plugin;
    private final Map<String, Object> holograms; // Store hologram objects
    private boolean fHoloAvailable = false;
    
    public HologramManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        
        // Check if fHolo is available
        checkFHoloAvailability();
    }
    
    private void checkFHoloAvailability() {
        try {
            Class.forName("me.filoghost.holographicdisplays.api.HolographicDisplaysAPI");
            fHoloAvailable = true;
            plugin.getLogger().info("§a[HologramManager] HolographicDisplays znalezione! Hologramy włączone.");
        } catch (ClassNotFoundException e) {
            fHoloAvailable = false;
            plugin.getLogger().info("§e[HologramManager] HolographicDisplays nie znalezione. Hologramy wyłączone.");
        }
    }
    
    public void initializeHolograms() {
        if (!fHoloAvailable) return;
        
        // Remove existing holograms first
        removeAllHolograms();
        
        // Create holograms for all cases with locations
        for (Case caseObj : plugin.getCaseManager().getCases().values()) {
            if (caseObj.getLocation() != null && isHologramEnabled(caseObj.getName())) {
                createHologram(caseObj.getName());
            }
        }
        
        plugin.getLogger().info("§a[HologramManager] Zainicjalizowano " + holograms.size() + " hologramów");
    }
    
    public void createHologram(String caseName) {
        if (!fHoloAvailable) return;
        
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null || caseObj.getLocation() == null) return;
        
        // Remove existing hologram if exists
        removeHologram(caseName);
        
        try {
            // Create hologram using HolographicDisplays API
            Location hologramLocation = caseObj.getLocation().clone().add(0.5, 2.5, 0.5);
            
            // This is a placeholder - actual implementation would depend on the specific fHolo version
            // For now, we'll create a simple text-based hologram representation
            List<String> lines = generateHologramLines(caseName);
            
            // Store hologram reference (placeholder implementation)
            HologramData hologramData = new HologramData(caseName, hologramLocation, lines);
            holograms.put(caseName, hologramData);
            
            plugin.getLogger().info("§a[HologramManager] Utworzono hologram dla: " + caseName);
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[HologramManager] Błąd podczas tworzenia hologramu dla " + caseName + ": " + e.getMessage());
        }
    }
    
    public void removeHologram(String caseName) {
        if (!fHoloAvailable) return;
        
        Object hologram = holograms.remove(caseName);
        if (hologram != null) {
            try {
                // Remove hologram using API (placeholder)
                plugin.getLogger().info("§e[HologramManager] Usunięto hologram dla: " + caseName);
            } catch (Exception e) {
                plugin.getLogger().severe("§c[HologramManager] Błąd podczas usuwania hologramu dla " + caseName + ": " + e.getMessage());
            }
        }
    }
    
    public void updateHologram(String caseName) {
        if (!fHoloAvailable) return;
        
        Object hologram = holograms.get(caseName);
        if (hologram == null) {
            // Create hologram if it doesn't exist
            createHologram(caseName);
            return;
        }
        
        try {
            // Update hologram lines
            List<String> newLines = generateHologramLines(caseName);
            
            // Update the hologram with new lines (placeholder)
            if (hologram instanceof HologramData) {
                ((HologramData) hologram).setLines(newLines);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[HologramManager] Błąd podczas aktualizacji hologramu dla " + caseName + ": " + e.getMessage());
        }
    }
    
    public void removeAllHolograms() {
        if (!fHoloAvailable) return;
        
        for (String caseName : new ArrayList<>(holograms.keySet())) {
            removeHologram(caseName);
        }
        
        holograms.clear();
    }
    
    private List<String> generateHologramLines(String caseName) {
        List<String> lines = new ArrayList<>();
        
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) return lines;
        
        // Line 1: Case name with gradient
        lines.add(MessageUtils.PRIMARY_GRADIENT + "✨ " + caseName + " ✨</gradient>");
        
        // Line 2: Case type
        lines.add(MessageUtils.SECONDARY_GRADIENT + "📦 " + caseObj.getType().name() + "</gradient>");
        
        // Line 3: Empty line for spacing
        lines.add("");
        
        // Line 4: Current boost/rotation info
        RotationProfile activeProfile = plugin.getRotationManager().getActiveProfile(caseName);
        if (activeProfile != null) {
            String boostInfo = activeProfile.getFormattedDescription();
            if (activeProfile.hasBoosts()) {
                lines.add(MessageUtils.WARNING_GRADIENT + "🔥 " + boostInfo + "</gradient>");
            } else if (activeProfile.hasOverrideItems()) {
                lines.add(MessageUtils.GOLD_GRADIENT + "⭐ " + boostInfo + "</gradient>");
            } else {
                lines.add(MessageUtils.SECONDARY_GRADIENT + "📋 " + boostInfo + "</gradient>");
            }
        } else {
            lines.add(MessageUtils.SECONDARY_GRADIENT + "📋 Standardowy profil</gradient>");
        }
        
        // Line 5: Time until next rotation
        long timeLeft = plugin.getRotationManager().getTimeUntilNextRotation(caseName);
        if (timeLeft > 0) {
            String timeStr = plugin.getMessageUtils().formatTimeRemaining(timeLeft);
            lines.add(MessageUtils.GOLD_GRADIENT + "⏰ Następna rotacja: " + timeStr + "</gradient>");
        } else {
            lines.add(MessageUtils.GOLD_GRADIENT + "⏰ Rotacja wkrótce...</gradient>");
        }
        
        // Line 6: Empty line
        lines.add("");
        
        // Line 7: Last top drop
        RotationManager.RotationState state = plugin.getRotationManager().getRotationState(caseName);
        if (state != null && !state.getLastTopDrop().equals("Brak")) {
            lines.add(MessageUtils.ERROR_GRADIENT + "💎 Ostatni TOP: " + state.getLastTopDrop() + "</gradient>");
        } else {
            lines.add(MessageUtils.SECONDARY_GRADIENT + "💎 Brak TOP DROP</gradient>");
        }
        
        // Line 8: Usage instruction
        lines.add("");
        lines.add(MessageUtils.SUCCESS_GRADIENT + "👆 Kliknij prawym przyciskiem myszy!</gradient>");
        lines.add(MessageUtils.SUCCESS_GRADIENT + "🔑 Potrzebujesz klucza</gradient>");
        
        return lines;
    }
    
    private boolean isHologramEnabled(String caseName) {
        FileConfiguration locations = plugin.getConfigManager().getLocations();
        return locations.getBoolean("cases." + caseName + ".hologram.enabled", true);
    }
    
    public void setHologramEnabled(String caseName, boolean enabled) {
        FileConfiguration locations = plugin.getConfigManager().getLocations();
        locations.set("cases." + caseName + ".hologram.enabled", enabled);
        plugin.getConfigManager().saveLocations();
        
        if (enabled) {
            createHologram(caseName);
        } else {
            removeHologram(caseName);
        }
    }
    
    public boolean isFHoloAvailable() {
        return fHoloAvailable;
    }
    
    public boolean hasHologram(String caseName) {
        return holograms.containsKey(caseName);
    }
    
    // Placeholder class for hologram data (would be replaced with actual fHolo objects)
    private static class HologramData {
        private final String caseName;
        private final Location location;
        private List<String> lines;
        
        public HologramData(String caseName, Location location, List<String> lines) {
            this.caseName = caseName;
            this.location = location;
            this.lines = new ArrayList<>(lines);
        }
        
        public void setLines(List<String> lines) {
            this.lines = new ArrayList<>(lines);
        }
        
        public String getCaseName() {
            return caseName;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public List<String> getLines() {
            return lines;
        }
    }
}