package pl.meklas.meklascase.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.CaseItem;
import pl.meklas.meklascase.case.RotationProfile;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaseSpinGUI implements InventoryHolder {
    
    private final MeklasCasePlugin plugin;
    private final Player player;
    private final Case caseObj;
    private final Inventory inventory;
    private final List<CaseItem> spinItems;
    private final CaseItem finalItem;
    
    private BukkitTask spinTask;
    private int currentPosition = 0;
    private int spinSpeed = 2; // ticks between moves
    private int totalSpins = 0;
    private boolean isSpinning = false;
    
    private static final int[] SPIN_SLOTS = {10, 11, 12, 13, 14, 15, 16}; // 7 slots for spinning
    private static final int FINAL_SLOT = 13; // Middle slot
    private static final Random random = new Random();
    
    public CaseSpinGUI(MeklasCasePlugin plugin, Player player, Case caseObj) {
        this.plugin = plugin;
        this.player = player;
        this.caseObj = caseObj;
        

        RotationProfile profile = plugin.getRotationManager().getActiveProfile(caseObj.getName());
        

        this.finalItem = caseObj.drawItem(profile);
        

        this.spinItems = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            if (i == 45) {
                // Ensure final item is at the end
                spinItems.add(finalItem);
            } else {
                spinItems.add(caseObj.drawItem(profile));
            }
        }
        
        // Create inventory
        this.inventory = Bukkit.createInventory(this, 27, 
            MessageUtils.PRIMARY_GRADIENT + "🎰 " + caseObj.getName() + " 🎰</gradient>");
        
        setupGUI();
    }
    
    private void setupGUI() {
        // Fill borders with glass
        ItemStack borderGlass = createGlassPane(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack spinGlass = createGlassPane(Material.LIME_STAINED_GLASS_PANE, 
            MessageUtils.SUCCESS_GRADIENT + "✨ Kręcenie... ✨</gradient>");
        
        // Fill entire inventory with border
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, borderGlass);
        }
        
        // Set spinning area
        for (int slot : SPIN_SLOTS) {
            inventory.setItem(slot, spinGlass);
        }
        
        // Add decorative elements
        inventory.setItem(4, createCaseInfo());
        inventory.setItem(22, createWinChance());
    }
    
    private ItemStack createGlassPane(Material material, String name) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            glass.setItemMeta(meta);
        }
        return glass;
    }
    
    private ItemStack createCaseInfo() {
        ItemStack info = new ItemStack(Material.CHEST);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.PRIMARY_GRADIENT + "📦 " + caseObj.getName() + "</gradient>");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(MessageUtils.SECONDARY_GRADIENT + "Typ: " + caseObj.getType().name() + "</gradient>");
            
            // Add rotation info
            RotationProfile profile = plugin.getRotationManager().getActiveProfile(caseObj.getName());
            if (profile != null) {
                lore.add(MessageUtils.WARNING_GRADIENT + "🔥 " + profile.getFormattedDescription() + "</gradient>");
            }
            
            // Add time until next rotation
            long timeLeft = plugin.getRotationManager().getTimeUntilNextRotation(caseObj.getName());
            if (timeLeft > 0) {
                String timeStr = plugin.getMessageUtils().formatTimeRemaining(timeLeft);
                lore.add(MessageUtils.GOLD_GRADIENT + "⏰ Następna rotacja: " + timeStr + "</gradient>");
            }
            
            lore.add("");
            lore.add(MessageUtils.SUCCESS_GRADIENT + "🍀 Powodzenia! 🍀</gradient>");
            
            meta.setLore(lore);
            info.setItemMeta(meta);
        }
        return info;
    }
    
    private ItemStack createWinChance() {
        ItemStack dice = new ItemStack(Material.EMERALD);
        ItemMeta meta = dice.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.SUCCESS_GRADIENT + "🎯 Szanse wygranej</gradient>");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            
            // Calculate and show chances for top items
            List<CaseItem> items = caseObj.getItems();
            items.sort((a, b) -> Double.compare(a.getWeight(), b.getWeight()));
            
            for (int i = 0; i < Math.min(5, items.size()); i++) {
                CaseItem item = items.get(i);
                double chance = calculateItemChance(item);
                String color = getChanceColor(chance);
                
                lore.add(color + "• " + item.getDisplayName() + " - " + 
                        plugin.getMessageUtils().formatPercentage(chance / 100.0) + "</gradient>");
            }
            
            lore.add("");
            lore.add(MessageUtils.GOLD_GRADIENT + "✨ Im niższa waga, tym rzadsza nagroda! ✨</gradient>");
            
            meta.setLore(lore);
            dice.setItemMeta(meta);
        }
        return dice;
    }
    
    private double calculateItemChance(CaseItem item) {
        double totalWeight = caseObj.getItems().stream()
            .mapToDouble(CaseItem::getWeight)
            .sum();
        return (item.getWeight() / totalWeight) * 100.0;
    }
    
    private String getChanceColor(double chance) {
        if (chance < 5) return MessageUtils.ERROR_GRADIENT;
        if (chance < 15) return MessageUtils.WARNING_GRADIENT;
        if (chance < 30) return MessageUtils.GOLD_GRADIENT;
        return MessageUtils.SUCCESS_GRADIENT;
    }
    
    public void startSpin() {
        if (isSpinning) return;
        
        isSpinning = true;
        currentPosition = 0;
        totalSpins = 0;
        
        // Play start sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
        
        spinTask = new BukkitRunnable() {
            @Override
            public void run() {
                performSpinTick();
            }
        }.runTaskTimer(plugin, 0L, spinSpeed);
    }
    
    private void performSpinTick() {
        // Clear current spin slots
        for (int slot : SPIN_SLOTS) {
            inventory.setItem(slot, createGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " "));
        }
        
        // Place items in spin slots
        for (int i = 0; i < SPIN_SLOTS.length; i++) {
            int itemIndex = (currentPosition + i) % spinItems.size();
            CaseItem item = spinItems.get(itemIndex);
            inventory.setItem(SPIN_SLOTS[i], item.createItemStack());
        }
        
        // Play spin sound
        if (totalSpins % 3 == 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f + (totalSpins * 0.02f));
        }
        
        currentPosition++;
        totalSpins++;
        
        // Slow down as we approach the end
        if (totalSpins > 30) {
            spinSpeed = Math.min(10, spinSpeed + 1);
            
            // Cancel task and restart with new speed
            if (spinTask != null) {
                spinTask.cancel();
            }
            
            if (totalSpins >= 45) {
                finishSpin();
                return;
            }
            
            spinTask = new BukkitRunnable() {
                @Override
                public void run() {
                    performSpinTick();
                }
            }.runTaskTimer(plugin, 0L, spinSpeed);
        }
    }
    
    private void finishSpin() {
        isSpinning = false;
        
        if (spinTask != null) {
            spinTask.cancel();
            spinTask = null;
        }
        
        // Highlight final slot
        for (int i = 0; i < SPIN_SLOTS.length; i++) {
            if (SPIN_SLOTS[i] == FINAL_SLOT) {
                // Winner slot - special glass
                inventory.setItem(FINAL_SLOT - 9, createGlassPane(Material.YELLOW_STAINED_GLASS_PANE, 
                    MessageUtils.GOLD_GRADIENT + "🏆 WYGRANA! 🏆</gradient>"));
                inventory.setItem(FINAL_SLOT + 9, createGlassPane(Material.YELLOW_STAINED_GLASS_PANE, 
                    MessageUtils.GOLD_GRADIENT + "🏆 WYGRANA! 🏆</gradient>"));
            } else {
                // Regular slot
                inventory.setItem(SPIN_SLOTS[i], createGlassPane(Material.RED_STAINED_GLASS_PANE, " "));
            }
        }
        
        // Place final item
        inventory.setItem(FINAL_SLOT, finalItem.createItemStack());
        
        // Play win sounds and effects
        playWinEffects();
        
        // Give item to player after delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveItemToPlayer();
            closeGUI();
        }, 60L); // 3 seconds
    }
    
    private void playWinEffects() {
        boolean isTopDrop = caseObj.isTopDrop(finalItem);
        
        if (isTopDrop) {
            // Top drop effects
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 2.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);
            
            plugin.getMessageUtils().announceTopDrop(player, caseObj.getName(), 
                finalItem.getDisplayName(), finalItem.getAmount());
            
            // Update rotation state
            plugin.getRotationManager().setTopDrop(caseObj.getName(), 
                finalItem.getDisplayName() + " x" + finalItem.getAmount());
            
        } else {
            // Regular win effects
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 1.0f);
            
            plugin.getMessageUtils().announceWin(player, caseObj.getName(), 
                finalItem.getDisplayName(), finalItem.getAmount());
        }
        
        // Particle effects (title and actionbar)
        String winTitle = isTopDrop ? 
            MessageUtils.ERROR_GRADIENT + "💎 TOP DROP! 💎</gradient>" :
            MessageUtils.SUCCESS_GRADIENT + "🎉 WYGRANA! 🎉</gradient>";
        
        String winSubtitle = MessageUtils.GOLD_GRADIENT + finalItem.getDisplayName() + 
            " x" + finalItem.getAmount() + "</gradient>";
        
        plugin.getMessageUtils().sendTitle(player, winTitle, winSubtitle, 10, 60, 20);
    }
    
    private void giveItemToPlayer() {
        ItemStack item = finalItem.createItemStack();
        
        // Try to add to inventory
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
            plugin.getMessageUtils().sendSuccess(player, 
                "Otrzymałeś " + finalItem.getDisplayName() + " x" + finalItem.getAmount() + "!");
        } else {
            // Drop at player location
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            plugin.getMessageUtils().sendWarning(player, 
                "Twój ekwipunek jest pełny! Przedmiot został upuszczony na ziemię.");
        }
    }
    
    public void closeGUI() {
        if (spinTask != null) {
            spinTask.cancel();
            spinTask = null;
        }
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().getHolder() == this) {
                player.closeInventory();
            }
        });
    }
    
    public boolean isSpinning() {
        return isSpinning;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Case getCase() {
        return caseObj;
    }
}