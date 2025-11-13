import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PageLoader implements Runnable {
    private final HW hw;
    private final SO so;
    private final BlockingQueue<PageRequest> requestQueue;
    private volatile boolean running;
    private final int diskDelay;
    
    public PageLoader(HW hw, SO so, int diskDelay) {
        this.hw = hw;
        this.so = so;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.running = true;
        this.diskDelay = diskDelay;
    }
    
    public void addRequest(PageRequest request) {
        try {
            requestQueue.put(request);
            if (hw.cpu.getDebug()) {
                System.out.println(">>> PageLoader: Requisição adicionada para PID " + request.pcb.pid + 
                                 " (Página " + request.pageNumber + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        System.out.println(">>> PageLoader: Iniciado");
        
        while (running) {
            try {
                PageRequest request = requestQueue.take(); // espera por uma requisição
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> PageLoader: Carregando página " + request.pageNumber + 
                                     " para PID " + request.pcb.pid);
                    System.out.print("SO> ");
                }
                
                Thread.sleep(diskDelay);
                
                synchronized (so) {
                    int frameAddress = so.gm.allocPage(
                        request.pcb.program.image, 
                        request.pageNumber, 
                        request.pcb.tabPag
                    );
                    
                    if (frameAddress != -1) {
                        request.pcb.loadedPages++;
                        request.pcb.nextPageToLoad++;
                        
                        if (hw.cpu.getDebug()) {
                            System.out.println(">>> PageLoader: Página " + request.pageNumber + 
                                             " carregada no frame " + frameAddress + 
                                             " para PID " + request.pcb.pid);
                        }
                        
                        if (so.blocked.contains(request.pcb)) {
                            so.blocked.remove(request.pcb);
                            ProcessState oldState = request.pcb.state;
                            request.pcb.state = ProcessState.READY;
                            so.ready.add(request.pcb);
                            
                            so.logger.logStateChange(request.pcb, "Fim Page Fault (pág " + request.pageNumber + " carregada)", 
                                                    oldState, ProcessState.READY);
                            
                            if (hw.cpu.getDebug()) {
                                System.out.println(">>> PageLoader: Processo PID " + request.pcb.pid + 
                                                 " movido de BLOCKED para READY");
                                so.utils.handlePsCommand();
                            }
                        }
                    } else {
                        System.err.println(">>> PageLoader: Falha ao carregar página " + request.pageNumber + 
                                         " para PID " + request.pcb.pid);
                    }
                }
                
                System.out.print("SO> ");
                System.out.flush();
                
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(">>> PageLoader: Erro - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println(">>> PageLoader: Parado");
    }
    
    public void stop() {
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
