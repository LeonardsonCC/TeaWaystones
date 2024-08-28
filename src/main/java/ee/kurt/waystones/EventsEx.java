package ee.kurt.waystones;

import ee.kurt.waystones.model.Waystone;
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

public class EventsEx implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;


        Player player = e.getPlayer();
        if(e.getClickedBlock() == null) return;
        Location loc = e.getClickedBlock().getLocation().toCenterLocation();


        if(e.getClickedBlock().getType() == Material.BEACON) {
            Waystone waystone = Waystones.manager.getWaystoneByLocation(loc);
            //PluginLogger.getLogger("TeaWaystones").log(Level.DEBUG, " - Accessting Beacon at " +loc);
            if (waystone != null) { // checks if the clicked beacon is a waystone
                //PluginLogger.getLogger("TeaWaystones").log(Level.DEBUG, " - Waystone found! " +waystone.getId());
                e.setCancelled(true);
                if (!waystone.hasVisited(player)) {
                    waystone.setVisitedBy(player);
                }

                Waystones.manager.openMenu(player, waystone, 0);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.isCancelled()){
            return;
        }
        Player player = e.getPlayer();
        Location loc = e.getBlockPlaced().getLocation();

        ItemMeta meta = e.getItemInHand().getItemMeta();

        if(meta != null){
            NamespacedKey key = new NamespacedKey(Waystones.instance, "id");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if(container.has(key, PersistentDataType.STRING)) {
                String itemid = container.get(key, PersistentDataType.STRING);
                if(itemid != null) {
                    if(itemid.equals("waystone")){
                        Waystones.manager.createWaystone(loc, player.getLocation());
                    }
                }
            }
        }
    }


    @EventHandler
    public void onInvClick(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if(item == null || item.isEmpty() || !item.hasItemMeta()){
            return;
        }

        if(e.getView().getTitle().contains("Waystones")){
            e.setCancelled(true);

            ItemMeta meta = item.getItemMeta();
            if(meta != null) {
                NamespacedKey key = new NamespacedKey(Waystones.instance, "waystoneid");
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if(container.has(key, PersistentDataType.STRING)) {
                    String idstr = container.get(key, PersistentDataType.STRING);
                    Waystone waystone = Waystones.manager.getWaystoneById(idstr);
                    Location loc = waystone.getTplocation();
                    if (loc == null) {
                        player.sendMessage(TextColor.color(255,0,0) + "Error: No location found for " + idstr);
                        return;
                    }
                    waystone.tpHere(player);
                }
                NamespacedKey nakey = new NamespacedKey(Waystones.instance, "navarrow");
                NamespacedKey currentWaystoneKey = new NamespacedKey(Waystones.instance, "currentwaystone");
                if (container.has(nakey, PersistentDataType.INTEGER)){
                    PersistentDataContainer arrowcontainer = meta.getPersistentDataContainer();
                    int pageid = arrowcontainer.get(nakey, PersistentDataType.INTEGER);
                    Waystone waystone = Waystones.manager.getWaystoneById(arrowcontainer.get(currentWaystoneKey, PersistentDataType.STRING));
                    Waystones.manager.openMenu(player, waystone, pageid);
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
            Waystone targetWaystone = null;
            for (Waystone waystone : Waystones.manager.getAllWaystones()) {
                Location l = waystone.getLocation();
                Location loc = e.getBlock().getLocation();

                if(loc.equals(l)) {
                    targetWaystone = waystone;
                }
            }
            if(targetWaystone == null) {return;}
            Waystones.manager.deleteWaystone(targetWaystone);
            e.setDropItems(false);
            ItemStack waystoneitem = new ItemStack(Material.BEACON, 1);
            ItemMeta meta = waystoneitem.getItemMeta();
            meta.displayName(Component.text("Waystone").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            NamespacedKey key = new NamespacedKey(Waystones.instance, "id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "waystone");
            waystoneitem.setItemMeta(meta);
            targetWaystone.getLocation().getWorld().dropItem(targetWaystone.getLocation().toCenterLocation(), waystoneitem);
        }
    }

}
