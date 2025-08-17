package pl.meklas.meklascase.case;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotationProfile {
    
    private final String name;
    private final String description;
    private final Map<Material, Double> boosts;
    private final List<CaseItem> overrideItems;
    
    public RotationProfile(String name, String description, Map<Material, Double> boosts, List<CaseItem> overrideItems) {
        this.name = name;
        this.description = description;
        this.boosts = boosts != null ? boosts : new HashMap<>();
        this.overrideItems = overrideItems;
    }
    
    /**
     * Gets the boost multiplier for a specific material
     */
    public double getBoostMultiplier(Material material) {
        return boosts.getOrDefault(material, 1.0);
    }
    
    /**
     * Checks if this profile has any boosts
     */
    public boolean hasBoosts() {
        return !boosts.isEmpty();
    }
    
    /**
     * Checks if this profile overrides the default items
     */
    public boolean hasOverrideItems() {
        return overrideItems != null && !overrideItems.isEmpty();
    }
    
    /**
     * Gets the primary boosted item (highest multiplier)
     */
    public Material getPrimaryBoostedItem() {
        return boosts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Gets the highest boost multiplier
     */
    public double getHighestBoostMultiplier() {
        return boosts.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(1.0);
    }
    
    /**
     * Formats the boost description for display
     */
    public String getFormattedDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        if (hasBoosts()) {
            Material primaryItem = getPrimaryBoostedItem();
            if (primaryItem != null) {
                double multiplier = getBoostMultiplier(primaryItem);
                return String.format("Boost x%.1f na %s", multiplier, 
                    primaryItem.name().toLowerCase().replace("_", " "));
            }
        }
        
        if (hasOverrideItems()) {
            return "Specjalne przedmioty na dziś!";
        }
        
        return "Standardowy profil";
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<Material, Double> getBoosts() {
        return boosts;
    }
    
    public List<CaseItem> getOverrideItems() {
        return overrideItems;
    }
    
    @Override
    public String toString() {
        return name + ": " + getFormattedDescription();
    }
}