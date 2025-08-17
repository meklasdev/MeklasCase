package pl.meklas.meklascase.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.gui.*;

public class DropManagementListener implements Listener {
    
    private final MeklasCasePlugin plugin;
    
    public DropManagementListener(MeklasCasePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Handle different GUI types
        if (holder instanceof CaseSelectorGUI) {
            ((CaseSelectorGUI) holder).handleClick(event);
            
        } else if (holder instanceof DropManagementGUI) {
            ((DropManagementGUI) holder).handleClick(event);
            
        } else if (holder instanceof DropEditorGUI) {
            ((DropEditorGUI) holder).handleClick(event);
            
        } else if (holder instanceof MaterialSelectorGUI) {
            ((MaterialSelectorGUI) holder).handleClick(event);
            
        } else if (holder instanceof LoreEditorGUI) {
            ((LoreEditorGUI) holder).handleClick(event);
            
        } else if (holder instanceof EnchantmentSelectorGUI) {
            ((EnchantmentSelectorGUI) holder).handleClick(event);
            
        } else if (holder instanceof EnchantmentManagerGUI) {
            ((EnchantmentManagerGUI) holder).handleClick(event);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        
        // Handle any cleanup needed when GUIs are closed
        // Currently no special cleanup is needed, but this is here for future use
        if (holder instanceof DropEditorGUI || 
            holder instanceof DropManagementGUI || 
            holder instanceof CaseSelectorGUI) {
            // Could add cleanup logic here if needed
        }
    }
}