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

import java.util.*;
import java.util.stream.Collectors;

public class TrailCreatorGUI implements InventoryHolder, Listener {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final TrailManager trailManager;
    private final TrailManagementGUI parentGUI;
    private final Inventory inventory;
    
    // Trail creation data
    private String trailName = "";
    private String trailDescription = "";
    private TrailType selectedType = TrailType.WALKING;
    private String selectedCategory = "Default";
    private int selectedPriority = 0;
    private boolean isPublic = false;
    private boolean isActive = false;
    
    // GUI State
    private CreationStep currentStep = CreationStep.NAME;
    private int typeSelectionPage = 0;
    
    // Layout constants
    private static final int[] TYPE_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    };
    
    // Control buttons
    private static final int BACK_SLOT = 45;
    private static final int NEXT_SLOT = 53;
    private static final int PREV_SLOT = 47;
    private static final int CREATE_SLOT = 49;
    private static final int STEP_INFO_SLOT = 4;
    
    public TrailCreatorGUI(MeklasCasePlugin plugin, Player player, TrailManagementGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.trailManager = plugin.getTrailManager();
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, 
            Component.text("Kreator Szlaku", NamedTextColor.GREEN, TextDecoration.BOLD));
        
        setupGUI();
        
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        inventory.clear();
        
        // Fill background
        fillBackground();
        
        // Add step info
        addStepInfo();
        
        // Add content based on current step
        switch (currentStep) {
            case NAME:
                addNameStep();
                break;
            case TYPE:
                addTypeStep();
                break;
            case DESCRIPTION:
                addDescriptionStep();
                break;
            case CATEGORY:
                addCategoryStep();
                break;
            case SETTINGS:
                addSettingsStep();
                break;
            case SUMMARY:
                addSummaryStep();
                break;
        }
        
        // Add navigation buttons
        addNavigationButtons();
    }
    
    private void fillBackground() {
        ItemStack background = createItem(Material.GREEN_STAINED_GLASS_PANE, " ");
        
        // Fill borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, background);
        }
        for (int i = 45; i < 54; i++) {
            if (i != BACK_SLOT && i != NEXT_SLOT && i != PREV_SLOT && i != CREATE_SLOT) {
                inventory.setItem(i, background);
            }
        }
        
        // Fill sides
        for (int row = 1; row < 5; row++) {
            inventory.setItem(row * 9, background);
            inventory.setItem(row * 9 + 8, background);
        }
    }
    
    private void addStepInfo() {
        inventory.setItem(STEP_INFO_SLOT, createItem(
            Material.BOOK,
            "§6📋 Krok " + (currentStep.ordinal() + 1) + "/6: " + currentStep.getDisplayName(),
            "§7Aktualny krok: §e" + currentStep.getDisplayName(),
            "§7Postęp: §e" + (currentStep.ordinal() + 1) + " / 6",
            "",
            "§7Kroki tworzenia szlaku:",
            "§8• §7Nazwa szlaku",
            "§8• §7Typ szlaku",
            "§8• §7Opis szlaku",
            "§8• §7Kategoria",
            "§8• §7Ustawienia",
            "§8• §7Podsumowanie"
        ));
    }
    
    private void addNameStep() {
        inventory.setItem(22, createItem(
            Material.NAME_TAG,
            "§b📝 Nazwa szlaku",
            trailName.isEmpty() ? "§7Nie wprowadzono nazwy" : "§7Aktualna nazwa: §e" + trailName,
            "",
            "§7Wprowadź nazwę dla nowego szlaku.",
            "§7Nazwa powinna być unikalna i opisowa.",
            "",
            "§eKliknij aby wprowadzić nazwę",
            "§7(Zostaniesz poproszony o wpisanie w chacie)"
        ));
        
        // Show example names
        inventory.setItem(19, createItem(
            Material.PAPER,
            "§a💡 Przykłady nazw",
            "§7Przykładowe nazwy szlaków:",
            "§8• §fSzlak Górski Zachód",
            "§8• §fTrasa Rowerowa Park",
            "§8• §fSzlak Edukacyjny Las",
            "§8• §fDroga Pielgrzymkowa"
        ));
        
        inventory.setItem(25, createItem(
            Material.WRITABLE_BOOK,
            "§c📋 Zasady nazewnictwa",
            "§7Zasady tworzenia nazw:",
            "§8• §7Długość: 3-32 znaki",
            "§8• §7Dozwolone znaki: litery, cyfry, spacje",
            "§8• §7Nazwa musi być unikalna",
            "§8• §7Unikaj znaków specjalnych"
        ));
    }
    
    private void addTypeStep() {
        TrailType[] types = TrailType.values();
        int startIndex = typeSelectionPage * TYPE_SLOTS.length;
        int endIndex = Math.min(startIndex + TYPE_SLOTS.length, types.length);
        
        for (int i = startIndex; i < endIndex; i++) {
            TrailType type = types[i];
            int slot = TYPE_SLOTS[i - startIndex];
            
            boolean isSelected = type == selectedType;
            ItemStack item = createTypeItem(type, isSelected);
            inventory.setItem(slot, item);
        }
        
        // Type selection info
        inventory.setItem(31, createItem(
            selectedType.getIcon(),
            "§b🏷 Wybrany typ",
            "§7Aktualnie wybrany: §e" + selectedType.getDisplayName(),
            "§7Opis: §f" + selectedType.getDescription(),
            "",
            "§7Kliknij na typ powyżej aby zmienić wybór"
        ));
    }
    
    private ItemStack createTypeItem(TrailType type, boolean isSelected) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Opis: §f" + type.getDescription());
        lore.add("§7Domyślne cząsteczki: §e" + type.getDefaultParticle().name());
        lore.add("");
        
        if (isSelected) {
            lore.add("§a✓ Aktualnie wybrany");
        } else {
            lore.add("§7Kliknij aby wybrać");
        }
        
        return createItem(
            isSelected ? Material.ENCHANTED_BOOK : type.getIcon(),
            (isSelected ? "§a✓ " : "§7") + type.getDisplayName(),
            lore.toArray(new String[0])
        );
    }
    
    private void addDescriptionStep() {
        inventory.setItem(22, createItem(
            Material.WRITABLE_BOOK,
            "§b📖 Opis szlaku",
            trailDescription.isEmpty() ? "§7Nie wprowadzono opisu" : "§7Aktualny opis:",
            trailDescription.isEmpty() ? "" : "§f" + trailDescription,
            "",
            "§7Wprowadź opis dla szlaku (opcjonalne).",
            "§7Opis pomoże innym zrozumieć cel szlaku.",
            "",
            "§eKliknij aby wprowadzić opis",
            "§7(Zostaniesz poproszony o wpisanie w chacie)",
            "",
            "§cShift + klik aby pominąć"
        ));
        
        // Description tips
        inventory.setItem(19, createItem(
            Material.BOOK,
            "§a💡 Wskazówki",
            "§7Wskazówki dla opisu:",
            "§8• §7Opisz cel szlaku",
            "§8• §7Wspomniej o trudności",
            "§8• §7Podaj czas przejścia",
            "§8• §7Wymień atrakcje po drodze"
        ));
        
        inventory.setItem(25, createItem(
            Material.PAPER,
            "§a📝 Przykłady",
            "§7Przykładowe opisy:",
            "§8• §fŁatwy szlak dla początkujących",
            "§8• §fTrudna trasa górska, 4h marszu",
            "§8• §fSzlak edukacyjny przez las",
            "§8• §fTrasa z pięknymi widokami"
        ));
    }
    
    private void addCategoryStep() {
        Set<String> existingCategories = trailManager.getAllCategories();
        List<String> categories = new ArrayList<>(existingCategories);
        if (!categories.contains("Default")) {
            categories.add(0, "Default");
        }
        
        // Show existing categories
        int slot = 10;
        for (String category : categories) {
            if (slot > 25) break;
            
            boolean isSelected = category.equals(selectedCategory);
            inventory.setItem(slot, createItem(
                isSelected ? Material.ENCHANTED_BOOK : Material.CHEST,
                (isSelected ? "§a✓ " : "§7") + category,
                "§7Kategoria: §e" + category,
                "",
                isSelected ? "§a✓ Aktualnie wybrana" : "§7Kliknij aby wybrać"
            ));
            slot++;
        }
        
        // New category option
        inventory.setItem(31, createItem(
            Material.WRITABLE_BOOK,
            "§b➕ Nowa kategoria",
            "§7Utwórz nową kategorię",
            "",
            "§eKliknij aby utworzyć nową kategorię"
        ));
        
        // Selected category info
        inventory.setItem(40, createItem(
            Material.CHEST,
            "§b📂 Wybrana kategoria",
            "§7Aktualnie wybrana: §e" + selectedCategory,
            "",
            "§7Kategorie pomagają organizować szlaki"
        ));
    }
    
    private void addSettingsStep() {
        // Priority setting
        inventory.setItem(10, createItem(
            Material.EXPERIENCE_BOTTLE,
            "§b⭐ Priorytet",
            "§7Aktualny priorytet: §e" + selectedPriority,
            "",
            "§7Priorytet określa kolejność wyświetlania",
            "§7Wyższy priorytet = wyższa pozycja",
            "",
            "§eKliknij aby zmienić priorytet"
        ));
        
        // Public setting
        inventory.setItem(11, createItem(
            isPublic ? Material.EMERALD : Material.REDSTONE,
            "§b🌐 Publiczny dostęp",
            "§7Status: " + (isPublic ? "§aPubliczny" : "§cPrywatny"),
            "",
            "§7Publiczne szlaki są widoczne dla wszystkich",
            "§7Prywatne szlaki tylko dla właściciela",
            "",
            "§eKliknij aby przełączyć"
        ));
        
        // Active setting
        inventory.setItem(12, createItem(
            isActive ? Material.LIME_DYE : Material.RED_DYE,
            "§b⚡ Status aktywności",
            "§7Status: " + (isActive ? "§aAktywny" : "§cNieaktywny"),
            "",
            "§7Aktywne szlaki są włączone do użycia",
            "§7Nieaktywne szlaki są wyłączone",
            "",
            "§eKliknij aby przełączyć"
        ));
        
        // Settings summary
        inventory.setItem(22, createItem(
            Material.COMPARATOR,
            "§6⚙ Podsumowanie ustawień",
            "§7Priorytet: §e" + selectedPriority,
            "§7Publiczny: " + (isPublic ? "§aTak" : "§cNie"),
            "§7Aktywny: " + (isActive ? "§aTak" : "§cNie"),
            "",
            "§7Te ustawienia można zmienić później"
        ));
    }
    
    private void addSummaryStep() {
        // Trail summary
        inventory.setItem(13, createItem(
            selectedType.getIcon(),
            "§a📋 Podsumowanie szlaku",
            "§7Nazwa: §e" + trailName,
            "§7Typ: §e" + selectedType.getDisplayName(),
            "§7Kategoria: §e" + selectedCategory,
            "§7Opis: " + (trailDescription.isEmpty() ? "§cBrak" : "§f" + trailDescription),
            "",
            "§7Ustawienia:",
            "§8• §7Priorytet: §e" + selectedPriority,
            "§8• §7Publiczny: " + (isPublic ? "§aTak" : "§cNie"),
            "§8• §7Aktywny: " + (isActive ? "§aTak" : "§cNie"),
            "",
            "§aGotowy do utworzenia!"
        ));
        
        // Creation info
        inventory.setItem(31, createItem(
            Material.INFORMATION_BOOK,
            "§b📚 Co dalej?",
            "§7Po utworzeniu szlaku:",
            "§8• §7Będziesz mógł dodać punkty",
            "§8• §7Skonfigurować efekty wizualne",
            "§8• §7Ustawić automatyzację",
            "§8• §7Zarządzać uprawnieniami",
            "",
            "§7Szlak zostanie utworzony bez punktów"
        ));
    }
    
    private void addNavigationButtons() {
        // Back button
        inventory.setItem(BACK_SLOT, createItem(
            Material.ARROW,
            "§c◀ Anuluj",
            "§7Kliknij aby anulować tworzenie",
            "§7Powrót do listy szlaków"
        ));
        
        // Previous step
        if (currentStep.ordinal() > 0) {
            inventory.setItem(PREV_SLOT, createItem(
                Material.ARROW,
                "§a◀ Poprzedni krok",
                "§7Kliknij aby wrócić do poprzedniego kroku"
            ));
        }
        
        // Next step or create
        if (currentStep == CreationStep.SUMMARY) {
            inventory.setItem(CREATE_SLOT, createItem(
                canCreate() ? Material.EMERALD : Material.RED_DYE,
                canCreate() ? "§a✓ Utwórz szlak" : "§c✗ Nie można utworzyć",
                canCreate() ? "§7Kliknij aby utworzyć szlak" : "§7Uzupełnij wymagane pola",
                "",
                canCreate() ? "§aWszystkie wymagane dane zostały podane" : getValidationErrors()
            ));
        } else {
            inventory.setItem(NEXT_SLOT, createItem(
                canProceedToNext() ? Material.ARROW : Material.GRAY_DYE,
                canProceedToNext() ? "§aNastępny krok ▶" : "§7Następny krok ▶",
                canProceedToNext() ? "§7Kliknij aby przejść dalej" : "§7Uzupełnij wymagane dane"
            ));
        }
    }
    
    private boolean canProceedToNext() {
        switch (currentStep) {
            case NAME:
                return !trailName.isEmpty() && trailName.length() >= 3;
            case TYPE:
                return selectedType != null;
            case DESCRIPTION:
                return true; // Description is optional
            case CATEGORY:
                return !selectedCategory.isEmpty();
            case SETTINGS:
                return true; // Settings have defaults
            default:
                return true;
        }
    }
    
    private boolean canCreate() {
        return !trailName.isEmpty() && 
               trailName.length() >= 3 && 
               selectedType != null && 
               !selectedCategory.isEmpty();
    }
    
    private String getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (trailName.isEmpty()) {
            errors.add("§c• Brak nazwy szlaku");
        } else if (trailName.length() < 3) {
            errors.add("§c• Nazwa zbyt krótka (min. 3 znaki)");
        }
        
        if (selectedType == null) {
            errors.add("§c• Nie wybrano typu szlaku");
        }
        
        if (selectedCategory.isEmpty()) {
            errors.add("§c• Nie wybrano kategorii");
        }
        
        return String.join("\n", errors);
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
    
    public void nextStep() {
        if (canProceedToNext() && currentStep.ordinal() < CreationStep.values().length - 1) {
            currentStep = CreationStep.values()[currentStep.ordinal() + 1];
            setupGUI();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
    
    public void previousStep() {
        if (currentStep.ordinal() > 0) {
            currentStep = CreationStep.values()[currentStep.ordinal() - 1];
            setupGUI();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
    
    public void createTrail() {
        if (!canCreate()) {
            player.sendMessage(Component.text("§cNie można utworzyć szlaku! Sprawdź wymagane pola.", NamedTextColor.RED));
            return;
        }
        
        // Check if name is unique
        if (trailManager.getTrailByName(trailName) != null) {
            player.sendMessage(Component.text("§cSzlak o tej nazwie już istnieje!", NamedTextColor.RED));
            return;
        }
        
        try {
            Trail trail = trailManager.createTrail(trailName, selectedType, player);
            trail.setDescription(trailDescription);
            trail.setCategory(selectedCategory);
            trail.setPriority(selectedPriority);
            trail.setPublicTrail(isPublic);
            trail.setActive(isActive);
            
            trailManager.updateTrail(trail);
            
            player.sendMessage(Component.text("§aSzlak '" + trailName + "' został utworzony pomyślnie!", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            
            // Open trail editor
            new TrailEditorGUI(plugin, player, trail, parentGUI).open();
            
        } catch (Exception e) {
            player.sendMessage(Component.text("§cBłąd podczas tworzenia szlaku: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error creating trail: " + e.getMessage());
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
        
        // Handle navigation
        if (slot == BACK_SLOT) {
            parentGUI.open();
        } else if (slot == NEXT_SLOT && canProceedToNext()) {
            nextStep();
        } else if (slot == PREV_SLOT) {
            previousStep();
        } else if (slot == CREATE_SLOT && canCreate()) {
            createTrail();
        } else {
            // Handle step-specific clicks
            handleStepClick(slot, event);
        }
    }
    
    private void handleStepClick(int slot, InventoryClickEvent event) {
        switch (currentStep) {
            case NAME:
                if (slot == 22) {
                    requestNameInput();
                }
                break;
            case TYPE:
                handleTypeSelection(slot);
                break;
            case DESCRIPTION:
                if (slot == 22) {
                    if (event.isShiftClick()) {
                        trailDescription = "";
                        nextStep();
                    } else {
                        requestDescriptionInput();
                    }
                }
                break;
            case CATEGORY:
                handleCategorySelection(slot, event);
                break;
            case SETTINGS:
                handleSettingsClick(slot);
                break;
        }
    }
    
    private void handleTypeSelection(int slot) {
        for (int i = 0; i < TYPE_SLOTS.length; i++) {
            if (slot == TYPE_SLOTS[i]) {
                int typeIndex = typeSelectionPage * TYPE_SLOTS.length + i;
                TrailType[] types = TrailType.values();
                if (typeIndex < types.length) {
                    selectedType = types[typeIndex];
                    setupGUI();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                }
                break;
            }
        }
    }
    
    private void handleCategorySelection(int slot, InventoryClickEvent event) {
        if (slot == 31) {
            requestNewCategoryInput();
        } else {
            // Handle existing category selection
            Set<String> existingCategories = trailManager.getAllCategories();
            List<String> categories = new ArrayList<>(existingCategories);
            if (!categories.contains("Default")) {
                categories.add(0, "Default");
            }
            
            int categoryIndex = slot - 10;
            if (categoryIndex >= 0 && categoryIndex < categories.size()) {
                selectedCategory = categories.get(categoryIndex);
                setupGUI();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }
    
    private void handleSettingsClick(int slot) {
        switch (slot) {
            case 10: // Priority
                requestPriorityInput();
                break;
            case 11: // Public
                isPublic = !isPublic;
                setupGUI();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                break;
            case 12: // Active
                isActive = !isActive;
                setupGUI();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                break;
        }
    }
    
    private void requestNameInput() {
        player.closeInventory();
        player.sendMessage(Component.text("§eWpisz nazwę szlaku w chacie:", NamedTextColor.YELLOW));
        // TODO: Implement chat input handler
    }
    
    private void requestDescriptionInput() {
        player.closeInventory();
        player.sendMessage(Component.text("§eWpisz opis szlaku w chacie:", NamedTextColor.YELLOW));
        // TODO: Implement chat input handler
    }
    
    private void requestNewCategoryInput() {
        player.closeInventory();
        player.sendMessage(Component.text("§eWpisz nazwę nowej kategorii w chacie:", NamedTextColor.YELLOW));
        // TODO: Implement chat input handler
    }
    
    private void requestPriorityInput() {
        player.closeInventory();
        player.sendMessage(Component.text("§eWpisz priorytet (liczba całkowita) w chacie:", NamedTextColor.YELLOW));
        // TODO: Implement chat input handler
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            // Unregister listener when GUI is closed
            InventoryClickEvent.getHandlerList().unregister(this);
        }
    }
    
    public enum CreationStep {
        NAME("Nazwa"),
        TYPE("Typ"),
        DESCRIPTION("Opis"),
        CATEGORY("Kategoria"),
        SETTINGS("Ustawienia"),
        SUMMARY("Podsumowanie");
        
        private final String displayName;
        
        CreationStep(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}