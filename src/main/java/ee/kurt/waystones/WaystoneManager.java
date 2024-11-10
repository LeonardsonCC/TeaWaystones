package ee.kurt.waystones;

import ee.kurt.waystones.model.Waystone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginLogger;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

@Singleton
public class WaystoneManager {

    private static List<Waystone> waystones;
    private static WaystoneManager instance;

    WaystoneManager() {
        waystones = new ArrayList<Waystone>();
    }

    public static WaystoneManager getInstance(){
        if(instance == null){
            instance = new WaystoneManager();
        }
        return instance;
    }

    public Waystone getWaystoneByLocation(Location location) {
        for(Waystone waystone : waystones){
            if(waystone.getLocation().toCenterLocation().equals(location.toCenterLocation())) {
                return waystone;
            }
        }
        return null;
    }

    public Waystone getWaystoneById(String id) {
        for(Waystone waystone : waystones){
            if(waystone.getId().equals(id)) {
                return waystone;
            }
        }
        return null;
    }

    public Waystone getClosestWaystone(Location location) {
        Waystone targetWaystone = null;
        for (Waystone waystone : waystones) {
            Location possibleWaystoneLocation = waystone.getLocation().toCenterLocation();
            if(possibleWaystoneLocation.getWorld().equals(location.getWorld()) && location.distance(possibleWaystoneLocation) < 2) {
                targetWaystone = waystone;
            }
        }
        return targetWaystone;
    }

    public void createWaystone(Location location, Location tplocation) {
        waystones.add(new Waystone(location, tplocation));
    }

    public void deleteWaystone(Waystone waystone) {
        waystone.delete();
        waystones.remove(waystone);
    }

    public void openMenu(Player player, Waystone currentWaystone, int page) {
        Inventory inv = Bukkit.createInventory(player, 9*6, Component.text("Waystones").color(TextColor.color(0,0,0)));

        int i = 0;
        List<Waystone> pwl = getWaystoneListForPlayer(player);
        Collections.sort(pwl);
        List<List<Waystone>> lss = getSubsetsBySize(pwl, 53);
        // printSetList(lss);
        if(lss.isEmpty()) {
            player.sendMessage(Component.text("Error: No waystones found.").color(TextColor.color(255,0,0)));
            return;
        }
        if(lss.size() < page+1){
            player.sendMessage(Component.text("Error: Page not found.").color(TextColor.color(255,0,0)));
            return;
        }

        for (Waystone waystone : lss.get(page)) {
            if (waystone.hasVisited(player) || waystone.isPublic()) {
                ItemStack item = waystone.getMenuItem(player, currentWaystone);
                inv.setItem(i, item);
                i++;
            }
        }

        inv.clear(53);
        ItemStack item = new ItemStack(Material.TIPPED_ARROW);
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Waystones.instance, "navarrow");
        NamespacedKey currentWaystoneKey = new NamespacedKey(Waystones.instance, "currentwaystone");

        if(lss.get(page).size() == 53) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, page+1);
            meta.displayName(Component.text("Next Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        } else {
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
            meta.displayName(Component.text("First Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        }
        if(currentWaystone != null) {
            meta.getPersistentDataContainer().set(currentWaystoneKey, PersistentDataType.STRING, currentWaystone.getId());
            meta.addEnchant(Enchantment.MENDING, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        }
        item.setItemMeta(meta);
        inv.setItem(53, item);

        player.openInventory(inv);
    }

    public List<Waystone> getWaystoneListForPlayer(Player player) {
        List<Waystone> list = new ArrayList<>();
        for(Waystone waystone : waystones){
            if(waystone.isPublic() || waystone.hasVisited(player)){
                list.add(waystone);
            }
        }
        return list;
    }

    private static List<List<Waystone>> getSubsetsBySize(List<Waystone> set, int size) {
        List<List<Waystone>> ret = new ArrayList<>();
        List<Waystone> tmp = new ArrayList<>();

        for (int i = 0; i < set.size(); i++) {
            if (tmp.size() == size) {

                ret.add(new ArrayList<>(tmp));
                tmp.clear();
            }
            tmp.add(set.get(i));
        }

        if (!tmp.isEmpty()) {
            ret.add(new ArrayList<>(tmp)); // Add the last subset
        }
        return ret;
    }

    public void loadFromFile(){
        try {
            Waystones.locationconf.load(Waystones.locFile);
            ConfigurationSection sec = Waystones.locationconf.getConfigurationSection("locations");

            if(sec != null && !sec.getKeys(false).isEmpty()) {
                for (String id : Waystones.locationconf.getConfigurationSection("locations").getKeys(false)) {
                    try {
                        PluginLogger.getLogger("TeaWaystones").log(Level.INFO, " - Loading Waystone " + id);
                        Location tplocation = Waystones.locationconf.getLocation("locations." + id + ".tplocation");
                        Location location = Waystones.locationconf.getLocation("locations." + id + ".location");
                        String name = Waystones.locationconf.getString("locations." + id + ".name");
                        boolean isPublic = Waystones.locationconf.getBoolean("locations." + id + ".public");
                        List<String> visitedBy = Waystones.locationconf.getStringList("locations." + id + ".visitedBy");
                        int sortorder = Waystones.locationconf.getInt("locations." + id + ".sortorder");
                        Waystone waystone = Waystone.createWaystone(id, name, isPublic, visitedBy, location, tplocation, sortorder);
                        //PluginLogger.getLogger("TeaWaystones").log(Level.DEBUG, " - WAYSTONE " + waystone.getName() + " " + waystone.getId() + " "+ waystone.getLocation() + " " + waystone.getTplocation());
                        waystones.add(waystone);
                        //PluginLogger.getLogger("TeaWaystones").log(Level.DEBUG, waystones.toString());
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        PluginLogger.getLogger("TeaWaystones").log(Level.WARNING, "Could not load Waystone " + id, exception);
                    }
                }
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveToFile(){
        try {
            for (Waystone waystone : waystones) {
                waystone.save();
            }
            Waystones.locationconf.save(Waystones.locFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Waystone> getAllWaystones() {
        return waystones;
    }

    public void clearAll(Player player) {
        List<Waystone> waystones = getWaystoneListForPlayer(player);
        for (Waystone waystone : waystones) {
            deleteWaystone(waystone);
        }
    }

    public void reload() {
        waystones.clear();
        this.loadFromFile();
    }
}
