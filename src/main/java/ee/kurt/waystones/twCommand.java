package ee.kurt.waystones;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class twCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            // /tw setname <id> <name>
            if(args.length == 0) {
                sender.sendMessage(Component.text("Syntax: \n/tw setname <name>\n/tw setpublic <true/false>\n/tw openui [page]\n/tw list").color(TextColor.color(255, 0, 0)));
                return true;
            }
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "setname":
                        if(args.length == 2) {
                            String name = args[1];
                            String id = "";
                            for (String path : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                                Location possibleWaystoneLocation = Waystones.locationconf.getLocation("locations." + path + ".location").toCenterLocation();
                                Location playerLocation = ((Player) sender).getLocation();
                                if(possibleWaystoneLocation.getWorld().equals(playerLocation.getWorld()) && playerLocation.distance(possibleWaystoneLocation) < 2) {
                                    id = path;
                                }
                            }
                            if(id.isBlank()){
                                sender.sendMessage(Component.text("There is no waystone near you.").color(TextColor.color(255, 0, 0)));
                                return true;
                            }

                            try {
                                Waystones.locationconf.load(Waystones.locFile);
                                Waystones.locationconf.set("locations." + id + ".name", name);
                                Waystones.locationconf.save(Waystones.locFile);
                            } catch (IOException | InvalidConfigurationException exc) {
                                exc.printStackTrace();
                            }
                            sender.sendMessage("§aSuccessfully set the name of the waystone §o"+id+"§a to §o"+name+"§a.");
                            return true;
                        }
                        break;
                    case "openui":
                        if(sender.isOp() || sender.hasPermission("waystones.command.openui")){
                            int page = 0;
                            if(args.length >= 2 && args[1] != null) {
                                try {
                                    page = parseInt(args[1]);
                                } catch (NumberFormatException e){
                                    sender.sendMessage(Component.text("Error: Not a number!").color(TextColor.color(255,0,0)));
                                    page = 0;
                                }
                            }
                            Waystones.openMenu((Player) sender, "", page);
                        } else {
                            sender.sendMessage(Component.text("Permission denied.").color(TextColor.color(255, 0, 0)));
                        }
                        return true;
                    case "setpublic":
                        if(sender.isOp() || sender.hasPermission("waystones.command.setpublic")){
                            if(args.length == 2) {
                                String value = args[1];
                                String id = "";
                                for (String path : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                                    Location possibleWaystoneLocation = Waystones.locationconf.getLocation("locations." + path + ".location").toCenterLocation();
                                    Location playerLocation = ((Player) sender).getLocation();
                                    if(possibleWaystoneLocation.getWorld().equals(playerLocation.getWorld()) && playerLocation.distance(possibleWaystoneLocation) < 2) {
                                        id = path;
                                    }
                                }
                                if(id.isBlank()){
                                    sender.sendMessage(Component.text("There is no waystone near you.").color(TextColor.color(255, 0, 0)));
                                    return true;
                                }

                                boolean boolvalue = false;

                                if(value.equalsIgnoreCase("true")) {
                                    boolvalue = true;
                                    sender.sendMessage("§aSuccessfully set the visibility of waystone §o"+Waystones.locationconf.getString("locations." + id + ".name")+"§a (§o"+id+"§a) to public. Everyone will be able to travel to this waystone, regardless of weather they used it before.");
                                }else{
                                    sender.sendMessage("§aSuccessfully set the visibility of waystone §o"+Waystones.locationconf.getString("locations." + id + ".name")+"§a (§o"+id+"§a) to private. Only people who already used this waystone will be able to travel to it.");
                                }

                                try {
                                    Waystones.locationconf.load(Waystones.locFile);
                                    Waystones.locationconf.set("locations." + id + ".public", boolvalue);
                                    Waystones.locationconf.save(Waystones.locFile);
                                } catch (IOException | InvalidConfigurationException exc) {
                                    exc.printStackTrace();
                                }
                                return true;
                            }
                        } else {
                            sender.sendMessage(Component.text("Permission denied.").color(TextColor.color(255, 0, 0)));
                            return true;
                        }
                        break;
                    case "list":
                        if(sender.hasPermission("waystones.command.list")) {
                            Component text = Component.text("=== List of Waystones ===").color(NamedTextColor.GREEN);
                            for (String path : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                                Location tplocation = Waystones.locationconf.getLocation("locations." + path + ".tplocation");
                                List<String> visitedBy = Waystones.locationconf.getStringList("locations." + path + ".visitedBy");
                                if(tplocation == null)
                                    continue;
                                text = text.appendNewline();
                                text = text.append(Component.text(Waystones.locationconf.getString("locations." + path + ".name")+" ("+path+") "+ (tplocation != null ? tplocation.toString() : TextColor.color(255,0,0)+"unknown location") +" "+visitedBy).color(NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/tp @s "+tplocation.x()+" "+tplocation.y()+" "+tplocation.z()+" "+tplocation.getYaw()+" "+tplocation.getPitch())));
                            }
                            sender.sendMessage(text);
                            return true;
                        } else {
                            sender.sendMessage(Component.text("Permission denied.").color(TextColor.color(255, 0, 0)));
                            return true;
                        }
                }
        }
        sender.sendMessage(Component.text("Syntax: \n/tw setname <name>\n/tw setpublic <true/false>\n/tw openui [page]\n/tw list").color(TextColor.color(255, 0, 0)));
        return true;

    }
}