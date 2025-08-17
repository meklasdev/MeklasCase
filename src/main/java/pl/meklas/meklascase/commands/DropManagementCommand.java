package pl.meklas.meklascase.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import pl.meklas.meklascase.MeklasCasePlugin;
import pl.meklas.meklascase.case.Case;
import pl.meklas.meklascase.gui.CaseSelectorGUI;
import pl.meklas.meklascase.gui.DropManagementGUI;
import pl.meklas.meklascase.utils.MessageUtils;

@Command(name = "dropmanager", aliases = {"dm", "drops", "dropsmanager"})
@Permission("meklascase.admin.drops")
public class DropManagementCommand {
    
    private final MeklasCasePlugin plugin;
    
    public DropManagementCommand(MeklasCasePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Execute
    public void execute(@Context Player player) {
        // Open case selector GUI
        new CaseSelectorGUI(plugin, player).open();
        MessageUtils.sendMessage(player, "§aOpening drop management interface...");
    }
    
    @Execute
    public void executeWithCase(@Context Player player, @OptionalArg String caseName) {
        if (caseName == null) {
            // Open case selector GUI
            new CaseSelectorGUI(plugin, player).open();
            MessageUtils.sendMessage(player, "§aOpening drop management interface...");
            return;
        }
        
        // Try to find the specific case
        Case targetCase = plugin.getCaseManager().getCase(caseName);
        if (targetCase == null) {
            MessageUtils.sendMessage(player, "§cCase '§e" + caseName + "§c' not found!");
            MessageUtils.sendMessage(player, "§7Available cases: §e" + 
                String.join("§7, §e", plugin.getCaseManager().getAllCases()
                    .stream()
                    .map(Case::getName)
                    .toArray(String[]::new)));
            return;
        }
        
        // Open drop management for specific case
        new DropManagementGUI(plugin, player, targetCase).open();
        MessageUtils.sendMessage(player, "§aOpening drop management for case: §e" + caseName);
    }
}