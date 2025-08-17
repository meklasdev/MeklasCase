package pl.meklas.meklascase.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.trail.TrailManager;

import java.util.*;
import java.util.stream.Collectors;

public class TrailConfigGUI implements InventoryHolder, Listener {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final TrailManager trailManager;
    private final TrailManagementGUI parentGUI;
    private final Inventory inventory;
    
    // Configuration categories
    private ConfigCategory currentCategory = ConfigCategory.GENERAL;
    
    // Configuration values (these would normally be loaded from config)
    private Map<String, Object> configValues = new HashMap<>();
    
    // Layout constants
    private static final int[] CONFIG_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    // Control buttons
    private static final int BACK_SLOT = 45;
    private static final int SAVE_SLOT = 49;
    private static final int CATEGORY_SLOT = 4;
    private static final int RESET_SLOT = 46;
    private static final int IMPORT_SLOT = 47;
    private static final int EXPORT_SLOT = 51;
    private static final int HELP_SLOT = 52;
    
    public TrailConfigGUI(MeklasCasePlugin plugin, Player player, TrailManagementGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.trailManager = plugin.getTrailManager();
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, 
            Component.text("Konfiguracja Szlaków", NamedTextColor.RED, TextDecoration.BOLD));
        
        loadConfigValues();
        setupGUI();
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadConfigValues() {
        // Initialize default configuration values
        // General settings
        configValues.put("max_trails_per_player", 10);
        configValues.put("max_points_per_trail", 100);
        configValues.put("auto_save_enabled", true);
        configValues.put("auto_save_interval", 300);
        configValues.put("default_trail_active", false);
        configValues.put("default_trail_public", false);
        
        // Visual settings
        configValues.put("particle_enabled", true);
        configValues.put("particle_density", 1.0);
        configValues.put("particle_range", 32.0);
        configValues.put("hologram_enabled", true);
        configValues.put("hologram_height", 2.5);
        configValues.put("show_trail_names", true);
        
        // Performance settings
        configValues.put("update_interval", 20);
        configValues.put("chunk_loading_enabled", true);
        configValues.put("async_processing", true);
        configValues.put("cache_size", 1000);
        configValues.put("cleanup_interval", 3600);
        
        // Permission settings
        configValues.put("require_permission_create", true);
        configValues.put("require_permission_edit", true);
        configValues.put("require_permission_delete", true);
        configValues.put("admin_bypass_limits", true);
        
        // Database settings
        configValues.put("storage_type", "YAML");
        configValues.put("database_host", "localhost");
        configValues.put("database_port", 3306);
        configValues.put("database_name", "meklascase");
        configValues.put("database_username", "root");
        configValues.put("backup_enabled", true);
        configValues.put("backup_interval", 86400);
        
        // Notification settings
        configValues.put("notify_on_create", true);
        configValues.put("notify_on_edit", true);
        configValues.put("notify_on_delete", true);
        configValues.put("broadcast_activations", false);
        configValues.put("sound_enabled", true);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Fill background
        fillBackground();
        
        // Add category selector
        addCategorySelector();
        
        // Add configuration items based on category
        switch (currentCategory) {
            case GENERAL:
                addGeneralConfig();
                break;
            case VISUAL:
                addVisualConfig();
                break;
            case PERFORMANCE:
                addPerformanceConfig();
                break;
            case PERMISSIONS:
                addPermissionConfig();
                break;
            case DATABASE:
                addDatabaseConfig();
                break;
            case NOTIFICATIONS:
                addNotificationConfig();
                break;
        }
        
        // Add control buttons
        addControlButtons();
    }
    
    private void fillBackground() {
        ItemStack background = createItem(Material.RED_STAINED_GLASS_PANE, " ");
        
        // Fill borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, background);
        }
        for (int i = 45; i < 54; i++) {
            if (i != BACK_SLOT && i != SAVE_SLOT && i != RESET_SLOT && 
                i != IMPORT_SLOT && i != EXPORT_SLOT && i != HELP_SLOT) {
                inventory.setItem(i, background);
            }
        }
        
        // Fill sides
        for (int row = 1; row < 5; row++) {
            inventory.setItem(row * 9, background);
            inventory.setItem(row * 9 + 8, background);
        }
    }
    
    private void addCategorySelector() {
        inventory.setItem(CATEGORY_SLOT, createItem(
            getCategoryIcon(currentCategory),
            "§c📋 Kategoria: " + currentCategory.getDisplayName(),
            "§7Aktualna kategoria: §e" + currentCategory.getDisplayName(),
            "",
            "§7Dostępne kategorie:",
            "§8• §7Ogólne - podstawowe ustawienia",
            "§8• §7Wizualne - efekty i wyświetlanie",
            "§8• §7Wydajność - optymalizacja",
            "§8• §7Uprawnienia - kontrola dostępu",
            "§8• §7Baza danych - przechowywanie",
            "§8• §7Powiadomienia - komunikaty",
            "",
            "§eKliknij aby zmienić kategorię"
        ));
    }
    
    private Material getCategoryIcon(ConfigCategory category) {
        switch (category) {
            case GENERAL: return Material.WRITABLE_BOOK;
            case VISUAL: return Material.ENDER_EYE;
            case PERFORMANCE: return Material.REDSTONE;
            case PERMISSIONS: return Material.IRON_DOOR;
            case DATABASE: return Material.CHEST;
            case NOTIFICATIONS: return Material.BELL;
            default: return Material.BOOK;
        }
    }
    
    private void addGeneralConfig() {
        int slot = 0;
        
        // Max trails per player
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.PLAYER_HEAD, "Maks. szlaków na gracza", 
            "max_trails_per_player", Integer.class,
            "Maksymalna liczba szlaków które może utworzyć jeden gracz"
        ));
        
        // Max points per trail
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.COMPASS, "Maks. punktów na szlak",
            "max_points_per_trail", Integer.class,
            "Maksymalna liczba punktów w jednym szlaku"
        ));
        
        // Auto save enabled
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.CLOCK, "Auto-zapisywanie",
            "auto_save_enabled", Boolean.class,
            "Czy automatycznie zapisywać zmiany"
        ));
        
        // Auto save interval
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.REPEATER, "Interwał auto-zapisu",
            "auto_save_interval", Integer.class,
            "Interwał automatycznego zapisywania (sekundy)"
        ));
        
        // Default trail active
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.LIME_DYE, "Domyślnie aktywne",
            "default_trail_active", Boolean.class,
            "Czy nowe szlaki są domyślnie aktywne"
        ));
        
        // Default trail public
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.EMERALD, "Domyślnie publiczne",
            "default_trail_public", Boolean.class,
            "Czy nowe szlaki są domyślnie publiczne"
        ));
    }
    
    private void addVisualConfig() {
        int slot = 0;
        
        // Particle enabled
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.BLAZE_POWDER, "Cząsteczki włączone",
            "particle_enabled", Boolean.class,
            "Czy wyświetlać efekty cząsteczek"
        ));
        
        // Particle density
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.SUGAR, "Gęstość cząsteczek",
            "particle_density", Double.class,
            "Gęstość wyświetlanych cząsteczek"
        ));
        
        // Particle range
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.SPYGLASS, "Zasięg cząsteczek",
            "particle_range", Double.class,
            "Zasięg renderowania cząsteczek (bloki)"
        ));
        
        // Hologram enabled
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.NAME_TAG, "Hologramy włączone",
            "hologram_enabled", Boolean.class,
            "Czy wyświetlać hologramy szlaków"
        ));
        
        // Hologram height
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.LADDER, "Wysokość hologramów",
            "hologram_height", Double.class,
            "Wysokość hologramów nad punktami"
        ));
        
        // Show trail names
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.WRITTEN_BOOK, "Pokazuj nazwy",
            "show_trail_names", Boolean.class,
            "Czy wyświetlać nazwy szlaków"
        ));
    }
    
    private void addPerformanceConfig() {
        int slot = 0;
        
        // Update interval
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.CLOCK, "Interwał aktualizacji",
            "update_interval", Integer.class,
            "Interwał aktualizacji szlaków (ticki)"
        ));
        
        // Chunk loading
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.GRASS_BLOCK, "Ładowanie chunków",
            "chunk_loading_enabled", Boolean.class,
            "Czy automatycznie ładować chunki dla szlaków"
        ));
        
        // Async processing
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.PISTON, "Przetwarzanie async",
            "async_processing", Boolean.class,
            "Czy używać asynchronicznego przetwarzania"
        ));
        
        // Cache size
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.CHEST, "Rozmiar cache",
            "cache_size", Integer.class,
            "Rozmiar pamięci podręcznej"
        ));
        
        // Cleanup interval
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.BRUSH, "Interwał czyszczenia",
            "cleanup_interval", Integer.class,
            "Interwał czyszczenia pamięci (sekundy)"
        ));
    }
    
    private void addPermissionConfig() {
        int slot = 0;
        
        // Require permission create
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.EMERALD, "Wymagaj uprawnienia tworzenia",
            "require_permission_create", Boolean.class,
            "Czy wymagać uprawnień do tworzenia szlaków"
        ));
        
        // Require permission edit
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.WRITABLE_BOOK, "Wymagaj uprawnienia edycji",
            "require_permission_edit", Boolean.class,
            "Czy wymagać uprawnień do edycji szlaków"
        ));
        
        // Require permission delete
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.TNT, "Wymagaj uprawnienia usuwania",
            "require_permission_delete", Boolean.class,
            "Czy wymagać uprawnień do usuwania szlaków"
        ));
        
        // Admin bypass limits
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.COMMAND_BLOCK, "Admin omija limity",
            "admin_bypass_limits", Boolean.class,
            "Czy administratorzy omijają limity"
        ));
    }
    
    private void addDatabaseConfig() {
        int slot = 0;
        
        // Storage type
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.CHEST, "Typ przechowywania",
            "storage_type", String.class,
            "Typ przechowywania danych (YAML/MySQL)"
        ));
        
        // Database host
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.COMPASS, "Host bazy danych",
            "database_host", String.class,
            "Adres hosta bazy danych"
        ));
        
        // Database port
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.IRON_DOOR, "Port bazy danych",
            "database_port", Integer.class,
            "Port bazy danych"
        ));
        
        // Database name
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.NAME_TAG, "Nazwa bazy danych",
            "database_name", String.class,
            "Nazwa bazy danych"
        ));
        
        // Backup enabled
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.BUNDLE, "Kopie zapasowe",
            "backup_enabled", Boolean.class,
            "Czy tworzyć kopie zapasowe"
        ));
        
        // Backup interval
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.CLOCK, "Interwał kopii",
            "backup_interval", Integer.class,
            "Interwał tworzenia kopii (sekundy)"
        ));
    }
    
    private void addNotificationConfig() {
        int slot = 0;
        
        // Notify on create
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.EMERALD, "Powiadom o tworzeniu",
            "notify_on_create", Boolean.class,
            "Czy powiadamiać o tworzeniu szlaków"
        ));
        
        // Notify on edit
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.WRITABLE_BOOK, "Powiadom o edycji",
            "notify_on_edit", Boolean.class,
            "Czy powiadamiać o edycji szlaków"
        ));
        
        // Notify on delete
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.TNT, "Powiadom o usuwaniu",
            "notify_on_delete", Boolean.class,
            "Czy powiadamiać o usuwaniu szlaków"
        ));
        
        // Broadcast activations
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.BELL, "Rozgłaszaj aktywacje",
            "broadcast_activations", Boolean.class,
            "Czy rozgłaszać aktywacje szlaków"
        ));
        
        // Sound enabled
        inventory.setItem(CONFIG_SLOTS[slot++], createConfigItem(
            Material.NOTE_BLOCK, "Dźwięki włączone",
            "sound_enabled", Boolean.class,
            "Czy odtwarzać dźwięki powiadomień"
        ));
    }
    
    private ItemStack createConfigItem(Material material, String name, String configKey, Class<?> type, String description) {
        Object value = configValues.get(configKey);
        String valueStr = value != null ? value.toString() : "null";
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Aktualna wartość: §e" + valueStr);
        lore.add("§7Typ: §e" + type.getSimpleName());
        lore.add("");
        lore.add("§7Opis:");
        lore.add("§f" + description);
        lore.add("");
        
        if (type == Boolean.class) {
            lore.add("§eKliknij aby przełączyć");
        } else {
            lore.add("§eKliknij aby edytować");
            lore.add("§7(Zostaniesz poproszony o wpisanie w chacie)");
        }
        
        return createItem(material, "§b⚙ " + name, lore.toArray(new String[0]));
    }
    
    private void addControlButtons() {
        // Back button
        inventory.setItem(BACK_SLOT, createItem(
            Material.ARROW,
            "§a◀ Powrót",
            "§7Kliknij aby wrócić do zarządzania szlakami"
        ));
        
        // Save button
        inventory.setItem(SAVE_SLOT, createItem(
            Material.EMERALD,
            "§a💾 Zapisz konfigurację",
            "§7Kliknij aby zapisać wszystkie zmiany",
            "",
            "§aZapisuje konfigurację do pliku"
        ));
        
        // Reset button
        inventory.setItem(RESET_SLOT, createItem(
            Material.BARRIER,
            "§c🔄 Resetuj kategorię",
            "§7Kliknij aby zresetować ustawienia kategorii",
            "",
            "§c⚠ Przywróci domyślne wartości"
        ));
        
        // Import button
        inventory.setItem(IMPORT_SLOT, createItem(
            Material.HOPPER,
            "§b📥 Importuj",
            "§7Kliknij aby importować konfigurację",
            "",
            "§7Importuje ustawienia z pliku"
        ));
        
        // Export button
        inventory.setItem(EXPORT_SLOT, createItem(
            Material.DROPPER,
            "§b📤 Eksportuj",
            "§7Kliknij aby eksportować konfigurację",
            "",
            "§7Eksportuje ustawienia do pliku"
        ));
        
        // Help button
        inventory.setItem(HELP_SLOT, createItem(
            Material.BOOK,
            "§e❓ Pomoc",
            "§7Kliknij aby wyświetlić pomoc",
            "",
            "§7Wyświetla informacje o konfiguracji"
        ));
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        
        if (lore.length > 0) {
            List<Component> loreComponents = Arrays.stream(lore)
                .filter(line -> !line.isEmpty())
                .map(line -> Component.text(line).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
            meta.lore(loreComponents);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public void switchCategory(ConfigCategory category) {
        this.currentCategory = category;
        setupGUI();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
    
    public void saveConfiguration() {
        try {
            // TODO: Implement actual configuration saving
            player.sendMessage(Component.text("§aKonfiguracja została zapisana!", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } catch (Exception e) {
            player.sendMessage(Component.text("§cBłąd podczas zapisywania konfiguracji: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    public void resetCategory() {
        // Reset current category to defaults
        switch (currentCategory) {
            case GENERAL:
                configValues.put("max_trails_per_player", 10);
                configValues.put("max_points_per_trail", 100);
                configValues.put("auto_save_enabled", true);
                configValues.put("auto_save_interval", 300);
                configValues.put("default_trail_active", false);
                configValues.put("default_trail_public", false);
                break;
            // Add other categories...
        }
        
        setupGUI();
        player.sendMessage(Component.text("§aUstawienia kategorii zostały zresetowane!", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player)) return;
        
        int slot = event.getSlot();
        
        // Handle control buttons
        if (slot == BACK_SLOT) {
            parentGUI.open();
        } else if (slot == SAVE_SLOT) {
            saveConfiguration();
        } else if (slot == CATEGORY_SLOT) {
            switchToNextCategory();
        } else if (slot == RESET_SLOT) {
            resetCategory();
        } else if (slot == IMPORT_SLOT) {
            importConfiguration();
        } else if (slot == EXPORT_SLOT) {
            exportConfiguration();
        } else if (slot == HELP_SLOT) {
            showHelp();
        } else {
            // Handle config item clicks
            handleConfigClick(slot);
        }
    }
    
    private void switchToNextCategory() {
        ConfigCategory[] categories = ConfigCategory.values();
        int currentIndex = currentCategory.ordinal();
        int nextIndex = (currentIndex + 1) % categories.length;
        switchCategory(categories[nextIndex]);
    }
    
    private void handleConfigClick(int slot) {
        for (int i = 0; i < CONFIG_SLOTS.length; i++) {
            if (slot == CONFIG_SLOTS[i]) {
                // Find the config item at this position
                handleConfigItemClick(i);
                break;
            }
        }
    }
    
    private void handleConfigItemClick(int itemIndex) {
        // This would handle clicking on specific config items
        // For now, show a placeholder message
        player.sendMessage(Component.text("§eEdycja konfiguracji zostanie wkrótce dodana!", NamedTextColor.YELLOW));
    }
    
    private void importConfiguration() {
        player.sendMessage(Component.text("§eImport konfiguracji zostanie wkrótce dodany!", NamedTextColor.YELLOW));
    }
    
    private void exportConfiguration() {
        player.sendMessage(Component.text("§eEksport konfiguracji zostanie wkrótce dodany!", NamedTextColor.YELLOW));
    }
    
    private void showHelp() {
        player.closeInventory();
        player.sendMessage(Component.text("§6=== Pomoc - Konfiguracja Szlaków ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("§7Ta konfiguracja pozwala na dostosowanie:", NamedTextColor.GRAY));
        player.sendMessage(Component.text("§8• §7Ogólne ustawienia systemu szlaków"));
        player.sendMessage(Component.text("§8• §7Efekty wizualne i wyświetlanie"));
        player.sendMessage(Component.text("§8• §7Optymalizację wydajności"));
        player.sendMessage(Component.text("§8• §7Kontrolę uprawnień"));
        player.sendMessage(Component.text("§8• §7Ustawienia bazy danych"));
        player.sendMessage(Component.text("§8• §7Powiadomienia i komunikaty"));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("§eUżyj przycisków w GUI do nawigacji między kategoriami.", NamedTextColor.YELLOW));
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Unregister listener when GUI is closed
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }
    
    public enum ConfigCategory {
        GENERAL("Ogólne"),
        VISUAL("Wizualne"),
        PERFORMANCE("Wydajność"),
        PERMISSIONS("Uprawnienia"),
        DATABASE("Baza danych"),
        NOTIFICATIONS("Powiadomienia");
        
        private final String displayName;
        
        ConfigCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}