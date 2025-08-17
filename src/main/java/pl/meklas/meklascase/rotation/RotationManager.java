package pl.meklas.meklascase.rotation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.RotationProfile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotationManager {
    
    private final MeklasCasePlugin plugin;
    private final Map<String, RotationState> rotationStates;
    private BukkitTask schedulerTask;
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;
    
    public RotationManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.rotationStates = new HashMap<>();
        loadRotationStates();
    }
    
    public void loadRotationStates() {
        rotationStates.clear();
        FileConfiguration rotationState = plugin.getConfigManager().getRotationState();
        
        if (rotationState.getConfigurationSection("cases") == null) {
            return;
        }
        
        for (String caseName : rotationState.getConfigurationSection("cases").getKeys(false)) {
            String path = "cases." + caseName;
            
            String lastRotationStr = rotationState.getString(path + ".lastRotationAt");
            String activeProfile = rotationState.getString(path + ".activeProfile");
            String lastTopDrop = rotationState.getString(path + ".lastTopDrop", "Brak");
            
            Instant lastRotation = null;
            if (lastRotationStr != null) {
                try {
                    lastRotation = Instant.parse(lastRotationStr);
                } catch (Exception e) {
                    plugin.getLogger().warning("Nieprawidłowy format daty rotacji dla " + caseName + ": " + lastRotationStr);
                }
            }
            
            RotationState state = new RotationState(caseName, lastRotation, activeProfile, lastTopDrop);
            rotationStates.put(caseName, state);
        }
        
        plugin.getLogger().info("§a[RotationManager] Załadowano stany rotacji dla " + rotationStates.size() + " skrzynek");
    }
    
    public void saveRotationStates() {
        FileConfiguration rotationState = plugin.getConfigManager().getRotationState();
        
        for (RotationState state : rotationStates.values()) {
            String path = "cases." + state.getCaseName();
            
            if (state.getLastRotationAt() != null) {
                rotationState.set(path + ".lastRotationAt", state.getLastRotationAt().toString());
            }
            
            rotationState.set(path + ".activeProfile", state.getActiveProfile());
            rotationState.set(path + ".lastTopDrop", state.getLastTopDrop());
        }
        
        plugin.getConfigManager().saveRotationState();
    }
    
    public void startScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
        
        // Run every minute to check for rotations
        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAndPerformRotations();
            }
        }.runTaskTimer(plugin, 0L, 20L * 60L); // Every minute
        
        plugin.getLogger().info("§a[RotationManager] Scheduler rotacji uruchomiony");
    }
    
    public void stopScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
            schedulerTask = null;
        }
    }
    
    private void checkAndPerformRotations() {
        for (String caseName : plugin.getCaseManager().getCaseNames()) {
            if (shouldRotate(caseName)) {
                performRotation(caseName);
            }
        }
    }
    
    public boolean shouldRotate(String caseName) {
        RotationState state = rotationStates.get(caseName);
        if (state == null) {
            // Create initial state
            state = new RotationState(caseName, null, null, "Brak");
            rotationStates.put(caseName, state);
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        boolean resetAtFixedTime = config.getBoolean("resetAtFixedTime", true);
        
        if (resetAtFixedTime) {
            return shouldRotateAtFixedTime(state);
        } else {
            return shouldRotateByWindow(state);
        }
    }
    
    private boolean shouldRotateAtFixedTime(RotationState state) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String fixedTimeStr = config.getString("fixedTime", "04:00");
        
        LocalTime fixedTime;
        try {
            fixedTime = LocalTime.parse(fixedTimeStr);
        } catch (Exception e) {
            plugin.getLogger().warning("Nieprawidłowy format czasu: " + fixedTimeStr + ", używam 04:00");
            fixedTime = LocalTime.of(4, 0);
        }
        
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime todayRotation = now.toLocalDate().atTime(fixedTime).atZone(ZoneOffset.UTC);
        
        // If it's past the rotation time today, check if we already rotated today
        if (now.isAfter(todayRotation)) {
            if (state.getLastRotationAt() == null) {
                return true; // Never rotated
            }
            
            ZonedDateTime lastRotation = state.getLastRotationAt().atZone(ZoneOffset.UTC);
            return lastRotation.isBefore(todayRotation);
        }
        
        return false;
    }
    
    private boolean shouldRotateByWindow(RotationState state) {
        if (state.getLastRotationAt() == null) {
            return true; // Never rotated
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        int windowHours = config.getInt("windowHours", 24);
        
        Instant nextRotation = state.getLastRotationAt().plus(Duration.ofHours(windowHours));
        return Instant.now().isAfter(nextRotation);
    }
    
    public void performRotation(String caseName) {
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) return;
        
        RotationState state = rotationStates.get(caseName);
        if (state == null) {
            state = new RotationState(caseName, null, null, "Brak");
            rotationStates.put(caseName, state);
        }
        
        // Get next profile
        String nextProfile = getNextProfile(caseObj, state.getActiveProfile());
        
        // Update state
        state.setLastRotationAt(Instant.now());
        state.setActiveProfile(nextProfile);
        
        // Save state
        saveRotationStates();
        
        // Update hologram
        plugin.getHologramManager().updateHologram(caseName);
        
        // Announce rotation
        RotationProfile profile = caseObj.getRotationProfile(nextProfile);
        if (profile != null) {
            String description = profile.getFormattedDescription();
            plugin.getMessageUtils().announceRotation(caseName, description);
        }
        
        plugin.getLogger().info("§a[RotationManager] Wykonano rotację dla " + caseName + " -> " + nextProfile);
    }
    
    private String getNextProfile(Case caseObj, String currentProfile) {
        List<String> profileNames = caseObj.getRotationProfiles().keySet().stream().toList();
        
        if (profileNames.isEmpty()) {
            return null;
        }
        
        if (currentProfile == null) {
            return profileNames.get(0);
        }
        
        int currentIndex = profileNames.indexOf(currentProfile);
        if (currentIndex == -1) {
            return profileNames.get(0);
        }
        
        // Next profile (cycle back to first if at end)
        int nextIndex = (currentIndex + 1) % profileNames.size();
        return profileNames.get(nextIndex);
    }
    
    public void forceRotation(String caseName) {
        performRotation(caseName);
    }
    
    public long getTimeUntilNextRotation(String caseName) {
        RotationState state = rotationStates.get(caseName);
        if (state == null) {
            return 0;
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        boolean resetAtFixedTime = config.getBoolean("resetAtFixedTime", true);
        
        if (resetAtFixedTime) {
            return getTimeUntilFixedTime();
        } else {
            return getTimeUntilWindow(state);
        }
    }
    
    private long getTimeUntilFixedTime() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String fixedTimeStr = config.getString("fixedTime", "04:00");
        
        LocalTime fixedTime;
        try {
            fixedTime = LocalTime.parse(fixedTimeStr);
        } catch (Exception e) {
            fixedTime = LocalTime.of(4, 0);
        }
        
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime nextRotation = now.toLocalDate().atTime(fixedTime).atZone(ZoneOffset.UTC);
        
        // If it's past today's rotation time, schedule for tomorrow
        if (now.isAfter(nextRotation)) {
            nextRotation = nextRotation.plusDays(1);
        }
        
        return Duration.between(now, nextRotation).toMillis();
    }
    
    private long getTimeUntilWindow(RotationState state) {
        if (state.getLastRotationAt() == null) {
            return 0;
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig();
        int windowHours = config.getInt("windowHours", 24);
        
        Instant nextRotation = state.getLastRotationAt().plus(Duration.ofHours(windowHours));
        return Duration.between(Instant.now(), nextRotation).toMillis();
    }
    
    public void setTopDrop(String caseName, String topDrop) {
        RotationState state = rotationStates.get(caseName);
        if (state == null) {
            state = new RotationState(caseName, null, null, topDrop);
            rotationStates.put(caseName, state);
        } else {
            state.setLastTopDrop(topDrop);
        }
        
        saveRotationStates();
        plugin.getHologramManager().updateHologram(caseName);
    }
    
    public RotationState getRotationState(String caseName) {
        return rotationStates.get(caseName);
    }
    
    public RotationProfile getActiveProfile(String caseName) {
        RotationState state = rotationStates.get(caseName);
        if (state == null || state.getActiveProfile() == null) {
            return null;
        }
        
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) return null;
        
        return caseObj.getRotationProfile(state.getActiveProfile());
    }
    
    public static class RotationState {
        private final String caseName;
        private Instant lastRotationAt;
        private String activeProfile;
        private String lastTopDrop;
        
        public RotationState(String caseName, Instant lastRotationAt, String activeProfile, String lastTopDrop) {
            this.caseName = caseName;
            this.lastRotationAt = lastRotationAt;
            this.activeProfile = activeProfile;
            this.lastTopDrop = lastTopDrop != null ? lastTopDrop : "Brak";
        }
        
        // Getters and setters
        public String getCaseName() {
            return caseName;
        }
        
        public Instant getLastRotationAt() {
            return lastRotationAt;
        }
        
        public void setLastRotationAt(Instant lastRotationAt) {
            this.lastRotationAt = lastRotationAt;
        }
        
        public String getActiveProfile() {
            return activeProfile;
        }
        
        public void setActiveProfile(String activeProfile) {
            this.activeProfile = activeProfile;
        }
        
        public String getLastTopDrop() {
            return lastTopDrop;
        }
        
        public void setLastTopDrop(String lastTopDrop) {
            this.lastTopDrop = lastTopDrop;
        }
    }
}