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
                System.out.println(">>> IODevice: Requisição adicionada à fila para PID " + request.pcb.pid);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        System.out.println(">>> IODevice: Iniciado");
        
        while (running) {
            try {
                IORequest request = ioQueue.take(); // fica esperando aqui
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: Processando requisição para PID " + request.pcb.pid + 
                                     " (operação: " + (request.operation == 1 ? "IN" : "OUT") + ")");
                }
                
                Thread.sleep(ioDelay);
                
                processIO(request);
                
                PCB completedPCB = request.pcb;
                
                synchronized (so) {
                    if (so.blocked.contains(completedPCB)) {
                        so.blocked.remove(completedPCB);
                        ProcessState oldState = completedPCB.state;
                        completedPCB.state = ProcessState.READY;
                        so.ready.add(completedPCB);
                        
                        String operation = request.operation == 1 ? "IN" : "OUT";
                        so.logger.logStateChange(completedPCB, "I/O Completo (" + operation + ")", 
                                                oldState, ProcessState.READY);
                        
                        if (hw.cpu.getDebug()) {
                            System.out.println(">>> IODevice: I/O Completo para PID " + completedPCB.pid + 
                                             " - movido de BLOCKED para READY");
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
                System.err.println(">>> IODevice: Erro ao processar requisição - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println(">>> IODevice: Parado");
    }
    
    private void processIO(IORequest request) {
        int physicalAddr = translateAddress(request.pcb, request.address);
        
        if (physicalAddr == -1) {
            System.err.println(">>> IODevice: Endereço inválido para PID " + request.pcb.pid);
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
                            System.out.println(">>> IODevice: Entrada inválida '" + skipped + "'. Por favor, digite um número inteiro.");
                            System.out.print("IN (PID " + request.pcb.pid + "):   ");
                            System.out.flush();
                        }
                    } catch (Exception e) {
                        scanner.nextLine(); // limpa o buffer
                        System.out.println(">>> IODevice: Erro ao ler entrada. Por favor, tente novamente.");
                        System.out.print("IN (PID " + request.pcb.pid + "):   ");
                        System.out.flush();
                    }
                }
                
                synchronized (hw.mem) {
                    hw.mem.pos[physicalAddr].opc = Opcode.DATA;
                    hw.mem.pos[physicalAddr].p = inputValue;
                }
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: IN concluído - escreveu " + inputValue + 
                                     " no endereço " + physicalAddr);
                }
                break;
                
            case 2: // OUT
                int outputValue;
                synchronized (hw.mem) {
                    outputValue = hw.mem.pos[physicalAddr].p;
                }
                
                System.out.println("\nOUT (PID " + request.pcb.pid + "):  " + outputValue);
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> IODevice: OUT concluído - leu " + outputValue + 
                                     " do endereço " + physicalAddr);
                }
                break;
                
            default:
                System.err.println(">>> IODevice: Operação inválida " + request.operation);
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
            System.err.println(">>> IODevice: Página " + pageNumber + " não carregada para PID " + pcb.pid);
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
