package com.example.encs3390_project2;
import java.io.*;
import java.util.*;
import javafx.scene.control.Alert;

public class HelloLogic {
    public static final int TIME_TO_AGE = 10;
    public static final int AGING_EFFECT = 1;

    public static SimulationResult run(File dataFile) {
        HashMap<Integer, Integer> availableResources = new HashMap<>();
        PriorityQueue<MyProcess> readyQueue = new PriorityQueue<>(ProcessComparators.READY_ORDER);
        PriorityQueue<MyProcess> arrivalQueue = new PriorityQueue<>(ProcessComparators.ARRIVAL_ORDER);
        PriorityQueue<MyProcess> waitingQueue = new PriorityQueue<>(ProcessComparators.WAITING_ORDER);
        int timeQuantum = processFile(dataFile, availableResources, arrivalQueue);
        if (timeQuantum < 0) {
            return new SimulationResult(0, 0, new ArrayList<>(), new ArrayList<>());
        }
        return beginExecutionAndReturn(timeQuantum, availableResources, readyQueue, arrivalQueue, waitingQueue);
    }
//we must enforce errors when data not correct
    public static int processFile(File dataFile, HashMap<Integer, Integer> availableResources, PriorityQueue<MyProcess> arrivalQueue) {
        if (dataFile == null) {
            showError("No file selected (dataFile is null).");
            return -1;
        }
        if (!dataFile.exists() || !dataFile.isFile() || !dataFile.canRead()) {
            showError("File does not exist or is not a valid file: " + dataFile.getPath());
            return -1;
        }
        ArrayList<Integer> allCPUBurstsExecutionDuration = new ArrayList<>();
        try (Scanner scanBy = new Scanner(dataFile)) {
            if (!scanBy.hasNextLine()) {
                showError("File is empty.");
                return -1;
            }
            String line = scanBy.nextLine().trim();
            if (line.isBlank()) {
                showError("File doesn't contain needed data (resources line is blank).");
                return -1;
            }
            String[] resourceNums = line.split("\\D+");//any non digit number
            int count = 0;
            int resourceId = 0;
            for (String s : resourceNums) {
                if (s == null || s.isBlank()) continue;
                int val;
                try {
                    val = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    showError("Invalid number in resources line: " + s);
                    return -1;
                }
                if (count % 2 == 0) {
                    resourceId = val;
                } else {
                    if (resourceId < 0 || val < 0) {
                        showError("ResourceID and instances must be non-negative. Found: " + resourceId + ", " + val);
                        return -1;
                    }
                    availableResources.put(resourceId, availableResources.getOrDefault(resourceId, 0) + val);
                }
                count++;
            }
            if (count % 2 != 0) {
                showError("Each resource must have an ID and an instance count.");
                return -1;
            }
            Set<Integer> pidCheckSet = new HashSet<>();
            while (scanBy.hasNextLine()) {
                line = scanBy.nextLine();
                if (line == null || line.isBlank()) continue;
                String[] lineParts = line.trim().split("\\s+", 4);
                if (lineParts.length != 4) {
                    showError("Invalid process line format: " + line);
                    return -1;
                }
                int pid, arrivalTime, priority;
                try {
                    pid = Integer.parseInt(lineParts[0]);
                    arrivalTime = Integer.parseInt(lineParts[1]);
                    priority = Integer.parseInt(lineParts[2]);
                } catch (NumberFormatException e) {
                    showError("PID/Arrival/Priority must be integers.\nLine: " + line);
                    return -1;
                }
                if (pid < 0 || arrivalTime < 0) {
                    showError("PID and arrival time must be non-negative.\nLine: " + line);
                    return -1;
                }
                if (priority < 0 || priority > 20) {
                    showError("Priority must be in range [0-20].\nLine: " + line);
                    return -1;
                }
                if (!pidCheckSet.add(pid)) {
                    showError("Duplicate PID detected: " + pid);
                    return -1;
                }
                String[] burstParts = lineParts[3].split("[{}\\[\\],]+");
                boolean isCPUBurst = false, isResourceAllocation = false, isResourceFree = false;
                Queue<BurstAction> burstActions = new ArrayDeque<>();
                ArrayDeque<Burst> bursts = new ArrayDeque<>();
                int numberOfTimesEntered = 0, resourceActionId = 0;
                for (int i = 0, numOfResources; i < burstParts.length; i++) {
                    String token = burstParts[i].trim();
                    if (token.isBlank()) continue;
                    if (token.equals("CPU")) {
                        isCPUBurst = true;
                        burstActions.clear();
                        numberOfTimesEntered = 0;
                        isResourceAllocation = false;
                        isResourceFree = false;
                        continue;
                    } else if (token.equals("IO")) {
                        isCPUBurst = false;
                        if (!burstActions.isEmpty()) {
                            bursts.add(new BurstForCPU(burstActions));
                        }
                        burstActions = new ArrayDeque<>();
                        numberOfTimesEntered = 0;
                        isResourceAllocation = false;
                        isResourceFree = false;
                        continue;
                    }
                    if (isCPUBurst) {
                        if (token.equals("R")) {
                            isResourceAllocation = true;
                            isResourceFree = false;
                            continue;
                        } else if (token.equals("F")) {
                            isResourceFree = true;
                            isResourceAllocation = false;
                            continue;
                        }
                        int val;
                        try {
                            val = Integer.parseInt(token);
                        } catch (NumberFormatException e) {
                            showError("Invalid number in burst sequence: " + token + "\nLine: " + line);
                            return -1;
                        }
                        if (isResourceAllocation) {
                            numberOfTimesEntered++;
                            if (numberOfTimesEntered == 1) {
                                resourceActionId = val;
                            } else {
                                numOfResources = val;
                                burstActions.add(new ResourceAllocation(resourceActionId, numOfResources));
                                numberOfTimesEntered = 0;
                                isResourceAllocation = false;
                            }
                        } else if (isResourceFree) {
                            numberOfTimesEntered++;
                            if (numberOfTimesEntered == 1) {
                                resourceActionId = val;
                            } else {
                                numOfResources = val;
                                burstActions.add(new ResourceFree(resourceActionId, numOfResources));
                                numberOfTimesEntered = 0;
                                isResourceFree = false;
                            }
                        } else {
                            burstActions.add(new Duration(val));
                            allCPUBurstsExecutionDuration.add(val);
                        }
                    } else {
                        numberOfTimesEntered++;
                        if (numberOfTimesEntered != 1) {
                            showError("More than one IO waiting duration.\nLine: " + line);
                            return -1;
                        }
                        int duration;
                        try {
                            duration = Integer.parseInt(token);
                        } catch (NumberFormatException e) {
                            showError("Invalid IO duration: " + token + "\nLine: " + line);
                            return -1;
                        }
                        bursts.add(new BurstForIO(new Duration(duration)));
                        numberOfTimesEntered = 0;
                    }
                }
                if (isResourceAllocation || isResourceFree || numberOfTimesEntered != 0) {
                    showError("Incomplete resource action (R/F) pair in line: " + line);
                    return -1;
                }
                if (!burstActions.isEmpty()) {
                    bursts.add(new BurstForCPU(burstActions));
                }
                arrivalQueue.add(new MyProcess(pid, arrivalTime, priority, bursts));
            }
        } catch (FileNotFoundException e) {
            showError("File not found: " + e.getMessage());
            return -1;
        }
        int timeQuantum = 1;
        if (!allCPUBurstsExecutionDuration.isEmpty()) {
            Collections.sort(allCPUBurstsExecutionDuration);
            int index = (int) Math.ceil(0.8 * allCPUBurstsExecutionDuration.size());
            timeQuantum = allCPUBurstsExecutionDuration.get(index - 1);
        }
        return timeQuantum;
    }

    public static SimulationResult beginExecutionAndReturn(int timeQuantum, HashMap<Integer, Integer> availableResources, PriorityQueue<MyProcess> readyQueue, PriorityQueue<MyProcess> arrivalQueue, PriorityQueue<MyProcess> waitingQueue) {
        int currentTime = 0;
        MyProcess running = null;
        PriorityQueue<MyProcess> resourceWaitingQueue = new PriorityQueue<>(ProcessComparators.RESOURCE_WAIT_ORDER);//Maybe should be changed to list
        ArrayList<DeadLockState> deadlockStates = new ArrayList<>();
        boolean deadlockCheckRequested = false;
        ArrayList<GanttSegment> timeline = new ArrayList<>();
        int numberOfProcessesToRun=arrivalQueue.size();
        HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources = new HashMap<>();
        HashMap<MyProcess, HashMap<Integer, Integer>> requestedResources = new HashMap<>();
        int waitingTime =0;
        int turnAroundTime=0;
        int nextIncreaseInTime=0;
        System.out.println(timeQuantum);
        while(true){
            int maxModTimeInReadyQueueForProcess = -1;
            int secondMaxModTimeInReadyQueueForProcess=-1;
            MyProcess maxModTimeForProcess=null;
            PriorityQueue<MyProcess> tempResourceWaitingQueue = new PriorityQueue<>(ProcessComparators.RESOURCE_WAIT_ORDER);
            while (!resourceWaitingQueue.isEmpty()) {
                MyProcess p = resourceWaitingQueue.poll();
                int state= addProcessToQueue(currentTime, p,running  , readyQueue, waitingQueue, tempResourceWaitingQueue,availableResources,allocatedResources,requestedResources);
                if (state == 4)
                    deadlockCheckRequested = true;
                else if (state == 0)
                    turnAroundTime+= (currentTime - p.getArrivalTime());
            }
            resourceWaitingQueue.addAll(tempResourceWaitingQueue);
            while (!waitingQueue.isEmpty() && ((BurstForIO) (waitingQueue.peek().getBursts().peek())).getDuration().getExecutionTime()+waitingQueue.peek().getTimeEnteringItsStatus()== currentTime){
                MyProcess p = waitingQueue.poll();
                Queue<Burst> burstsNow = p.getBursts();
                burstsNow.poll();
                p.setBursts(new ArrayDeque<>(burstsNow));
                int state = addProcessToQueue(currentTime, p,running  , readyQueue, waitingQueue, resourceWaitingQueue, availableResources, allocatedResources, requestedResources);
                if (state == 4)
                    deadlockCheckRequested = true;
                else if (state == 0)
                    turnAroundTime += (currentTime - p.getArrivalTime());
            }
            while (!arrivalQueue.isEmpty() && arrivalQueue.peek() != null && arrivalQueue.peek().getArrivalTime() == currentTime) {
                MyProcess p = arrivalQueue.poll();
                int state= addProcessToQueue(currentTime,p, running  ,readyQueue, waitingQueue, resourceWaitingQueue, availableResources, allocatedResources, requestedResources);
                if (state == 4)
                    deadlockCheckRequested = true;
                else if (state == 0)
                    turnAroundTime += (currentTime - p.getArrivalTime());
            }
            if (running != null) {
                Duration currentDuration = ((Duration) (((BurstForCPU) (running.getBursts().peek())).getActions().peek()));
                currentDuration.setExecutionTime(currentDuration.getExecutionTime() - nextIncreaseInTime);
                System.out.println((currentTime-nextIncreaseInTime)+" "+running.getPid()+" "+currentTime);
                if (currentDuration.getExecutionTime() == 0) {
                    Queue<BurstAction> actions = ((BurstForCPU) (running.getBursts().peek())).getActions();
                    actions.poll();
                    if (actions.isEmpty()){
                        Queue<Burst> burstsNow = running.getBursts();
                        burstsNow.poll();
                        running.setBursts(new ArrayDeque<>(burstsNow));
                    } else ((BurstForCPU) (running.getBursts().peek())).setActions(new ArrayDeque<>(actions));
                    int state = addProcessToQueue(currentTime, running,running ,readyQueue, waitingQueue, resourceWaitingQueue, availableResources, allocatedResources, requestedResources);
                    if (state == 4)
                        deadlockCheckRequested = true;
                    else if (state == 0)
                        turnAroundTime += (currentTime - running.getArrivalTime());
                    if(state!=1)
                        running = null;
                }
            }
                if (running!=null&&currentTime - running.getTimeEnteringItsStatus()== timeQuantum) {
                    running.setPriorityAfterAgingEffect(running.getPriority());
                    running.setTimeEnteringItsStatus(currentTime);
                    running.setRunAssignedNumber(MyProcess.nextReadyNumber());
                    readyQueue.add(running);
                    running=null;
                }
                //we should take first and second max
            PriorityQueue<MyProcess> tempReadyQueue = new PriorityQueue<>(ProcessComparators.READY_ORDER);
            while (!readyQueue.isEmpty()) {
                MyProcess p = readyQueue.poll();
                int waitingTimeInReadyQueue = currentTime - p.getTimeEnteringItsStatus();
                if (waitingTimeInReadyQueue != 0 && waitingTimeInReadyQueue % TIME_TO_AGE == 0 && p.getPriorityAfterAgingEffect() > 0) {
                    p.setPriorityAfterAgingEffect(p.getPriorityAfterAgingEffect() - AGING_EFFECT);
                }
                if (p.getPriorityAfterAgingEffect() > 0&& waitingTimeInReadyQueue % TIME_TO_AGE > maxModTimeInReadyQueueForProcess) {
                    secondMaxModTimeInReadyQueueForProcess=maxModTimeInReadyQueueForProcess;
                    maxModTimeInReadyQueueForProcess = waitingTimeInReadyQueue % TIME_TO_AGE;
                    maxModTimeForProcess=p;
                }
                tempReadyQueue.add(p);
            }
            readyQueue.addAll(tempReadyQueue);
            if (deadlockCheckRequested) {
                DeadlockResult det = detectDeadlock(availableResources, allocatedResources, requestedResources);
                if (det.isDeadlock()) {
                    ArrayList<Integer> deadlockedPids = new ArrayList<>();
                    for (MyProcess dp : det.getDeadlockedProcesses()) deadlockedPids.add(dp.getPid());
                    DeadLockState row = new DeadLockState(currentTime, deadlockedPids);
                    while (det.isDeadlock()) {
                        MyProcess victim = chooseDeadlockVictim(det.getDeadlockedProcesses(), allocatedResources);
                        if (victim == null) break;
                        if (readyQueue.contains(victim)) {
                            int extra = currentTime - victim.getTimeEnteringItsStatus();
                            victim.setWaitingTime(victim.getWaitingTime() + extra);
                            waitingTime += extra;
                        }
                        turnAroundTime += (currentTime - victim.getArrivalTime());
                        row.addVictim(victim.getPid());
                        running = terminateDeadlockVictimAndFreeResources(victim, running, readyQueue, waitingQueue, resourceWaitingQueue, allocatedResources, requestedResources, availableResources);
                       det = detectDeadlock(availableResources, allocatedResources, requestedResources);
                    }
                    deadlockStates.add(row);
                    while (!resourceWaitingQueue.isEmpty()) {
                        MyProcess p = resourceWaitingQueue.poll();
                        int state= addProcessToQueue(currentTime, p,running  , readyQueue, waitingQueue, tempResourceWaitingQueue,availableResources,allocatedResources,requestedResources);
                        if (state == 4)
                            deadlockCheckRequested = true;
                        else if (state == 0)
                            turnAroundTime+= (currentTime - p.getArrivalTime());
                    }
                }
            }
            deadlockCheckRequested = false;
            if (running == null && !readyQueue.isEmpty()) {
                running = readyQueue.poll();
                running.setWaitingTime(running.getWaitingTime() + (currentTime - running.getTimeEnteringItsStatus()));
                waitingTime+= (currentTime - running.getTimeEnteringItsStatus());
                running.setRunAssignedNumber(MyProcess.nextReadyNumber());
                running.setTimeEnteringItsStatus(currentTime);
            } else if (running != null && !readyQueue.isEmpty() &&(ProcessComparators.FETCH_TO_RUN.compare(readyQueue.peek(), running)<0)) {
                running.setPriorityAfterAgingEffect(running.getPriority());
                running.setTimeEnteringItsStatus(currentTime);
                running.setRunAssignedNumber(MyProcess.nextReadyNumber());
                readyQueue.add(running);
                running = readyQueue.poll();
                running.setWaitingTime(running.getWaitingTime() + (currentTime - running.getTimeEnteringItsStatus()));
                waitingTime+= (currentTime - running.getTimeEnteringItsStatus());
                running.setRunAssignedNumber(MyProcess.nextReadyNumber());
                running.setTimeEnteringItsStatus(currentTime);
            }
            if(running == null &&readyQueue.isEmpty()&& waitingQueue.isEmpty() && arrivalQueue.isEmpty() && resourceWaitingQueue.isEmpty())
                return new SimulationResult(waitingTime*1.0/numberOfProcessesToRun, turnAroundTime*1.0/numberOfProcessesToRun, deadlockStates, timeline);
            nextIncreaseInTime = Math.min(running != null ? ((Duration) (((BurstForCPU) (running.getBursts().peek())).getActions().peek())).getExecutionTime() : Integer.MAX_VALUE,
                    Math.min(maxModTimeInReadyQueueForProcess >= 0&&running!=maxModTimeForProcess? TIME_TO_AGE - maxModTimeInReadyQueueForProcess : (secondMaxModTimeInReadyQueueForProcess >= 0&&running!=maxModTimeForProcess)?TIME_TO_AGE - secondMaxModTimeInReadyQueueForProcess:Integer.MAX_VALUE,
                            Math.min(!readyQueue.isEmpty()?TIME_TO_AGE:Integer.MAX_VALUE,
                                    Math.min(!arrivalQueue.isEmpty() ? arrivalQueue.peek().getArrivalTime() - currentTime : Integer.MAX_VALUE,
                                            Math.min(!waitingQueue.isEmpty() ? ((BurstForIO) (waitingQueue.peek().getBursts().peek())).getDuration().getExecutionTime() - (currentTime - waitingQueue.peek().getTimeEnteringItsStatus()) : Integer.MAX_VALUE, (running != null ? timeQuantum - (currentTime - running.getTimeEnteringItsStatus()) : Integer.MAX_VALUE))))));
                addGanttInterval(timeline, currentTime, currentTime + nextIncreaseInTime, running);
            currentTime += nextIncreaseInTime;
        }
    }
    public static int addProcessToQueue(int currentTime, MyProcess p, MyProcess running, PriorityQueue<MyProcess> readyQueue, PriorityQueue<MyProcess> waitingQueue, PriorityQueue<MyProcess> resourceWaitingQueue, HashMap<Integer, Integer> availableResources,HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources,HashMap<MyProcess, HashMap<Integer, Integer>> requestedResources) {
        if (p == null || p.getBursts() == null)
            return -1;
        if (p.getBursts().isEmpty())
            return 0;
        Queue<Burst> bursts = p.getBursts();
        if (bursts.peek() instanceof BurstForIO) {
            p.setTimeEnteringItsStatus(currentTime);
            p.setPriorityAfterAgingEffect(p.getPriority());
            waitingQueue.add(p);
            return 3;
        } else {
            Queue<BurstAction> actions = ((BurstForCPU) bursts.peek()).getActions();
            if (actions == null)
                return -1;
            if (actions.isEmpty()) {
                bursts.poll();
                p.setBursts(new ArrayDeque<>(bursts));
                return addProcessToQueue(currentTime, p,running  ,readyQueue, waitingQueue, resourceWaitingQueue,availableResources,allocatedResources,requestedResources);
            }
            if(actions.peek() instanceof ResourceFree) {
                while (!actions.isEmpty() && actions.peek() instanceof ResourceFree freeAction) {
                    freeAction.doAction(p, availableResources, allocatedResources);
                    actions.poll();
                }
                if (actions.isEmpty()) {
                    bursts.poll();
                    p.setBursts(new ArrayDeque<>(bursts));
                } else ((BurstForCPU) (bursts.peek())).setActions(new ArrayDeque<>(actions));
                return addProcessToQueue(currentTime, p,running ,readyQueue, waitingQueue, resourceWaitingQueue, availableResources, allocatedResources, requestedResources);
            }
            if (actions.peek() instanceof ResourceAllocation) {
                if (tryAllocateResources(p, ((BurstForCPU) bursts.peek()),availableResources,allocatedResources, requestedResources))
                    return addProcessToQueue(currentTime, p, running, readyQueue, waitingQueue, resourceWaitingQueue, availableResources, allocatedResources, requestedResources);
                else {
                    p.setPriorityAfterAgingEffect(p.getPriority());
                    p.setTimeEnteringItsStatus(currentTime);
                    resourceWaitingQueue.add(p);
                    return 4;
                }
            } else {
                if(p!=running) {
                    p.setPriorityAfterAgingEffect(p.getPriority());
                    p.setRunAssignedNumber(MyProcess.nextReadyNumber());
                    p.setTimeEnteringItsStatus(currentTime);
                    readyQueue.add(p);
                    return 2;
                }
                return 1;
            }
        }
    }
    public static boolean tryAllocateResources(MyProcess p, BurstForCPU cpuBurst, HashMap<Integer, Integer> availableResources, HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources, HashMap<MyProcess, HashMap<Integer, Integer>> requestedResources) {
        Queue<BurstAction> copyOfActions = new ArrayDeque<>(cpuBurst.getActions());
        HashMap<Integer, Integer> neededResources = new HashMap<>();
        while (!copyOfActions.isEmpty() && copyOfActions.peek() instanceof ResourceAllocation ra) {
            neededResources.put(ra.getResourceId(), neededResources.getOrDefault(ra.getResourceId(),0)+ra.getNumberOfResourcesToDoAction());
            copyOfActions.poll();
        }
        if (neededResources.isEmpty()) {
            requestedResources.remove(p);
            return true;
        }
        for (HashMap.Entry<Integer, Integer> e : neededResources.entrySet()) {
            int resourceID = e.getKey();
            int count = e.getValue();
            if (count > availableResources.getOrDefault(resourceID, 0)) {
                requestedResources.put(p, new HashMap<>(neededResources));
                return false;
            }
        }
        Deque<BurstAction> real = new ArrayDeque<>(cpuBurst.getActions());
        while (!real.isEmpty() && real.peek() instanceof ResourceAllocation ra) {
            ra.doAction(p, availableResources,allocatedResources);
            real.poll();
        }
        cpuBurst.setActions(real);
        requestedResources.remove(p);
        return true;
    }
    public static DeadlockResult detectDeadlock(HashMap<Integer, Integer> availableResources, HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResources, HashMap<MyProcess, HashMap<Integer, Integer>> requestedResources){
        HashMap<Integer, Integer> work = new HashMap<>(availableResources);
        LinkedHashSet<MyProcess> all = new LinkedHashSet<>();
        all.addAll(allocatedResources.keySet());
        all.addAll(requestedResources.keySet());
        HashMap<MyProcess, Boolean> finish = new HashMap<>();
        for (MyProcess p : all) finish.put(p, false);
        boolean progressMade;
        do { progressMade = false;
            for (MyProcess p : all) {
                if (finish.get(p)) continue;
                HashMap<Integer, Integer> req = requestedResources.getOrDefault(p, new HashMap<>());
                if (canFinish(req, work)) {
                    HashMap<Integer, Integer> alloc = allocatedResources.getOrDefault(p, new HashMap<>());
                    for (Map.Entry<Integer, Integer> e : alloc.entrySet()) {
                        work.put(e.getKey(), work.getOrDefault(e.getKey(), 0) + e.getValue());
                    }
                    finish.put(p, true);
                    progressMade = true;
                }
            }
        } while (progressMade);

        ArrayList<MyProcess> deadlocked = new ArrayList<>();
        for (MyProcess p : all) {
            if (!finish.get(p))
                deadlocked.add(p);
        }
        return new DeadlockResult(!deadlocked.isEmpty(), deadlocked);
    }
    public static boolean canFinish(HashMap<Integer, Integer> requestedResources, HashMap<Integer, Integer> work) {
        for (Map.Entry<Integer, Integer> entry : requestedResources.entrySet()) {
            if (entry.getValue() > work.getOrDefault(entry.getKey(), 0)) {
                return false;
            }
        }
        return true;
    }
    public static MyProcess chooseDeadlockVictim(ArrayList<MyProcess> deadlockedProcesses, HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResourcesByProcess) {
        if (deadlockedProcesses == null || deadlockedProcesses.isEmpty()) return null;
        MyProcess victim = null;
        int maxTotalAllocated = 0;
        int maxPriority = 0;
        for (MyProcess p : deadlockedProcesses) {
            int totalAllocated = 0;
            HashMap<Integer, Integer> allocMap = allocatedResourcesByProcess.get(p);
            if (allocMap != null)
                for (int sum : allocMap.values())
                    totalAllocated += sum;
            if ((totalAllocated > maxTotalAllocated) || (totalAllocated == maxTotalAllocated &&     p.getPriority()> maxPriority)) {
                victim = p;
                maxTotalAllocated = totalAllocated;
                maxPriority =     p.getPriority();
            }
        }
        return victim;
    }
    public static MyProcess terminateDeadlockVictimAndFreeResources(MyProcess victimProcess, MyProcess currentlyRunning, PriorityQueue<MyProcess> readyQueue, PriorityQueue<MyProcess> ioWaitingQueue, PriorityQueue<MyProcess> resourceWaitingQueue, HashMap<MyProcess, HashMap<Integer, Integer>> allocatedResourcesByProcess, HashMap<MyProcess, HashMap<Integer, Integer>> requestedResourcesByProcess, HashMap<Integer, Integer> availableInstancesByResource){
        if (victimProcess == null) return currentlyRunning;
        readyQueue.remove(victimProcess);
        ioWaitingQueue.remove(victimProcess);
        resourceWaitingQueue.remove(victimProcess);
        if (currentlyRunning == victimProcess) {
            currentlyRunning = null;
        }
        HashMap<Integer, Integer> victimAllocMap = allocatedResourcesByProcess.get(victimProcess);
        if (victimAllocMap != null && !victimAllocMap.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : victimAllocMap.entrySet()) {
                int resourceId = entry.getKey();
                int allocatedCount = entry.getValue();
                if (allocatedCount <= 0) continue;
                availableInstancesByResource.put(
                        resourceId,
                        availableInstancesByResource.getOrDefault(resourceId, 0) + allocatedCount
                );
            }
        }
        allocatedResourcesByProcess.remove(victimProcess);
        requestedResourcesByProcess.remove(victimProcess);
        victimProcess.setBursts(new ArrayDeque<>());
        return currentlyRunning;
    }

    public static int IoCompletionTime(MyProcess p) {
        return p.getTimeEnteringItsStatus() + ((BurstForIO) (p.getBursts().peek())).getDuration().getExecutionTime();
    }
    public static void addGanttInterval(List<GanttSegment> timeline, int start, int endExclusive, MyProcess running) {
        if (endExclusive <= start) return;
        int pid = (running == null) ? -1 : running.getPid();
        if (timeline.isEmpty()) {
            timeline.add(new GanttSegment(pid, start, endExclusive));
            return;
        }
        GanttSegment last = timeline.getLast();
        if (last.pid == pid && last.end == start) {
            last.end = endExclusive;
        } else {
            timeline.add(new GanttSegment(pid, start, endExclusive));
        }
    }
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid input file.");
        alert.setContentText(message);
        alert.showAndWait();
    }
}