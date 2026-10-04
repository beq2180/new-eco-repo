package com.example.aioeconomy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ShopManager {
    public record ShopItem(String section, Material material, double buy, double sell) {}

    private final AIOEconomyPlugin plugin;
    private final Map<String, List<ShopItem>> sections = new LinkedHashMap<>();
    private File file;
    private String lastSection;

    public ShopManager(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    public void load() {
        file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection s = y.getConfigurationSection("sections");
        if (s == null) return;
        for (String section : s.getKeys(false)) {
            List<ShopItem> list = new ArrayList<>();
            for (String key : s.getConfigurationSection(section).getKeys(false)) {
                String path = "sections." + section + "." + key;
                Material m = Material.matchMaterial(y.getString(path + ".material", ""));
                if (m != null) list.add(new ShopItem(section, m, y.getDouble(path + ".buy"), y.getDouble(path + ".sell")));
            }
            sections.put(section, list);
            lastSection = section;
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (var e : sections.entrySet()) {
            int i = 0;
            for (ShopItem item : e.getValue()) {
                String path = "sections." + e.getKey() + ".item" + i++;
                y.set(path + ".material", item.material().name());
                y.set(path + ".buy", item.buy());
                y.set(path + ".sell", item.sell());
            }
        }
        try { y.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save shop: " + e.getMessage()); }
    }

    public void addSection(String name) {
        sections.putIfAbsent(name, new ArrayList<>());
        lastSection = name;
    }

    public boolean addItem(ItemStack stack, double buy, double sell) {
        if (lastSection == null || stack.getType().isAir()) return false;
        sections.get(lastSection).add(new ShopItem(lastSection, stack.getType(), buy, sell));
        return true;
    }

    public Set<String> sections() { return sections.keySet(); }
    public List<ShopItem> items(String section) { return sections.getOrDefault(section, List.of()); }
    public String lastSection() { return lastSection; }
}
