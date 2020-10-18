import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

//Reference - https://github.com/alexholmes/hadoop-book/blob/master/src/main/java/com/manning/hip/ch3/TextArrayWritable.java

public class TextArrayWritable extends ArrayWritable {
    public TextArrayWritable() {
        super(Text.class);
    }

    public TextArrayWritable(Text[] strings) {
        super(Text.class, strings);
    }
    @Override
    public Text[] get() {
        return (Text[]) super.get();
    }

    @Override
    public void write(DataOutput arg0) throws IOException {
        for(Text i : get()){
            i.write(arg0);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(get());
    }
}