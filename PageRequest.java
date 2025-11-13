public class PageRequest {
    public PCB pcb;
    public int pageNumber;
    
    public PageRequest(PCB pcb, int pageNumber) {
        this.pcb = pcb;
        this.pageNumber = pageNumber;
    }
}
