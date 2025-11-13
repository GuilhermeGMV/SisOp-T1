public class Disco{
  public Programs programs;
  public Word[] vm;

  public Disco(){
    programs = new Programs();
    vm = new Word[10000];
    
    for(int i = 0; i < vm.length; i++){
      vm[i] = new Word(Opcode.___, -1, -1, -1);
    }
  }

  public void loadProgramToDisk(Program program, int startAddress){
    for(int i = 0; i < program.image.length; i++){
      if(startAddress + i < vm.length){
        vm[startAddress + i].opc = program.image[i].opc;
        vm[startAddress + i].ra = program.image[i].ra;
        vm[startAddress + i].rb = program.image[i].rb;
        vm[startAddress + i].p = program.image[i].p;
      }
    }
  }

}