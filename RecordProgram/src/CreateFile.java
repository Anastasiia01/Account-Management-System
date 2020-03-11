// File: CreateFile.java

import javax.swing.*;
import java.io.*;
public class CreateFile {
    private Block block;
    private Record record;
    String [] last  = {"Jones","Schmidt","Thomas","Williams","Randolph"};
    String [] first = {"Joe", "Fred", "Erin", "Ron", "Alvin"};
    int [] account  = { 1001, 1003, 1005, 1006, 1102 };
    double [] bal   = {1075.35, 755, 23000, 45000, 115560.77 };
    private RandomAccessFile file;
    private long DP;
    private long FP;
    private static int totalNumberOfBlocks;


    //creates a random access file and fills in blank blocks to the number of blocks that was entered

    public CreateFile(int numberOfBlocks){
        DP=-1;
        FP=0;
        totalNumberOfBlocks = numberOfBlocks ;
        block=new Block();
        try {

            file = new RandomAccessFile("rand.dat", "rw");
            file.setLength(Block.size()*(totalNumberOfBlocks+16));

            file.writeLong(DP);
            file.writeLong(FP);
            for (int i = 0; i < totalNumberOfBlocks; i++) {
                if(i==0){
                    block.setPrev(-1);
                }
                else{
                    block.setPrev((i-1)*Block.size());
                }
                if(i==totalNumberOfBlocks-1){
                    block.setNext(-1);
                }
                else{
                    block.setNext((i+1)*Block.size());
                }
                block.write(file);
            }

            file.close();

        } catch (IOException e) {
            System.err.println("File not opened properly\n" +
                    e.toString());
            System.exit(1);
        }
    }
    //property value for the number of blocks that were created
    public static int blockNum(){
        return totalNumberOfBlocks;
    }
    public static int readInteger(String prompt, int min) {

        int val = 0;

        do {
            String ans = JOptionPane.showInputDialog(null, prompt, "");

            try {
                val = Integer.parseInt(ans);
            } catch (NumberFormatException nfe) {
                val = 0;
            }
        } while (val < min);

        return val;

    }
    public static void main( String args[] ) {
        int numberOfBlocks=readInteger("Enter total amount of empty blocks",1);
        CreateFile account = new CreateFile(numberOfBlocks);
    }


}
