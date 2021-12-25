import java.util.*;
import java.io.*;

public class Assembler {

    int loc_ctr;
    FileReader fr;
    BufferedReader in;
    BufferedWriter out;
    HashMap<String, String> opCode, flagCode, regCode;
    // opcode table, flag code table, register Code, assembler directive table,
    // declaration code tab
    // symbol table
    HashMap<Integer, SymTabElem> SymbolTable;
    HashMap<Integer, LitrTabElem> LiteralTable;
    HashMap<String, String> TableOfIncompleteInstruction;
    // pool table
    ArrayList<Integer> pooltab;
    // assembler directives, declaration statement
    ArrayList<String> asmDirective, declStat;
    ArrayList<String> buffer = new ArrayList<String>();

    Assembler() {
        try {
            // reading input file
            FileReader fr = new FileReader(new File("InputCode.txt"));
            in = new BufferedReader(fr);

            // for writing intermediate code
            out = new BufferedWriter(new FileWriter("MachineCode.txt"));
        } catch (Exception e) {
            System.out.println(e);
        }

        // assembler directives
        asmDirective = new ArrayList<String>();
        // declaration statements
        declStat = new ArrayList<String>();
        // opcode table
        opCode = new HashMap<String, String>();
        // flag code table
        flagCode = new HashMap<String, String>();
        // register code table
        regCode = new HashMap<String, String>();

        // symbol table
        SymbolTable = new HashMap<Integer, SymTabElem>();
        // literal table
        LiteralTable = new HashMap<Integer, LitrTabElem>();
        // table of incomplete instruction
        TableOfIncompleteInstruction = new HashMap<String, String>();
        // pooltable
        pooltab = new ArrayList<Integer>();

        // initializing assembler
        this.initialize();
    }

    class SymTabElem {
        String Symbol;
        String address;

        SymTabElem(String Symbol) {
            this.Symbol = Symbol;
            this.address = null;
        }

        void setAddress(String address) {
            this.address = address;
        }

        String getSymbol() {
            return this.Symbol;
        }

        String getAddress() {
            return this.address;
        }
    }

    class LitrTabElem {
        private String literal;
        private String address;

        LitrTabElem(String Symbol) {
            this.literal = Symbol;
            this.address = null;
        }

        void setAddress(String address) {
            this.address = address;
        }

        String getLiteral() {
            return this.literal;
        }

        String getAddress() {
            return this.address;
        }
    }

    // initializing assembler
    void initialize() {
 
        //assembler directive
        asmDirective.add("START");  asmDirective.add("END");  asmDirective.add("ORIGIN");  asmDirective.add("EQU");  asmDirective.add("LTORG");
        
        //declaration statement
        declStat.add("DC");   declStat.add("DS");

        // creating opcode table
        opCode.put("STOP", "00");
        opCode.put("ADD", "01");
        opCode.put("SUB", "02");
        opCode.put("MULT", "03");
        opCode.put("MOVER", "04");
        opCode.put("MOVEM", "05");
        opCode.put("COMP", "06");
        opCode.put("BC", "07");
        opCode.put("DIV", "08");
        opCode.put("READ", "09");
        opCode.put("PRINT", "10");

        // register code
        regCode.put("AREG", "01");
        regCode.put("BREG", "02");
        regCode.put("CREG", "03");
        regCode.put("DREG", "04");

        // flag code table
        flagCode.put("LT", "01");
        flagCode.put("LE", "02");
        flagCode.put("EQ", "03");
        flagCode.put("GT", "04");
        flagCode.put("GE", "05");
        flagCode.put("ANY", "06");
    }

    // checking for is symbol present in symbol table
    // it will return row number of the symbol if present or else return 0
    int isSymPresent(String Symbol) {

        int res = 0;

        // iterating hashmap
        for (Map.Entry<Integer, SymTabElem> entry : SymbolTable.entrySet()) {

            if (entry.getValue().getSymbol().equals(Symbol)) {
                res = entry.getKey();
                break;
            }
        }

        return res;
    }

    int isLiteralPresent(String literal) {

        int res = 0;

        // iterating hashmap
        for (Map.Entry<Integer, LitrTabElem> entry : LiteralTable.entrySet()) {
            if (entry.getKey() >= pooltab.get(pooltab.size() - 1)) {
                if (entry.getValue().getLiteral().equals(literal)) {
                    res = entry.getKey();
                    break;
                }
            }

        }

        return res;
    }

    // generates Instrction code for Imperative Statement
    int[] getInstrucCode(int loc_ctr, int sym_ptr, int lit_ptr, String[] arr) {
        try {
            
            String interCode = "";

            // statement of type (opcode - -)
            // stop statement
            if (arr[0].equalsIgnoreCase("STOP")) {
                interCode = loc_ctr + ") (" + opCode.get(arr[0].toUpperCase()) + ") (-) (-)";
            }

            // statement of type (opcode, memoryoperand)
            // read, print
            else if (arr[0].equalsIgnoreCase("READ") || arr[0].equalsIgnoreCase("PRINT")) {
                // checking is memory operand specified in the instruction is literal
                if (arr[1].charAt(0) == '=') {
                    int row = isLiteralPresent(arr[1]);
                    if (row == 0) {
                        row = ++lit_ptr;
                        LiteralTable.put(lit_ptr, new LitrTabElem(arr[1]));
                    }
                }
                // memory operand is symbol
                else {

                    // checking is memory operand specified in the instruction is present in the
                    // symbol table
                    int row = isSymPresent(arr[1]);

                    // if symbol is not present in symbol table, then putting it into the symbol
                    // table
                    if (row == 0) {
                        SymTabElem temp = new SymTabElem(arr[1]);
                        row = ++sym_ptr;
                        // adding symbol into the symbol table
                        SymbolTable.put(row, temp);
                    }

                }

                // generating instruction code
                interCode = loc_ctr + ") (" + opCode.get(arr[0].toUpperCase()) + ") (-)" ;
                buffer.add(interCode);
                // adding instruction in table of incomplete instruction
                TableOfIncompleteInstruction.put(Integer.toString(loc_ctr), arr[1]);

            }

            // branch condition (opcode flag memoryOperand)
            else if (arr[0].equalsIgnoreCase("BC")) {
                // checking is memory operand specified in the instruction is present in the
                // symbol table
                int row = isSymPresent(arr[2]);

                // if symbol is not present in symbol table, then putting it into the symbol
                // table
                if (row == 0) {
                    SymTabElem temp = new SymTabElem(arr[2]);
                    row = ++sym_ptr;
                    // adding symbol into the symbol table
                    SymbolTable.put(row, temp);
                }

            }

            else {
            
                // if memory operand is literal
                if (arr[2].charAt(0) == '=') {
                    int row = isLiteralPresent(arr[2]);
                    if (row == 0) {
                        row = lit_ptr++;
                        LiteralTable.put(lit_ptr, new LitrTabElem(arr[2]));
                    }

                }

                // if memory operand is symbol
                else {

                    // Imperative statement of format (opcode reg memoryOperand)
                    // checking is memory operand specified in the instruction is present in the
                    // symbol table
                    int row = isSymPresent(arr[2]);

                    // if symbol is not present in symbol table, then putting it into the symbol
                    // table
                    if (row == 0) {
                        SymTabElem temp = new SymTabElem(arr[2]);
                        row = ++sym_ptr;
                        // adding symbol into the symbol table
                        SymbolTable.put(row, temp);

                    }

                }

                // generating instruction code
                interCode = loc_ctr + ") (" + opCode.get(arr[0].toUpperCase()) + ") ("+ regCode.get(arr[1].substring(0, 4).toUpperCase()) +")";
                buffer.add(interCode);
                // adding instruction in table of incomplete instruction
                TableOfIncompleteInstruction.put(Integer.toString(loc_ctr), arr[2]);

            }

        } catch (Exception e) {
            System.out.println(e);
        }

        return new int[] { sym_ptr, lit_ptr };
    }

    // pass1
    void generateMachineCode() {
        String instruction;
        String[] arr;
        // symbol table pointer
        int sym_ptr = 0, lit_ptr = 0;
        // location counter
        loc_ctr = 0;

        // first pool tab
        pooltab.add(1);

        try {
            while ((instruction = in.readLine()) != null) {
                arr = instruction.split(" ");


                // is end statement
                if (arr[0].equalsIgnoreCase("END")) {
                    
                    if (pooltab.get(pooltab.size() - 1) <= lit_ptr) {
                        for (int i = pooltab.get(pooltab.size() - 1); i <= lit_ptr; i++) {
                            LiteralTable.get(i).setAddress(Integer.toString(loc_ctr));
                            String interCode = Integer.toString(loc_ctr) + ")  (-)  (-)  (00" + LiteralTable.get(i).getLiteral().substring(2, LiteralTable.get(i).getLiteral().length() -1) + ")" ;
                            buffer.add(interCode);                          
                            loc_ctr++;
                        }
                        pooltab.add(lit_ptr + 1);
                    }
                    return;
                }

                // checking for label
                if (!arr[0].equalsIgnoreCase("STOP") && !arr[0].equalsIgnoreCase("LTORG")
                        && !arr[1].equalsIgnoreCase("EQU")
                        && (opCode.containsKey(arr[1]) || asmDirective.contains(arr[1]))) {
                    // if label is present, then checking its existence in symbol table.
                    int row = isSymPresent(arr[0]);

                    SymTabElem temp = new SymTabElem(arr[0]);
                    // updating address for label
                    temp.address = Integer.toString(loc_ctr);

                    // if label is not present in symbol table, then putting it into the symbol
                    // table
                    if (row == 0) {
                        row = ++sym_ptr;
                        // adding symbol into the symbol table
                        SymbolTable.put(row, temp);
                    } else {
                        // adding symbol into the symbol table
                        SymbolTable.put(row, temp);
                    }

                    // giving further instruction for processing
                    for (int i = 1; i < arr.length; i++)
                        arr[i - 1] = arr[i];
                }

                // start or origin statement
                // updating location counter
                if (arr[0].equalsIgnoreCase("START") || arr[0].equalsIgnoreCase("ORIGIN")) {

                    if (arr[1].contains("+")) {
                        String[] operators = arr[1].split("\\+");
                        loc_ctr = Integer.parseInt(SymbolTable.get(isSymPresent(operators[0])).getAddress())
                                + Integer.parseInt(operators[1]);
                    } else {
                        loc_ctr = Integer.parseInt(arr[1]);
                    }

                }

                else if (arr[0].equalsIgnoreCase("LTORG")) {

                    // alloting memory to iterals
                    for (int i = pooltab.get(pooltab.size() - 1); i <= lit_ptr; i++) {
                        String interCode = "";
                        LiteralTable.get(i).setAddress(Integer.toString(loc_ctr));
                        interCode = loc_ctr + ") (-) (-) (00" + LiteralTable.get(i).getLiteral().substring(2, (LiteralTable.get(i).getLiteral().length() - 2)) + ")";
                        buffer.add(interCode);
                        loc_ctr++;
                    }
                    pooltab.add(lit_ptr + 1);
                }

                // declarartion statement
                else if (!arr[0].equalsIgnoreCase("STOP") && declStat.contains(arr[1].toUpperCase())) {
                    String interCode = "";
                    // updating address in the symbol table
                    int row = isSymPresent(arr[0]);
                    SymbolTable.get(row).setAddress(Integer.toString(loc_ctr));
                   

                    // generating instruction code
                    interCode = loc_ctr + ") (-) (-) ";

                    if (arr[1].equalsIgnoreCase("DS")){
                        interCode += "(-)";
                        loc_ctr += Integer.parseInt(arr[2]);
                    }
                    else{
                        interCode += "(00" + arr[2] + ")";
                        ++loc_ctr;
                    }

                    buffer.add(interCode);
                }

                // equate statement
                else if (!arr[0].equalsIgnoreCase("STOP") && arr[1].equalsIgnoreCase("EQU")) {
                    // getting row number of symbol table for both the memory operands
                    int index1 = isSymPresent(arr[0]);
                    int index2 = isSymPresent(arr[2]);

                    // updating address in symbol table
                    SymbolTable.get(index1).setAddress(SymbolTable.get(index2).getAddress());
                }

                // Imperative statements
                else {
                    int[] ptr = getInstrucCode(loc_ctr, sym_ptr, lit_ptr, arr);
                    // updating symbol table pointer and literal table pointer
                    sym_ptr = ptr[0];
                    lit_ptr = ptr[1];
                    loc_ctr++;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
           
            try {
                in.close();
               
            } catch (Exception e) {
                System.out.print(e);
            }
            
        }
    }


    String getSymbolAddress(String symbol) {

        String address = "";

        for (Map.Entry<Integer, SymTabElem> entry : SymbolTable.entrySet()) {

            if (entry.getValue().getSymbol().equals(symbol)) {
                address = entry.getValue().getAddress();
                break;
            }
        }

        return address;
    }

    String getLiteralAddress(String literal, int locationCtr) {

        String address = "";
        int startofPool=1, endOfPool = LiteralTable.size();
        // getting literal pool
        for(int i=0; i< pooltab.size(); i++){

            if(Integer.parseInt(LiteralTable.get(pooltab.get(i)).getAddress()) > locationCtr){
          
                if(i == 0) {
                    endOfPool = pooltab.get(i+1);
                }
                else {
                    startofPool = pooltab.get(i-1);
                    endOfPool = pooltab.get(i);
                }
                break;
            }
            
        }
        
        for(int i= startofPool; i<endOfPool; i++){
            
            if (LiteralTable.get(i).getLiteral().equals(literal)) {
                address = LiteralTable.get(i).getAddress();
                break;
            }
        }

        return address;
    }

    int getInstructionLocationInBuffer(String loc_ctr){
       
        for(int i=0; i<buffer.size(); i++){
            if(buffer.get(i).contains(loc_ctr)){
                return i;
            }
        }

        return -1;
    }

    void backPatcher(){
        
        // iterating TII table
        // using for-each loop for iteration over Map.entrySet()
        for (Map.Entry<String,String> entry : TableOfIncompleteInstruction.entrySet()){
            
            String memoryOperandLocation = "";
            // checking if the incomplete instruction contains symbol or literal
            if(entry.getValue().contains("=")){
                // memory operand is literal
                memoryOperandLocation = getLiteralAddress(entry.getValue(), Integer.parseInt(entry.getKey()));
            }
            else {
                // memory operand is symbol
                memoryOperandLocation = getSymbolAddress(entry.getValue());
            }

            int instructionIndex = getInstructionLocationInBuffer(entry.getKey());
            if(instructionIndex != -1){
                String machineCode = buffer.get(instructionIndex) + " (" + memoryOperandLocation + ")";
                buffer.set(instructionIndex, machineCode);
            }
        }

      
            for(int i=0; i<buffer.size(); i++){
                String temp = buffer.get(i);
                System.out.println(temp);

                try{
                    out.write(temp + "\n");
                }
                catch(Exception e){System.out.println(e);}
            }

            try {
                out.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        
    }


    void writeToFile() {
        try{
            for(int i=0; i<buffer.size(); i++){
                out.write(buffer.get(i) + "\n");
            }
        }
        catch(Exception e){
            System.out.print(e);
        }
        
    }
    

    void printSymbolTable() {
        System.out.println("\n\n  |------------------------------|");
        System.out.println("  |         SYMBOL TABLE         |");
        System.out.println("  |------------------------------|");
        for (Map.Entry<Integer, SymTabElem> entry : SymbolTable.entrySet()) {
            System.out.println("  |   " + entry.getKey() + " |\t" + entry.getValue().getSymbol() + "\t|  "
                    + entry.getValue().getAddress() + "\t | ");
            System.out.println("  |------------------------------|");
        }
    }

    void printLiteralTable() {

        System.out.println("\n\n\n  |------------------------------|");
        System.out.println("  |         LITERAL TABLE        |");
        System.out.println("  |------------------------------|");

        for (Map.Entry<Integer, LitrTabElem> entry : LiteralTable.entrySet()) {
            System.out.println("  |   " + entry.getKey() + " |\t" + entry.getValue().getLiteral() + "\t|  "
                    + entry.getValue().getAddress() + "\t | ");
            System.out.println("  |------------------------------|");
        }
    }

    void printPoolTable() {

        System.out.println("\n\n\n  |----------------|");
        System.out.println("  |    POOL TABLE  |");
        System.out.println("  |----------------|");

        for (int i = 0; i < pooltab.size() - 1; i++) {
            System.out.println("  |        " + pooltab.get(i) + "       |");
            System.out.println("  |----------------|");
        }

        System.out.println("\n\n");
    }

    void printTIITable() {

        System.out.println("\n\n\n  |---------------------------------|");
        System.out.println("  | TABLE OF INCOMPLETE INSTRUCTION |");
        System.out.println("  |---------------------------------|");

        for (Map.Entry<String, String> entry : TableOfIncompleteInstruction.entrySet()) {
            System.out.println("  \t" + entry.getKey() + " \t|\t" + entry.getValue() );
            System.out.println("  |---------------------------------|");
        }

        System.out.println("\n\n");
    }
}
