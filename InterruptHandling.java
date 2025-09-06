public class InterruptHandling {
    private final HW hw; // referencia ao hw se tiver que setar algo

    public InterruptHandling(HW _hw) {
        hw = _hw;
    }

    public void handle(Interrupts irpt) {
        // apenas avisa - todas interrupcoes neste momento finalizam o programa
        int pc = (hw.cpu.pcb != null) ? hw.cpu.pcb.pc : -1;
        System.out.println(
                "                                               Interrupcao " + irpt + "   pc: " + pc);
    }
}
