public class Scheduler implements Runnable {
    private final SO so;
    private final HW hw;
    private boolean running;
    private final Object lock = new Object();
    
    public Scheduler(SO so, HW hw) {
        this.so = so;
        this.hw = hw;
        this.running = true;
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                synchronized (lock) {
                    if (!so.ready.isEmpty() && so.running == null) {
                        PCB nextPCB = so.ready.remove(0);
                        so.running = nextPCB;

                        if(hw.cpu.getDebug()){
                          System.out.println("\n>>> Escalonando processo PID: " + nextPCB.pid + " (" + nextPCB.program.name + ")");
                        }

                        hw.cpu.setContext(nextPCB);
                        hw.cpu.setIR(nextPCB.ir);
                        hw.cpu.reg = nextPCB.reg.clone();
                        
                        Thread cpuThread = new Thread(() -> {
                            try {
                                hw.cpu.run();
                                
                                synchronized (lock) {
                                    if (so.running != null && so.running.pid == nextPCB.pid) {
                                        System.out.println(">>> Processo PID: " + nextPCB.pid + " terminou execução");
                                        System.out.print("SO> ");
                                        so.gp.terminateProcess(so.running);
                                        so.running = null;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Erro na execução do processo: " + e.getMessage());
                                synchronized (lock) {
                                  so.running = null;
                                }
                            }
                        });
                        
                        cpuThread.setName("CPU-Process-" + nextPCB.pid);
                        cpuThread.start();
                    }
                }
                
                Thread.sleep(50);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Erro no escalonador: " + e.getMessage());
            }
        }
        
        System.out.println("Escalonador parado");
    }
    
    public void stop() {
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public Object getLock() {
        return lock;
    }
}
