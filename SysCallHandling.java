import java.util.Scanner;

public class SysCallHandling {
    private final HW hw; // referencia ao hw se tiver que setar algo
    private SO so;
    private IODevice ioDevice;

    public SysCallHandling(HW _hw) {
        hw = _hw;
    }
    
    public void setSO(SO _so) {
        this.so = _so;
    }
    
    public void setIODevice(IODevice _ioDevice) {
        this.ioDevice = _ioDevice;
    }

    public void stop() { // chamada de sistema indicando final de programa
                         // nesta versao cpu simplesmente pára
        System.out.println("                                               SYSCALL STOP");
    }

    public void handle() { // chamada de sistema 
                           // suporta somente IO, com parametros 
                           // reg[8] = in ou out    e reg[9] endereco do inteiro
        System.out.println("SYSCALL pars:  " + hw.cpu.reg[8] + " / " + hw.cpu.reg[9]);

        if (so == null || ioDevice == null || !so.continuous) {
            handleSynchronous();
            return;
        }
        
        PCB currentProcess = hw.cpu.pcb;
        int operation = hw.cpu.reg[8];
        int address = hw.cpu.reg[9];
        
        IORequest request = new IORequest(currentProcess, operation, address, 0);
        
        ioDevice.addRequest(request);
        
        if (hw.cpu.getDebug()) {
            System.out.println(">>> SysCall: Process PID " + currentProcess.pid + 
                             " blocked for I/O (operation: " + (operation == 1 ? "IN" : "OUT") + ")");
        }
        
        synchronized (so) {
            currentProcess.pc = hw.cpu.pcb.pc;
            currentProcess.ir = hw.cpu.getIR();
            currentProcess.reg = hw.cpu.reg.clone();
            currentProcess.irpt = Interrupts.noInterrupt;
            
            currentProcess.state = ProcessState.BLOCKED;
            so.blocked.add(currentProcess);

            so.running = null;
        }
        
        // escalona outro processo
        hw.cpu.setIrpt(Interrupts.intEscalonar);
    }
    
    private void handleSynchronous() {
        switch (hw.cpu.reg[8]) {
            case 1:
                // leitura ...
                Scanner scanner = new Scanner(System.in);
                System.out.print("IN:   ");
                hw.cpu.reg[9] = scanner.nextInt();
                break;
            case 2:
                // escrita - escreve o conteuodo da memoria na posicao dada em reg[9]
                int logicalAddr = hw.cpu.reg[9];
                int physicalAddr = hw.cpu.translateAddress(logicalAddr);
                if (physicalAddr != -1) {
                    System.out.println("OUT:   "+ hw.mem.pos[physicalAddr].p);
                } else {
                    System.out.println("ERRO: Endereço inválido " + logicalAddr);
                }
                break;
            default:
                System.out.println("  PARAMETRO INVALIDO");
                break;
        }
    }
}
