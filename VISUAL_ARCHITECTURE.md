# Concurrent Operating System - Visual Architecture

## System Overview
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CONCURRENT OPERATING SYSTEM                          │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         USER INTERFACE (Shell)                       │   │
│  │  Commands: new, ps, dump, traceon, traceoff, exit                  │   │
│  └────────────────────────────┬────────────────────────────────────────┘   │
│                                │                                              │
│                                ↓                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    OPERATING SYSTEM (SO)                             │   │
│  │                                                                       │   │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐         │   │
│  │  │ READY Queue  │    │   RUNNING    │    │BLOCKED Queue │         │   │
│  │  │              │    │              │    │              │         │   │
│  │  │  PCB 1       │    │    PCB 2     │    │   PCB 3      │         │   │
│  │  │  PCB 4       │    │              │    │   PCB 5      │         │   │
│  │  │  PCB 6       │    │              │    │              │         │   │
│  │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘         │   │
│  │         │                    │                    │                  │   │
│  └─────────┼────────────────────┼────────────────────┼─────────────────┘   │
│            │                    │                    │                      │
│            │                    │                    │                      │
│  ┌─────────▼─────────┐ ┌───────▼────────┐  ┌────────▼──────────┐         │
│  │   SCHEDULER       │ │   CPU THREAD   │  │    IO DEVICE      │         │
│  │     THREAD        │ │   (per PCB)    │  │      THREAD       │         │
│  │                   │ │                │  │                   │         │
│  │ - Picks READY     │ │ - Executes     │  │ - Processes I/O   │         │
│  │   process         │ │   instructions │  │   requests        │         │
│  │ - Sets RUNNING    │ │ - Time quantum │  │ - DMA access      │         │
│  │ - Manages quantum │ │ - Context save │  │ - Unblock process │         │
│  │                   │ │                │  │                   │         │
│  └───────────────────┘ └────────────────┘  └───────────────────┘         │
│         ↑  ↓                   ↑  ↓                  ↑  ↓                   │
└─────────┼──┼───────────────────┼──┼──────────────────┼──┼──────────────────┘
          │  │                   │  │                  │  │
          │  │   ┌───────────────┼──┼──────────────────┘  │
          │  │   │               │  │                     │
          │  └───┼───────────────┘  └─────────────────────┘
          │      │
          │      ↓
    ┌─────▼──────────────────────────────────────────┐
    │         HARDWARE (HW)                           │
    │                                                 │
    │  ┌──────────────┐         ┌─────────────────┐ │
    │  │    CPU       │         │     MEMORY      │ │
    │  │              │◄────────┤                 │ │
    │  │ - Registers  │   DMA   │  [Page 0]      │ │
    │  │ - PC, IR     │ Access  │  [Page 1]      │ │
    │  │ - Context    │         │  ...           │ │
    │  └──────────────┘         └─────────────────┘ │
    └─────────────────────────────────────────────────┘
```

## Process Lifecycle Flow
```
┌─────────┐
│  START  │  User command: "new programName"
└────┬────┘
     │
     ↓
┌─────────────────────────────────────────┐
│  GP.createProcess()                     │
│  - Allocate memory pages                │
│  - Create PCB with state = READY        │
│  - Load program into memory             │
│  - Add to READY queue                   │
└────┬────────────────────────────────────┘
     │
     ↓
┌────────────────────────────────────────────────────┐
│  READY State                                       │
│  - Waiting in ready queue                          │
│  - PCB contains saved context                      │
└────┬───────────────────────────────────────────────┘
     │
     │ ◄────────────────────────────────┐
     │                                   │
     ↓                                   │
┌────────────────────────────────────────┴───────────┐
│  RUNNING State                                      │
│  - Scheduler selects process                       │
│  - Context loaded into CPU                         │
│  - CPU thread executes instructions                │
│  - state = RUNNING                                 │
└────┬──────────────────┬────────────────┬───────────┘
     │                  │                │
     │ Time            │ SYSCALL        │ STOP
     │ Quantum          │ (I/O)          │
     │ Expired          │                │
     ↓                  ↓                ↓
┌──────────┐    ┌──────────────┐    ┌──────────┐
│  READY   │    │   BLOCKED    │    │  FINISH  │
│          │    │              │    │          │
│ - Save   │    │ - Save ctx   │    │ - Free   │
│   context│    │ - Queue I/O  │    │   memory │
│ - Back to│    │ - Wait       │    │ - Remove │
│   queue  │    │              │    │   PCB    │
└──────────┘    └──────┬───────┘    └──────────┘
     ↑                 │
     │                 │ I/O Complete
     │                 ↓
     │          ┌────────────┐
     │          │   READY    │
     └──────────┤            │
                │ - Unblocked│
                │ - Ready to │
                │   resume   │
                └────────────┘
```

## I/O Operation Sequence Diagram
```
Process         CPU Thread      SysCall         IODevice        Memory
   │                │              │                │              │
   │ SYSCALL(IN/OUT)│              │                │              │
   ├───────────────►│              │                │              │
   │                │  handle()    │                │              │
   │                ├─────────────►│                │              │
   │                │              │ Create Request │              │
   │                │              │ ──────────────►│              │
   │                │              │                │              │
   │                │ BLOCK Process│                │              │
   │    BLOCKED ◄───┤              │                │              │
   │                │              │                │              │
   │                │ Save Context │                │              │
   │                │              │                │              │
   │                STOP EXECUTION │                │              │
   │                                                │              │
   │                             IODevice Queue     │              │
   │                                                ├─┐            │
   │                                                │ │ Process    │
   │                                                │ │ Request    │
   │                                                │ │ (Delay)    │
   │                                                │◄┘            │
   │                                                │              │
   │                                                │ DMA Read/Write│
   │                                                ├─────────────►│
   │                                                │◄─────────────┤
   │                                                │              │
   │                              Move to READY     │              │
   │    READY    ◄──────────────────────────────────┤              │
   │                                                │              │
   │                                                                │
   │ ... wait for scheduler ...                                    │
   │                │                                               │
   │ Scheduled      │                                               │
   ├───────────────►│                                               │
   │   RUNNING      │                                               │
   │                │ Resume Execution                              │
   │                │ (with I/O result in memory)                   │
   │                │                                               │
```

## Thread Interactions
```
┌──────────────────────────────────────────────────────────────┐
│                      TIME LINE                                │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Scheduler Thread: ▓▓▓░░░▓▓▓░░░▓▓▓░░░▓▓▓░░░▓▓▓░░░          │
│                     │   │   │   │   │   │   │                │
│                     Pick P1 Pick P2 Pick P3 ...              │
│                                                               │
│  CPU Thread (P1):   ──▓▓▓▓▓▓░░░░░░░░░░░░░──▓▓▓▓──          │
│                       Run  I/O Wait        Run               │
│                                                               │
│  CPU Thread (P2):   ░░░░░░░──▓▓▓▓▓▓▓▓──░░░░░░──▓▓──        │
│                              Run      Blocked  Run            │
│                                                               │
│  IODevice Thread:   ░░░░░░░░░░──██████──░░──████──░░░       │
│                                Process   Process              │
│                                P1 I/O    P2 I/O               │
│                                                               │
│  Legend: ▓ = Active  ░ = Waiting  ── = Not created yet       │
│          ██ = Processing I/O                                 │
└──────────────────────────────────────────────────────────────┘

Key Observation: CPU and IODevice work CONCURRENTLY!
While IODevice processes I/O for P1, CPU runs P2.
```

## Memory Layout with Paging
```
Physical Memory                    Process View (Logical)
┌─────────────┐                   ┌─────────────┐
│ Page 0      │ ◄─────────────────│ Page 0      │ Process 1
│ (8 words)   │                   │ (code/data) │
├─────────────┤                   └─────────────┘
│ Page 1      │ ◄─────────────────┐
│ (8 words)   │                   │
├─────────────┤                   │ ┌─────────────┐
│ Page 2      │ ◄─────────────────┼─│ Page 0      │ Process 2
│ (8 words)   │                   │ │ (code/data) │
├─────────────┤                   │ └─────────────┘
│ Page 3      │ ◄─────────────────┘
│ (8 words)   │                   
├─────────────┤                   ┌─────────────┐
│ Page 4      │ ◄─────────────────│ Page 0      │ Process 3
│ (8 words)   │                   │ (code/data) │
├─────────────┤                   └─────────────┘
│ ...         │
└─────────────┘

Page Table Translation:
Logical Address → Physical Address
    (via PCB.tabPag[])
    
DMA Access: IODevice can read/write
any physical page directly!
```

## Synchronization Points
```
Critical Sections Protected by Locks:

1. SO.ready Queue
   ├─ Scheduler adds/removes
   ├─ InterruptHandling adds
   └─ Must be synchronized

2. SO.blocked Queue
   ├─ SysCallHandling adds
   ├─ IODevice removes
   └─ Must be synchronized

3. SO.running
   ├─ Scheduler sets
   ├─ Various handlers clear
   └─ Must be synchronized

4. Memory (during DMA)
   ├─ CPU reads/writes
   ├─ IODevice reads/writes
   └─ Must be synchronized

Synchronization Mechanisms:
- synchronized(so) { ... }
- synchronized(lock) { ... }
- BlockingQueue (thread-safe)
```

This visual guide helps understand how all components work together to achieve true concurrency!
