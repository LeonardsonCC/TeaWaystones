package ee.kurt.waystones;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TeaWaystones implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            // /tw setname <id> <name>
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "setname":
                        if(args.length == 3) {
                            String id = args[1];
                            String name = args[2];
                            try {
                                Waystones.locationconf.load(Waystones.locFile);
                                Waystones.locationconf.set("locations." + id + ".name", name);
                                Waystones.locationconf.save(Waystones.locFile);
                            } catch (IOException | InvalidConfigurationException exc) {
                                exc.printStackTrace();
                            }
                            sender.sendMessage("§aSuccsessfully set the name of the waypoint §o"+id+"§a to §o"+name+"§a.");
                            return true;
                        }
                    default:
                        sender.sendMessage("§cSub-Command not found. Syntax: /tw setname <id> <name>");
                        break;
            }
        }
        return false;

    }
}