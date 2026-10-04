package com.example.aioeconomy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class GuiListener implements Listener {
    private final AIOEconomyPlugin plugin;
    public GuiListener(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        Inventory top = e.getView().getTopInventory();

        if (top.getHolder() instanceof TradeHolder trade) {
            handleTradeClick(e, p, trade);
            return;
        }

        if (title.equals("Sell GUI")) {
            if (e.getClickedInventory() == top && e.getCurrentItem() != null) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item.getType().isAir()) return;
                double price = sellPrice(item.getType());
                if (price <= 0) { p.sendMessage("§cNo sell price configured for " + item.getType()); return; }
                int amount = item.getAmount();
                p.getInventory().removeItem(item);
                plugin.economy().deposit(p.getUniqueId(), price * amount);
                p.sendMessage("§aSold " + amount + "x " + item.getType() + " for $" + money(price * amount));
                p.closeInventory();
            }
            return;
        }

        if (title.equals("Shop")) {
            if (e.getClickedInventory() != top || e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;
            e.setCancelled(true);
            int slot = e.getRawSlot();
            List<String> sections = new ArrayList<>(plugin.shop().sections());
            if (slot >= 0 && slot < sections.size()) new ShopCommand(plugin).openSection(p, sections.get(slot));
            return;
        }

        if (title.startsWith("Shop: ")) {
            if (e.getClickedInventory() != top) return;
            e.setCancelled(true);
            if (e.getRawSlot() == 49) { new ShopCommand(plugin).openSections(p); return; }
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= 45) return;
            String section = title.substring("Shop: ".length());
            List<ShopManager.ShopItem> items = plugin.shop().items(section);
            if (slot >= items.size()) return;
            ShopManager.ShopItem shopItem = items.get(slot);
            if (e.isLeftClick()) {
                ItemStack purchased = new ItemStack(shopItem.material(), 1);
                var leftovers = p.getInventory().addItem(purchased);
                if (!leftovers.isEmpty()) { p.sendMessage("§cYour inventory is full."); return; }
                if (!plugin.economy().withdraw(p.getUniqueId(), shopItem.buy())) {
                    removeOne(p, shopItem.material());
                    p.sendMessage("§cYou cannot afford that item."); return;
                }
                p.sendMessage("§aBought 1x " + ShopCommand.pretty(shopItem.material()) + " for $" + money(shopItem.buy()));
            } else if (e.isRightClick()) {
                ItemStack held = findOne(p, shopItem.material());
                if (held == null) { p.sendMessage("§cYou don't have that item."); return; }
                removeOne(p, shopItem.material());
                plugin.economy().deposit(p.getUniqueId(), shopItem.sell());
                p.sendMessage("§aSold 1x " + ShopCommand.pretty(shopItem.material()) + " for $" + money(shopItem.sell()));
            }
            return;
        }

        if (title.startsWith("Auction House")) {
            if (e.getClickedInventory() != top) return;
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType().isAir()) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || meta.getLore() == null) return;
            for (String line : meta.getLore()) {
                if (!line.startsWith("§0ID:")) continue;
                try {
                    UUID id = UUID.fromString(line.substring(5));
                    var listing = plugin.auction().get(id);
                    if (listing == null) { p.sendMessage("§cThat listing no longer exists."); return; }

                    if (listing.seller().equals(p.getUniqueId())) {
                        if (p.getInventory().firstEmpty() == -1) {
                            p.sendMessage("§cYou need an empty inventory slot to take this listing off the AH."); return;
                        }
                        p.getInventory().addItem(listing.item());
                        plugin.auction().remove(id);
                        p.sendMessage("§aListing removed and item returned to you.");
                        p.closeInventory();
                    } else if (plugin.auction().buy(p.getUniqueId(), id)) {
                        p.getInventory().addItem(listing.item());
                        p.sendMessage("§aPurchased the item for $" + money(listing.price()));
                        p.closeInventory();
                    } else {
                        p.sendMessage("§cYou cannot afford this listing or it no longer exists.");
                    }
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void handleTradeClick(InventoryClickEvent e, Player p, TradeHolder trade) {
        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            if (e.isShiftClick()) e.setCancelled(true);
            return;
        }
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (slot == 22 || slot == 31) {
            if ((trade.isA(p) && slot != 22) || (trade.isB(p) && slot != 31)) {
                p.sendMessage("§cThat's the other player's accept button."); return;
            }
            trade.toggleAccept(p);
            if (trade.bothAccepted()) {
                trade.markFinished();
                plugin.trades().complete(trade);
            } else {
                p.sendMessage("§aYou accepted the trade.");
            }
            return;
        }
        if (slot == 49) { trade.markFinished(); plugin.trades().cancel(trade); return; }

        if (trade.ownSlot(p, slot)) {
            // We cancelled the normal event, so manually support simple pickup/place.
            ItemStack cursor = e.getCursor();
            ItemStack clicked = trade.inventory().getItem(slot);
            trade.inventory().setItem(slot, cursor == null || cursor.getType().isAir() ? null : cursor.clone());
            e.getWhoClicked().setItemOnCursor(clicked == null ? null : clicked.clone());
            trade.resetAccepts();
        } else {
            p.sendMessage("§cYou can only edit your side of the trade.");
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof TradeHolder trade)) return;
        for (int slot : e.getRawSlots()) {
            if (slot < e.getView().getTopInventory().getSize()) {
                e.setCancelled(true);
                p.sendMessage("§cClick individual slots to place items in a trade.");
                return;
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof TradeHolder trade)) return;
        if (!trade.finished()) {
            trade.markFinished();
            plugin.trades().cancel(trade);
        }
    }

    private double sellPrice(Material m) {
        for (String section : plugin.shop().sections())
            for (var item : plugin.shop().items(section))
                if (item.material() == m) return item.sell();
        return 0;
    }

    private ItemStack findOne(Player p, Material m) {
        for (ItemStack item : p.getInventory().getContents())
            if (item != null && item.getType() == m && item.getAmount() > 0) return item;
        return null;
    }

    private void removeOne(Player p, Material m) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null || item.getType() != m) continue;
            if (item.getAmount() == 1) p.getInventory().setItem(i, null);
            else item.setAmount(item.getAmount() - 1);
            return;
        }
    }

    static String money(double n) { return String.format("%.2f", n); }
}
