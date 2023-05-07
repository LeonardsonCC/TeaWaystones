package ee.kurt.waystones;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Waystones extends JavaPlugin {

      public static File path = new File("plugins//TeaWaystones");
      public static File locFile = new File("plugins//TeaWaystones//locations.yml");
      public static YamlConfiguration locationconf = YamlConfiguration.loadConfiguration(locFile);
      public static Waystones instance;


      public static String generateString(Random rng, String characters, int length) {
            char[] text = new char[length];
            for (int i = 0; i < length; i++)
            {
                  text[i] = characters.charAt(rng.nextInt(characters.length()));
            }
            return new String(text);
      }

      public static void openMenu(Player p, String currentWaystoneId){
            Inventory inv = Bukkit.createInventory(p,9*6, "Waystones");

            try {
                  locationconf.load(locFile);
                  int i = 0;
                  for (String path : locationconf.getConfigurationSection("locations").getKeys(false)) {
                        String name = locationconf.getString("locations." + path + ".name");
                        List<String> visitedBy = Waystones.locationconf.getStringList("locations." + path + ".visitedBy");
                        if (visitedBy.contains(p.getUniqueId().toString())) {
                             // System.out.println("[Waystones Debugger] Menu: Player [" + p.getUniqueId() + "] has already visited the waystone ["+path+"].");
                              ItemStack item = new ItemStack(Material.BEACON);
                              item.setAmount(1);
                              ItemMeta meta = item.getItemMeta();
                              meta.setDisplayName("§6Waystone - "+name);
                              List<String> lore = new ArrayList<>();
                              lore.add("ID: "+path);
                              if(p.getLocation() == Waystones.locationconf.getLocation("locations." + path + ".location")) {
                                    meta.addEnchant(Enchantment.MENDING, 1, false);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                    lore.add("§cYou are here.");
                              }
                              meta.setLore(lore);
                              item.setItemMeta(meta);
                              inv.setItem(i, item);
                              i++;
                        }
                  }
                  p.openInventory(inv);
            } catch (IOException | InvalidConfigurationException exc) {
                  exc.printStackTrace();
            }
      }

      public void onEnable() {
            if(!path.exists()) path.mkdir();
            if(!locFile.exists()) this.saveResource("locations.yml", true);

            getCommand("tw").setExecutor(new TeaWaystones());
            Bukkit.getPluginManager().registerEvents(new Events(), this);

            ItemStack waystone = new ItemStack(Material.BEACON, 1);
            ItemMeta meta = waystone.getItemMeta();
            meta.setDisplayName("§aWaystone");
            waystone.setItemMeta(meta);
            ShapedRecipe waystonerec = new ShapedRecipe(waystone);

            waystonerec.shape("OEO","EBE","OCO");

            waystonerec.setIngredient('O', Material.OBSIDIAN);
            waystonerec.setIngredient('E', Material.ENDER_EYE);
            waystonerec.setIngredient('C', Material.COMPASS);
            waystonerec.setIngredient('B', Material.LIGHT_BLUE_CANDLE);

            getServer().addRecipe(waystonerec);
      }
}
