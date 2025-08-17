package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.CaseItem;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropManagementGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final Case caseObj;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 21; // 3 rows of 7 items
    
    // GUI Layout slots
    private static final int[] DROP_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    
    // Navigation slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int ADD_DROP_SLOT = 49;
    private static final int CASE_INFO_SLOT = 4;
    private static final int BACK_SLOT = 0;
    
    public DropManagementGUI(MeklasCasePlugin plugin, Player player, Case caseObj) {
        this.plugin = plugin;
        this.player = player;
        this.caseObj = caseObj;
        this.inventory = Bukkit.createInventory(this, 54, "§6§lDrop Management: §e" + caseObj.getName());
        
        setupGUI();
        updateDropsDisplay();
    }
    
    private void setupGUI() {
        // Fill borders with glass panes
        ItemStack borderPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        
        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            if (i != CASE_INFO_SLOT) inventory.setItem(i, borderPane);
            inventory.setItem(45 + i, borderPane);
        }
        
        // Side borders
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, borderPane);
            inventory.setItem(i + 8, borderPane);
        }
        
        // Case info
        ItemStack caseInfo = createItem(
            caseObj.getKeyItem().getType(),
            "§6§lCase Information",
            Arrays.asList(
                "§7Name: §e" + caseObj.getName(),
                "§7Type: §e" + caseObj.getType(),
                "§7Total Drops: §e" + caseObj.getItems().size(),
                "§7Status: " + (caseObj.isEnabled() ? "§aEnabled" : "§cDisabled"),
                "",
                "§8Left click to toggle status"
            )
        );
        inventory.setItem(CASE_INFO_SLOT, caseInfo);
        
        // Navigation buttons
        ItemStack prevPage = createItem(
            Material.ARROW,
            "§a§lPrevious Page",
            Arrays.asList("§7Page: §e" + (currentPage + 1), "", "§8Click to go to previous page")
        );
        inventory.setItem(PREV_PAGE_SLOT, prevPage);
        
        ItemStack nextPage = createItem(
            Material.ARROW,
            "§a§lNext Page",
            Arrays.asList("§7Page: §e" + (currentPage + 1), "", "§8Click to go to next page")
        );
        inventory.setItem(NEXT_PAGE_SLOT, nextPage);
        
        // Add drop button
        ItemStack addDrop = createItem(
            Material.EMERALD,
            "§a§lAdd New Drop",
            Arrays.asList(
                "§7Click to add a new drop",
                "§7to this case",
                "",
                "§8Left click to add drop"
            )
        );
        inventory.setItem(ADD_DROP_SLOT, addDrop);
        
        // Back button
        ItemStack backButton = createItem(
            Material.BARRIER,
            "§c§lBack",
            Arrays.asList("§7Return to case selection")
        );
        inventory.setItem(BACK_SLOT, backButton);
    }
    
    private void updateDropsDisplay() {
        // Clear drop slots
        for (int slot : DROP_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        List<CaseItem> items = caseObj.getItems();
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        
        // Update navigation buttons
        ItemStack prevPage = inventory.getItem(PREV_PAGE_SLOT);
        if (prevPage != null && prevPage.hasItemMeta()) {
            ItemMeta meta = prevPage.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Page: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages));
            lore.add("");
            if (currentPage > 0) {
                lore.add("§8Click to go to previous page");
            } else {
                lore.add("§8No previous page");
            }
            meta.setLore(lore);
            prevPage.setItemMeta(meta);
        }
        
        ItemStack nextPage = inventory.getItem(NEXT_PAGE_SLOT);
        if (nextPage != null && nextPage.hasItemMeta()) {
            ItemMeta meta = nextPage.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Page: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages));
            lore.add("");
            if (currentPage < totalPages - 1) {
                lore.add("§8Click to go to next page");
            } else {
                lore.add("§8No next page");
            }
            meta.setLore(lore);
            nextPage.setItemMeta(meta);
        }
        
        // Display drops for current page
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            CaseItem item = items.get(i);
            int slotIndex = i - startIndex;
            
            if (slotIndex < DROP_SLOTS.length) {
                ItemStack displayItem = createDropDisplayItem(item, i);
                inventory.setItem(DROP_SLOTS[slotIndex], displayItem);
            }
        }
    }
    
    private ItemStack createDropDisplayItem(CaseItem caseItem, int index) {
        ItemStack displayItem = caseItem.createItemStack();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            
            // Original lore
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
                lore.add("");
            }
            
            // Drop information
            lore.add("§6§lDrop Information:");
            lore.add("§7Index: §e#" + (index + 1));
            lore.add("§7Material: §e" + caseItem.getMaterial().name());
            lore.add("§7Amount: §e" + caseItem.getAmount());
            lore.add("§7Weight: §e" + String.format("%.2f", caseItem.getWeight()));
            lore.add("§7Glow: " + (caseItem.isGlow() ? "§aYes" : "§cNo"));
            
            if (caseItem.getEnchantments() != null && !caseItem.getEnchantments().isEmpty()) {
                lore.add("§7Enchantments: §e" + caseItem.getEnchantments().size());
            }
            
            // Chance calculation
            double totalWeight = caseObj.getItems().stream().mapToDouble(CaseItem::getWeight).sum();
            double chance = (caseItem.getWeight() / totalWeight) * 100;
            lore.add("§7Chance: §e" + String.format("%.2f%%", chance));
            
            lore.add("");
            lore.add("§8Left click: Edit drop");
            lore.add("§8Right click: Delete drop");
            lore.add("§8Shift+Left: Duplicate drop");
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        return displayItem;
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
        
        if (slot == CASE_INFO_SLOT) {
            // Toggle case enabled status
            caseObj.setEnabled(!caseObj.isEnabled());
            plugin.getCaseManager().saveCase(caseObj);
            setupGUI();
            updateDropsDisplay();
            MessageUtils.sendMessage(player, caseObj.isEnabled() ? 
                "§aCase enabled!" : "§cCase disabled!");
            
        } else if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            updateDropsDisplay();
            
        } else if (slot == NEXT_PAGE_SLOT) {
            int totalPages = (int) Math.ceil((double) caseObj.getItems().size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateDropsDisplay();
            }
            
        } else if (slot == ADD_DROP_SLOT) {
            // Open drop editor for new drop
            new DropEditorGUI(plugin, player, caseObj, null, this).open();
            
        } else if (slot == BACK_SLOT) {
            // Open case selector
            new CaseSelectorGUI(plugin, player).open();
            
        } else {
            // Check if clicked on a drop slot
            for (int i = 0; i < DROP_SLOTS.length; i++) {
                if (DROP_SLOTS[i] == slot) {
                    int itemIndex = currentPage * ITEMS_PER_PAGE + i;
                    if (itemIndex < caseObj.getItems().size()) {
                        CaseItem clickedItem = caseObj.getItems().get(itemIndex);
                        
                        if (clickType == ClickType.LEFT) {
                            // Edit drop
                            new DropEditorGUI(plugin, player, caseObj, clickedItem, this).open();
                            
                        } else if (clickType == ClickType.RIGHT) {
                            // Delete drop
                            caseObj.getItems().remove(itemIndex);
                            plugin.getCaseManager().saveCase(caseObj);
                            
                            // Adjust current page if necessary
                            int totalPages = (int) Math.ceil((double) caseObj.getItems().size() / ITEMS_PER_PAGE);
                            if (currentPage >= totalPages && currentPage > 0) {
                                currentPage--;
                            }
                            
                            updateDropsDisplay();
                            MessageUtils.sendMessage(player, "§cDrop deleted!");
                            
                        } else if (clickType == ClickType.SHIFT_LEFT) {
                            // Duplicate drop
                            CaseItem duplicate = new CaseItem(
                                clickedItem.getMaterial(),
                                clickedItem.getAmount(),
                                clickedItem.getWeight(),
                                clickedItem.getDisplayName(),
                                clickedItem.getLore(),
                                clickedItem.isGlow(),
                                clickedItem.getEnchantments()
                            );
                            caseObj.getItems().add(duplicate);
                            plugin.getCaseManager().saveCase(caseObj);
                            updateDropsDisplay();
                            MessageUtils.sendMessage(player, "§aDrop duplicated!");
                        }
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