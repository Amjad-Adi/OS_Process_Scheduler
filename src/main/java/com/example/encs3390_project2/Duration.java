package com.example.encs3390_project2;

public class Duration extends BurstAction{
    private int executionTime;
    public Duration(int executionTime) {
        if(executionTime>=0)
        this.executionTime = executionTime;
        else throw new IllegalArgumentException("executionTime must be >= 0");
    }
    public int getExecutionTime() {
        return executionTime;
    }
    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }
}
