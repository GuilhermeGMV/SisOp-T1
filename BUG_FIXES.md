# Bug Fixes Applied

## Issues Found
1. Processes appearing in multiple queues (READY and BLOCKED simultaneously)
2. Infinite SYSCALL STOP messages
3. Processes not terminating properly after I/O completion

## Root Causes
1. **Double context saving**: When a SYSCALL blocked a process, the context was saved in `SysCallHandling`, then the `intEscalonar` interrupt triggered `InterruptHandling` which saved it again and added the process to the ready queue (even though it was already in blocked queue).

2. **Missing state check**: The scheduler didn't check if a process was already terminated before rescheduling it.

3. **No TERMINATED state**: Processes that completed execution were being added back to queues.

## Fixes Applied

### 1. ProcessState.java
- Added `TERMINATED` state to enum

### 2. SysCallHandling.java
- Ensured context is saved properly before blocking
- Clear interrupt flag to prevent confusion
- PC is correctly preserved (already incremented by CPU)

### 3. InterruptHandling.java
- Added check: only save context if `so.running != null`
- This prevents trying to save context for already-blocked processes
- Clears interrupt after saving

### 4. Scheduler.java
- Added check to skip TERMINATED processes
- Mark process as TERMINATED when it completes
- Better error handling

### 5. Utilities.java
- Added synchronization to ps command
- Show RUNNING process first for clarity
- Thread-safe queue iteration

## Expected Behavior Now

1. Process requests I/O → moves to BLOCKED queue only
2. Another process runs while I/O happens
3. I/O completes → process moves from BLOCKED to READY queue only
4. Process eventually gets scheduled and resumes
5. Process completes → marked as TERMINATED, removed from all queues
6. `ps` command shows each process in exactly one state

## Test It
```bash
javac *.java
java Sistema

SO> traceon
SO> new fatorialV2      # Uses OUT (output) - works well
SO> new fibonacci10     # CPU-only, no I/O - works well  
SO> new progMinimo      # CPU-only - works well
SO> ps
# Should see processes in correct states without duplicates
```

## Known Limitations

### Scanner Conflict with System.in
The IODevice and the Shell both use `System.in` through Scanner. In Java, when multiple threads read from `System.in`, there can be conflicts if shell commands are typed while waiting for I/O input.

**Workaround:**
1. When prompted with `IN (PID X):`, enter ONLY the number (no shell commands)
2. Wait for the `SO>` prompt to return before entering more shell commands
3. For testing concurrency without input conflicts, use:
   - `fibonacci10` (CPU-only)
   - `fatorial` (CPU-only)
   - `progMinimo` (CPU-only)
   - `fatorialV2` (uses OUT only, no input conflicts)

**Why this happens:**
- Shell reads from `System.in` for commands
- IODevice reads from `System.in` for process input
- When IODevice is waiting for input and user types shell command, Scanner gets confused

**Proper solution (for future):**
- Implement a centralized console manager
- Use separate input channels
- Implement a request/response queue for console I/O

