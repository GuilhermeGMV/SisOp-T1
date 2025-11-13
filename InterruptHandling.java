public class InterruptHandling {
    private final HW hw;
    private final SO so;

    public InterruptHandling(HW _hw, SO _so) {
        hw = _hw;
        so = _so;
    }

    public void handle(Interrupts irpt) {
        switch (irpt) {
            case intPageFault:
                System.out.println();
                System.out.println(">>> Interrupcao " + irpt);
                handlePageFault();
                break;
                
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
    
    private void handlePageFault() {
        synchronized (so) {
            if (hw.cpu.pcb != null && so.running != null) {
                PCB currentPCB = hw.cpu.pcb;
                
                currentPCB.pc = hw.cpu.pcb.pc;
                currentPCB.ir = hw.cpu.getIR();
                currentPCB.reg = hw.cpu.reg.clone();
                currentPCB.irpt = Interrupts.noInterrupt;
                
                if (currentPCB.nextPageToLoad < currentPCB.totalPages) {
                    ProcessState oldState = currentPCB.state;
                    currentPCB.state = ProcessState.BLOCKED;
                    so.blocked.add(currentPCB);
                    so.running = null;
                    
                    // Registra o page fault no log
                    so.logger.logStateChange(currentPCB, "Page Fault (carregando pág " + currentPCB.nextPageToLoad + ")", oldState, ProcessState.BLOCKED);
                    
                    if (hw.cpu.getDebug()) {
                        System.out.println("\n>>> Page Fault: Processo PID " + currentPCB.pid + 
                                         " bloqueado. Carregando página " + currentPCB.nextPageToLoad);
                        so.utils.handlePsCommand();
                        System.out.print("SO> ");
                    }
                    
                    PageRequest request = new PageRequest(currentPCB, currentPCB.nextPageToLoad);
                    so.pageLoader.addRequest(request);
                } else {
                    System.err.println(">>> Erro Page Fault: Sem mais páginas para carregar para PID " + currentPCB.pid);
                    currentPCB.state = ProcessState.TERMINATED;
                    so.gp.terminateProcess(currentPCB);
                    so.running = null;
                }
            }
        }
    }
    
    private void handleIOComplete() {
        synchronized (so) {
            if (!so.blocked.isEmpty()) {
                PCB unblockedProcess = so.blocked.remove(0);
                ProcessState oldState = unblockedProcess.state;
                unblockedProcess.state = ProcessState.READY;
                so.ready.add(unblockedProcess);
                
                so.logger.logStateChange(unblockedProcess, "I/O Completo", oldState, ProcessState.READY);
                
                if (hw.cpu.getDebug()) {
                    System.out.println("\n>>> I/O Completo: Processo PID " + unblockedProcess.pid + 
                                     " movido de BLOCKED para READY");
                    System.out.print("SO> ");
                }
            }
        }
    }
    
    private void handleScheduling() {
        if (so.continuous) {
            if (hw.cpu.pcb != null && so.running != null) {
                hw.cpu.pcb.ir = hw.cpu.getIR();
                hw.cpu.pcb.reg = hw.cpu.reg.clone();
                hw.cpu.pcb.irpt = Interrupts.noInterrupt;
                
                synchronized (so.scheduler.getLock()) {
                    ProcessState oldState = hw.cpu.pcb.state;
                    hw.cpu.pcb.state = ProcessState.READY;
                    so.ready.add(hw.cpu.pcb);
                    
                    so.logger.logStateChange(hw.cpu.pcb, "Fatia de tempo esgotada", oldState, ProcessState.READY);
                    
                    so.running = null;
                }
                
                if (hw.cpu.getDebug()) {
                    System.out.println("\n>>> Processo PID: " + hw.cpu.pcb.pid + " interrompido por escalonamento");
                    System.out.print("SO> ");
                }
            }
        } else {
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
