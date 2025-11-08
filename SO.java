import java.util.ArrayList;

public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public PCB running;
    public ArrayList<PCB> ready;
    public ArrayList<PCB> blocked;
    public GM gm;
    public GP gp;
    public Scheduler scheduler;
    public IODevice ioDevice;
    private Thread schedulerThread;
    private Thread ioDeviceThread;
    private boolean systemRunning;
    public final boolean continuous;
    private final HW hw;

    public SO(HW hw, int pageSize, boolean continuous) {
        this.hw = hw;
        ih = new InterruptHandling(hw, this);
        sc = new SysCallHandling(hw);
        hw.cpu.setAddressOfHandlers(ih, sc);
        gm = new GM(pageSize, hw);
        ready = new ArrayList<>();
        blocked = new ArrayList<>();
        utils = new Utilities(hw, this);
        gp = new GP(this);
        scheduler = new Scheduler(this, hw);
        ioDevice = new IODevice(hw, this, 1000); // 1 segundo de delay de I/O
        systemRunning = false;
        this.continuous = continuous;
        
        sc.setSO(this);
        sc.setIODevice(ioDevice);
    }
    
    public void startScheduler() {
        if (!systemRunning) {
            systemRunning = true;
            
            schedulerThread = new Thread(scheduler);
            schedulerThread.setName("SO-Scheduler");
            schedulerThread.setDaemon(true);
            schedulerThread.start();
            
            ioDeviceThread = new Thread(ioDevice);
            ioDeviceThread.setName("SO-IODevice");
            ioDeviceThread.setDaemon(true);
            ioDeviceThread.start();
            
            System.out.println("Sistema operacional iniciado com escalonamento contínuo.");
            System.out.println("Threads ativas: Scheduler, IODevice");
        }
    }
    
    public void stopScheduler() {
        if (systemRunning) {
            systemRunning = false;
            
            // Stop scheduler
            scheduler.stop();
            if (schedulerThread != null) {
                schedulerThread.interrupt();
            }
            
            // Stop I/O device
            ioDevice.stop();
            if (ioDeviceThread != null) {
                ioDeviceThread.interrupt();
            }
            
            System.out.println("Sistema operacional parado.");
        }
    }
    
    public boolean isSystemRunning() {
        return systemRunning;
    }

    public PCB getRunning() {
        return running;
    }

    public PCB getReady(int index) {
        if (index >= 0 && index < ready.size()) {
            return ready.get(index);
        }
        return null;
    }

    public PCB findPCBById(int pid) {
        for (PCB pcb : ready) {
            if (pcb.pid == pid) {
                return pcb;
            }
        }
        
        // Also check blocked list
        for (PCB pcb : blocked) {
            if (pcb.pid == pid) {
                return pcb;
            }
        }
        
        // Check running process
        if (running != null && running.pid == pid) {
            return running;
        }
        
        return null;
    }
}
