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
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.CaseItem;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.*;

public class CaseSelectorGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int CASES_PER_PAGE = 28; // 4 rows of 7 cases
    
    // GUI Layout slots
    private static final int[] CASE_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    // Navigation slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int INFO_SLOT = 4;
    private static final int CREATE_CASE_SLOT = 49;
    
    public CaseSelectorGUI(MeklasCasePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "§6§lCase Management - Select Case");
        
        setupGUI();
        updateCaseDisplay();
    }
    
    private void setupGUI() {
        // Fill borders with glass panes
        ItemStack borderPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        
        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            if (i != INFO_SLOT) inventory.setItem(i, borderPane);
            inventory.setItem(45 + i, borderPane);
        }
        
        // Side borders
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, borderPane);
            inventory.setItem(i + 8, borderPane);
        }
        
        // Info item
        Collection<Case> allCases = plugin.getCaseManager().getAllCases();
        ItemStack info = createItem(
            Material.INFORMATION_BOOK,
            "§6§lCase Management",
            Arrays.asList(
                "§7Select a case to manage its drops",
                "§7Total Cases: §e" + allCases.size(),
                "§7Active Cases: §e" + allCases.stream().mapToInt(c -> c.isEnabled() ? 1 : 0).sum(),
                "",
                "§8Click on a case to manage its drops"
            )
        );
        inventory.setItem(INFO_SLOT, info);
        
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
        
        // Create case button (if needed)
        ItemStack createCase = createItem(
            Material.EMERALD_BLOCK,
            "§a§lCreate New Case",
            Arrays.asList(
                "§7Create a new case",
                "",
                "§8Click to create new case"
            )
        );
        inventory.setItem(CREATE_CASE_SLOT, createCase);
    }
    
    private void updateCaseDisplay() {
        // Clear case slots
        for (int slot : CASE_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        Collection<Case> allCases = plugin.getCaseManager().getAllCases();
        List<Case> caseList = new ArrayList<>(allCases);
        
        // Sort cases alphabetically
        caseList.sort(Comparator.comparing(Case::getName));
        
        int totalPages = (int) Math.ceil((double) caseList.size() / CASES_PER_PAGE);
        
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
        
        // Display cases for current page
        int startIndex = currentPage * CASES_PER_PAGE;
        int endIndex = Math.min(startIndex + CASES_PER_PAGE, caseList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Case caseObj = caseList.get(i);
            int slotIndex = i - startIndex;
            
            if (slotIndex < CASE_SLOTS.length) {
                ItemStack caseDisplay = createCaseDisplayItem(caseObj);
                inventory.setItem(CASE_SLOTS[slotIndex], caseDisplay);
            }
        }
    }
    
    private ItemStack createCaseDisplayItem(Case caseObj) {
        // Use the case's key item as the display item
        ItemStack displayItem = caseObj.getKeyItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta != null) {
            // Override display name
            meta.setDisplayName("§6§l" + caseObj.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §e" + caseObj.getType().name());
            lore.add("§7Status: " + (caseObj.isEnabled() ? "§aEnabled" : "§cDisabled"));
            lore.add("§7Total Drops: §e" + caseObj.getItems().size());
            
            if (!caseObj.getItems().isEmpty()) {
                // Calculate total weight and show some stats
                double totalWeight = caseObj.getItems().stream().mapToDouble(CaseItem::getWeight).sum();
                lore.add("§7Total Weight: §e" + String.format("%.2f", totalWeight));
                
                // Show rarest drop
                Optional<CaseItem> rarest = caseObj.getItems().stream()
                    .min(Comparator.comparingDouble(CaseItem::getWeight));
                if (rarest.isPresent()) {
                    double rarestChance = (rarest.get().getWeight() / totalWeight) * 100;
                    lore.add("§7Rarest Drop: §e" + String.format("%.2f%%", rarestChance));
                }
            } else {
                lore.add("§8No drops configured");
            }
            
            if (caseObj.getLocation() != null) {
                lore.add("§7Location: §e" + caseObj.getLocation().getWorld().getName() + 
                        " §8(" + caseObj.getLocation().getBlockX() + ", " + 
                        caseObj.getLocation().getBlockY() + ", " + 
                        caseObj.getLocation().getBlockZ() + ")");
            } else {
                lore.add("§7Location: §8Not set");
            }
            
            lore.add("");
            lore.add("§8Left click: Manage drops");
            lore.add("§8Right click: Toggle enabled");
            lore.add("§8Shift+Right: Delete case");
            
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
        
        if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            updateCaseDisplay();
            
        } else if (slot == NEXT_PAGE_SLOT) {
            Collection<Case> allCases = plugin.getCaseManager().getAllCases();
            int totalPages = (int) Math.ceil((double) allCases.size() / CASES_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateCaseDisplay();
            }
            
        } else if (slot == CREATE_CASE_SLOT) {
            // TODO: Implement case creation GUI
            MessageUtils.sendMessage(player, "§eCase creation GUI coming soon!");
            
        } else {
            // Check if clicked on a case slot
            for (int i = 0; i < CASE_SLOTS.length; i++) {
                if (CASE_SLOTS[i] == slot) {
                    Collection<Case> allCases = plugin.getCaseManager().getAllCases();
                    List<Case> caseList = new ArrayList<>(allCases);
                    caseList.sort(Comparator.comparing(Case::getName));
                    
                    int caseIndex = currentPage * CASES_PER_PAGE + i;
                    if (caseIndex < caseList.size()) {
                        Case clickedCase = caseList.get(caseIndex);
                        
                        if (clickType == ClickType.LEFT) {
                            // Open drop management for this case
                            new DropManagementGUI(plugin, player, clickedCase).open();
                            
                        } else if (clickType == ClickType.RIGHT) {
                            // Toggle case enabled status
                            clickedCase.setEnabled(!clickedCase.isEnabled());
                            plugin.getCaseManager().saveCase(clickedCase);
                            updateCaseDisplay();
                            MessageUtils.sendMessage(player, "§aCase " + clickedCase.getName() + 
                                " is now " + (clickedCase.isEnabled() ? "§aenabled" : "§cdisabled") + "§a!");
                            
                        } else if (clickType == ClickType.SHIFT_RIGHT) {
                            // Confirm and delete case
                            if (player.isSneaking()) {
                                plugin.getCaseManager().removeCase(clickedCase.getName());
                                updateCaseDisplay();
                                MessageUtils.sendMessage(player, "§cCase " + clickedCase.getName() + " deleted!");
                            } else {
                                MessageUtils.sendMessage(player, "§eHold shift and right-click again to confirm deletion of case: " + clickedCase.getName());
                            }
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