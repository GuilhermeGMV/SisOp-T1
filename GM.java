import java.util.ArrayList;

public class GM {
  public int tamPag = 8; 
  public HW hw;

  public GM(int tp, HW hw) {
    this.tamPag = tp;
    this.hw = hw;
  }

  public int[] alloc(Word[] p){
    Word[] m = hw.mem.pos;

    if (p.length > m.length) {
      return null;
    }

    ArrayList<Integer> freeFrames = new ArrayList<>();
    int programIndex = 0;

    for(int i = 0; i < m.length && programIndex < p.length; i += tamPag){
      if(m[i].opc == Opcode.___){
        for(int j = 0; j < tamPag && programIndex < p.length; j++){
          m[i+j].opc = p[programIndex].opc;
          m[i+j].ra = p[programIndex].ra;
          m[i+j].rb = p[programIndex].rb;
          m[i+j].p = p[programIndex].p;
          programIndex++;
        }
        freeFrames.add(i);
      }
    }

    if(programIndex == p.length){
      int[] result = new int[freeFrames.size()];
      for(int i = 0; i < freeFrames.size(); i++){
        result[i] = freeFrames.get(i);
      }
      return result;
    }

    return null;
  }

  public void free(int[] tabPag){
    Word[] m = hw.mem.pos;

    for(int pag : tabPag){
      for(int j = 0; j < tamPag; j++){
        m[pag+j] = new Word(Opcode.___, -1, -1, -1);
      }
      
    }
  }

}
