import java.util.ArrayList;

public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public PCB running;
    public ArrayList<PCB> ready;
    public GM gm;
    public GP gp;

    public SO(HW hw, int pageSize) {
        ih = new InterruptHandling(hw); // rotinas de tratamento de int
        sc = new SysCallHandling(hw); // chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);
        gm = new GM(pageSize, hw);
        ready = new ArrayList<>(); // inicializa ArrayList para PCBs
        utils = new Utilities(hw, this);
        gp = new GP(this);
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
