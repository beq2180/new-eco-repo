package com.example.aioeconomy;
import org.bukkit.command.*; import org.bukkit.entity.Player;
public final class ShopAddCommand implements CommandExecutor{
 private final AIOEconomyPlugin p; public ShopAddCommand(AIOEconomyPlugin p){this.p=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[]a){
  if(!(s instanceof Player pl)||!pl.hasPermission("aioeconomy.admin"))return true;
  if(a.length==2&&a[0].equalsIgnoreCase("section")){p.shop().addSection(a[1]);p.shop().save();pl.sendMessage("§aCreated shop section "+a[1]);return true;}
  if(a.length==3&&a[0].equalsIgnoreCase("item")){try{double buy=Double.parseDouble(a[1]),sell=Double.parseDouble(a[2]);if(p.shop().addItem(pl.getInventory().getItemInMainHand(),buy,sell)){p.shop().save();pl.sendMessage("§aAdded item to "+p.shop().lastSection());}else pl.sendMessage("§cCreate a section first.");}catch(Exception e){pl.sendMessage("§cInvalid prices.");}return true;}
  pl.sendMessage("§c/shopadd section <name> OR /shopadd item <buyprice> <sellprice>");return true;
 }
}
