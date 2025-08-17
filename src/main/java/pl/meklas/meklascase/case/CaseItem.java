package pl.meklas.meklascase.case;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class CaseItem {
    
    private final Material material;
    private final int amount;
    private final double weight;
    private final String displayName;
    private final List<String> lore;
    private final boolean glow;
    private final Map<Enchantment, Integer> enchantments;
    
    public CaseItem(Material material, int amount, double weight, String displayName, 
                   List<String> lore, boolean glow, Map<Enchantment, Integer> enchantments) {
        this.material = material;
        this.amount = amount;
        this.weight = weight;
        this.displayName = displayName;
        this.lore = lore;
        this.glow = glow;
        this.enchantments = enchantments;
    }
    
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName.replace("&", "§"));
            }
            
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = lore.stream()
                    .map(line -> line.replace("&", "§"))
                    .toList();
                meta.setLore(coloredLore);
            }
            
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
            }
            
            if (glow && (enchantments == null || enchantments.isEmpty())) {
                // Add fake enchantment for glow effect
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public String getDisplayName() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName.replace("&", "§");
        }
        return material.name().replace("_", " ").toLowerCase();
    }
    
    // Getters
    public Material getMaterial() {
        return material;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public boolean isGlow() {
        return glow;
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    
    @Override
    public String toString() {
        return getDisplayName() + " x" + amount + " (weight: " + weight + ")";
    }
}