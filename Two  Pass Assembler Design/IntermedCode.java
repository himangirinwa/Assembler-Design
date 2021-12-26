import java.io.*;
import java.util.*;

class IntermedCode {

    int loc_ctr;
    FileReader fr;
    BufferedReader in;
    BufferedWriter out;
    HashMap<String, String> opCode, flagCode, regCode, asmDirective, declStat;
    //opcode table, flag code table, register Code, assembler directive table, declaration code tab
    //symbol table
    HashMap<Integer, SymTabElem> SymTab;   
    HashMap<Integer, LitrTabElem> LiteralTab;  
    //pool table
    ArrayList<Integer> pooltab;

    IntermedCode(){
        try{
            //reading input file
            FileReader fr = new FileReader(new File("InputCode.txt"));
            in = new BufferedReader(fr);

            //for writing intermediate code
            out = new BufferedWriter(new FileWriter("IntermediateCode.txt"));
        }
        catch(Exception e){
            System.out.println(e);
        }

        //assembler directive table
        asmDirective = new HashMap<String, String>();
        //declaration code table
        declStat = new HashMap<String, String>();
        //opcode table
        opCode = new HashMap<String, String>();
        //flag code table
        flagCode = new HashMap<String, String>();
        //register code table
        regCode = new HashMap<String, String>();

        //symbol table
        SymTab = new HashMap<Integer, SymTabElem>();
        //literal table
        LiteralTab = new HashMap<Integer, LitrTabElem>();

        //pooltable
        pooltab = new ArrayList<Integer>();

        //initializing assembler
        this.initialize();
    }

    class SymTabElem{
        String Symbol;
        String address;

        SymTabElem(String Symbol){
            this.Symbol = Symbol;   this.address = null;
        }

        void setAddress(String address){
            this.address = address;
        }

        String getSymbol(){
            return this.Symbol;
        }

        String getAddress(){
            return this.address;
        }
    }

    class LitrTabElem{
        private String literal;
        private String address;

        LitrTabElem(String Symbol){
            this.literal = Symbol;   this.address = null;
        }

        void setAddress(String address){
            this.address = address;
        }


        String getLiteral(){
            return this.literal;
        }

        String getAddress(){
            return this.address;
        }
    }
    
    //initializing assembler
    void initialize(){

        //assembler directive
        asmDirective.put("START", "01");  asmDirective.put("END", "02");  asmDirective.put("ORIGIN", "03");  asmDirective.put("EQU", "04");  asmDirective.put("LTORG", "05");
        
        //declaration statement
        declStat.put("DC", "01");   declStat.put("DS", "02");

        //creating opcode table
        opCode.put("STOP", "00");    opCode.put("ADD", "01");    opCode.put("SUB", "02");    opCode.put("MULT", "03");
        opCode.put("MOVER", "04");    opCode.put("MOVEM", "05");    opCode.put("COMP", "06");    opCode.put("BC", "07");
        opCode.put("DIV", "08");    opCode.put("READ", "09"); opCode.put("PRINT", "10");

        //register code
        regCode.put("AREG", "01");    regCode.put("BREG", "02");    regCode.put("CREG", "03");    regCode.put("DREG", "04");

        //flag code table
        flagCode.put("LT", "01");    flagCode.put("LE", "02"); flagCode.put("EQ", "03"); flagCode.put("GT", "04"); flagCode.put("GE", "05");
        flagCode.put("ANY", "06");
    }

    //checking for is symbol present in symbol table
    //it will return row number of the symbol if present or else return 0
    int isSymPresent(String Symbol){

        int res = 0;

        //iterating hashmap
        for (Map.Entry<Integer, SymTabElem> entry : SymTab.entrySet()){

            
            if(entry.getValue().getSymbol().equals(Symbol)){
                res = entry.getKey();
                break;
            }
        }
    
        return res;
    }

    int isLiteralPresent(String literal){

        int res = 0;

        //iterating hashmap
        for (Map.Entry<Integer, LitrTabElem> entry : LiteralTab.entrySet()){
            if(entry.getKey()>= pooltab.get(pooltab.size()-1)){
                if(entry.getValue().getLiteral().equals(literal)){
                    res = entry.getKey();
                    break;
                }
            }
            
            
        }
    
        return res;
    }

    //generates Instrction code for Imperative Statement
    int[] getInstrucCode(int loc_ctr, int sym_ptr, int lit_ptr, String[] arr){
        try{
        String interCode = "";

        //statement of type (opcode -  -)
        //stop statement 
        if(arr[0].equalsIgnoreCase("STOP")){
            interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  -  -";
            
        }
        
        //statement of type (opcode, memoryoperand)
        //read, print
        else if(arr[0].equalsIgnoreCase("READ") || arr[0].equalsIgnoreCase("PRINT")){
            //checking is memory operand specified in the instruction is literal
            if(arr[1].charAt(0) == '='){
                int row = isLiteralPresent(arr[1]);
                if(row == 0){
                    row = ++lit_ptr;
                    LiteralTab.put(lit_ptr,  new LitrTabElem(arr[1]));
                }
                
                interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  -  (L," + row + ")";   
            }
            //memory operand is symbol
            else {

                //checking is memory operand specified in the instruction is present in the symbol table
                int row = isSymPresent(arr[1]);
    
                //if symbol is not present in symbol table, then putting it into the symbol table
                if(row == 0){
                    SymTabElem temp = new SymTabElem(arr[1]);
                    row = ++sym_ptr;
                    //adding symbol into the symbol table
                    SymTab.put(row, temp);
                }

                interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  -  (S," + row + ")";     

            }
            
            
        }

        //branch condition (opcode flag memoryOperand)
        else if(arr[0].equalsIgnoreCase("BC")){
            //checking is memory operand specified in the instruction is present in the symbol table
            int row = isSymPresent(arr[2]);
    
            //if symbol is not present in symbol table, then putting it into the symbol table
            if(row == 0){
                SymTabElem temp = new SymTabElem(arr[2]);
                row = ++sym_ptr;
                //adding symbol into the symbol table
                SymTab.put(row, temp);
            }

            interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  ("+ flagCode.get(arr[1].substring(0, 2).toUpperCase()) +")  (S," + row + ")";
        }
        
        else{


            //if memory operand is literal
            
            if(arr[2].charAt(0) == '='){
                int row = isLiteralPresent(arr[2]);
                if(row == 0){
                    row = lit_ptr++;
                    LiteralTab.put(lit_ptr,  new LitrTabElem(arr[2]));
                }
                
                interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  (" + regCode.get(arr[1].substring(0, 4).toUpperCase()) + ")  (L," + row + ")"; 
            }
            

            //if memory operand is symbol
            else {

                
                //Imperative statement of format (opcode reg memoryOperand)
                //checking is memory operand specified in the instruction is present in the symbol table
                int row = isSymPresent(arr[2]);
        
                //if symbol is not present in symbol table, then putting it into the symbol table
                if(row == 0){
                    SymTabElem temp = new SymTabElem(arr[2]);
                    row = ++sym_ptr;
                    //adding symbol into the symbol table
                    SymTab.put(row, temp);
        
                }
        
                //constructing intermediate code
                interCode = Integer.toString(loc_ctr) + ")  (IS," + opCode.get(arr[0].toUpperCase()) + ")  (" + regCode.get(arr[1].substring(0, 4).toUpperCase()) + ")  (S," + row + ")";

            }
            
        }

        try{
            out.write(interCode + "\n");
        }
        catch(Exception e){System.out.println(e);}
    }
    catch(Exception e){
        System.out.println(e);
    }

        return new int[]{sym_ptr, lit_ptr};
    }

    
    //pass1
    void generateIntermediatecode(){
        String instruction, interCode;
        String[] arr;
        //symbol table pointer
        int sym_ptr = 0, lit_ptr = 0;
        //location counter
        loc_ctr = 0;
        
        //first pool tab
        pooltab.add(1);
        
        try{
            while((instruction = in.readLine()) != null){
                arr = instruction.split(" ");

                interCode="";

                //is end statement
                if(arr[0].equalsIgnoreCase("END")){
                    interCode = "(AD," + asmDirective.get(arr[0].toUpperCase()) + ")";
                    out.write(interCode + "\n");

                    if(pooltab.get(pooltab.size()-1) <= lit_ptr){
                        for(int i = pooltab.get(pooltab.size()-1); i <= lit_ptr; i++){
                            LiteralTab.get(i).setAddress(Integer.toString(loc_ctr));
                            interCode = Integer.toString(loc_ctr) + ")  (-)  (-)  00" + LiteralTab.get(i).getLiteral().substring(2, LiteralTab.get(i).getLiteral().length() -1);
                            out.write(interCode + "\n");
                            loc_ctr++;
                        }
    
                        pooltab.add(lit_ptr + 1);
                    }

                    break;
                }

                //checking for label
                if(!arr[0].equalsIgnoreCase("STOP") && !arr[0].equalsIgnoreCase("LTORG") && !arr[1].equalsIgnoreCase("EQU") && (opCode.containsKey(arr[1]) || asmDirective.containsKey(arr[1]))){
                    //if label is present, then checking its existence in symbol table.
                    int row = isSymPresent(arr[0]);

                    SymTabElem temp = new SymTabElem(arr[0]);
                    // updating address for label
                    temp.address = Integer.toString(loc_ctr);
    
                    //if label is not present in symbol table, then putting it into the symbol table
                    if(row == 0){
                        row = ++sym_ptr;
                        //adding symbol into the symbol table
                        SymTab.put(row, temp);
                    }
                    else{
                        //adding symbol into the symbol table
                        SymTab.put(row, temp);
                    }

                    //giving further instruction for processing
                    for(int i=1; i<arr.length; i++)
                        arr[i-1] = arr[i];
                }

                
                //start or origin statement
                //updating location counter
                if(arr[0].equalsIgnoreCase("START") || arr[0].equalsIgnoreCase("ORIGIN")){

                    
                    if(arr[1].contains("+")){
                        String[] operators = arr[1].split("\\+");
                        loc_ctr = Integer.parseInt(SymTab.get(isSymPresent(operators[0])).getAddress()) + Integer.parseInt(operators[1]);
                    }
                    else {
                        loc_ctr = Integer.parseInt(arr[1]);
                    }
                    
                    interCode = "(AD," + asmDirective.get(arr[0].toUpperCase()) + ")  -  (C," + loc_ctr + ")";
                    
                    out.write(interCode + "\n");
                }

                else if(arr[0].equalsIgnoreCase("LTORG")){
                   
                    interCode = "(AD," + asmDirective.get(arr[0].toUpperCase()) + ") ";
                    out.write(interCode + "\n");
                    
                    //alloting memory to iterals
                    for(int i = pooltab.get(pooltab.size()-1); i <= lit_ptr; i++){
                        LiteralTab.get(i).setAddress(Integer.toString(loc_ctr));
                        interCode = Integer.toString(loc_ctr) + ")  (-)  (-)  00" + LiteralTab.get(i).getLiteral().substring(2, LiteralTab.get(i).getLiteral().length() -1);
                        out.write(interCode + "\n");
                        loc_ctr++;
                    }

                    pooltab.add(lit_ptr + 1);
                }
                
                //declarartion statement
                else if(!arr[0].equalsIgnoreCase("STOP") && declStat.containsKey(arr[1].toUpperCase())){

                    //putting address in the symbol table
                    int row = isSymPresent(arr[0]);
                    SymTab.get(row).setAddress(Integer.toString(loc_ctr));;

                    //generating instruction code
                    interCode =  Integer.toString(loc_ctr) + ")  (DL," + declStat.get(arr[1].toUpperCase()) + ")  -  (C," + arr[2] + ")";
                    out.write(interCode + "\n");

                    if(arr[1].equalsIgnoreCase("DS"))
                        loc_ctr += Integer.parseInt(arr[2]);
                    else
                        ++loc_ctr;
                }

                //equate statement
                else if(!arr[0].equalsIgnoreCase("STOP") && arr[1].equalsIgnoreCase("EQU")){
                    //getting row number of symbol table for both the memory operands
                    int index1 = isSymPresent(arr[0]);
                    int index2 = isSymPresent(arr[2]);

                    //updating address in symbol table
                    SymTab.get(index1).setAddress(SymTab.get(index2).getAddress());

                    //instruction code
                    interCode = "(AD," + asmDirective.get(arr[1].toUpperCase()) + ")";
                    out.write(interCode + "\n");
                }

                //Imperative statements
                else{
                    int[] ptr = getInstrucCode(loc_ctr, sym_ptr, lit_ptr, arr);
                    //updating symbol table pointer and literal table pointer
                    sym_ptr = ptr[0];
                    lit_ptr = ptr[1];
                    loc_ctr++;
                }
            }   
        }
        catch(Exception e){
            System.out.println(e);
        }
        finally{
            try{
                in.close();
                out.close();
            }
            catch(Exception e){
                System.out.print(e);
            }
        }
    }

    void printSymbolTable(){
        System.out.println("\n\n  |------------------------------|");
        System.out.println("  |         SYMBOL TABLE         |");
        System.out.println("  |------------------------------|");
        for (Map.Entry<Integer, SymTabElem> entry : SymTab.entrySet()){
            System.out.println("  |   " + entry.getKey() + " |\t" + entry.getValue().getSymbol() + "\t|  " + entry.getValue().getAddress() + "\t | ");
            System.out.println("  |------------------------------|");
        }
    }

    void printLiteralTable(){

        System.out.println("\n\n\n  |------------------------------|");
        System.out.println("  |         LITERAL TABLE        |");
        System.out.println("  |------------------------------|");

        for (Map.Entry<Integer, LitrTabElem> entry : LiteralTab.entrySet()){
            System.out.println("  |   " + entry.getKey() + " |\t" + entry.getValue().getLiteral() + "\t|  " + entry.getValue().getAddress() + "\t | ");
            System.out.println("  |------------------------------|");
        }
    }

    void printPoolTable(){

        System.out.println("\n\n\n  |----------------|");
        System.out.println("  |    POOL TABLE  |");
        System.out.println("  |----------------|");

        for(int i=0; i<pooltab.size()-1; i++){
            System.out.println("  |        " + pooltab.get(i) + "       |");
            System.out.println("  |----------------|");
        }

        System.out.println("\n\n");
    }
}