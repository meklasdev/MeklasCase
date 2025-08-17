package pl.meklas.meklascase.case;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Case {
    
    public enum CaseType {
        LOOTBOX, LUCKBLOCK
    }
    
    private final String name;
    private final CaseType type;
    private final ItemStack keyItem;
    private final List<CaseItem> items;
    private final Map<String, RotationProfile> rotationProfiles;
    private Location location;
    private boolean enabled;
    
    private static final Random random = new Random();
    
    public Case(String name, CaseType type, ItemStack keyItem, List<CaseItem> items, 
               Map<String, RotationProfile> rotationProfiles) {
        this.name = name;
        this.type = type;
        this.keyItem = keyItem;
        this.items = items != null ? items : new ArrayList<>();
        this.rotationProfiles = rotationProfiles != null ? rotationProfiles : new HashMap<>();
        this.enabled = true;
    }
    
    /**
     * Draws a random item from the case based on weights
     */
    public CaseItem drawItem() {
        return drawItem(null);
    }
    
    /**
     * Draws a random item with optional rotation profile applied
     */
    public CaseItem drawItem(RotationProfile profile) {
        List<CaseItem> itemsToUse = items;
        
        // If profile has override items, use those instead
        if (profile != null && profile.getOverrideItems() != null && !profile.getOverrideItems().isEmpty()) {
            itemsToUse = profile.getOverrideItems();
        }
        
        if (itemsToUse.isEmpty()) {
            return null;
        }
        
        // Calculate total weight with boosts
        double totalWeight = 0;
        Map<CaseItem, Double> adjustedWeights = new HashMap<>();
        
        for (CaseItem item : itemsToUse) {
            double weight = item.getWeight();
            
            // Apply boosts if profile exists
            if (profile != null) {
                Double multiplier = profile.getBoosts().get(item.getMaterial());
                if (multiplier != null) {
                    weight *= multiplier;
                }
            }
            
            adjustedWeights.put(item, weight);
            totalWeight += weight;
        }
        
        // Random selection
        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;
        
        for (Map.Entry<CaseItem, Double> entry : adjustedWeights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }
        
        // Fallback to last item (shouldn't happen)
        return itemsToUse.get(itemsToUse.size() - 1);
    }
    
    /**
     * Gets all possible items for spinning animation
     */
    public List<CaseItem> getSpinItems(RotationProfile profile, int count) {
        List<CaseItem> spinItems = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            CaseItem item = drawItem(profile);
            if (item != null) {
                spinItems.add(item);
            }
        }
        
        return spinItems;
    }
    
    /**
     * Checks if an item is a top drop (lowest weight)
     */
    public boolean isTopDrop(CaseItem item) {
        if (items.isEmpty()) return false;
        
        double minWeight = items.stream()
            .mapToDouble(CaseItem::getWeight)
            .min()
            .orElse(Double.MAX_VALUE);
            
        return item.getWeight() <= minWeight;
    }
    
    /**
     * Creates a key item for this case
     */
    public ItemStack createKeyItem() {
        return keyItem.clone();
    }
    
    /**
     * Creates a key item with specific amount
     */
    public ItemStack createKeyItem(int amount) {
        ItemStack key = keyItem.clone();
        key.setAmount(amount);
        return key;
    }
    
    /**
     * Checks if an item stack is a valid key for this case
     */
    public boolean isValidKey(ItemStack item) {
        if (item == null || keyItem == null) return false;
        
        // Check material
        if (item.getType() != keyItem.getType()) return false;
        
        // Check meta
        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta keyMeta = keyItem.getItemMeta();
        
        if (itemMeta == null && keyMeta == null) return true;
        if (itemMeta == null || keyMeta == null) return false;
        
        // Check display name
        String itemName = itemMeta.getDisplayName();
        String keyName = keyMeta.getDisplayName();
        
        if (!itemName.equals(keyName)) return false;
        
        // Check lore
        List<String> itemLore = itemMeta.getLore();
        List<String> keyLore = keyMeta.getLore();
        
        if (itemLore == null && keyLore == null) return true;
        if (itemLore == null || keyLore == null) return false;
        
        return itemLore.equals(keyLore);
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public CaseType getType() {
        return type;
    }
    
    public ItemStack getKeyItem() {
        return keyItem;
    }
    
    public List<CaseItem> getItems() {
        return items;
    }
    
    public Map<String, RotationProfile> getRotationProfiles() {
        return rotationProfiles;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public RotationProfile getRotationProfile(String profileName) {
        return rotationProfiles.get(profileName);
    }
    
    public void addRotationProfile(String name, RotationProfile profile) {
        rotationProfiles.put(name, profile);
    }
    
    public void removeRotationProfile(String name) {
        rotationProfiles.remove(name);
    }
}