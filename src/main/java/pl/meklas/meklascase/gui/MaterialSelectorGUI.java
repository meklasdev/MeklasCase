package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialSelectorGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final DropEditorGUI parentGUI;
    private final Inventory inventory;
    private int currentPage = 0;
    private MaterialCategory currentCategory = MaterialCategory.ALL;
    
    private static final int MATERIALS_PER_PAGE = 28; // 4 rows of 7 materials
    
    // GUI Layout slots
    private static final int[] MATERIAL_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    // Navigation and category slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int CATEGORY_SLOT = 49;
    private static final int BACK_SLOT = 0;
    private static final int SEARCH_SLOT = 4;
    
    private enum MaterialCategory {
        ALL("Wszystkie Materiały", Material.CHEST),
        BLOCKS("Bloki", Material.STONE),
        ITEMS("Przedmioty", Material.DIAMOND),
        TOOLS("Narzędzia i Bronie", Material.DIAMOND_SWORD),
        ARMOR("Zbroja", Material.DIAMOND_CHESTPLATE),
        FOOD("Jedzenie", Material.BREAD),
        REDSTONE("Redstone", Material.REDSTONE),
        DECORATION("Dekoracje", Material.FLOWER_POT),
        MISC("Różne", Material.EXPERIENCE_BOTTLE);
        
        private final String displayName;
        private final Material icon;
        
        MaterialCategory(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
    }
    
    public MaterialSelectorGUI(MeklasCasePlugin plugin, Player player, DropEditorGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, "§6§lSelektor Materiałów");
        
        setupGUI();
        updateMaterialDisplay();
    }
    
    private void setupGUI() {
        ItemStack borderPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        
        ItemStack backButton = createItem(
            Material.BARRIER,
            "§c§lPowrót",
            Arrays.asList("§7Powróć do edytora dropów")
        );
        inventory.setItem(BACK_SLOT, backButton);
        
        ItemStack searchButton = createItem(
            Material.COMPASS,
            "§a§lSzukaj Materiałów",
            Arrays.asList(
                "§7Szukaj konkretnych materiałów",
                "",
                "§8Kliknij aby szukać po nazwie"
            )
        );
        inventory.setItem(SEARCH_SLOT, searchButton);
        
        updateCategoryButton();
        
        ItemStack prevPage = createItem(
            Material.ARROW,
            "§a§lPoprzednia Strona",
            Arrays.asList("§8Kliknij aby przejść do poprzedniej strony")
        );
        inventory.setItem(PREV_PAGE_SLOT, prevPage);
        
        ItemStack nextPage = createItem(
            Material.ARROW,
            "§a§lNastępna Strona",
            Arrays.asList("§8Kliknij aby przejść do następnej strony")
        );
        inventory.setItem(NEXT_PAGE_SLOT, nextPage);
    }
    
    private void updateCategoryButton() {
        ItemStack categoryButton = createItem(
            currentCategory.getIcon(),
            "§e§lKategoria: " + currentCategory.getDisplayName(),
            Arrays.asList(
                "§7Obecna kategoria: §e" + currentCategory.getDisplayName(),
                "",
                "§8Kliknij aby zmienić kategorię"
            )
        );
        inventory.setItem(CATEGORY_SLOT, categoryButton);
    }
    
    private void updateMaterialDisplay() {
        // Clear material slots
        for (int slot : MATERIAL_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        List<Material> materials = getFilteredMaterials();
        int totalPages = (int) Math.ceil((double) materials.size() / MATERIALS_PER_PAGE);
        
        // Update navigation buttons
        ItemStack prevPage = inventory.getItem(PREV_PAGE_SLOT);
        if (prevPage != null && prevPage.hasItemMeta()) {
            ItemMeta meta = prevPage.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Strona: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages));
            lore.add("");
            if (currentPage > 0) {
                lore.add("§8Kliknij aby przejść do poprzedniej strony");
            } else {
                lore.add("§8Brak poprzedniej strony");
            }
            meta.setLore(lore);
            prevPage.setItemMeta(meta);
        }
        
        ItemStack nextPage = inventory.getItem(NEXT_PAGE_SLOT);
        if (nextPage != null && nextPage.hasItemMeta()) {
            ItemMeta meta = nextPage.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Strona: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages));
            lore.add("");
            if (currentPage < totalPages - 1) {
                lore.add("§8Kliknij aby przejść do następnej strony");
            } else {
                lore.add("§8Brak następnej strony");
            }
            meta.setLore(lore);
            nextPage.setItemMeta(meta);
        }
        
        // Display materials for current page
        int startIndex = currentPage * MATERIALS_PER_PAGE;
        int endIndex = Math.min(startIndex + MATERIALS_PER_PAGE, materials.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Material material = materials.get(i);
            int slotIndex = i - startIndex;
            
            if (slotIndex < MATERIAL_SLOTS.length) {
                ItemStack materialItem = createMaterialDisplayItem(material);
                inventory.setItem(MATERIAL_SLOTS[slotIndex], materialItem);
            }
        }
    }
    
    private List<Material> getFilteredMaterials() {
        return Arrays.stream(Material.values())
            .filter(material -> material.isItem() && !material.isAir())
            .filter(this::matchesCategory)
            .sorted(Comparator.comparing(Material::name))
            .collect(Collectors.toList());
    }
    
    private boolean matchesCategory(Material material) {
        if (currentCategory == MaterialCategory.ALL) {
            return true;
        }
        
        String name = material.name().toLowerCase();
        
        switch (currentCategory) {
            case BLOCKS:
                return material.isBlock() && material.isSolid();
            case ITEMS:
                return !material.isBlock() && !isToolOrWeapon(material) && !isArmor(material) && !isFood(material);
            case TOOLS:
                return isToolOrWeapon(material);
            case ARMOR:
                return isArmor(material);
            case FOOD:
                return isFood(material);
            case REDSTONE:
                return name.contains("redstone") || name.contains("piston") || name.contains("dispenser") 
                    || name.contains("hopper") || name.contains("comparator") || name.contains("repeater");
            case DECORATION:
                return name.contains("flower") || name.contains("pot") || name.contains("banner") 
                    || name.contains("carpet") || name.contains("painting");
            case MISC:
                return !matchesCategory(material, MaterialCategory.BLOCKS) 
                    && !matchesCategory(material, MaterialCategory.TOOLS)
                    && !matchesCategory(material, MaterialCategory.ARMOR)
                    && !matchesCategory(material, MaterialCategory.FOOD)
                    && !matchesCategory(material, MaterialCategory.REDSTONE)
                    && !matchesCategory(material, MaterialCategory.DECORATION);
            default:
                return true;
        }
    }
    
    private boolean matchesCategory(Material material, MaterialCategory category) {
        MaterialCategory oldCategory = currentCategory;
        currentCategory = category;
        boolean result = matchesCategory(material);
        currentCategory = oldCategory;
        return result;
    }
    
    private boolean isToolOrWeapon(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("sword") || name.contains("axe") || name.contains("pickaxe") 
            || name.contains("shovel") || name.contains("hoe") || name.contains("bow") 
            || name.contains("crossbow") || name.contains("trident");
    }
    
    private boolean isArmor(Material material) {
        String name = material.name().toLowerCase();
        return name.contains("helmet") || name.contains("chestplate") || name.contains("leggings") 
            || name.contains("boots") || name.contains("elytra") || name.contains("shield");
    }
    
    private boolean isFood(Material material) {
        return material.isEdible();
    }
    
    private ItemStack createMaterialDisplayItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§l" + formatMaterialName(material));
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Materiał: §e" + material.name());
            lore.add("§7Typ: §e" + (material.isBlock() ? "Blok" : "Przedmiot"));
            
            if (material.getMaxStackSize() != 64) {
                lore.add("§7Maks Stos: §e" + material.getMaxStackSize());
            }
            
            if (material == parentGUI.getCurrentMaterial()) {
                lore.add("");
                lore.add("§a§l✓ Obecnie Wybrany");
            }
            
            lore.add("");
            lore.add("§8Kliknij aby wybrać ten materiał");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String formatMaterialName(Material material) {
        return Arrays.stream(material.name().split("_"))
            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        
        if (slot == BACK_SLOT) {
            parentGUI.open();
            
        } else if (slot == SEARCH_SLOT) {
            MessageUtils.sendMessage(player, "§eSzukanie materiałów wkrótce! Na razie użyj filtra kategorii.");
            
        } else if (slot == CATEGORY_SLOT) {
            MaterialCategory[] categories = MaterialCategory.values();
            int currentIndex = Arrays.asList(categories).indexOf(currentCategory);
            currentCategory = categories[(currentIndex + 1) % categories.length];
            currentPage = 0;
            updateCategoryButton();
            updateMaterialDisplay();
            
        } else if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            updateMaterialDisplay();
            
        } else if (slot == NEXT_PAGE_SLOT) {
            List<Material> materials = getFilteredMaterials();
            int totalPages = (int) Math.ceil((double) materials.size() / MATERIALS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateMaterialDisplay();
            }
            
        } else {
            for (int i = 0; i < MATERIAL_SLOTS.length; i++) {
                if (MATERIAL_SLOTS[i] == slot) {
                    List<Material> materials = getFilteredMaterials();
                    int materialIndex = currentPage * MATERIALS_PER_PAGE + i;
                    
                    if (materialIndex < materials.size()) {
                        Material selectedMaterial = materials.get(materialIndex);
                        parentGUI.setCurrentMaterial(selectedMaterial);
                        MessageUtils.sendMessage(player, "§aMateriał wybrany: §e" + formatMaterialName(selectedMaterial));
                        parentGUI.open();
                    }
                    break;
                }
            }
        }
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}