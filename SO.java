public class SO {
    public InterruptHandling ih;
    public SysCallHandling sc;
    public Utilities utils;
    public PCB running;
    public PCB[] ready;
    public GM gm;

    public SO(HW hw) {
        ih = new InterruptHandling(hw); // rotinas de tratamento de int
        sc = new SysCallHandling(hw); // chamadas de sistema
        hw.cpu.setAddressOfHandlers(ih, sc);
        gm = new GM(8, hw);
        utils = new Utilities(hw, this);
    }
}
