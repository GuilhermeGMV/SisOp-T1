public class HW {
    public Memory mem;
    public CPU cpu;

    public HW(int tamMem, int pageSize, int delta) {
        mem = new Memory(tamMem);
        cpu = new CPU(mem, true, pageSize, delta);
    }
}
