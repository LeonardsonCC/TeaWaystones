package ee.kurt.waystones.model;

import ee.kurt.waystones.Waystones;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Waystone implements Comparable {
    private String id;
    private String name;
    private boolean isPublic;
    private List<String> visitedBy;
    private Location location;
    private Location tplocation;
    private boolean isDeleted = false;
    private int sortorder;

    public Waystone(String id) {
        this.id = id;
    }

    public Waystone(String id, String name, boolean isPublic, List<String> visitedBy, Location location, Location tplocation, int sortorder) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.visitedBy = visitedBy;
        this.location = location;
        this.tplocation = tplocation;
        this.sortorder = sortorder;
    }

    public Waystone(Location location, Location tplocation) {
        this.location = location;
        this.tplocation = tplocation;
        this.isPublic = false;
        this.visitedBy = new ArrayList<>();
        this.id = Waystones.generateString(new Random(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", 10);
    }

    public static Waystone createWaystone(String id, String name, boolean isPublic, List<String> visitedBy, Location location, Location tplocation, int sortorder) {
        return new Waystone(id, name, isPublic, visitedBy, location.toCenterLocation(), tplocation, sortorder);
    }

    public void save() {
        if(this.isDeleted) {
            Waystones.locationconf.set("locations." + id, null);
        }else{
            Waystones.locationconf.set("locations." + id + ".name", this.name);
            Waystones.locationconf.set("locations." + id + ".visitedBy", this.visitedBy);
            Waystones.locationconf.set("locations." + id + ".public", this.isPublic);
            Waystones.locationconf.set("locations." + id + ".location", location);
            Waystones.locationconf.set("locations." + id + ".tplocation", tplocation);
            if(this.sortorder != 0)
                Waystones.locationconf.set("locations." + id + ".sortorder", this.sortorder);
        }
    }

    public ItemStack getMenuItem(Player player, Waystone currentWaystone) {
        ItemStack item = new ItemStack(Material.BEACON);
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Waystones.instance, "waystoneid");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);
        meta.displayName(Component.text("Waystone - "+this.name).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        if(player.hasPermission("waystones.menu.seeWaystoneIds")){
            lore.add(Component.text("ID: "+this.id).color(NamedTextColor.GRAY));
        }
        if(isPublic){
            lore.add(Component.text("Public").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
        }
        if (currentWaystone != null && currentWaystone.equals(this)) {
            meta.addEnchant(Enchantment.MENDING, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            lore.add(Component.text("You are here.").color(TextColor.color(145, 229, 110)).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean hasVisited(Player player) {
        return this.visitedBy.contains(player.getUniqueId().toString());
    }

    public void setVisitedBy(Player player) {
        if(!visitedBy.contains(player.getUniqueId().toString()))
            this.visitedBy.add(player.getUniqueId().toString());
    }

    public void delete() {
        Waystones.locationconf.set("locations." + id, null);
        this.isDeleted = true;
    }

    public void tpHere(Player player) {
        player.teleport(tplocation);
    }

    public Location getTplocation() {
        return tplocation;
    }

    public void setTplocation(Location tplocation) {
        this.tplocation = tplocation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getVisitedBy() {
        return visitedBy;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int getSortOrder() {
        return sortorder;
    }

    private void setSortOrder(int sortOrder) {
        this.sortorder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return this == o;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if(o instanceof Waystone otherWaystone) {
            if(otherWaystone.getSortOrder() != this.getSortOrder()) {
                return this.getSortOrder() > otherWaystone.getSortOrder() ? -1 : 1;
            }
            switch (Waystones.sortby) {
                case NAME:
                    String n1 = this.getName();
                    String n2 = otherWaystone.getName();
                    if (n1 == null && n2 == null)
                        return 0;
                    if(n1 == null)
                        return 1;
                    if(n2 == null)
                        return -1;
                    return n1.compareTo(n2);
                case ID:
                    String id1 = this.getId();
                    String id2 = otherWaystone.getId();
                    if (id1 == null && id2 == null)
                        return 0;
                    if(id1 == null)
                        return 1;
                    if(id2 == null)
                        return -1;
                    return id1.compareTo(id2);
                default:
                case NONE:
                    return 0;
            }
        }
        return 0;
    }

}
