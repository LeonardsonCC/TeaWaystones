package ee.kurt.waystones.commands;

import ee.kurt.waystones.WaystoneManager;
import ee.kurt.waystones.model.Waystone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TwTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        final List<String> completions = new ArrayList<String>();

        // TODO: check permissions
        if (args.length == 1) {
            completions.addAll(TwCommandOptions.all);
            return completions;
        }

        switch (args[0]) {
            case TwCommandOptions.setName:
                if (args.length == 1) {
                    List<Waystone> waystones = WaystoneManager.getInstance().getWaystoneListForPlayer((Player) commandSender);
                    for (Waystone waystone : waystones) {
                        completions.add(waystone.getId());
                    }
                    return completions;
                }
                if (args.length == 2) {
                    return completions;
                }
        }

        return completions;
    }
}
