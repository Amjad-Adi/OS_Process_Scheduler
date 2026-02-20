package com.example.encs3390_project2;

import java.util.HashMap;

public abstract class ResourceAction extends BurstAction {
    protected final int resourceId;
    protected final int numberOfResourcesToDoAction;

    public ResourceAction(int resourceId, int numberOfResourcesToDoAction) {
        if (resourceId < 0) throw new IllegalArgumentException("resourceId must be >= 0");
        if (numberOfResourcesToDoAction < 0) throw new IllegalArgumentException("numberOfResourcesToDoAction must be >= 0");
        this.resourceId = resourceId;
        this.numberOfResourcesToDoAction = numberOfResourcesToDoAction;
    }
    public int getResourceId() {
        return resourceId;
    }
    public int getNumberOfResourcesToDoAction() {
        return numberOfResourcesToDoAction;
    }
    protected abstract boolean doAction(MyProcess process, HashMap<Integer, Integer> availableResources,HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources);
}
