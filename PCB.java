public class PCB {
  private static int nextId = 1;
  
  public int pid;
  public Program program;
  public int[] tabPag;
  public int pc;
  public Word ir;
  public int[] reg;
  public Interrupts irpt;
  
  public PCB(Program program, int[] tabPag){
    this.pid = nextId++;
    this.program = program;
    this.tabPag = tabPag;
    this.pc = 0;
    this.ir = program.image[0];
    this.reg = new int[10];
    this.irpt = Interrupts.noInterrupt;
  }
}
