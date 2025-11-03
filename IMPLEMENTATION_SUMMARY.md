# Concurrent Operating System Implementation - Summary

## Implementation Overview

This implementation transforms the sequential operating system into a concurrent system with a 3-state process model and asynchronous I/O handling.

## Key Components Added/Modified

### 1. New Files Created

#### `ProcessState.java`
- Enum defining three process states: READY, RUNNING, BLOCKED
- Used to track process state transitions throughout their lifecycle

#### `IORequest.java`
- Represents an I/O request with:
  - Reference to requesting PCB
  - Operation type (1=IN, 2=OUT)
  - Memory address for operation
  - Value for data transfer

#### `IODevice.java`
- **Thread implementation** that simulates an I/O device
- Uses a `BlockingQueue` to receive I/O requests (producer-consumer pattern)
- Simulates I/O delay (configurable, default 1000ms)
- Implements **DMA (Direct Memory Access)** - reads/writes memory directly
- Generates process state transitions (BLOCKED → READY)
- Runs concurrently with CPU

### 2. Modified Files

#### `PCB.java`
- Added `ProcessState state` field
- Initialized to `ProcessState.READY` on creation
- Tracks current state: READY, RUNNING, or BLOCKED

#### `Interrupts.java`
- Added `intIOComplete` interrupt type
- Used to signal I/O operation completion

#### `SO.java`
- Added `public ArrayList<PCB> blocked` - queue for blocked processes
- Added `IODevice ioDevice` reference
- Added `Thread ioDeviceThread` management
- Modified `startScheduler()` to start both Scheduler and IODevice threads
- Modified `stopScheduler()` to stop all threads gracefully
- Updated `findPCBById()` to search in ready, blocked, and running processes

#### `SysCallHandling.java`
- Added references to `SO` and `IODevice`
- Modified `handle()` to support **asynchronous I/O**:
  - Creates `IORequest` and queues it to IODevice
  - Blocks the current process (state → BLOCKED)
  - Adds process to blocked queue
  - Saves CPU context to PCB
  - Triggers scheduling interrupt to run another process
- Maintains backward compatibility with synchronous mode

#### `InterruptHandling.java`
- Added `handleIOComplete()` method:
  - Moves first blocked process to ready queue
  - Updates process state: BLOCKED → READY
  - Process becomes eligible for scheduling
- Refactored interrupt handling for better organization
- Maintains proper synchronization with SO

#### `Scheduler.java`
- Updated to set process state to RUNNING when scheduled
- Works with 3-state model
- Maintains thread safety with synchronized blocks

#### `Utilities.java`
- Updated `handlePsCommand()` to display:
  - READY processes
  - RUNNING process
  - BLOCKED processes
- Provides complete system state visibility

#### `IODevice.java` Implementation Details
```
Key Features:
- Producer-Consumer pattern with BlockingQueue
- Thread-safe I/O request processing
- DMA memory access (no CPU involvement)
- Automatic process unblocking on completion
- Simulated I/O delay for realism
- Handles both IN and OUT operations
```

## Architecture

### Thread Model
```
┌─────────────────────────────────────────────────────┐
│                  Operating System                    │
├─────────────────────────────────────────────────────┤
│                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐│
│  │  Scheduler   │  │   IODevice   │  │    Shell   ││
│  │   Thread     │  │    Thread    │  │   (main)   ││
│  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘│
│         │                 │                 │       │
│         │ schedules       │ processes       │ user  │
│         │ processes       │ I/O requests    │ input │
│         ↓                 ↓                 ↓       │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐│
│  │ CPU Thread 1 │  │ I/O Queue    │  │  Commands  ││
│  │ (Process 1)  │  │ (requests)   │  │  (new, ps) ││
│  ├──────────────┤  └──────────────┘  └────────────┘│
│  │ CPU Thread 2 │                                   │
│  │ (Process 2)  │                                   │
│  └──────────────┘                                   │
└─────────────────────────────────────────────────────┘
```

### Process State Diagram
```
         ┌─────────┐
         │   NEW   │
         └────┬────┘
              │ create
              ↓
         ┌─────────┐
    ┌───┤  READY  │←──────────┐
    │   └─────────┘            │
    │ schedule   ↑             │
    │            │ I/O complete│
    │            │             │
    ↓     quantum expired      │
┌──────────┐           ┌───────┴────┐
│ RUNNING  │─────────→ │  BLOCKED   │
└────┬─────┘  I/O req  └────────────┘
     │
     │ terminate
     ↓
┌──────────┐
│  FINISH  │
└──────────┘
```

### I/O Operation Flow
```
1. Process executes SYSCALL (IN/OUT)
   └→ SysCallHandling.handle()

2. Create IORequest and queue to IODevice
   └→ IODevice.addRequest(request)

3. Block current process
   ├→ state = BLOCKED
   ├→ save CPU context
   └→ add to blocked queue

4. Scheduler picks another READY process
   └→ CPU executes different process (CONCURRENCY!)

5. IODevice processes request
   ├→ simulate delay
   ├→ DMA: read/write memory directly
   └→ unblock process

6. Process moves BLOCKED → READY
   └→ wait for scheduler

7. Scheduler eventually picks process
   └→ process continues from where it stopped
```

## Synchronization

### Critical Sections Protected
1. **SO synchronized blocks**: Protect ready, blocked, running queues
2. **Scheduler lock**: Coordinates process scheduling
3. **IODevice synchronized**: Protects process state transitions
4. **Memory synchronized**: Protects DMA memory access

## Benefits Achieved

### 1. **CPU Utilization**
- CPU no longer waits for slow I/O operations
- Multiple processes can run while others wait for I/O
- True concurrent execution

### 2. **Responsiveness**
- System remains interactive during process execution
- Can create new processes while others run
- Shell accepts commands continuously

### 3. **Throughput**
- Overlapping CPU and I/O operations
- Better resource utilization
- More processes complete in less time

### 4. **Realistic OS Behavior**
- Models real operating systems accurately
- 3-state process model
- Asynchronous I/O handling
- DMA simulation

## Testing

### Test Programs Available
- `fibonacciREAD`: Uses IN operation (blocks for input)
- `fatorialV2`: Uses OUT operation (blocks for output)
- `progMinimo`: CPU-only (no I/O)

### Testing Scenarios
1. **Single I/O Process**: Observe blocking and unblocking
2. **Multiple I/O Processes**: See concurrent I/O handling
3. **Mixed Workload**: CPU-bound + I/O-bound processes
4. **Time-sharing**: Observe quantum-based preemption

### Running Tests
```bash
./run_system.sh

# In the shell:
SO> traceon                  # Enable debug output
SO> new fibonacciREAD       # Create I/O process
SO> new progMinimo          # Create CPU process
SO> new fibonacciREAD       # Create another I/O process
SO> ps                      # Check process states
```

## Configuration

### Adjustable Parameters (in SO.java constructor)
- **I/O Delay**: `new IODevice(hw, this, 1000)` - milliseconds
- **Quantum**: Set in Sistema.java via delta parameter
- **Memory Size**: Set in Sistema.java
- **Page Size**: Set in Sistema.java

## Future Enhancements

Possible improvements:
1. Multiple I/O devices (disk, console, network)
2. Priority-based scheduling
3. I/O request priorities
4. More sophisticated DMA controller
5. Interrupt coalescing
6. Process priorities affecting scheduling

## Compliance with Requirements

✅ **3-State Model**: READY, RUNNING, BLOCKED implemented
✅ **Asynchronous I/O**: Process blocks, CPU continues with others
✅ **Concurrency**: Scheduler, CPU, IODevice as separate threads
✅ **DMA**: IODevice accesses memory directly
✅ **Interrupts**: I/O completion interrupt added
✅ **Interactive Shell**: Continuous operation with user commands
✅ **State Transitions**: Proper handling of all transitions
✅ **Synchronization**: Thread-safe operations throughout

## Conclusion

The system now fully implements a concurrent operating system with:
- True parallelism between CPU and I/O devices
- Proper process state management
- Asynchronous I/O handling
- Interactive continuous operation
- Thread-safe execution

This provides a realistic simulation of how modern operating systems handle concurrency and I/O operations!
