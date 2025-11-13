import java.util.LinkedList;
import java.util.Queue;

public class GM {
  public int pageSize = 8; 
  public HW hw;
  private final Queue<Integer> frameQueue;  // Para algoritmo FIFO de substituição de páginas

  public GM(int tp, HW hw) {
    this.pageSize = tp;
    this.hw = hw;
    this.frameQueue = new LinkedList<>();
  }

  // aloca APENAS a primeira página do programa
  public int[] alloc(Word[] p, int totalPages){
    Word[] m = hw.mem.pos;

    if (p.length == 0) {
      return null;
    }

    int wordsToLoad = Math.min(pageSize, p.length);
    
    int[] pageTable = new int[totalPages];
    for(int i = 0; i < totalPages; i++){
      pageTable[i] = -1;  // -1 indica página não carregada
    }

    int frameAddress = findFreeFrame(m);
    
    if(frameAddress == -1){
      frameAddress = victimizeFrame();
      if(frameAddress == -1){
        System.out.println("Erro: Não foi possível alocar ou vitimar uma página");
        return null;
      }
    }

    for(int j = 0; j < wordsToLoad; j++){
      m[frameAddress + j].opc = p[j].opc;
      m[frameAddress + j].ra = p[j].ra;
      m[frameAddress + j].rb = p[j].rb;
      m[frameAddress + j].p = p[j].p;
    }
    
    pageTable[0] = frameAddress;
    frameQueue.add(frameAddress);

    return pageTable;
  }

  public int allocPage(Word[] programImage, int pageNumber, int[] pageTable){
    Word[] m = hw.mem.pos;

    int startIndex = pageNumber * pageSize;
    int endIndex = Math.min(startIndex + pageSize, programImage.length);
    
    if(startIndex >= programImage.length){
      return -1;  // página inválida
    }

    int frameAddress = findFreeFrame(m);
    
    if(frameAddress == -1){
      frameAddress = victimizeFrame();
      if(frameAddress == -1){
        return -1;
      }
    }

    for(int i = startIndex, j = 0; i < endIndex; i++, j++){
      m[frameAddress + j].opc = programImage[i].opc;
      m[frameAddress + j].ra = programImage[i].ra;
      m[frameAddress + j].rb = programImage[i].rb;
      m[frameAddress + j].p = programImage[i].p;
    }
    
    pageTable[pageNumber] = frameAddress;
    frameQueue.add(frameAddress);

    return frameAddress;
  }

  private int findFreeFrame(Word[] m){
    for(int i = 0; i < m.length; i += pageSize){
      if(m[i].opc == Opcode.___){
        return i;
      }
    }
    return -1;
  }

  private int victimizeFrame(){
    if(frameQueue.isEmpty()){
      return -1;
    }
    
    int victimFrame = frameQueue.poll();
    
    Word[] m = hw.mem.pos;
    for(int j = 0; j < pageSize; j++){
      m[victimFrame + j] = new Word(Opcode.___, -1, -1, -1);
    }
    
    System.out.println(">>> Vitimando frame na posição " + victimFrame);
    
    return victimFrame;
  }

  public void free(int[] tabPag){
    Word[] m = hw.mem.pos;

    for(int pag : tabPag){
      if(pag != -1){
        for(int j = 0; j < pageSize; j++){
          m[pag+j] = new Word(Opcode.___, -1, -1, -1);
        }
        frameQueue.remove(pag);
      }
    }
  }

}
