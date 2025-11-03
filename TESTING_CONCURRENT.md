# Testing the Concurrent Operating System

## Overview
This system now implements a 3-state process model (READY, RUNNING, BLOCKED) with concurrent execution using multiple threads:
- **Scheduler Thread**: Manages process scheduling
- **CPU Thread**: Executes process instructions (one per process)
- **IODevice Thread**: Handles asynchronous I/O operations

## How to Test

### 1. Start the System
```bash
java Sistema
```

The system will start in continuous mode with:
- Scheduler thread running
- IODevice thread waiting for I/O requests

### 2. Test Commands

#### Create processes with I/O operations:
```
SO> new fibonacciREAD
SO> new fatorialV2
SO> new fibonacciREAD
```

#### Check process states:
```
SO> ps
```
You should see processes in READY, RUNNING, or BLOCKED states.

#### View process details:
```
SO> dump <pid>
```

#### Enable debug trace:
```
SO> traceon
```

#### Exit:
```
SO> exit
```

## Expected Behavior

### Concurrent Execution
1. When a process requests I/O (IN or OUT):
   - The process is **BLOCKED**
   - It's added to the blocked queue
   - The IODevice thread receives the request
   - Another process from the ready queue starts **RUNNING**
   - CPU and I/O device work in parallel

2. When I/O completes:
   - IODevice generates an interrupt
   - The blocked process moves to **READY** state
   - It will be scheduled again when CPU is available

3. Time-sharing (quantum):
   - Running processes are interrupted after their time slice
   - They move back to READY state
   - Next process in ready queue is scheduled

### Process State Transitions
```
NEW -> READY -> RUNNING -> BLOCKED (I/O) -> READY -> RUNNING -> TERMINATED
                  ^            |
                  |____________|
                   (time quantum)
```

## Key Features Implemented

1. **Asynchronous I/O**: I/O operations no longer block the CPU
2. **DMA (Direct Memory Access)**: IODevice accesses memory directly
3. **Process States**: PCB now tracks READY/RUNNING/BLOCKED states
4. **Multiple Threads**: Scheduler, CPU, and IODevice run concurrently
5. **Interactive Shell**: Create processes while system is running

## Testing I/O Operations

### Test with fibonacciREAD
This program performs an IN operation:
1. Process starts RUNNING
2. Requests input (BLOCKED)
3. IODevice prompts for input
4. While waiting, other processes can RUN
5. After input, process becomes READY
6. Eventually scheduled and continues execution

### Test with fatorialV2
This program performs an OUT operation:
1. Process runs factorial calculation
2. Requests output (BLOCKED)
3. IODevice performs the output
4. Process becomes READY again

## Observing Concurrency

With `traceon`, you'll see:
- `>>> Escalonando processo PID: X` - Process scheduled
- `>>> SysCall: Process PID X blocked for I/O` - Process blocked
- `>>> IODevice: Processing request for PID X` - I/O being processed
- `>>> IODevice: I/O Complete for PID X` - I/O done, process unblocked
- `>>> Processo PID: X interrompido por escalonamento` - Time quantum expired

This demonstrates true concurrent execution where CPU time is overlapped with I/O time!
