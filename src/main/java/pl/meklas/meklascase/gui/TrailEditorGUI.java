package pl.meklas.meklascase.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import pl.meklas.meklascase.trail.Trail;
import pl.meklas.meklascase.trail.TrailManager;
import pl.meklas.meklascase.trail.TrailType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TrailEditorGUI implements InventoryHolder, Listener {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final Trail trail;
    private final TrailManager trailManager;
    private final TrailManagementGUI parentGUI;
    private final Inventory inventory;
    
    // GUI State
    private EditorMode currentMode = EditorMode.OVERVIEW;
    private int pointsPage = 0;
    private final int pointsPerPage = 21; // 3 rows of 7 points
    
    // Layout constants
    private static final int[] POINT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    
    // Control buttons
    private static final int BACK_SLOT = 45;
    private static final int SAVE_SLOT = 49;
    private static final int MODE_SLOT = 4;
    private static final int ADD_POINT_SLOT = 46;
    private static final int PREV_POINTS_SLOT = 47;
    private static final int NEXT_POINTS_SLOT = 51;
    private static final int DELETE_TRAIL_SLOT = 53;
    
    // Property editing slots
    private static final int NAME_SLOT = 10;
    private static final int DESCRIPTION_SLOT = 11;
    private static final int TYPE_SLOT = 12;
    private static final int CATEGORY_SLOT = 13;
    private static final int PRIORITY_SLOT = 14;
    private static final int ACTIVE_SLOT = 15;
    private static final int PUBLIC_SLOT = 16;
    private static final int SPEED_SLOT = 19;
    private static final int DURATION_SLOT = 20;
    private static final int MATERIAL_SLOT = 21;
    private static final int PARTICLE_SLOT = 22;
    
    public TrailEditorGUI(MeklasCasePlugin plugin, Player player, Trail trail, TrailManagementGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.trail = trail;
        this.trailManager = plugin.getTrailManager();
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, 
            Component.text("Edytor Szlaku: " + trail.getName(), NamedTextColor.BLUE, TextDecoration.BOLD));
        
        setupGUI();
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Fill background
        fillBackground();
        
        // Add mode selector
        addModeSelector();
        
        // Add content based on current mode
        switch (currentMode) {
            case OVERVIEW:
                addOverviewContent();
                break;
            case PROPERTIES:
                addPropertiesContent();
                break;
            case POINTS:
                addPointsContent();
                break;
            case SETTINGS:
                addSettingsContent();
                break;
        }
        
        // Add control buttons
        addControlButtons();
    }
    
    private void fillBackground() {
        ItemStack background = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        
        // Fill borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, background);
        }
        for (int i = 45; i < 54; i++) {
            if (i != BACK_SLOT && i != SAVE_SLOT && i != ADD_POINT_SLOT && 
                i != PREV_POINTS_SLOT && i != NEXT_POINTS_SLOT && i != DELETE_TRAIL_SLOT) {
                inventory.setItem(i, background);
            }
        }
        
        // Fill sides
        for (int row = 1; row < 5; row++) {
            inventory.setItem(row * 9, background);
            inventory.setItem(row * 9 + 8, background);
        }
    }
    
    private void addModeSelector() {
        inventory.setItem(MODE_SLOT, createItem(
            getModeIcon(currentMode),
            "§6📋 Tryb: " + currentMode.getDisplayName(),
            "§7Aktualny tryb: §e" + currentMode.getDisplayName(),
            "",
            "§7Dostępne tryby:",
            "§8• §7Przegląd - ogólne informacje",
            "§8• §7Właściwości - edycja ustawień",
            "§8• §7Punkty - zarządzanie punktami",
            "§8• §7Ustawienia - zaawansowane opcje",
            "",
            "§eKliknij aby zmienić tryb"
        ));
    }
    
    private Material getModeIcon(EditorMode mode) {
        switch (mode) {
            case OVERVIEW: return Material.BOOK;
            case PROPERTIES: return Material.WRITABLE_BOOK;
            case POINTS: return Material.COMPASS;
            case SETTINGS: return Material.REDSTONE;
            default: return Material.PAPER;
        }
    }
    
    private void addOverviewContent() {
        // Trail statistics and overview
        inventory.setItem(10, createItem(
            Material.NAME_TAG,
            "§b📝 Nazwa",
            "§7Aktualna nazwa: §e" + trail.getName(),
            "",
            "§7Kliknij aby zmienić nazwę"
        ));
        
        inventory.setItem(11, createItem(
            trail.getType().getIcon(),
            "§b🏷 Typ",
            "§7Aktualny typ: §e" + trail.getType().getDisplayName(),
            "§7Opis: §f" + trail.getType().getDescription(),
            "",
            "§7Kliknij aby zmienić typ"
        ));
        
        inventory.setItem(12, createItem(
            Material.CHEST,
            "§b📂 Kategoria",
            "§7Aktualna kategoria: §e" + trail.getCategory(),
            "",
            "§7Kliknij aby zmienić kategorię"
        ));
        
        inventory.setItem(13, createItem(
            Material.MAP,
            "§b📍 Punkty",
            "§7Liczba punktów: §e" + trail.getPoints().size(),
            "§7Długość szlaku: §e" + String.format("%.1f", trail.getLength()) + "m",
            "",
            "§7Kliknij aby zarządzać punktami"
        ));
        
        inventory.setItem(14, createItem(
            trail.isActive() ? Material.LIME_DYE : Material.RED_DYE,
            "§b⚡ Status",
            "§7Aktualny status: " + (trail.isActive() ? "§aAktywny" : "§cNieaktywny"),
            "",
            "§7Kliknij aby przełączyć status"
        ));
        
        inventory.setItem(15, createItem(
            trail.isPublicTrail() ? Material.EMERALD : Material.REDSTONE,
            "§b🌐 Publiczny",
            "§7Publiczny dostęp: " + (trail.isPublicTrail() ? "§aTak" : "§cNie"),
            "",
            "§7Kliknij aby przełączyć dostęp"
        ));
        
        inventory.setItem(16, createItem(
            Material.CLOCK,
            "§b📅 Informacje",
            "§7Utworzono: §e" + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(trail.getCreatedAt())),
            "§7Ostatnia modyfikacja: §e" + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(trail.getLastModified())),
            "§7Właściciel: §e" + Bukkit.getOfflinePlayer(trail.getOwner()).getName(),
            "",
            "§7Priorytet: §e" + trail.getPriority()
        ));
        
        // Description
        if (trail.getDescription() != null && !trail.getDescription().isEmpty()) {
            inventory.setItem(22, createItem(
                Material.WRITTEN_BOOK,
                "§b📖 Opis",
                "§f" + trail.getDescription(),
                "",
                "§7Kliknij aby edytować opis"
            ));
        } else {
            inventory.setItem(22, createItem(
                Material.WRITABLE_BOOK,
                "§b📖 Dodaj opis",
                "§7Ten szlak nie ma opisu",
                "",
                "§7Kliknij aby dodać opis"
            ));
        }
    }
    
    private void addPropertiesContent() {
        // Editable properties in table format
        inventory.setItem(NAME_SLOT, createEditableItem(
            Material.NAME_TAG, "Nazwa", trail.getName()
        ));
        
        inventory.setItem(DESCRIPTION_SLOT, createEditableItem(
            Material.WRITABLE_BOOK, "Opis", 
            trail.getDescription() != null ? trail.getDescription() : "Brak opisu"
        ));
        
        inventory.setItem(TYPE_SLOT, createEditableItem(
            trail.getType().getIcon(), "Typ", trail.getType().getDisplayName()
        ));
        
        inventory.setItem(CATEGORY_SLOT, createEditableItem(
            Material.CHEST, "Kategoria", trail.getCategory()
        ));
        
        inventory.setItem(PRIORITY_SLOT, createEditableItem(
            Material.EXPERIENCE_BOTTLE, "Priorytet", String.valueOf(trail.getPriority())
        ));
        
        inventory.setItem(ACTIVE_SLOT, createToggleItem(
            trail.isActive() ? Material.LIME_DYE : Material.RED_DYE, 
            "Aktywny", trail.isActive()
        ));
        
        inventory.setItem(PUBLIC_SLOT, createToggleItem(
            trail.isPublicTrail() ? Material.EMERALD : Material.REDSTONE,
            "Publiczny", trail.isPublicTrail()
        ));
        
        inventory.setItem(SPEED_SLOT, createEditableItem(
            Material.SUGAR, "Prędkość", String.valueOf(trail.getSpeed())
        ));
        
        inventory.setItem(DURATION_SLOT, createEditableItem(
            Material.CLOCK, "Czas trwania", trail.getDuration() + " sekund"
        ));
        
        inventory.setItem(MATERIAL_SLOT, createEditableItem(
            trail.getDisplayMaterial(), "Materiał", trail.getDisplayMaterial().name()
        ));
        
        inventory.setItem(PARTICLE_SLOT, createEditableItem(
            Material.BLAZE_POWDER, "Cząsteczki", trail.getParticle().name()
        ));
    }
    
    private void addPointsContent() {
        List<Location> points = trail.getPoints();
        int startIndex = pointsPage * pointsPerPage;
        int endIndex = Math.min(startIndex + pointsPerPage, points.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Location point = points.get(i);
            int slot = POINT_SLOTS[i - startIndex];
            
            inventory.setItem(slot, createPointItem(point, i));
        }
        
        // Points navigation
        int maxPages = (int) Math.ceil((double) points.size() / pointsPerPage);
        inventory.setItem(PREV_POINTS_SLOT, createItem(
            pointsPage > 0 ? Material.ARROW : Material.GRAY_DYE,
            "§a◀ Poprzednie punkty",
            pointsPage > 0 ? "§7Kliknij aby przejść do poprzednich punktów" : "§7Brak poprzednich punktów"
        ));
        
        inventory.setItem(NEXT_POINTS_SLOT, createItem(
            pointsPage < maxPages - 1 ? Material.ARROW : Material.GRAY_DYE,
            "§aNastępne punkty ▶",
            pointsPage < maxPages - 1 ? "§7Kliknij aby przejść do następnych punktów" : "§7Brak następnych punktów"
        ));
        
        // Points info
        inventory.setItem(4, createItem(
            Material.MAP,
            "§6📍 Punkty szlaku",
            "§7Strona: §e" + (pointsPage + 1) + " / " + Math.max(1, maxPages),
            "§7Punkty na stronie: §e" + (endIndex - startIndex),
            "§7Wszystkie punkty: §e" + points.size(),
            "§7Długość szlaku: §e" + String.format("%.1f", trail.getLength()) + "m"
        ));
    }
    
    private ItemStack createPointItem(Location point, int index) {
        return createItem(
            Material.ENDER_PEARL,
            "§b📍 Punkt " + (index + 1),
            "§7Świat: §e" + point.getWorld().getName(),
            "§7X: §e" + String.format("%.1f", point.getX()),
            "§7Y: §e" + String.format("%.1f", point.getY()),
            "§7Z: §e" + String.format("%.1f", point.getZ()),
            "",
            "§eLewy klik: §7Teleportuj do punktu",
            "§ePrawy klik: §7Edytuj współrzędne",
            "§eShift + klik: §7Usuń punkt"
        );
    }
    
    private void addSettingsContent() {
        // Advanced settings
        inventory.setItem(10, createItem(
            Material.REDSTONE_TORCH,
            "§c⚙ Zaawansowane ustawienia",
            "§7Kliknij aby otworzyć zaawansowane ustawienia"
        ));
        
        inventory.setItem(11, createItem(
            Material.COMMAND_BLOCK,
            "§c🔧 Automatyzacja",
            "§7Konfiguruj automatyczne akcje"
        ));
        
        inventory.setItem(12, createItem(
            Material.OBSERVER,
            "§c👁 Monitoring",
            "§7Ustawienia monitorowania szlaku"
        ));
        
        inventory.setItem(13, createItem(
            Material.BARRIER,
            "§c🚫 Ograniczenia",
            "§7Konfiguruj ograniczenia dostępu"
        ));
    }
    
    private ItemStack createEditableItem(Material material, String property, String value) {
        return createItem(
            material,
            "§b📝 " + property,
            "§7Aktualna wartość: §e" + value,
            "",
            "§7Kliknij aby edytować",
            "§eLewy klik: §7Szybka edycja",
            "§ePrawy klik: §7Zaawansowana edycja"
        );
    }
    
    private ItemStack createToggleItem(Material material, String property, boolean value) {
        return createItem(
            material,
            "§b🔄 " + property,
            "§7Aktualny stan: " + (value ? "§aWłączony" : "§cWyłączony"),
            "",
            "§7Kliknij aby przełączyć"
        );
    }
    
    private void addControlButtons() {
        // Back button
        inventory.setItem(BACK_SLOT, createItem(
            Material.ARROW,
            "§a◀ Powrót",
            "§7Kliknij aby wrócić do listy szlaków"
        ));
        
        // Save button
        inventory.setItem(SAVE_SLOT, createItem(
            Material.EMERALD,
            "§a💾 Zapisz",
            "§7Kliknij aby zapisać zmiany",
            "",
            "§aZapisuje wszystkie wprowadzone zmiany"
        ));
        
        // Add point button (only in points mode)
        if (currentMode == EditorMode.POINTS) {
            inventory.setItem(ADD_POINT_SLOT, createItem(
                Material.LIME_DYE,
                "§a➕ Dodaj punkt",
                "§7Kliknij aby dodać nowy punkt",
                "",
                "§eLewy klik: §7Dodaj aktualną pozycję",
                "§ePrawy klik: §7Wprowadź współrzędne"
            ));
        }
        
        // Delete trail button
        inventory.setItem(DELETE_TRAIL_SLOT, createItem(
            Material.TNT,
            "§c🗑 Usuń szlak",
            "§7Kliknij aby usunąć szlak",
            "",
            "§c⚠ UWAGA: Ta akcja jest nieodwracalna!"
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
    
    public void switchMode(EditorMode mode) {
        this.currentMode = mode;
        this.pointsPage = 0; // Reset points page when switching modes
        setupGUI();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
    
    public void saveChanges() {
        if (trailManager.updateTrail(trail)) {
            player.sendMessage(Component.text("§aZmiany zostały zapisane pomyślnie!", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(Component.text("§cBłąd podczas zapisywania zmian!", NamedTextColor.RED));
        }
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
            saveChanges();
        } else if (slot == MODE_SLOT) {
            switchToNextMode();
        } else if (slot == DELETE_TRAIL_SLOT) {
            openDeleteConfirmation();
        } else if (currentMode == EditorMode.POINTS) {
            handlePointsMode(slot, event);
        } else if (currentMode == EditorMode.PROPERTIES) {
            handlePropertiesMode(slot, event);
        } else if (currentMode == EditorMode.OVERVIEW) {
            handleOverviewMode(slot, event);
        }
    }
    
    private void switchToNextMode() {
        EditorMode[] modes = EditorMode.values();
        int currentIndex = currentMode.ordinal();
        int nextIndex = (currentIndex + 1) % modes.length;
        switchMode(modes[nextIndex]);
    }
    
    private void handlePointsMode(int slot, InventoryClickEvent event) {
        if (slot == ADD_POINT_SLOT) {
            if (event.isRightClick()) {
                openCoordinateInput();
            } else {
                addCurrentLocation();
            }
        } else if (slot == PREV_POINTS_SLOT) {
            if (pointsPage > 0) {
                pointsPage--;
                setupGUI();
            }
        } else if (slot == NEXT_POINTS_SLOT) {
            int maxPages = (int) Math.ceil((double) trail.getPoints().size() / pointsPerPage);
            if (pointsPage < maxPages - 1) {
                pointsPage++;
                setupGUI();
            }
        } else {
            // Handle point clicks
            for (int i = 0; i < POINT_SLOTS.length; i++) {
                if (slot == POINT_SLOTS[i]) {
                    int pointIndex = pointsPage * pointsPerPage + i;
                    if (pointIndex < trail.getPoints().size()) {
                        handlePointClick(pointIndex, event);
                    }
                    break;
                }
            }
        }
    }
    
    private void handlePropertiesMode(int slot, InventoryClickEvent event) {
        switch (slot) {
            case ACTIVE_SLOT:
                trail.setActive(!trail.isActive());
                setupGUI();
                break;
            case PUBLIC_SLOT:
                trail.setPublicTrail(!trail.isPublicTrail());
                setupGUI();
                break;
            // Add more property handlers here
            default:
                player.sendMessage(Component.text("§eEdycja tej właściwości zostanie wkrótce dodana!", NamedTextColor.YELLOW));
                break;
        }
    }
    
    private void handleOverviewMode(int slot, InventoryClickEvent event) {
        // Handle overview interactions
        player.sendMessage(Component.text("§eInterakcje w trybie przeglądu zostaną wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void handlePointClick(int pointIndex, InventoryClickEvent event) {
        Location point = trail.getPoints().get(pointIndex);
        
        if (event.isShiftClick()) {
            // Delete point
            trail.removePoint(pointIndex);
            setupGUI();
            player.sendMessage(Component.text("§aPunkt został usunięty!", NamedTextColor.GREEN));
        } else if (event.isRightClick()) {
            // Edit coordinates
            player.sendMessage(Component.text("§eEdycja współrzędnych zostanie wkrótce dodana!", NamedTextColor.YELLOW));
        } else {
            // Teleport to point
            player.teleport(point);
            player.sendMessage(Component.text("§aTeleportowano do punktu " + (pointIndex + 1), NamedTextColor.GREEN));
        }
    }
    
    private void addCurrentLocation() {
        trail.addPoint(player.getLocation());
        setupGUI();
        player.sendMessage(Component.text("§aDodano nowy punkt na aktualnej pozycji!", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
    
    private void openCoordinateInput() {
        player.sendMessage(Component.text("§eWprowadzanie współrzędnych zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void openDeleteConfirmation() {
        player.sendMessage(Component.text("§ePotwierdzenie usunięcia zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Unregister listener when GUI is closed
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }
    
    public enum EditorMode {
        OVERVIEW("Przegląd"),
        PROPERTIES("Właściwości"),
        POINTS("Punkty"),
        SETTINGS("Ustawienia");
        
        private final String displayName;
        
        EditorMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}