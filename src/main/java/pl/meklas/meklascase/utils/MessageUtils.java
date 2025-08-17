package pl.meklas.meklascase.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.meklas.meklascase.MeklasCasePlugin;

import java.time.Duration;
import java.util.List;

public class MessageUtils {
    
    private final MeklasCasePlugin plugin;
    private final MiniMessage miniMessage;
    
    // Color schemes
    public static final String PRIMARY_GRADIENT = "<gradient:#00ff87:#60efff>";
    public static final String SECONDARY_GRADIENT = "<gradient:#667eea:#764ba2>";
    public static final String SUCCESS_GRADIENT = "<gradient:#56ab2f:#a8e6cf>";
    public static final String ERROR_GRADIENT = "<gradient:#ff416c:#ff4b2b>";
    public static final String WARNING_GRADIENT = "<gradient:#f093fb:#f5576c>";
    public static final String GOLD_GRADIENT = "<gradient:#ffd89b:#19547b>";
    
    // Icons and symbols
    public static final String SUCCESS_ICON = "✓";
    public static final String ERROR_ICON = "✗";
    public static final String WARNING_ICON = "⚠";
    public static final String INFO_ICON = "ℹ";
    public static final String STAR_ICON = "★";
    public static final String DIAMOND_ICON = "◆";
    public static final String ARROW_RIGHT = "→";
    public static final String ARROW_LEFT = "←";
    
    public MessageUtils(MeklasCasePlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    /**
     * Sends a beautifully formatted message to player
     */
    public void sendMessage(Player player, String message) {
        Component component = miniMessage.deserialize(message);
        player.sendMessage(component);
    }
    
    /**
     * Sends a success message with green gradient
     */
    public void sendSuccess(Player player, String message) {
        String formatted = SUCCESS_GRADIENT + SUCCESS_ICON + " " + message + "</gradient>";
        sendMessage(player, formatted);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }
    
    /**
     * Sends an error message with red gradient
     */
    public void sendError(Player player, String message) {
        String formatted = ERROR_GRADIENT + ERROR_ICON + " " + message + "</gradient>";
        sendMessage(player, formatted);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
    }
    
    /**
     * Sends a warning message with orange gradient
     */
    public void sendWarning(Player player, String message) {
        String formatted = WARNING_GRADIENT + WARNING_ICON + " " + message + "</gradient>";
        sendMessage(player, formatted);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.9f);
    }
    
    /**
     * Sends an info message with blue gradient
     */
    public void sendInfo(Player player, String message) {
        String formatted = SECONDARY_GRADIENT + INFO_ICON + " " + message + "</gradient>";
        sendMessage(player, formatted);
    }
    
    /**
     * Sends a premium/special message with gold gradient
     */
    public void sendPremium(Player player, String message) {
        String formatted = GOLD_GRADIENT + STAR_ICON + " " + message + " " + STAR_ICON + "</gradient>";
        sendMessage(player, formatted);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    }
    
    /**
     * Creates a beautiful header with title
     */
    public void sendHeader(Player player, String title) {
        String border = PRIMARY_GRADIENT + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>";
        String titleFormatted = "<center>" + PRIMARY_GRADIENT + "<bold>" + title + "</bold></gradient></center>";
        
        sendMessage(player, border);
        sendMessage(player, titleFormatted);
        sendMessage(player, border);
    }
    
    /**
     * Creates a beautiful footer
     */
    public void sendFooter(Player player) {
        String footer = PRIMARY_GRADIENT + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</gradient>";
        sendMessage(player, footer);
    }
    
    /**
     * Sends a title with subtitle to player
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = miniMessage.deserialize(title);
        Component subtitleComponent = miniMessage.deserialize(subtitle);
        
        Title titleObj = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(
                Duration.ofMillis(fadeIn * 50),
                Duration.ofMillis(stay * 50),
                Duration.ofMillis(fadeOut * 50)
            )
        );
        
        player.showTitle(titleObj);
    }
    
    /**
     * Sends an action bar message
     */
    public void sendActionBar(Player player, String message) {
        Component component = miniMessage.deserialize(message);
        player.sendActionBar(component);
    }
    
    /**
     * Broadcasts a message to all players with permission
     */
    public void broadcast(String message, String permission) {
        Component component = miniMessage.deserialize(message);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) {
                player.sendMessage(component);
            }
        }
    }
    
    /**
     * Creates a case win announcement
     */
    public void announceWin(Player player, String caseName, String itemName, int amount) {
        String message = PRIMARY_GRADIENT + "🎉 " + player.getName() + " wygrał " + 
                        GOLD_GRADIENT + itemName + " x" + amount + PRIMARY_GRADIENT + 
                        " z " + caseName + "! 🎉</gradient>";
        
        broadcast(message, null);
        
        // Special effects for the winner
        sendTitle(player, 
            SUCCESS_GRADIENT + "🎉 WYGRANA! 🎉</gradient>", 
            GOLD_GRADIENT + itemName + " x" + amount + "</gradient>", 
            10, 60, 20);
            
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.2f);
    }
    
    /**
     * Creates a top drop announcement
     */
    public void announceTopDrop(Player player, String caseName, String itemName, int amount) {
        String message = ERROR_GRADIENT + "💎 TOP DROP! " + player.getName() + " wygrał " + 
                        GOLD_GRADIENT + itemName + " x" + amount + ERROR_GRADIENT + 
                        " z " + caseName + "! 💎</gradient>";
        
        broadcast(message, null);
        
        // Extra special effects for top drop
        sendTitle(player, 
            ERROR_GRADIENT + "💎 TOP DROP! 💎</gradient>", 
            GOLD_GRADIENT + itemName + " x" + amount + "</gradient>", 
            15, 80, 25);
            
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 2.0f);
        
        // Firework effect for nearby players
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= 50) {
                nearby.playSound(nearby.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 0.8f, 1.0f);
            }
        }
    }
    
    /**
     * Creates a rotation change announcement
     */
    public void announceRotation(String caseName, String boostDescription) {
        String message = WARNING_GRADIENT + "🔄 Nowy dzień w " + caseName + "! " + 
                        PRIMARY_GRADIENT + boostDescription + "</gradient>";
        
        broadcast(message, null);
    }
    
    /**
     * Formats time remaining in a nice way
     */
    public String formatTimeRemaining(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * Creates a beautiful list display
     */
    public void sendList(Player player, String title, List<String> items) {
        sendHeader(player, title);
        
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String prefix = SECONDARY_GRADIENT + (i + 1) + ". " + ARROW_RIGHT + " </gradient>";
            sendMessage(player, prefix + PRIMARY_GRADIENT + item + "</gradient>");
        }
        
        sendFooter(player);
    }
    
    /**
     * Creates a progress bar
     */
    public String createProgressBar(double percentage, int length, String color) {
        int filled = (int) (percentage * length);
        int empty = length - filled;
        
        StringBuilder bar = new StringBuilder();
        bar.append(color);
        
        // Filled part
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        
        // Empty part
        bar.append("<gray>");
        for (int i = 0; i < empty; i++) {
            bar.append("░");
        }
        
        bar.append("</gray></gradient>");
        return bar.toString();
    }
    
    /**
     * Formats a percentage nicely
     */
    public String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
}