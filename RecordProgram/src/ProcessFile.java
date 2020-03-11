// File: ProcessFile.java

import javax.swing.*;
import java.io.*;

public class ProcessFile {
    private Block block;
    private RandomAccessFile file;
    private double balance;
    private final int pointerSizes=16;
    private long dp;
    private long fp;

    /*

     */
    public ProcessFile() {
        block = new Block();

        try {
            file = new RandomAccessFile("rand.dat", "rw");
            file.seek(0);
            dp = file.readLong();
            fp = file.readLong();
        }

        catch(EOFException eof) {  }
        catch( IOException e ) {
            System.err.println( "File not opened properly\n" + e.toString() );
            System.exit( 1 ); }


        System.out.println ("\nFile dump:\n");
        fileDump();
        }

        /*
        file dump prints everything in the file. as it sits in the file
         */
        public void fileDump(){
        try {
            file.seek(0);
            System.out.println("DP: "+file.readLong());
            System.out.println("FP: "+file.readLong());
            while(file.getFilePointer()!=Block.size()*CreateFile.blockNum()+pointerSizes){
                block.read(file);
                System.out.println(block.toString());
            }
        }catch(IOException e2){
        }
        }

        /*
        display prints only the filled blocks of the file
         */
    public void display(){
        System.out.println("\nDisplay:");
        try {
            file = new RandomAccessFile("rand.dat", "rw" );
            file.seek(0);
            dp=file.readLong();
            fp=file.readLong();
            System.out.println("DP = "+ dp+" FP = "+fp);
            System.out.print("Current: "+dp+ " ");
            file.seek(dp+pointerSizes);
            do {
                block.read(file);
                System.out.println(block.toString());
                if(block.getNext()==-1){
                    break;
                }
                System.out.print("Current: "+block.getNext()+" ");
                file.seek(block.getNext()+pointerSizes);
            }
            while(true);
        } // try

        catch(EOFException eof) {  }
        catch( IOException e ) {
            System.err.println( "File not opened properly\n" + e.toString() );
            System.exit( 1 ); }
    }

    /*
    add method adds a record to the file. throwing an exception should the file be full, or not opened properly
    the add method will add records in order of their account number, sorting the records accordingly, manipulating the pointers
    of each block, prev and next, to act as a sorted linked list
     */

    public void add(Record record){
        System.out.println("\nAdd new Record: "+record);
        try {
            //jump to start of file read DP and FP
            file.seek(0);
            dp = file.readLong();
            fp = file.readLong();
            //if fp = -1 then there is no free block and the file is full
            if (fp == -1) {
                System.out.println("File is full");
                return;
            }
            //seek fp and write the record being added
            file.seek(fp + pointerSizes);
            record.write(file);
            int nextAddress=-1;
            int prevAddress=-1;
            int next=(int)dp;
            int existingAcc;
            /*
            starting at dp loop through the file, to find where in the list the record needs to be inserted
            when the next element is greater than the element being added then the element must be added before that next element in the file
             */

            while (next!=-1){
                file.seek(next+pointerSizes);
                existingAcc=file.readInt();
                if (existingAcc<record.getAccount()) {
                    prevAddress = (int) file.getFilePointer() - 4-pointerSizes;
                }
                else{
                    nextAddress=(int)file.getFilePointer() - 4-pointerSizes;
                }
                file.seek(file.getFilePointer() + Block.size() - 8);
                next = file.readInt();
            }

            if(prevAddress!=-1){
                file.seek(prevAddress+Record.size()+4+pointerSizes);
                nextAddress=file.readInt();
                file.seek(file.getFilePointer() - 4);
                file.writeInt((int)fp);
            }
            else{
                dp=fp;
            }
            if(nextAddress!=-1){
                file.seek(nextAddress+pointerSizes+Record.size());
                file.writeInt((int)fp);
            }
            /*
            jump to prev of fp and write the correct prev address
             */
            file.seek(fp+pointerSizes+Record.size());
            file.writeInt(prevAddress);
            //fp gets assigned to next
            fp = file.readInt();
            //write over what was just captured with the correct next address
            file.seek(file.getFilePointer() - 4);
            file.writeInt(nextAddress);
            //write -1 to prev of fp
            file.seek(fp+pointerSizes+Record.size());
            file.writeInt(-1);
            //write new dp and fp to begining of file
            file.seek(0);
            file.writeLong(dp);
            file.writeLong(fp);
        }
        catch( IOException e ) {
            System.err.println( "File not opened properly\n" +
                    e.toString() );
            System.exit( 1 ); }
    }

    /*
    removes a record from the list having had the account number of the record being deleted passed through

     */
    public void delete(int accountNo){
        //use find method to find the location of the record in the file
        System.out.println("\nDelete with Account No.(key): "+accountNo);
        long location=find(accountNo);
        if (location == -1) {
            System.out.println("Invalid account number");
            return;
        }
            try {
            //read dp and fp from begining of file, if dp is being deleted then dp is assigned to next of dp
                file.seek(0);
                dp = file.readLong();
                fp = file.readLong();
                if (dp + pointerSizes == location) {
                    file.seek(location + Block.size() - 4);
                    dp = file.readInt();
                }
                //seek the location capture the prev and next of desired block
                file.seek(location + Block.size() - 8);
                int prevLocation = file.readInt();
                int nextLocation = file.readInt();
                //next of location prev is assigned next of location
                file.seek(prevLocation + Block.size() + pointerSizes - 4);
                file.writeInt(nextLocation);
                //prev of location next is assigned prev of location
                file.seek(nextLocation + Block.size() + pointerSizes - 8);
                file.writeInt(prevLocation);
                if(fp!=-1) {
                    file.seek(fp + Block.size() + pointerSizes - 8);
                    file.writeInt((int) location - pointerSizes);
                }
                //location becomes new fp
                file.seek(location + Block.size() - 8);
                file.writeInt(-1);
                file.writeInt((int) fp);
                fp = location - pointerSizes;
                //update fp and dp in file
                file.seek(0);
                file.writeLong(dp);
                file.writeLong(fp);

            } catch (IOException e) {
                System.err.println("File not opened properly delete\n" + e.toString());
                System.exit(1);
            }
    }

    /*
    modify takes an account number as an argument, and prompts the user to change one of the properties of that record
     */
    public void modify(int accountNo){
        System.out.println("\nModify with Account No.(key): "+accountNo);
        long location=find(accountNo);
        if (location == -1){
            System.out.println("Invalid account number");
            return;
        }
            try {
                file.seek(location);
                Record temp=new Record();
                temp.read(file);
                System.out.println(block.toString());
                int choice=readInteger("Enter 1 to change last name, 2 for first name, 3 for balance: ",1,3);
                String ans="";
                switch (choice){
                    case 1:
                        ans = JOptionPane.showInputDialog(null, "Enter new last name: ", "");
                        temp.setLastName(ans);
                        break;
                    case 2:
                        ans = JOptionPane.showInputDialog(null, "Enter new first name: ", "");
                        temp.setFirstName(ans);
                        break;
                    case 3:
                        choice=readInteger("Enter new balance: ",-1000000,1000000);
                        temp.setBalance(choice);
                        break;
                }
                file.seek(location);
                temp.write(file);
        }
        catch (IOException e){
            System.err.println("File not opened properly modify\n" + e.toString());
            System.exit(1);
        }
    }
    /*
    returns the ofset of the start of 1 record searched by account number
     */
    public long find(int accountNo) {
        try{
            file.seek(0);
            dp = file.readLong();
            int next=(int)dp;
            int existingAcc;
            while (next!=-1){
                file.seek(next+pointerSizes);
                existingAcc=file.readInt();
                if(existingAcc==accountNo){
                    return file.getFilePointer()-4;
                }
                else if (existingAcc>accountNo) {
                    return -1;
                }
                file.seek(file.getFilePointer() + Block.size() - 8);
                next = file.readInt();
            }
        }catch (IOException e) {
            System.err.println("File not opened properly find\n" + e.toString());
            System.exit(1);

        }
        return -1;
    }
    /*
    readInteger creates a input box with a prompt, minimum and maximum value that it will accept, takes user string input and
    converts it to an integer
     */

    public static int readInteger(String prompt, int min, int max) {
        int val = 0;
        do {
            String ans = JOptionPane.showInputDialog(null, prompt, "");

            try {
                val = Integer.parseInt(ans);
            } catch (NumberFormatException nfe) {
                val = 0;
            }
        } while (val <= min && val>=max);
        return val;
    }
    //readInteger but for doubles
    public static Double readDouble(String prompt, int min, int max) {
        double val = 0;
        do {
            String ans = JOptionPane.showInputDialog(null, prompt, "");

            try {
                val = Double.parseDouble(ans);
            } catch (NumberFormatException nfe) {
                val = 0;
            }
        } while (val <= min && val>=max);
        return val;
    }
    /*
    when run the program first asks the user how many blocks they would like.
    Then prompts for demo mode or manual input demo mode automatically fills in 5 blocks with preset data, in either case afterwards the program simply
    prompts the user to do one of the functions, add a record, modify an existing record, delete a record, display all records or dump the file
     */
    public static void main( String args[] )  {
        int numberOfBlocks=readInteger("Enter total amount of empty blocks",1,100000);
        CreateFile creation = new CreateFile(numberOfBlocks);
        ProcessFile accounts = new ProcessFile();
        int choice=readInteger("1: demo mode, 2: manual",1,2);

        if(choice==1){
            Record rec=new Record(2 ,"Black","Tom",900);
            accounts.add(rec);
            accounts.display();
            rec=new Record(4,"Blue","Lora",900);
            accounts.add(rec);
            accounts.display();
            rec=new Record(7,"White","David",900);
            accounts.add(rec);
            accounts.display();
            rec=new Record(3,"Purple","Nick",900);
            accounts.add(rec);
            accounts.display();
            rec=new Record(5,"Yellow","Brian",900);
            accounts.add(rec);
            accounts.display();
        }

        accounts.modify(9);
        System.out.println("file dump");
        accounts.fileDump();
        while(true){
            choice = readInteger("What would you like to do? 1: add a record, 2: modify a record, 3: delete a record, 4: display records, 5: File Dump 0: exit",0,5);
            switch(choice){
                case 0:
                    System.exit(1);
                case 1:
                        int account =readInteger("Please enter the account number",0,10000);
                        String last = JOptionPane.showInputDialog(null, "Enter last name: ", "");
                        String first =  JOptionPane.showInputDialog(null, "Enter first name: ", "");
                        double balance = readDouble("Please enter the balance ",0,10000);
                        Record record = new Record(account,last,first,balance);
                        accounts.add(record);
                        accounts.display();break;
                case 2:
                    choice=readInteger("what is the account number to modify?",0,999999);
                    accounts.modify(choice);
                    accounts.display();
                    break;
                case 3:
                    choice=readInteger("what is the account number to delete?",0,999999);
                    accounts.delete(choice);
                    accounts.display();
                    break;
                case 4:
                     accounts.display();
                    break;
                case 5:
                    accounts.fileDump();
                    break;
            }
        }
    }
}