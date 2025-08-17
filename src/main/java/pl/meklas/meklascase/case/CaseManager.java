package pl.meklas.meklascase.case;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.meklas.meklascase.MeklasCasePlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseManager {
    
    private final MeklasCasePlugin plugin;
    private final Map<String, Case> cases;
    private final Map<Location, String> locationToCaseName;
    
    public CaseManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.cases = new HashMap<>();
        this.locationToCaseName = new HashMap<>();
        loadCases();
    }
    
    public void loadCases() {
        cases.clear();
        locationToCaseName.clear();
        
        Map<String, FileConfiguration> caseConfigs = plugin.getConfigManager().getCaseConfigs();
        
        for (Map.Entry<String, FileConfiguration> entry : caseConfigs.entrySet()) {
            String caseName = entry.getKey();
            FileConfiguration config = entry.getValue();
            
            try {
                Case loadedCase = loadCaseFromConfig(caseName, config);
                if (loadedCase != null) {
                    cases.put(caseName, loadedCase);
                    
                    // Load location if exists
                    Location location = loadCaseLocation(caseName);
                    if (location != null) {
                        loadedCase.setLocation(location);
                        locationToCaseName.put(location, caseName);
                    }
                    
                    plugin.getLogger().info("§a[CaseManager] Załadowano skrzynkę: " + caseName);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("§c[CaseManager] Błąd podczas ładowania skrzynki " + caseName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("§a[CaseManager] Załadowano " + cases.size() + " skrzynek");
    }
    
    private Case loadCaseFromConfig(String caseName, FileConfiguration config) {
        // Load type
        String typeStr = config.getString("type", "LOOTBOX");
        Case.CaseType type;
        try {
            type = Case.CaseType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Nieprawidłowy typ skrzynki dla " + caseName + ": " + typeStr + ", używam LOOTBOX");
            type = Case.CaseType.LOOTBOX;
        }
        
        // Load key item
        ItemStack keyItem = loadKeyItem(config);
        if (keyItem == null) {
            plugin.getLogger().severe("Nie można załadować klucza dla skrzynki: " + caseName);
            return null;
        }
        
        // Load items
        List<CaseItem> items = loadCaseItems(config);
        if (items.isEmpty()) {
            plugin.getLogger().warning("Skrzynka " + caseName + " nie ma żadnych przedmiotów!");
        }
        
        // Load rotation profiles
        Map<String, RotationProfile> profiles = loadRotationProfiles(config);
        
        return new Case(caseName, type, keyItem, items, profiles);
    }
    
    private ItemStack loadKeyItem(FileConfiguration config) {
        ConfigurationSection keySection = config.getConfigurationSection("key");
        if (keySection == null) {
            return null;
        }
        
        String materialName = keySection.getString("material", "TRIPWIRE_HOOK");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Nieprawidłowy materiał klucza: " + materialName);
            material = Material.TRIPWIRE_HOOK;
        }
        
        ItemStack key = new ItemStack(material);
        ItemMeta meta = key.getItemMeta();
        
        if (meta != null) {
            String name = keySection.getString("name");
            if (name != null) {
                meta.setDisplayName(name.replace("&", "§"));
            }
            
            List<String> lore = keySection.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> coloredLore = lore.stream()
                    .map(line -> line.replace("&", "§"))
                    .toList();
                meta.setLore(coloredLore);
            }
            
            boolean glow = keySection.getBoolean("glow", false);
            if (glow) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            }
            
            key.setItemMeta(meta);
        }
        
        return key;
    }
    
    private List<CaseItem> loadCaseItems(FileConfiguration config) {
        List<CaseItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        
        if (itemsSection == null) {
            return items;
        }
        
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;
            
            try {
                CaseItem item = loadCaseItem(itemSection);
                if (item != null) {
                    items.add(item);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Błąd podczas ładowania przedmiotu " + key + ": " + e.getMessage());
            }
        }
        
        return items;
    }
    
    private CaseItem loadCaseItem(ConfigurationSection section) {
        String materialName = section.getString("item");
        if (materialName == null) return null;
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Nieprawidłowy materiał: " + materialName);
            return null;
        }
        
        int amount = section.getInt("amount", 1);
        double weight = section.getDouble("weight", 1.0);
        String displayName = section.getString("name");
        List<String> lore = section.getStringList("lore");
        boolean glow = section.getBoolean("glow", false);
        
        // Load enchantments
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
        if (enchSection != null) {
            for (String enchName : enchSection.getKeys(false)) {
                try {
                    Enchantment enchantment = Enchantment.getByName(enchName.toUpperCase());
                    if (enchantment != null) {
                        int level = enchSection.getInt(enchName);
                        enchantments.put(enchantment, level);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Nieprawidłowe zaklęcie: " + enchName);
                }
            }
        }
        
        return new CaseItem(material, amount, weight, displayName, lore, glow, enchantments);
    }
    
    private Map<String, RotationProfile> loadRotationProfiles(FileConfiguration config) {
        Map<String, RotationProfile> profiles = new HashMap<>();
        ConfigurationSection rotationSection = config.getConfigurationSection("rotation.profiles");
        
        if (rotationSection == null) {
            return profiles;
        }
        
        for (String profileName : rotationSection.getKeys(false)) {
            ConfigurationSection profileSection = rotationSection.getConfigurationSection(profileName);
            if (profileSection == null) continue;
            
            try {
                RotationProfile profile = loadRotationProfile(profileName, profileSection);
                if (profile != null) {
                    profiles.put(profileName, profile);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Błąd podczas ładowania profilu rotacji " + profileName + ": " + e.getMessage());
            }
        }
        
        return profiles;
    }
    
    private RotationProfile loadRotationProfile(String name, ConfigurationSection section) {
        String description = section.getString("description", "");
        
        // Load boosts
        Map<Material, Double> boosts = new HashMap<>();
        ConfigurationSection boostsSection = section.getConfigurationSection("boosts");
        if (boostsSection != null) {
            for (String key : boostsSection.getKeys(false)) {
                ConfigurationSection boostSection = boostsSection.getConfigurationSection(key);
                if (boostSection != null) {
                    String materialName = boostSection.getString("item");
                    double multiplier = boostSection.getDouble("multiplier", 1.0);
                    
                    try {
                        Material material = Material.valueOf(materialName.toUpperCase());
                        boosts.put(material, multiplier);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Nieprawidłowy materiał w boost: " + materialName);
                    }
                }
            }
        }
        
        // Load override items
        List<CaseItem> overrideItems = null;
        ConfigurationSection overrideSection = section.getConfigurationSection("override");
        if (overrideSection != null) {
            overrideItems = new ArrayList<>();
            for (String key : overrideSection.getKeys(false)) {
                ConfigurationSection itemSection = overrideSection.getConfigurationSection(key);
                if (itemSection != null) {
                    CaseItem item = loadCaseItem(itemSection);
                    if (item != null) {
                        overrideItems.add(item);
                    }
                }
            }
        }
        
        return new RotationProfile(name, description, boosts, overrideItems);
    }
    
    private Location loadCaseLocation(String caseName) {
        FileConfiguration locations = plugin.getConfigManager().getLocations();
        ConfigurationSection caseSection = locations.getConfigurationSection("cases." + caseName);
        
        if (caseSection == null) {
            return null;
        }
        
        String worldName = caseSection.getString("world");
        if (worldName == null) return null;
        
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        
        double x = caseSection.getDouble("x");
        double y = caseSection.getDouble("y");
        double z = caseSection.getDouble("z");
        
        return new Location(world, x, y, z);
    }
    
    public void setCaseLocation(String caseName, Location location) {
        Case caseObj = cases.get(caseName);
        if (caseObj == null) return;
        
        // Remove old location
        if (caseObj.getLocation() != null) {
            locationToCaseName.remove(caseObj.getLocation());
        }
        
        // Set new location
        caseObj.setLocation(location);
        locationToCaseName.put(location, caseName);
        
        // Save to config
        FileConfiguration locations = plugin.getConfigManager().getLocations();
        String path = "cases." + caseName;
        locations.set(path + ".world", location.getWorld().getName());
        locations.set(path + ".x", location.getX());
        locations.set(path + ".y", location.getY());
        locations.set(path + ".z", location.getZ());
        locations.set(path + ".hologram.enabled", true);
        
        plugin.getConfigManager().saveLocations();
    }
    
    public void removeCaseLocation(String caseName) {
        Case caseObj = cases.get(caseName);
        if (caseObj == null) return;
        
        if (caseObj.getLocation() != null) {
            locationToCaseName.remove(caseObj.getLocation());
            caseObj.setLocation(null);
        }
        
        // Remove from config
        FileConfiguration locations = plugin.getConfigManager().getLocations();
        locations.set("cases." + caseName, null);
        plugin.getConfigManager().saveLocations();
    }
    
    public Case createCase(String name, Case.CaseType type) {
        if (cases.containsKey(name)) {
            return null; // Case already exists
        }
        
        // Create case config
        plugin.getConfigManager().createCaseConfig(name, type.name());
        
        // Reload cases
        loadCases();
        
        return cases.get(name);
    }
    
    public boolean deleteCase(String name) {
        Case caseObj = cases.get(name);
        if (caseObj == null) return false;
        
        // Remove location
        removeCaseLocation(name);
        
        // Remove hologram
        plugin.getHologramManager().removeHologram(name);
        
        // Delete config
        plugin.getConfigManager().deleteCaseConfig(name);
        
        // Remove from memory
        cases.remove(name);
        
        return true;
    }
    
    // Getters
    public Case getCase(String name) {
        return cases.get(name);
    }
    
    public Case getCaseAtLocation(Location location) {
        String caseName = locationToCaseName.get(location);
        return caseName != null ? cases.get(caseName) : null;
    }
    
    public Map<String, Case> getCases() {
        return cases;
    }
    
    public boolean caseExists(String name) {
        return cases.containsKey(name);
    }
    
    public List<String> getCaseNames() {
        return new ArrayList<>(cases.keySet());
    }
    
    public java.util.Collection<Case> getAllCases() {
        return cases.values();
    }
    
    public void saveCase(Case caseObj) {
        if (caseObj != null) {
            cases.put(caseObj.getName(), caseObj);
            // Save to configuration file
            saveCaseToConfig(caseObj);
        }
    }
    
    private void saveCaseToConfig(Case caseObj) {
        // For now, just save the existing config
        // TODO: Implement proper case-to-config serialization
        plugin.getConfigManager().saveCaseConfig(caseObj.getName());
    }
    
    public void removeCase(String caseName) {
        Case removedCase = cases.remove(caseName);
        if (removedCase != null && removedCase.getLocation() != null) {
            locationToCaseName.remove(removedCase.getLocation());
        }
        // Remove from configuration
        plugin.getConfigManager().deleteCaseConfig(caseName);
    }
}