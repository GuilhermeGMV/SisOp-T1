public class GP {
  private SO so;

  public GP(SO _so){
      so = _so;
  }

  public int createProcess(Program p){
    int totalPages = (int) Math.ceil((double) p.image.length / so.gm.pageSize);
    
    p.tabPag = so.gm.alloc(p.image, totalPages);
    
    if(p.tabPag == null){
      System.out.println("Erro: Falha na alocação de memória para o programa " + p.name);
      return -1;
    }
    
    PCB pcb = new PCB(p, p.tabPag, totalPages);
    so.ready.add(pcb);
    
    System.out.println("Processo criado com PID: " + pcb.pid + " para o programa: " + p.name + 
                       " (Total de páginas: " + totalPages + ", Carregadas: " + pcb.loadedPages + ")");
    return pcb.pid;
  }

  public void terminateProcess(PCB pcb){
    if (pcb == null) {
        System.out.println("Erro: PCB é nulo, não é possível terminar o processo.");
        return;
    }
    so.gm.free(pcb.tabPag);
    so.ready.remove(pcb);
  }

  public boolean terminateProcessById(int pid){
    PCB pcb = so.findPCBById(pid);
    if (pcb == null) {
        System.out.println("Erro: PCB com PID " + pid + " não encontrado.");
        return false;
    }
    terminateProcess(pcb);
    return true;
  }

}
