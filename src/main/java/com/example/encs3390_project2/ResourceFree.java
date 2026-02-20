package com.example.encs3390_project2;

import java.util.HashMap;

public class ResourceFree extends ResourceAction{
    public ResourceFree(int resourceId, int resourcesTOBeFreed) {
        super(resourceId, resourcesTOBeFreed);
    }
    @Override
    protected boolean doAction(MyProcess process,  HashMap<Integer, Integer> availableResources,HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources) {
        if (allocatedResources.get(process)==null||allocatedResources.get(process).getOrDefault(resourceId,0) <numberOfResourcesToDoAction)
            return false;
        allocatedResources.get(process).put(resourceId,allocatedResources.get(process).getOrDefault(resourceId,0)-numberOfResourcesToDoAction);
        if( allocatedResources.get(process).getOrDefault(resourceId,0)==0)
            allocatedResources.get(process).remove(resourceId);
        if(allocatedResources.get(process).isEmpty())
            allocatedResources.remove(process);
        availableResources.put(resourceId, availableResources.getOrDefault(resourceId, 0) + numberOfResourcesToDoAction);
        return true;
    }
}
