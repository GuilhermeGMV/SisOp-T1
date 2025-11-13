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
                System.out.println(">>> PageLoader: Request added for PID " + request.pcb.pid + 
                                 " (Page " + request.pageNumber + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void run() {
        System.out.println(">>> PageLoader: Started");
        
        while (running) {
            try {
                PageRequest request = requestQueue.take(); // espera por uma requisição
                
                if (hw.cpu.getDebug()) {
                    System.out.println(">>> PageLoader: Loading page " + request.pageNumber + 
                                     " for PID " + request.pcb.pid);
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
                            System.out.println(">>> PageLoader: Page " + request.pageNumber + 
                                             " loaded at frame " + frameAddress + 
                                             " for PID " + request.pcb.pid);
                        }
                        
                        if (so.blocked.contains(request.pcb)) {
                            so.blocked.remove(request.pcb);
                            request.pcb.state = ProcessState.READY;
                            so.ready.add(request.pcb);
                            
                            if (hw.cpu.getDebug()) {
                                System.out.println(">>> PageLoader: Process PID " + request.pcb.pid + 
                                                 " moved from BLOCKED to READY");
                            }
                        }
                    } else {
                        System.err.println(">>> PageLoader: Failed to load page " + request.pageNumber + 
                                         " for PID " + request.pcb.pid);
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
                System.err.println(">>> PageLoader: Error - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println(">>> PageLoader: Stopped");
    }
    
    public void stop() {
        running = false;
    }
    
    public boolean isRunning() {
        return running;
    }
}
