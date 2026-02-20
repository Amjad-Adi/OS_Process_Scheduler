package com.example.encs3390_project2;
import java.util.Comparator;

public class ProcessComparators {
    public static final Comparator<MyProcess> READY_ORDER = Comparator.comparingInt(MyProcess::getPriorityAfterAgingEffect)
            .thenComparingInt(MyProcess::getRunAssignedNumber);
    public static final Comparator<MyProcess> FETCH_TO_RUN = Comparator.comparingInt(MyProcess::getPriorityAfterAgingEffect);
    public static final Comparator<MyProcess> ARRIVAL_ORDER = Comparator.comparingInt(MyProcess::getArrivalTime)
            .thenComparingInt(MyProcess::getPid);
    public static final Comparator<MyProcess> WAITING_ORDER = Comparator.comparingInt(HelloLogic::IoCompletionTime)
            .thenComparingInt(MyProcess::getPid);
    public static final Comparator<MyProcess> RESOURCE_WAIT_ORDER = Comparator.comparingInt(MyProcess::getTimeEnteringItsStatus)
            .thenComparingInt(MyProcess::getPid);
}
