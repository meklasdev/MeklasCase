package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

public class EnchantmentManagerGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final DropEditorGUI parentGUI;
    private final Inventory inventory;
    
    private static final int[] ENCHANT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };
    
    private static final int BACK_SLOT = 0;
    private static final int ADD_SLOT = 8;
    private static final int CLEAR_ALL_SLOT = 4;
    
    public EnchantmentManagerGUI(MeklasCasePlugin plugin, Player player, DropEditorGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, "§6§lZarządzaj Zaklęciami");
        
        setupGUI();
        updateEnchantmentDisplay();
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
            Arrays.asList("§7Return to drop editor")
        );
        inventory.setItem(BACK_SLOT, backButton);
        
        // Add enchantment button
        ItemStack addButton = createItem(
            Material.EMERALD,
            "§a§lAdd Enchantment",
            Arrays.asList(
                "§7Open enchantment selector",
                "",
                "§8Click to add new enchantment"
            )
        );
        inventory.setItem(ADD_SLOT, addButton);
        
        // Clear all button
        ItemStack clearButton = createItem(
            Material.TNT,
            "§c§lClear All",
            Arrays.asList(
                "§7Remove all enchantments",
                "",
                "§8Click to clear all enchantments"
            )
        );
        inventory.setItem(CLEAR_ALL_SLOT, clearButton);
    }
    
    private void updateEnchantmentDisplay() {
        // Clear enchantment slots
        for (int slot : ENCHANT_SLOTS) {
            inventory.setItem(slot, null);
        }
        
        Map<Enchantment, Integer> enchantments = parentGUI.getCurrentEnchantments();
        List<Map.Entry<Enchantment, Integer>> enchantList = new ArrayList<>(enchantments.entrySet());
        
        for (int i = 0; i < Math.min(enchantList.size(), ENCHANT_SLOTS.length); i++) {
            Map.Entry<Enchantment, Integer> entry = enchantList.get(i);
            ItemStack enchantItem = createEnchantmentItem(entry.getKey(), entry.getValue());
            inventory.setItem(ENCHANT_SLOTS[i], enchantItem);
        }
    }
    
    private ItemStack createEnchantmentItem(Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String name = formatEnchantmentName(enchantment);
            meta.setDisplayName("§a§l" + name + " " + level);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Enchantment: §e" + enchantment.getKey().getKey());
            lore.add("§7Level: §e" + level + "§7/§e" + enchantment.getMaxLevel());
            lore.add("");
            lore.add("§8Right click: Remove enchantment");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String formatEnchantmentName(Enchantment enchantment) {
        return Arrays.stream(enchantment.getKey().getKey().split("_"))
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
            
        } else if (slot == ADD_SLOT) {
            new EnchantmentSelectorGUI(plugin, player, parentGUI).open();
            
        } else if (slot == CLEAR_ALL_SLOT) {
            Map<Enchantment, Integer> enchantments = parentGUI.getCurrentEnchantments();
            enchantments.clear();
            parentGUI.setCurrentEnchantments(enchantments);
            updateEnchantmentDisplay();
            MessageUtils.sendMessage(player, "§cAll enchantments cleared!");
            
        } else {
            // Check if clicked on an enchantment slot
            for (int i = 0; i < ENCHANT_SLOTS.length; i++) {
                if (ENCHANT_SLOTS[i] == slot) {
                    Map<Enchantment, Integer> enchantments = parentGUI.getCurrentEnchantments();
                    List<Map.Entry<Enchantment, Integer>> enchantList = new ArrayList<>(enchantments.entrySet());
                    
                    if (i < enchantList.size()) {
                        Map.Entry<Enchantment, Integer> entry = enchantList.get(i);
                        
                        if (clickType == ClickType.RIGHT) {
                            // Remove enchantment
                            enchantments.remove(entry.getKey());
                            parentGUI.setCurrentEnchantments(enchantments);
                            updateEnchantmentDisplay();
                            MessageUtils.sendMessage(player, "§cEnchantment " + 
                                formatEnchantmentName(entry.getKey()) + " removed!");
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