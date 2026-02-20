package com.example.encs3390_project2;

public class BurstForIO extends Burst{
    private final Duration duration;
    public BurstForIO(Duration duration) {
        this.duration=duration;
    }
    public Duration getDuration() {
        return duration;
    }
}
