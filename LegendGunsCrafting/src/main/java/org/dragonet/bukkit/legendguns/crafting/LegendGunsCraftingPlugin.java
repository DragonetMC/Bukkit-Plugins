package org.dragonet.bukkit.legendguns.crafting;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.dragonet.bukkit.legendguns.LegendGunsPlugin;
import org.dragonet.bukkit.legendguns.config.magazine.MagazineType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/10/5.
 */
public class LegendGunsCraftingPlugin extends JavaPlugin implements Listener {

    private Map<NamespacedKey, String> permissionMap = new HashMap<>();

    @Override
    public void onEnable() {
        saveResource("lang.yml", false);
        Lang.lang = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "lang.yml"));

        getServer().getPluginManager().registerEvents(this, this);

        getDataFolder().mkdirs();
        File recipes_folder = new File(getDataFolder(), "recipes");
        recipes_folder.mkdirs();
        File[] configs = recipes_folder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".yml")));
        if(configs.length <= 0) {
            // no configs?
            // generate default ones
            saveResource("recipes/sample-config.yml", false);
            saveResource("recipes/another-sample.yml", false);
            saveResource("recipes/default-weapon-crafting.yml", false);
        }
        readRecipes(configs);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if(!Keyed.class.isAssignableFrom(e.getRecipe().getClass())) return;
        if(permissionMap.containsKey(((Keyed)e.getRecipe()).getKey())) {
            String required = permissionMap.get(((Keyed)e.getRecipe()).getKey());
            if(!e.getWhoClicked().hasPermission(required)) {
                e.setResult(Event.Result.DENY);
                e.setCurrentItem(null);
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(Lang.NO_CRAFTING_PERMISSION.build());
                e.getWhoClicked().sendMessage(Lang.PERMISSION_NOTIFICATION.build(required));
            }
        }
    }

    private void readRecipes(File[] files) {
        for(File f : files) {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            for(String recipe_name : c.getKeys(false)) {
                getLogger().info(String.format("Loading recipe named <%s>", recipe_name));
                try {
                    Recipe r = processRecipe(recipe_name, c.getConfigurationSection(recipe_name));
                    if(r != null) {
                        permissionMap.put(((Keyed)r).getKey(), String.format("legendguns.crafting.%s.%s", f.getName().substring(0, f.getName().length() - 4).toLowerCase(), recipe_name.toLowerCase()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().severe(String.format("Failed to load recipe <%s> from file [%s], due to: %s", recipe_name, f.getName(), e.getMessage()));
                }
            }
        }
    }

    private Recipe processRecipe(String recipe_name, ConfigurationSection config) throws Exception {
        Recipe r;
        if(config.getString("type").equalsIgnoreCase("SHAPED")) {
            r = processShapedRecipe(recipe_name, config);
        } else {
            r = processShapelessRecipe(recipe_name, config);
        }
        if(r != null) {
            getServer().addRecipe(r);
        }
        return r;
    }

    private ItemStack parseResultItem(ConfigurationSection section) throws Exception {
        if(!section.contains("result")) {
            throw new RuntimeException("No result defined! ");
        }
        if(!section.contains("result.type")) {
            throw new RuntimeException("No result type defined! ");
        }
        if(!section.contains("result.name")) {
            throw new RuntimeException("No result weapon/magazine name defined! ");
        }
        String type = section.getString("result.type").toUpperCase();
        String name = section.getString("result.name");
        if(type.equals("WEAPON")) {
            return LegendGunsPlugin.getInstance().getWeaponManager().generateWeaponItem(name);
        } else if(type.equals("MAGAZINE")) {
            MagazineType mtype = LegendGunsPlugin.getInstance().getMagazineManager().get(name);
            return mtype != null ? mtype.generateItem(1) : null;
        } else {
            throw new RuntimeException("Result item type can only be WEAPON or MAGAZINE! ");
        }
    }

    private Recipe processShapedRecipe(String recipe_name, ConfigurationSection config) throws Exception {
        ItemStack result = parseResultItem(config);
        if(result == null) {
            throw new RuntimeException("Failed to parse result item! ");
        }
        ShapedRecipe shaped = new ShapedRecipe(new NamespacedKey(this, String.format("CRAFTING:%s", recipe_name)), result);
        String[] shape = config.getStringList("matrix").toArray(new String[0]);
        shaped.shape(shape);
        ConfigurationSection ingredient_map = config.getConfigurationSection("ingredients");
        for(String ingredient : ingredient_map.getKeys(false)) {
            shaped.setIngredient(ingredient.charAt(0), Material.valueOf(ingredient_map.getString(ingredient)));
        }
        return shaped;
    }

    private Recipe processShapelessRecipe(String recipe_name, ConfigurationSection config) throws Exception {
        ItemStack result = parseResultItem(config);
        if(result == null) {
            throw new RuntimeException("Failed to parse result item! ");
        }
        ShapelessRecipe shapeless = new ShapelessRecipe(new NamespacedKey(this, String.format("CRAFTING:%s", recipe_name)), result);
        ConfigurationSection ingredient_map = config.getConfigurationSection("items");
        for(String material : ingredient_map.getKeys(false)) {
            shapeless.addIngredient(ingredient_map.getInt(material), Material.valueOf(material));
        }
        return shapeless;
    }
}
