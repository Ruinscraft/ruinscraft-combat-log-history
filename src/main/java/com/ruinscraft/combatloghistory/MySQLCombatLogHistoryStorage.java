package com.ruinscraft.combatloghistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySQLCombatLogHistoryStorage implements CombatLogHistoryStorage {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public MySQLCombatLogHistoryStorage(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        // create table
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS combat_log_history (" +
                            "logger VARCHAR(16) NOT NULL, " +
                            "time BIGINT, " +
                            "location VARCHAR(128), " +
                            "participants TEXT DEFAULT NULL" + // comma separated list of usernames
                            ");");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<List<CombatLog>> queryHistory(String username) {
        return CompletableFuture.supplyAsync(() -> {
            List<CombatLog> history = new ArrayList<>();

            try (Connection connection = getConnection()) {
                try (PreparedStatement query = connection.prepareStatement("SELECT * FROM combat_log_history WHERE logger = ?;")) {
                    try (ResultSet result = query.executeQuery()) {
                        while (result.next()) {
                            long time = result.getLong("time");
                            String locationString = result.getString("location");
                            List<String> participants = Arrays.asList(result.getString("participants").split(","));
                            CombatLog combatLog = new CombatLog(username, time, locationString, participants);

                            history.add(combatLog);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return history;
        });
    }

    @Override
    public CompletableFuture<Void> insertCombatLog(CombatLog log) {
        return CompletableFuture.runAsync(() -> {
           try (Connection connection = getConnection()) {
               try (PreparedStatement insert = connection.prepareStatement("INSERT INTO combat_log_history (logger, time, location, participants) VALUES (?, ?, ?, ?);")) {
                   insert.setString(1, log.getLogger());
                   insert.setLong(2, log.getTime());
                   insert.setString(3, log.getLocationString());
                   String participants = String.join(",", log.getParticipants());
                   insert.setString(4, participants);
                   insert.execute();
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
        });
    }

    // Close when done
    private Connection getConnection() {
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
