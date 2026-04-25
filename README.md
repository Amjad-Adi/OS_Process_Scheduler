# 📌 Operating Systems Simulation Project

---

## 📖 Overview

This project is a **Java-based Operating Systems simulator** that models core OS concepts including **CPU scheduling**, **I/O management**, **resource allocation**, and **deadlock detection**.

It also includes a **JavaFX interface** for loading process data and visualizing execution results.

---

## ⚙️ Features

### 🧠 CPU Scheduling
- Priority Scheduling combined with **Round Robin**
- Dynamic time quantum based on CPU burst distribution
- **Aging mechanism** to prevent starvation

---

### 🔄 Process Management
- Supports:
  - CPU bursts
  - I/O bursts
  - Resource allocation
  - Resource release
- Full **process lifecycle simulation**:
  - Ready
  - Running
  - Waiting

---

### 💾 Resource Allocation
- Tracks:
  - Available resources
  - Allocated resources
  - Requested resources
- Blocks processes when resources are unavailable

---

### 🚨 Deadlock Handling
- Detects deadlocks using a **Banker-style safety algorithm**
- Selects victim processes based on:
  - Allocated resources
  - Priority
- Recovers system by:
  - Terminating victims
  - Freeing resources

---

### 📊 Performance Metrics
- Average waiting time
- Average turnaround time
- Gantt chart execution timeline
- Deadlock event tracking

---

## 📁 Input Format

### Resources (first line)
[resourceID, instances] [resourceID, instances] ...

---

### Processes
PID ArrivalTime Priority {CPU/IO/Resource bursts}

---

### Example
0 0 1 CPU {R[1,2], 50, F[1,1], 20, F[1,1]}
1 5 1 CPU {20} IO{30} CPU{20, R[2,3], 30, F[2,3], 10}

---

## 🚀 How It Works

### 📥 File Parsing
- Reads resources and processes
- Converts bursts into structured execution units

---

### ⚙️ Simulation Execution
- Processes move between:
  - Ready Queue
  - Waiting Queue
  - Running state
- CPU scheduling applies **Priority + Round Robin**
- I/O and resource operations handled dynamically

---

### 🚨 Deadlock Detection
- Triggered on failed resource allocation
- Identifies deadlocked processes
- Terminates selected victims
- Frees resources and continues execution

---

### 🏁 Termination
Simulation ends when all queues are empty:
- Ready queue
- Waiting queue
- Arrival queue
- Resource waiting queue

Outputs final performance metrics and execution timeline.

---

## 🧠 Concepts Used

- CPU Scheduling (Priority + Round Robin)
- Process Synchronization
- Resource Allocation & Management
- Deadlock Detection & Recovery
- Starvation Prevention (Aging)
- Event-driven OS simulation

---

## 🖥 Output

The system produces:

- 📊 Average Waiting Time
- ⏱ Average Turnaround Time
- 🚨 Deadlock history
- 📈 Gantt chart of CPU execution

---

## 👨‍💻 Implementation

- **Language:** Java
- **UI:** JavaFX
- **Core Engine:** Custom OS simulation logic (`HelloLogic`)
- **Data Structures:**
  - Priority Queues
  - HashMaps
  - Deques
