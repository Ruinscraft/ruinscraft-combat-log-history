package com.ruinscraft.combatloghistory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CombatLogHistoryStorage {

    CompletableFuture<List<CombatLog>> queryHistory(String username);

    CompletableFuture<Void> insertCombatLog(CombatLog log);

}
