package com.ruinscraft.combatloghistory;

import org.bukkit.plugin.java.JavaPlugin;

public class CombatLogHistoryPlugin extends JavaPlugin {

    private CombatLogHistoryStorage storage;

    public CombatLogHistoryStorage getStorage() {
        return storage;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupStorage();
        getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);
        getCommand("combatloghistory").setExecutor(new CombatLogHistoryCommand(storage));
    }

    private void setupStorage() {
        String mysqlHost = getConfig().getString("storage.mysql.host");
        int mysqlPort = getConfig().getInt("storage.mysql.port");
        String mysqlDatabase = getConfig().getString("storage.mysql.database");
        String mysqlUsername = getConfig().getString("storage.mysql.username");
        String mysqlPassword = getConfig().getString("storage.mysql.password");

        storage = new MySQLCombatLogHistoryStorage(mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPassword);
    }

}
