package pl.meklas.meklascase.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.meklas.meklascase.MeklasCasePlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final MeklasCasePlugin plugin;
    private FileConfiguration config;
    private FileConfiguration locations;
    private FileConfiguration rotationState;
    private Map<String, FileConfiguration> caseConfigs;
    
    private File configFile;
    private File locationsFile;
    private File rotationStateFile;
    private File casesFolder;

    public ConfigManager(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.caseConfigs = new HashMap<>();
    }

    public void loadConfigs() {
        // Create plugin folder if doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Create cases folder
        casesFolder = new File(plugin.getDataFolder(), "cases");
        if (!casesFolder.exists()) {
            casesFolder.mkdirs();
        }
        
        // Load main config
        loadMainConfig();
        
        // Load locations config
        loadLocationsConfig();
        
        // Load rotation state
        loadRotationState();
        
        // Load all case configs
        loadCaseConfigs();
        
        plugin.getLogger().info("§a[ConfigManager] Wszystkie konfiguracje załadowane pomyślnie!");
    }
    
    private void loadMainConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Set default values if they don't exist
        if (!config.contains("resetAtFixedTime")) {
            config.set("resetAtFixedTime", true);
            config.set("fixedTime", "04:00");
            config.set("windowHours", 24);
            config.set("quickOpen", true);
            
            config.set("sounds.noKey", "ENTITY_VILLAGER_NO");
            config.set("sounds.spin", "UI_BUTTON_CLICK");
            config.set("sounds.win", "ENTITY_PLAYER_LEVELUP");
            
            config.set("broadcast.enabled", true);
            config.set("broadcast.messages.win", "{player} wygrał {item} x{amount} z {case}");
            config.set("broadcast.messages.top", "{player} pobił TOP DROP! {item} x{amount}");
            config.set("broadcast.messages.rotation", "Nowy dzień! Dziś wysoka szansa na {boost_item}");
            
            saveConfig();
        }
    }
    
    private void loadLocationsConfig() {
        locationsFile = new File(plugin.getDataFolder(), "locations.yml");
        
        if (!locationsFile.exists()) {
            try {
                locationsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Nie można utworzyć pliku locations.yml: " + e.getMessage());
            }
        }
        
        locations = YamlConfiguration.loadConfiguration(locationsFile);
    }
    
    private void loadRotationState() {
        rotationStateFile = new File(plugin.getDataFolder(), "rotation_state.yml");
        
        if (!rotationStateFile.exists()) {
            try {
                rotationStateFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Nie można utworzyć pliku rotation_state.yml: " + e.getMessage());
            }
        }
        
        rotationState = YamlConfiguration.loadConfiguration(rotationStateFile);
    }
    
    private void loadCaseConfigs() {
        caseConfigs.clear();
        
        if (!casesFolder.exists()) {
            return;
        }
        
        File[] files = casesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        
        for (File file : files) {
            String caseName = file.getName().replace(".yml", "");
            FileConfiguration caseConfig = YamlConfiguration.loadConfiguration(file);
            caseConfigs.put(caseName, caseConfig);
        }
        
        // Create example case if no cases exist
        if (caseConfigs.isEmpty()) {
            createExampleCase();
        }
    }
    
    private void createExampleCase() {
        File exampleFile = new File(casesFolder, "example.yml");
        FileConfiguration example = new YamlConfiguration();
        
        example.set("type", "LOOTBOX");
        example.set("key.material", "TRIPWIRE_HOOK");
        example.set("key.name", "&aKlucz do ExampleCase");
        example.set("key.lore", java.util.Arrays.asList("&7Użyj na skrzynce"));
        example.set("key.glow", true);
        
        example.set("items.0.item", "DIAMOND");
        example.set("items.0.amount", 1);
        example.set("items.0.weight", 10);
        
        example.set("items.1.item", "EMERALD");
        example.set("items.1.amount", 2);
        example.set("items.1.weight", 30);
        
        example.set("items.2.item", "GOLD_INGOT");
        example.set("items.2.amount", 8);
        example.set("items.2.weight", 60);
        
        example.set("rotation.profiles.day1.description", "Dziś diamenty lecą częściej!");
        example.set("rotation.profiles.day1.boosts.0.item", "DIAMOND");
        example.set("rotation.profiles.day1.boosts.0.multiplier", 3.0);
        
        example.set("rotation.profiles.day2.description", "Zielony dzień");
        example.set("rotation.profiles.day2.boosts.0.item", "EMERALD");
        example.set("rotation.profiles.day2.boosts.0.multiplier", 2.0);
        
        example.set("rotation.profiles.day3.override.0.item", "ENCHANTED_GOLDEN_APPLE");
        example.set("rotation.profiles.day3.override.0.amount", 1);
        example.set("rotation.profiles.day3.override.0.weight", 100);
        
        try {
            example.save(exampleFile);
            caseConfigs.put("example", example);
            plugin.getLogger().info("§a[ConfigManager] Utworzono przykładową skrzynkę 'example'");
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można utworzyć przykładowej skrzynki: " + e.getMessage());
        }
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać config.yml: " + e.getMessage());
        }
    }
    
    public void saveLocations() {
        try {
            locations.save(locationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać locations.yml: " + e.getMessage());
        }
    }
    
    public void saveRotationState() {
        try {
            rotationState.save(rotationStateFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać rotation_state.yml: " + e.getMessage());
        }
    }
    
    public void saveCaseConfig(String caseName) {
        FileConfiguration caseConfig = caseConfigs.get(caseName);
        if (caseConfig == null) return;
        
        try {
            File caseFile = new File(casesFolder, caseName + ".yml");
            caseConfig.save(caseFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać konfiguracji skrzynki " + caseName + ": " + e.getMessage());
        }
    }
    
    public void createCaseConfig(String caseName, String type) {
        File caseFile = new File(casesFolder, caseName + ".yml");
        FileConfiguration caseConfig = new YamlConfiguration();
        
        caseConfig.set("type", type);
        caseConfig.set("key.material", "TRIPWIRE_HOOK");
        caseConfig.set("key.name", "&a" + caseName + " Key");
        caseConfig.set("key.lore", java.util.Arrays.asList("&7Użyj na skrzynce " + caseName));
        caseConfig.set("key.glow", true);
        
        // Default items
        caseConfig.set("items.0.item", "DIAMOND");
        caseConfig.set("items.0.amount", 1);
        caseConfig.set("items.0.weight", 10);
        
        try {
            caseConfig.save(caseFile);
            caseConfigs.put(caseName, caseConfig);
            plugin.getLogger().info("§a[ConfigManager] Utworzono konfigurację skrzynki: " + caseName);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można utworzyć konfiguracji skrzynki " + caseName + ": " + e.getMessage());
        }
    }
    
    public void deleteCaseConfig(String caseName) {
        File caseFile = new File(casesFolder, caseName + ".yml");
        if (caseFile.exists()) {
            caseFile.delete();
        }
        caseConfigs.remove(caseName);
    }

    // Getters
    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getLocations() {
        return locations;
    }

    public FileConfiguration getRotationState() {
        return rotationState;
    }

    public FileConfiguration getCaseConfig(String caseName) {
        return caseConfigs.get(caseName);
    }

    public Map<String, FileConfiguration> getCaseConfigs() {
        return caseConfigs;
    }
    
    public boolean caseExists(String caseName) {
        return caseConfigs.containsKey(caseName);
    }
}