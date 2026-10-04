package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

public final class SellCommand implements CommandExecutor {
    private final AIOEconomyPlugin p;
    public SellCommand(AIOEconomyPlugin p) { this.p = p; }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player player)) return true;
        if (a.length == 0 || a[0].equalsIgnoreCase("gui")) {
            Inventory inv = Bukkit.createInventory(null, 27, "Sell GUI");
            int slot = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir() && slot < 27) inv.setItem(slot++, item.clone());
            }
            player.openInventory(inv);
            return true;
        }
        if (a[0].equalsIgnoreCase("hand") || a[0].equalsIgnoreCase("allhand")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) { player.sendMessage("§cHold an item."); return true; }
            double price = price(hand.getType());
            if (price <= 0) { player.sendMessage("§cThat item has no sell price."); return true; }
            int amount = a[0].equalsIgnoreCase("hand") ? hand.getAmount() : count(player, hand.getType());
            if (a[0].equalsIgnoreCase("hand")) player.getInventory().setItemInMainHand(null);
            else removeAll(player, hand.getType());
            double total = price * amount;
            p.economy().deposit(player.getUniqueId(), total);
            player.sendMessage("§aSold " + amount + "x " + hand.getType() + " for $" + GuiListener.money(total));
            return true;
        }
        player.sendMessage("§cUsage: /sell <gui|hand|allhand>");
        return true;
    }

    private double price(Material m) {
        for (String sec : p.shop().sections())
            for (var i : p.shop().items(sec)) if (i.material() == m) return i.sell();
        return 0;
    }
    private int count(Player pl, Material m) {
        int n=0; for (ItemStack x:pl.getInventory().getContents()) if(x!=null&&x.getType()==m)n+=x.getAmount(); return n;
    }
    private void removeAll(Player pl, Material m) {
        for(int i=0;i<pl.getInventory().getSize();i++){ ItemStack x=pl.getInventory().getItem(i); if(x!=null&&x.getType()==m)pl.getInventory().setItem(i,null); }
    }
}
