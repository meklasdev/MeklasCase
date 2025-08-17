package pl.meklas.meklascase.trail;

import org.bukkit.Material;
import org.bukkit.Particle;

public enum TrailType {
    
    WALKING("Szlak Pieszy", Material.LEATHER_BOOTS, Particle.FOOTSTEP, "Tradycyjny szlak dla pieszych"),
    CYCLING("Szlak Rowerowy", Material.IRON_HORSE_ARMOR, Particle.CLOUD, "Szlak przystosowany dla rowerów"),
    MOUNTAIN("Szlak Górski", Material.STONE_PICKAXE, Particle.FALLING_DUST, "Trudny szlak górski"),
    NATURE("Szlak Przyrodniczy", Material.OAK_LEAVES, Particle.HAPPY_VILLAGER, "Szlak edukacyjny przyrodniczy"),
    HISTORICAL("Szlak Historyczny", Material.BOOK, Particle.ENCHANTMENT_TABLE, "Szlak z walorami historycznymi"),
    ADVENTURE("Szlak Przygodowy", Material.COMPASS, Particle.PORTAL, "Szlak z elementami przygodowymi"),
    RACING("Szlak Wyścigowy", Material.GOLDEN_BOOTS, Particle.FLAME, "Szlak do zawodów i wyścigów"),
    SCENIC("Szlak Widokowy", Material.SPYGLASS, Particle.END_ROD, "Szlak z pięknymi widokami"),
    CUSTOM("Niestandardowy", Material.BARRIER, Particle.REDSTONE, "Niestandardowy typ szlaku");
    
    private final String displayName;
    private final Material icon;
    private final Particle defaultParticle;
    private final String description;
    
    TrailType(String displayName, Material icon, Particle defaultParticle, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.defaultParticle = defaultParticle;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public Particle getDefaultParticle() {
        return defaultParticle;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TrailType fromString(String name) {
        for (TrailType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return CUSTOM;
    }
}