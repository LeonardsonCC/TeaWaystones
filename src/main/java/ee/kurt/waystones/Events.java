package ee.kurt.waystones;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class Events implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
      //  System.out.println("[Waystones Debugger] Player Interact Event.");


        Player p = e.getPlayer();
        Location loc = Objects.requireNonNull(e.getClickedBlock()).getLocation();

       // System.out.println("[Waystones Debugger] Player Interact Event. "+ p.getUniqueId() + " | "+ loc);

        if(e.getClickedBlock().getType() == Material.BEACON) {
           // System.out.println("[Waystones Debugger] Material Check success.");
            try {
                Waystones.locationconf.load(Waystones.locFile);
                for (String path : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                    Location l = Waystones.locationconf.getLocation("locations." + path + ".location");

                   // System.out.println("[Waystones Debugger] "+loc.equals(l)+" | "+l.toBlockLocation().equals(loc.toBlockLocation())+" | "+(l.getBlockX() == loc.getBlockX() && l.getBlockY() == loc.getBlockY() && l.getBlockZ() == loc.getBlockZ() && l.getWorld() == loc.getWorld() ));
                    if (loc.equals(l)) { // checks if the current waystone is the clicked block
                        e.setCancelled(true);
                        List<String> visitedBy = Waystones.locationconf.getStringList("locations." + path + ".visitedBy");
                        if (visitedBy.contains(p.getUniqueId().toString())) {
                           // System.out.println("[Waystones Debugger] @" + path + " Player [" + p.getUniqueId() + "] has already visited the current waystone.");
                        } else {
                            //System.out.println("[Waystones Debugger] @" + path + " Player [" + p.getUniqueId() + "] has not visited the current waystone, yet. Adding...");
                            visitedBy.add(p.getUniqueId().toString());
                            Waystones.locationconf.set("locations." + path + ".visitedBy", visitedBy);
                            Waystones.locationconf.save(Waystones.locFile);
                        }

                        Waystones.openMenu(p, path, 0);
                    }
                }
            } catch (IOException | InvalidConfigurationException exc) {
                exc.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.isCancelled()){
            return;
        }
        Player p = e.getPlayer();
        Location loc = e.getBlockPlaced().getLocation();
        String id = Waystones.generateString(new Random(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", 10);

        ItemMeta meta = e.getItemInHand().getItemMeta();

        if(meta != null){

            NamespacedKey key = new NamespacedKey(Waystones.instance, "id");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if(container.has(key, PersistentDataType.STRING)) {
                    String itemid = container.get(key, PersistentDataType.STRING);
                    if(itemid.equals("waystone")){
                        try {
                            Waystones.locationconf.load(Waystones.locFile);

                            Waystones.locationconf.set("locations."+id+".location", loc);
                            Waystones.locationconf.set("locations."+id+".tplocation", p.getLocation());

                            Waystones.locationconf.save(Waystones.locFile);
                        } catch (IOException | InvalidConfigurationException exc) {
                            exc.printStackTrace();
                        }
                    }
            }
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if(item == null || item.isEmpty() || !item.hasItemMeta()){
            return;
        }
        //System.out.println("[Waystones Debugger] Inv Click Event. "+p.getUniqueId() + " | "+item.getType());

        if(e.getView().getTitle().contains("Waystones")){
            e.setCancelled(true);
            //System.out.println("[Waystones Debugger] Player [" + p.getUniqueId() + "] clicked on the following waystone:");

            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(Waystones.instance, "waystoneid");
            if(meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if(container.has(key, PersistentDataType.STRING)) {
                    String idstr = container.get(key, PersistentDataType.STRING);
                    try {
                        Waystones.locationconf.load(Waystones.locFile);

                        Location loc = Waystones.locationconf.getLocation("locations."+idstr+".tplocation");
                        if (loc == null) {
                            p.sendMessage(TextColor.color(255,0,0) + "Error: No location found for " + idstr);
                            return;
                        }
                        p.teleport(loc);


                    } catch (IOException | InvalidConfigurationException exc) {
                        PluginLogger.getLogger("TeaWaystones").log(Level.WARNING, "Configuration file not found, not accessible or invalid.");
                    }
                }
                NamespacedKey nakey = new NamespacedKey(Waystones.instance, "navarrow");
                NamespacedKey currentWaystoneKey = new NamespacedKey(Waystones.instance, "currentwaystone");
                if (container.has(nakey, PersistentDataType.INTEGER)){
                    PersistentDataContainer arrowcontainer = meta.getPersistentDataContainer();
                    int pageid = arrowcontainer.get(nakey, PersistentDataType.INTEGER);
                    String currentWaystoneId = arrowcontainer.get(currentWaystoneKey, PersistentDataType.STRING);
                    // System.out.println(pageid);
                    Waystones.openMenu(p, currentWaystoneId, pageid);
                }
            }else{
                PluginLogger.getLogger("TeaWaystones").log(Level.WARNING, "Meta is null.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        if(e.isCancelled()){
            return;
        }
        if(e.getBlock().getType() == Material.BEACON) {

            try {
                Waystones.locationconf.load(Waystones.locFile);
                for (String path : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                    Location l = Waystones.locationconf.getLocation("locations." + path + ".location");
                    Location loc = e.getBlock().getLocation();

                    if(loc.equals(l)){
                        e.setDropItems(false);
                        ItemStack waystone = new ItemStack(Material.BEACON, 1);
                        ItemMeta meta = waystone.getItemMeta();
                        meta.displayName(Component.text("Waystone").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                        NamespacedKey key = new NamespacedKey(Waystones.instance, "id");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "waystone");
                        waystone.setItemMeta(meta);
                        loc.getWorld().dropItem(loc, waystone);
                        Waystones.locationconf.set("locations." + path, null);
                        Waystones.locationconf.save(Waystones.locFile);
                    }
                }
            } catch (IOException | InvalidConfigurationException exc) {
                PluginLogger.getLogger("TeaWaystones").log(Level.WARNING, "Configuration file not found, not accessible or invalid.");
            }
        }
    }

}
