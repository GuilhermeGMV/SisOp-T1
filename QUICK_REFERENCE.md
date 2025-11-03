# Quick Reference Guide - Concurrent OS

## Starting the System

```bash
./run_system.sh
# or
java Sistema
```

## Shell Commands

| Command | Description | Example |
|---------|-------------|---------|
| `new <program>` | Create a new process | `new fibonacciREAD` |
| `ps` | List all processes and their states | `ps` |
| `dump <pid>` | Show process details and memory | `dump 1` |
| `dumpM <start,end>` | Show memory range | `dumpM 0,50` |
| `traceon` | Enable execution trace | `traceon` |
| `traceoff` | Disable execution trace | `traceoff` |
| `exit` | Shutdown system | `exit` |

## Available Programs

### I/O Programs (will block)
- `fibonacciREAD` - Reads input, generates fibonacci sequence
- `fatorialV2` - Calculates factorial, outputs result

### CPU-Only Programs (no I/O)
- `fatorial` - Simple factorial calculation
- `fibonacci10` - Generates fibonacci sequence
- `progMinimo` - Minimal test program

## Process States

- **READY** - Process is ready to execute, waiting for CPU
- **RUNNING** - Process is currently executing on CPU
- **BLOCKED** - Process is waiting for I/O to complete

## Example Session

```
SO> traceon
SO> new fibonacciREAD
SO> new progMinimo
SO> ps
=== Lista de Processos ===
PID     Programa        Estado
---     --------        ------
1       fibonacciREAD   RUNNING
2       progMinimo      READY
==========================

# When process 1 requests I/O:
>>> SysCall: Process PID 1 blocked for I/O
>>> IODevice: Processing request for PID 1

SO> ps
=== Lista de Processos ===
PID     Programa        Estado
---     --------        ------
2       progMinimo      RUNNING
1       fibonacciREAD   BLOCKED
==========================

# After I/O completes:
>>> IODevice: I/O Complete for PID 1 - moved from BLOCKED to READY

SO> ps
=== Lista de Processos ===
PID     Programa        Estado
---     --------        ------
1       fibonacciREAD   READY
2       progMinimo      RUNNING
==========================
```

## Observing Concurrency

With `traceon`, look for:

1. **Process Scheduling**:
   ```
   >>> Escalonando processo PID: X (programName)
   ```

2. **I/O Blocking**:
   ```
   >>> SysCall: Process PID X blocked for I/O (operation: IN/OUT)
   ```

3. **I/O Processing** (concurrent with CPU):
   ```
   >>> IODevice: Processing request for PID X
   ```

4. **I/O Completion**:
   ```
   >>> IODevice: I/O Complete for PID X - moved from BLOCKED to READY
   ```

5. **Time Quantum Expiration**:
   ```
   >>> Processo PID: X interrompido por escalonamento
   ```

## Key Features to Observe

1. **CPU continues while I/O happens** - Other processes run while one is blocked
2. **Multiple processes in different states** - Use `ps` to see READY/RUNNING/BLOCKED
3. **Automatic state transitions** - Processes move between states automatically
4. **Fair scheduling** - Time quantum ensures all processes get CPU time

## Troubleshooting

### No processes running
- Create processes with `new <program>`
- Check with `ps` command

### Process stuck in BLOCKED
- IODevice is waiting for input (type a number)
- Check if IODevice thread is running

### Want to see detailed execution
- Use `traceon` to enable debug output
- Use `traceoff` to disable when too verbose

## System Configuration

Default settings (in Sistema.java):
- Memory: 1024 words
- Page Size: 8 words
- Time Quantum (delta): 4 instructions
- I/O Delay: 1000 ms (1 second)
- Mode: continuous = true

## Architecture Summary

```
User Shell (Interactive)
    ↓
Operating System (SO)
    ├── Scheduler Thread → Manages READY queue
    ├── IODevice Thread → Handles I/O requests
    └── CPU Threads → Execute processes (one per process)
```

**Three Concurrent Entities:**
1. **CPU** - Executes process instructions
2. **IODevice** - Handles I/O operations (parallel to CPU)
3. **Scheduler** - Manages process scheduling

This achieves **true concurrency** - CPU and I/O work simultaneously!
