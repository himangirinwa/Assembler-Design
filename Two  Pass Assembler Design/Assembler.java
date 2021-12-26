import java.io.*;


public class Assembler extends IntermedCode{
    
    FileReader fr;
    BufferedReader in_read;
    BufferedWriter out_write;

    Assembler(){
        try{
            //reading input file
            in_read = new BufferedReader(new FileReader("IntermediateCode.txt"));

            //for writing intermediate code
            out_write = new BufferedWriter(new FileWriter("MachineCode.txt"));
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    //generating machine code for Imperative statements
    //opcode regOperand memory operand
    StringBuffer getCodeForIS(String[] arr){
        StringBuffer machineCode = new StringBuffer();

        //location 
        machineCode.append(arr[0] + "  (");

        //opcode
        machineCode.append(arr[1].substring(4, 6));

        //register operand
        machineCode.append(")  " + arr[2] + "  ");
            
        //memory operand
        if(!arr[3].equals("-")){
            //memory operand is symbol 
            if(arr[3].charAt(1) == 'S'){
                int symTabRow = Integer.parseInt(arr[3].substring(3, arr[3].length()-1));
                String operandAddress = SymTab.get(symTabRow).getAddress();
                machineCode.append("(" + operandAddress + ")");
            }
            //memory operand is literal
            else{
                int litTabRow = Integer.parseInt(arr[3].substring(3, arr[3].length()-1));
                String operandAddress = LiteralTab.get(litTabRow).getAddress();
                machineCode.append("(" + operandAddress + ")");
            }

        }
        else
            machineCode.append("(" +arr[3] + ")");

    
        return machineCode;
    }

    void getMachineCode(){

        String instruction;
        String[] arr;
        StringBuffer machineCode;

        try{
            while((instruction = in_read.readLine()) != null){
                arr = instruction.split("  ");
                machineCode = new StringBuffer();
                machineCode.append("");
                
                //not processing assembler directives
                if(arr[0].charAt(1) != 'A'){

                    //putting location
                    machineCode.append(arr[0]);
                    
                    //literal storage
                    if(arr[1].equals("(-)") && arr[2].equals("(-)")){
                        //opcode regOp
                        machineCode.append(arr[1] + " " + arr[2] + " " + arr[3]);
                    }

                    //checking for declaration statements
                    else if(arr[1].substring(1, 3).equals("DL")){
                        //opcode regOp
                        machineCode.append("  (-)  (-)  ");
                        //declare constant DC
                        if(arr[1].substring(4, 6).equals("01"))
                            machineCode.append("(" + arr[3].substring(3, arr[3].length()-1) + ")");
                        
                        else
                            machineCode.append("(-)");
                            //opcode regOp MemoryOp
                    }

                    
                    //Imperative statement
                    else
                        machineCode = getCodeForIS(arr);
                }

                out_write.write(machineCode + "\n");
            }    

        }
        catch(Exception e){
            System.out.println(e);
        }
        finally{
            try{
                in_read.close();
                out_write.close();
            }
            catch(Exception e){
                System.out.println(e);
            }
        }
    }

}
