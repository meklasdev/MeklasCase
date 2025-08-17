package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
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

public class LoreEditorGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final DropEditorGUI parentGUI;
    private final Inventory inventory;
    private final List<String> loreLines;
    private int currentPage = 0;
    
    private static final int LINES_PER_PAGE = 21; // 3 rows of 7 lines
    
    // GUI Layout slots
    private static final int[] LORE_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    
    // Control slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int ADD_LINE_SLOT = 49;
    private static final int BACK_SLOT = 0;
    private static final int SAVE_SLOT = 8;
    private static final int CLEAR_ALL_SLOT = 4;
    
    public LoreEditorGUI(MeklasCasePlugin plugin, Player player, DropEditorGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.loreLines = new ArrayList<>(parentGUI.getCurrentLore());
        this.inventory = Bukkit.createInventory(this, 54, "§6§lLore Editor");
        
        setupGUI();
        updateLoreDisplay();
    }
    
    private void setupGUI() {
        // Fill borders
        ItemStack borderPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderPane);
            }
        }
        
        // Back button
        ItemStack backButton = createItem(
            Material.BARRIER,
            "§c§lBack",
            Arrays.asList("§7Return to drop editor without saving")
        );
        inventory.setItem(BACK_SLOT, backButton);
        
        // Save button
        ItemStack saveButton = createItem(
            Material.EMERALD,
            "§a§lSave & Return",
            Arrays.asList(
                "§7Save lore changes and return",
                "§7to drop editor",
                "",
                "§8Click to save and return"
            )
        );
        inventory.setItem(SAVE_SLOT, saveButton);
        
        // Clear all button
        ItemStack clearButton = createItem(
            Material.TNT,
            "§c§lClear All Lore",
            Arrays.asList(
                "§7Remove all lore lines",
                "",
                "§8Click to clear all lore"
            )
        );
        inventory.setItem(CLEAR_ALL_SLOT, clearButton);
        
        // Add line button
        ItemStack addButton = createItem(
            Material.EMERALD,
            "§a§lAdd Lore Line",
            Arrays.asList(
                "§7Add a new lore line",
                "",
                "§8Click to add new line"
            )
        );
        inventory.setItem(ADD_LINE_SLOT, addButton);
        
        // Navigation buttons
        ItemStack prevPage = createItem(
            Material.ARROW,
            "§a§lPrevious Page",
            Arrays.asList("§8Click to go to previous page")
        );
        inventory.setItem(PREV_PAGE_SLOT, prevPage);
        
        ItemStack nextPage = createItem(
            Material.ARROW,
            "§a§lNext Page",
            Arrays.asList("§8Click to go to next page")
        );
        inventory.setItem(NEXT_PAGE_SLOT, nextPage);
    }
    
    private void updateLoreDisplay() {
        // Clear lore slots
        for (int slot : LORE_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        int totalPages = (int) Math.ceil((double) loreLines.size() / LINES_PER_PAGE);
        
        // Update navigation buttons
        ItemStack prevPage = inventory.getItem(PREV_PAGE_SLOT);
        if (prevPage != null && prevPage.hasItemMeta()) {
            ItemMeta meta = prevPage.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Page: §e" + (currentPage + 1) + "§7/§e" + Math.max(1, totalPages));
            lore.add("§7Lines: §e" + loreLines.size());
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
            lore.add("§7Lines: §e" + loreLines.size());
            lore.add("");
            if (currentPage < totalPages - 1) {
                lore.add("§8Click to go to next page");
            } else {
                lore.add("§8No next page");
            }
            meta.setLore(lore);
            nextPage.setItemMeta(meta);
        }
        
        // Display lore lines for current page
        int startIndex = currentPage * LINES_PER_PAGE;
        int endIndex = Math.min(startIndex + LINES_PER_PAGE, loreLines.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String loreLine = loreLines.get(i);
            int slotIndex = i - startIndex;
            
            if (slotIndex < LORE_SLOTS.length) {
                ItemStack loreItem = createLoreLineItem(loreLine, i + 1);
                inventory.setItem(LORE_SLOTS[slotIndex], loreItem);
            }
        }
    }
    
    private ItemStack createLoreLineItem(String loreLine, int lineNumber) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e§lLine " + lineNumber);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Content: " + (loreLine.isEmpty() ? "§8Empty line" : loreLine));
            lore.add("§7Raw: §8" + loreLine.replace("§", "&"));
            lore.add("");
            lore.add("§8Left click: Edit line");
            lore.add("§8Right click: Delete line");
            lore.add("§8Shift+Left: Move up");
            lore.add("§8Shift+Right: Move down");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
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
            
        } else if (slot == SAVE_SLOT) {
            parentGUI.setCurrentLore(new ArrayList<>(loreLines));
            MessageUtils.sendMessage(player, "§aLore saved!");
            parentGUI.open();
            
        } else if (slot == CLEAR_ALL_SLOT) {
            loreLines.clear();
            currentPage = 0;
            updateLoreDisplay();
            MessageUtils.sendMessage(player, "§cAll lore lines cleared!");
            
        } else if (slot == ADD_LINE_SLOT) {
            startAddLineConversation();
            
        } else if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            updateLoreDisplay();
            
        } else if (slot == NEXT_PAGE_SLOT) {
            int totalPages = (int) Math.ceil((double) loreLines.size() / LINES_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateLoreDisplay();
            }
            
        } else {
            // Check if clicked on a lore line slot
            for (int i = 0; i < LORE_SLOTS.length; i++) {
                if (LORE_SLOTS[i] == slot) {
                    int lineIndex = currentPage * LINES_PER_PAGE + i;
                    
                    if (lineIndex < loreLines.size()) {
                        handleLoreLineClick(lineIndex, clickType);
                    }
                    break;
                }
            }
        }
    }
    
    private void handleLoreLineClick(int lineIndex, ClickType clickType) {
        switch (clickType) {
            case LEFT:
                // Edit line
                startEditLineConversation(lineIndex);
                break;
                
            case RIGHT:
                // Delete line
                loreLines.remove(lineIndex);
                
                // Adjust current page if necessary
                int totalPages = (int) Math.ceil((double) loreLines.size() / LINES_PER_PAGE);
                if (currentPage >= totalPages && currentPage > 0) {
                    currentPage--;
                }
                
                updateLoreDisplay();
                MessageUtils.sendMessage(player, "§cLore line deleted!");
                break;
                
            case SHIFT_LEFT:
                // Move line up
                if (lineIndex > 0) {
                    String line = loreLines.remove(lineIndex);
                    loreLines.add(lineIndex - 1, line);
                    updateLoreDisplay();
                    MessageUtils.sendMessage(player, "§aLine moved up!");
                }
                break;
                
            case SHIFT_RIGHT:
                // Move line down
                if (lineIndex < loreLines.size() - 1) {
                    String line = loreLines.remove(lineIndex);
                    loreLines.add(lineIndex + 1, line);
                    updateLoreDisplay();
                    MessageUtils.sendMessage(player, "§aLine moved down!");
                }
                break;
        }
    }
    
    private void startAddLineConversation() {
        player.closeInventory();
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the new lore line (use & for colors, or 'cancel' to cancel):";
                }
                
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if ("cancel".equalsIgnoreCase(input)) {
                        return END_OF_CONVERSATION;
                    }
                    loreLines.add(input.replace("&", "§"));
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    updateLoreDisplay();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    private void startEditLineConversation(int lineIndex) {
        player.closeInventory();
        String currentLine = loreLines.get(lineIndex).replace("§", "&");
        
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new StringPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEdit lore line (current: §e" + currentLine + "§a) or 'cancel' to cancel:";
                }
                
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if ("cancel".equalsIgnoreCase(input)) {
                        return END_OF_CONVERSATION;
                    }
                    loreLines.set(lineIndex, input.replace("&", "§"));
                    return END_OF_CONVERSATION;
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    updateLoreDisplay();
                    open();
                });
            })
            .buildConversation(player);
        
        conversation.begin();
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}