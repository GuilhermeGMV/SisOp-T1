import java.util.ArrayList;

public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public PCB running;
    public ArrayList<PCB> ready;
    public GM gm;
    public GP gp;
    public Scheduler scheduler;
    private Thread schedulerThread;
    private boolean systemRunning;
    public final boolean continuous;

    public SO(HW hw, int pageSize, boolean continuous) {
        ih = new InterruptHandling(hw, this);
        sc = new SysCallHandling(hw);
        hw.cpu.setAddressOfHandlers(ih, sc);
        gm = new GM(pageSize, hw);
        ready = new ArrayList<>();
        utils = new Utilities(hw, this);
        gp = new GP(this);
        scheduler = new Scheduler(this, hw);
        systemRunning = false;
        this.continuous = continuous;
    }
    
    public void startScheduler() {
        if (!systemRunning) {
            systemRunning = true;
            schedulerThread = new Thread(scheduler);
            schedulerThread.setName("SO-Scheduler");
            schedulerThread.setDaemon(true);
            schedulerThread.start();
            System.out.println("Sistema operacional iniciado com escalonamento contínuo.");
        }
    }
    
    public void stopScheduler() {
        if (systemRunning) {
            systemRunning = false;
            scheduler.stop();
            if (schedulerThread != null) {
                schedulerThread.interrupt();
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
        return null;
    }
}
