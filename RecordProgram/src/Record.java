// File: Record.java

import java.io.*;
import java.text.DecimalFormat;


public class Record {
    private int account;
    private String lastName;
    private String firstName;
    private double balance;
    private final int LENGTH = 15;
    private DecimalFormat nice = new DecimalFormat ("0.00");

    //constructors
    public Record() {
        this.account = 0;
        this.lastName = "";
        this.firstName = "";
        this.balance = 0.0;
    }

    public Record(int account, String lastName, String firstName, double balance) {
        this.account = account;
        this.lastName = lastName;
        this.firstName = firstName;
        this.balance = balance;
    }

    //reads from a file assuming the record is organized in this specific way
    public void read(RandomAccessFile file ) throws IOException   {
        account = file.readInt();
        firstName = file.readUTF();
        lastName =  file.readUTF();
        balance = file.readDouble();  }

        //writes the record and conforms the record to ensure it will always be the same number of bytes

    public void write( RandomAccessFile file ) throws IOException {
        file.writeInt( account );

        StringBuffer buf = new StringBuffer();

        buf.setLength(15);

        for (int i = 0; i < firstName.length(); i++)
        {
            buf.setCharAt(i, firstName.charAt(i));
        }

        for (int i = firstName.length(); i < 15; i++)
            buf.setCharAt(i, ' ');

        buf.setLength(15);

        file.writeUTF(buf.toString());


        for (int i = 0; i < lastName.length(); i++)
            buf.setCharAt(i, lastName.charAt(i));

        for (int i = lastName.length(); i < 15; i++)
            buf.setCharAt(i, ' ');

        buf.setLength( 15 );

        file.writeUTF(buf.toString());

        file.writeDouble( balance );

    }

    //get and set methods for data
    public void setAccount( int a ) { account = a; }

    public int getAccount() { return account; }

    public void setFirstName( String f ) { firstName = new String(f); }

    public String getFirstName() { return firstName; }

    public void setLastName( String l ) { lastName = new String(l); }

    public String getLastName() { return lastName; }

    public void setBalance( double b ) { balance = b; }

    public double getBalance() { return balance; }

    public String toString() {
        return " Account No.(key): " + account + "  " +firstName + " " + lastName + " " + nice.format(balance); }

    // NOTE: This method contains a hard coded value for the
    // size of a record of information. Check to make sure it is correct

    public static int size() { return 46; } }
