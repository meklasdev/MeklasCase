package pl.meklas.meklascase.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.gui.TrailManagementGUI;
import pl.meklas.meklascase.trail.Trail;
import pl.meklas.meklascase.trail.TrailManager;

import java.util.List;

@Command(name = "trail", aliases = {"szlak", "trails"})
public class TrailCommand {
    
    private final MeklasCasePlugin plugin;
    private final TrailManager trailManager;
    
    public TrailCommand(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.trailManager = plugin.getTrailManager();
    }
    
    @Execute
    @Permission("meklascase.trail.use")
    public void openTrailGUI(@Context Player player) {
        new TrailManagementGUI(plugin, player).open();
        player.sendMessage(Component.text("§aOtwarto menu zarządzania szlakami!", NamedTextColor.GREEN));
    }
    
    @Execute(name = "list")
    @Permission("meklascase.trail.list")
    public void listTrails(@Context Player player) {
        List<Trail> trails = trailManager.getAllTrails().stream()
                .filter(trail -> trailManager.canPlayerViewTrail(player, trail))
                .toList();
        
        if (trails.isEmpty()) {
            player.sendMessage(Component.text("§cBrak dostępnych szlaków!", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("§6=== Lista Szlaków ===", NamedTextColor.GOLD));
        for (Trail trail : trails) {
            String status = trail.isActive() ? "§aAktywny" : "§cNieaktywny";
            String access = trail.isPublicTrail() ? "§bPubliczny" : "§ePrywatny";
            
            player.sendMessage(Component.text(String.format(
                "§7• §b%s §7(%s) - %s, %s - §e%d punktów",
                trail.getName(),
                trail.getType().getDisplayName(),
                status,
                access,
                trail.getPoints().size()
            )));
        }
        player.sendMessage(Component.text("§6Użyj §e/trail §6aby otworzyć GUI", NamedTextColor.GOLD));
    }
    
    @Execute(name = "info")
    @Permission("meklascase.trail.info")
    public void trailInfo(@Context Player player, @Arg String trailName) {
        Trail trail = trailManager.getTrailByName(trailName);
        
        if (trail == null) {
            player.sendMessage(Component.text("§cNie znaleziono szlaku o nazwie: " + trailName, NamedTextColor.RED));
            return;
        }
        
        if (!trailManager.canPlayerViewTrail(player, trail)) {
            player.sendMessage(Component.text("§cNie masz uprawnień do przeglądania tego szlaku!", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("§6=== Informacje o szlaku ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("§7Nazwa: §b" + trail.getName()));
        player.sendMessage(Component.text("§7Typ: §e" + trail.getType().getDisplayName()));
        player.sendMessage(Component.text("§7Kategoria: §e" + trail.getCategory()));
        player.sendMessage(Component.text("§7Status: " + (trail.isActive() ? "§aAktywny" : "§cNieaktywny")));
        player.sendMessage(Component.text("§7Dostęp: " + (trail.isPublicTrail() ? "§bPubliczny" : "§ePrywatny")));
        player.sendMessage(Component.text("§7Punkty: §e" + trail.getPoints().size()));
        player.sendMessage(Component.text("§7Długość: §e" + String.format("%.1f", trail.getLength()) + "m"));
        player.sendMessage(Component.text("§7Priorytet: §e" + trail.getPriority()));
        
        if (trail.getDescription() != null && !trail.getDescription().isEmpty()) {
            player.sendMessage(Component.text("§7Opis: §f" + trail.getDescription()));
        }
        
        player.sendMessage(Component.text("§6Właściciel: §e" + org.bukkit.Bukkit.getOfflinePlayer(trail.getOwner()).getName()));
    }
    
    @Execute(name = "activate")
    @Permission("meklascase.trail.manage")
    public void activateTrail(@Context Player player, @Arg String trailName) {
        Trail trail = trailManager.getTrailByName(trailName);
        
        if (trail == null) {
            player.sendMessage(Component.text("§cNie znaleziono szlaku o nazwie: " + trailName, NamedTextColor.RED));
            return;
        }
        
        if (!trailManager.canPlayerEditTrail(player, trail)) {
            player.sendMessage(Component.text("§cNie masz uprawnień do edycji tego szlaku!", NamedTextColor.RED));
            return;
        }
        
        if (trail.isActive()) {
            player.sendMessage(Component.text("§cSzlak jest już aktywny!", NamedTextColor.RED));
            return;
        }
        
        trail.setActive(true);
        trailManager.updateTrail(trail);
        player.sendMessage(Component.text("§aSzlak '" + trail.getName() + "' został aktywowany!", NamedTextColor.GREEN));
    }
    
    @Execute(name = "deactivate")
    @Permission("meklascase.trail.manage")
    public void deactivateTrail(@Context Player player, @Arg String trailName) {
        Trail trail = trailManager.getTrailByName(trailName);
        
        if (trail == null) {
            player.sendMessage(Component.text("§cNie znaleziono szlaku o nazwie: " + trailName, NamedTextColor.RED));
            return;
        }
        
        if (!trailManager.canPlayerEditTrail(player, trail)) {
            player.sendMessage(Component.text("§cNie masz uprawnień do edycji tego szlaku!", NamedTextColor.RED));
            return;
        }
        
        if (!trail.isActive()) {
            player.sendMessage(Component.text("§cSzlak jest już nieaktywny!", NamedTextColor.RED));
            return;
        }
        
        trail.setActive(false);
        trailManager.updateTrail(trail);
        player.sendMessage(Component.text("§aSzlak '" + trail.getName() + "' został dezaktywowany!", NamedTextColor.GREEN));
    }
    
    @Execute(name = "delete")
    @Permission("meklascase.trail.admin")
    public void deleteTrail(@Context Player player, @Arg String trailName) {
        Trail trail = trailManager.getTrailByName(trailName);
        
        if (trail == null) {
            player.sendMessage(Component.text("§cNie znaleziono szlaku o nazwie: " + trailName, NamedTextColor.RED));
            return;
        }
        
        if (!trailManager.canPlayerEditTrail(player, trail)) {
            player.sendMessage(Component.text("§cNie masz uprawnień do usunięcia tego szlaku!", NamedTextColor.RED));
            return;
        }
        
        trailManager.deleteTrail(trail.getId());
        player.sendMessage(Component.text("§aSzlak '" + trail.getName() + "' został usunięty!", NamedTextColor.GREEN));
    }
    
    @Execute(name = "stats")
    @Permission("meklascase.trail.stats")
    public void trailStats(@Context Player player) {
        int totalTrails = trailManager.getTotalTrailCount();
        int activeTrails = trailManager.getActiveTrailCount();
        int playerTrails = trailManager.getPlayerTrailCount(player.getUniqueId());
        
        player.sendMessage(Component.text("§6=== Statystyki Szlaków ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("§7Wszystkie szlaki: §e" + totalTrails));
        player.sendMessage(Component.text("§7Aktywne szlaki: §a" + activeTrails));
        player.sendMessage(Component.text("§7Nieaktywne szlaki: §c" + (totalTrails - activeTrails)));
        player.sendMessage(Component.text("§7Twoje szlaki: §b" + playerTrails));
        
        // Show trail count by type
        player.sendMessage(Component.text("§6Szlaki według typu:", NamedTextColor.GOLD));
        trailManager.getTrailCountByType().forEach((type, count) -> {
            player.sendMessage(Component.text("§8• §7" + type.getDisplayName() + ": §e" + count));
        });
    }
    
    @Execute(name = "reload")
    @Permission("meklascase.trail.admin")
    public void reloadTrails(@Context Player player) {
        try {
            trailManager.reload();
            player.sendMessage(Component.text("§aSzlaki zostały przeładowane!", NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("§cBłąd podczas przeładowania szlaków: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    @Execute(name = "help")
    public void showHelp(@Context Player player) {
        player.sendMessage(Component.text("§6=== Komendy Szlaków ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("§e/trail §7- Otwórz GUI zarządzania szlakami"));
        player.sendMessage(Component.text("§e/trail list §7- Wyświetl listę szlaków"));
        player.sendMessage(Component.text("§e/trail info <nazwa> §7- Informacje o szlaku"));
        player.sendMessage(Component.text("§e/trail activate <nazwa> §7- Aktywuj szlak"));
        player.sendMessage(Component.text("§e/trail deactivate <nazwa> §7- Dezaktywuj szlak"));
        player.sendMessage(Component.text("§e/trail stats §7- Statystyki szlaków"));
        
        if (player.hasPermission("meklascase.trail.admin")) {
            player.sendMessage(Component.text("§c=== Komendy Administratora ===", NamedTextColor.RED));
            player.sendMessage(Component.text("§e/trail delete <nazwa> §7- Usuń szlak"));
            player.sendMessage(Component.text("§e/trail reload §7- Przeładuj szlaki"));
        }
    }
}