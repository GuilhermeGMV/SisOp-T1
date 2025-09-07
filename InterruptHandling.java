public class InterruptHandling {
    private final HW hw; // referencia ao hw se tiver que setar algo
    private final SO so;

    public InterruptHandling(HW _hw, SO _so) {
        hw = _hw;
        so = _so;
    }

    public void handle(Interrupts irpt) {
        switch (irpt) {
            case intEscalonar -> {
                hw.cpu.pcb.ir = hw.cpu.getIR();
                hw.cpu.pcb.reg = hw.cpu.reg;
                hw.cpu.pcb.irpt = hw.cpu.getIrpt();
                so.ready.add(hw.cpu.pcb);
                System.out.println("                                               Interrupcao " + irpt + "   pc: " + hw.cpu.pcb.pc);
                if (!so.ready.isEmpty()) {
                    PCB nextPCB = so.ready.remove(0);
                    so.running = nextPCB;
                    hw.cpu.pcb = nextPCB;
                    hw.cpu.setIR(nextPCB.ir);
                    hw.cpu.reg = nextPCB.reg;
                    System.out.println("                                               Escalonando processo PID: " + nextPCB.pid + "   pc: " + nextPCB.pc);
                    hw.cpu.run();
                } else {
                    so.running = null;
                    hw.cpu.pcb = null;
                    hw.cpu.setIR(null);
                    hw.cpu.reg = new int[10];
                    System.out.println("                                               Nenhum processo pronto para escalonar.");
                }
            }
            default -> {
                int pc = (hw.cpu.pcb != null) ? hw.cpu.pcb.pc : -1;
                System.out.println(
                        "                                               Interrupcao " + irpt + "   pc: " + pc);
            }
        }   
    }
}
