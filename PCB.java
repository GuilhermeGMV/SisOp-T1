public class PCB {
  private static int nextId = 1;
  
  public int pid;
  public Program program;
  public int[] tabPag;
  public int pc;
  
  public PCB(Program program, int[] tabPag){
    this.pid = nextId++;
    this.program = program;
    this.tabPag = tabPag;
    this.pc = 0;
  }
}
