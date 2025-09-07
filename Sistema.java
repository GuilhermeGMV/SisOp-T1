// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Estrutura deste código:
//    As classes foram separadas em arquivos diferentes para melhor organização.
//    O Sistema agora instancia e coordena os componentes:
//           HW (Hardware) - constituído de CPU e Memory
//           SO (Sistema Operacional) - com InterruptHandling, SysCallHandling e Utilities  
//           Programs - programas disponíveis para execução
//
// Veja o main. Ele instancia o Sistema com os elementos mencionados acima.
// em seguida solicita a execução de algum programa com loadAndExec

public class Sistema {
    // ------------------- S I S T E M A
    // --------------------------------------------------------------------

    public HW hw;
    public SO so;
    public Programs progs;

    public Sistema(int tamMem, int pageSize, int delta) {
        hw = new HW(tamMem, pageSize, delta);           // memoria do HW tem tamMem palavras
        so = new SO(hw, pageSize);
        hw.cpu.setUtilities(so.utils); // permite cpu fazer dump de memoria ao avancar
        progs = new Programs();
    }

    public void run() {
        // Inicia o sistema interativo
        so.utils.interactiveSystem(progs);
        
        // Código de teste (comentado)
        //so.utils.testGM(progs.retrieveProgram("fatorialV2"), progs.retrieveProgram("fatorial"), progs.retrieveProgram("fibonacci10v2"), progs.retrieveProgram("PC"), progs.retrieveProgram("progMinimo"));
        //so.utils.testGP(progs.retrieveProgram("fatorialV2"));
        //so.utils.loadAndExec(progs.retrieveProgram("fatorialV2"));

        // so.utils.loadAndExec(progs.retrieveProgram("fatorial"));
        // fibonacci10,
        // fibonacci10v2,
        // progMinimo,
        // fatorialWRITE, // saida
        // fibonacciREAD, // entrada
        // PB
        // PC, // bubble sort
    }
    // ------------------- S I S T E M A - fim
    // --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // ------------------- instancia e testa sistema
    public static void main(String args[]) {
        Sistema s = new Sistema(1024, 8, 3);
        s.run();
    }
}