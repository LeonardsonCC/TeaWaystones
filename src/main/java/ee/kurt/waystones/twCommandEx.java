package ee.kurt.waystones;

import ee.kurt.waystones.model.Waystone;
import net.kyori.adventure.text.Component;
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


public class twCommandEx implements CommandExecutor {

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
                            Waystone targetWaystone = Waystones.manager.getClosestWaystone(((Player) sender).getLocation());

                            if(targetWaystone == null){
                                sender.sendMessage(Component.text("There is no waystone near you.").color(TextColor.color(255, 0, 0)));
                                return true;
                            }

                            targetWaystone.setName(name);
                            sender.sendMessage("§aSuccessfully set the name of the waystone §o"+targetWaystone.getId()+"§a to §o"+name+"§a.");
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
                                }
                            }
                            Waystones.manager.openMenu((Player) sender, null, page);
                        } else {
                            sender.sendMessage(Component.text("Permission denied.").color(TextColor.color(255, 0, 0)));
                        }
                        return true;
                    case "setpublic":
                        if(sender.isOp() || sender.hasPermission("waystones.command.setpublic")){
                            if(args.length == 2) {
                                String value = args[1];

                                Waystone targetWaystone = Waystones.manager.getClosestWaystone(((Player) sender).getLocation());

                                if(targetWaystone == null){
                                    sender.sendMessage(Component.text("There is no waystone near you.").color(TextColor.color(255, 0, 0)));
                                    return true;
                                }

                                boolean boolvalue = false;

                                if(value.equalsIgnoreCase("true")) {
                                    boolvalue = true;
                                    sender.sendMessage("§aSuccessfully set the visibility of waystone §o"+targetWaystone.getName()+"§a (§o"+targetWaystone.getId()+"§a) to public. Everyone will be able to travel to this waystone, regardless of weather they used it before.");
                                }else{
                                    sender.sendMessage("§aSuccessfully set the visibility of waystone §o"+targetWaystone.getName()+"§a (§o"+targetWaystone.getId()+"§a) to private. Only people who already used this waystone will be able to travel to it.");
                                }

                                targetWaystone.setPublic(boolvalue);
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
                            for (Waystone waystone : Waystones.manager.getAllWaystones()) {
                                Location tplocation = waystone.getTplocation();
                                List<String> visitedBy = waystone.getVisitedBy();
                                if(tplocation == null)
                                    continue;
                                text = text.appendNewline();
                                text = text.append(Component.text(waystone.getName()+" ("+waystone.getId()+") "+ (tplocation != null ? tplocation.toString() : TextColor.color(255,0,0)+"unknown location") +" "+visitedBy).color(NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/tp @s "+tplocation.x()+" "+tplocation.y()+" "+tplocation.z()+" "+tplocation.getYaw()+" "+tplocation.getPitch())));
                            }
                            sender.sendMessage(text);
                            return true;
                        } else {
                            sender.sendMessage(Component.text("Permission denied.").color(TextColor.color(255, 0, 0)));
                            return true;
                        }
                    case "loadfromfile":
                        if(sender.hasPermission("waystones.command.reload") || sender.isOp()) {
                            Waystones.manager.reload();
                            return true;
                        }
                        return false;
                    case "savetofile":
                        if(sender.hasPermission("waystones.command.reload") || sender.isOp()) {
                            Waystones.manager.saveToFile();
                            return true;
                        }
                        return false;
                }
        }
        sender.sendMessage(Component.text("Syntax: \n/tw setname <name>\n/tw setpublic <true/false>\n/tw openui [page]\n/tw list").color(TextColor.color(255, 0, 0)));
        return true;

    }
}