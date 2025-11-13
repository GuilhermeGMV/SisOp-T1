public class CPU {
    private final int maxInt;
    private final int minInt;

    // CONTEXTO da CPU
    public PCB pcb;
    private Word ir;
    public int[] reg;
    private Interrupts irpt;
    
    private final Word[] m; // array de memória física
    private InterruptHandling ih;
    private SysCallHandling sysCall;
    private boolean execStop;
    private boolean debug;
    private Utilities u;
    public final int pageSize;
    public final int delta;

    public CPU(Memory _mem, boolean _debug, int _pageSize, int _delta) {
        maxInt = 32767;
        minInt = -32767;
        m = _mem.pos;
        pageSize = _pageSize;
        reg = new int[10];
        delta = _delta;
        debug = _debug;
    }

    public void setAddressOfHandlers(InterruptHandling _ih, SysCallHandling _sysCall) {
        ih = _ih;
        sysCall = _sysCall;
    }

    public void setUtilities(Utilities _u) {
        u = _u;
    }

    public void setDebug(boolean d) {
        debug = d;
    }

    public void setIR(Word ir) {
        this.ir = ir;
    }
    
    public void setIrpt(Interrupts irpt) {
        this.irpt = irpt;
    }

    public Word getIR() {
        return ir;
    }

    public Interrupts getIrpt() {
        return irpt;
    }

    public boolean  getDebug() {
        return debug;
    }

    public int translateAddress(int logicalAddress) {
        if (pcb == null || pcb.tabPag == null) {
            return logicalAddress;
        }
        int pageNumber = logicalAddress / pageSize;
        int offset = logicalAddress % pageSize;
        
        if (pageNumber >= pcb.tabPag.length) {
            irpt = Interrupts.intEnderecoInvalido;
            return -1;
        }
        
        int frameAddress = pcb.tabPag[pageNumber];
        if (frameAddress == -1) {
            irpt = Interrupts.intPageFault;
            return -1;
        }
        
        int physicalAddress = frameAddress + offset;
        return physicalAddress;
    }

    private boolean legal(int logicalAddress) {
        int physicalAddress = translateAddress(logicalAddress);
        if (physicalAddress == -1) {
            return false;
        }
        if (physicalAddress >= 0 && physicalAddress < m.length) {
            return true;
        } else {
            irpt = Interrupts.intEnderecoInvalido;
            return false;
        }
    }

    private boolean testOverflow(int v) {
        if ((v < minInt) || (v > maxInt)) {
            irpt = Interrupts.intOverflow;
            return false;
        }
        return true;
    }

    public void setContext(PCB _pcb) {
        irpt = _pcb.irpt;
        pcb = _pcb;
        reg = _pcb.reg;
        ir = _pcb.ir;
    }

    public void run() {
        int d = 0;
        execStop = false;
        while (!execStop) {
            d++;
            if (legal(pcb.pc)) {
                int physicalPC = translateAddress(pcb.pc);
                ir = m[physicalPC];
                if (debug) {
                    System.out.print("                                              regs: ");
                    for (int i = 0; i < 10; i++) {
                        System.out.print(" r[" + i + "]:" + reg[i]);
                    }
                    System.out.println();
                }
                if (debug) {
                    System.out.print("                      pc: " + pcb.pc + "       exec: ");
                    u.dump(ir);
                }

                switch (ir.opc) {
                    // Instrucoes de Busca e Armazenamento em Memoria
                    case LDI: // Rd ← k        veja a tabela de instrucoes do HW simulado para entender a semantica da instrucao
                        reg[ir.ra] = ir.p;
                        pcb.pc++;
                        break;
                    case LDD: // Rd <- [A]
                        if (legal(ir.p)) {
                            int physicalAddr = translateAddress(ir.p);
                            reg[ir.ra] = m[physicalAddr].p;
                            pcb.pc++;
                        }
                        break;
                    case LDX: // RD <- [RS] // NOVA
                        if (legal(reg[ir.rb])) {
                            int physicalAddr = translateAddress(reg[ir.rb]);
                            reg[ir.ra] = m[physicalAddr].p;
                            pcb.pc++;
                        }
                        break;
                    case STD: // [A] ← Rs
                        if (legal(ir.p)) {
                            int physicalAddr = translateAddress(ir.p);
                            m[physicalAddr].opc = Opcode.DATA;
                            m[physicalAddr].p = reg[ir.ra];
                            pcb.pc++;
                            if (debug)
                                {   System.out.print("                                  ");
                                    u.dump(physicalAddr, physicalAddr+1);
                                }
                        }
                        break;
                    case STX: // [Rd] ←Rs
                        if (legal(reg[ir.ra])) {
                            int physicalAddr = translateAddress(reg[ir.ra]);
                            m[physicalAddr].opc = Opcode.DATA;
                            m[physicalAddr].p = reg[ir.rb];
                            pcb.pc++;
                        }
                        break;
                    case MOVE: // RD <- RS
                        reg[ir.ra] = reg[ir.rb];
                        pcb.pc++;
                        break;
                    // Instrucoes Aritmeticas
                    case ADD: // Rd ← Rd + Rs
                        reg[ir.ra] = reg[ir.ra] + reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pcb.pc++;
                        break;
                    case ADDI: // Rd ← Rd + k
                        reg[ir.ra] = reg[ir.ra] + ir.p;
                        testOverflow(reg[ir.ra]);
                        pcb.pc++;
                        break;
                    case SUB: // Rd ← Rd - Rs
                        reg[ir.ra] = reg[ir.ra] - reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pcb.pc++;
                        break;
                    case SUBI: // RD <- RD - k // NOVA
                        reg[ir.ra] = reg[ir.ra] - ir.p;
                        testOverflow(reg[ir.ra]);
                        pcb.pc++;
                        break;
                    case MULT: // Rd <- Rd * Rs
                        reg[ir.ra] = reg[ir.ra] * reg[ir.rb];
                        testOverflow(reg[ir.ra]);
                        pcb.pc++;
                        break;
                    // Instrucoes JUMP
                    case JMP: // PC <- k
                        pcb.pc = ir.p;
                        break;
                    case JMPIM: // PC <- [A]
                        if (legal(ir.p)) {
                            int physicalAddr = translateAddress(ir.p);
                            pcb.pc = m[physicalAddr].p;
                        }
                        break;
                    case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                        if (reg[ir.rb] > 0) {
                            pcb.pc = reg[ir.ra];
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIGK: // If RC > 0 then PC <- k else PC++
                        if (reg[ir.rb] > 0) {
                            pcb.pc = ir.p;
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPILK: // If RC < 0 then PC <- k else PC++
                        if (reg[ir.rb] < 0) {
                            pcb.pc = ir.p;
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIEK: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.rb] == 0) {
                            pcb.pc = ir.p;
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                        if (reg[ir.rb] < 0) {
                            pcb.pc = reg[ir.ra];
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                        if (reg[ir.rb] == 0) {
                            pcb.pc = reg[ir.ra];
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                        if (legal(ir.p)){
                            int physicalAddr = translateAddress(ir.p);
                            if (reg[ir.rb] > 0) {
                               pcb.pc = m[physicalAddr].p;
                            } else {
                              pcb.pc++;
                           }
                        }
                        break;
                    case JMPILM: // If RC < 0 then PC <- [A] else PC++
                        if (legal(ir.p)) {
                            int physicalAddr = translateAddress(ir.p);
                            if (reg[ir.rb] < 0) {
                                pcb.pc = m[physicalAddr].p;
                            } else {
                                pcb.pc++;
                            }
                        }
                        break;
                    case JMPIEM: // If RC = 0 then PC <- k else PC++
                        if (reg[ir.rb] == 0) {
                            pcb.pc = m[ir.p].p;
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case JMPIGT: // If RS>RC then PC <- k else PC++
                        if (reg[ir.ra] > reg[ir.rb]) {
                            pcb.pc = ir.p;
                        } else {
                            pcb.pc++;
                        }
                        break;
                    case DATA: // pc está sobre área supostamente de dados
                        irpt = Interrupts.intInstrucaoInvalida;
                        break;

                    // Chamadas de sistema
                    case SYSCALL:
                        sysCall.handle();
                        pcb.pc++;
                        break;

                    case STOP: // por enquanto, para execucao
                        sysCall.stop();
                        execStop = true;
                        break;
                    default:
                        irpt = Interrupts.intInstrucaoInvalida;
                        break;
                }
            }
            if (irpt != Interrupts.noInterrupt) {
                ih.handle(irpt);
                execStop = true;
            }
            if(d >= delta) {
                ih.handle(Interrupts.intEscalonar);
                execStop = true;
            }
        } // FIM DO CICLO DE UMA INSTRUÇÃO
      if (debug && delta > d) {
          System.out.println("-------------------------------- programa (" + pcb.program.name + ") depois da execução");
          for (int page : pcb.tabPag) {
              if (page != -1) {
                  System.out.println("Página iniciando em " + page + ":");
                  u.dump(page, Math.min(page + pageSize, m.length));
              }
          }
      }
    }
}
