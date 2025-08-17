package pl.meklas.meklascase.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.case.RotationProfile;
import pl.meklas.meklascase.rotation.RotationManager;
import pl.meklas.meklascase.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

@Command(name = "meklascase", aliases = {"mcase", "case"})
public class MeklasCaseCommand {
    
    private final MeklasCasePlugin plugin;
    
    public MeklasCaseCommand(MeklasCasePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Execute
    public void help(@Context Player player) {
        plugin.getMessageUtils().sendHeader(player, "meklasCase - Pomoc");
        
        List<String> commands = new ArrayList<>();
        commands.add("§e/meklascase help §7- Wyświetla tę pomoc");
        commands.add("§e/meklascase create <nazwa> [typ] §7- Tworzy nową skrzynkę");
        commands.add("§e/meklascase delete <nazwa> §7- Usuwa skrzynkę");
        commands.add("§e/meklascase setlocation <nazwa> §7- Ustawia lokalizację skrzynki");
        commands.add("§e/meklascase removelocation <nazwa> §7- Usuwa lokalizację skrzynki");
        commands.add("§e/meklascase give <gracz> <case> <ilość> §7- Daje klucze");
        commands.add("§e/meklascase giveall <case> <ilość> §7- Daje klucze wszystkim");
        commands.add("§e/meklascase reload §7- Przeładowuje plugin");
        commands.add("§e/meklascase enable <nazwa> §7- Włącza skrzynkę");
        commands.add("§e/meklascase disable <nazwa> §7- Wyłącza skrzynkę");
        commands.add("§e/meklascase rotate now §7- Wymusza rotację");
        commands.add("§e/meklascase info <case> §7- Informacje o skrzynce");
        commands.add("§e/meklascase hologram <toggle|reload|effects> §7- Zarządzanie hologramami");
        
        for (String command : commands) {
            plugin.getMessageUtils().sendMessage(player, MessageUtils.PRIMARY_GRADIENT + command + "</gradient>");
        }
        
        plugin.getMessageUtils().sendFooter(player);
    }
    
    @Execute(name = "create")
    @Permission("meklascase.admin")
    public void createCase(@Context Player player, @Arg String name, @Arg(value = "LOOTBOX") String type) {
        if (plugin.getCaseManager().caseExists(name)) {
            plugin.getMessageUtils().sendError(player, "Skrzynka o nazwie '" + name + "' już istnieje!");
            return;
        }
        
        Case.CaseType caseType;
        try {
            caseType = Case.CaseType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getMessageUtils().sendError(player, "Nieprawidłowy typ skrzynki! Dostępne: LOOTBOX, LUCKBLOCK");
            return;
        }
        
        Case createdCase = plugin.getCaseManager().createCase(name, caseType);
        if (createdCase != null) {
            plugin.getMessageUtils().sendSuccess(player, "Utworzono skrzynkę '" + name + "' typu " + caseType.name());
            plugin.getMessageUtils().sendInfo(player, "Użyj /meklascase setlocation " + name + " aby ustawić lokalizację");
        } else {
            plugin.getMessageUtils().sendError(player, "Nie udało się utworzyć skrzynki!");
        }
    }
    
    @Execute(name = "delete")
    @Permission("meklascase.admin")
    public void deleteCase(@Context Player player, @Arg String name) {
        if (!plugin.getCaseManager().caseExists(name)) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        boolean deleted = plugin.getCaseManager().deleteCase(name);
        if (deleted) {
            plugin.getMessageUtils().sendSuccess(player, "Usunięto skrzynkę '" + name + "'");
        } else {
            plugin.getMessageUtils().sendError(player, "Nie udało się usunąć skrzynki!");
        }
    }
    
    @Execute(name = "setlocation")
    @Permission("meklascase.admin")
    public void setLocation(@Context Player player, @Arg String name) {
        if (!plugin.getCaseManager().caseExists(name)) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        Location location = player.getTargetBlock(null, 10).getLocation();
        if (location.getBlock().getType() == Material.AIR) {
            plugin.getMessageUtils().sendError(player, "Musisz patrzeć na blok!");
            return;
        }
        
        plugin.getCaseManager().setCaseLocation(name, location);
        plugin.getHologramManager().createHologram(name);
        
        plugin.getMessageUtils().sendSuccess(player, "Ustawiono lokalizację skrzynki '" + name + "'");
        plugin.getMessageUtils().sendInfo(player, "Lokalizacja: " + location.getBlockX() + ", " + 
            location.getBlockY() + ", " + location.getBlockZ());
    }
    
    @Execute(name = "removelocation")
    @Permission("meklascase.admin")
    public void removeLocation(@Context Player player, @Arg String name) {
        if (!plugin.getCaseManager().caseExists(name)) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        plugin.getCaseManager().removeCaseLocation(name);
        plugin.getHologramManager().removeHologram(name);
        
        plugin.getMessageUtils().sendSuccess(player, "Usunięto lokalizację skrzynki '" + name + "'");
    }
    
    @Execute(name = "give")
    @Permission("meklascase.admin")
    public void giveKey(@Context Player player, @Arg String targetName, @Arg String caseName, @Arg int amount) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            plugin.getMessageUtils().sendError(player, "Gracz '" + targetName + "' nie jest online!");
            return;
        }
        
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + caseName + "' nie istnieje!");
            return;
        }
        
        if (amount <= 0 || amount > 64) {
            plugin.getMessageUtils().sendError(player, "Ilość musi być między 1 a 64!");
            return;
        }
        
        ItemStack key = caseObj.createKeyItem(amount);
        
        if (target.getInventory().firstEmpty() != -1) {
            target.getInventory().addItem(key);
            plugin.getMessageUtils().sendSuccess(player, "Dano " + amount + " kluczy do " + caseName + " graczowi " + targetName);
            plugin.getMessageUtils().sendSuccess(target, "Otrzymałeś " + amount + " kluczy do " + caseName + "!");
        } else {
            target.getWorld().dropItemNaturally(target.getLocation(), key);
            plugin.getMessageUtils().sendSuccess(player, "Dano " + amount + " kluczy do " + caseName + " graczowi " + targetName + " (upuszczone)");
            plugin.getMessageUtils().sendWarning(target, "Otrzymałeś " + amount + " kluczy do " + caseName + " (upuszczone na ziemię)!");
        }
    }
    
    @Execute(name = "giveall")
    @Permission("meklascase.admin")
    public void giveAllKeys(@Context Player player, @Arg String caseName, @Arg int amount) {
        Case caseObj = plugin.getCaseManager().getCase(caseName);
        if (caseObj == null) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + caseName + "' nie istnieje!");
            return;
        }
        
        if (amount <= 0 || amount > 64) {
            plugin.getMessageUtils().sendError(player, "Ilość musi być między 1 a 64!");
            return;
        }
        
        int playersGiven = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ItemStack key = caseObj.createKeyItem(amount);
            
            if (onlinePlayer.getInventory().firstEmpty() != -1) {
                onlinePlayer.getInventory().addItem(key);
            } else {
                onlinePlayer.getWorld().dropItemNaturally(onlinePlayer.getLocation(), key);
            }
            
            plugin.getMessageUtils().sendSuccess(onlinePlayer, "Otrzymałeś " + amount + " kluczy do " + caseName + "!");
            playersGiven++;
        }
        
        plugin.getMessageUtils().sendSuccess(player, "Dano " + amount + " kluczy do " + caseName + " dla " + playersGiven + " graczy");
    }
    
    @Execute(name = "reload")
    @Permission("meklascase.admin")
    public void reload(@Context Player player) {
        try {
            plugin.reload();
            plugin.getMessageUtils().sendSuccess(player, "Plugin został pomyślnie przeładowany!");
        } catch (Exception e) {
            plugin.getMessageUtils().sendError(player, "Błąd podczas przeładowania: " + e.getMessage());
        }
    }
    
    @Execute(name = "enable")
    @Permission("meklascase.admin")
    public void enableCase(@Context Player player, @Arg String name) {
        Case caseObj = plugin.getCaseManager().getCase(name);
        if (caseObj == null) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        caseObj.setEnabled(true);
        plugin.getMessageUtils().sendSuccess(player, "Włączono skrzynkę '" + name + "'");
    }
    
    @Execute(name = "disable")
    @Permission("meklascase.admin")
    public void disableCase(@Context Player player, @Arg String name) {
        Case caseObj = plugin.getCaseManager().getCase(name);
        if (caseObj == null) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        caseObj.setEnabled(false);
        plugin.getMessageUtils().sendSuccess(player, "Wyłączono skrzynkę '" + name + "'");
    }
    
    @Execute(name = "rotate")
    @Permission("meklascase.rotate.admin")
    public void rotateNow(@Context Player player, @Arg("now") String now) {
        if (!now.equalsIgnoreCase("now")) {
            plugin.getMessageUtils().sendError(player, "Użyj: /meklascase rotate now");
            return;
        }
        
        int rotated = 0;
        for (String caseName : plugin.getCaseManager().getCaseNames()) {
            plugin.getRotationManager().forceRotation(caseName);
            rotated++;
        }
        
        plugin.getMessageUtils().sendSuccess(player, "Wymuszono rotację dla " + rotated + " skrzynek");
    }
    
    @Execute(name = "info")
    @Permission("meklascase.admin")
    public void caseInfo(@Context Player player, @Arg String name) {
        Case caseObj = plugin.getCaseManager().getCase(name);
        if (caseObj == null) {
            plugin.getMessageUtils().sendError(player, "Skrzynka '" + name + "' nie istnieje!");
            return;
        }
        
        plugin.getMessageUtils().sendHeader(player, "Informacje o " + name);
        
        // Basic info
        plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
            "Typ: " + caseObj.getType().name() + "</gradient>");
        plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
            "Status: " + (caseObj.isEnabled() ? "§aWłączona" : "§cWyłączona") + "</gradient>");
        plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
            "Lokalizacja: " + (caseObj.getLocation() != null ? "Ustawiona" : "Brak") + "</gradient>");
        plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
            "Przedmioty: " + caseObj.getItems().size() + "</gradient>");
        plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
            "Profile rotacji: " + caseObj.getRotationProfiles().size() + "</gradient>");
        
        // Current rotation info
        RotationProfile activeProfile = plugin.getRotationManager().getActiveProfile(name);
        if (activeProfile != null) {
            plugin.getMessageUtils().sendMessage(player, MessageUtils.WARNING_GRADIENT + 
                "Aktywny profil: " + activeProfile.getName() + "</gradient>");
            plugin.getMessageUtils().sendMessage(player, MessageUtils.WARNING_GRADIENT + 
                "Opis: " + activeProfile.getFormattedDescription() + "</gradient>");
        } else {
            plugin.getMessageUtils().sendMessage(player, MessageUtils.SECONDARY_GRADIENT + 
                "Aktywny profil: Brak</gradient>");
        }
        
        // Time until next rotation
        long timeLeft = plugin.getRotationManager().getTimeUntilNextRotation(name);
        if (timeLeft > 0) {
            String timeStr = plugin.getMessageUtils().formatTimeRemaining(timeLeft);
            plugin.getMessageUtils().sendMessage(player, MessageUtils.GOLD_GRADIENT + 
                "Następna rotacja: " + timeStr + "</gradient>");
        }
        
        // Last top drop
        RotationManager.RotationState state = plugin.getRotationManager().getRotationState(name);
        if (state != null) {
            plugin.getMessageUtils().sendMessage(player, MessageUtils.ERROR_GRADIENT + 
                "Ostatni TOP DROP: " + state.getLastTopDrop() + "</gradient>");
        }
        
        plugin.getMessageUtils().sendFooter(player);
    }
    
    @Execute(name = "hologram")
    @Permission("meklascase.admin")
    public void hologramCommand(@Context Player player, @Arg String action, @Arg(value = "") String caseName) {
        switch (action.toLowerCase()) {
            case "toggle":
                if (caseName.isEmpty()) {
                    plugin.getMessageUtils().sendError(player, "Użyj: /meklascase hologram toggle <nazwa>");
                    return;
                }
                
                boolean currentState = plugin.getConfigManager().getLocations()
                    .getBoolean("cases." + caseName + ".hologram.enabled", true);
                
                plugin.getHologramManager().setHologramEnabled(caseName, !currentState);
                
                String status = !currentState ? "włączony" : "wyłączony";
                plugin.getMessageUtils().sendSuccess(player, "Hologram dla " + caseName + " został " + status);
                break;
                
            case "reload":
                plugin.getHologramManager().removeAllHolograms();
                plugin.getHologramManager().initializeHolograms();
                plugin.getMessageUtils().sendSuccess(player, "Wszystkie hologramy zostały przeładowane!");
                break;
                
            case "effects":
                showEffectsStatus(player);
                break;
                
            default:
                plugin.getMessageUtils().sendError(player, "Użyj: hologram <toggle|reload|effects>");
                break;
        }
    }
    
    private void showEffectsStatus(Player player) {
        plugin.getMessageUtils().sendHeader(player, "Status efektów hologramów");
        
        String[] effects = {"rainbow", "particles", "fire", "glitch", "neon", "constellation", "digitalRain", "pulsingBorder", "waveAnimation"};
        
        for (String effect : effects) {
            boolean enabled = plugin.getConfigManager().getConfig()
                .getBoolean("holograms.animations.effects." + effect, true);
            
            String status = enabled ? "§a✓ Włączony" : "§c✗ Wyłączony";
            plugin.getMessageUtils().sendMessage(player, 
                MessageUtils.SECONDARY_GRADIENT + effect + ": " + status + "</gradient>");
        }
        
        boolean animationsEnabled = plugin.getConfigManager().getConfig()
            .getBoolean("holograms.animations.enabled", true);
        long updateInterval = plugin.getConfigManager().getConfig()
            .getLong("holograms.animations.updateInterval", 10L);
            
        plugin.getMessageUtils().sendMessage(player, "");
        plugin.getMessageUtils().sendMessage(player, 
            MessageUtils.GOLD_GRADIENT + "Animacje: " + (animationsEnabled ? "§a✓" : "§c✗") + "</gradient>");
        plugin.getMessageUtils().sendMessage(player, 
            MessageUtils.GOLD_GRADIENT + "Interwał aktualizacji: " + updateInterval + " ticków</gradient>");
        
        plugin.getMessageUtils().sendFooter(player);
    }
}