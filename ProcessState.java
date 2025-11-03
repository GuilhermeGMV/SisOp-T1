public enum ProcessState {
    READY,      // Process is ready to execute
    RUNNING,    // Process is currently executing
    BLOCKED,    // Process is blocked waiting for I/O
    TERMINATED  // Process has finished execution
}
