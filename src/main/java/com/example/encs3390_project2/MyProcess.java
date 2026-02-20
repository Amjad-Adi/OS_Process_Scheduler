package com.example.encs3390_project2;

import java.util.*;

public class MyProcess {
    private int pid;
    private int arrivalTime;
    private int priority;
    private int priorityAfterAgingEffect;
    private Queue<Burst> bursts;
    private int TimeEnteringItsStatus;
    private int RunAssignedNumber;
    private static int globalRunAssignedNumber=1;
    private int waitingTime = 0;
    public MyProcess() {
        this.bursts = new ArrayDeque<>();
        this.TimeEnteringItsStatus =0;
    }
    public MyProcess(int pid, int arrivalTime, int priority, ArrayDeque<Burst> bursts) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        if (bursts == null) throw new IllegalArgumentException("bursts cannot be null");
        this.bursts = new ArrayDeque<>(bursts);
        this.TimeEnteringItsStatus =0;
        this.priorityAfterAgingEffect=priority;
    }
    public int getWaitingTime() {
        return waitingTime;}
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;}
        public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public int getPriorityAfterAgingEffect() {
        return priorityAfterAgingEffect;
    }
    public void setPriorityAfterAgingEffect(int priorityAfterAgingEffect) {
        this.priorityAfterAgingEffect = priorityAfterAgingEffect;
    }
    public void setTimeEnteringItsStatus(int TimeEnteringItsQueue) {
        this.TimeEnteringItsStatus = TimeEnteringItsQueue;
    }
    public int getTimeEnteringItsStatus() {
        return TimeEnteringItsStatus;
    }
    public int getRunAssignedNumber() {
        return RunAssignedNumber;
    }
    public void setRunAssignedNumber(int runAssignedNumber) {
        RunAssignedNumber = runAssignedNumber;
    }
    public static int nextReadyNumber() { return globalRunAssignedNumber++; }
    public Queue<Burst> getBursts() {
        return new ArrayDeque<>(bursts);
    }
    public void setBursts(ArrayDeque<Burst> bursts) {
        this.bursts=new ArrayDeque<>(bursts);
    }
}
