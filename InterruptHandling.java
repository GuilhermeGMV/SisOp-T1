public class InterruptHandling {
    private final HW hw;
    private final SO so;

    public InterruptHandling(HW _hw, SO _so) {
        hw = _hw;
        so = _so;
    }

    public void handle(Interrupts irpt) {
        switch (irpt) {
            case intIOComplete:
                handleIOComplete();
                break;
                
            case intEscalonar: 
                handleScheduling();
                break;
                
            default:
                int pc = (hw.cpu.pcb != null) ? hw.cpu.pcb.pc : -1;
                System.out.println(">>> Interrupcao " + irpt + " pc: " + pc);
                break;
        }   
    }
    
    private void handleIOComplete() {
        synchronized (so) {
            // Find the first blocked process and unblock it
            if (!so.blocked.isEmpty()) {
                PCB unblockedProcess = so.blocked.remove(0);
                unblockedProcess.state = ProcessState.READY;
                so.ready.add(unblockedProcess);
                
                if (hw.cpu.getDebug()) {
                    System.out.println("\n>>> I/O Complete: Process PID " + unblockedProcess.pid + 
                                     " moved from BLOCKED to READY");
                    System.out.print("SO> ");
                }
            }
        }
    }
    
    private void handleScheduling() {
        if (so.continuous) {
            // Save context and move to ready queue
            if (hw.cpu.pcb != null && so.running != null) {
                // Only save if process is still running (not already blocked)
                hw.cpu.pcb.pc = hw.cpu.pcb.pc;
                hw.cpu.pcb.ir = hw.cpu.getIR();
                hw.cpu.pcb.reg = hw.cpu.reg.clone();
                hw.cpu.pcb.irpt = Interrupts.noInterrupt;
                
                synchronized (so.scheduler.getLock()) {
                    hw.cpu.pcb.state = ProcessState.READY;
                    so.ready.add(hw.cpu.pcb);
                    so.running = null;
                }
                
                if (hw.cpu.getDebug()) {
                    System.out.println("\n>>> Processo PID: " + hw.cpu.pcb.pid + " interrompido por escalonamento");
                    System.out.print("SO> ");
                }
            }
        } else {
            // Sequential mode - not used in concurrent version
            hw.cpu.pcb.ir = hw.cpu.getIR();
            hw.cpu.pcb.reg = hw.cpu.reg;
            hw.cpu.pcb.irpt = hw.cpu.getIrpt();
            so.ready.add(hw.cpu.pcb);
            System.out.println(">>> Interrupcao " + Interrupts.intEscalonar + " pc: " + hw.cpu.pcb.pc);
            
            if (!so.ready.isEmpty()) {
                PCB nextPCB = so.ready.remove(0);
                so.running = nextPCB;
                hw.cpu.pcb = nextPCB;
                hw.cpu.setIR(nextPCB.ir);
                hw.cpu.reg = nextPCB.reg;
                System.out.println("\n>>> Escalonando processo PID: " + nextPCB.pid + " (" + nextPCB.program.name + ")");
                hw.cpu.run();
            } else {
                so.running = null;
                hw.cpu.pcb = null;
                hw.cpu.setIR(null);
                hw.cpu.reg = new int[10];
                System.out.println(">>> Nenhum processo pronto para escalonar.");
            }
        }
    }
}
