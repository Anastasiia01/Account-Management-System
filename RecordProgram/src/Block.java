import java.io.IOException;
import java.io.RandomAccessFile;

public class Block {
    private int prev;
    private int next;
    private Record record;

    public Block() {
        this.prev = 0;
        this.next = 0;
        this.record = new Record();
    }

    public void read(RandomAccessFile file ) throws IOException {
        record.read(file);
        prev = file.readInt();
        next = file.readInt();
    }

    public void write( RandomAccessFile file ) throws IOException {
        record.write(file);
        file.writeInt( prev );
        file.writeInt( next );
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public int getPrev() {
        return prev;
    }

    public void setPrev(int prev) {
        this.prev = prev;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
    public static int size() { return 54; }

    @Override
    public String toString() {
        return "Prev: " + prev + " "+"Next: "+ next+ " "+record  ;
    }

}