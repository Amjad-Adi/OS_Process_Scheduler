package com.example.encs3390_project2;
import java.util.*;
public class SimulationResult {
    public final double averageWaitingTime;
    public final double averageTurnAroundTime;
    public final ArrayList<DeadLockState> deadLockStates;
    public final ArrayList<GanttSegment> timeLine;

    public SimulationResult(double averageWaitingTime, double averageTurnAroundTime, ArrayList<DeadLockState> deadLockStates, ArrayList<GanttSegment> timeLine) {
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnAroundTime = averageTurnAroundTime;
        this.deadLockStates = new ArrayList<>(deadLockStates);
        this.timeLine = new ArrayList<>(timeLine);
    }
}
