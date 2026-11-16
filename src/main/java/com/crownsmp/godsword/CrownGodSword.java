package com.crownsmp.godsword;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

import java.util.*;

public final class CrownGodSword extends JavaPlugin implements Listener {

    private NamespacedKey key;
    private final Set<UUID> active = new HashSet<>();
    private final Map<String, Long> cooldown = new HashMap<>();

    @Override
    public void onEnable() {
        key = new NamespacedKey(this, "crown_god_sword");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("cgs").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            if (!p.hasPermission("crowngodsword.give")) return true;
            p.getInventory().addItem(createSword());
            p.sendMessage("§dYou received CROWN GOD SWORD!");
            return true;
        });

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (UUID id : new HashSet<>(active)) {
                Player p = Bukkit.getPlayer(id);
                if (p == null || !isSword(p.getInventory().getItemInMainHand())) {
                    active.remove(id);
                    continue;
                }
                p.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE, 50, 1, true, true, true));
                p.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH, 50, 2, true, true, true));
            }
        }, 0L, 40L);
    }

    private ItemStack createSword() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§d§l👑 CROWN GOD SWORD");
        meta.setLore(Arrays.asList(
            "§6Legendary Weapon",
            "§eSHIFT + LEFT CLICK: Lightning",
            "§bSHIFT + RIGHT CLICK: Royal Power"
        ));

        meta.addEnchant(Enchantment.SHARPNESS, 7, true);
        meta.addEnchant(Enchantment.LOOTING, 7, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 4, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(
            key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    private boolean isSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte value = item.getItemMeta().getPersistentDataContainer()
            .get(key, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    private boolean ready(Player p, String ability, long ms) {
        String id = p.getUniqueId() + ":" + ability;
        long now = System.currentTimeMillis();
        if (now - cooldown.getOrDefault(id, 0L) < ms) return false;
        cooldown.put(id, now);
        return true;
    }

    @EventHandler
    public void onLeftClick(PlayerAnimationEvent e) {
        Player p = e.getPlayer();

        if (!p.isSneaking() ||
            !isSword(p.getInventory().getItemInMainHand())) return;

        if (!ready(p, "lightning", 1500)) return;

        Location target = p.getTargetBlockExact(30) != null
            ? p.getTargetBlockExact(30).getLocation().add(0.5, 1, 0.5)
            : p.getLocation().add(p.getLocation().getDirection().multiply(8));

        p.getWorld().strikeLightning(target);
        p.getWorld().spawnParticle(
            Particle.ELECTRIC_SPARK, target, 35, 0.6, 0.8, 0.6, 0.08);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR &&
            e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();

        if (!p.isSneaking() ||
            !isSword(p.getInventory().getItemInMainHand())) return;

        e.setCancelled(true);

        if (!ready(p, "buff", 1000)) return;

        if (!active.remove(p.getUniqueId())) {
            active.add(p.getUniqueId());
            p.sendMessage("§dCROWN POWER ACTIVATED!");
        } else {
            p.sendMessage("§7CROWN POWER DEACTIVATED!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        active.remove(e.getPlayer().getUniqueId());
    }
          }
