package ee.kurt.waystones;

import ee.kurt.waystones.model.SortCriteria;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Waystones extends JavaPlugin {

      public static File path = new File("plugins//TeaWaystones");
      public static File locFile = new File("plugins//TeaWaystones//locations.yml");
      public static YamlConfiguration locationconf;
      public static Waystones instance;
      public static WaystoneManager manager = WaystoneManager.getInstance();
      public static SortCriteria sortby = SortCriteria.NAME;


      public static String generateString(Random rng, String characters, int length) {
            char[] text = new char[length];
            for (int i = 0; i < length; i++) {
                  text[i] = characters.charAt(rng.nextInt(characters.length()));
            }
            return new String(text);
      }

      public static void openMenu(Player p, String currentWaystoneId, int page){
            Inventory inv = Bukkit.createInventory(p, 9*6, Component.text("Waystones").color(TextColor.color(0,0,0)));

            try {
                  locationconf.load(locFile);
                  int i = 0;
                  List<String> ss = locationconf.getConfigurationSection("locations").getKeys(false).stream().sorted(Waystones::compareWaystone).toList();
                  List<List<String>> lss = getSubsetsBySize(ss, 53);
                  // printSetList(lss);
                  if(lss.isEmpty()) {
                        p.sendMessage(Component.text("Error: No waystones found.").color(TextColor.color(255,0,0)));
                        return;
                  }
                  if(lss.size() < page+1){
                        p.sendMessage(Component.text("Error: Page not found.").color(TextColor.color(255,0,0)));
                        return;
                  }
                  for (String path : lss.get(page)) {
                        String name = locationconf.getString("locations." + path + ".name");
                        List<String> visitedBy = Waystones.locationconf.getStringList("locations." + path + ".visitedBy");
                        boolean isPublic = locationconf.getBoolean("locations." + path + ".public");
                        if (visitedBy.contains(p.getUniqueId().toString()) || isPublic) {
                            ItemStack item = new ItemStack(Material.BEACON);
                            item.setAmount(1);
                            ItemMeta meta = item.getItemMeta();
                            NamespacedKey key = new NamespacedKey(Waystones.instance, "waystoneid");
                            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, path);
                            meta.displayName(Component.text("Waystone - "+name).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                            List<Component> lore = new ArrayList<>();
                            if(p.hasPermission("waystones.menu.seeWaystoneIds")){
                                  lore.add(Component.text("ID: "+path).color(NamedTextColor.GRAY));
                            }
                            if(isPublic){
                                  lore.add(Component.text("Public").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
                            }
                            if (currentWaystoneId.equals(path)) {
                                  meta.addEnchant(Enchantment.MENDING, 1, false);
                                  meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                  lore.add(Component.text("You are here.").color(TextColor.color(145, 229, 110)).decoration(TextDecoration.ITALIC, false));
                            }
                            meta.lore(lore);
                            item.setItemMeta(meta);
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
                  meta.getPersistentDataContainer().set(currentWaystoneKey, PersistentDataType.STRING, currentWaystoneId);
                  meta.addEnchant(Enchantment.MENDING, 1, false);
                  meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                  meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                  item.setItemMeta(meta);
                  inv.setItem(53, item);
                  i++;

                  p.openInventory(inv);
            } catch (IOException | InvalidConfigurationException exc) {
                  exc.printStackTrace();
            }
      }

      public static int compareWaystone(String w1, String w2){
            if(w1 == null || w2 == null){
                  return 0;
            }
            int result = 0;
            try {
                  w1 = locationconf.getString("locations." + w1 + ".name");
                  w2 = locationconf.getString("locations." + w2 + ".name");
                  if(w1 == null || w2 == null){
                        return 0;
                  }
                  result = w1.compareTo(w2);
            }catch (Exception e){
                  e.printStackTrace();
            } finally {
                  return result;
            }
      }


      public static List<List<String>> getSubsetsBySize(List<String> set, int size) {
            List<String> seta = new ArrayList<>(set); // Convert set to list
            List<List<String>> ret = new ArrayList<>();
            List<String> tmp = new ArrayList<>();

            for (int i = 0; i < seta.size(); i++) {
                  if (tmp.size() == size) {

                        ret.add(new ArrayList<>(tmp));
                        tmp.clear();
                  }
                  tmp.add(seta.get(i));
            }

            if (!tmp.isEmpty()) {
                  ret.add(new ArrayList<>(tmp)); // Add the last subset
            }
            return ret;
      }


      public static void printSet(List<String> set){
            System.out.println("=== SET ===");
            for(String e : set){
                  System.out.println(e);
            }
            System.out.println("=== END SET ===");
      }
      public static void printSetList(List<List<String>> set){
            System.out.println("=== SET ===");
            for(List<String> e : set){
                  System.out.println("  === SUBSET ===");
                  for(String s : e) {
                        System.out.println(s);
                  }
                  System.out.println("  === END SUBSET ===");
            }
            System.out.println("=== END SET ===");
      }

      @Override
      public void onEnable() {
            if(!path.exists()) path.mkdir();
            if(!locFile.exists()) this.saveResource("locations.yml", true);
            locationconf = YamlConfiguration.loadConfiguration(locFile);
            instance = this;
            manager.loadFromFile();

            getCommand("tw").setTabCompleter(
                    new TabCompleter() {
                          @Override
                          public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
                                final List<String> completions = new ArrayList<String>();

                                if (args.length == 1) {
                                    completions.addAll(List.of("setname", "setpublic", "openui", "list"));
                                    return completions;
                                }

                                return completions;
                          }
                    }
            );
            getCommand("tw").setExecutor(new twCommandEx());

            Bukkit.getPluginManager().registerEvents(new EventsEx(), this);

            ItemStack waystone = new ItemStack(Material.BEACON, 1);
            ItemMeta meta = waystone.getItemMeta();
            meta.displayName(Component.text("Waystone").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            NamespacedKey key = new NamespacedKey(Waystones.instance, "id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "waystone");
            waystone.setItemMeta(meta);
            ShapedRecipe waystonerec = new ShapedRecipe(key, waystone);

            waystonerec.shape("OEO","EBE","OCO");

            waystonerec.setIngredient('O', Material.OBSIDIAN);
            waystonerec.setIngredient('E', Material.ENDER_EYE);
            waystonerec.setIngredient('C', Material.COMPASS);
            waystonerec.setIngredient('B', Material.LIGHT_BLUE_CANDLE);

            getServer().addRecipe(waystonerec);
      }


      @Override
      public void onDisable() {
            manager.saveToFile();
      }
}
