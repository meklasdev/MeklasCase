package pl.meklas.meklascase.hologram;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
    private final Map<String, HologramAnimation> animations; // Store animation data
    private boolean fHoloAvailable = false;
    private BukkitTask animationTask;
    
    public HologramManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
        this.animations = new HashMap<>();
        
        // Check if fHolo is available
        checkFHoloAvailability();
    }
    
    private void checkFHoloAvailability() {
        try {
            Class.forName("me.filoghost.holographicdisplays.api.HolographicDisplaysAPI");
            fHoloAvailable = true;
            plugin.getLogger().info("§a[HologramManager] HolographicDisplays znalezione! Animowane hologramy włączone.");
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
        
        // Start animation task
        startAnimations();
        
        plugin.getLogger().info("§a[HologramManager] Zainicjalizowano " + holograms.size() + " animowanych hologramów");
    }
    
    private void startAnimations() {
        if (animationTask != null) {
            animationTask.cancel();
        }
        
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAnimations();
            }
        }.runTaskTimer(plugin, 0L, getAnimationUpdateInterval()); // Configurable update interval
    }
    
    private void updateAnimations() {
        for (String caseName : new ArrayList<>(animations.keySet())) {
            HologramAnimation animation = animations.get(caseName);
            if (animation != null) {
                animation.tick();
                updateHologramLines(caseName, animation);
            }
        }
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
            
            // Create animation data
            HologramAnimation animation = new HologramAnimation(caseName);
            animations.put(caseName, animation);
            
            // Generate initial lines
            List<String> lines = generateAnimatedHologramLines(caseName, animation);
            
            // Store hologram reference (placeholder implementation)
            HologramData hologramData = new HologramData(caseName, hologramLocation, lines);
            holograms.put(caseName, hologramData);
            
            plugin.getLogger().info("§a[HologramManager] Utworzono animowany hologram dla: " + caseName);
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[HologramManager] Błąd podczas tworzenia hologramu dla " + caseName + ": " + e.getMessage());
        }
    }
    
    public void removeHologram(String caseName) {
        if (!fHoloAvailable) return;
        
        Object hologram = holograms.remove(caseName);
        animations.remove(caseName);
        
        if (hologram != null) {
            try {
                // Remove hologram using API (placeholder)
                plugin.getLogger().info("§e[HologramManager] Usunięto animowany hologram dla: " + caseName);
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
        
        // Animation will handle updates automatically
    }
    
    private void updateHologramLines(String caseName, HologramAnimation animation) {
        Object hologram = holograms.get(caseName);
        if (hologram == null) return;
        
        try {
            // Update hologram lines with animation
            List<String> newLines = generateAnimatedHologramLines(caseName, animation);
            
            // Update the hologram with new lines (placeholder)
            if (hologram instanceof HologramData) {
                ((HologramData) hologram).setLines(newLines);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[HologramManager] Błąd podczas aktualizacji animacji hologramu dla " + caseName + ": " + e.getMessage());
        }
    }
    
    public void removeAllHolograms() {
        if (!fHoloAvailable) return;
        
        // Stop animations
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        
        for (String caseName : new ArrayList<>(holograms.keySet())) {
            removeHologram(caseName);
        }
        
        holograms.clear();
        animations.clear();
    }
    
    private List<String> generateAnimatedHologramLines(String caseName, HologramAnimation animation) {
        List<String> lines = new ArrayList<>();
        
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) return lines;
        
        // Line 1: Animated case name with advanced effects
        String nameColor = animation.getPulsingColor();
        String sparkles = animation.getSparkleEffect();
        String displayName = isEffectEnabled("rainbow") ? 
            HologramEffects.createRainbowText(caseName, animation.tick / 2) : caseName;
        lines.add(nameColor + sparkles + " " + displayName + " " + sparkles + "</gradient>");
        
        // Line 2: Case type with rotating icon
        String rotatingIcon = animation.getRotatingIcon();
        lines.add(MessageUtils.SECONDARY_GRADIENT + rotatingIcon + " " + caseObj.getType().name() + "</gradient>");
        
        // Line 3: Empty line for spacing
        lines.add("");
        
        // Line 4: Current boost/rotation info with advanced animated effects
        RotationProfile activeProfile = plugin.getRotationManager().getActiveProfile(caseName);
        if (activeProfile != null) {
            String boostInfo = activeProfile.getFormattedDescription();
            
            if (activeProfile.hasBoosts()) {
                // Special fire effect for boosts
                String fireEffect = HologramEffects.createFireEffect(3, animation.tick);
                String neonGlow = HologramEffects.createNeonGlow("BOOST AKTYWNY", animation.tick / 2);
                lines.add(fireEffect);
                lines.add(neonGlow);
                lines.add(MessageUtils.WARNING_GRADIENT + boostInfo + "</gradient>");
            } else if (activeProfile.hasOverrideItems()) {
                // Glitch effect for override items
                String glitchEffect = HologramEffects.createGlitchEffect("SPECJALNE PRZEDMIOTY", animation.tick);
                String starEffect = animation.getStarEffect();
                lines.add(glitchEffect);
                lines.add(MessageUtils.GOLD_GRADIENT + starEffect + " " + boostInfo + "</gradient>");
            } else {
                // Breathing effect for normal profile
                String breathingText = HologramEffects.createBreathingEffect(boostInfo, animation.tick);
                lines.add(breathingText);
            }
        } else {
            lines.add(MessageUtils.SECONDARY_GRADIENT + "📋 Standardowy profil</gradient>");
        }
        
        // Line 5: Animated time countdown with progress bar
        long timeLeft = plugin.getRotationManager().getTimeUntilNextRotation(caseName);
        if (timeLeft > 0) {
            String timeStr = plugin.getMessageUtils().formatTimeRemaining(timeLeft);
            String clockIcon = animation.getClockAnimation();
            String progressBar = animation.getTimeProgressBar(timeLeft);
            
            lines.add(MessageUtils.GOLD_GRADIENT + clockIcon + " Rotacja za: " + timeStr + "</gradient>");
            lines.add(progressBar);
        } else {
            // Ultra urgent rotation warning with special effects
            String digitalRain = HologramEffects.createDigitalRain(20, animation.tick);
            String spinner = HologramEffects.createSpinner(animation.tick);
            String urgentEffect = animation.getUrgentEffect();
            String glitchText = HologramEffects.createGlitchEffect("ROTACJA WKRÓTCE", animation.tick);
            
            lines.add(digitalRain);
            lines.add(MessageUtils.ERROR_GRADIENT + urgentEffect + " " + spinner + " " + urgentEffect + "</gradient>");
            lines.add(glitchText);
        }
        
        // Line 6: Empty line
        lines.add("");
        
        // Line 7: Animated TOP DROP with advanced diamond effects
        RotationManager.RotationState state = plugin.getRotationManager().getRotationState(caseName);
        if (state != null && !state.getLastTopDrop().equals("Brak")) {
            // Epic TOP DROP effects
            String pulsingBorder = HologramEffects.createPulsingBorder(25, animation.tick / 2);
            String rainbowTopDrop = HologramEffects.createRainbowText("TOP DROP", animation.tick);
            String waveAnimation = HologramEffects.createWaveAnimation(state.getLastTopDrop(), animation.tick);
            String diamondEffect = animation.getDiamondEffect();
            
            lines.add(pulsingBorder);
            lines.add(MessageUtils.ERROR_GRADIENT + diamondEffect + " " + rainbowTopDrop + " " + diamondEffect + "</gradient>");
            lines.add(waveAnimation);
            lines.add(pulsingBorder);
        } else {
            // Constellation effect for no TOP DROP
            String constellation = HologramEffects.createConstellation(20, animation.tick);
            lines.add(constellation);
            lines.add(MessageUtils.SECONDARY_GRADIENT + "💎 Czekamy na TOP DROP...</gradient>");
        }
        
        // Line 8: Animated usage instructions
        lines.add("");
        String handIcon = animation.getHandAnimation();
        String keyIcon = animation.getKeyAnimation();
        
        lines.add(MessageUtils.SUCCESS_GRADIENT + handIcon + " Kliknij prawym przyciskiem!</gradient>");
        lines.add(MessageUtils.SUCCESS_GRADIENT + keyIcon + " Potrzebujesz klucza</gradient>");
        
        // Line 9: Advanced visual effects at bottom
        String particleField = HologramEffects.createParticleField(20, 15, animation.tick);
        String constellation = HologramEffects.createConstellation(15, animation.tick);
        String geometricPattern = HologramEffects.createGeometricPattern(animation.tick / 3);
        
        lines.add(particleField);
        lines.add(constellation);
        lines.add(geometricPattern);
        
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
    
    private boolean areAnimationsEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("holograms.animations.enabled", true);
    }
    
    private long getAnimationUpdateInterval() {
        return plugin.getConfigManager().getConfig().getLong("holograms.animations.updateInterval", 10L);
    }
    
    private boolean isEffectEnabled(String effectName) {
        return plugin.getConfigManager().getConfig().getBoolean("holograms.animations.effects." + effectName, true);
    }
    
    // Animation class for hologram effects
    private static class HologramAnimation {
        private final String caseName;
        private int tick = 0;
        private int pulsePhase = 0;
        private int rotationPhase = 0;
        private int sparklePhase = 0;
        private int firePhase = 0;
        private int floatPhase = 0;
        
        // Animation sequences
        private final String[] SPARKLES = {"✨", "⭐", "🌟", "💫", "✨"};
        private final String[] ROTATING_ICONS = {"📦", "📋", "📊", "📈", "📦"};
        private final String[] FIRE_EFFECTS = {"🔥", "🔥🔥", "🔥🔥🔥", "🔥🔥", "🔥"};
        private final String[] STAR_EFFECTS = {"⭐", "🌟", "✨", "💫", "⭐"};
        private final String[] CLOCK_ANIMATIONS = {"🕐", "🕑", "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘", "🕙", "🕚", "🕛"};
        private final String[] DIAMOND_EFFECTS = {"💎", "💠", "🔷", "🔹", "💎"};
        private final String[] HAND_ANIMATIONS = {"👆", "☝️", "👉", "👆"};
        private final String[] KEY_ANIMATIONS = {"🔑", "🗝️", "🔐", "🔑"};
        private final String[] FLOATING_EFFECTS = {"∘∘∘∘∘∘", "∘∘∘∘∘∘", "∘∘∘∘∘∘", " ∘∘∘∘∘", "  ∘∘∘∘", "   ∘∘∘", "    ∘∘", "     ∘", "      ", "     ∘", "    ∘∘", "   ∘∘∘", "  ∘∘∘∘", " ∘∘∘∘∘", "∘∘∘∘∘∘"};
        
        // Pulse colors for case name
        private final String[] PULSE_COLORS = {
            "<gradient:#00ff87:#60efff>",  // Primary
            "<gradient:#ff6b6b:#feca57>",  // Warm
            "<gradient:#48cae4:#023e8a>",  // Cool
            "<gradient:#f72585:#b5179e>",  // Purple
            "<gradient:#00ff87:#60efff>"   // Back to primary
        };
        
        public HologramAnimation(String caseName) {
            this.caseName = caseName;
        }
        
        public void tick() {
            tick++;
            
            // Update different animation phases at different speeds
            if (tick % 10 == 0) pulsePhase = (pulsePhase + 1) % PULSE_COLORS.length;
            if (tick % 8 == 0) rotationPhase = (rotationPhase + 1) % ROTATING_ICONS.length;
            if (tick % 6 == 0) sparklePhase = (sparklePhase + 1) % SPARKLES.length;
            if (tick % 12 == 0) firePhase = (firePhase + 1) % FIRE_EFFECTS.length;
            if (tick % 4 == 0) floatPhase = (floatPhase + 1) % FLOATING_EFFECTS.length;
        }
        
        public String getPulsingColor() {
            return PULSE_COLORS[pulsePhase];
        }
        
        public String getSparkleEffect() {
            return SPARKLES[sparklePhase];
        }
        
        public String getRotatingIcon() {
            return ROTATING_ICONS[rotationPhase];
        }
        
        public String getFireEffect() {
            return FIRE_EFFECTS[firePhase];
        }
        
        public String getStarEffect() {
            return STAR_EFFECTS[sparklePhase % STAR_EFFECTS.length];
        }
        
        public String getClockAnimation() {
            return CLOCK_ANIMATIONS[tick % CLOCK_ANIMATIONS.length];
        }
        
        public String getDiamondEffect() {
            return DIAMOND_EFFECTS[sparklePhase % DIAMOND_EFFECTS.length];
        }
        
        public String getHandAnimation() {
            return HAND_ANIMATIONS[tick % HAND_ANIMATIONS.length];
        }
        
        public String getKeyAnimation() {
            return KEY_ANIMATIONS[(tick / 2) % KEY_ANIMATIONS.length];
        }
        
        public String getFloatingEffect() {
            return MessageUtils.SECONDARY_GRADIENT + FLOATING_EFFECTS[floatPhase] + "</gradient>";
        }
        
        public String getUrgentEffect() {
            // Fast blinking effect for urgent messages
            return tick % 4 < 2 ? "⚠️" : "🔴";
        }
        
        public String getTimeProgressBar(long timeLeft) {
            // Create animated progress bar for time countdown
            long totalTime = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
            double progress = Math.max(0, Math.min(1, (double) timeLeft / totalTime));
            
            int barLength = 20;
            int filled = (int) (progress * barLength);
            int empty = barLength - filled;
            
            StringBuilder bar = new StringBuilder();
            
            // Animated colors based on time left
            String barColor;
            if (progress > 0.5) {
                barColor = MessageUtils.SUCCESS_GRADIENT;
            } else if (progress > 0.25) {
                barColor = MessageUtils.WARNING_GRADIENT;
            } else {
                barColor = MessageUtils.ERROR_GRADIENT;
            }
            
            bar.append(barColor);
            
            // Filled part with animation
            String fillChar = tick % 6 < 3 ? "█" : "▓";
            for (int i = 0; i < filled; i++) {
                bar.append(fillChar);
            }
            
            // Empty part
            bar.append("<gray>");
            for (int i = 0; i < empty; i++) {
                bar.append("░");
            }
            
            bar.append("</gray></gradient>");
            
            // Add percentage
            int percentage = (int) (progress * 100);
            bar.append(" ").append(MessageUtils.GOLD_GRADIENT).append(percentage).append("%</gradient>");
            
            return bar.toString();
        }
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