package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
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

public class EnchantmentSelectorGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final DropEditorGUI parentGUI;
    private final Inventory inventory;
    
    private static final int[] ENCHANT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    private static final int BACK_SLOT = 0;
    
    public EnchantmentSelectorGUI(MeklasCasePlugin plugin, Player player, DropEditorGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.inventory = Bukkit.createInventory(this, 54, "§6§lWybierz Zaklęcie");
        
        setupGUI();
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
            "§c§lPowrót",
            Arrays.asList("§7Powróć do edytora dropów")
        );
        inventory.setItem(BACK_SLOT, backButton);
        
        // Display common enchantments
        List<Enchantment> commonEnchantments = Arrays.asList(
            Enchantment.SHARPNESS,
            Enchantment.PROTECTION,
            Enchantment.EFFICIENCY,
            Enchantment.FORTUNE,
            Enchantment.SILK_TOUCH,
            Enchantment.UNBREAKING,
            Enchantment.MENDING,
            Enchantment.LOOTING,
            Enchantment.FIRE_ASPECT,
            Enchantment.KNOCKBACK,
            Enchantment.FEATHER_FALLING,
            Enchantment.RESPIRATION,
            Enchantment.AQUA_AFFINITY,
            Enchantment.THORNS,
            Enchantment.POWER,
            Enchantment.PUNCH,
            Enchantment.FLAME,
            Enchantment.INFINITY,
            Enchantment.LUCK_OF_THE_SEA,
            Enchantment.LURE,
            Enchantment.FROST_WALKER,
            Enchantment.DEPTH_STRIDER,
            Enchantment.SOUL_SPEED,
            Enchantment.SWIFT_SNEAK,
            Enchantment.CHANNELING,
            Enchantment.LOYALTY,
            Enchantment.IMPALING,
            Enchantment.RIPTIDE
        );
        
        for (int i = 0; i < Math.min(commonEnchantments.size(), ENCHANT_SLOTS.length); i++) {
            Enchantment enchant = commonEnchantments.get(i);
            ItemStack enchantItem = createEnchantmentItem(enchant);
            inventory.setItem(ENCHANT_SLOTS[i], enchantItem);
        }
    }
    
    private ItemStack createEnchantmentItem(Enchantment enchantment) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String name = formatEnchantmentName(enchantment);
            meta.setDisplayName("§a§l" + name);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Enchantment: §e" + enchantment.getKey().getKey());
            lore.add("§7Max Level: §e" + enchantment.getMaxLevel());
            
            Map<Enchantment, Integer> currentEnchants = parentGUI.getCurrentEnchantments();
            if (currentEnchants.containsKey(enchantment)) {
                lore.add("§7Current Level: §e" + currentEnchants.get(enchantment));
                lore.add("");
                lore.add("§a§l✓ Already Applied");
            } else {
                lore.add("");
                lore.add("§8Click to add this enchantment");
            }
            
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
        
        if (slot == BACK_SLOT) {
            parentGUI.open();
            return;
        }
        
        // Check if clicked on an enchantment slot
        for (int i = 0; i < ENCHANT_SLOTS.length; i++) {
            if (ENCHANT_SLOTS[i] == slot) {
                ItemStack clickedItem = inventory.getItem(slot);
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    String displayName = clickedItem.getItemMeta().getDisplayName();
                    
                    // Find the enchantment by display name
                    for (Enchantment enchant : Enchantment.values()) {
                        if (("§a§l" + formatEnchantmentName(enchant)).equals(displayName)) {
                            startLevelConversation(enchant);
                            return;
                        }
                    }
                }
                break;
            }
        }
    }
    
    private void startLevelConversation(Enchantment enchantment) {
        player.closeInventory();
        
        Conversation conversation = new ConversationFactory(plugin)
            .withFirstPrompt(new NumericPrompt() {
                @Override
                public String getPromptText(ConversationContext context) {
                    return "§aEnter the level for " + formatEnchantmentName(enchantment) + 
                           " (1-" + enchantment.getMaxLevel() + ") or 'cancel' to cancel:";
                }
                
                @Override
                protected boolean isNumberValid(ConversationContext context, Number input) {
                    int level = input.intValue();
                    return level >= 1 && level <= enchantment.getMaxLevel();
                }
                
                @Override
                protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
                    return "§cLevel must be between 1 and " + enchantment.getMaxLevel() + "!";
                }
                
                @Override
                protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
                    int level = input.intValue();
                    Map<Enchantment, Integer> enchantments = parentGUI.getCurrentEnchantments();
                    enchantments.put(enchantment, level);
                    parentGUI.setCurrentEnchantments(enchantments);
                    
                    context.getForWhom().sendRawMessage("§aEnchantment " + formatEnchantmentName(enchantment) + 
                                                      " level " + level + " added!");
                    return END_OF_CONVERSATION;
                }
                
                @Override
                public Prompt acceptInput(ConversationContext context, String input) {
                    if ("cancel".equalsIgnoreCase(input)) {
                        return END_OF_CONVERSATION;
                    }
                    return super.acceptInput(context, input);
                }
            })
            .withLocalEcho(false)
            .addConversationAbandonedListener(event -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    parentGUI.open();
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