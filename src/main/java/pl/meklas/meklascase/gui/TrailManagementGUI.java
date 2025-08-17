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
import pl.meklas.meklascase.trail.Trail;
import pl.meklas.meklascase.trail.TrailManager;
import pl.meklas.meklascase.trail.TrailType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TrailManagementGUI implements InventoryHolder, Listener {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final TrailManager trailManager;
    private final Inventory inventory;
    
    // GUI State
    private List<Trail> currentTrails;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 4 rows of 7 items
    private TrailFilterType currentFilter = TrailFilterType.ALL;
    private String searchQuery = "";
    private SortType sortType = SortType.NAME;
    
    // GUI Layout Constants
    private static final int[] TRAIL_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    // Control buttons
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int CREATE_TRAIL_SLOT = 49;
    private static final int FILTER_SLOT = 47;
    private static final int SORT_SLOT = 51;
    private static final int SEARCH_SLOT = 46;
    private static final int REFRESH_SLOT = 52;
    private static final int SETTINGS_SLOT = 48;
    private static final int CLOSE_SLOT = 50;
    
    public TrailManagementGUI(MeklasCasePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.trailManager = plugin.getTrailManager();
        this.inventory = Bukkit.createInventory(this, 54, 
            Component.text("Zarządzanie Szlakami", NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
        
        loadTrails();
        setupGUI();
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadTrails() {
        List<Trail> allTrails;
        
        // Apply filter
        switch (currentFilter) {
            case MY_TRAILS:
                allTrails = trailManager.getPlayerTrails(player.getUniqueId());
                break;
            case PUBLIC_TRAILS:
                allTrails = trailManager.getPublicTrails();
                break;
            case ACTIVE_TRAILS:
                allTrails = trailManager.getActiveTrails();
                break;
            case BY_TYPE:
                // This would be set by a separate method
                allTrails = trailManager.getAllTrails();
                break;
            default:
                allTrails = trailManager.getAllTrails().stream()
                    .filter(trail -> trailManager.canPlayerViewTrail(player, trail))
                    .collect(Collectors.toList());
                break;
        }
        
        // Apply search
        if (!searchQuery.isEmpty()) {
            allTrails = allTrails.stream()
                .filter(trail -> trail.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                               (trail.getDescription() != null && 
                                trail.getDescription().toLowerCase().contains(searchQuery.toLowerCase())))
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        switch (sortType) {
            case NAME:
                allTrails.sort(Comparator.comparing(Trail::getName));
                break;
            case DATE_CREATED:
                allTrails.sort(Comparator.comparingLong(Trail::getCreatedAt).reversed());
                break;
            case PRIORITY:
                allTrails.sort(Comparator.comparingInt(Trail::getPriority).reversed());
                break;
            case TYPE:
                allTrails.sort(Comparator.comparing(trail -> trail.getType().getDisplayName()));
                break;
        }
        
        this.currentTrails = allTrails;
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Fill background
        fillBackground();
        
        // Add control buttons
        addControlButtons();
        
        // Add trail items
        addTrailItems();
        
        // Add page info
        addPageInfo();
    }
    
    private void fillBackground() {
        ItemStack background = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // Fill borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, background);
        }
        for (int i = 45; i < 54; i++) {
            if (i != PREV_PAGE_SLOT && i != NEXT_PAGE_SLOT && i != CREATE_TRAIL_SLOT && 
                i != FILTER_SLOT && i != SORT_SLOT && i != SEARCH_SLOT && 
                i != REFRESH_SLOT && i != SETTINGS_SLOT && i != CLOSE_SLOT) {
                inventory.setItem(i, background);
            }
        }
        
        // Fill sides
        for (int row = 1; row < 5; row++) {
            inventory.setItem(row * 9, background);
            inventory.setItem(row * 9 + 8, background);
        }
    }
    
    private void addControlButtons() {
        // Previous page
        inventory.setItem(PREV_PAGE_SLOT, createItem(
            currentPage > 0 ? Material.ARROW : Material.GRAY_DYE,
            "§a◀ Poprzednia strona",
            currentPage > 0 ? "§7Kliknij aby przejść do poprzedniej strony" : "§7Brak poprzednich stron"
        ));
        
        // Next page
        int maxPages = (int) Math.ceil((double) currentTrails.size() / itemsPerPage);
        inventory.setItem(NEXT_PAGE_SLOT, createItem(
            currentPage < maxPages - 1 ? Material.ARROW : Material.GRAY_DYE,
            "§aStrona następna ▶",
            currentPage < maxPages - 1 ? "§7Kliknij aby przejść do następnej strony" : "§7Brak następnych stron"
        ));
        
        // Create new trail
        inventory.setItem(CREATE_TRAIL_SLOT, createItem(
            Material.EMERALD,
            "§a✚ Utwórz nowy szlak",
            "§7Kliknij aby utworzyć nowy szlak",
            "",
            "§eLewy klik: §7Szybkie tworzenie",
            "§eShift + klik: §7Zaawansowane opcje"
        ));
        
        // Filter button
        inventory.setItem(FILTER_SLOT, createItem(
            Material.HOPPER,
            "§6🔍 Filtr: " + currentFilter.getDisplayName(),
            "§7Aktualny filtr: §e" + currentFilter.getDisplayName(),
            "",
            "§7Dostępne filtry:",
            "§8• §7Wszystkie szlaki",
            "§8• §7Moje szlaki", 
            "§8• §7Szlaki publiczne",
            "§8• §7Aktywne szlaki",
            "",
            "§eKliknij aby zmienić filtr"
        ));
        
        // Sort button
        inventory.setItem(SORT_SLOT, createItem(
            Material.COMPARATOR,
            "§6📊 Sortowanie: " + sortType.getDisplayName(),
            "§7Aktualne sortowanie: §e" + sortType.getDisplayName(),
            "",
            "§7Dostępne opcje:",
            "§8• §7Według nazwy",
            "§8• §7Według daty utworzenia",
            "§8• §7Według priorytetu",
            "§8• §7Według typu",
            "",
            "§eKliknij aby zmienić sortowanie"
        ));
        
        // Search button
        inventory.setItem(SEARCH_SLOT, createItem(
            Material.COMPASS,
            "§b🔎 Szukaj",
            searchQuery.isEmpty() ? "§7Kliknij aby wyszukać szlaki" : "§7Szukasz: §e" + searchQuery,
            "",
            "§eKliknij aby wyszukać szlaki",
            searchQuery.isEmpty() ? "" : "§cShift + klik aby wyczyścić"
        ));
        
        // Refresh button
        inventory.setItem(REFRESH_SLOT, createItem(
            Material.RECOVERY_COMPASS,
            "§a🔄 Odśwież",
            "§7Kliknij aby odświeżyć listę szlaków"
        ));
        
        // Settings button
        inventory.setItem(SETTINGS_SLOT, createItem(
            Material.REDSTONE,
            "§c⚙ Ustawienia",
            "§7Kliknij aby otworzyć ustawienia szlaków"
        ));
        
        // Close button
        inventory.setItem(CLOSE_SLOT, createItem(
            Material.BARRIER,
            "§c✖ Zamknij",
            "§7Kliknij aby zamknąć menu"
        ));
    }
    
    private void addTrailItems() {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, currentTrails.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Trail trail = currentTrails.get(i);
            int slot = TRAIL_SLOTS[i - startIndex];
            
            ItemStack item = createTrailItem(trail);
            inventory.setItem(slot, item);
        }
    }
    
    private ItemStack createTrailItem(Trail trail) {
        Material material = trail.getType().getIcon();
        List<String> lore = new ArrayList<>();
        
        lore.add("§7Typ: §e" + trail.getType().getDisplayName());
        lore.add("§7Kategoria: §e" + trail.getCategory());
        lore.add("§7Punkty: §e" + trail.getPoints().size());
        lore.add("§7Długość: §e" + String.format("%.1f", trail.getLength()) + "m");
        lore.add("§7Status: " + (trail.isActive() ? "§aAktywny" : "§cNieaktywny"));
        lore.add("§7Publiczny: " + (trail.isPublicTrail() ? "§aTak" : "§cNie"));
        lore.add("§7Priorytet: §e" + trail.getPriority());
        
        if (trail.getDescription() != null && !trail.getDescription().isEmpty()) {
            lore.add("");
            lore.add("§7Opis:");
            lore.add("§f" + trail.getDescription());
        }
        
        lore.add("");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        lore.add("§7Utworzono: §e" + sdf.format(new Date(trail.getCreatedAt())));
        
        lore.add("");
        lore.add("§eLewy klik: §7Edytuj szlak");
        lore.add("§ePrawy klik: §7Szybkie akcje");
        lore.add("§eShift + klik: §7Usuń szlak");
        
        if (trailManager.canPlayerEditTrail(player, trail)) {
            lore.add("§aUprawnienia: §7Możesz edytować");
        } else {
            lore.add("§cUprawnienia: §7Tylko do odczytu");
        }
        
        return createItem(material, "§b" + trail.getName(), lore.toArray(new String[0]));
    }
    
    private void addPageInfo() {
        int totalPages = Math.max(1, (int) Math.ceil((double) currentTrails.size() / itemsPerPage));
        int currentPageDisplay = currentTrails.isEmpty() ? 0 : currentPage + 1;
        
        ItemStack pageInfo = createItem(
            Material.BOOK,
            "§6📄 Informacje",
            "§7Strona: §e" + currentPageDisplay + " / " + totalPages,
            "§7Szlaki na stronie: §e" + Math.min(itemsPerPage, currentTrails.size() - (currentPage * itemsPerPage)),
            "§7Wszystkie szlaki: §e" + currentTrails.size(),
            "§7Całkowita liczba: §e" + trailManager.getTotalTrailCount(),
            "",
            "§7Filtr: §e" + currentFilter.getDisplayName(),
            "§7Sortowanie: §e" + sortType.getDisplayName(),
            searchQuery.isEmpty() ? "" : "§7Wyszukiwanie: §e" + searchQuery
        );
        
        inventory.setItem(4, pageInfo);
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
    
    public void refresh() {
        loadTrails();
        setupGUI();
    }
    
    public void nextPage() {
        int maxPages = (int) Math.ceil((double) currentTrails.size() / itemsPerPage);
        if (currentPage < maxPages - 1) {
            currentPage++;
            refresh();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
    
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
    
    public void setFilter(TrailFilterType filter) {
        this.currentFilter = filter;
        this.currentPage = 0;
        refresh();
    }
    
    public void setSort(SortType sort) {
        this.sortType = sort;
        refresh();
    }
    
    public void setSearchQuery(String query) {
        this.searchQuery = query;
        this.currentPage = 0;
        refresh();
    }
    
    public void openTrailEditor(Trail trail) {
        if (trailManager.canPlayerEditTrail(player, trail)) {
            new TrailEditorGUI(plugin, player, trail, this).open();
        } else {
            player.sendMessage(Component.text("§cNie masz uprawnień do edycji tego szlaku!", NamedTextColor.RED));
        }
    }
    
    public void openTrailCreator() {
        new TrailCreatorGUI(plugin, player, this).open();
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
        if (slot == PREV_PAGE_SLOT) {
            previousPage();
        } else if (slot == NEXT_PAGE_SLOT) {
            nextPage();
        } else if (slot == CREATE_TRAIL_SLOT) {
            openTrailCreator();
        } else if (slot == FILTER_SLOT) {
            openFilterMenu();
        } else if (slot == SORT_SLOT) {
            openSortMenu();
        } else if (slot == SEARCH_SLOT) {
            openSearchMenu();
        } else if (slot == REFRESH_SLOT) {
            refresh();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else if (slot == SETTINGS_SLOT) {
            openSettingsMenu();
        } else if (slot == CLOSE_SLOT) {
            player.closeInventory();
        } else {
            // Handle trail items
            for (int i = 0; i < TRAIL_SLOTS.length; i++) {
                if (slot == TRAIL_SLOTS[i]) {
                    int trailIndex = currentPage * itemsPerPage + i;
                    if (trailIndex < currentTrails.size()) {
                        Trail trail = currentTrails.get(trailIndex);
                        handleTrailClick(trail, event);
                    }
                    break;
                }
            }
        }
    }
    
    private void handleTrailClick(Trail trail, InventoryClickEvent event) {
        if (event.isShiftClick()) {
            // Delete trail
            if (trailManager.canPlayerEditTrail(player, trail)) {
                openDeleteConfirmation(trail);
            } else {
                player.sendMessage(Component.text("§cNie masz uprawnień do usunięcia tego szlaku!", NamedTextColor.RED));
            }
        } else if (event.isRightClick()) {
            // Quick actions menu
            openQuickActionsMenu(trail);
        } else {
            // Edit trail
            openTrailEditor(trail);
        }
    }
    
    private void openFilterMenu() {
        // TODO: Implement filter selection menu
        player.sendMessage(Component.text("§eMenu filtrów zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void openSortMenu() {
        // TODO: Implement sort selection menu
        player.sendMessage(Component.text("§eMenu sortowania zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void openSearchMenu() {
        // TODO: Implement search input (using anvil GUI or chat)
        player.sendMessage(Component.text("§eWyszukiwanie zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void openSettingsMenu() {
        new TrailConfigGUI(plugin, player, this).open();
    }
    
    private void openQuickActionsMenu(Trail trail) {
        // TODO: Implement quick actions menu
        player.sendMessage(Component.text("§eMenu szybkich akcji zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    private void openDeleteConfirmation(Trail trail) {
        // TODO: Implement delete confirmation dialog
        player.sendMessage(Component.text("§ePotwierdzenie usunięcia zostanie wkrótce dodane!", NamedTextColor.YELLOW));
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Unregister listener when GUI is closed
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }
    
    // Enums for GUI state
    public enum TrailFilterType {
        ALL("Wszystkie"),
        MY_TRAILS("Moje szlaki"),
        PUBLIC_TRAILS("Publiczne"),
        ACTIVE_TRAILS("Aktywne"),
        BY_TYPE("Według typu");
        
        private final String displayName;
        
        TrailFilterType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum SortType {
        NAME("Nazwa"),
        DATE_CREATED("Data utworzenia"),
        PRIORITY("Priorytet"),
        TYPE("Typ");
        
        private final String displayName;
        
        SortType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}