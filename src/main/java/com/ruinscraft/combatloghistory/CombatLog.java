package com.ruinscraft.combatloghistory;

import java.util.List;

public class CombatLog {

    private final String logger;
    private final long time;
    private final String locationString;
    private final List<String> participants;

    public CombatLog(String logger, long time, String locationString, List<String> participants) {
        this.logger = logger;
        this.time = time;
        this.locationString = locationString;
        this.participants = participants;
    }

    public String getLogger() {
        return logger;
    }

    public long getTime() {
        return time;
    }

    public String getLocationString() {
        return locationString;
    }

    public List<String> getParticipants() {
        return participants;
    }

}
