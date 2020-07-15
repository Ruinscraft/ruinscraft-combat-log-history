package com.ruinscraft.combatloghistory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CombatLogListener implements Listener {

    private static final int COMBAT_LOG_COOL_DOWN_SECONDS = 30;

    private CombatLogHistoryPlugin plugin;
    private Map<UUID, DamageLog> damageLogs;

    public CombatLogListener(CombatLogHistoryPlugin plugin) {
        this.plugin = plugin;
        damageLogs = new HashMap<>();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player attacked = (Player) event.getEntity();

        if (damageLogs.containsKey(attacked.getUniqueId())) {
            damageLogs.get(attacked.getUniqueId()).updateTime();
            damageLogs.get(attacked.getUniqueId()).addParticipant(attacker.getName());
        } else {
            DamageLog damageLog = new DamageLog();
            damageLog.addParticipant(attacker.getName());
            damageLogs.put(attacked.getUniqueId(), damageLog);
        }

        if (damageLogs.containsKey(attacker.getUniqueId())) {
            damageLogs.get(attacker.getUniqueId()).updateTime();
            damageLogs.get(attacker.getUniqueId()).addParticipant(attacked.getName());
        } else {
            DamageLog damageLog = new DamageLog();
            damageLog.addParticipant(attacked.getName());
            damageLogs.put(attacker.getUniqueId(), damageLog);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (damageLogs.containsKey(player.getUniqueId())) {
            DamageLog damageLog = damageLogs.get(player.getUniqueId());

            // mark as combat log
            if (damageLog.getTime() + TimeUnit.SECONDS.toMillis(COMBAT_LOG_COOL_DOWN_SECONDS) > System.currentTimeMillis()) {
                String locationString = player.getLocation().getWorld().getName() + "," + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
                CombatLog combatLog = new CombatLog(player.getName(), damageLog.getTime(), locationString, damageLog.getParticipants());

                plugin.getStorage().insertCombatLog(combatLog).thenRun(() -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                       for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                           if (onlinePlayer.hasPermission("ruinscraft.combatloghistory.alert")) {
                               onlinePlayer.sendMessage(ChatColor.GOLD + player.getName() + " has possibly combat logged. /combatloghistory " + player.getName());
                           }
                       }
                    });
                });
            }
        }

        damageLogs.remove(player.getUniqueId());
    }

    private class DamageLog {
        private long time;
        private List<String> participants;

        public DamageLog() {
            time = System.currentTimeMillis();
            participants = new ArrayList<>();
        }

        public long getTime() {
            return time;
        }

        public void updateTime() {
            time = System.currentTimeMillis();
        }

        public List<String> getParticipants() {
            return participants;
        }

        public void addParticipant(String username) {
            participants.add(username);
        }
    }

}
