package com.example.encs3390_project2;
public class GanttSegment {
    public final int pid;
    public final int start;
    public int end;

    public GanttSegment(int pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getPid() {
        return pid;
    }
}