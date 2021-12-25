public class App {
    public static void main(String[] args){
        Assembler assem = new Assembler();
        
        //pass1
        //gets intermediate code
        assem.generateMachineCode();

        assem.backPatcher();
        // assem.writeToFile();
        //prints symbol table
        assem.printSymbolTable();
        assem.printLiteralTable();
        assem.printPoolTable();
        assem.printTIITable();

        //pass2
        //gets machine code
        // assem.getMachineCode();
    }
}
