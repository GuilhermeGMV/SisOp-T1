public class IORequest {
    public PCB pcb;              // Process that requested the I/O
    public int operation;        // 1 = IN, 2 = OUT
    public int address;          // Memory address for the operation
    public int value;            // Value to write (for OUT) or to store result (for IN)
    
    public IORequest(PCB pcb, int operation, int address, int value) {
        this.pcb = pcb;
        this.operation = operation;
        this.address = address;
        this.value = value;
    }
}
