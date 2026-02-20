package com.example.encs3390_project2;

import java.util.*;

public class DeadLockState {
    private final int detectionTime;
    private final ArrayList<Integer> deadlockedProcesses; // PIDs
    private final ArrayList<Integer> victimsSelected;     // PIDs (in order)
    public DeadLockState(int detectionTime, List<Integer> deadlockedProcesses) {
        this.detectionTime = detectionTime;
        this.deadlockedProcesses = new ArrayList<>(deadlockedProcesses);
        this.victimsSelected = new ArrayList<>();
    }
    public int getDetectionTime() {
        return detectionTime;
    }
    public List<Integer> getDeadlockedProcesses() {
        return Collections.unmodifiableList(deadlockedProcesses);
    }
    public List<Integer> getVictimsSelected() {
        return Collections.unmodifiableList(victimsSelected);
    }
    public void addVictim(int victimPid) {
        victimsSelected.add(victimPid);
    }
    public String getDeadlockedProcessesText() {
        return formatPids(deadlockedProcesses);
    }
    public String getVictimsSelectedText() {
        return formatPids(victimsSelected);
    }
    private static String formatPids(List<Integer> pids) {
        if (pids == null || pids.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pids.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("P").append(pids.get(i));
        }
        return sb.toString();
    }
    @Override
    public String toString() {
        return "DeadlockState{time=" + detectionTime +
                ", deadlocked=" + deadlockedProcesses +
                ", victims=" + victimsSelected + "}";
    }
}