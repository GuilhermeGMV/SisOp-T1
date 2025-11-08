public class IORequest {
    public PCB pcb;
    public int operation;
    public int address;
    public int value;
    
    public IORequest(PCB pcb, int operation, int address, int value) {
        this.pcb = pcb;
        this.operation = operation;
        this.address = address;
        this.value = value;
    }
}
