package pl.meklas.meklascase.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.CaseItem;
import pl.meklas.meklascase.case.RotationProfile;
import pl.meklas.meklascase.gui.CaseSpinGUI;

public class CaseInteractionListener implements Listener {
    
    private final MeklasCasePlugin plugin;
    
    public CaseInteractionListener(MeklasCasePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        Location clickedLocation = event.getClickedBlock().getLocation();
        
        // Check if clicked block is a case
        Case caseObj = plugin.getCaseManager().getCaseAtLocation(clickedLocation);
        if (caseObj == null) return;
        
        // Cancel the event to prevent other interactions
        event.setCancelled(true);
        
        // Check if case is enabled
        if (!caseObj.isEnabled()) {
            plugin.getMessageUtils().sendError(player, "Ta skrzynka jest obecnie wyłączona!");
            return;
        }
        
        // Check if player has permission
        if (!player.hasPermission("meklascase.use")) {
            plugin.getMessageUtils().sendError(player, "Nie masz uprawnień do używania skrzynek!");
            return;
        }
        
        // Get item in hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // Check if player has a valid key
        if (!caseObj.isValidKey(itemInHand)) {
            plugin.getMessageUtils().sendError(player, "Potrzebujesz klucza do tej skrzynki!");
            
            // Play no key sound
            String soundName = plugin.getConfigManager().getConfig().getString("sounds.noKey", "ENTITY_VILLAGER_NO");
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 0.5f, 0.8f);
            } catch (IllegalArgumentException e) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
            }
            return;
        }
        
        // Check for quick open (Shift + Right Click)
        boolean quickOpen = player.isSneaking() && plugin.getConfigManager().getConfig().getBoolean("quickOpen", true);
        
        if (quickOpen) {
            // Quick open - no animation
            performQuickOpen(player, caseObj, itemInHand);
        } else {
            // Normal open - with animation
            performNormalOpen(player, caseObj, itemInHand);
        }
    }
    
    private void performQuickOpen(Player player, Case caseObj, ItemStack keyItem) {
        // Remove key from inventory
        if (keyItem.getAmount() > 1) {
            keyItem.setAmount(keyItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Get active rotation profile
        RotationProfile profile = plugin.getRotationManager().getActiveProfile(caseObj.getName());
        
        // Draw item
        CaseItem wonItem = caseObj.drawItem(profile);
        if (wonItem == null) {
            plugin.getMessageUtils().sendError(player, "Błąd podczas losowania przedmiotu!");
            return;
        }
        
        // Give item to player
        ItemStack item = wonItem.createItemStack();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            plugin.getMessageUtils().sendWarning(player, "Twój ekwipunek jest pełny! Przedmiot został upuszczony.");
        }
        
        // Check if it's a top drop
        boolean isTopDrop = caseObj.isTopDrop(wonItem);
        
        if (isTopDrop) {
            plugin.getMessageUtils().announceTopDrop(player, caseObj.getName(), 
                wonItem.getDisplayName(), wonItem.getAmount());
            plugin.getRotationManager().setTopDrop(caseObj.getName(), 
                wonItem.getDisplayName() + " x" + wonItem.getAmount());
        } else {
            plugin.getMessageUtils().announceWin(player, caseObj.getName(), 
                wonItem.getDisplayName(), wonItem.getAmount());
        }
        
        // Play win sound
        String soundName = plugin.getConfigManager().getConfig().getString("sounds.win", "ENTITY_PLAYER_LEVELUP");
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
        
        plugin.getMessageUtils().sendSuccess(player, 
            "Otrzymałeś " + wonItem.getDisplayName() + " x" + wonItem.getAmount() + "!");
    }
    
    private void performNormalOpen(Player player, Case caseObj, ItemStack keyItem) {
        // Remove key from inventory
        if (keyItem.getAmount() > 1) {
            keyItem.setAmount(keyItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Create and open spin GUI
        CaseSpinGUI spinGUI = new CaseSpinGUI(plugin, player, caseObj);
        player.openInventory(spinGUI.getInventory());
        
        // Start spinning animation
        spinGUI.startSpin();
        
        plugin.getMessageUtils().sendInfo(player, "Otwieranie skrzynki " + caseObj.getName() + "...");
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof CaseSpinGUI)) return;
        
        // Cancel all clicks in spin GUI
        event.setCancelled(true);
        
        CaseSpinGUI spinGUI = (CaseSpinGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        
        // Play click sound if not spinning
        if (!spinGUI.isSpinning()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof CaseSpinGUI)) return;
        
        CaseSpinGUI spinGUI = (CaseSpinGUI) event.getInventory().getHolder();
        
        // If spinning is still in progress, don't allow closing
        if (spinGUI.isSpinning()) {
            // Reopen inventory after a tick
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                spinGUI.getPlayer().openInventory(spinGUI.getInventory());
            });
            return;
        }
        
        // Clean up the GUI
        spinGUI.closeGUI();
    }
}