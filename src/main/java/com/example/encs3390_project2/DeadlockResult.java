package com.example.encs3390_project2;
import java.util.ArrayList;

public class DeadlockResult {
        private boolean deadlock;
        private ArrayList<MyProcess> deadlockedProcesses;
        DeadlockResult(boolean deadlock, ArrayList<MyProcess> deadlockedProcesses) {
            this.deadlock = deadlock;
            this.deadlockedProcesses = deadlockedProcesses;
        }
    public boolean isDeadlock() {
        return deadlock;
    }
    public void setDeadlock(boolean deadlock) {
        this.deadlock = deadlock;
    }
    public ArrayList<MyProcess> getDeadlockedProcesses() {
        return new ArrayList<>(deadlockedProcesses);
    }
    public void setDeadlockedProcesses(ArrayList<MyProcess> deadlockedProcesses) {
        this.deadlockedProcesses = new ArrayList<>(deadlockedProcesses);
    }
}
