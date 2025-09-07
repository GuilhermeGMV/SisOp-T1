import java.util.Scanner;

public class SysCallHandling {
    private final HW hw; // referencia ao hw se tiver que setar algo

    public SysCallHandling(HW _hw) {
        hw = _hw;
    }

    public void stop() { // chamada de sistema indicando final de programa
                         // nesta versao cpu simplesmente pára
        System.out.println("                                               SYSCALL STOP");
    }

    public void handle() { // chamada de sistema 
                           // suporta somente IO, com parametros 
                           // reg[8] = in ou out    e reg[9] endereco do inteiro
        System.out.println("SYSCALL pars:  " + hw.cpu.reg[8] + " / " + hw.cpu.reg[9]);

        switch (hw.cpu.reg[8]) {
            case 1:
                // leitura ...
                Scanner scanner = new Scanner(System.in);
                System.out.print("IN:   ");
                hw.cpu.reg[9] = scanner.nextInt();
                break;
            case 2:
                // escrita - escreve o conteuodo da memoria na posicao dada em reg[9]
                int logicalAddr = hw.cpu.reg[9];
                int physicalAddr = hw.cpu.translateAddress(logicalAddr);
                if (physicalAddr != -1) {
                    System.out.println("OUT:   "+ hw.mem.pos[physicalAddr].p);
                } else {
                    System.out.println("ERRO: Endereço inválido " + logicalAddr);
                }
                break;
            default:
                System.out.println("  PARAMETRO INVALIDO");
                break;
        }
    }
}
