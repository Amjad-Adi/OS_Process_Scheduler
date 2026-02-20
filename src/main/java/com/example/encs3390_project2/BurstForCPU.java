package com.example.encs3390_project2;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Deque;


public class BurstForCPU extends Burst{
    private Queue<BurstAction> actions;
    public BurstForCPU(Queue<BurstAction> actions) {
        if (actions == null) throw new IllegalArgumentException("actions cannot be null");
        this.actions = new ArrayDeque<>(actions);
    }
    public ArrayDeque<BurstAction> getActions() {
        return new ArrayDeque<>(actions);
    }
    public void setActions(Deque<BurstAction> actions) {
        this.actions = new ArrayDeque<>(actions);
    }

}
