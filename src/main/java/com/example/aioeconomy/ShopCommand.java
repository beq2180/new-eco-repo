package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ShopCommand implements CommandExecutor {
    private final AIOEconomyPlugin plugin;
    public ShopCommand(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) openSections(player);
        else openSection(player, String.join(" ", args));
        return true;
    }

    public void openSections(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Shop");
        int slot = 0;
        for (String section : plugin.shop().sections()) {
            if (slot >= 45) break;
            ItemStack icon = new ItemStack(iconFor(section));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§6" + section);
            meta.setLore(List.of("§7Click to browse this section."));
            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
        }
        player.openInventory(inv);
    }

    public void openSection(Player player, String section) {
        if (!plugin.shop().sections().contains(section)) {
            player.sendMessage("§cShop section not found. Use /shop to see the available sections.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, "Shop: " + section);
        int slot = 0;
        for (var item : plugin.shop().items(section)) {
            if (slot >= 45) break;
            ItemStack display = new ItemStack(item.material());
            ItemMeta meta = display.getItemMeta();
            meta.setDisplayName("§f" + pretty(item.material()));
            List<String> lore = new ArrayList<>();
            lore.add("§aBuy: §f$" + GuiListener.money(item.buy()));
            lore.add("§cSell: §f$" + GuiListener.money(item.sell()));
            lore.add("§7Left-click: buy 1");
            lore.add("§7Right-click: sell 1");
            display.setItemMeta(meta);
            meta = display.getItemMeta();
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slot++, display);
        }
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName("§cBack");
        back.setItemMeta(meta);
        inv.setItem(49, back);
        player.openInventory(inv);
    }

    private Material iconFor(String section) {
        var items = plugin.shop().items(section);
        return items.isEmpty() ? Material.CHEST : items.get(0).material();
    }

    public static String pretty(Material m) {
        String[] parts = m.name().toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        return out.toString().trim();
    }
}
