package com.example.encs3390_project2;
import javafx.application.Application;
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
/*
Our Assumptions:
Time quantum is bigger than 80% of CPU bursts
when a process finishes its CPU burst or IO burst or time quantum, its modified priority will be restored to its initial priority.
order of entering when same priority processes fetched at same time:
process waiting for resources
Arrival process
Waiting process that finished IO

process that finished its burst or time quantum
 */