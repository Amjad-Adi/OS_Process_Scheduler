package com.example.encs3390_project2;

import java.util.HashMap;

public class ResourceAllocation extends ResourceAction{
    public ResourceAllocation(int resourceId, int resourcesTOBeAllocated) {
        super(resourceId, resourcesTOBeAllocated);
    }
    @Override
    protected boolean doAction(MyProcess process, HashMap<Integer, Integer> availableResources,HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources){
        if (availableResources.getOrDefault(resourceId,0) <numberOfResourcesToDoAction)
            return false;//error in file I think we need to enforce that else errors
        availableResources.put(resourceId, availableResources.getOrDefault(resourceId, 0) - numberOfResourcesToDoAction);
        allocatedResources.putIfAbsent(process, new HashMap<>());
        allocatedResources.get(process).put(resourceId,allocatedResources.get(process).getOrDefault(resourceId,0)+numberOfResourcesToDoAction);
        return true;
    }

}
