import java.util.Arrays;
import java.util.Scanner;

public class Utilities {
    private HW hw;
    private SO so;

    public Utilities(HW _hw, SO _so) {
        hw = _hw;
        so = _so;
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

    public void interactiveSystem(Programs programs) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        if(so.continuous){
          so.startScheduler();
        }
        
        while (running) {
            System.out.print("SO> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            switch (command) {
                case "new":
                    handleNewCommand(parts, programs);
                    break;
                case "load":
                    handleLoadCommand(programs);
                    break;
                case "rm":
                    handleRmCommand(parts);
                    break;
                case "ps":
                    handlePsCommand();
                    break;
                case "dump":
                    handleDumpCommand(parts);
                    break;
                case "dumpm":
                    handleDumpMCommand(parts);
                    break;
                case "exec":
                    if(so.continuous){
                      System.out.println("Comando inválido: " + command);
                      break;
                    }
                    handleExecCommand(parts);
                    break;
                case "execall":
                    if(so.continuous){
                      System.out.println("Comando inválido: " + command);
                      break;
                    }
                    handleExecAllCommand();
                    break;
                case "traceon":
                    handleTraceOnCommand();
                    break;
                case "traceoff":
                    handleTraceOffCommand();
                    break;
                case "exit":
                    running = false;
                    if(so.continuous){
                      so.stopScheduler();
                    }
                    System.out.println("Sistema encerrado.");
                    break;
                case "help":
                    System.out.println("=======================================");
                    System.out.println("Comandos disponíveis:");
                    System.out.println("  new <nomePrograma> - cria processo (execução automática)");
                    System.out.println("  rm <id> - remove processo");
                    System.out.println("  ps - lista processos");
                    System.out.println("  dump <id> - mostra PCB e memória do processo");
                    System.out.println("  dumpM <inicio,fim> - mostra memória entre posições");
                    if(!so.continuous) {
                      System.out.println("  exec <id> - executa processo");
                      System.out.println("  execAll - executa todos os processos em memória");
                    }
                    System.out.println("  traceOn - liga trace de execução");
                    System.out.println("  traceOff - desliga trace de execução");
                    System.out.println("  exit - sair do sistema");
                    System.out.println("=======================================");
                    break;
                default:
                    System.out.println("Comando inválido: " + command);
                    break;
            }
        }
        scanner.close();
    }
    
    private void handleNewCommand(String[] parts, Programs programs) {
        if (parts.length < 2) {
            System.out.println("Uso: new <nomePrograma>");
            return;
        }
        
        String programName = parts[1];
        Program program = programs.retrieveProgram(programName);
        
        if (program == null) {
            System.out.println("Programa '" + programName + "' não encontrado.");
            return;
        }
        
        int pid = so.gp.createProcess(program);
        if (pid == -1) {
            System.out.println("Falha ao criar processo para '" + programName + "'.");
        }
    }
    
    private void handleRmCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: rm <id>");
            return;
        }
        
        try {
            int pid = Integer.parseInt(parts[1]);
            if (so.gp.terminateProcessById(pid)) {
                System.out.println("Processo " + pid + " removido com sucesso.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido: " + parts[1]);
        }
    }
    
    private void handlePsCommand() {
        System.out.println("=== Lista de Processos ===");
        if (so.ready.isEmpty()) {
            System.out.println("Nenhum processo na fila de prontos.");
        } else {
            System.out.println("PID\tPrograma\t\tEstado");
            System.out.println("---\t--------\t\t------");
            for (PCB pcb : so.ready) {
                System.out.println(pcb.pid + "\t" + pcb.program.name + "\t\tPronto");
            }
        }
        
        if (so.running != null) {
            System.out.println(so.running.pid + "\t" + so.running.program.name + "\t\tExecutando");
        }
        System.out.println("==========================");
    }
    
    private void handleDumpCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: dump <id>");
            return;
        }
        
        try {
            int pid = Integer.parseInt(parts[1]);
            PCB pcb = so.findPCBById(pid);
            
            if (pcb == null) {
                System.out.println("Processo com ID " + pid + " não encontrado.");
                return;
            }
            
            System.out.println("=== Dump do Processo " + pid + " ===");
            System.out.println("PID: " + pcb.pid);
            System.out.println("Programa: " + pcb.program.name);
            System.out.println("PC: " + pcb.pc);
            System.out.println("Tabela de Páginas: " + Arrays.toString(pcb.tabPag));
            System.out.println("\n--- Memória do Processo ---");
            
            for (int page : pcb.tabPag) {
                System.out.println("Página iniciando em " + page + ":");
                dump(page, Math.min(page + so.gm.pageSize, hw.mem.pos.length));
            }
            System.out.println("==============================");
            
        } catch (NumberFormatException e) {
            System.out.println("ID inválido: " + parts[1]);
        }
    }
    
    private void handleDumpMCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: dumpM <inicio,fim>");
            return;
        }
        
        try {
            String[] range = parts[1].split(",");
            if (range.length != 2) {
                System.out.println("Formato inválido. Use: dumpM <inicio,fim>");
                return;
            }
            
            int inicio = Integer.parseInt(range[0].trim());
            int fim = Integer.parseInt(range[1].trim());
            
            if (inicio < 0 || fim >= hw.mem.pos.length || inicio > fim) {
                System.out.println("Range inválido. Deve estar entre 0 e " + (hw.mem.pos.length - 1));
                return;
            }
            
            System.out.println("=== Dump da Memória [" + inicio + "," + fim + "] ===");
            dump(inicio, fim + 1);
            System.out.println("=====================================");
            
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido. Use números inteiros.");
        }
    }
    
    private void handleExecCommand(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: exec <id>");
            return;
        }
        
        try {
            int pid = Integer.parseInt(parts[1]);
            PCB pcb = so.findPCBById(pid);
            
            if (pcb == null) {
                System.out.println("Processo com ID " + pid + " não encontrado.");
                return;
            }
            
            System.out.println("Executando processo " + pid + " (" + pcb.program.name + ")...");
            
            so.ready.remove(pcb);
            so.running = pcb;
            
            hw.cpu.setContext(pcb);
            
            System.out.println("---------------------------------- inicia execucao ");
            hw.cpu.run();

            so.running = null;
            so.gp.terminateProcess(pcb);
            
            System.out.println("Processo " + pid + " terminou execução.");
            
        } catch (NumberFormatException e) {
            System.out.println("ID inválido: " + parts[1]);
        }
    }

    private void handleExecAllCommand() {
        while (!so.ready.isEmpty()) {
            PCB pcb = so.ready.get(0);
            System.out.println("Executando processo " + pcb.pid + " (" + pcb.program.name + ")...");
            
            so.ready.remove(pcb);
            so.running = pcb;
            
            hw.cpu.setContext(pcb);
            
            System.out.println("---------------------------------- inicia execucao ");
            hw.cpu.run();

            so.running = null;
            so.gp.terminateProcess(pcb);
            
            System.out.println("Processo " + pcb.pid + " terminou execução.");
        }
        System.out.println("Nenhum processo na fila de prontos.");
    }
    
    private void handleTraceOnCommand() {
        hw.cpu.setDebug(true);
        System.out.println("Trace de execução ligado.");
    }
    
    private void handleTraceOffCommand() {
        hw.cpu.setDebug(false);
        System.out.println("Trace de execução desligado.");
    }

    private void handleLoadCommand(Programs programs) {
        String[] programsNames = {
            "fatorial",
            "fatorialV2",
            "progMinimo",
            "fibonacci10",
            "fibonacci10v2",
            "fibonacciREAD",
            "PB",
            "PC",
            "fatorial",
            "fibonacci10"
        };
        for (int i = 0; i < 10; i++) {
            Program program = programs.retrieveProgram(programsNames[i]);
            int pid = so.gp.createProcess(program);
            if (pid == -1) {
                System.out.println("Falha ao criar processo para '" + programsNames[i] + "'.");
            }
        }
    }
}
