import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessLogger {
    private BufferedWriter writer;
    private final String logFileName;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ProcessLogger(String fileName) {
        this.logFileName = fileName;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, false)); // false = sobrescreve
            writeHeader();
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de log: " + e.getMessage());
        }
    }

    private void writeHeader() throws IOException {
        writer.write("=".repeat(150));
        writer.newLine();
        writer.write("LOG DE MUDANÇAS DE ESTADO DOS PROCESSOS");
        writer.newLine();
        writer.write("Formato: [Tempo] PID | Nome | Razão | Estado Inicial -> Próximo Estado | Tabela de Páginas");
        writer.newLine();
        writer.write("=".repeat(150));
        writer.newLine();
        writer.newLine();
        writer.flush();
    }

    public synchronized void logStateChange(PCB pcb, String reason, ProcessState oldState, ProcessState newState) {
        if (writer == null) return;

        try {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            StringBuilder log = new StringBuilder();
            
            log.append(String.format("[%s] ", timestamp));
            log.append(String.format("PID: %-3d | ", pcb.pid));
            log.append(String.format("%-15s | ", pcb.program.name));
            log.append(String.format("%-20s | ", reason));
            log.append(String.format("%-10s -> %-10s | ", 
                oldState == null ? "nulo" : oldState.toString(),
                newState.toString()));
            
            log.append("TabPag: ");
            log.append(formatPageTable(pcb));
            
            writer.write(log.toString());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    private String formatPageTable(PCB pcb) {
        StringBuilder sb = new StringBuilder("{ ");
        
        for (int i = 0; i < pcb.tabPag.length; i++) {
            int frame = pcb.tabPag[i];
            sb.append("[pag:").append(i).append(",");
            
            if (frame == -1) {
                sb.append("frame:_,loc:_");
            } else if (frame == -2) {
                sb.append("frame:_,loc:ms");
            } else {
                sb.append("frame:").append(frame).append(",loc:mp");
            }
            
            sb.append("]");
            
            if (i < pcb.tabPag.length - 1) {
                sb.append(", ");
            }
        }
        
        sb.append(" }");
        return sb.toString();
    }

    public void close() {
        if (writer != null) {
            try {
                writer.write("\n");
                writer.write("=".repeat(150));
                writer.newLine();
                writer.write("FIM DO LOG");
                writer.newLine();
                writer.write("=".repeat(150));
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar arquivo de log: " + e.getMessage());
            }
        }
    }
}
