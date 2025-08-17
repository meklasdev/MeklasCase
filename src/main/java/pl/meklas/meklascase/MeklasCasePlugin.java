package pl.meklas.meklascase;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import org.bukkit.plugin.java.JavaPlugin;
import pl.meklas.meklascase.commands.MeklasCaseCommand;
import pl.meklas.meklascase.config.ConfigManager;
import pl.meklas.meklascase.case.CaseManager;
import pl.meklas.meklascase.rotation.RotationManager;
import pl.meklas.meklascase.hologram.HologramManager;
import pl.meklas.meklascase.listeners.CaseInteractionListener;
import pl.meklas.meklascase.utils.MessageUtils;

public class MeklasCasePlugin extends JavaPlugin {

    private static MeklasCasePlugin instance;
    
    private ConfigManager configManager;
    private CaseManager caseManager;
    private RotationManager rotationManager;
    private HologramManager hologramManager;
    private MessageUtils messageUtils;
    
    private LiteCommands<org.bukkit.command.CommandSender> liteCommands;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize message utils first
        this.messageUtils = new MessageUtils(this);
        
        // Initialize configuration system
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();
        
        // Initialize managers
        this.caseManager = new CaseManager(this);
        this.rotationManager = new RotationManager(this);
        this.hologramManager = new HologramManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new CaseInteractionListener(this), this);
        
        // Initialize LiteCommands
        this.liteCommands = LiteBukkitFactory.builder()
                .commands(new MeklasCaseCommand(this))
                .build();
        
        // Start rotation scheduler
        this.rotationManager.startScheduler();
        
        // Initialize holograms
        this.hologramManager.initializeHolograms();
        
        getLogger().info("§a[meklasCase] Plugin został pomyślnie włączony!");
        getLogger().info("§a[meklasCase] Autor: meklas | Wersja: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) {
            liteCommands.unregister();
        }
        
        if (rotationManager != null) {
            rotationManager.stopScheduler();
        }
        
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        
        // Save rotation state
        if (configManager != null) {
            configManager.saveRotationState();
        }
        
        getLogger().info("§c[meklasCase] Plugin został wyłączony!");
    }
    
    public void reload() {
        try {
            // Save current state
            configManager.saveRotationState();
            
            // Reload configs
            configManager.loadConfigs();
            
            // Restart rotation scheduler
            rotationManager.stopScheduler();
            rotationManager.startScheduler();
            
            // Reload holograms
            hologramManager.removeAllHolograms();
            hologramManager.initializeHolograms();
            
            getLogger().info("§a[meklasCase] Plugin został przeładowany!");
        } catch (Exception e) {
            getLogger().severe("§c[meklasCase] Błąd podczas przeładowania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MeklasCasePlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CaseManager getCaseManager() {
        return caseManager;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
}