import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IODevice implements Runnable {
    private final HW hw;
    private final SO so;
    private final BlockingQueue<IORequest> ioQueue;
    private volatile boolean running;
    private final Scanner scanner;
    private final int ioDelay;
    
    public IODevice(HW hw, SO so, int ioDelay) {
        this.hw = hw;
        this.so = so;
        this.ioQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.scanner = new Scanner(System.in);
        this.ioDelay = ioDelay;
    }
    
    public void addRequest(IORequest request) {
        try {
            ioQueue.put(request);
            if (hw.cpu.getDebug()) {
                System.out.println(">>> IODevice: Request added to queue for PID " + request.pcb.pid);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        System.out.println(">>> IODevice: Started");
        
        while (running) {
            try {
                IORequest request = ioQueue.take(); // fica esperando aqui
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: Processing request for PID " + request.pcb.pid + 
                                     " (operation: " + (request.operation == 1 ? "IN" : "OUT") + ")");
                }
                
                Thread.sleep(ioDelay);
                
                processIO(request);
                
                PCB completedPCB = request.pcb;
                
                synchronized (so) {
                    if (so.blocked.contains(completedPCB)) {
                        so.blocked.remove(completedPCB);
                        completedPCB.state = ProcessState.READY;
                        so.ready.add(completedPCB);
                        
                        if (hw.cpu.getDebug()) {
                            System.out.println(">>> IODevice: I/O Complete for PID " + completedPCB.pid + 
                                             " - moved from BLOCKED to READY");
                        }
                    }
                }
                
                System.out.print("SO> ");
                System.out.flush();
                
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(">>> IODevice: Error processing request - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println(">>> IODevice: Stopped");
    }
    
    private void processIO(IORequest request) {
        int physicalAddr = translateAddress(request.pcb, request.address);
        
        if (physicalAddr == -1) {
            System.err.println(">>> IODevice: Invalid address for PID " + request.pcb.pid);
            return;
        }
        
        switch (request.operation) {
            case 1: // IN
                System.out.print("\nIN (PID " + request.pcb.pid + "):   ");
                System.out.flush();
                
                int inputValue = 0;
                boolean validInput = false;
                
                while (!validInput) {
                    try {
                        if (scanner.hasNextInt()) {
                            inputValue = scanner.nextInt();
                            scanner.nextLine();
                            validInput = true;
                        } else {
                            String skipped = scanner.nextLine();
                            System.out.println(">>> IODevice: Invalid input '" + skipped + "'. Please enter an integer.");
                            System.out.print("IN (PID " + request.pcb.pid + "):   ");
                            System.out.flush();
                        }
                    } catch (Exception e) {
                        scanner.nextLine(); // limpa o buffer
                        System.out.println(">>> IODevice: Error reading input. Please try again.");
                        System.out.print("IN (PID " + request.pcb.pid + "):   ");
                        System.out.flush();
                    }
                }
                
                synchronized (hw.mem) {
                    hw.mem.pos[physicalAddr].opc = Opcode.DATA;
                    hw.mem.pos[physicalAddr].p = inputValue;
                }
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: IN completed - wrote " + inputValue + 
                                     " to address " + physicalAddr);
                }
                break;
                
            case 2: // OUT
                int outputValue;
                synchronized (hw.mem) {
                    outputValue = hw.mem.pos[physicalAddr].p;
                }
                
                System.out.println("\nOUT (PID " + request.pcb.pid + "):  " + outputValue);
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: OUT completed - read " + outputValue + 
                                     " from address " + physicalAddr);
                }
                break;
                
            default:
                System.err.println(">>> IODevice: Invalid operation " + request.operation);
                break;
        }
    }
    
    private int translateAddress(PCB pcb, int logicalAddress) {
        if (pcb == null || pcb.tabPag == null) {
            return logicalAddress;
        }
        
        int pageNumber = logicalAddress / hw.cpu.pageSize;
        int offset = logicalAddress % hw.cpu.pageSize;
        
        if (pageNumber >= pcb.tabPag.length) {
            return -1;
        }
        
        int frameAddress = pcb.tabPag[pageNumber];
        if (frameAddress == -1) {
            System.err.println(">>> IODevice: Page " + pageNumber + " not loaded for PID " + pcb.pid);
            return -1;
        }
        
        return frameAddress + offset;
    }
    
    public void stop() {
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
