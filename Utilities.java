import java.util.Arrays;

public class Utilities {
    private HW hw;
    private SO so;

    public Utilities(HW _hw, SO _so) {
        hw = _hw;
        so = _so;
    }

    private void loadProgram(Word[] p) {
        Word[] m = hw.mem.pos; // m[] é o array de posições memória do hw
        for (int i = 0; i < p.length; i++) {
            m[i].opc = p[i].opc;
            m[i].ra = p[i].ra;
            m[i].rb = p[i].rb;
            m[i].p = p[i].p;
        }
    }

    // dump da memória
    public void dump(Word w) { // funcoes de DUMP nao existem em hardware - colocadas aqui para facilidade
        System.out.print("[ ");
        System.out.print(w.opc);
        System.out.print(", ");
        System.out.print(w.ra);
        System.out.print(", ");
        System.out.print(w.rb);
        System.out.print(", ");
        System.out.print(w.p);
        System.out.println("  ] ");
    }

    public void dump(int ini, int fim) {
        Word[] m = hw.mem.pos; // m[] é o array de posições memória do hw
        for (int i = ini; i < fim; i++) {
            System.out.print(i);
            System.out.print(":  ");
            dump(m[i]);
        }
    }

    public void loadAndExec(Program p) {
        // loadProgram(p); // carga do programa na memoria
        p.tabPag = so.gm.alloc(p.image);
        System.out.println("---------------------------------- programa carregado na memoria nos endereços: " + Arrays.toString(p.tabPag));
        dump(0, p.image.length); // dump da memoria nestas posicoes
        hw.cpu.setContext(0); // seta pc para endereço 0 - ponto de entrada dos programas
        System.out.println("---------------------------------- inicia execucao ");
        hw.cpu.run(); // cpu roda programa ate parar
        System.out.println("---------------------------------- memoria após execucao ");
        dump(0, p.image.length); // dump da memoria com resultado
        so.gm.free(p.tabPag);
        System.out.println("---------------------------------- memoria após free ");
        dump(0, p.image.length);
    }

    public void testGM(Program p, Program p1, Program p2, Program p3, Program p4){
      System.out.println("---------------------------------- memoria antes ");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p);

      System.out.println("---------------------------------- p carregado");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p1);

      System.out.println("---------------------------------- p1 carregado ");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p2);

      System.out.println("---------------------------------- p2 carregado");
      dump(0, hw.mem.pos.length);
      so.gp.terminateProcessById(2);

      System.out.println("---------------------------------- memoria sem o p1");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p3);

      System.out.println("---------------------------------- p3 carregado");
      dump(0, hw.mem.pos.length);
      so.gp.terminateProcessById(1);
      so.gp.terminateProcessById(3);

      System.out.println("---------------------------------- memoria sem o p e o p2");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p4);

      System.out.println("---------------------------------- p4 carregado");
      dump(0, hw.mem.pos.length);

    }

    void testGP(Program p){
      System.out.println("---------------------------------- memoria antes ");
      dump(0, hw.mem.pos.length);
      so.gp.createProcess(p);

      System.out.println("---------------------------------- p carregado");
      dump(0, hw.mem.pos.length);
      so.gp.terminateProcessById(1);

      System.out.println("---------------------------------- memoria após free ");
      dump(0, hw.mem.pos.length);
    }
}
